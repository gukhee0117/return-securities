package com.app.returns.global.config;

import com.app.returns.global.filter.InternalSecretFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalSecurityConfig {
    @Bean
    public FilterRegistrationBean<InternalSecretFilter> internalSecretFilter(
            @Value("${market.maria.secret:}") String secret) {
        var registration = new FilterRegistrationBean<>(new InternalSecretFilter(secret));
        registration.addUrlPatterns("/api/internal/*");
        return registration;
    }
}
