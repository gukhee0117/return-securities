package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.FxRateResponseDTO;
import com.app.returns.domain.mapper.FxRateMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FxRateServiceImpl implements FxRateService {
    private final FxRateMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<FxRateResponseDTO> findRates(List<String> currencies, LocalDate asOf) {
        return mapper.selectRates(currencies.stream().distinct().toList(), asOf).stream()
                .map(
                        row ->
                                new FxRateResponseDTO(
                                        row.getCurrency(),
                                        row.getBaseRate(),
                                        row.getFinalRate(),
                                        row.getRateDate()))
                .toList();
    }
}
