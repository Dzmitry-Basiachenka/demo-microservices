package com.microservices.resource.service.dto;

public record UploadedFileMetadata(

    String bucket,
    String key
) {
}
