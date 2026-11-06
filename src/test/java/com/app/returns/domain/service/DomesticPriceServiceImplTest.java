package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.returns.domain.mapper.DomesticPriceMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomesticPriceServiceImplTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);

    @Test
    void deduplicatesRequestInEncounterOrder() {
        var mapper = mock(DomesticPriceMapper.class);
        when(mapper.selectLatestByTickers(List.of("005930", "000660"), AS_OF))
                .thenReturn(List.of());
        var service = new DomesticPriceServiceImpl(mapper);
        assertThat(service.findLatest(List.of("005930", "000660", "005930"), AS_OF)).isEmpty();
        verify(mapper).selectLatestByTickers(List.of("005930", "000660"), AS_OF);
    }
}
