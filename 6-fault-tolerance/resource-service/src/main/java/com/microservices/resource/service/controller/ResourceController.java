package com.microservices.resource.service.controller;

import com.microservices.resource.service.dto.ResourceCompletedResponse;
import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.service.ResourceService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;

@RequestMapping("/resources")
@RestController
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceUploadedResponse> uploadResource(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(resourceService.uploadResource(multipartFile));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResourceEntity> getResource(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @GetMapping(value = "/{id}/download", produces = CONTENT_TYPE_AUDIO_MPEG)
    public ResponseEntity<ByteArrayResource> downloadResource(@PathVariable Long id) {
        var resourceEntry = resourceService.getResource(id);
        var content = resourceService.downloadResource(resourceEntry);
        ContentDisposition contentDisposition = ContentDisposition
            .builder("attachment")
            .name("filename")
            .filename(resourceEntry.getName())
            .build();
        return ResponseEntity.ok()
            .contentLength(content.length)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_AUDIO_MPEG)
            .body(new ByteArrayResource(content));
    }

    @PutMapping(value = "/{id}/complete")
    public ResponseEntity<ResourceCompletedResponse> completeResourceUpload(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.completeResourceUpload(id));
    }

    @DeleteMapping
    public ResponseEntity<ResourcesDeletedResponse> deleteResources(@RequestParam List<Long> ids) {
        var deletedIds = resourceService.deleteResources(ids);
        return ResponseEntity.ok(deletedIds);
    }
}
