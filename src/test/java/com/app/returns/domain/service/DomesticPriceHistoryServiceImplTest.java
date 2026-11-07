package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.returns.domain.dto.DomesticPriceHistoryRowDTO;
import com.app.returns.domain.dto.response.DomesticPriceHistoryResponseDTO;
import com.app.returns.domain.mapper.DomesticPriceMapper;
import com.app.returns.domain.market.PriceFactorStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomesticPriceHistoryServiceImplTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 18);

    private static DomesticPriceHistoryRowDTO row(String ticker, String date, String close) {
        var dto = mock(DomesticPriceHistoryRowDTO.class);
        when(dto.getTicker()).thenReturn(ticker);
        when(dto.getBaseDate()).thenReturn(LocalDate.parse(date));
        when(dto.getCloseKrw()).thenReturn(new BigDecimal(close));
        return dto;
    }

    private static DomesticPriceServiceImpl service(DomesticPriceMapper mapper) {
        return new DomesticPriceServiceImpl(mapper, new PriceFactorStore(), Set.of());
    }

    @Test
    void 종목별로_묶고_날짜_순서를_그대로_유지한다() {
        var mapper = mock(DomesticPriceMapper.class);
        var rows =
                List.of(
                        row("000660", "2026-11-16", "5100"),
                        row("000660", "2026-11-17", "5200"),
                        row("005930", "2026-11-17", "71000"));
        when(mapper.selectHistoryByTickers(
                        List.of("000660", "005930"), AS_OF, AS_OF.minusDays(120), 30))
                .thenReturn(rows);

        var result = service(mapper).findHistory(List.of("000660", "005930"), AS_OF, 30);

        assertThat(result)
                .extracting(DomesticPriceHistoryResponseDTO::getTicker)
                .containsExactly("000660", "005930");
        assertThat(result.get(0).getPoints()).hasSize(2);
        assertThat(result.get(0).getPoints().get(0).getBaseDate())
                .isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(result.get(0).getPoints().get(1).getCloseKrw()).isEqualByComparingTo("5200");
        assertThat(result.get(1).getPoints()).hasSize(1);
    }

    @Test
    void 종가에_현재가용_배수를_곱하지_않는다() {
        var mapper = mock(DomesticPriceMapper.class);
        var rows = List.of(row("005930", "2026-11-17", "100000"));
        when(mapper.selectHistoryByTickers(List.of("005930"), AS_OF, AS_OF.minusDays(120), 30))
                .thenReturn(rows);
        var store = new PriceFactorStore();
        store.update("005930", v -> 1.0123);

        var result =
                new DomesticPriceServiceImpl(mapper, store, Set.of())
                        .findHistory(List.of("005930"), AS_OF, 30);

        assertThat(result.get(0).getPoints().get(0).getCloseKrw()).isEqualByComparingTo("100000");
    }

    @Test
    void 중복_종목은_처음_나온_순서로_한_번만_조회하고_조회_범위는_asOf에서_120일_전이다() {
        var mapper = mock(DomesticPriceMapper.class);
        when(mapper.selectHistoryByTickers(
                        List.of("005930", "000660"), AS_OF, AS_OF.minusDays(120), 20))
                .thenReturn(List.of());

        assertThat(service(mapper).findHistory(List.of("005930", "000660", "005930"), AS_OF, 20))
                .isEmpty();
        verify(mapper)
                .selectHistoryByTickers(
                        List.of("005930", "000660"), AS_OF, LocalDate.of(2026, 7, 21), 20);
    }
}
