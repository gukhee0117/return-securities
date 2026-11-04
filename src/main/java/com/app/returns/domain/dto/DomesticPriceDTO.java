package com.app.returns.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DomesticPriceDTO {
    private String ticker;
    private BigDecimal priceKrw;
    private LocalDate baseDate;
    private LocalDateTime priceAt;
    private String source;
}
