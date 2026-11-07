package com.app.returns.domain.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** maria 내부 주문 API 응답의 data. 주문을 넣기 전에 거절되면 domesticOrderId 등은 null이다. */
public record MariaOrderResult(
        String status,
        String rejectReason,
        Long domesticOrderId,
        String ticker,
        BigDecimal qty,
        BigDecimal price,
        BigDecimal amount,
        LocalDateTime processedAt) {

    public boolean executed() {
        return "EXECUTED".equals(status);
    }
}
