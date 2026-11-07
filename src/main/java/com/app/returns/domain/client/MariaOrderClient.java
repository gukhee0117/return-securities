package com.app.returns.domain.client;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** maria 내부 주문 API 호출. 응답 오류·타임아웃은 RestClientException으로 그대로 올린다(재시도는 호출한 쪽이 정한다). */
@Component
public class MariaOrderClient {
    private static final String SECRET_HEADER = "X-Internal-Secret";

    private final RestClient client;
    private final String secret;

    @Autowired
    public MariaOrderClient(
            RestClient.Builder builder,
            @Value("${market.maria.url:}") String url,
            @Value("${market.maria.secret:}") String secret) {
        var requests = new SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(Duration.ofSeconds(2));
        requests.setReadTimeout(Duration.ofSeconds(3));
        this.client = builder.baseUrl(url).requestFactory(requests).build();
        this.secret = secret;
    }

    MariaOrderClient(RestClient client, String secret) {
        this.client = client;
        this.secret = secret;
    }

    public MariaOrderResult order(String ciHash, String side, String idempotencyKey) {
        Envelope response =
                client.post()
                        .uri("/api/internal/domestic-orders")
                        .header(SECRET_HEADER, secret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                Map.of(
                                        "ciHash", ciHash,
                                        "side", side,
                                        "idempotencyKey", idempotencyKey))
                        .retrieve()
                        .body(Envelope.class);
        if (response == null || response.data() == null) {
            throw new IllegalStateException("maria 주문 응답이 비어 있습니다.");
        }
        return response.data();
    }

    private record Envelope(MariaOrderResult data) {}
}
