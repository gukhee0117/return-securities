package com.app.returns.domain.service;

import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import com.app.returns.domain.mapper.DomesticPriceMapper;
import com.app.returns.domain.market.KrxTickSize;
import com.app.returns.domain.market.PriceFactorStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DomesticPriceServiceImpl implements DomesticPriceService {
    private final DomesticPriceMapper mapper;
    private final PriceFactorStore factorStore;
    private final Set<String> etfCodes;

    public DomesticPriceServiceImpl(
            DomesticPriceMapper mapper,
            PriceFactorStore factorStore,
            @Value("${market.price.etf-codes:}") Set<String> etfCodes) {
        this.mapper = mapper;
        this.factorStore = factorStore;
        this.etfCodes = etfCodes;
    }

    /** 현재가 = 종가 × 종목별 배수를 호가 단위로 맞춘 값. 전일 종가에는 배수를 곱하지 않는다. */
    @Override
    @Transactional(readOnly = true)
    public List<DomesticPriceResponseDTO> findLatest(List<String> tickers, LocalDate asOf) {
        return mapper.selectLatestByTickers(tickers.stream().distinct().toList(), asOf).stream()
                .map(
                        row ->
                                new DomesticPriceResponseDTO(
                                        row,
                                        KrxTickSize.round(
                                                row.getPriceKrw()
                                                        .multiply(
                                                                BigDecimal.valueOf(
                                                                        factorStore.get(
                                                                                row.getTicker()))),
                                                etfCodes.contains(row.getTicker()))))
                .toList();
    }
}
