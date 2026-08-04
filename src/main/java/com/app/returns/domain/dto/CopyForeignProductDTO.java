package com.app.returns.domain.dto;

import com.app.returns.domain.type.Type;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class CopyForeignProductDTO {
    private Long foreignProductId;
    private String ticker;
    private String name;
    private String market;
    private String currency;
    private Type type;
}