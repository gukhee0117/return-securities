package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TradePickerTest {
    private static final List<TradeCandidate> CANDIDATES =
            List.of(
                    new TradeCandidate(1L, "a".repeat(64)),
                    new TradeCandidate(2L, "b".repeat(64)),
                    new TradeCandidate(3L, "c".repeat(64)));

    @Test
    void 같은_시드면_같은_결과() {
        for (int i = 0; i < 100; i++) {
            var first = TradePicker.pick(CANDIDATES, 0.94, new Random(i));
            var second = TradePicker.pick(CANDIDATES, 0.94, new Random(i));
            assertThat(first).isEqualTo(second);
        }
    }

    @Test
    void 매수_비율은_설정값_근처() {
        Random random = new Random(7);
        int buys = 0;
        for (int i = 0; i < 10_000; i++) {
            if (TradePicker.pick(CANDIDATES, 0.94, random).side().equals("BUY")) {
                buys++;
            }
        }
        assertThat(buys / 10_000.0).isBetween(0.92, 0.96);
    }

    @Test
    void 후보_목록_안에서만_고르고_모두_골고루_뽑힌다() {
        Random random = new Random(3);
        var seen = new java.util.HashSet<Long>();
        for (int i = 0; i < 300; i++) {
            seen.add(TradePicker.pick(CANDIDATES, 0.5, random).candidate().getGeneralCustomerId());
        }
        assertThat(seen).containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}
