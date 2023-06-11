package com.microservices.resource.processor.service;

import com.microservices.resource.processor.dto.SongDto;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

@Component
public class MetadataReader {

    private static final String NAME = "dc:title";
    private static final String ARTIST = "xmpDM:artist";
    private static final String ALBUM = "xmpDM:album";
    private static final String LENGTH = "xmpDM:duration";
    private static final String RELEASED = "xmpDM:releaseDate";

    public SongDto createSong(Long id, Metadata metadata) {
        return new SongDto(
            id,
            metadata.get(NAME),
            metadata.get(ARTIST),
            metadata.get(ALBUM),
            metadata.get(LENGTH),
            metadata.get(RELEASED)
        );
    }
}
