package com.app.returns.domain.service;

import com.app.returns.domain.dto.DomesticPriceHistoryRowDTO;
import com.app.returns.domain.dto.response.DomesticPriceHistoryResponseDTO;
import com.app.returns.domain.dto.response.DomesticPriceResponseDTO;
import com.app.returns.domain.mapper.DomesticPriceMapper;
import com.app.returns.domain.market.KrxTickSize;
import com.app.returns.domain.market.PriceFactorStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DomesticPriceServiceImpl implements DomesticPriceService {
    /** 영업일 최대 60개를 휴장일까지 넉넉히 덮는 조회 범위(달력일). 스캔 범위를 묶는 용도다. */
    static final int HISTORY_LOOKBACK_DAYS = 120;

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

    /** 저장된 종가를 그대로 종목별로 묶는다. 현재가용 배수는 곱하지 않고, 요청한 종목 중 종가가 있는 종목만 담긴다. */
    @Override
    @Transactional(readOnly = true)
    public List<DomesticPriceHistoryResponseDTO> findHistory(
            List<String> tickers, LocalDate asOf, int days) {
        List<DomesticPriceHistoryRowDTO> rows =
                mapper.selectHistoryByTickers(
                        tickers.stream().distinct().toList(),
                        asOf,
                        asOf.minusDays(HISTORY_LOOKBACK_DAYS),
                        days);
        Map<String, List<DomesticPriceHistoryResponseDTO.Point>> byTicker =
                rows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        DomesticPriceHistoryRowDTO::getTicker,
                                        LinkedHashMap::new,
                                        Collectors.mapping(
                                                row ->
                                                        new DomesticPriceHistoryResponseDTO.Point(
                                                                row.getBaseDate(),
                                                                row.getCloseKrw()),
                                                Collectors.toList())));
        return byTicker.entrySet().stream()
                .map(e -> new DomesticPriceHistoryResponseDTO(e.getKey(), e.getValue()))
                .toList();
    }
}
