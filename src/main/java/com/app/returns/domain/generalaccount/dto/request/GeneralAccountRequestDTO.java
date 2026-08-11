package com.app.returns.domain.generalaccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
@Builder

public class GeneralAccountRequestDTO {
    @NotBlank(message = "ciHash는 필수입니다.")
    private String ciHash;

    @NotNull(message = "generalAccountId는 필수입니다.")
    @Positive(message = "generalAccountId는 양수여야 합니다.")
    private Long generalAccountId;
}

