package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface DomesticPriceService {
    List<DomesticPriceResponseDTO> findLatest(List<String> tickers, LocalDate asOf);
}
