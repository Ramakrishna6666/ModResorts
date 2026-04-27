package com.acme.modres.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Service for interacting with Amazon S3 for cloud-native file storage.
 * Replaces local file system operations with durable S3 storage.
 */
@Service
public class S3StorageService {
    
    private static final Logger logger = Logger.getLogger(S3StorageService.class.getName());
    
    @Autowired
    private S3Client s3Client;
    
    private String bucketName = System.getenv().getOrDefault("S3_BUCKET_NAME", "modresorts-data");
    
    /**
     * Upload data to S3
     */
    public void uploadToS3(String key, byte[] data) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
            logger.info("Successfully uploaded " + key + " to S3 bucket " + bucketName);
        } catch (Exception e) {
            logger.severe("Failed to upload to S3: " + e.getMessage());
            throw new RuntimeException("Failed to upload to S3", e);
        }
    }
    
    /**
     * Download data from S3
     */
    public InputStream downloadFromS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            byte[] data = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes()).asByteArray();
            logger.info("Successfully downloaded " + key + " from S3 bucket " + bucketName);
            return new ByteArrayInputStream(data);
        } catch (Exception e) {
            logger.severe("Failed to download from S3: " + e.getMessage());
            throw new RuntimeException("Failed to download from S3", e);
        }
    }
    
    /**
     * Check if object exists in S3
     */
    public boolean existsInS3(String key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
