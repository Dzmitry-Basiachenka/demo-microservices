package com.microservices.resource.service.client;

import com.microservices.resource.service.config.FeignConfig;
import com.microservices.resource.service.dto.StorageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "${com.microservices.service.storage.name}",
    url = "${com.microservices.service.api-gateway.url}",
    path = "/storages",
    configuration = FeignConfig.class
)
public interface StorageServiceClient {

    @GetMapping
    List<StorageDto> getAllStorages();
}
