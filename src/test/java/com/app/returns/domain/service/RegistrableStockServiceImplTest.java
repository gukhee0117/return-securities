package com.app.returns.domain.service;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrableStockServiceImplTest {

    @Mock
    RegistrableStockMapper registrableStockMapper;

    @InjectMocks
    RegistrableStockServiceImpl registrableStockService;

    @Test
    void findHeldQty_정상요청이면_결과를_반환한다() {
        Long generalAccountId = 1L;
        Long foreignProductId = 1L;

        RegistrableStockDTO dto = RegistrableStockDTO.builder()
                .registrableStockId(1L)
                .generalAccountId(generalAccountId)
                .foreignProductId(foreignProductId)
                .heldQty(BigDecimal.valueOf(100))
                .purchaseDate(LocalDateTime.now())
                .purchasePrice(BigDecimal.valueOf(150.25))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1320.5))
                .build();

        when(registrableStockMapper.findHeldQty(any(RegistrableStockRequestDTO.class)))
                .thenReturn(Optional.of(dto));

        RegistrableStockResponseDTO result = registrableStockService.findHeldQty(generalAccountId, foreignProductId);

        assertThat(result.getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getPurchaseCurrency()).isEqualTo("USD");
    }

    @Test
    void findHeldQty_generalAccountId가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> registrableStockService.findHeldQty(null, 1L))
                .isInstanceOf(RegistrableStockException.class);

        verifyNoInteractions(registrableStockMapper);
    }

    @Test
    void findHeldQty_foreignProductId가_null이면_예외를_던진다() {
        assertThatThrownBy(() -> registrableStockService.findHeldQty(1L, null))
                .isInstanceOf(RegistrableStockException.class);

        verifyNoInteractions(registrableStockMapper);
    }

    @Test
    void findHeldQty_조회결과가_없으면_NotFoundException을_던진다() {
        Long generalAccountId = 1L;
        Long foreignProductId = 1L;

        when(registrableStockMapper.findHeldQty(any(RegistrableStockRequestDTO.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrableStockService.findHeldQty(generalAccountId, foreignProductId))
                .isInstanceOf(RegistrableStockNotFoundException.class);
    }
}