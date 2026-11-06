package com.app.returns.domain.market;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 거래를 만들 수 있는 고객(국내 거래 이력이 있는 증권사 고객). */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TradeCandidate {
    // MyBatis field injection용 조회 DTO
    private Long generalCustomerId;
    private String ciHash;
}
