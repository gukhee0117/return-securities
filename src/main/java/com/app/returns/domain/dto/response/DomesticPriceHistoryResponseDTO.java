package com.app.returns.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 종목별 asOf 당일까지(당일 포함) 최근 일별 종가(저장된 값 그대로, 날짜 오름차순). 종목은 종목코드 오름차순으로 나온다. 현재가 계산에 쓰는 배수는 곱하지 않는다.
 */
@Getter
@AllArgsConstructor
public class DomesticPriceHistoryResponseDTO {
    private final String ticker;
    private final List<Point> points;

    @Getter
    @AllArgsConstructor
    public static class Point {
        private final LocalDate baseDate;
        private final BigDecimal closeKrw;
    }
}
