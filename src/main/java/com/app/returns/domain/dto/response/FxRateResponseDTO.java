package com.app.returns.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 통화 1단위 기준 원화 환율(JPY도 1엔). */
@Getter
@AllArgsConstructor
public class FxRateResponseDTO {
    private final String currency;
    private final BigDecimal baseRate;
    private final BigDecimal finalRate;
    private final LocalDate rateDate;
}
