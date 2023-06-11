package com.microservices.song.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record SongDto(

    @NotNull
    Long id,

    @NotBlank
    String name,

    @NotBlank
    String artist,

    String album,

    @NotNull
    String length,

    String released
) {
}
