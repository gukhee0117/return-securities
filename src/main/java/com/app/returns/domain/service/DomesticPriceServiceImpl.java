package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import com.app.returns.domain.mapper.DomesticPriceMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DomesticPriceServiceImpl implements DomesticPriceService {
    private final DomesticPriceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<DomesticPriceResponseDTO> findLatest(List<String> tickers, LocalDate asOf) {
        return mapper.selectLatestByTickers(tickers.stream().distinct().toList(), asOf).stream()
                .map(DomesticPriceResponseDTO::new)
                .toList();
    }
}
