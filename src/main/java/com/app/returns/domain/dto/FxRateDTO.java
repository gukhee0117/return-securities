package com.app.returns.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FxRateDTO {
    // MyBatis field injection용 조회 DTO
    private String currency;
    private BigDecimal baseRate;
    private BigDecimal finalRate;
    private LocalDate rateDate;
}
