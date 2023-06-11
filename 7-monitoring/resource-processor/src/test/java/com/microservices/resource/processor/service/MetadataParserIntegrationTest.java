package com.microservices.resource.processor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {MetadataParser.class})
@ExtendWith(SpringExtension.class)
class MetadataParserIntegrationTest {

    private static final String FILE_PATH = "/audio/Kevin MacLeod - Impact Moderato.mp3";

    @Autowired
    private MetadataParser metadataParser;

    @Test
    void shouldParseMetadata() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var resource = new ByteArrayResource(content);

        var metadata = metadataParser.parseMetadata(resource);

        assertEquals("audio/mpeg", metadata.get("Content-Type"));
        assertEquals("Impact Moderato", metadata.get("dc:title"));
        assertEquals("Kevin MacLeod", metadata.get("xmpDM:artist"));
        assertEquals("Impact", metadata.get("xmpDM:album"));
        assertEquals("75.67630767822266", metadata.get("xmpDM:duration"));
        assertEquals("2014-11-19T15:43:31", metadata.get("xmpDM:releaseDate"));
    }
}
