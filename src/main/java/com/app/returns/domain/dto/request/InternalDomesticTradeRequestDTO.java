package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** maria가 체결한 고객 주문을 증권사 거래로 남기라는 요청. 체결 시각은 maria 업무시계 값이다. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InternalDomesticTradeRequestDTO {
    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F]{64}", message = "ciHash 형식이 올바르지 않습니다.")
    private String ciHash;

    @NotBlank
    @Pattern(regexp = "BUY|SELL", message = "side는 BUY 또는 SELL이어야 합니다.")
    private String side;

    @NotBlank
    @Pattern(regexp = "[0-9A-Z]{6}", message = "종목코드는 영문 대문자·숫자 6자리여야 합니다.")
    private String ticker;

    @NotNull @Positive private BigDecimal qty;
    @NotNull @Positive private BigDecimal price;
    @NotNull private LocalDateTime executedAt;
    @NotNull @Positive private Long mariaOrderId;
}
