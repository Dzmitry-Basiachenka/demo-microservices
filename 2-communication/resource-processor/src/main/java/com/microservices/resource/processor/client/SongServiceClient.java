package com.microservices.resource.processor.client;

import com.microservices.resource.processor.config.FeignConfig;
import com.microservices.resource.processor.dto.SongCreatedResponse;
import com.microservices.resource.processor.dto.SongDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    value = "${com.microservices.service.song.name}",
    url = "${com.microservices.service.song.url}",
    configuration = FeignConfig.class
)
public interface SongServiceClient {

    @PostMapping("/songs")
    SongCreatedResponse createSong(@RequestBody SongDto songDto);
}
