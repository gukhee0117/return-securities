package com.app.returns.domain.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.app.returns.domain.service.DomesticPriceService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DomesticPriceApiTest {
    private DomesticPriceService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(DomesticPriceService.class);
        mvc =
                MockMvcBuilders.standaloneSetup(new DomesticPriceApi(service))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void commaSeparatedTickersAreBound() throws Exception {
        when(service.findLatest(List.of("005930", "000660"))).thenReturn(List.of());
        mvc.perform(get("/api/domestic-prices").param("tickers", "005930,000660"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("현재가 조회 성공"))
                .andExpect(jsonPath("$.data").isEmpty());
        verify(service).findLatest(List.of("005930", "000660"));
    }

    @Test
    void missingInvalidAndOversizedRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/domestic-prices")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/domestic-prices").param("tickers", "invalid"))
                .andExpect(status().isBadRequest());
        String tooMany =
                IntStream.range(0, 51)
                        .mapToObj(i -> String.format("%06d", i))
                        .collect(Collectors.joining(","));
        mvc.perform(get("/api/domestic-prices").param("tickers", tooMany))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
