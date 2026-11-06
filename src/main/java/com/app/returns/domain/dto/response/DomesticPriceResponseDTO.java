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
        ticker = price.getTicker();
        priceKrw = price.getPriceKrw();
        previousCloseKrw = price.getPreviousCloseKrw();
        baseDate = price.getBaseDate();
        priceAt = price.getPriceAt();
        source = price.getSource();
    }
}
