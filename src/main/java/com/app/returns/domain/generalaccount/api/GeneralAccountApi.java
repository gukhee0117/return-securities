package com.app.returns.domain.generalaccount.api;

import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;
import com.app.returns.domain.generalaccount.service.GeneralAccountService;
import com.app.returns.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/general-accounts")
public class GeneralAccountApi {
    private final GeneralAccountService generalAccountService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDTO<GeneralAccountResponseDTO>> verifyGeneralAccount (
            @Valid @RequestBody GeneralAccountRequestDTO requestDTO){
        GeneralAccountResponseDTO result = generalAccountService.verifyGeneralAccount(requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.of("일반계좌 검증 성공",result));
    }











}
