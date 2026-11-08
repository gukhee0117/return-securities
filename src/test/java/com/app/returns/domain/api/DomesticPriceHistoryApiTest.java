package com.app.returns.domain.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.returns.domain.dto.response.DomesticPriceHistoryResponseDTO;
import com.app.returns.domain.service.DomesticPriceService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DomesticPriceHistoryApiTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 18);

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
    void days를_안_주면_30일로_조회하고_종목이_쉼표로_묶인다() throws Exception {
        when(service.findHistory(List.of("005930", "000660"), AS_OF, 30))
                .thenReturn(
                        List.of(
                                new DomesticPriceHistoryResponseDTO(
                                        "005930",
                                        List.of(
                                                new DomesticPriceHistoryResponseDTO.Point(
                                                        LocalDate.of(2026, 11, 17),
                                                        new BigDecimal("71000"))))));

        mvc.perform(
                        get("/api/domestic-prices/history")
                                .param("tickers", "005930,000660")
                                .param("asOf", "2026-11-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("종가 이력 조회 성공"))
                .andExpect(jsonPath("$.data[0].ticker").value("005930"))
                .andExpect(jsonPath("$.data[0].points.length()").value(1))
                .andExpect(jsonPath("$.data[0].points[0].closeKrw").value(71000));
        verify(service).findHistory(List.of("005930", "000660"), AS_OF, 30);
    }

    @Test
    void days를_주면_그대로_넘기고_최소_1_최대_60이다() throws Exception {
        when(service.findHistory(List.of("005930"), AS_OF, 60)).thenReturn(List.of());
        mvc.perform(
                        get("/api/domestic-prices/history")
                                .param("tickers", "005930")
                                .param("asOf", "2026-11-18")
                                .param("days", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        verify(service).findHistory(List.of("005930"), AS_OF, 60);
    }

    private static final String PATH = "/api/domestic-prices/history";

    @Test
    void 파라미터가_하나도_없으면_서비스를_부르지_않고_400이다() throws Exception {
        mvc.perform(get(PATH)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void 종목_코드_형식이_틀리면_서비스를_부르지_않고_400이다() throws Exception {
        mvc.perform(get(PATH).param("tickers", "invalid").param("asOf", "2026-11-18"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void 종목이_51개면_서비스를_부르지_않고_400이다() throws Exception {
        String tooMany =
                IntStream.range(0, 51)
                        .mapToObj(i -> String.format("%06d", i))
                        .collect(Collectors.joining(","));
        mvc.perform(get(PATH).param("tickers", tooMany).param("asOf", "2026-11-18"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest(name = "asOf={0}")
    @NullAndEmptySource
    @ValueSource(strings = {"11/18", "2026-13-40"})
    void asOf가_없거나_형식이_틀리면_서비스를_부르지_않고_400이다(String asOf) throws Exception {
        var request = get(PATH).param("tickers", "005930");
        if (asOf != null) {
            request = request.param("asOf", asOf);
        }
        mvc.perform(request).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest(name = "days={0}")
    @ValueSource(strings = {"0", "61", "-3", "abc"})
    void days가_범위를_벗어나거나_숫자가_아니면_서비스를_부르지_않고_400이다(String days) throws Exception {
        mvc.perform(
                        get(PATH)
                                .param("tickers", "005930")
                                .param("asOf", "2026-11-18")
                                .param("days", days))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
