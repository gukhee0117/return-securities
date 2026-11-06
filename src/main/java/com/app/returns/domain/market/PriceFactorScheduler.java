package com.app.returns.domain.market;

import com.app.returns.domain.mapper.DomesticPriceMapper;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 일정 간격으로 모든 국내 종목의 배수를 한 걸음씩 움직인다. DB에는 쓰지 않는다. */
@Component
@RequiredArgsConstructor
public class PriceFactorScheduler {
    private final DomesticPriceMapper mapper;
    private final PriceFactorStore store;
    private final Random random = new Random();
    private volatile List<String> stockCodes;

    @Scheduled(fixedDelayString = "${market.factor.interval-ms:3000}")
    public void tick() {
        if (stockCodes == null) {
            stockCodes = mapper.selectStockCodes();
        }
        for (String code : stockCodes) {
            store.update(code, current -> PriceFactorModel.next(current, random));
        }
    }
}
