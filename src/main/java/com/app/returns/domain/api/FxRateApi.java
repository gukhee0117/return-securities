package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.FxRateRequestDTO;
import com.app.returns.domain.dto.response.FxRateResponseDTO;
import com.app.returns.domain.service.FxRateService;
import com.app.returns.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fx-rates")
public class FxRateApi {
    private final FxRateService service;

    /** 통화별 asOf 이하 가장 최근 환율. 없는 통화는 누락된다. */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDTO<List<FxRateResponseDTO>> getRates(
            @Valid @ModelAttribute FxRateRequestDTO request) {
        return ApiResponseDTO.of(
                "환율 조회 성공", service.findRates(request.getCurrencies(), request.getAsOf()));
    }
}
