package com.microservices.resource.processor.client;

import com.microservices.resource.processor.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    value = "${com.microservices.service.resource.name}",
    url = "${com.microservices.service.resource.url}",
    configuration = FeignConfig.class
)
public interface ResourceServiceClient {

    @GetMapping("/{id}/download")
    ByteArrayResource downloadResource(@PathVariable Long id);
}
