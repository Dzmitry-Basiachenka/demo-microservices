package com.microservices.resource.service.dto;

public record ResourceDto(

    Long id,
    Long storageId,
    String key,
    String name,
    Long size
) {
}
