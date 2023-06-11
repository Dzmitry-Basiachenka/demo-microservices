package com.microservices.resource.processor.config;

import com.microservices.resource.processor.exception.BadRequestException;
import com.microservices.resource.processor.exception.NotFoundException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new BadRequestException(response.reason());
            case 404 -> new NotFoundException(response.reason());
            default -> new RuntimeException(response.reason());
        };
    }

    @Bean
    public Retryer retryer(@Value("${feign.retry.period}") long period,
                           @Value("${feign.retry.max-period}") long maxPeriod,
                           @Value("${feign.retry.max-attempts}") int maxAttempts) {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }
}
