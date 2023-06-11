package com.microservices.resource.service.cucumber.definition;

public record Resource(

    Long id,
    Long storageId,
    String key,
    String name,
    Long size
) {
}
