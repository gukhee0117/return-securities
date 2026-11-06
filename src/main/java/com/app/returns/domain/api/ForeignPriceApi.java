package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.ForeignPriceRequestDTO;
import com.app.returns.domain.dto.response.ForeignPriceResponseDTO;
import com.app.returns.domain.service.ForeignPriceService;
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
@RequestMapping("/api/foreign-prices")
public class ForeignPriceApi {
    private final ForeignPriceService service;

    /** 종목별 asOf 이전 가장 최근 종가. 없는 종목은 누락되며 0원으로 대체하지 않는다. */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDTO<List<ForeignPriceResponseDTO>> getPrices(
            @Valid @ModelAttribute ForeignPriceRequestDTO request) {
        return ApiResponseDTO.of(
                "해외 종가 조회 성공", service.findPreviousCloses(request.getTickers(), request.getAsOf()));
    }
}
