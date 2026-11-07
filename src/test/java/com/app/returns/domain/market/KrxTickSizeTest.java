package com.app.returns.domain.market;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class KrxTickSizeTest {

    private static void stock(String price, String expected) {
        assertThat(KrxTickSize.round(new BigDecimal(price), false))
                .as("주식 %s", price)
                .isEqualByComparingTo(expected);
    }

    private static void etf(String price, String expected) {
        assertThat(KrxTickSize.round(new BigDecimal(price), true))
                .as("ETF %s", price)
                .isEqualByComparingTo(expected);
    }

    @Test
    void 주식_구간_경계() {
        stock("1999.4", "1999"); // 2천원 미만 1원
        stock("2002", "2000"); // 2천~5천 5원
        stock("2003", "2005");
        stock("4997", "4995");
        stock("4998", "5000");
        stock("5004", "5000"); // 5천~2만 10원
        stock("5006", "5010");
        stock("19994", "19990");
        stock("19996", "20000");
        stock("20024", "20000"); // 2만~5만 50원
        stock("20026", "20050");
        stock("49974", "49950");
        stock("49976", "50000");
        stock("50049", "50000"); // 5만~20만 100원
        stock("50051", "50100");
        stock("199949", "199900");
        stock("199951", "200000");
        stock("200249", "200000"); // 20만~50만 500원
        stock("200251", "200500");
        stock("499749", "499500");
        stock("499751", "500000");
        stock("500499", "500000"); // 50만~ 1000원
        stock("500501", "501000");
    }

    @Test
    void ETF는_2천원_미만_1원_그_이상_5원() {
        etf("1999.4", "1999");
        etf("2000", "2000");
        etf("2002", "2000");
        etf("2003", "2005");
        etf("36442.8", "36445");
        etf("123456", "123455");
        etf("700000", "700000");
        etf("700002", "700000");
        etf("700003", "700005");
    }
}
