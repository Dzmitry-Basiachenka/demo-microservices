package com.microservices.resource.service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "s3")
public record S3Properties(

    URI endpointUri,
    String region,
    String bucket
) {
}
