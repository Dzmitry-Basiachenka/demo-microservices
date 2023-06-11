package com.microservices.resource.processor.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(

    String bootstrapAddress,
    String topic,
    String groupId,
    Map<String, String> properties
) {
}
