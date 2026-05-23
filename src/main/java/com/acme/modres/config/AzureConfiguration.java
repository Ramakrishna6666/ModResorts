package com.acme.modres.config;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.service.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Azure Configuration for cloud-native services
 * Initializes Azure Blob Storage, Key Vault, and Service Bus
 */
@Configuration
public class AzureConfiguration {
    
    private static final Logger logger = Logger.getLogger(AzureConfiguration.class.getName());
    
    @Autowired(required = false)
    private AzureBlobStorageService blobStorageService;
    
    @PostConstruct
    public void init() {
        logger.info("Initializing Azure cloud services...");
        
        // Inject blob storage service into IOUtils for static access
        if (blobStorageService != null) {
            IOUtils.setBlobStorageService(blobStorageService);
            logger.info("Azure Blob Storage service configured for IOUtils");
        } else {
            logger.warning("Azure Blob Storage service not available. Using classpath resources only.");
        }
        
        logger.info("Azure cloud services initialization complete");
    }
}
