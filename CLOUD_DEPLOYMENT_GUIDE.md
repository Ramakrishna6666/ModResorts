# ModResorts Cloud Deployment Guide

## Overview
This application has been modernized for cloud-native deployment on AWS. It now uses:
- **Executable JAR** with embedded Tomcat (Spring Boot)
- **Amazon S3** for file storage
- **AWS Secrets Manager** for credential management
- **HikariCP** connection pooling for database connections
- **Java 8 Time API** for timezone-safe date handling
- **Environment-based configuration** following 12-factor app principles

## Cloud Readiness Fixes Applied

### 1. Packaging Migration (WAR → JAR)
- **Before**: WAR file requiring external application server
- **After**: Executable JAR with embedded Tomcat
- **Benefit**: Simplified containerization, smaller images, faster startup

### 2. File System Dependencies Eliminated
- **Before**: Hard-coded file paths, local file writes, temporary file storage
- **After**: Amazon S3 for persistent storage, classpath resources for configuration
- **Files Modified**: 
  - `AvailabilityCheckerServlet.java` - S3 export functionality
  - `IOUtils.java` - S3-based resource loading

### 3. Secret Management
- **Before**: Hardcoded API keys in source code
- **After**: AWS Secrets Manager integration
- **Files Modified**: `WeatherServlet.java`
- **Secret Name**: `modresorts/weather-api-key`

### 4. Date/Time Handling
- **Before**: `java.util.Date`, `SimpleDateFormat` (timezone issues)
- **After**: `java.time.LocalDate`, `DateTimeFormatter` (UTC standardized)
- **Files Modified**: 
  - `AvailabilityCheckerServlet.java`
  - `DateChecker.java`
  - `ReservationCheckerData.java`

### 5. EJB Migration
- **Before**: EJB 2.x with heavy container dependencies
- **After**: Spring Boot components with dependency injection
- **Files Modified**: `ModResortsCustomerInformation.java`

### 6. Resource Management
- **Before**: Manual resource cleanup (potential leaks)
- **After**: Try-with-resources pattern for automatic cleanup
- **Files Modified**: All servlet and database classes

## Environment Variables

### Required for AWS Deployment
```bash
# AWS Configuration
AWS_REGION=us-east-1
S3_BUCKET_NAME=modresorts-data
USE_S3_STORAGE=true

# Secrets Manager
WEATHER_API_SECRET_NAME=modresorts/weather-api-key

# Database (RDS)
DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/modresorts
DATABASE_USERNAME=modresorts_user
DATABASE_PASSWORD=<from-secrets-manager>
DATABASE_DRIVER=org.postgresql.Driver

# Application
PORT=8080
LOG_LEVEL=INFO
```

### Optional Configuration
```bash
# Connection Pool Tuning
DB_POOL_SIZE=20
DB_POOL_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=30000

# JPA Settings
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
```

## AWS Services Setup

### 1. Amazon S3 Bucket
```bash
aws s3 mb s3://modresorts-data --region us-east-1
aws s3api put-bucket-versioning \
  --bucket modresorts-data \
  --versioning-configuration Status=Enabled
```

### 2. AWS Secrets Manager
```bash
aws secretsmanager create-secret \
  --name modresorts/weather-api-key \
  --description "Weather API key for ModResorts" \
  --secret-string '{"WEATHER_API_KEY":"your-api-key-here"}' \
  --region us-east-1
```

### 3. Amazon RDS (PostgreSQL)
```bash
aws rds create-db-instance \
  --db-instance-identifier modresorts-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username modresorts_admin \
  --master-user-password <secure-password> \
  --allocated-storage 20 \
  --vpc-security-group-ids sg-xxxxx \
  --db-subnet-group-name default \
  --backup-retention-period 7 \
  --region us-east-1
```

## Building the Application

### Maven Build
```bash
mvn clean package
```

This produces: `target/modresorts-2.0.0.jar`

### Run Locally
```bash
java -jar target/modresorts-2.0.0.jar
```

## Container Deployment (Next Steps)

The application is now ready for containerization. The next workflow will:
1. Create Dockerfile for the executable JAR
2. Build container image
3. Deploy to Amazon ECS/EKS/Fargate
4. Configure load balancer and auto-scaling

## IAM Permissions Required

The application requires the following IAM permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::modresorts-data",
        "arn:aws:s3:::modresorts-data/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-1:*:secret:modresorts/*"
      ]
    }
  ]
}
```

## Health Check Endpoint

Spring Boot Actuator provides health checks:
- **URL**: `http://localhost:8080/actuator/health`
- **Response**: `{"status":"UP"}`

## Monitoring and Logging

- Logs are written to stdout (container-friendly)
- Structured logging format for CloudWatch integration
- Metrics available via `/actuator/metrics`

## Migration Checklist

- [x] Convert WAR to executable JAR
- [x] Replace file system operations with S3
- [x] Migrate secrets to AWS Secrets Manager
- [x] Replace java.util.Date with java.time API
- [x] Migrate EJB to Spring Boot
- [x] Implement try-with-resources for resource management
- [x] Externalize all configuration to environment variables
- [x] Add HikariCP connection pooling
- [ ] Create Dockerfile (next workflow)
- [ ] Deploy to AWS ECS/EKS (next workflow)

## Support

For issues or questions, contact the cloud migration team.
