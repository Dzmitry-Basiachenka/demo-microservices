package com.microservices.resource.service.controller;

import com.microservices.resource.service.AbstractIntegrationTest;
import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.repository.ResourceRepository;
import com.microservices.resource.service.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.microservices.resource.service.TestConstants.UUID_REGEXP;
import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResourceControllerApplicationTest extends AbstractIntegrationTest {

    private static final String URL_PATH = "/resources";
    private static final String FILE_PATH = "/audio/audio1.mp3";
    private static final String BUCKET = "resources";
    private static final String KEY = "11111111-2222-3333-4444-555555555555";
    private static final String FILE_NAME = "audio1.mp3";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private S3Service s3Service;

    @BeforeEach
    void init() {
        resourceRepository.deleteAll();
    }

    @Test
    void shouldUploadResource() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var requestEntity = new HttpEntity<>(getMultipartBody(content), getMultipartHeaders());

        var responseEntity = testRestTemplate.postForEntity(URL_PATH, requestEntity, ResourceUploadedResponse.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        var resourceUploadedResponse = responseEntity.getBody();
        assertNotNull(resourceUploadedResponse);
        assertNotNull(resourceUploadedResponse.id());

        var foundResourceEntity = resourceRepository.findById(resourceUploadedResponse.id());
        assertTrue(foundResourceEntity.isPresent());

        var actualResourceEntity = foundResourceEntity.get();
        assertEquals(resourceUploadedResponse.id(), actualResourceEntity.getId());
        assertEquals(BUCKET, actualResourceEntity.getBucket());
        assertTrue(Pattern.matches(UUID_REGEXP, actualResourceEntity.getKey()));
        assertEquals(FILE_NAME, actualResourceEntity.getName());
        assertEquals(content.length, actualResourceEntity.getSize());
    }

    private MultiValueMap<String, Object> getMultipartBody(byte[] content) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        ContentDisposition contentDisposition = ContentDisposition
            .builder("form-data")
            .name("file")
            .filename(FILE_NAME)
            .build();

        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        headers.add(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_AUDIO_MPEG);

        HttpEntity<byte[]> fileEntity = new HttpEntity<>(content, headers);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);
        return body;
    }

    private HttpHeaders getMultipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    @Test
    void shouldDownloadResource() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, BUCKET);
        assertEquals(BUCKET, uploadedFileMetadata.bucket());
        assertTrue(Pattern.matches(UUID_REGEXP, uploadedFileMetadata.key()));

        var resourceEntity = new ResourceEntity();
        resourceEntity.setBucket(BUCKET);
        resourceEntity.setKey(uploadedFileMetadata.key());
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize((long) content.length);
        var savedResourceEntity = resourceRepository.save(resourceEntity);

        var responseEntity = testRestTemplate.getForEntity(URL_PATH + "/" + savedResourceEntity.getId() + "/download", ByteArrayResource.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(CONTENT_TYPE_AUDIO_MPEG, responseEntity.getHeaders().getContentType().toString());

        assertNotNull(responseEntity.getBody());
        assertArrayEquals(content, responseEntity.getBody().getByteArray());
    }

    @Test
    void shouldDeleteResource() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();

        var resourceEntity = new ResourceEntity();
        resourceEntity.setBucket(BUCKET);
        resourceEntity.setKey(KEY);
        resourceEntity.setName(FILE_NAME);
        resourceEntity.setSize((long) content.length);

        var savedResourceEntity = resourceRepository.save(resourceEntity);

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var responseEntity = testRestTemplate.exchange(
            UriComponentsBuilder.fromUriString(URL_PATH).queryParam("ids", savedResourceEntity.getId()).build().toUri(),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            ResourcesDeletedResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        var resourceDeletedResponse = responseEntity.getBody();
        assertNotNull(resourceDeletedResponse);
        assertNotNull(resourceDeletedResponse.ids());
        assertEquals(1, resourceDeletedResponse.ids().size());

        var foundResourceEntities = resourceRepository.findAllById(resourceDeletedResponse.ids());
        assertNotNull(foundResourceEntities);
        assertTrue(foundResourceEntities.isEmpty());
    }
}
