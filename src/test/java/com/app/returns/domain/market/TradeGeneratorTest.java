package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.returns.domain.client.MariaOrderClient;
import com.app.returns.domain.client.MariaOrderResult;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.client.ResourceAccessException;

class TradeGeneratorTest {
    private static final String SHOWCASE_CI = "d".repeat(64);
    private static final LocalDateTime PROCESSED_AT = LocalDateTime.of(2026, 11, 18, 10, 0);

    DomesticTradeMapper mapper = mock(DomesticTradeMapper.class);
    MariaOrderClient client = mock(MariaOrderClient.class);
    TaskScheduler scheduler = mock(TaskScheduler.class);
    PriceFactorStore store = new PriceFactorStore();
    MarketTradeProperties properties = new MarketTradeProperties();
    TradeGenerator generator;

    @BeforeEach
    void setUp() {
        properties.setEnabled(true);
        properties.setMinDelayMs(1000);
        properties.setMaxDelayMs(4000);
        properties.setBuyRatio(1.0); // 매수만 나오게 해서 방향을 고정한다
        properties.setMaxAttempts(5);
        properties.setExcludedCiHashes(Set.of(SHOWCASE_CI));
        when(mapper.selectTradeCandidates())
                .thenReturn(
                        List.of(
                                new TradeCandidate(1L, "a".repeat(64)),
                                new TradeCandidate(2L, SHOWCASE_CI)));
        generator =
                new TradeGenerator(
                        mapper,
                        client,
                        store,
                        scheduler,
                        Clock.fixed(Instant.parse("2026-11-18T01:00:00Z"), ZoneId.of("Asia/Seoul")),
                        properties,
                        new Random(1));
        generator.start();
    }

    private static MariaOrderResult executed(long orderId) {
        return new MariaOrderResult(
                "EXECUTED",
                null,
                orderId,
                "005930",
                BigDecimal.TEN,
                new BigDecimal("85500"),
                new BigDecimal("-855034"),
                PROCESSED_AT);
    }

    private static MariaOrderResult rejected() {
        return new MariaOrderResult("REJECTED", "NO_ACCOUNT", null, null, null, null, null, null);
    }

    @Test
    void 시작하면_제외_고객을_빼고_첫_실행을_예약한다() {
        verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
        when(client.order(anyString(), anyString(), anyString())).thenReturn(rejected());
        generator.runOnce();
        ArgumentCaptor<String> ci = ArgumentCaptor.forClass(String.class);
        verify(client, org.mockito.Mockito.atLeastOnce())
                .order(ci.capture(), anyString(), anyString());
        assertThat(ci.getAllValues()).doesNotContain(SHOWCASE_CI);
    }

    @Test
    void 꺼져_있으면_예약하지_않는다() {
        var off = new MarketTradeProperties();
        off.setEnabled(false);
        var scheduler2 = mock(TaskScheduler.class);
        new TradeGenerator(mapper, client, store, scheduler2, Clock.systemUTC(), off, new Random())
                .start();
        verify(scheduler2, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void 체결되면_거래를_기록하고_가격을_민다() {
        when(client.order(anyString(), eq("BUY"), anyString())).thenReturn(executed(11L));

        generator.runOnce();

        verify(mapper)
                .insertGeneratedTrade(
                        eq(1L),
                        eq("BUY"),
                        eq("005930"),
                        eq(BigDecimal.TEN),
                        eq(new BigDecimal("85500")),
                        eq(PROCESSED_AT),
                        eq(11L));
        assertThat(store.get("005930")).isEqualTo(1.003);
    }

    @Test
    void 타임아웃이면_같은_키로_한번_더_보낸다() {
        when(client.order(anyString(), anyString(), anyString()))
                .thenThrow(new ResourceAccessException("timeout"))
                .thenReturn(executed(12L));

        generator.runOnce();

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(client, times(2)).order(anyString(), anyString(), key.capture());
        assertThat(key.getAllValues().get(0))
                .startsWith("GEN-")
                .isEqualTo(key.getAllValues().get(1));
        verify(mapper)
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), eq(12L));
    }

    @Test
    void 재전송도_실패하면_이번_실행을_끝낸다() {
        when(client.order(anyString(), anyString(), anyString()))
                .thenThrow(new ResourceAccessException("timeout"));

        generator.runOnce();

        verify(client, times(2)).order(anyString(), anyString(), anyString());
        verify(mapper, never())
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), anyLong());
        assertThat(store.get("005930")).isEqualTo(1.0);
    }

    @Test
    void 이미_기록된_거래면_가격을_밀지_않는다() {
        when(client.order(anyString(), anyString(), anyString())).thenReturn(executed(13L));
        doThrow(new DuplicateKeyException("uk_domestic_trade_maria_order"))
                .when(mapper)
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), eq(13L));

        generator.runOnce();

        assertThat(store.get("005930")).isEqualTo(1.0);
    }

    @Test
    void 거절이면_다른_시도로_다시_하고_다섯번_모두_거절이면_멈춘다() {
        when(client.order(anyString(), anyString(), anyString())).thenReturn(rejected());

        generator.runOnce();

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(client, times(5)).order(anyString(), anyString(), key.capture());
        assertThat(key.getAllValues()).doesNotHaveDuplicates();
        verify(mapper, never())
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), anyLong());
    }

    @Test
    void 거절_뒤_체결되면_거기서_멈춘다() {
        when(client.order(anyString(), anyString(), anyString()))
                .thenReturn(rejected(), rejected(), executed(14L));

        generator.runOnce();

        verify(client, times(3)).order(anyString(), anyString(), anyString());
        verify(mapper, times(1))
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), eq(14L));
    }

    @Test
    void 예외가_나도_다음_실행을_예약한다() {
        when(client.order(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("boom"));

        generator.tick();

        // start()의 첫 예약 + tick 뒤 다음 예약
        verify(scheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void 예약_간격은_최소와_최대_사이() {
        for (int i = 0; i < 200; i++) {
            generator.tick();
        }
        ArgumentCaptor<Instant> at = ArgumentCaptor.forClass(Instant.class);
        verify(scheduler, times(201)).schedule(any(Runnable.class), at.capture());
        Instant now = Instant.parse("2026-11-18T01:00:00Z");
        assertThat(at.getAllValues())
                .allMatch(
                        t -> !t.isBefore(now.plusMillis(1000)) && !t.isAfter(now.plusMillis(4000)));
    }
}
