package com.acme.modres.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for ModResorts application
 * Provides detailed health status for container orchestration platforms
 */
@Component
public class ModResortsHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Perform basic health checks
            boolean isHealthy = checkApplicationHealth();
            
            if (isHealthy) {
                return Health.up()
                    .withDetail("application", "ModResorts")
                    .withDetail("status", "operational")
                    .withDetail("version", "2.0.0")
                    .build();
            } else {
                return Health.down()
                    .withDetail("application", "ModResorts")
                    .withDetail("status", "degraded")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    /**
     * Performs basic application health checks
     * @return true if application is healthy, false otherwise
     */
    private boolean checkApplicationHealth() {
        // Basic health check - can be extended to check database, cache, etc.
        return true;
    }
}
