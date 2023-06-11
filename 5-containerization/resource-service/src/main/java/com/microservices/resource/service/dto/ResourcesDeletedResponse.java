package com.microservices.resource.service.dto;

import java.util.List;

public record ResourcesDeletedResponse(

    List<Long> ids
) {
}
