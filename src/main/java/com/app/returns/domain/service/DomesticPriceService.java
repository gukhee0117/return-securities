package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import java.util.List;

public interface DomesticPriceService {
    List<DomesticPriceResponseDTO> findLatest(List<String> tickers);
}
