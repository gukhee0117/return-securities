package com.app.returns.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.app.returns.domain.mapper.DomesticPriceMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomesticPriceServiceImplTest {
    @Test
    void deduplicatesRequestAndKeepsMissingPricesAbsent() {
        var mapper = mock(DomesticPriceMapper.class);
        when(mapper.selectLatestByTickers(List.of("000660", "005930"))).thenReturn(List.of());
        var service = new DomesticPriceServiceImpl(mapper);
        assertThat(service.findLatest(List.of("005930", "000660", "005930"))).isEmpty();
        verify(mapper).selectLatestByTickers(List.of("000660", "005930"));
    }
}
