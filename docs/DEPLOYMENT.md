# ModResorts Application - AWS ECS Fargate Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development Setup](#local-development-setup)
4. [Docker Build and Push](#docker-build-and-push)
5. [AWS ECS Fargate Deployment](#aws-ecs-fargate-deployment)
6. [Configuration Management](#configuration-management)
7. [Monitoring and Logging](#monitoring-and-logging)
8. [Troubleshooting](#troubleshooting)
9. [Security Considerations](#security-considerations)
10. [Scaling and Management](#scaling-and-management)

---

## Overview

ModResorts is a Java EE 7 web application packaged as a WAR file, running on Apache Tomcat 9 in a containerized environment. This guide covers deployment to AWS ECS Fargate, a serverless container orchestration platform.

### Application Details
- **Technology Stack**: Java 8, Java EE 7, Maven
- **Application Server**: Apache Tomcat 9.0.82
- **Package Type**: WAR (Web Application Archive)
- **Default Port**: 8080
- **Health Check Endpoint**: `/health` and `/actuator/health`

### Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Application Load Balancer                │
│                         (Port 80)                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      ECS Service                             │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │   ECS Task 1     │         │   ECS Task 2     │         │
│  │  ┌────────────┐  │         │  ┌────────────┐  │         │
│  │  │ ModResorts │  │         │  │ ModResorts │  │         │
│  │  │ Container  │  │         │  │ Container  │  │         │
│  │  │ (Port 8080)│  │         │  │ (Port 8080)│  │         │
│  │  └────────────┘  │         │  └────────────┘  │         │
│  └──────────────────┘         └──────────────────┘         │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │  CloudWatch Logs     │
              │  /ecs/modresorts     │
              └──────────────────────┘
```

---

## Prerequisites

### Required Tools
1. **Docker Desktop** (v20.10 or later)
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`

2. **AWS CLI** (v2.x)
   - Download: https://aws.amazon.com/cli/
   - Verify: `aws --version`
   - Configure: `aws configure`

3. **Git** (for version control)
   - Download: https://git-scm.com/
   - Verify: `git --version`

4. **Maven** (v3.6 or later) - for local builds
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn --version`

### AWS Account Requirements
1. **AWS Account** with appropriate permissions
2. **IAM User** with the following policies:
   - `AmazonECS_FullAccess`
   - `AmazonEC2ContainerRegistryFullAccess`
   - `CloudWatchLogsFullAccess`
   - `IAMReadOnlyAccess`
   - `ElasticLoadBalancingFullAccess`

3. **AWS Resources**:
   - VPC with at least 2 subnets in different availability zones
   - Security Group allowing inbound traffic on port 8080
   - IAM Role: `ecsTaskExecutionRole` (for ECS to pull images and write logs)
   - IAM Role: `ecsTaskRole` (optional, for task-level permissions)

### Network Requirements
- **VPC**: A VPC with internet gateway for public access
- **Subnets**: At least 2 public subnets in different AZs
- **Security Group Rules**:
  - Inbound: Port 8080 (from ALB security group)
  - Inbound: Port 80 (from 0.0.0.0/0 for ALB)
  - Outbound: All traffic (for pulling images and external calls)

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Newrepocheck
```

### 2. Build the Application Locally
```bash
# Using Maven
mvn clean package -DskipTests

# The WAR file will be generated at:
# target/modresorts-2.0.0.war
```

### 3. Run with Docker Compose
```bash
# Build and start the application
docker-compose up --build

# Access the application
# http://localhost:8080

# Health check
# http://localhost:8080/health

# Stop the application
docker-compose down
```

### 4. Test the Application
```bash
# Health check
curl http://localhost:8080/health

# Expected response:
# {"status":"UP","application":"ModResorts"}

# Test welcome endpoint
curl http://localhost:8080/welcome

# Test weather endpoint
curl http://localhost:8080/weather
```

---

## Docker Build and Push

### Option 1: Using Build Script (Recommended)

#### Linux/macOS
```bash
cd scripts
chmod +x build-push.sh
./build-push.sh
```

#### Windows
```cmd
cd scripts
build-push.bat
```

The script will:
1. Prompt for registry selection (AWS ECR or Docker Hub)
2. Prompt for registry credentials and details
3. Build the Docker image with proper tagging
4. Authenticate with the selected registry
5. Push the image to the registry

### Option 2: Manual Build and Push

#### AWS ECR
```bash
# Set variables
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=123456789012
ECR_REPO=modresorts
IMAGE_TAG=latest

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Create ECR repository (if not exists)
aws ecr create-repository \
  --repository-name $ECR_REPO \
  --region $AWS_REGION

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

## AWS ECS Fargate Deployment

### Prerequisites Setup

#### 1. Create IAM Roles

**ECS Task Execution Role** (required):
```bash
# Create trust policy file
cat > ecs-task-execution-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# Create role
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document file://ecs-task-execution-trust-policy.json

# Attach policy
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

**ECS Task Role** (optional, for application permissions):
```bash
# Create task role
aws iam create-role \
  --role-name ecsTaskRole \
  --assume-role-policy-document file://ecs-task-execution-trust-policy.json

# Attach policies as needed (e.g., S3, DynamoDB access)
```

#### 2. Create VPC and Networking (if not exists)

```bash
# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --query 'Vpc.VpcId' \
  --output text)

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --query 'InternetGateway.InternetGatewayId' \
  --output text)

aws ec2 attach-internet-gateway \
  --vpc-id $VPC_ID \
  --internet-gateway-id $IGW_ID

# Create Subnets (2 in different AZs)
SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --query 'Subnet.SubnetId' \
  --output text)

SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --query 'Subnet.SubnetId' \
  --output text)

# Create Route Table
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

# Create Security Group
SG_ID=$(aws ec2 create-security-group \
  --group-name modresorts-sg \
  --description "Security group for ModResorts ECS tasks" \
  --vpc-id $VPC_ID \
  --query 'GroupId' \
  --output text)

# Add inbound rules
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0
```

#### 3. Create CloudWatch Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/modresorts \
  --region us-east-1
```

### Deployment Using Script (Recommended)

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

The script will:
1. Prompt for AWS region and ECS cluster name
2. Check/create ECS cluster
3. Prompt for network configuration (VPC, subnets, security group)
4. Prompt for Docker image URI
5. Ask if you need a load balancer
6. Create ALB and Target Group (if requested)
7. Create/update CloudWatch log group
8. Register ECS task definition
9. Create or update ECS service
10. Wait for service to stabilize
11. Display deployment status and access URLs

### Manual Deployment Steps

#### 1. Create ECS Cluster
```bash
aws ecs create-cluster \
  --cluster-name modresorts-cluster \
  --region us-east-1
```

#### 2. Register Task Definition
```bash
# Update ecs/task-definition.json with your values
# Then register:
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.json \
  --region us-east-1
```

#### 3. Create ECS Service
```bash
# Update ecs/service-definition.json with your values
# Then create:
aws ecs create-service \
  --cli-input-json file://ecs/service-definition.json \
  --region us-east-1
```

#### 4. Wait for Service Stability
```bash
aws ecs wait services-stable \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1
```

---

## Configuration Management

### Environment Variables

The application supports the following environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` | No |
| `TZ` | Timezone | `UTC` | No |
| `APP_ENV` | Application environment | `production` | No |

### Updating Configuration

#### Update Task Definition
```bash
# Modify ecs/task-definition.json
# Register new revision
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.json \
  --region us-east-1

# Update service to use new task definition
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --task-definition modresorts-task \
  --region us-east-1
```

#### Update Service Configuration
```bash
# Update desired count
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --desired-count 3 \
  --region us-east-1
```

---

## Monitoring and Logging

### CloudWatch Logs

View logs in real-time:
```bash
# Tail logs
aws logs tail /ecs/modresorts --follow --region us-east-1

# Filter logs
aws logs filter-log-events \
  --log-group-name /ecs/modresorts \
  --filter-pattern "ERROR" \
  --region us-east-1
```

### CloudWatch Metrics

Key metrics to monitor:
- **CPUUtilization**: CPU usage percentage
- **MemoryUtilization**: Memory usage percentage
- **TargetResponseTime**: Response time from targets
- **HealthyHostCount**: Number of healthy targets
- **UnHealthyHostCount**: Number of unhealthy targets

Create CloudWatch Dashboard:
```bash
aws cloudwatch put-dashboard \
  --dashboard-name ModResorts \
  --dashboard-body file://cloudwatch-dashboard.json \
  --region us-east-1
```

### Application Health Checks

The application exposes health check endpoints:
- **Primary**: `http://<alb-dns>/health`
- **Alternative**: `http://<alb-dns>/actuator/health`

Response format:
```json
{
  "status": "UP",
  "application": "ModResorts"
}
```

---

## Troubleshooting

### Common Issues

#### 1. Task Fails to Start

**Symptoms**: Tasks start and immediately stop

**Possible Causes**:
- Invalid CPU/memory combination
- Image pull errors
- Application startup failures

**Solutions**:
```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1 \
  --query 'tasks[0].stoppedReason'

# Check CloudWatch logs
aws logs tail /ecs/modresorts --follow --region us-east-1

# Verify task definition
aws ecs describe-task-definition \
  --task-definition modresorts-task \
  --region us-east-1
```

#### 2. Service Not Reaching Steady State

**Symptoms**: Service stuck in deployment

**Possible Causes**:
- Health check failures
- Network connectivity issues
- Insufficient resources

**Solutions**:
```bash
# Check service events
aws ecs describe-services \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1 \
  --query 'services[0].events[0:10]'

# Check target health
aws elbv2 describe-target-health \
  --target-group-arn <target-group-arn> \
  --region us-east-1
```

#### 3. Cannot Access Application

**Symptoms**: ALB returns 503 or connection timeout

**Possible Causes**:
- Security group misconfiguration
- Target group health check failures
- No healthy targets

**Solutions**:
```bash
# Verify security group rules
aws ec2 describe-security-groups \
  --group-ids <security-group-id> \
  --region us-east-1

# Check target health
aws elbv2 describe-target-health \
  --target-group-arn <target-group-arn> \
  --region us-east-1

# Test health endpoint directly on task
# Get task private IP
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1 \
  --query 'tasks[0].attachments[0].details[?name==`privateIPv4Address`].value'

# SSH to bastion host and test
curl http://<task-private-ip>:8080/health
```

#### 4. High Memory Usage

**Symptoms**: Tasks being killed due to OOM

**Solutions**:
```bash
# Increase task memory
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.json \
  --region us-east-1
  # (Update memory to 2048)

# Adjust JVM heap size
# Update JAVA_OPTS in task definition:
# -Xmx1536m -Xms768m
```

### Debugging Commands

```bash
# List running tasks
aws ecs list-tasks \
  --cluster modresorts-cluster \
  --service-name modresorts-service \
  --region us-east-1

# Describe specific task
aws ecs describe-tasks \
  --cluster modresorts-cluster \
  --tasks <task-id> \
  --region us-east-1

# View service details
aws ecs describe-services \
  --cluster modresorts-cluster \
  --services modresorts-service \
  --region us-east-1

# Check ALB target health
aws elbv2 describe-target-health \
  --target-group-arn <target-group-arn> \
  --region us-east-1

# View recent log events
aws logs tail /ecs/modresorts \
  --since 1h \
  --region us-east-1
```

---

## Security Considerations

### 1. Network Security

- **Use Private Subnets**: Deploy tasks in private subnets with NAT Gateway
- **Security Groups**: Implement least privilege access
- **ALB Security**: Only expose port 80/443 on ALB

```bash
# Example: Restrict task security group to ALB only
aws ec2 authorize-security-group-ingress \
  --group-id <task-sg-id> \
  --protocol tcp \
  --port 8080 \
  --source-group <alb-sg-id>
```

### 2. IAM Roles

- **Task Execution Role**: Minimal permissions for ECS operations
- **Task Role**: Application-specific permissions only
- **Avoid**: Hardcoding credentials in environment variables

### 3. Secrets Management

Use AWS Secrets Manager or Parameter Store:

```json
{
  "secrets": [
    {
      "name": "DB_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:region:account:secret:db-password"
    }
  ]
}
```

### 4. Image Security

- **Scan Images**: Use ECR image scanning
- **Use Specific Tags**: Avoid `latest` tag in production
- **Minimal Base Images**: Use slim/alpine variants

```bash
# Enable ECR scanning
aws ecr put-image-scanning-configuration \
  --repository-name modresorts \
  --image-scanning-configuration scanOnPush=true \
  --region us-east-1
```

### 5. HTTPS/TLS

Configure ALB with SSL certificate:

```bash
# Request certificate
aws acm request-certificate \
  --domain-name modresorts.example.com \
  --validation-method DNS \
  --region us-east-1

# Add HTTPS listener to ALB
aws elbv2 create-listener \
  --load-balancer-arn <alb-arn> \
  --protocol HTTPS \
  --port 443 \
  --certificates CertificateArn=<certificate-arn> \
  --default-actions Type=forward,TargetGroupArn=<target-group-arn>
```

---

## Scaling and Management

### Auto Scaling

#### 1. Configure Service Auto Scaling

```bash
# Register scalable target
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/modresorts-cluster/modresorts-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10 \
  --region us-east-1

# Create scaling policy (CPU-based)
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/modresorts-cluster/modresorts-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json \
  --region us-east-1
```

**scaling-policy.json**:
```json
{
  "TargetValue": 70.0,
  "PredefinedMetricSpecification": {
    "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
  },
  "ScaleInCooldown": 300,
  "ScaleOutCooldown": 60
}
```

#### 2. Manual Scaling

```bash
# Scale up
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --desired-count 5 \
  --region us-east-1

# Scale down
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --desired-count 2 \
  --region us-east-1
```

### Blue/Green Deployments

```bash
# Create new task definition revision
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition-v2.json \
  --region us-east-1

# Update service with deployment configuration
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --task-definition modresorts-task:2 \
  --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100" \
  --region us-east-1
```

### Rolling Updates

ECS automatically performs rolling updates when you update the service:

```bash
# Update to new image
aws ecs update-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
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
  --region us-east-1
```

---

## Performance Tuning

### JVM Tuning

Optimize JVM settings in task definition:

```json
{
  "environment": [
    {
      "name": "JAVA_OPTS",
      "value": "-Xmx1536m -Xms768m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
    }
  ]
}
```

### Tomcat Tuning

Create custom `server.xml` configuration:
- Adjust thread pool size
- Configure connection timeout
- Enable compression

### Resource Allocation

**Fargate CPU/Memory Combinations**:
- CPU: 512 (.5 vCPU) → Memory: 1024, 2048, 3072, 4096 MB
- CPU: 1024 (1 vCPU) → Memory: 2048-8192 MB
- CPU: 2048 (2 vCPU) → Memory: 4096-16384 MB

Choose based on application load testing results.

---

## Maintenance

### Regular Tasks

1. **Update Base Images**: Rebuild with latest security patches
2. **Review Logs**: Check for errors and warnings
3. **Monitor Metrics**: Track CPU, memory, and response times
4. **Update Dependencies**: Keep Maven dependencies current
5. **Backup Configuration**: Version control all IaC files

### Cleanup

```bash
# Delete service
aws ecs delete-service \
  --cluster modresorts-cluster \
  --service modresorts-service \
  --force \
  --region us-east-1

# Delete cluster
aws ecs delete-cluster \
  --cluster modresorts-cluster \
  --region us-east-1

# Delete ALB
aws elbv2 delete-load-balancer \
  --load-balancer-arn <alb-arn> \
  --region us-east-1

# Delete target group
aws elbv2 delete-target-group \
  --target-group-arn <target-group-arn> \
  --region us-east-1

# Delete log group
aws logs delete-log-group \
  --log-group-name /ecs/modresorts \
  --region us-east-1
```

---

## Additional Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS Fargate Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/AWS_Fargate.html)
- [Docker Documentation](https://docs.docker.com/)
- [Apache Tomcat Documentation](https://tomcat.apache.org/tomcat-9.0-doc/)

---

## Support

For issues or questions:
1. Check CloudWatch logs: `/ecs/modresorts`
2. Review ECS service events
3. Consult AWS documentation
4. Contact your DevOps team

---

**Last Updated**: 2024
**Version**: 1.0.0
