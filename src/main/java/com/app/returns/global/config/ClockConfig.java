package com.app.returns.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {
    /** 실제 시각이 필요한 운영 타이머(거래 생성 예약·멱등키)용. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
