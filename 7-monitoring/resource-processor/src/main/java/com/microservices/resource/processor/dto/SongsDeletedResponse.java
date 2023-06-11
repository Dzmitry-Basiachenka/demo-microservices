package com.microservices.resource.processor.dto;

import java.util.List;

public record SongsDeletedResponse(

    List<Long> ids
) {
}
