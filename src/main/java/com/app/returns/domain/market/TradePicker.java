package com.app.returns.domain.market;

import java.util.List;
import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TradePicker {

    public static TradeIntent pick(
            List<TradeCandidate> candidates, double buyRatio, Random random) {
        TradeCandidate candidate = candidates.get(random.nextInt(candidates.size()));
        return new TradeIntent(candidate, random.nextDouble() < buyRatio ? "BUY" : "SELL");
    }
}
