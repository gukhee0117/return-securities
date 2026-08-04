package com.app.returns.domain.dto;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class GeneralCustomerDTO {
    private Long generalCustomerId;
    private String ciHash;
    private String name;
    private LocalDate birthDate;
}
