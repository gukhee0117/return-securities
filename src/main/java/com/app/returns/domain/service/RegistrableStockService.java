package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;

public interface RegistrableStockService {

    RegistrableStockResponseDTO findHeldQty(
            Long generalAccountId, Long foreignProductId);
}
