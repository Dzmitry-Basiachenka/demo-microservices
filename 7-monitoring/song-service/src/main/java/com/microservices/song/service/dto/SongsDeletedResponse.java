package com.microservices.song.service.dto;

import java.util.List;

public record SongsDeletedResponse(

    List<Long> ids
) {
}
