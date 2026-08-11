package com.app.returns.domain.generalaccount.dto.response;

import com.app.returns.domain.generalaccount.dto.GeneralAccountDTO;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
@Builder
public class GeneralAccountResponseDTO {
    private Long generalAccountId;
    private String accountNo;
    private GeneralStatus status;

    public GeneralAccountResponseDTO(GeneralAccountDTO dto){
        this.generalAccountId = dto.getGeneralAccountId();
        this.accountNo = dto.getAccountNo();
        this.status = dto.getStatus();

    }
}
