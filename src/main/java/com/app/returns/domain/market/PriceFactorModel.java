package com.app.returns.domain.market;

import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 종가에 곱하는 배수의 움직임. 1.0 쪽으로 되돌아오는 잡음이라 오래 돌려도 ±3%를 넘지 않는다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PriceFactorModel {
    private static final double MIN = 0.97;
    private static final double MAX = 1.03;
    private static final double PULL_TO_ONE = 0.02;
    private static final double NOISE = 0.001;
    private static final double PUSH = 0.003;

    public static double next(double current, Random random) {
        return clamp(current + PULL_TO_ONE * (1 - current) + random.nextGaussian() * NOISE);
    }

    /** 다른 고객의 매수는 가격을 조금 올리고 매도는 조금 내린다. */
    public static double push(double current, boolean buy) {
        return clamp(buy ? current + PUSH : current - PUSH);
    }

    private static double clamp(double factor) {
        return Math.max(MIN, Math.min(MAX, factor));
    }
}
