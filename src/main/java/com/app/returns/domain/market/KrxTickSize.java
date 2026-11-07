package com.app.returns.domain.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 호가 가격 단위로 반올림한다.
 *
 * <p>주식(유가증권·코스닥 통일, 2023-01-25 시행): 한화투자증권 안내문 "주식시장 및 파생상품시장 호가단위 등 변경사항 안내" (2023-01-10, 근거 =
 * 유가증권시장·코스닥시장 업무규정 시행세칙) 1쪽 표. 2천원 미만 1원 / 5천원 미만 5원 / 2만원 미만 10원 / 5만원 미만 50원 / 20만원 미만 100원 /
 * 50만원 미만 500원 / 50만원 이상 1,000원. 삼성증권 호가단위 안내 표와도 같다.
 *
 * <p>ETF·ETN: 2023-01-25에는 가격과 무관하게 5원. 이후 거래소가 2천원 미만만 1원으로 낮추도록 시행세칙을 개정했다(2023-10-05 예고, 12월 시행
 * 예정 — 비즈워치 2023-10-05 기사). 2천원 미만 1원, 그 이상 5원.
 *
 * <p>KRX 규정 원문(regulation.krx.co.kr)은 자바스크립트 화면이라 직접 읽지 못했고 위 증권사·언론 자료로 확인했다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KrxTickSize {
    private static final int[][] STOCK_BANDS = {
        {2_000, 1}, {5_000, 5}, {20_000, 10}, {50_000, 50}, {200_000, 100}, {500_000, 500}
    };
    private static final int STOCK_TOP_TICK = 1_000;
    private static final int ETF_LOW_PRICE_LIMIT = 2_000;

    public static BigDecimal round(BigDecimal price, boolean etf) {
        BigDecimal tick = BigDecimal.valueOf(tickOf(price, etf));
        return price.divide(tick, 0, RoundingMode.HALF_UP).multiply(tick);
    }

    private static int tickOf(BigDecimal price, boolean etf) {
        if (etf) {
            return price.compareTo(BigDecimal.valueOf(ETF_LOW_PRICE_LIMIT)) < 0 ? 1 : 5;
        }
        for (int[] band : STOCK_BANDS) {
            if (price.compareTo(BigDecimal.valueOf(band[0])) < 0) {
                return band[1];
            }
        }
        return STOCK_TOP_TICK;
    }
}
