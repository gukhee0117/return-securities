package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.DomesticPriceHistoryRequestDTO;
import com.app.returns.domain.dto.request.DomesticPriceRequestDTO;
import com.app.returns.domain.dto.response.DomesticPriceHistoryResponseDTO;
import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import com.app.returns.domain.service.DomesticPriceService;
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
@RequestMapping("/api/domestic-prices")
public class DomesticPriceApi {
    private final DomesticPriceService service;

    /** 종목별 asOf 이하 가장 최근 base_date의 저장 가격과 그 직전 종가. 없는 종목은 누락되며 0원으로 대체하지 않는다. */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDTO<List<DomesticPriceResponseDTO>> getPrices(
            @Valid @ModelAttribute DomesticPriceRequestDTO request) {
        return ApiResponseDTO.of(
                "현재가 조회 성공", service.findLatest(request.getTickers(), request.getAsOf()));
    }

    /**
     * 종목별 asOf 이하 최근 days개 영업일의 저장 종가(날짜 오름차순, 기본 30·최대 60). 없는 종목은 누락되며 0원으로 대체하지 않는다. 저장된 종가
     * 그대로이고 현재가용 배수는 곱하지 않는다.
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponseDTO<List<DomesticPriceHistoryResponseDTO>> getHistory(
            @Valid @ModelAttribute DomesticPriceHistoryRequestDTO request) {
        return ApiResponseDTO.of(
                "종가 이력 조회 성공",
                service.findHistory(request.getTickers(), request.getAsOf(), request.getDays()));
    }
}
