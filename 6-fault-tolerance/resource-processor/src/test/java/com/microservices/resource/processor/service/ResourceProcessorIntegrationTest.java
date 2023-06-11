package com.microservices.resource.processor.service;

import com.microservices.resource.processor.client.ResourceServiceClient;
import com.microservices.resource.processor.client.SongServiceClient;
import com.microservices.resource.processor.config.properties.KafkaProperties;
import com.microservices.resource.processor.dto.ResourceDto;
import com.microservices.resource.processor.dto.SongCreatedResponse;
import com.microservices.resource.processor.dto.SongDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://${kafka.bootstrap-address}", "port=9092"},
    topics = "${kafka.topic}"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ResourceProcessorIntegrationTest {

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    private ResourceProcessor resourceProcessorService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceServiceClient resourceServiceClient;

    @MockBean
    private SongServiceClient songServiceClient;

    private Producer<Long, String> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> properties = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        producer = new DefaultKafkaProducerFactory<Long, String>(properties).createProducer();
    }

    @AfterEach
    void tearDown() {
        producer.close();
    }

    @Test
    public void shouldProcess() throws IOException {
        var id = 1L;

        var resource = new ClassPathResource("audio/Kevin MacLeod - Impact Moderato.mp3");
        var content = resource.getInputStream().readAllBytes();
        var byteArrayResource = new ByteArrayResource(content);
        when(resourceServiceClient.downloadResource(id)).thenReturn(byteArrayResource);

        var songDto = new SongDto(
            id,
            "Impact Moderato",
            "Kevin MacLeod",
            "Impact",
            "75.67630767822266",
            "2014-11-19T15:43:31"
        );
        when(songServiceClient.createSong(songDto)).thenReturn(new SongCreatedResponse(id));

        var resourceDto = new ResourceDto(
            id,
            1L,
            "11111111-2222-3333-4444-555555555555",
            "Kevin MacLeod - Impact Moderato.mp3",
            3636515L
        );
        String message = objectMapper.writeValueAsString(resourceDto);
        producer.send(new ProducerRecord<>(topic, resourceDto.id(), message));

        verify(resourceServiceClient, timeout(5_000L)).downloadResource(id);
        verify(songServiceClient, timeout(5_000L)).createSong(songDto);
    }

    @TestConfiguration
    @EnableConfigurationProperties({KafkaProperties.class})
    static class ResourceProcessorTestConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}
