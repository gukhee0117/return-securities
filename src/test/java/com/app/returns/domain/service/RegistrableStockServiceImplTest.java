package com.app.returns.domain.service;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.mapper.RegistrableStockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrableStockServiceImplTest {

    @Mock
    private RegistrableStockMapper registrableStockMapper;

    @InjectMocks
    private RegistrableStockServiceImpl registrableStockService;

    @Test
    void findHeldQtyReturnsResponseWhenStockExists() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .generalAccountId(1L)
                .foreignProductId(1L)
                .build();

        RegistrableStockDTO dto = RegistrableStockDTO.builder()
                .registrableStockId(1L)
                .generalAccountId(1L)
                .foreignProductId(1L)
                .heldQty(BigDecimal.valueOf(100))
                .sourceBroker(null)
                .recordedAt(LocalDateTime.now())
                .purchaseDate(LocalDateTime.now())
                .purchasePrice(BigDecimal.valueOf(150.25))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1320.5))
                .build();
        when(registrableStockMapper.findHeldQty(request)).thenReturn(Optional.of(dto));

        RegistrableStockResponseDTO result = registrableStockService.findHeldQty(request);

        assertThat(result.getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getPurchaseCurrency()).isEqualTo("USD");
    }

    @Test
    void findHeldQtyThrowsNotFoundExceptionWhenStockDoesNotExist() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .generalAccountId(1L)
                .foreignProductId(1L)
                .build();

        when(registrableStockMapper.findHeldQty(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrableStockService.findHeldQty(request))
                .isInstanceOf(RegistrableStockNotFoundException.class)
                .hasMessage("등록가능 보유수량 조회 실패");
    }
}