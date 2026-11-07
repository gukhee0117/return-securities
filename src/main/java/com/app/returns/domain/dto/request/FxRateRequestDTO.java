package com.app.returns.domain.dto.request;

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
public class FxRateRequestDTO {
    @NotEmpty(message = "조회할 통화를 입력해 주세요.")
    @Size(max = 20, message = "통화는 한 번에 20개까지 조회할 수 있습니다.")
    private List<@Pattern(regexp = "[A-Z]{3}", message = "통화는 영문 대문자 3자리여야 합니다.") String>
            currencies;

    @NotNull(message = "기준일(asOf)을 입력해 주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate asOf;
}
