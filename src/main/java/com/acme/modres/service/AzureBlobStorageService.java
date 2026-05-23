package com.acme.modres.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Azure Blob Storage Service for cloud-native file operations
 * Replaces local file system dependencies with Azure Blob Storage
 */
@Service
public class AzureBlobStorageService {
    
    private static final Logger logger = Logger.getLogger(AzureBlobStorageService.class.getName());
    
    @Value("${azure.storage.account-name:#{null}}")
    private String storageAccountName;
    
    @Value("${azure.storage.container-name:modresorts-data}")
    private String containerName;
    
    @Value("${azure.storage.connection-string:#{null}}")
    private String connectionString;
    
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;
    
    @PostConstruct
    public void init() {
        try {
            if (connectionString != null && !connectionString.isEmpty()) {
                // Use connection string if provided
                blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            } else if (storageAccountName != null && !storageAccountName.isEmpty()) {
                // Use Managed Identity authentication
                String endpoint = String.format("https://%s.blob.core.windows.net", storageAccountName);
                blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
            } else {
                logger.warning("Azure Storage not configured. File operations will use fallback to classpath resources.");
                return;
            }
            
            containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
                logger.info("Created Azure Blob Storage container: " + containerName);
            }
        } catch (Exception e) {
            logger.warning("Failed to initialize Azure Blob Storage: " + e.getMessage() + ". Using fallback mode.");
        }
    }
    
    /**
     * Upload data to Azure Blob Storage
     */
    public void uploadBlob(String blobName, byte[] data) {
        if (containerClient == null) {
            logger.warning("Azure Blob Storage not available. Cannot upload: " + blobName);
            return;
        }
        
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(new ByteArrayInputStream(data), data.length, true);
            logger.info("Uploaded blob: " + blobName);
        } catch (Exception e) {
            logger.severe("Failed to upload blob " + blobName + ": " + e.getMessage());
            throw new RuntimeException("Failed to upload to Azure Blob Storage", e);
        }
    }
    
    /**
     * Download data from Azure Blob Storage
     */
    public byte[] downloadBlob(String blobName) {
        if (containerClient == null) {
            logger.warning("Azure Blob Storage not available. Cannot download: " + blobName);
            return null;
        }
        
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if (!blobClient.exists()) {
                logger.warning("Blob does not exist: " + blobName);
                return null;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);
            logger.info("Downloaded blob: " + blobName);
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.severe("Failed to download blob " + blobName + ": " + e.getMessage());
            throw new RuntimeException("Failed to download from Azure Blob Storage", e);
        }
    }
    
    /**
     * Check if blob exists
     */
    public boolean blobExists(String blobName) {
        if (containerClient == null) {
            return false;
        }
        
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            logger.warning("Failed to check blob existence: " + blobName);
            return false;
        }
    }
    
    /**
     * Delete blob from Azure Blob Storage
     */
    public void deleteBlob(String blobName) {
        if (containerClient == null) {
            logger.warning("Azure Blob Storage not available. Cannot delete: " + blobName);
            return;
        }
        
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
            logger.info("Deleted blob: " + blobName);
        } catch (Exception e) {
            logger.severe("Failed to delete blob " + blobName + ": " + e.getMessage());
        }
    }
    
    public boolean isAvailable() {
        return containerClient != null;
    }
}
