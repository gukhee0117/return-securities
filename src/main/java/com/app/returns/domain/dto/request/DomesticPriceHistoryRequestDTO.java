package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/** 종목코드(tickers)와 기준일(asOf) 규칙은 {@link DomesticPriceRequestDTO}와 같고, 가져올 일수(days)만 더한다. */
@Getter
@Setter
public class DomesticPriceHistoryRequestDTO extends DomesticPriceRequestDTO {
    public static final int DEFAULT_DAYS = 30;
    public static final int MAX_DAYS = 60;

    /** 종목마다 가져올 최근 영업일(종가) 수. */
    @Min(value = 1, message = "일수는 1 이상이어야 합니다.")
    @Max(value = MAX_DAYS, message = "일수는 60까지 조회할 수 있습니다.")
    private int days = DEFAULT_DAYS;
}
