package com.app.returns.domain.market;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;

/** 종목별 현재 배수. 메모리에만 두고 재시작하면 1.0으로 돌아간다. */
@Component
public class PriceFactorStore {
    private final Map<String, Double> factors = new ConcurrentHashMap<>();

    public double get(String ticker) {
        return factors.getOrDefault(ticker, 1.0);
    }

    public void update(String ticker, UnaryOperator<Double> change) {
        factors.compute(ticker, (key, current) -> change.apply(current == null ? 1.0 : current));
    }
}
