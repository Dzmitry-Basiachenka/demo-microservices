package com.microservices.resource.service.service;

import com.microservices.resource.service.config.properties.KafkaProperties;
import com.microservices.resource.service.entity.ResourceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ResourcePublisher {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePublisher.class);

    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final PublishEventCallback publishEventCallback;
    private final ObjectMapper objectMapper;

    public ResourcePublisher(KafkaTemplate<Long, String> kafkaTemplate, KafkaProperties kafkaProperties, PublishEventCallback publishEventCallback, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
        this.publishEventCallback = publishEventCallback;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        kafkaTemplate.setDefaultTopic(kafkaProperties.getTopic());
    }

    public void publish(ResourceEntity resource) {
        var message = MessageBuilder.withPayload(getPayload(resource))
            .setHeader(KafkaHeaders.KEY, resource.getId())
            .build();

        logger.info("Publish message: {}", message);
        var future = kafkaTemplate.send(message);
        future.addCallback(publishEventCallback);
    }

    private String getPayload(ResourceEntity resource) {
        try {
            return objectMapper.writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build resource upload message", e);
        }
    }
}
