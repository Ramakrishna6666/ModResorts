package com.acme.modres.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Azure Key Vault Service for secure secret management
 * Replaces hard-coded credentials with cloud-native secret management
 */
@Service
public class AzureKeyVaultService {
    
    private static final Logger logger = Logger.getLogger(AzureKeyVaultService.class.getName());
    
    @Value("${azure.keyvault.uri:#{null}}")
    private String keyVaultUri;
    
    private SecretClient secretClient;
    
    @PostConstruct
    public void init() {
        try {
            if (keyVaultUri != null && !keyVaultUri.isEmpty()) {
                secretClient = new SecretClientBuilder()
                    .vaultUrl(keyVaultUri)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
                logger.info("Azure Key Vault initialized successfully");
            } else {
                logger.warning("Azure Key Vault URI not configured. Secrets will fall back to environment variables.");
            }
        } catch (Exception e) {
            logger.warning("Failed to initialize Azure Key Vault: " + e.getMessage() + ". Using fallback mode.");
        }
    }
    
    /**
     * Get secret from Azure Key Vault with fallback to environment variable
     */
    public String getSecret(String secretName) {
        // Try Azure Key Vault first
        if (secretClient != null) {
            try {
                KeyVaultSecret secret = secretClient.getSecret(secretName);
                if (secret != null && secret.getValue() != null) {
                    logger.info("Retrieved secret from Azure Key Vault: " + secretName);
                    return secret.getValue();
                }
            } catch (Exception e) {
                logger.warning("Failed to retrieve secret from Key Vault: " + secretName + ". Falling back to environment variable.");
            }
        }
        
        // Fallback to environment variable
        String envValue = System.getenv(secretName);
        if (envValue != null && !envValue.isEmpty()) {
            logger.info("Retrieved secret from environment variable: " + secretName);
            return envValue;
        }
        
        logger.warning("Secret not found: " + secretName);
        return null;
    }
    
    /**
     * Set secret in Azure Key Vault
     */
    public void setSecret(String secretName, String secretValue) {
        if (secretClient == null) {
            logger.warning("Azure Key Vault not available. Cannot set secret: " + secretName);
            return;
        }
        
        try {
            secretClient.setSecret(secretName, secretValue);
            logger.info("Set secret in Azure Key Vault: " + secretName);
        } catch (Exception e) {
            logger.severe("Failed to set secret in Key Vault: " + secretName + ": " + e.getMessage());
            throw new RuntimeException("Failed to set secret in Azure Key Vault", e);
        }
    }
    
    public boolean isAvailable() {
        return secretClient != null;
    }
}
