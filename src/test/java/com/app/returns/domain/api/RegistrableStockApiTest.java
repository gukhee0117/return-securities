package com.app.returns.domain.api;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.service.RegistrableStockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrableStockApi.class)
class RegistrableStockApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RegistrableStockService registrableStockService;

    @Test
    void findHeldQty_정상요청이면_200을_반환한다() throws Exception {
        RegistrableStockResponseDTO responseDTO = RegistrableStockResponseDTO.builder()
                .heldQty(BigDecimal.valueOf(100))
                .purchaseCurrency("USD")
                .build();

        when(registrableStockService.findHeldQty(1L, 1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/registrable-stocks")
                        .param("generalAccountId", "1")
                        .param("foreignProductId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.heldQty").value(100));
    }

    @Test
    void findHeldQty_존재하지않으면_404를_반환한다() throws Exception {
        when(registrableStockService.findHeldQty(1L, 1L))
                .thenThrow(new RegistrableStockNotFoundException("등록가능 보유수량 조회 실패"));

        mockMvc.perform(get("/api/registrable-stocks")
                        .param("generalAccountId", "1")
                        .param("foreignProductId", "1"))
                .andExpect(status().isNotFound());
    }
}