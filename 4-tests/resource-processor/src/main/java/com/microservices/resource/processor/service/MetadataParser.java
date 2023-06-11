package com.microservices.resource.processor.service;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class MetadataParser {

    private Parser parser;

    @PostConstruct
    void init() {
        parser = new Mp3Parser();
    }

    public Metadata parseMetadata(ByteArrayResource resource) {
        var contentHandler = new DefaultHandler();
        var metadata = new Metadata();
        var parseContext = new ParseContext();

        try (var inputStream = resource.getInputStream()) {
            parser.parse(inputStream, contentHandler, metadata, parseContext);
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException("Failed to parse resource metadata", e);
        }

        return metadata;
    }
}
