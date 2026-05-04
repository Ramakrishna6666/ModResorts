package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Spring Boot Application class for ModResorts
 * Enables Spring Boot Actuator health check endpoint at /actuator/health
 * This provides container-ready health monitoring for GKE, Cloud Run, and App Engine
 */
@SpringBootApplication
@ServletComponentScan
public class ModResortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }
}
