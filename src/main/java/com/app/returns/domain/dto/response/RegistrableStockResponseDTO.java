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
        this.heldQty = dto.getHeldQty();
        this.sourceBroker = dto.getSourceBroker();
        this.purchaseDate = dto.getPurchaseDate();
        this.purchasePrice = dto.getPurchasePrice();
        this.purchaseCurrency = dto.getPurchaseCurrency();
        this.purchaseFxRate = dto.getPurchaseFxRate();
    }
}
