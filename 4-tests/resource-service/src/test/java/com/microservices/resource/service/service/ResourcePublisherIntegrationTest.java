package com.microservices.resource.service.service;

import com.microservices.resource.service.AbstractIntegrationTest;
import com.microservices.resource.service.entity.ResourceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ResourcePublisherIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ResourcePublisher resourcePublisher;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<Long, String> consumer;

    @BeforeEach
    void setUp() {
        var properties = new HashMap<String, Object>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new DefaultKafkaConsumerFactory<Long, String>(properties).createConsumer();
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    }

    @Test
    @Transactional
    void shouldPublishResource() throws JsonProcessingException {
        var resourceEntity = new ResourceEntity();
        resourceEntity.setId(1L);
        resourceEntity.setBucket("resources");
        resourceEntity.setKey("11111111-2222-3333-4444-555555555555");
        resourceEntity.setName("audio.mp3");
        resourceEntity.setSize(10L);

        resourcePublisher.publish(resourceEntity);

        var consumerRecord = KafkaTestUtils.getSingleRecord(consumer, TOPIC_NAME);
        assertNotNull(consumerRecord);
        assertEquals(resourceEntity.getId(), consumerRecord.key());
        assertEquals(objectMapper.writeValueAsString(resourceEntity), consumerRecord.value());
    }
}
