package com.app.returns.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 해외 종목의 asOf 이전 가장 최근 종가(외화 기준). */
@Getter
@AllArgsConstructor
public class ForeignPriceResponseDTO {
    private final String ticker;
    private final BigDecimal previousClose;
    private final LocalDate previousCloseDate;
}
