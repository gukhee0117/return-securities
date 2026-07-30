package com.app.returns.domain.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class DomesticAccountDTO {
    private Long domesticAccountId;
    private String accountName;
}
