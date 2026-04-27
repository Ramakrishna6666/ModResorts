# Cloud Migration Guide - ModResorts Application

## Overview
This application has been migrated from a traditional WebSphere/EJB architecture to a cloud-native Spring Boot application ready for deployment on AWS.

## Key Changes

### 1. Packaging (WAR → JAR)
- **Before**: WAR file requiring external application server (WebSphere)
- **After**: Executable JAR with embedded Tomcat
- **Benefit**: Simplified containerization, smaller images, faster startup

### 2. File System Operations → Amazon S3
- **Before**: Local file system operations with hardcoded paths
- **After**: Amazon S3 for durable, scalable storage
- **Files Changed**:
  - `IOUtils.java`: Now uses classpath resources
  - `AvailabilityCheckerServlet.java`: Exports to S3 instead of local filesystem
  - `S3StorageService.java`: New service for S3 operations

### 3. Secrets Management → AWS Secrets Manager
- **Before**: Hardcoded API keys in environment variables
- **After**: AWS Secrets Manager with fallback to environment variables
- **Files Changed**:
  - `WeatherServlet.java`: Uses SecretsService
  - `SecretsService.java`: New service for secret retrieval

### 4. Date/Time Handling → java.time API
- **Before**: java.util.Date with timezone issues
- **After**: java.time API standardized on UTC
- **Files Changed**:
  - `DateChecker.java`
  - `ReservationCheckerData.java`
  - `AvailabilityCheckerServlet.java`

### 5. Session Management → Amazon ElastiCache (Redis)
- **Before**: WebSphere cluster state replication
- **After**: Spring Session with Redis for distributed sessions
- **Files Changed**:
  - `LogoutServlet.java`: Standard session management
  - `pom.xml`: Added Spring Session Redis dependencies
  - `application.properties`: Redis configuration

### 6. Database Access → Spring Data JPA with HikariCP
- **Before**: EJB 2.x with manual connection management
- **After**: Spring Service with HikariCP connection pooling
- **Files Changed**:
  - `ModResortsCustomerInformation.java`: Now a Spring @Service
  - Uses try-with-resources to prevent connection leaks

### 7. WebSphere Dependencies Removed
- **Before**: Tight coupling to WebSphere APIs
- **After**: Standard Java and Spring APIs
- **Files Changed**:
  - `LogoutServlet.java`: Removed WSSecurityHelper
  - `UpperServlet.java`: Removed ResponseUtils
  - `WeatherServlet.java`: Removed WebSphere naming context

### 8. Resource Leak Prevention
- **Before**: Manual resource cleanup with potential leaks
- **After**: try-with-resources pattern throughout
- **Files Changed**: All servlets and data access classes

## Environment Variables

### Required
- `AWS_REGION`: AWS region (default: us-east-1)
- `S3_BUCKET_NAME`: S3 bucket for file storage
- `REDIS_HOST`: ElastiCache Redis endpoint
- `DATABASE_URL`: RDS database connection string
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password

### Optional
- `PORT`: Application port (default: 8080)
- `REDIS_PORT`: Redis port (default: 6379)
- `REDIS_PASSWORD`: Redis password
- `WEATHER_API_KEY`: Weather API key (fallback if Secrets Manager not configured)

## AWS Services Required

1. **Amazon S3**: File storage
   - Bucket: `modresorts-data` (configurable)
   
2. **AWS Secrets Manager**: Secret management
   - Secret: `modresorts/weather-api-key`
   
3. **Amazon ElastiCache (Redis)**: Session management
   - Cluster mode or standalone Redis instance
   
4. **Amazon RDS**: Database (PostgreSQL recommended)
   - HikariCP connection pooling configured

## Deployment Options

### AWS Elastic Beanstalk
```bash
eb init -p java-8 modresorts
eb create modresorts-env
```

### Amazon ECS/Fargate
Build Docker image and deploy to ECS using the executable JAR.

### Amazon EKS
Deploy as Kubernetes deployment with appropriate service and ingress configurations.

## Running Locally

```bash
# Set environment variables
export AWS_REGION=us-east-1
export S3_BUCKET_NAME=modresorts-data
export REDIS_HOST=localhost
export DATABASE_URL=jdbc:postgresql://localhost:5432/modresorts
export DATABASE_USERNAME=modresorts
export DATABASE_PASSWORD=yourpassword

# Run the application
mvn spring-boot:run
```

## Health Check
The application exposes health check endpoints:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

## Migration Checklist

- [x] Convert WAR to executable JAR
- [x] Replace file system operations with S3
- [x] Externalize secrets to AWS Secrets Manager
- [x] Replace java.util.Date with java.time API
- [x] Externalize session state to Redis
- [x] Replace EJB with Spring Services
- [x] Remove WebSphere dependencies
- [x] Implement try-with-resources for resource management
- [x] Add HikariCP connection pooling
- [x] Configure for 12-factor app principles

## Next Steps

1. Create S3 bucket and configure bucket policy
2. Store secrets in AWS Secrets Manager
3. Provision ElastiCache Redis cluster
4. Provision RDS database instance
5. Build Docker image (if containerizing)
6. Deploy to chosen AWS service
7. Configure CloudWatch for monitoring and logging
8. Set up AWS X-Ray for distributed tracing (optional)
