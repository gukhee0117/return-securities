package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.InternalDomesticTradeRequestDTO;
import com.app.returns.domain.service.InternalDomesticTradeService;
import com.app.returns.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** maria 전용. X-Internal-Secret이 있어야 하고 외부(nginx)에는 열지 않는다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/domestic-trades")
public class InternalDomesticTradeApi {
    private final InternalDomesticTradeService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDTO<Void> record(
            @Valid @RequestBody InternalDomesticTradeRequestDTO request) {
        service.record(request);
        return ApiResponseDTO.of("거래 기록 성공");
    }
}
