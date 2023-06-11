package com.microservices.resource.processor.dto;

import java.io.Serializable;

public record ResourceDto(

    Long id,
    Long storageId,
    String key,
    String name,
    Long size
) implements Serializable {
}
