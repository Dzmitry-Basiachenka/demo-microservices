package com.microservices.resource.service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import static com.microservices.resource.service.TestConstants.UUID_REGEXP;
import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    private static final String BUCKET = "resources";
    private static final String SOURCE_BUCKET = "resources-staging";
    private static final String DESTINATION_BUCKET = "resources-permanent";
    private static final String KEY = "11111111-2222-3333-4444-555555555555";

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Client s3Client;

    @Test
    void shouldCreateBucket() {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        when(s3Client.headBucket(headBucketRequest)).thenThrow(NoSuchBucketException.builder().build());

        var createBucketRequest = CreateBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var createBucketResponse = CreateBucketResponse.builder().build();
        when(s3Client.createBucket(createBucketRequest)).thenReturn(createBucketResponse);

        s3Service.createBucketIfNotExists(BUCKET);

        verify(s3Client).headBucket(headBucketRequest);
        verify(s3Client).createBucket(createBucketRequest);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldDoNotCreateBucket() {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var headBucketResponse = HeadBucketResponse.builder().build();
        when(s3Client.headBucket(headBucketRequest)).thenReturn(headBucketResponse);

        s3Service.createBucketIfNotExists(BUCKET);

        verify(s3Client).headBucket(headBucketRequest);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldUploadFileFolderDoesNotExist() throws IOException {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        when(s3Client.headBucket(headBucketRequest)).thenThrow(NoSuchBucketException.builder().build());

        var createBucketRequest = CreateBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var createBucketResponse = CreateBucketResponse.builder().build();
        when(s3Client.createBucket(createBucketRequest)).thenReturn(createBucketResponse);

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        PutObjectResponse putObjectResponse = PutObjectResponse.builder().build();

        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        when(s3Client.putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture())).thenReturn(putObjectResponse);

        var multipartFile = mock(MultipartFile.class);
        var content = new byte[]{0};
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_AUDIO_MPEG);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(multipartFile.getSize()).thenReturn((long) content.length);

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, BUCKET);

        assertEquals(BUCKET, uploadedFileMetadata.bucket());
        assertTrue(Pattern.matches(UUID_REGEXP, uploadedFileMetadata.key()));

        var putObjectRequest = putObjectRequestCaptor.getValue();
        assertEquals(BUCKET, putObjectRequest.bucket());
        assertTrue(Pattern.matches(UUID_REGEXP, putObjectRequest.key()));
        assertEquals(uploadedFileMetadata.key(), putObjectRequest.key());

        RequestBody requestBody = requestBodyCaptor.getValue();
        assertArrayEquals(content, requestBody.contentStreamProvider().newStream().readAllBytes());
        assertEquals(content.length, requestBody.optionalContentLength().get());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, requestBody.contentType());

        verify(s3Client).headBucket(headBucketRequest);
        verify(s3Client).createBucket(createBucketRequest);
        verify(multipartFile).getContentType();
        verify(multipartFile).getInputStream();
        verify(multipartFile, times(2)).getSize();
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());
        verifyNoMoreInteractions(multipartFile, s3Client);
    }

    @Test
    void shouldUploadFileFolderExists() throws IOException {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(BUCKET)
            .build();
        var headBucketResponse = HeadBucketResponse.builder().build();
        when(s3Client.headBucket(headBucketRequest)).thenReturn(headBucketResponse);

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        PutObjectResponse putObjectResponse = PutObjectResponse.builder().build();

        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        when(s3Client.putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture())).thenReturn(putObjectResponse);

        var multipartFile = mock(MultipartFile.class);
        var content = new byte[]{0};
        when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE_AUDIO_MPEG);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(multipartFile.getSize()).thenReturn((long) content.length);

        var uploadedFileMetadata = s3Service.uploadFile(multipartFile, BUCKET);

        assertEquals(BUCKET, uploadedFileMetadata.bucket());
        assertTrue(Pattern.matches(UUID_REGEXP, uploadedFileMetadata.key()));

        var putObjectRequest = putObjectRequestCaptor.getValue();
        assertEquals(BUCKET, putObjectRequest.bucket());
        assertTrue(Pattern.matches(UUID_REGEXP, putObjectRequest.key()));
        assertEquals(uploadedFileMetadata.key(), putObjectRequest.key());

        RequestBody requestBody = requestBodyCaptor.getValue();
        assertArrayEquals(content, requestBody.contentStreamProvider().newStream().readAllBytes());
        assertEquals(content.length, requestBody.optionalContentLength().get());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, requestBody.contentType());

        verify(s3Client).headBucket(headBucketRequest);
        verify(multipartFile).getContentType();
        verify(multipartFile).getInputStream();
        verify(multipartFile, times(2)).getSize();
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());
        verifyNoMoreInteractions(multipartFile, s3Client);
    }

    @Test
    void shouldDownloadFile() {
        var getObjectRequest = GetObjectRequest.builder()
            .bucket(BUCKET)
            .key(KEY)
            .build();
        var content = new byte[]{0};
        ResponseBytes<GetObjectResponse> getObjectResponse = ResponseBytes.fromByteArray(
            GetObjectResponse.builder().build(),
            content
        );
        when(s3Client.getObjectAsBytes(getObjectRequest)).thenReturn(getObjectResponse);

        assertArrayEquals(content, s3Service.downloadFile(BUCKET, KEY));

        verify(s3Client).getObjectAsBytes(getObjectRequest);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldCopyFileDestinationFolderDoesNotExist() {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(DESTINATION_BUCKET)
            .build();
        when(s3Client.headBucket(headBucketRequest)).thenThrow(NoSuchBucketException.builder().build());

        var createBucketRequest = CreateBucketRequest.builder()
            .bucket(DESTINATION_BUCKET)
            .build();
        var createBucketResponse = CreateBucketResponse.builder().build();
        when(s3Client.createBucket(createBucketRequest)).thenReturn(createBucketResponse);

        var copyObjectRequest = CopyObjectRequest.builder()
            .sourceBucket(SOURCE_BUCKET)
            .sourceKey(KEY)
            .destinationBucket(DESTINATION_BUCKET)
            .destinationKey(KEY)
            .build();
        var copyObjectResponse = CopyObjectResponse.builder().build();
        when(s3Client.copyObject(copyObjectRequest)).thenReturn(copyObjectResponse);

        s3Service.copyFile(SOURCE_BUCKET, DESTINATION_BUCKET, KEY);

        verify(s3Client).headBucket(headBucketRequest);
        verify(s3Client).createBucket(createBucketRequest);
        verify(s3Client).copyObject(copyObjectRequest);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldCopyFileDestinationFolderExists() {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(DESTINATION_BUCKET)
            .build();
        var headBucketResponse = HeadBucketResponse.builder().build();
        when(s3Client.headBucket(headBucketRequest)).thenReturn(headBucketResponse);

        var copyObjectRequest = CopyObjectRequest.builder()
            .sourceBucket(SOURCE_BUCKET)
            .sourceKey(KEY)
            .destinationBucket(DESTINATION_BUCKET)
            .destinationKey(KEY)
            .build();
        var copyObjectResponse = CopyObjectResponse.builder().build();
        when(s3Client.copyObject(copyObjectRequest)).thenReturn(copyObjectResponse);

        s3Service.copyFile(SOURCE_BUCKET, DESTINATION_BUCKET, KEY);

        verify(s3Client).headBucket(headBucketRequest);
        verify(s3Client).copyObject(copyObjectRequest);
        verifyNoMoreInteractions(s3Client);
    }

    @Test
    void shouldDeleteFile() {
        var deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(BUCKET)
            .key(KEY)
            .build();
        var deleteObjectResponse = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(deleteObjectRequest)).thenReturn(deleteObjectResponse);

        s3Service.deleteFile(BUCKET, KEY);

        verify(s3Client).deleteObject(deleteObjectRequest);
        verifyNoMoreInteractions(s3Client);
    }
}
