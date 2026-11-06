package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.returns.domain.mapper.ForeignPriceMapper;
import com.app.returns.domain.mapper.FxRateMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ForeignPriceServiceImplTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);

    @Test
    void deduplicatesTickers() {
        var mapper = mock(ForeignPriceMapper.class);
        when(mapper.selectPreviousCloses(List.of("QQQ", "AAPL"), AS_OF)).thenReturn(List.of());
        assertThat(
                        new ForeignPriceServiceImpl(mapper)
                                .findPreviousCloses(List.of("QQQ", "AAPL", "QQQ"), AS_OF))
                .isEmpty();
        verify(mapper).selectPreviousCloses(List.of("QQQ", "AAPL"), AS_OF);
    }

    @Test
    void deduplicatesCurrencies() {
        var mapper = mock(FxRateMapper.class);
        when(mapper.selectRates(List.of("USD", "JPY"), AS_OF)).thenReturn(List.of());
        assertThat(new FxRateServiceImpl(mapper).findRates(List.of("USD", "JPY", "USD"), AS_OF))
                .isEmpty();
        verify(mapper).selectRates(List.of("USD", "JPY"), AS_OF);
    }
}
