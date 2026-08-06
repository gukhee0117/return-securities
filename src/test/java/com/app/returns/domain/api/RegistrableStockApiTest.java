package com.app.returns.domain.api;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.service.RegistrableStockService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrableStockApiTest {

    private RegistrableStockService registrableStockService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registrableStockService = mock(RegistrableStockService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RegistrableStockApi(registrableStockService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRegistrableStockAcceptsValidRequest() throws Exception {
        RegistrableStockResponseDTO response = RegistrableStockResponseDTO.builder()
                .heldQty(BigDecimal.valueOf(100))
                .sourceBroker(null)
                .purchaseDate(LocalDateTime.now())
                .purchasePrice(BigDecimal.valueOf(150.25))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1320.5))
                .build();
        when(registrableStockService.findHeldQty(any())).thenReturn(response);

        mockMvc.perform(get("/registrable-stocks")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("generalAccountId", "1")
                        .param("foreignProductId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"))
                .andExpect(jsonPath("$.data.heldQty").value(100));

        verify(registrableStockService).findHeldQty(any());
    }

    @Test
    void getRegistrableStockRejectsMissingGeneralAccountId() throws Exception {
        mockMvc.perform(get("/registrable-stocks")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("foreignProductId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("generalAccountId는 필수입니다."));

        verify(registrableStockService, never()).findHeldQty(any());
    }

    @Test
    void getRegistrableStockRejectsMissingForeignProductId() throws Exception {
        mockMvc.perform(get("/registrable-stocks")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("generalAccountId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("foreignProductId는 필수입니다."));

        verify(registrableStockService, never()).findHeldQty(any());
    }

    @Test
    void getRegistrableStockReturnsNotFoundWhenStockMissing() throws Exception {
        when(registrableStockService.findHeldQty(any()))
                .thenThrow(new RegistrableStockNotFoundException("등록가능 보유수량 조회 실패"));

        mockMvc.perform(get("/registrable-stocks")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("generalAccountId", "1")
                        .param("foreignProductId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("등록가능 보유수량 조회 실패"));
    }
}