package com.microservices.resource.service.service;

import com.microservices.resource.service.config.properties.S3Properties;
import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.exception.BadRequestException;
import com.microservices.resource.service.exception.NotFoundException;
import com.microservices.resource.service.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final S3Properties s3Properties;
    private final S3Service s3Service;
    private final ResourcePublisher resourcePublisher;

    public ResourceService(ResourceRepository resourceRepository, S3Properties s3Properties, S3Service s3Service, ResourcePublisher resourcePublisher) {
        this.resourceRepository = resourceRepository;
        this.s3Properties = s3Properties;
        this.s3Service = s3Service;
        this.resourcePublisher = resourcePublisher;
    }

    @Transactional
    public ResourceUploadedResponse uploadResource(MultipartFile multipartFile) {
        if (!CONTENT_TYPE_AUDIO_MPEG.equalsIgnoreCase(multipartFile.getContentType())) {
            throw new BadRequestException(String.format("Content type %s is not supported", multipartFile.getContentType()));
        }

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, s3Properties.bucket());

        var resourceEntity = new ResourceEntity();
        resourceEntity.setBucket(uploadedFileMetadata.bucket());
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
        return s3Service.downloadFile(resourceEntity.getBucket(), resourceEntity.getKey());
    }

    @Transactional
    public ResourcesDeletedResponse deleteResources(List<Long> ids) {
        var deletedIds = resourceRepository.findAllById(ids)
            .stream()
            .map(resource -> {
                s3Service.deleteFile(resource.getBucket(), resource.getKey());
                resourceRepository.deleteById(resource.getId());
                return resource.getId();
            })
            .collect(Collectors.toList());

        return new ResourcesDeletedResponse(deletedIds);
    }
}
