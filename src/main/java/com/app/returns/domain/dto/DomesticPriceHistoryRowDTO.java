package com.app.returns.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DomesticPriceHistoryRowDTO {
    // MyBatis field injection용 조회 DTO: 종목별 일별 종가 한 행
    private String ticker;
    private LocalDate baseDate;
    private BigDecimal closeKrw;
}
