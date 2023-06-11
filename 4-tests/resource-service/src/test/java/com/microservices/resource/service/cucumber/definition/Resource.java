package com.microservices.resource.service.cucumber.definition;

public record Resource(

    Long id,
    String bucket,
    String key,
    String name,
    Long size
) {
}
