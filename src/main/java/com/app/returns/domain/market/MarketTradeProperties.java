package com.app.returns.domain.market;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 거래 생성기 설정(market.trade.*). 기본은 꺼짐. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "market.trade")
public class MarketTradeProperties {
    private boolean enabled = false;
    private long minDelayMs = 1000;
    private long maxDelayMs = 4000;
    private double buyRatio = 0.94;
    private int maxAttempts = 5;
    private Set<String> excludedCiHashes = Set.of();
}
