package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.ForeignPriceResponseDTO;
import com.app.returns.domain.mapper.ForeignPriceMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForeignPriceServiceImpl implements ForeignPriceService {
    private final ForeignPriceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ForeignPriceResponseDTO> findPreviousCloses(List<String> tickers, LocalDate asOf) {
        return mapper.selectPreviousCloses(tickers.stream().distinct().toList(), asOf).stream()
                .map(
                        row ->
                                new ForeignPriceResponseDTO(
                                        row.getTicker(),
                                        row.getPreviousClose(),
                                        row.getPreviousCloseDate()))
                .toList();
    }
}
