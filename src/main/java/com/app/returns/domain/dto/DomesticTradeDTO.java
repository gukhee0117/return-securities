package com.app.returns.domain.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class DomesticTradeDTO {
    private Long tradeId;
    private Long generalAccountId;
    private String tradeType;
    private String stockCode;
    private BigDecimal qty;
    private BigDecimal price;
    private LocalDateTime executedAt;
}
