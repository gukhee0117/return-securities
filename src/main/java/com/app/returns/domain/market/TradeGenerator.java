package com.app.returns.domain.market;

import com.app.returns.domain.client.MariaOrderClient;
import com.app.returns.domain.client.MariaOrderResult;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * 1~4초마다 무작위 고객 한 명의 매수/매도를 maria에 보내고, 체결되면 증권사 거래 기록을 남기고 그 종목 가격을 민다. 종목·수량은 maria가 고른다. 매번 무작위
 * 간격으로 다시 예약하고, 어떤 예외도 다음 예약을 막지 않는다.
 */
@Slf4j
@Component
public class TradeGenerator {
    private final DomesticTradeMapper mapper;
    private final MariaOrderClient client;
    private final PriceFactorStore factorStore;
    private final TaskScheduler scheduler;
    private final Clock clock;
    private final MarketTradeProperties properties;
    private final Random random;

    private volatile List<TradeCandidate> candidates = List.of();

    @Autowired
    public TradeGenerator(
            DomesticTradeMapper mapper,
            MariaOrderClient client,
            PriceFactorStore factorStore,
            TaskScheduler scheduler,
            Clock clock,
            MarketTradeProperties properties) {
        this(mapper, client, factorStore, scheduler, clock, properties, new Random());
    }

    TradeGenerator(
            DomesticTradeMapper mapper,
            MariaOrderClient client,
            PriceFactorStore factorStore,
            TaskScheduler scheduler,
            Clock clock,
            MarketTradeProperties properties,
            Random random) {
        this.mapper = mapper;
        this.client = client;
        this.factorStore = factorStore;
        this.scheduler = scheduler;
        this.clock = clock;
        this.properties = properties;
        this.random = random;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!properties.isEnabled()) {
            return;
        }
        candidates =
                mapper.selectTradeCandidates().stream()
                        .filter(c -> !properties.getExcludedCiHashes().contains(c.getCiHash()))
                        .toList();
        log.info("거래 생성기 시작: 후보 고객 {}명", candidates.size());
        scheduleNext();
    }

    void tick() {
        try {
            runOnce();
        } catch (Exception e) {
            log.warn("거래 생성 실패(다음 실행은 예약한다): {}", e.toString());
        } finally {
            scheduleNext();
        }
    }

    /** 체결 1건이 목표. 거절이면 다른 고객으로 최대 maxAttempts번. */
    void runOnce() {
        if (candidates.isEmpty()) {
            return;
        }
        for (int attempt = 1; attempt <= properties.getMaxAttempts(); attempt++) {
            TradeIntent intent = TradePicker.pick(candidates, properties.getBuyRatio(), random);
            String key = "GEN-" + clock.millis() + "-" + attempt;
            Optional<MariaOrderResult> result = send(intent, key);
            if (result.isEmpty()) {
                return;
            }
            if (result.get().executed()) {
                record(intent, result.get());
                return;
            }
        }
    }

    /** 타임아웃·5xx면 같은 멱등키로 한 번 더. 그래도 안 되면 이번 실행은 포기한다. */
    private Optional<MariaOrderResult> send(TradeIntent intent, String key) {
        for (int call = 1; call <= 2; call++) {
            try {
                return Optional.of(
                        client.order(intent.candidate().getCiHash(), intent.side(), key));
            } catch (ResourceAccessException e) {
                log.warn("maria 주문 응답 없음(시도 {}/2)", call);
            } catch (RestClientResponseException e) {
                if (!e.getStatusCode().is5xxServerError()) {
                    log.warn("maria 주문 거절 status={}", e.getStatusCode().value());
                    return Optional.empty();
                }
                log.warn("maria 주문 5xx(시도 {}/2)", call);
            }
        }
        return Optional.empty();
    }

    private void record(TradeIntent intent, MariaOrderResult order) {
        try {
            mapper.insertGeneratedTrade(
                    intent.candidate().getGeneralCustomerId(),
                    intent.side(),
                    order.ticker(),
                    order.qty(),
                    order.price(),
                    order.processedAt(),
                    order.domesticOrderId());
        } catch (DuplicateKeyException e) {
            return; // 같은 주문이 이미 기록됨(재전송) — 가격은 한 번만 민다
        }
        factorStore.update(
                order.ticker(),
                current -> PriceFactorModel.push(current, "BUY".equals(intent.side())));
    }

    private void scheduleNext() {
        long min = properties.getMinDelayMs();
        long delay = min + (long) (random.nextDouble() * (properties.getMaxDelayMs() - min));
        scheduler.schedule(this::tick, Instant.now(clock).plusMillis(delay));
    }
}
