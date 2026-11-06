package com.app.returns.domain.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class MariaOrderClientTest {
    private MockRestServiceServer server;
    private MariaOrderClient client;

    @BeforeEach
    void setup() {
        var builder = RestClient.builder().baseUrl("http://maria.test:10000");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new MariaOrderClient(builder.build(), "s3cret");
    }

    @Test
    void 비밀_헤더와_본문을_보내고_data를_읽는다() {
        server.expect(requestTo("http://maria.test:10000/api/internal/domestic-orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Secret", "s3cret"))
                .andExpect(jsonPath("$.ciHash").value("a".repeat(64)))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.idempotencyKey").value("GEN-1-1"))
                .andRespond(
                        withSuccess(
                                "{\"message\":\"ok\",\"data\":{\"status\":\"EXECUTED\",\"domesticOrderId\":11,"
                                        + "\"ticker\":\"005930\",\"qty\":10,\"price\":85500,\"amount\":-855034,"
                                        + "\"processedAt\":\"2026-11-18T10:00:00\"}}",
                                MediaType.APPLICATION_JSON));

        var result = client.order("a".repeat(64), "BUY", "GEN-1-1");

        assertThat(result.status()).isEqualTo("EXECUTED");
        assertThat(result.domesticOrderId()).isEqualTo(11L);
        assertThat(result.price()).isEqualByComparingTo("85500");
        assertThat(result.processedAt()).isEqualTo(LocalDateTime.of(2026, 11, 18, 10, 0));
    }

    @Test
    void 서버_오류는_예외로_올라온다() {
        server.expect(requestTo("http://maria.test:10000/api/internal/domestic-orders"))
                .andRespond(withServerError());
        assertThatThrownBy(() -> client.order("a".repeat(64), "BUY", "GEN-1-1"))
                .isInstanceOf(RestClientResponseException.class);
    }
}
