package com.microservices.resource.service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.StringJoiner;

@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapAddress;
    private String topic;

    public String getBootstrapAddress() {
        return bootstrapAddress;
    }

    public void setBootstrapAddress(String bootstrapAddress) {
        this.bootstrapAddress = bootstrapAddress;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KafkaProperties.class.getSimpleName() + "[", "]")
            .add("bootstrapAddress='" + bootstrapAddress + "'")
            .add("topic='" + topic + "'")
            .toString();
    }
}
