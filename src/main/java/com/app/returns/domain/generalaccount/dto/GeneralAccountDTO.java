package com.app.returns.domain.generalaccount.dto;

import com.app.returns.domain.generalaccount.type.GeneralStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class GeneralAccountDTO {
    private Long generalAccountId;
    private Long generalCustomerId;
    private String accountNo;
    private String accountType;
    private GeneralStatus status;
}
