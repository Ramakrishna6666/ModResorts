package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Spring Boot application main class for ModResorts.
 * Enables cloud-native deployment with embedded Tomcat server.
 */
@SpringBootApplication
@ServletComponentScan
@EnableRedisHttpSession
public class ModResortsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ModResortsApplication.class, args);
  }

  /**
   * AWS S3 Client bean for cloud storage operations
   */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder().build();
  }

  /**
   * AWS Secrets Manager Client bean for secure credential management
   */
  @Bean
  public SecretsManagerClient secretsManagerClient() {
    return SecretsManagerClient.builder().build();
  }
}
