# ModResorts (newcontainertest) - AWS ECS Fargate Deployment Guide

## Table of Contents
1. [Application Overview](#application-overview)
2. [Prerequisites](#prerequisites)
3. [Local Development with Docker Compose](#local-development-with-docker-compose)
4. [Build and Push Docker Image](#build-and-push-docker-image)
5. [AWS ECS Fargate Prerequisites](#aws-ecs-fargate-prerequisites)
6. [ECS Task Definition Explained](#ecs-task-definition-explained)
7. [ECS Service Configuration](#ecs-service-configuration)
8. [ECS Fargate Deployment Walkthrough](#ecs-fargate-deployment-walkthrough)
9. [ECS-Specific Troubleshooting](#ecs-specific-troubleshooting)
10. [ECS Fargate Scaling and Management](#ecs-fargate-scaling-and-management)
11. [Configuration Management](#configuration-management)
12. [Security Considerations](#security-considerations)
13. [Java-Specific Notes](#java-specific-notes)

---

## Application Overview

**Application**: ModResorts (`newcontainertest`)  
**Type**: Java EE Web Application (WAR)  
**Framework**: Java EE 7 / Servlet 3.1  
**Build Tool**: Maven  
**Java Version**: 8  
**Runtime**: Apache Tomcat 9 on `eclipse-temurin:8-jre`  
**Application Port**: 8080  
**Context Root**: `/resorts`  
**Health Endpoint**: `GET /resorts/health` → `{"status":"UP","application":"modresorts"}`

---

## Prerequisites

### Local Development
- Docker Desktop 20.10+ or Docker Engine 20.10+
- Docker Compose v2.0+
- Java 8 JDK (for local builds outside Docker)
- Maven 3.6+ (for local builds outside Docker)

### AWS Deployment
- AWS CLI v2 configured with appropriate credentials (`aws configure`)
- IAM permissions for: ECS, ECR, IAM, CloudWatch Logs, ELBv2, VPC
- An existing AWS VPC with at least 2 subnets in different AZs
- Security group allowing inbound TCP on port 8080 (or 80 via ALB)

---

## Local Development with Docker Compose

### Start the application locally

```bash
# From the project root directory
docker compose up --build
```

The application will be available at:
- **Application**: http://localhost:8080/resorts/
- **Health Check**: http://localhost:8080/resorts/health

### Environment Variables (local)

Create a `.env` file in the project root to override defaults:

```env
WEATHER_API_KEY=your_weather_api_key_here
SERVER_DISPLAY_NAME=modresorts-local
SERVER_FULL_NAME=modresorts-local-container
```

### Stop the application

```bash
docker compose down
```

### View logs

```bash
docker compose logs -f newcontainertest
```

---

## Build and Push Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

The script will prompt you to:
1. Choose registry type (AWS ECR or Docker Hub)
2. Enter registry credentials and details
3. Enter an image tag (defaults to `latest`)

The script automatically:
- Sanitizes the image name (lowercase, hyphens)
- Creates the ECR repository if it doesn't exist (for ECR)
- Builds the Docker image from the project root
- Pushes the image to the selected registry

### Manual Build (ECR example)

```bash
# Authenticate to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  123456789012.dkr.ecr.us-east-1.amazonaws.com

# Build
docker build -t 123456789012.dkr.ecr.us-east-1.amazonaws.com/newcontainertest:latest .

# Push
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/newcontainertest:latest
```

---

## AWS ECS Fargate Prerequisites

### 1. IAM Roles

#### ECS Task Execution Role
This role allows ECS to pull images from ECR and write logs to CloudWatch.

```bash
# Create the execution role
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

# Attach the managed policy
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

#### ECS Task Role (optional, for application AWS API access)
```bash
aws iam create-role \
  --role-name ecsTaskRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'
```

### 2. VPC and Networking

Ensure you have:
- A VPC with DNS resolution enabled
- At least 2 public or private subnets in different Availability Zones
- A security group with inbound rule: TCP port 8080 from `0.0.0.0/0` (or your ALB security group)
- Outbound rule: All traffic (for ECR image pulls and CloudWatch logs)

### 3. CloudWatch Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/newcontainertest \
  --region us-east-1
```

### 4. ECR Repository

```bash
aws ecr create-repository \
  --repository-name newcontainertest \
  --region us-east-1
```

---

## ECS Task Definition Explained

The task definition (`ecs/task-definition.json`) configures:

| Field | Value | Notes |
|-------|-------|-------|
| `family` | `newcontainertest-task` | Task definition family name |
| `requiresCompatibilities` | `["FARGATE"]` | Fargate launch type |
| `networkMode` | `awsvpc` | Required for Fargate |
| `cpu` | `"512"` | 0.5 vCPU |
| `memory` | `"1024"` | 1 GB RAM |
| `executionRoleArn` | `ecsTaskExecutionRole` | For ECR pull + CloudWatch logs |
| `containerPort` | `8080` | Application port |

### Valid Fargate CPU/Memory Combinations

| CPU | Memory Options |
|-----|---------------|
| 256 (.25 vCPU) | 512, 1024, 2048 MB |
| **512 (.5 vCPU)** | **1024, 2048, 3072, 4096 MB** ← Used |
| 1024 (1 vCPU) | 2048–8192 MB |
| 2048 (2 vCPU) | 4096–16384 MB |
| 4096 (4 vCPU) | 8192–30720 MB |

### Environment Variables in Task Definition

| Variable | Purpose |
|----------|---------|
| `JAVA_OPTS` | JVM tuning flags for containerized Java |
| `TZ` | Timezone (UTC) |
| `WEATHER_API_KEY` | Weather Underground API key (optional) |
| `SERVER_DISPLAY_NAME` | Server display name for logging |
| `SERVER_FULL_NAME` | Full server name for logging |

---

## ECS Service Configuration

The service definition (`ecs/service-definition.json`) configures:

| Field | Value | Notes |
|-------|-------|-------|
| `serviceName` | `newcontainertest-service` | ECS service name |
| `launchType` | `FARGATE` | Serverless containers |
| `desiredCount` | `2` | 2 running tasks for HA |
| `networkMode` | `awsvpc` | Each task gets its own ENI |
| `assignPublicIp` | `ENABLED` | For public subnet deployment |
| `maximumPercent` | `200` | Rolling deploy: up to 4 tasks |
| `minimumHealthyPercent` | `50` | Rolling deploy: min 1 task |

---

## ECS Fargate Deployment Walkthrough

### Step 1: Build and push the image

```bash
./scripts/build-push.sh
# Note the full image URI output at the end
```

### Step 2: Run the deployment script

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

You will be prompted for:
- AWS Region
- ECS Cluster name
- ECR Image URI (from Step 1)
- VPC ID
- Subnet IDs (comma-separated, at least 2)
- Security Group ID
- Whether to create an Application Load Balancer

### Step 3: Verify the deployment

```bash
# Check service status
aws ecs describe-services \
  --cluster newcontainertest-cluster \
  --services newcontainertest-service \
  --region us-east-1

# List running tasks
aws ecs list-tasks \
  --cluster newcontainertest-cluster \
  --service-name newcontainertest-service \
  --region us-east-1

# View application logs
aws logs tail /ecs/newcontainertest --follow --region us-east-1
```

### Step 4: Access the application

- **Without ALB**: Use the task's public IP on port 8080: `http://<TASK_PUBLIC_IP>:8080/resorts/`
- **With ALB**: Use the load balancer DNS: `http://<ALB_DNS>/resorts/`
- **Health check**: `http://<HOST>/resorts/health`

---

## ECS-Specific Troubleshooting

### Task fails to start

```bash
# Check stopped task reason
aws ecs describe-tasks \
  --cluster newcontainertest-cluster \
  --tasks <TASK_ARN> \
  --region us-east-1 \
  --query "tasks[0].{Status:lastStatus,StopCode:stopCode,StopReason:stoppedReason}"
```

Common causes:
- **ImagePullBackOff**: ECR permissions missing on `ecsTaskExecutionRole`
- **ResourceInitializationError**: VPC/subnet/security group misconfiguration
- **OutOfMemory**: Increase `memory` in task definition (use valid Fargate combination)

### Container exits immediately

```bash
# View CloudWatch logs
aws logs get-log-events \
  --log-group-name /ecs/newcontainertest \
  --log-stream-name ecs/newcontainertest/<TASK_ID> \
  --region us-east-1
```

### Network connectivity issues

- Ensure security group allows **inbound TCP 8080** from ALB security group (or `0.0.0.0/0`)
- Ensure security group allows **outbound all traffic** (for ECR pulls, CloudWatch)
- For private subnets: ensure NAT Gateway or VPC endpoints for ECR/CloudWatch

### Health check failures

The application health endpoint is: `GET /resorts/health`  
Expected response: `{"status":"UP","application":"modresorts"}` with HTTP 200

If using ALB, configure the target group health check:
- Path: `/resorts/health`
- Protocol: HTTP
- Port: 8080
- Healthy threshold: 2
- Unhealthy threshold: 3
- Interval: 30 seconds

### Invalid CPU/Memory combination

Ensure you use valid Fargate combinations. The default is `cpu: "512"`, `memory: "1024"`.

---

## ECS Fargate Scaling and Management

### Manual scaling

```bash
aws ecs update-service \
  --cluster newcontainertest-cluster \
  --service newcontainertest-service \
  --desired-count 4 \
  --region us-east-1
```

### Auto Scaling

```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/newcontainertest-cluster/newcontainertest-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# Create CPU-based scaling policy
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/newcontainertest-cluster/newcontainertest-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name newcontainertest-cpu-scaling \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }'
```

### Blue/Green Deployment with CodeDeploy

For zero-downtime deployments, configure CodeDeploy with ECS:
1. Create a CodeDeploy application with `ECS` compute platform
2. Create a deployment group linked to your ECS service
3. Use `appspec.yaml` to define the deployment lifecycle

### Force new deployment (rolling update)

```bash
aws ecs update-service \
  --cluster newcontainertest-cluster \
  --service newcontainertest-service \
  --force-new-deployment \
  --region us-east-1
```

---

## Configuration Management

### Environment Variables

Sensitive values (API keys, passwords) should be stored in AWS Secrets Manager or SSM Parameter Store and referenced in the task definition:

```json
"secrets": [
  {
    "name": "WEATHER_API_KEY",
    "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:modresorts/weather-api-key"
  }
]
```

Grant the `ecsTaskExecutionRole` permission to read secrets:
```bash
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite
```

### Application Profiles

The application uses environment variables for configuration:
- `WEATHER_API_KEY`: Weather Underground API key (optional; uses default data if not set)
- `SERVER_DISPLAY_NAME`: Display name for the server
- `SERVER_FULL_NAME`: Full server name

---

## Security Considerations

1. **Non-root container**: The Dockerfile creates and uses a non-root `appuser` for running Tomcat
2. **Minimal base image**: Uses `eclipse-temurin:8-jre` (JRE only, not JDK)
3. **No sensitive data in image**: All secrets via environment variables or Secrets Manager
4. **Security groups**: Restrict inbound traffic to only required ports
5. **Private subnets**: Consider deploying tasks in private subnets with NAT Gateway
6. **ECR image scanning**: Enable ECR image scanning for vulnerability detection:
   ```bash
   aws ecr put-image-scanning-configuration \
     --repository-name newcontainertest \
     --image-scanning-configuration scanOnPush=true
   ```
7. **Task role least privilege**: Grant `ecsTaskRole` only the AWS permissions the application needs
8. **HTTPS**: Use ACM certificate on ALB listener for HTTPS termination

---

## Java-Specific Notes

### JVM Configuration

The `JAVA_OPTS` environment variable is configured with container-aware JVM flags:

```
-Xmx512m                          # Maximum heap size
-Xms256m                          # Initial heap size
-XX:+UseContainerSupport          # Enable container memory/CPU awareness (Java 8u191+)
-XX:MaxRAMPercentage=75.0         # Use 75% of container memory for heap
-XX:+UnlockExperimentalVMOptions  # Enable experimental JVM options
-Djava.security.egd=file:/dev/./urandom  # Faster random number generation
-Dfile.encoding=UTF-8             # Ensure UTF-8 encoding
-Duser.timezone=UTC               # Set timezone
```

### Tomcat Configuration

The application runs on Apache Tomcat 9 with:
- WAR deployed as `resorts.war` → context root `/resorts`
- Default connector on port 8080
- Default session timeout: 30 minutes (from web.xml)

### JVM Startup Time

Java EE applications on Tomcat typically take 15-30 seconds to start. The ECS service is configured with:
- `healthCheckGracePeriodSeconds: 300` (when ALB is used) to allow JVM warmup
- ALB health check interval: 30 seconds

### Monitoring

For JVM monitoring in production, consider:
- **JMX**: The application registers MBeans (AppInfo) - expose JMX port 9999 if needed
- **CloudWatch Container Insights**: Enable for ECS cluster-level metrics
- **AWS X-Ray**: Add X-Ray SDK for distributed tracing

```bash
# Enable Container Insights for the cluster
aws ecs update-cluster-settings \
  --cluster newcontainertest-cluster \
  --settings name=containerInsights,value=enabled \
  --region us-east-1
```
