package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.FxRateResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface FxRateService {
    List<FxRateResponseDTO> findRates(List<String> currencies, LocalDate asOf);
}
