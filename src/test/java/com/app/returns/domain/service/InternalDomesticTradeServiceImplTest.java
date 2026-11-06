package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.returns.domain.dto.request.InternalDomesticTradeRequestDTO;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import com.app.returns.domain.member.exception.MemberNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class InternalDomesticTradeServiceImplTest {
    private DomesticTradeMapper mapper;
    private InternalDomesticTradeServiceImpl service;

    private static InternalDomesticTradeRequestDTO request() {
        return new InternalDomesticTradeRequestDTO(
                "a".repeat(64),
                "SELL",
                "005930",
                new BigDecimal("10"),
                new BigDecimal("87500"),
                LocalDateTime.of(2026, 11, 18, 10, 0),
                77L);
    }

    @BeforeEach
    void setUp() {
        mapper = mock(DomesticTradeMapper.class);
        service = new InternalDomesticTradeServiceImpl(mapper);
    }

    @Test
    void 고객을_찾아_maria_주문번호와_함께_기록한다() {
        when(mapper.selectGeneralCustomerIdByCiHash("a".repeat(64))).thenReturn(Optional.of(9L));

        service.record(request());

        verify(mapper)
                .insertGeneratedTrade(
                        eq(9L),
                        eq("SELL"),
                        eq("005930"),
                        eq(new BigDecimal("10")),
                        eq(new BigDecimal("87500")),
                        eq(LocalDateTime.of(2026, 11, 18, 10, 0)),
                        eq(77L));
    }

    @Test
    void 이미_기록된_주문번호면_성공으로_본다() {
        when(mapper.selectGeneralCustomerIdByCiHash(anyString())).thenReturn(Optional.of(9L));
        doThrow(new DuplicateKeyException("uk_domestic_trade_maria_order"))
                .when(mapper)
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), eq(77L));

        service.record(request()); // 예외 없이 끝난다
    }

    @Test
    void 모르는_고객이면_기록하지_않고_예외() {
        when(mapper.selectGeneralCustomerIdByCiHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.record(request()))
                .isInstanceOf(MemberNotFoundException.class);
        org.mockito.Mockito.verify(mapper, org.mockito.Mockito.never())
                .insertGeneratedTrade(
                        anyLong(), anyString(), anyString(), any(), any(), any(), anyLong());
    }
}
