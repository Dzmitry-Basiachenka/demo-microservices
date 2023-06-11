package com.microservices.resource.service;

import com.microservices.resource.service.config.properties.KafkaProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@ContextConfiguration(
    classes = AbstractIntegrationTest.TestConfig.class,
    initializers = WireMockInitializer.class
)
public abstract class AbstractIntegrationTest {

    protected static final String TOPIC_NAME = "resource-upload";
    protected static final String GROUP_ID = "resource-upload";

    @Container
    protected static final LocalStackContainer LOCAL_STACK =
        new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.2.0"))
            .withServices(S3);

    @Container
    protected static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.10"));

    static {
        LOCAL_STACK.start();
        KAFKA.start();
    }

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    public void resetMocks() {
        WireMock.reset();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public S3Client amazonS3() {
            return S3Client.builder()
                .endpointOverride(LOCAL_STACK.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCAL_STACK.getAccessKey(), LOCAL_STACK.getSecretKey())
                    )
                )
                .region(Region.of(LOCAL_STACK.getRegion()))
                .build();
        }

        @Bean
        @Primary
        public KafkaProperties kafkaProperties() {
            var properties = new KafkaProperties();
            properties.setBootstrapAddress(KAFKA.getBootstrapServers());
            properties.setTopic(TOPIC_NAME);
            return properties;
        }
    }
}
