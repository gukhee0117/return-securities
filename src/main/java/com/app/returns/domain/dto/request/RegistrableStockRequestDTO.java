package com.app.returns.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class RegistrableStockRequestDTO {
    private Long generalAccountId;
    private Long foreignProductId;
}
