package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.InternalDomesticTradeRequestDTO;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import com.app.returns.domain.member.exception.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalDomesticTradeServiceImpl implements InternalDomesticTradeService {
    private final DomesticTradeMapper mapper;

    @Override
    public void record(InternalDomesticTradeRequestDTO request) {
        Long customerId =
                mapper.selectGeneralCustomerIdByCiHash(request.getCiHash())
                        .orElseThrow(() -> new MemberNotFoundException("증권사 고객을 찾을 수 없습니다."));
        try {
            mapper.insertGeneratedTrade(
                    customerId,
                    request.getSide(),
                    request.getTicker(),
                    request.getQty(),
                    request.getPrice(),
                    request.getExecutedAt(),
                    request.getMariaOrderId());
        } catch (DuplicateKeyException e) {
            // 재전송으로 이미 기록된 주문 — 성공과 같다
        }
    }
}
