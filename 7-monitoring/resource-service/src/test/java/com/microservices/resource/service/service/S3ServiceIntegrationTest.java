package com.microservices.resource.service.service;

import com.microservices.resource.service.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.microservices.resource.service.TestConstants.UUID_REGEXP;
import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
public class S3ServiceIntegrationTest extends AbstractIntegrationTest {

    private static final String KEY = "11111111-2222-3333-4444-555555555555";
    private static final String BUCKET = "resources";
    private static final String SOURCE_BUCKET = "resources-staging";
    private static final String DESTINATION_BUCKET = "resources-permanent";
    private static final String FILE_PATH = "/audio/audio1.mp3";
    private static final String FILE_NAME = "audio1.mp3";

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3Client s3Client;

    @Test
    void shouldCreateBucket() {
        s3Service.createBucketIfNotExists(BUCKET);

        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var headBucketResponse = s3Client.headBucket(headBucketRequest);

        assertEquals(200, headBucketResponse.sdkHttpResponse().statusCode());
    }

    @Test
    void shouldDoNotCreateBucket() {
        s3Service.createBucketIfNotExists(BUCKET);

        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var headBucketResponse = s3Client.headBucket(headBucketRequest);

        assertEquals(200, headBucketResponse.sdkHttpResponse().statusCode());

        s3Service.createBucketIfNotExists(BUCKET);
    }

    @Test
    void shouldUploadFile() throws IOException {
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
    }

    @Test
    void shouldDownloadFile() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(KEY)
            .contentType(multipartFile.getContentType())
            .contentLength(multipartFile.getSize())
            .build();

        var putObjectResponse = s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
        );
        assertEquals(200, putObjectResponse.sdkHttpResponse().statusCode());

        var actualContent = s3Service.downloadFile(BUCKET, KEY);

        assertArrayEquals(content, actualContent);
    }

    @Test
    void shouldCopyFile() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, SOURCE_BUCKET);
        var key = uploadedFileMetadata.key();

        assertArrayEquals(content, s3Service.downloadFile(SOURCE_BUCKET, key));
        try {
            s3Service.downloadFile(DESTINATION_BUCKET, key);
            fail();
        } catch (NoSuchBucketException e) {
            assertTrue(true);
        }

        s3Service.copyFile(SOURCE_BUCKET, DESTINATION_BUCKET, key);

        assertArrayEquals(content, s3Service.downloadFile(SOURCE_BUCKET, key));
        assertArrayEquals(content, s3Service.downloadFile(DESTINATION_BUCKET, key));
    }

    @Test
    void shouldDeleteFile() throws IOException {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(KEY)
            .contentType(multipartFile.getContentType())
            .contentLength(multipartFile.getSize())
            .build();

        var putObjectResponse = s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize())
        );
        assertEquals(200, putObjectResponse.sdkHttpResponse().statusCode());

        s3Service.deleteFile(BUCKET, KEY);

        try {
            s3Service.downloadFile(BUCKET, KEY);
            fail();
        } catch (NoSuchKeyException e) {
            assertTrue(true);
        }
    }
}
