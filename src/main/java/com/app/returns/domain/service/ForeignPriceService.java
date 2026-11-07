package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.ForeignPriceResponseDTO;
import java.time.LocalDate;
import java.util.List;

public interface ForeignPriceService {
    List<ForeignPriceResponseDTO> findPreviousCloses(List<String> tickers, LocalDate asOf);
}
