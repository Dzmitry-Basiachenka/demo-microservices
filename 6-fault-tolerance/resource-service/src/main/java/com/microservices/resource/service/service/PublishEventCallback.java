package com.microservices.resource.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class PublishEventCallback implements ListenableFutureCallback<SendResult<Long, String>> {

    private static final Logger logger = LoggerFactory.getLogger(PublishEventCallback.class);

    @Override
    public void onSuccess(SendResult<Long, String> result) {
        logger.info("Resource upload message with key {} and value {} was published to topic {} with offset {}",
            result.getProducerRecord().key(),
            result.getProducerRecord().value(),
            result.getRecordMetadata().topic(),
            result.getRecordMetadata().offset()
        );
    }

    @Override
    public void onFailure(Throwable e) {
        logger.error("Failed to publish resource upload message", e);
    }
}
