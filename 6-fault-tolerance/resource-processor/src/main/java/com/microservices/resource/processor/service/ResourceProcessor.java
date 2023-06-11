package com.microservices.resource.processor.service;

import com.microservices.resource.processor.client.ResourceServiceClient;
import com.microservices.resource.processor.client.SongServiceClient;
import com.microservices.resource.processor.dto.ResourceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceProcessor.class);

    private final ObjectMapper objectMapper;
    private final ResourceServiceClient resourceServiceClient;
    private final MetadataParser metadataParser;
    private final MetadataReader metadataReader;
    private final SongServiceClient songServiceClient;

    public ResourceProcessor(ObjectMapper objectMapper, ResourceServiceClient resourceServiceClient, MetadataParser metadataParser, MetadataReader metadataReader, SongServiceClient songServiceClient) {
        this.objectMapper = objectMapper;
        this.resourceServiceClient = resourceServiceClient;
        this.metadataParser = metadataParser;
        this.metadataReader = metadataReader;
        this.songServiceClient = songServiceClient;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.group-id}")
    public void process(String message) throws JsonProcessingException {
        logger.info("Message received: {}", message);

        var resourceDto = objectMapper.readValue(message, ResourceDto.class);
        logger.info("Resource deserialized: {}", resourceDto);

        var resource = resourceServiceClient.downloadResource(resourceDto.id());
        logger.info("Resource received from resource service: {}", resource);

        var metadata = metadataParser.parseMetadata(resource);
        logger.info("Metadata parsed: {}", metadata);

        var songDto = metadataReader.createSong(resourceDto.id(), metadata);
        logger.info("Song created: {}", songDto);

        var songCreatedResponse = songServiceClient.createSong(songDto);
        logger.info("Song sent to song service: {}", songCreatedResponse);

        resourceServiceClient.completeResourceUpload(resourceDto.id());
        logger.info("Resource upload completed in resource service: {}", resourceDto);
    }
}
