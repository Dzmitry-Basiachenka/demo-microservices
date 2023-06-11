package com.microservices.resource.service.service;

import com.microservices.resource.service.client.StorageServiceClient;
import com.microservices.resource.service.dto.StorageDto;
import com.microservices.resource.service.dto.StorageType;
import com.microservices.resource.service.exception.NotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    private final StorageServiceClient storageServiceClient;

    public StorageService(StorageServiceClient storageServiceClient) {
        this.storageServiceClient = storageServiceClient;
    }

    @CircuitBreaker(name = "storage-service-client", fallbackMethod = "getAllStoragesFallback")
    public List<StorageDto> getAllStorages() {
        return storageServiceClient.getAllStorages();
    }

    private List<StorageDto> getAllStoragesFallback(Exception e) {
        logger.warn("Failed to read storages, use a local fallback instead");

        return List.of(
            new StorageDto(1L, StorageType.STAGING, "resources-staging"),
            new StorageDto(2L, StorageType.PERMANENT, "resources-permanent")
        );
    }

    public StorageDto getStorageById(long storageId) {
        List<StorageDto> storages = getAllStorages();
        return storages
            .stream()
            .filter(storageDto -> storageId == storageDto.id())
            .findAny()
            .orElseThrow(() -> new NotFoundException(String.format("Storage with id %s not found", storageId)));
    }

    public StorageDto getStagingStorage() {
        List<StorageDto> storages = getAllStorages();
        return storages
            .stream()
            .filter(storageDto -> StorageType.STAGING.equals(storageDto.type()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(String.format("Staging storage not found in storages: %s", storages)));
    }

    public StorageDto getPermanentStorage() {
        List<StorageDto> storages = getAllStorages();
        return storages
            .stream()
            .filter(storageDto -> StorageType.PERMANENT.equals(storageDto.type()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(String.format("Permanent storage not found in storages: %s", storages)));
    }
}
