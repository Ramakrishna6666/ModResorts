# ModResorts - AWS ECS Fargate Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [AWS ECS Fargate Prerequisites](#aws-ecs-fargate-prerequisites)
5. [Building and Pushing Docker Image](#building-and-pushing-docker-image)
6. [ECS Task Definition Explained](#ecs-task-definition-explained)
7. [ECS Service Configuration](#ecs-service-configuration)
8. [Deployment to AWS ECS Fargate](#deployment-to-aws-ecs-fargate)
9. [Monitoring and Logging](#monitoring-and-logging)
10. [Troubleshooting](#troubleshooting)
11. [Scaling and Management](#scaling-and-management)
12. [Security Considerations](#security-considerations)

---

## Overview

ModResorts is a Java 8 web application packaged as a WAR file, deployed on Apache Tomcat 9 within a Docker container. This guide covers containerization and deployment to AWS ECS Fargate.

**Technology Stack:**
- Java 8 (Eclipse Temurin JDK)
- Apache Tomcat 9.0.82
- Maven 3.9.4
- Spring Framework 5.3.20
- Docker multi-stage build
- AWS ECS Fargate

**Application Details:**
- **Port:** 8080
- **Health Endpoint:** `/health` and `/actuator/health`
- **Package Type:** WAR
- **Build Tool:** Maven

---

## Prerequisites

### Required Software
1. **Docker Desktop** (v20.10+)
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`

2. **AWS CLI** (v2.x)
   - Download: https://aws.amazon.com/cli/
   - Verify: `aws --version`
   - Configure: `aws configure`

3. **Git** (for version control)
   - Download: https://git-scm.com/
   - Verify: `git --version`

4. **Maven** (v3.6+) - Optional for local builds
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn --version`

### AWS Account Requirements
- Active AWS account with appropriate permissions
- IAM user with programmatic access
- AWS CLI configured with credentials

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd testcontaunercheck
```

### 2. Build Locally with Maven (Optional)
```bash
mvn clean package -DskipTests
```

The WAR file will be generated in `target/modresorts-2.0.0.war`

### 3. Run with Docker Compose
```bash
# Build and start the application
docker-compose up --build

# Access the application
# Health check: http://localhost:8080/health
# Welcome page: http://localhost:8080/welcome
```

### 4. Stop the Application
```bash
docker-compose down
```

---

## AWS ECS Fargate Prerequisites

### 1. AWS IAM Roles

#### ECS Task Execution Role
This role allows ECS to pull images from ECR and write logs to CloudWatch.

**Create the role:**
```bash
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

#### ECS Task Role (Optional)
This role grants permissions to the application running in the container.

**Create the role:**
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

# Attach policies as needed (e.g., S3, DynamoDB access)
```

### 2. VPC and Networking Setup

#### Create VPC (if not exists)
```bash
# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --query 'Vpc.VpcId' \
  --output text)

# Enable DNS hostnames
aws ec2 modify-vpc-attribute \
  --vpc-id $VPC_ID \
  --enable-dns-hostnames
```

#### Create Subnets
```bash
# Create public subnet 1
SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --query 'Subnet.SubnetId' \
  --output text)

# Create public subnet 2
SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --query 'Subnet.SubnetId' \
  --output text)

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --query 'InternetGateway.InternetGatewayId' \
  --output text)

# Attach to VPC
aws ec2 attach-internet-gateway \
  --vpc-id $VPC_ID \
  --internet-gateway-id $IGW_ID

# Create route table
ROUTE_TABLE_ID=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --query 'RouteTable.RouteTableId' \
  --output text)

# Add route to Internet Gateway
aws ec2 create-route \
  --route-table-id $ROUTE_TABLE_ID \
  --destination-cidr-block 0.0.0.0/0 \
  --gateway-id $IGW_ID

# Associate subnets with route table
aws ec2 associate-route-table \
  --subnet-id $SUBNET_1 \
  --route-table-id $ROUTE_TABLE_ID

aws ec2 associate-route-table \
  --subnet-id $SUBNET_2 \
  --route-table-id $ROUTE_TABLE_ID
```

#### Create Security Group
```bash
# Create security group
SG_ID=$(aws ec2 create-security-group \
  --group-name modresorts-sg \
  --description "Security group for ModResorts ECS tasks" \
  --vpc-id $VPC_ID \
  --query 'GroupId' \
  --output text)

# Allow inbound HTTP traffic on port 8080
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0

# Allow inbound HTTP traffic on port 80 (for ALB)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0
```

### 3. CloudWatch Log Group
```bash
aws logs create-log-group --log-group-name /ecs/modresorts
```

### 4. ECS Cluster
```bash
aws ecs create-cluster --cluster-name modresorts-cluster
```

---

## Building and Pushing Docker Image

### Option 1: Using build-push.sh (Linux/macOS)

```bash
cd scripts
chmod +x build-push.sh
./build-push.sh
```

**Script will prompt for:**
1. Image tag (default: latest)
2. Registry choice (AWS ECR or Docker Hub)
3. Registry-specific credentials

**For AWS ECR:**
- AWS Region
- AWS Account ID
- ECR Repository Name

**For Docker Hub:**
- Docker Hub Username
- Docker Hub Password/Token

### Option 2: Using build-push.bat (Windows)

```cmd
cd scripts
build-push.bat
```

Follow the same prompts as the Linux/macOS version.

### Manual Build and Push

#### AWS ECR
```bash
# Set variables
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPO=modresorts
IMAGE_TAG=latest

# Authenticate with ECR
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Create ECR repository (if not exists)
aws ecr create-repository --repository-name $ECR_REPO --region $AWS_REGION

# Build image
docker build -t modresorts:$IMAGE_TAG .

# Tag image
docker tag modresorts:$IMAGE_TAG \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG

# Push image
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO:$IMAGE_TAG
```

#### Docker Hub
```bash
# Login to Docker Hub
docker login

# Build image
docker build -t your-username/modresorts:latest .

# Push image
docker push your-username/modresorts:latest
```

---

## ECS Task Definition Explained

The task definition (`ecs/task-definition.json`) defines how your container runs on ECS Fargate.

### Key Components

#### 1. Launch Type Configuration
```json
{
  "requiresCompatibilities": ["FARGATE"],
  "networkMode": "awsvpc"
}
```
- **FARGATE**: Serverless compute engine
- **awsvpc**: Each task gets its own ENI with private IP

#### 2. CPU and Memory
```json
{
  "cpu": "512",
  "memory": "1024"
}
```

**Valid Fargate CPU/Memory Combinations:**
- CPU: 256 (.25 vCPU) → Memory: 512, 1024, 2048 MB
- CPU: 512 (.5 vCPU) → Memory: 1024, 2048, 3072, 4096 MB
- CPU: 1024 (1 vCPU) → Memory: 2048-8192 MB (1024 increments)
- CPU: 2048 (2 vCPU) → Memory: 4096-16384 MB (1024 increments)
- CPU: 4096 (4 vCPU) → Memory: 8192-30720 MB (1024 increments)

#### 3. IAM Roles
```json
{
  "executionRoleArn": "arn:aws:iam::{{ACCOUNT_ID}}:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::{{ACCOUNT_ID}}:role/ecsTaskRole"
}
```
- **executionRoleArn**: Required for pulling images and logging
- **taskRoleArn**: Optional, for application permissions

#### 4. Container Definition
```json
{
  "name": "modresorts",
  "image": "{{IMAGE_URI}}",
  "essential": true,
  "portMappings": [
    {
      "containerPort": 8080,
      "protocol": "tcp"
    }
  ]
}
```

#### 5. Environment Variables
```json
{
  "environment": [
    {
      "name": "JAVA_OPTS",
      "value": "-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
    }
  ]
}
```

#### 6. Logging Configuration
```json
{
  "logConfiguration": {
    "logDriver": "awslogs",
    "options": {
      "awslogs-group": "/ecs/modresorts",
      "awslogs-region": "{{AWS_REGION}}",
      "awslogs-stream-prefix": "ecs"
    }
  }
}
```

---

## ECS Service Configuration

The service definition (`ecs/service-definition.json`) manages task deployment and scaling.

### Key Components

#### 1. Service Configuration
```json
{
  "serviceName": "modresorts-service",
  "desiredCount": 2,
  "launchType": "FARGATE"
}
```
- **desiredCount**: Number of tasks to run (2 for high availability)

#### 2. Network Configuration
```json
{
  "networkConfiguration": {
    "awsvpcConfiguration": {
      "subnets": ["{{SUBNET_1}}", "{{SUBNET_2}}"],
      "securityGroups": ["{{SECURITY_GROUP}}"],
      "assignPublicIp": "ENABLED"
    }
  }
}
```
- **subnets**: At least 2 subnets in different AZs
- **assignPublicIp**: ENABLED for public internet access

#### 3. Deployment Configuration
```json
{
  "deploymentConfiguration": {
    "maximumPercent": 200,
    "minimumHealthyPercent": 50,
    "deploymentCircuitBreaker": {
      "enable": true,
      "rollback": true
    }
  }
}
```
- **maximumPercent**: Max tasks during deployment (200% = 4 tasks)
- **minimumHealthyPercent**: Min healthy tasks (50% = 1 task)
- **deploymentCircuitBreaker**: Auto-rollback on failure

#### 4. Load Balancer (Optional)
```json
{
  "loadBalancers": [
    {
      "targetGroupArn": "{{TARGET_GROUP_ARN}}",
      "containerName": "modresorts",
      "containerPort": 8080
    }
  ],
  "healthCheckGracePeriodSeconds": 300
}
```

---

## Deployment to AWS ECS Fargate

### Automated Deployment

#### Linux/macOS
```bash
cd scripts
chmod +x deploy-image.sh
./deploy-image.sh
```

#### Windows
```cmd
cd scripts
deploy-image.bat
```

### Script Prompts

The deployment script will ask for:

1. **AWS Region** (e.g., us-east-1)
2. **ECS Cluster Name** (e.g., modresorts-cluster)
3. **VPC ID** (from prerequisites)
4. **Subnet IDs** (comma-separated, at least 2)
5. **Security Group ID** (from prerequisites)
6. **Docker Image URI** (from build-push step)
7. **Load Balancer** (y/n)

### What the Script Does

1. ✅ Retrieves AWS Account ID
2. ✅ Checks/creates ECS cluster
3. ✅ Creates CloudWatch log group
4. ✅ Optionally creates Application Load Balancer and Target Group
5. ✅ Registers task definition with placeholders replaced
6. ✅ Creates or updates ECS service
7. ✅ Waits for service to stabilize
8. ✅ Displays deployment summary

### Manual Deployment

#### 1. Register Task Definition
```bash
# Replace placeholders in task-definition.json
sed -i 's|{{IMAGE_URI}}|123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest|g' ecs/task-definition.json
sed -i 's|{{AWS_REGION}}|us-east-1|g' ecs/task-definition.json
sed -i 's|{{ACCOUNT_ID}}|123456789|g' ecs/task-definition.json

# Register
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.json \
  --region us-east-1
```

#### 2. Create Service
```bash
# Replace placeholders in service-definition.json
sed -i 's|{{CLUSTER_NAME}}|modresorts-cluster|g' ecs/service-definition.json
sed -i 's|{{SUBNET_1}}|subnet-xxx|g' ecs/service-definition.json
sed -i 's|{{SUBNET_2}}|subnet-yyy|g' ecs/service-definition.json
sed -i 's|{{SECURITY_GROUP}}|sg-zzz|g' ecs/service-definition.json

# Create
aws ecs create-service \
  --cli-input-json file://ecs/service-definition.json \
  --region us-east-1
```

#### 3. Wait for Stability
```bash
aws ecs wait services-stable \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1
```

---

## Monitoring and Logging

### CloudWatch Logs

#### View Logs in Console
1. Navigate to CloudWatch → Log groups
2. Select `/ecs/modresorts`
3. View log streams for each task

#### View Logs with AWS CLI
```bash
# Tail logs in real-time
aws logs tail /ecs/modresorts --follow --region us-east-1

# View specific time range
aws logs tail /ecs/modresorts \
  --since 1h \
  --region us-east-1
```

### ECS Service Metrics

#### View in Console
1. Navigate to ECS → Clusters → modresorts-cluster
2. Select modresorts-service
3. View Metrics tab

#### Key Metrics
- **CPUUtilization**: CPU usage percentage
- **MemoryUtilization**: Memory usage percentage
- **TargetResponseTime**: Response time from ALB
- **HealthyHostCount**: Number of healthy tasks

### Application Health Check

```bash
# Direct task access (if public IP assigned)
curl http://<task-public-ip>:8080/health

# Through load balancer
curl http://<alb-dns-name>/health
```

Expected response:
```json
{"status":"UP","application":"ModResorts"}
```

---

## Troubleshooting

### Common Issues

#### 1. Task Fails to Start

**Symptoms:**
- Tasks transition from PENDING to STOPPED
- No logs in CloudWatch

**Possible Causes:**
- Invalid CPU/memory combination
- Image pull failure (ECR permissions)
- Missing execution role

**Solutions:**
```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1 \
  --query 'tasks[0].stoppedReason'

# Verify execution role
aws iam get-role --role-name ecsTaskExecutionRole

# Check ECR permissions
aws ecr get-repository-policy --repository-name modresorts
```

#### 2. Container Health Check Failures

**Symptoms:**
- Tasks repeatedly restart
- ALB shows unhealthy targets

**Solutions:**
```bash
# Check container logs
aws logs tail /ecs/modresorts --follow

# Verify health endpoint
curl http://<task-ip>:8080/health

# Increase health check grace period
# Edit service-definition.json:
"healthCheckGracePeriodSeconds": 300
```

#### 3. Network Connectivity Issues

**Symptoms:**
- Cannot access application
- Tasks cannot pull images

**Solutions:**
```bash
# Verify security group rules
aws ec2 describe-security-groups --group-ids <sg-id>

# Check subnet route table
aws ec2 describe-route-tables --filters "Name=association.subnet-id,Values=<subnet-id>"

# Verify Internet Gateway attached
aws ec2 describe-internet-gateways --filters "Name=attachment.vpc-id,Values=<vpc-id>"
```

#### 4. Out of Memory Errors

**Symptoms:**
- Tasks stop with exit code 137
- Logs show OutOfMemoryError

**Solutions:**
```bash
# Increase task memory in task-definition.json
"memory": "2048"

# Adjust JVM heap size
"JAVA_OPTS": "-Xmx1536m -Xms768m"

# Re-register task definition and update service
```

#### 5. Deployment Stuck

**Symptoms:**
- Service update never completes
- Old tasks not draining

**Solutions:**
```bash
# Force new deployment
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --force-new-deployment \
  --region us-east-1

# Check service events
aws ecs describe-services \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1 \
  --query 'services[0].events[0:10]'
```

### Debugging Commands

```bash
# List running tasks
aws ecs list-tasks \
  --cluster modresorts-cluster \
  --service-name modresorts-service \
  --region us-east-1

# Describe task details
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1

# Get task public IP
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1 \
  --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' \
  --output text | xargs -I {} aws ec2 describe-network-interfaces \
  --network-interface-ids {} \
  --query 'NetworkInterfaces[0].Association.PublicIp' \
  --output text

# View service events
aws ecs describe-services \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1 \
  --query 'services[0].events[0:20]'
```

---

## Scaling and Management

### Manual Scaling

#### Update Desired Count
```bash
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --desired-count 4 \
  --region us-east-1
```

### Auto Scaling

#### Create Auto Scaling Target
```bash
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/modresorts-cluster/modresorts-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10 \
  --region us-east-1
```

#### Create Scaling Policy (CPU-based)
```bash
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/modresorts-cluster/modresorts-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }' \
  --region us-east-1
```

### Blue/Green Deployment

#### Using AWS CodeDeploy
1. Create CodeDeploy application
2. Configure deployment group with ECS service
3. Define appspec.yml for traffic shifting
4. Trigger deployment with new task definition

### Rolling Updates

```bash
# Update service with new task definition
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --task-definition modresorts-task:2 \
  --force-new-deployment \
  --region us-east-1
```

### Rollback

```bash
# Rollback to previous task definition
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --task-definition modresorts-task:1 \
  --force-new-deployment \
  --region us-east-1
```

---

## Security Considerations

### 1. Container Security

#### Use Non-Root User
The Dockerfile already creates and uses a non-root user:
```dockerfile
USER tomcat
```

#### Scan Images for Vulnerabilities
```bash
# Using AWS ECR image scanning
aws ecr start-image-scan \
  --repository-name modresorts \
  --image-id imageTag=latest \
  --region us-east-1

# View scan results
aws ecr describe-image-scan-findings \
  --repository-name modresorts \
  --image-id imageTag=latest \
  --region us-east-1
```

### 2. Network Security

#### Security Group Best Practices
- Restrict inbound traffic to necessary ports only
- Use security group rules instead of CIDR blocks when possible
- Implement least privilege access

```bash
# Allow traffic only from ALB security group
aws ec2 authorize-security-group-ingress \
  --group-id <task-sg-id> \
  --protocol tcp \
  --port 8080 \
  --source-group <alb-sg-id>
```

#### Use Private Subnets
For production, use private subnets with NAT Gateway:
```json
{
  "assignPublicIp": "DISABLED"
}
```

### 3. Secrets Management

#### Use AWS Secrets Manager
```bash
# Create secret
aws secretsmanager create-secret \
  --name modresorts/db-password \
  --secret-string "your-secure-password" \
  --region us-east-1

# Reference in task definition
{
  "secrets": [
    {
      "name": "DB_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:modresorts/db-password"
    }
  ]
}
```

#### Grant Task Execution Role Access
```bash
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite
```

### 4. IAM Best Practices

- Use separate execution and task roles
- Grant minimum required permissions
- Regularly audit IAM policies
- Enable CloudTrail for API logging

### 5. Logging and Monitoring

- Enable CloudWatch Container Insights
- Set up CloudWatch alarms for critical metrics
- Use AWS X-Ray for distributed tracing
- Implement centralized log aggregation

---

## Java-Specific Optimizations

### JVM Tuning for Containers

#### Memory Settings
```bash
# In task definition environment variables
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

**Explanation:**
- `-Xmx512m`: Maximum heap size
- `-Xms256m`: Initial heap size
- `-XX:+UseContainerSupport`: Enable container awareness
- `-XX:MaxRAMPercentage=75.0`: Use 75% of container memory

#### Garbage Collection
```bash
# For low-latency applications
JAVA_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200

# For throughput-focused applications
JAVA_OPTS=-XX:+UseParallelGC
```

### Tomcat Optimization

#### Connector Configuration
Edit `server.xml` in Tomcat:
```xml
<Connector port="8080" protocol="HTTP/1.1"
           maxThreads="200"
           minSpareThreads="25"
           connectionTimeout="20000"
           redirectPort="8443" />
```

#### JVM Options for Tomcat
```bash
CATALINA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
```

### Application Performance

#### Enable JMX Monitoring
```bash
JAVA_OPTS=-Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9010 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false
```

#### Startup Time Optimization
- Use `-XX:TieredStopAtLevel=1` for faster startup (dev only)
- Consider GraalVM native image for production
- Implement lazy initialization where possible

---

## Additional Resources

### AWS Documentation
- [ECS Fargate Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html)
- [ECS Task Definitions](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html)
- [ECS Service Definition](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/service_definition_parameters.html)

### Docker Best Practices
- [Docker Multi-Stage Builds](https://docs.docker.com/develop/develop-images/multistage-build/)
- [Docker Security Best Practices](https://docs.docker.com/develop/security-best-practices/)

### Java in Containers
- [Java SE Support for Docker CPU and Memory Limits](https://blogs.oracle.com/java/post/java-se-support-for-docker-cpu-and-memory-limits)
- [Best Practices: Java Memory Arguments for Containers](https://developers.redhat.com/blog/2017/03/14/java-inside-docker)

---

## Support and Maintenance

### Regular Maintenance Tasks

1. **Update Base Images**
   - Rebuild with latest Eclipse Temurin images monthly
   - Scan for vulnerabilities

2. **Review Logs**
   - Check CloudWatch logs for errors
   - Set up log retention policies

3. **Monitor Costs**
   - Review ECS Fargate usage
   - Optimize task sizing

4. **Security Updates**
   - Update dependencies in pom.xml
   - Patch Tomcat and Java versions

### Backup and Disaster Recovery

1. **Task Definition Versions**
   - ECS maintains task definition history
   - Tag important versions

2. **Configuration Backup**
   - Store task/service definitions in version control
   - Document infrastructure as code

3. **Database Backups**
   - Implement automated RDS snapshots
   - Test restore procedures

---

## Conclusion

This guide provides comprehensive instructions for containerizing and deploying the ModResorts application to AWS ECS Fargate. Follow the steps carefully, and refer to the troubleshooting section for common issues.

For questions or issues, consult the AWS documentation or contact your DevOps team.

**Happy Deploying! 🚀**
