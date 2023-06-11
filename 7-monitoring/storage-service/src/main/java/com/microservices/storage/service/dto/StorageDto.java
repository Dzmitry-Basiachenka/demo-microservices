package com.microservices.storage.service.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public record StorageDto(

    @Null
    Long id,

    @NotNull
    StorageType type,

    @NotBlank
    String bucket
) {
}
