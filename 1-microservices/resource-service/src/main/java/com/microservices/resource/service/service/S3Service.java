package com.microservices.resource.service.service;

import com.microservices.resource.service.dto.UploadedFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public UploadedFileMetadata uploadFile(MultipartFile multipartFile, String bucket) {
        logger.info("Upload file {} to bucket {}", multipartFile, bucket);

        createBucketIfNotExists(bucket);

        var key = UUID.randomUUID().toString();

        var putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(multipartFile.getContentType())
            .contentLength(multipartFile.getSize())
            .build();

        logger.info("Put object request: {}", putObjectRequest);
        var putObjectResponse = s3Client.putObject(
            putObjectRequest,
            getRequestBody(multipartFile)
        );
        logger.info("Put object response: {}", putObjectResponse);

        return new UploadedFileMetadata(
            bucket,
            key
        );
    }

    private RequestBody getRequestBody(MultipartFile multipartFile) {
        try {
            return RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadFile(String bucket, String key) {
        logger.info("Download file from bucket {} and key {}", bucket, key);

        var getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        logger.info("Get object request: {}", getObjectRequest);
        var getObjectResponse = s3Client.getObjectAsBytes(getObjectRequest);
        //logger.info("Get object response: {}", getObjectResponse);

        return getObjectResponse.asByteArray();
    }

    public void deleteFile(String bucket, String key) {
        logger.info("Delete file from bucket {} and key {}", bucket, key);

        var deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        logger.info("Delete object request: {}", deleteObjectRequest);
        var deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);
        logger.info("Delete object response: {}", deleteObjectResponse);
    }

    void createBucketIfNotExists(String bucket) {
        logger.info("Create bucket {} if not exists", bucket);

        if (isBucketExist(bucket)) {
            logger.info("Bucket {} already exists", bucket);
            return;
        }

        var createBucketRequest = CreateBucketRequest.builder()
            .bucket(bucket)
            .build();

        logger.info("Create bucket request: {}", createBucketRequest);
        var createBucketResponse = s3Client.createBucket(createBucketRequest);
        logger.info("Create bucket response: {}", createBucketResponse);
    }

    private boolean isBucketExist(String bucket) {
        var headBucketRequest = HeadBucketRequest.builder()
            .bucket(bucket)
            .build();
        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
