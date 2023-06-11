package com.microservices.resource.processor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {MetadataParser.class})
@ExtendWith(SpringExtension.class)
class MetadataParserTest {

    @Autowired
    private MetadataParser metadataParser;

    @Test
    void shouldParseMetadata() {
        var resource = new ByteArrayResource(new byte[]{73, 68, 51, 3, 0, 0, 0, 7, 61, 69});

        var metadata = metadataParser.parseMetadata(resource);

        assertEquals("audio/mpeg", metadata.get("Content-Type"));
    }
}

