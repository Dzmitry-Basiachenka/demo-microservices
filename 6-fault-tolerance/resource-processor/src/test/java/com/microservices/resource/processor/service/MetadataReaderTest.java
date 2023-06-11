package com.microservices.resource.processor.service;

import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {MetadataReader.class})
@ExtendWith(SpringExtension.class)
class MetadataReaderTest {

    private static final String NAME = "Impact Moderato";
    private static final String ARTIST = "Kevin MacLeod";
    private static final String ALBUM = "Impact";
    private static final String LENGTH = "75.67630767822266";
    private static final String RELEASED = "2014-11-19T15:43:31";

    @Autowired
    private MetadataReader metadataReader;

    @Test
    void shouldCreateSong() {
        var metadata = new Metadata();
        metadata.set("dc:title", NAME);
        metadata.set("xmpDM:artist", ARTIST);
        metadata.set("xmpDM:album", ALBUM);
        metadata.set("xmpDM:duration", LENGTH);
        metadata.set("xmpDM:releaseDate", RELEASED);

        var id = 1L;
        var songDto = metadataReader.createSong(id, metadata);

        assertEquals(id, songDto.id());
        assertEquals(NAME, songDto.name());
        assertEquals(ARTIST, songDto.artist());
        assertEquals(ALBUM, songDto.album());
        assertEquals(LENGTH, songDto.length());
        assertEquals(RELEASED, songDto.released());
    }
}

