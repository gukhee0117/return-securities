package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.returns.domain.dto.DomesticPriceDTO;
import com.app.returns.domain.mapper.DomesticPriceMapper;
import com.app.returns.domain.market.PriceFactorStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomesticPriceServiceImplTest {
    private static DomesticPriceDTO row(String ticker, String close, String previous) {
        var dto = mock(DomesticPriceDTO.class);
        when(dto.getTicker()).thenReturn(ticker);
        when(dto.getPriceKrw()).thenReturn(new BigDecimal(close));
        when(dto.getPreviousCloseKrw()).thenReturn(new BigDecimal(previous));
        return dto;
    }

    @Test
    void 종가에_배수를_곱해_호가_단위로_맞추고_전일_종가는_그대로() {
        var mapper = mock(DomesticPriceMapper.class);
        var rows =
                List.of(
                        row("005930", "100000", "99000"),
                        row("069500", "36000", "35900"),
                        row("000660", "480000", "470000"));
        when(mapper.selectLatestByTickers(List.of("005930", "069500", "000660"), AS_OF))
                .thenReturn(rows);
        var store = new PriceFactorStore();
        store.update("005930", v -> 1.0123);
        store.update("069500", v -> 1.0123);
        var service = new DomesticPriceServiceImpl(mapper, store, Set.of("069500"));
        var result = service.findLatest(List.of("005930", "069500", "000660"), AS_OF);
        assertThat(result.get(0).getPriceKrw()).isEqualByComparingTo("101200"); // 주식 100원 단위
        assertThat(result.get(0).getPreviousCloseKrw()).isEqualByComparingTo("99000");
        assertThat(result.get(1).getPriceKrw()).isEqualByComparingTo("36445"); // ETF 5원 단위
        assertThat(result.get(2).getPriceKrw()).isEqualByComparingTo("480000"); // 배수 없음 → 종가
    }

    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);

    @Test
    void deduplicatesRequestInEncounterOrder() {
        var mapper = mock(DomesticPriceMapper.class);
        when(mapper.selectLatestByTickers(List.of("005930", "000660"), AS_OF))
                .thenReturn(List.of());
        var service = new DomesticPriceServiceImpl(mapper, new PriceFactorStore(), Set.of());
        assertThat(service.findLatest(List.of("005930", "000660", "005930"), AS_OF)).isEmpty();
        verify(mapper).selectLatestByTickers(List.of("005930", "000660"), AS_OF);
    }
}
