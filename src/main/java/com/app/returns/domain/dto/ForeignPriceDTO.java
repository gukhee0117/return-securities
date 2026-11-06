package com.app.returns.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ForeignPriceDTO {
    // MyBatis field injection용 조회 DTO
    private String ticker;
    private BigDecimal previousClose;
    private LocalDate previousCloseDate;
}
