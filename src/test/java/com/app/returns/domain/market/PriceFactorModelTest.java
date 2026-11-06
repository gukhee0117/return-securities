package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Test;

class PriceFactorModelTest {

    @Test
    void 십만번_반복해도_범위_안이고_평균은_1() {
        Random random = new Random(42);
        double factor = 1.0;
        double sum = 0;
        for (int i = 0; i < 100_000; i++) {
            factor = PriceFactorModel.next(factor, random);
            assertThat(factor).isBetween(0.97, 1.03);
            sum += factor;
        }
        assertThat(sum / 100_000).isBetween(0.99, 1.01);
    }

    @Test
    void 평균_쪽으로_되돌아온다() {
        Random zeroNoise =
                new Random() {
                    @Override
                    public double nextGaussian() {
                        return 0;
                    }
                };
        assertThat(PriceFactorModel.next(1.02, zeroNoise)).isLessThan(1.02);
        assertThat(PriceFactorModel.next(0.98, zeroNoise)).isGreaterThan(0.98);
    }

    @Test
    void push는_한쪽으로_연속_밀어도_범위_안() {
        double up = 1.0;
        double down = 1.0;
        for (int i = 0; i < 100; i++) {
            up = PriceFactorModel.push(up, true);
            down = PriceFactorModel.push(down, false);
            assertThat(up).isBetween(0.97, 1.03);
            assertThat(down).isBetween(0.97, 1.03);
        }
        assertThat(up).isEqualTo(1.03);
        assertThat(down).isEqualTo(0.97);
        assertThat(PriceFactorModel.push(1.0, true)).isEqualTo(1.003);
        assertThat(PriceFactorModel.push(1.0, false)).isEqualTo(0.997);
    }
}
