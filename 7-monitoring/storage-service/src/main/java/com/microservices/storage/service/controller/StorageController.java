package com.microservices.storage.service.controller;

import com.microservices.storage.service.dto.StorageCreatedResponse;
import com.microservices.storage.service.dto.StorageDto;
import com.microservices.storage.service.dto.StoragesDeletedResponse;
import com.microservices.storage.service.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequestMapping("/storages")
@RestController
public class StorageController {

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    private final AtomicInteger simulatedDelaySeconds = new AtomicInteger(0);

    @Value("${com.microservices.simulate.error}")
    private boolean simulateError;

    @Value("${com.microservices.simulate.delay}")
    private boolean simulateDelay;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StorageCreatedResponse> createStorage(@RequestBody @Valid StorageDto storageDto) {
        logger.info("Create storage: {}", storageDto);
        return ResponseEntity.ok(storageService.createStorage(storageDto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StorageDto>> getAllStorages() {
        logger.info("Get all storages");
        if (simulateError) {
            logger.warn("Exception simulated");
            throw new RuntimeException("Simulated exception");
        }
        if (simulateDelay) {
            logger.warn("Delay {} second(s) simulated", simulatedDelaySeconds.get());
            delay(simulatedDelaySeconds.getAndIncrement());
        }
        return ResponseEntity.ok(storageService.getAllStorages());
    }

    private void delay(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoragesDeletedResponse> deleteStorages(@RequestParam List<Long> ids) {
        logger.info("Delete storages by ids: {}", ids);
        var deletedIds = storageService.deleteResponses(ids);
        return ResponseEntity.ok(deletedIds);
    }
}
