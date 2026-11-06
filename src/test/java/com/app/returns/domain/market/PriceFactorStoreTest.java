package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PriceFactorStoreTest {

    @Test
    void 없는_종목은_1() {
        assertThat(new PriceFactorStore().get("005930")).isEqualTo(1.0);
    }

    @Test
    void update는_현재값에서_바꾼다() {
        var store = new PriceFactorStore();
        store.update("005930", v -> v + 0.01);
        store.update("005930", v -> v + 0.01);
        assertThat(store.get("005930")).isEqualTo(1.02);
        assertThat(store.get("000660")).isEqualTo(1.0);
    }
}
