package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.mapper.RegistrableStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RegistrableStockServiceImpl implements RegistrableStockService {

    private final RegistrableStockMapper registrableStockMapper;

    @Override
    public RegistrableStockResponseDTO findHeldQty(RegistrableStockRequestDTO requestDTO) {
        return registrableStockMapper.findHeldQty(requestDTO)
                .map(RegistrableStockResponseDTO::new)
                .orElseThrow(() -> new RegistrableStockNotFoundException("등록가능 보유수량 조회 실패"));
    }
}
