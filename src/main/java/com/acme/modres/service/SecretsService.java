package com.acme.modres.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.logging.Logger;

/**
 * Service for retrieving secrets from AWS Secrets Manager.
 * Replaces hardcoded credentials and API keys with secure cloud-native secret management.
 */
@Service
public class SecretsService {
    
    private static final Logger logger = Logger.getLogger(SecretsService.class.getName());
    
    @Autowired
    private SecretsManagerClient secretsManagerClient;
    
    /**
     * Retrieve a secret value from AWS Secrets Manager
     */
    public String getSecret(String secretName) {
        try {
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            
            GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
            String secret = getSecretValueResponse.secretString();
            
            logger.info("Successfully retrieved secret: " + secretName);
            return secret;
        } catch (Exception e) {
            logger.warning("Failed to retrieve secret " + secretName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Retrieve a secret with fallback to environment variable
     */
    public String getSecretWithFallback(String secretName, String envVarName) {
        String secret = getSecret(secretName);
        if (secret == null || secret.trim().isEmpty()) {
            logger.info("Secret not found in AWS Secrets Manager, falling back to environment variable: " + envVarName);
            return System.getenv(envVarName);
        }
        return secret;
    }
}
