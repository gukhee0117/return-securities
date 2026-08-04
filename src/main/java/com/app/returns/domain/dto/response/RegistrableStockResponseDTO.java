package com.app.returns.domain.dto.response;

import com.app.returns.domain.dto.RegistrableStockDTO;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class RegistrableStockResponseDTO {

    private BigDecimal heldQty;
    private String sourceBroker;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;

    public RegistrableStockResponseDTO(RegistrableStockDTO dto) {
        this.heldQty = dto.getHeldQty() != null ? dto.getHeldQty() : null;
        this.sourceBroker = dto.getSourceBroker() != null ? dto.getSourceBroker() : null;
        this.purchaseDate = dto.getPurchaseDate() != null ? dto.getPurchaseDate() : null;
        this.purchasePrice = dto.getPurchasePrice() != null ? dto.getPurchasePrice() : null;
        this.purchaseCurrency = dto.getPurchaseCurrency() != null ? dto.getPurchaseCurrency() : null;
        this.purchaseFxRate = dto.getPurchaseFxRate() != null ? dto.getPurchaseFxRate() : null;
    }
}
