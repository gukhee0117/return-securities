package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class DomesticPriceHistoryRequestDTO {
    public static final int DEFAULT_DAYS = 30;
    public static final int MAX_DAYS = 60;

    @NotEmpty(message = "조회할 종목코드를 입력해 주세요.")
    @Size(max = 50, message = "종목은 한 번에 50개까지 조회할 수 있습니다.")
    private List<@Pattern(regexp = "[0-9A-Z]{6}", message = "종목코드는 영문 대문자·숫자 6자리여야 합니다.") String>
            tickers;

    @NotNull(message = "기준일(asOf)을 입력해 주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate asOf;

    /** 종목마다 가져올 최근 영업일(종가) 수. */
    @Min(value = 1, message = "일수는 1 이상이어야 합니다.")
    @Max(value = MAX_DAYS, message = "일수는 60까지 조회할 수 있습니다.")
    private int days = DEFAULT_DAYS;
}
