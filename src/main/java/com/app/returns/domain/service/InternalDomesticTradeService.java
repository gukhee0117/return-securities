package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.InternalDomesticTradeRequestDTO;

public interface InternalDomesticTradeService {

    /** maria 주문 한 건을 증권사 거래로 기록한다. 같은 maria 주문번호가 이미 있으면 다시 기록하지 않고 성공으로 본다. */
    void record(InternalDomesticTradeRequestDTO request);
}
