package com.microservices.storage.service.dto;

import java.util.List;

public record StoragesDeletedResponse(

    List<Long> ids
) {
}
