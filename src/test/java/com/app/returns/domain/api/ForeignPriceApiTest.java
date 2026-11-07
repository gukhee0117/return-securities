package com.app.returns.domain.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.app.returns.domain.dto.response.ForeignPriceResponseDTO;
import com.app.returns.domain.dto.response.FxRateResponseDTO;
import com.app.returns.domain.service.ForeignPriceService;
import com.app.returns.domain.service.FxRateService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ForeignPriceApiTest {
    private ForeignPriceService priceService;
    private FxRateService fxService;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        priceService = mock(ForeignPriceService.class);
        fxService = mock(FxRateService.class);
        mvc =
                MockMvcBuilders.standaloneSetup(
                                new ForeignPriceApi(priceService), new FxRateApi(fxService))
                        .setMessageConverters(
                                new MappingJackson2HttpMessageConverter(
                                        Jackson2ObjectMapperBuilder.json()
                                                .featuresToDisable(
                                                        SerializationFeature
                                                                .WRITE_DATES_AS_TIMESTAMPS)
                                                .build()))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void foreignPriceFieldNames() throws Exception {
        var day = LocalDate.of(2026, 11, 17);
        when(priceService.findPreviousCloses(List.of("QQQ", "BRK.B"), day))
                .thenReturn(
                        List.of(
                                new ForeignPriceResponseDTO(
                                        "QQQ",
                                        new BigDecimal("600.5"),
                                        LocalDate.of(2026, 11, 16))));
        mvc.perform(
                        get("/api/foreign-prices")
                                .param("tickers", "QQQ,BRK.B")
                                .param("asOf", "2026-11-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].ticker").value("QQQ"))
                .andExpect(jsonPath("$.data[0].previousClose").value(600.5))
                .andExpect(jsonPath("$.data[0].previousCloseDate").value("2026-11-16"));
    }

    @Test
    void fxRateFieldNames() throws Exception {
        var day = LocalDate.of(2026, 11, 17);
        when(fxService.findRates(List.of("USD"), day))
                .thenReturn(
                        List.of(
                                new FxRateResponseDTO(
                                        "USD",
                                        new BigDecimal("1510"),
                                        new BigDecimal("1512.5"),
                                        day)));
        mvc.perform(get("/api/fx-rates").param("currencies", "USD").param("asOf", "2026-11-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].currency").value("USD"))
                .andExpect(jsonPath("$.data[0].baseRate").value(1510))
                .andExpect(jsonPath("$.data[0].finalRate").value(1512.5))
                .andExpect(jsonPath("$.data[0].rateDate").value("2026-11-17"));
    }

    @Test
    void missingOrBadParametersAreRejected() throws Exception {
        mvc.perform(get("/api/foreign-prices").param("tickers", "QQQ"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/foreign-prices").param("tickers", "QQQ").param("asOf", "11/17"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/foreign-prices").param("tickers", "qqq").param("asOf", "2026-11-17"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/fx-rates").param("asOf", "2026-11-17"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/fx-rates").param("currencies", "USD"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(priceService, fxService);
    }
}
