package com.microservices.resource.service.service;

import com.microservices.resource.service.dto.StorageDto;
import com.microservices.resource.service.dto.StorageType;
import com.microservices.resource.service.dto.UploadedFileMetadata;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.exception.BadRequestException;
import com.microservices.resource.service.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
    private static final String KEY = "11111111-2222-3333-4444-555555555555";
    private static final String FILE_NAME = "audio.mp3";
    private static final byte[] FILE_CONTENT = new byte[]{0};
    private static final long FILE_SIZE = FILE_CONTENT.length;

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ResourcePublisher resourcePublisher;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void shouldFailWhenUploadingNonAudioContentType() {
        when(multipartFile.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        assertThrows(BadRequestException.class, () -> resourceService.uploadResource(multipartFile));

        verify(multipartFile, times(2)).getContentType();
        verifyNoMoreInteractions(multipartFile);
        verifyNoInteractions(resourceRepository, storageService, s3Service, resourcePublisher);
    }

    @Test
    void shouldUploadResource() {
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_AUDIO_MPEG);
        when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
        when(multipartFile.getSize()).thenReturn(FILE_SIZE);

        StorageDto stagingStorageDto = getStagingStorageDto();
        when(storageService.getStagingStorage()).thenReturn(stagingStorageDto);

        var uploadedFileMetadata = new UploadedFileMetadata(stagingStorageDto.bucket(), KEY);
        when(s3Service.uploadFile(multipartFile, stagingStorageDto.bucket())).thenReturn(uploadedFileMetadata);

        var resourceEntity = new ResourceEntity();
        resourceEntity.setStorageId(stagingStorageDto.id());
        resourceEntity.setKey(KEY);
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize(FILE_SIZE);

        var savedResourceEntry = new ResourceEntity();
        savedResourceEntry.setId(ID);
        savedResourceEntry.setStorageId(stagingStorageDto.id());
        savedResourceEntry.setKey(KEY);
        savedResourceEntry.setName(FILE_NAME);
        savedResourceEntry.setSize(FILE_SIZE);
        when(resourceRepository.save(resourceEntity)).thenReturn(savedResourceEntry);

        var resourceUploadedResponse = resourceService.uploadResource(multipartFile);

        assertEquals(savedResourceEntry.getId(), resourceUploadedResponse.id());

        verify(multipartFile).getContentType();
        verify(multipartFile).getOriginalFilename();
        verify(multipartFile).getSize();
        verify(storageService).getStagingStorage();
        verify(s3Service).uploadFile(multipartFile, stagingStorageDto.bucket());
        verify(resourceRepository).save(resourceEntity);
        verify(resourcePublisher).publish(savedResourceEntry);
        verifyNoMoreInteractions(resourceRepository, storageService, s3Service, resourcePublisher, multipartFile);
    }

    @Test
    void shouldGetResource() {
        ResourceEntity resourceEntity = getResourceEntity();
        var id = resourceEntity.getId();
        when(resourceRepository.findById(id)).thenReturn(Optional.of(resourceEntity));

        var actualResourceEntry = resourceService.getResource(id);

        assertEquals(id, actualResourceEntry.getId());
        assertEquals(resourceEntity.getStorageId(), actualResourceEntry.getStorageId());
        assertEquals(resourceEntity.getKey(), actualResourceEntry.getKey());
        assertEquals(resourceEntity.getName(), actualResourceEntry.getName());
        assertEquals(resourceEntity.getSize(), actualResourceEntry.getSize());

        verify(resourceRepository).findById(id);
        verifyNoMoreInteractions(resourceRepository);
        verifyNoInteractions(storageService, s3Service, resourcePublisher, multipartFile);
    }

    @Test
    void shouldDownloadResource() {
        ResourceEntity resourceEntity = getResourceEntity();

        StorageDto stagingStorageDto = getStagingStorageDto();
        when(storageService.getStorageById(stagingStorageDto.id())).thenReturn(stagingStorageDto);

        when(s3Service.downloadFile(stagingStorageDto.bucket(), resourceEntity.getKey())).thenReturn(FILE_CONTENT);

        var actualContent = resourceService.downloadResource(resourceEntity);

        assertArrayEquals(FILE_CONTENT, actualContent);

        verify(storageService).getStorageById(stagingStorageDto.id());
        verify(s3Service).downloadFile(stagingStorageDto.bucket(), resourceEntity.getKey());
        verifyNoMoreInteractions(storageService, s3Service);
        verifyNoInteractions(resourceRepository, resourcePublisher, multipartFile);
    }

    @Test
    void shouldCompleteResourceUpload() {
        ResourceEntity resourceEntity = getResourceEntity();
        var id = resourceEntity.getId();
        when(resourceRepository.findById(id)).thenReturn(Optional.of(resourceEntity));

        StorageDto stagingStorageDto = getStagingStorageDto();
        when(storageService.getStorageById(stagingStorageDto.id())).thenReturn(stagingStorageDto);
        StorageDto permanentStorageDto = getPermanentStorageDto();
        when(storageService.getPermanentStorage()).thenReturn(permanentStorageDto);

        doNothing().when(s3Service).copyFile(stagingStorageDto.bucket(), permanentStorageDto.bucket(), resourceEntity.getKey());
        doNothing().when(s3Service).deleteFile(stagingStorageDto.bucket(), resourceEntity.getKey());

        var savedResourceEntity = new ResourceEntity();
        savedResourceEntity.setId(id);
        savedResourceEntity.setStorageId(permanentStorageDto.id());
        savedResourceEntity.setKey(resourceEntity.getKey());
        savedResourceEntity.setName(resourceEntity.getName());
        savedResourceEntity.setSize(resourceEntity.getSize());
        when(resourceRepository.save(savedResourceEntity)).thenReturn(savedResourceEntity);

        resourceService.completeResourceUpload(id);

        verify(resourceRepository).findById(id);
        verify(storageService).getStorageById(stagingStorageDto.id());
        verify(storageService).getPermanentStorage();
        verify(s3Service).copyFile(stagingStorageDto.bucket(), permanentStorageDto.bucket(), resourceEntity.getKey());
        verify(s3Service).deleteFile(stagingStorageDto.bucket(), resourceEntity.getKey());
        verify(resourceRepository).save(savedResourceEntity);
        verifyNoMoreInteractions(resourceRepository, storageService, s3Service);
        verifyNoInteractions(resourcePublisher);
    }

    @Test
    void shouldDeleteResources() {
        ResourceEntity resourceEntity = getResourceEntity();
        var ids = List.of(resourceEntity.getId());
        when(resourceRepository.findAllById(ids)).thenReturn(List.of(resourceEntity));

        StorageDto stagingStorageDto = getStagingStorageDto();
        when(storageService.getStorageById(stagingStorageDto.id())).thenReturn(stagingStorageDto);

        doNothing().when(resourceRepository).deleteById(resourceEntity.getId());

        var resourcesDeletedResponse = resourceService.deleteResources(ids);

        assertEquals(ids, resourcesDeletedResponse.ids());

        verify(resourceRepository).findAllById(ids);
        verify(storageService).getStorageById(stagingStorageDto.id());
        verify(s3Service).deleteFile(stagingStorageDto.bucket(), resourceEntity.getKey());
        verify(resourceRepository).deleteById(resourceEntity.getId());
        verifyNoMoreInteractions(storageService, s3Service, resourceRepository);
        verifyNoInteractions(resourcePublisher);
    }

    private ResourceEntity getResourceEntity() {
        var resourceEntity = new ResourceEntity();
        resourceEntity.setId(ID);
        resourceEntity.setStorageId(getStagingStorageDto().id());
        resourceEntity.setKey(KEY);
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize(FILE_SIZE);
        return resourceEntity;
    }

    private StorageDto getStagingStorageDto() {
        return new StorageDto(
            1000L,
            StorageType.STAGING,
            "resources-staging"
        );
    }

    private StorageDto getPermanentStorageDto() {
        return new StorageDto(
            1001L,
            StorageType.PERMANENT,
            "resources-permanent"
        );
    }
}
