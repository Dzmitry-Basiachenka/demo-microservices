package com.microservices.resource.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record StorageDto(

    @NotNull
    Long id,

    @NotNull
    StorageType type,

    @NotBlank
    String bucket
) {
}
