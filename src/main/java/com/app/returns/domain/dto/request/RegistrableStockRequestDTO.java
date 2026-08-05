package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class RegistrableStockRequestDTO {

    @NotNull(message = "generalAccountId는 필수입니다.")
    private Long generalAccountId;

    @NotNull(message = "foreignProductId는 필수입니다.")
    private Long foreignProductId;
}
