package com.app.returns.domain.api;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.service.RegistrableStockService;
import com.app.returns.global.response.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/registrable-stocks")
public class RegistrableStockApi {

    private final RegistrableStockService registrableStockService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<RegistrableStockResponseDTO>> findHeldQty(
            @RequestParam Long generalAccountId,
            @RequestParam Long foreignProductId) {
        RegistrableStockResponseDTO result = registrableStockService.findHeldQty(
                generalAccountId, foreignProductId);
        return ResponseEntity.ok(ApiResponseDTO.of("등록가능 보유수량 조회 성공", result));
    }
}

