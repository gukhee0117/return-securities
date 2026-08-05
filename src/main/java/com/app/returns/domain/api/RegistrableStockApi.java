package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.service.RegistrableStockService;
import com.app.returns.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/registrable-stocks")
public class RegistrableStockApi {

    private final RegistrableStockService registrableStockService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<RegistrableStockResponseDTO>> getRegistrableStock(
            @Valid RegistrableStockRequestDTO request) {
        RegistrableStockResponseDTO result = registrableStockService.findHeldQty(request);
        return ResponseEntity.ok(ApiResponseDTO.of("조회 성공", result));
    }
}

