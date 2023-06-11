package com.microservices.resource.service.service;

import com.microservices.resource.service.dto.ResourceCompletedResponse;
import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.dto.StorageType;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.exception.BadRequestException;
import com.microservices.resource.service.exception.NotFoundException;
import com.microservices.resource.service.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final StorageService storageService;
    private final S3Service s3Service;
    private final ResourcePublisher resourcePublisher;

    public ResourceService(ResourceRepository resourceRepository, StorageService storageService, S3Service s3Service, ResourcePublisher resourcePublisher) {
        this.resourceRepository = resourceRepository;
        this.storageService = storageService;
        this.s3Service = s3Service;
        this.resourcePublisher = resourcePublisher;
    }

    @Transactional
    public ResourceUploadedResponse uploadResource(MultipartFile multipartFile) {
        if (!CONTENT_TYPE_AUDIO_MPEG.equalsIgnoreCase(multipartFile.getContentType())) {
            throw new BadRequestException(String.format("Content type %s is not supported", multipartFile.getContentType()));
        }

        var stagingStorageDto = storageService.getStagingStorage();
        var bucket = stagingStorageDto.bucket();

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, bucket);

        var resourceEntity = new ResourceEntity();
        resourceEntity.setStorageId(stagingStorageDto.id());
        resourceEntity.setKey(uploadedFileMetadata.key());
        resourceEntity.setName(multipartFile.getOriginalFilename());
        resourceEntity.setSize(multipartFile.getSize());

        var savedResourceEntity = resourceRepository.save(resourceEntity);
        resourcePublisher.publish(savedResourceEntity);

        return new ResourceUploadedResponse(savedResourceEntity.getId());
    }

    public ResourceEntity getResource(Long id) {
        return resourceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Resource with id %s not found", id)));
    }

    public byte[] downloadResource(ResourceEntity resourceEntity) {
        var storageDto = storageService.getStorageById(resourceEntity.getStorageId());
        return s3Service.downloadFile(storageDto.bucket(), resourceEntity.getKey());
    }

    @Transactional
    public ResourceCompletedResponse completeResourceUpload(Long id) {
        var resourceEntity = resourceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Resource with id %s not found", id)));

        var stagingStorageDto = storageService.getStorageById(resourceEntity.getStorageId());

        if (StorageType.PERMANENT.equals(stagingStorageDto.type())) {
            logger.warn("Resource upload already completed: {}", resourceEntity);
        } else {
            var permanentStorageDto = storageService.getPermanentStorage();
            s3Service.copyFile(stagingStorageDto.bucket(), permanentStorageDto.bucket(), resourceEntity.getKey());
            s3Service.deleteFile(stagingStorageDto.bucket(), resourceEntity.getKey());

            resourceEntity.setStorageId(permanentStorageDto.id());
            resourceRepository.save(resourceEntity);
        }

        return new ResourceCompletedResponse(resourceEntity.getId(), resourceEntity.getStorageId());
    }

    @Transactional
    public ResourcesDeletedResponse deleteResources(List<Long> ids) {
        var deletedIds = resourceRepository.findAllById(ids)
            .stream()
            .map(resource -> {
                var storageDto = storageService.getStorageById(resource.getStorageId());
                s3Service.deleteFile(storageDto.bucket(), resource.getKey());
                resourceRepository.deleteById(resource.getId());
                return resource.getId();
            })
            .collect(Collectors.toList());

        return new ResourcesDeletedResponse(deletedIds);
    }
}
