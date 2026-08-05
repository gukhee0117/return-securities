package com.app.returns.domain.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class GeneralAccountDTO {
    private Long generalAccountId;
    private Long generalCustomerId;
    private String accountNo;
    private String accountType;
    private String status;
}
