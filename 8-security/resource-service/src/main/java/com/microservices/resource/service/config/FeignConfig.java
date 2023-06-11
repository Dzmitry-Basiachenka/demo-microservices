package com.microservices.resource.service.config;

import com.microservices.resource.service.exception.BadRequestException;
import com.microservices.resource.service.exception.NotFoundException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 400 -> new BadRequestException(response.reason());
            case 404 -> new NotFoundException(response.reason());
            default -> new RuntimeException(response.reason());
        };
    }
}
