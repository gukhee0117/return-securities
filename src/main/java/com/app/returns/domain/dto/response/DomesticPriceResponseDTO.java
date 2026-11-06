package com.app.returns.domain.dto.response;

import com.app.returns.domain.dto.DomesticPriceDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

/** 프로젝트 저장 가격. priceAt은 해당 가격 행의 저장·갱신 시각이며 실제 시장 체결시각이 아니다. */
@Getter
public class DomesticPriceResponseDTO {
    private final String ticker;
    private final BigDecimal priceKrw;
    private final BigDecimal previousCloseKrw;
    private final LocalDate baseDate;
    private final LocalDateTime priceAt;
    private final String source;

    public DomesticPriceResponseDTO(DomesticPriceDTO price) {
        this(price, price.getPriceKrw());
    }

    /** 저장 종가가 아니라 계산한 현재가를 내려 줄 때. 전일 종가·기준일·저장 시각은 행 그대로. */
    public DomesticPriceResponseDTO(DomesticPriceDTO price, BigDecimal currentPriceKrw) {
        ticker = price.getTicker();
        priceKrw = currentPriceKrw;
        previousCloseKrw = price.getPreviousCloseKrw();
        baseDate = price.getBaseDate();
        priceAt = price.getPriceAt();
        source = price.getSource();
    }
}
