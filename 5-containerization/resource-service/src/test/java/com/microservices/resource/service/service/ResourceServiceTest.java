package com.microservices.resource.service.service;

import com.microservices.resource.service.config.properties.S3Properties;
import com.microservices.resource.service.dto.UploadedFileMetadata;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.exception.BadRequestException;
import com.microservices.resource.service.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    private static final long ID = 1L;
    private static final String BUCKET = "resources";
    private static final String KEY = "11111111-2222-3333-4444-555555555555";
    private static final String FILE_NAME = "audio.mp3";
    private static final byte[] FILE_CONTENT = new byte[]{0};
    private static final long FILE_SIZE = FILE_CONTENT.length;

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ResourcePublisher resourcePublisher;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(resourceService, "s3Properties", new S3Properties(null, null, BUCKET));
    }

    @Test
    void shouldFailWhenUploadingNonAudioContentType() {
        when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        assertThrows(BadRequestException.class, () -> resourceService.uploadResource(multipartFile));

        verify(multipartFile, times(2)).getContentType();
        verifyNoMoreInteractions(multipartFile);
        verifyNoInteractions(resourceRepository, s3Service, resourcePublisher);
    }

    @Test
    void shouldUploadResource() {
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_AUDIO_MPEG);
        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);

        var uploadedFileMetadata = new UploadedFileMetadata(BUCKET, KEY);
        when(s3Service.uploadFile(multipartFile, BUCKET)).thenReturn(uploadedFileMetadata);

        var resourceEntity = new ResourceEntity();
        resourceEntity.setBucket(BUCKET);
        resourceEntity.setKey(KEY);
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize(FILE_SIZE);

        var savedResourceEntry = new ResourceEntity();
        savedResourceEntry.setId(ID);
        savedResourceEntry.setBucket(BUCKET);
        savedResourceEntry.setKey(KEY);
        savedResourceEntry.setName(FILE_NAME);
        savedResourceEntry.setSize(FILE_SIZE);
        when(resourceRepository.save(resourceEntity)).thenReturn(savedResourceEntry);

        var resourceUploadedResponse = resourceService.uploadResource(multipartFile);

        assertEquals(savedResourceEntry.getId(), resourceUploadedResponse.id());

        verify(multipartFile).getContentType();
        verify(multipartFile).getOriginalFilename();
        verify(multipartFile).getSize();
        verify(s3Service).uploadFile(multipartFile, BUCKET);
        verify(resourceRepository).save(resourceEntity);
        verify(resourcePublisher).publish(savedResourceEntry);
        verifyNoMoreInteractions(resourceRepository, s3Service, resourcePublisher, multipartFile);
    }

    @Test
    void shouldGetResource() {
        ResourceEntity resourceEntity = getResourceEntity();
        var id = resourceEntity.getId();
        when(resourceRepository.findById(id)).thenReturn(Optional.of(resourceEntity));

        var actualResourceEntry = resourceService.getResource(id);

        assertEquals(id, actualResourceEntry.getId());
        assertEquals(resourceEntity.getBucket(), actualResourceEntry.getBucket());
        assertEquals(resourceEntity.getKey(), actualResourceEntry.getKey());
        assertEquals(resourceEntity.getName(), actualResourceEntry.getName());
        assertEquals(resourceEntity.getSize(), actualResourceEntry.getSize());

        verify(resourceRepository).findById(id);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(s3Service, resourcePublisher, multipartFile);
    }

    @Test
    void shouldDownloadResource() {
        ResourceEntity resourceEntity = getResourceEntity();

        when(s3Service.downloadFile(resourceEntity.getBucket(), resourceEntity.getKey())).thenReturn(FILE_CONTENT);

        var actualContent = resourceService.downloadResource(resourceEntity);

        assertArrayEquals(FILE_CONTENT, actualContent);

        verify(s3Service).downloadFile(resourceEntity.getBucket(), resourceEntity.getKey());
        verifyNoMoreInteractions(s3Service);
        verifyNoInteractions(resourceRepository, resourcePublisher, multipartFile);
    }

    @Test
    void shouldDeleteResources() {
        ResourceEntity resourceEntity = getResourceEntity();
        var ids = List.of(resourceEntity.getId());
        when(resourceRepository.findAllById(ids)).thenReturn(List.of(resourceEntity));

        doNothing().when(resourceRepository).deleteById(resourceEntity.getId());

        var resourcesDeletedResponse = resourceService.deleteResources(ids);

        assertEquals(ids, resourcesDeletedResponse.ids());

        verify(resourceRepository).findAllById(ids);
        verify(s3Service).deleteFile(resourceEntity.getBucket(), resourceEntity.getKey());
        verify(resourceRepository).deleteById(resourceEntity.getId());
        verifyNoMoreInteractions(s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }

    private ResourceEntity getResourceEntity() {
        var resourceEntity = new ResourceEntity();
        resourceEntity.setId(ID);
        resourceEntity.setBucket(BUCKET);
        resourceEntity.setKey(KEY);
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize(FILE_SIZE);
        return resourceEntity;
    }
}
