package com.app.returns.domain.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder

public class RegistrableStockDTO {
    private Long registrableStockId;
    private Long domesticAccountId;
    private Long foreignProductId;
    private BigDecimal heldQty;
    private String sourceBroker;
    private LocalDateTime recordedAt;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private Long purchaseFxRate;
}
