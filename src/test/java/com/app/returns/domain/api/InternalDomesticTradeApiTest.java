package com.app.returns.domain.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.returns.domain.member.exception.MemberNotFoundException;
import com.app.returns.domain.service.InternalDomesticTradeService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import com.app.returns.global.filter.InternalSecretFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalDomesticTradeApiTest {
    private static final String PATH = "/api/internal/domestic-trades";
    private static final String BODY =
            "{\"ciHash\":\""
                    + "a".repeat(64)
                    + "\",\"side\":\"BUY\",\"ticker\":\"005930\",\"qty\":3,\"price\":85500,"
                    + "\"executedAt\":\"2026-11-18T10:00:00\",\"mariaOrderId\":77}";

    private InternalDomesticTradeService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(InternalDomesticTradeService.class);
        mvc =
                MockMvcBuilders.standaloneSetup(new InternalDomesticTradeApi(service))
                        .addFilters(new InternalSecretFilter("s3cret"))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void 비밀_헤더가_없거나_다르면_401이고_서비스를_부르지_않는다() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        mvc.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "wrong")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void 비밀을_설정하지_않으면_모든_요청을_거절한다() throws Exception {
        MockMvc open =
                MockMvcBuilders.standaloneSetup(new InternalDomesticTradeApi(service))
                        .addFilters(new InternalSecretFilter(""))
                        .build();
        open.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void 비밀이_맞으면_기록하고_200() throws Exception {
        mvc.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "s3cret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("거래 기록 성공"));
        verify(service).record(any());
    }

    @Test
    void 잘못된_본문은_400() throws Exception {
        mvc.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "s3cret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY.replace("\"qty\":3", "\"qty\":0")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "s3cret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY.replace("005930", "abc")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void 모르는_고객이면_404() throws Exception {
        Mockito.doThrow(new MemberNotFoundException("고객 없음")).when(service).record(any());
        mvc.perform(
                        post(PATH)
                                .header("X-Internal-Secret", "s3cret")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BODY))
                .andExpect(status().isNotFound());
    }
}
