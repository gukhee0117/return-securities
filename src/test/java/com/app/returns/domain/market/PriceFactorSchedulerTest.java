package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.returns.domain.mapper.DomesticPriceMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceFactorSchedulerTest {

    @Test
    void 종목_코드는_처음_한번만_읽고_모든_종목에_다음_배수를_적용한다() {
        var mapper = mock(DomesticPriceMapper.class);
        when(mapper.selectStockCodes()).thenReturn(List.of("005930", "069500"));
        var store = new PriceFactorStore();
        var scheduler = new PriceFactorScheduler(mapper, store);
        scheduler.tick();
        scheduler.tick();
        verify(mapper, times(1)).selectStockCodes();
        assertThat(store.get("005930")).isBetween(0.97, 1.03).isNotEqualTo(1.0);
        assertThat(store.get("069500")).isBetween(0.97, 1.03).isNotEqualTo(1.0);
        assertThat(store.get("000000")).isEqualTo(1.0);
    }
}
