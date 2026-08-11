package com.app.returns.domain.generalaccount.service;

import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;

public interface GeneralAccountService {
    public GeneralAccountResponseDTO verifyGeneralAccount(GeneralAccountRequestDTO requestDTO);
}
