package com.microservices.resource.processor.dto;

public record SongDto(

    Long id,
    String name,
    String artist,
    String album,
    String length,
    String released
) {
}
