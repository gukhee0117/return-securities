package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomesticPriceRequestDTO {
    @NotEmpty(message = "조회할 종목코드를 입력해 주세요.")
    @Size(max = 50, message = "종목은 한 번에 50개까지 조회할 수 있습니다.")
    private List<@Pattern(regexp = "[0-9A-Z]{6}", message = "종목코드는 영문 대문자·숫자 6자리여야 합니다.") String>
            tickers;
}
