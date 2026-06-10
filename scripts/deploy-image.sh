#!/bin/bash

# Deploy ModResorts to AWS ECS Fargate
# This script registers task definition and creates/updates ECS service

set -e
set -o pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}ModResorts - AWS ECS Fargate Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Project configuration
PROJECT_NAME="modresorts"
SERVICE_NAME="${PROJECT_NAME}-service"
TASK_FAMILY="${PROJECT_NAME}-task"

# Prompt for AWS configuration
echo -e "${BLUE}=== AWS Configuration ===${NC}"
read -p "Enter AWS Region (e.g., us-east-1): " AWS_REGION
read -p "Enter ECS Cluster Name: " CLUSTER_NAME
echo ""

# Get AWS Account ID
echo -e "${YELLOW}Retrieving AWS Account ID...${NC}"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to retrieve AWS Account ID. Please check AWS CLI configuration.${NC}"
    exit 1
fi
echo -e "${GREEN}AWS Account ID: ${ACCOUNT_ID}${NC}"
echo ""

# Check if cluster exists, create if not
echo -e "${YELLOW}Checking if ECS cluster exists...${NC}"
aws ecs describe-clusters --clusters "$CLUSTER_NAME" --region "$AWS_REGION" >/dev/null 2>&1 || {
    echo -e "${YELLOW}Cluster does not exist. Creating ECS cluster: $CLUSTER_NAME${NC}"
    aws ecs create-cluster --cluster-name "$CLUSTER_NAME" --region "$AWS_REGION"
    echo -e "${GREEN}ECS cluster created successfully${NC}"
}
echo ""

# Prompt for network configuration
echo -e "${BLUE}=== Network Configuration ===${NC}"
read -p "Enter VPC ID: " VPC_ID
read -p "Enter Subnet IDs (comma-separated, at least 2): " SUBNETS_INPUT
read -p "Enter Security Group ID: " SECURITY_GROUP
echo ""

# Parse subnets
IFS=',' read -ra SUBNET_ARRAY <<< "$SUBNETS_INPUT"
SUBNET_1=$(echo "${SUBNET_ARRAY[0]}" | xargs)
SUBNET_2=$(echo "${SUBNET_ARRAY[1]}" | xargs)

# Prompt for Docker image
echo -e "${BLUE}=== Docker Image Configuration ===${NC}"
read -p "Enter Docker Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI
echo ""

# Load balancer configuration
echo -e "${BLUE}=== Load Balancer Configuration ===${NC}"
read -p "Do you need a load balancer for this service? (y/n): " NEED_LB

TARGET_GROUP_ARN=""
if [[ "$NEED_LB" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Creating Application Load Balancer and Target Group...${NC}"
    
    # Create target group with target-type ip (required for Fargate awsvpc mode)
    TG_NAME="${PROJECT_NAME}-tg-$(date +%s)"
    TARGET_GROUP_ARN=$(aws elbv2 create-target-group \
        --name "$TG_NAME" \
        --protocol HTTP \
        --port 8080 \
        --vpc-id "$VPC_ID" \
        --target-type ip \
        --health-check-enabled \
        --health-check-protocol HTTP \
        --health-check-path "/health" \
        --health-check-interval-seconds 30 \
        --health-check-timeout-seconds 5 \
        --healthy-threshold-count 2 \
        --unhealthy-threshold-count 3 \
        --region "$AWS_REGION" \
        --query 'TargetGroups[0].TargetGroupArn' \
        --output text)
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to create target group${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Target Group created: $TARGET_GROUP_ARN${NC}"
    
    # Create Application Load Balancer
    ALB_NAME="${PROJECT_NAME}-alb"
    ALB_ARN=$(aws elbv2 create-load-balancer \
        --name "$ALB_NAME" \
        --subnets "$SUBNET_1" "$SUBNET_2" \
        --security-groups "$SECURITY_GROUP" \
        --scheme internet-facing \
        --type application \
        --ip-address-type ipv4 \
        --region "$AWS_REGION" \
        --query 'LoadBalancers[0].LoadBalancerArn' \
        --output text)
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to create load balancer${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Load Balancer created: $ALB_ARN${NC}"
    
    # Create listener
    aws elbv2 create-listener \
        --load-balancer-arn "$ALB_ARN" \
        --protocol HTTP \
        --port 80 \
        --default-actions Type=forward,TargetGroupArn="$TARGET_GROUP_ARN" \
        --region "$AWS_REGION" >/dev/null
    
    echo -e "${GREEN}Listener created on port 80${NC}"
    
    # Get ALB DNS name
    ALB_DNS=$(aws elbv2 describe-load-balancers \
        --load-balancer-arns "$ALB_ARN" \
        --region "$AWS_REGION" \
        --query 'LoadBalancers[0].DNSName' \
        --output text)
    
    echo -e "${GREEN}Load Balancer DNS: ${ALB_DNS}${NC}"
    echo ""
fi

# Create CloudWatch log group
echo -e "${YELLOW}Creating CloudWatch log group...${NC}"
aws logs create-log-group --log-group-name "/ecs/${PROJECT_NAME}" --region "$AWS_REGION" 2>/dev/null || echo -e "${YELLOW}Log group already exists${NC}"
echo ""

# Prepare task definition
echo -e "${YELLOW}Preparing task definition...${NC}"
TASK_DEF_FILE="../ecs/task-definition.json"
TASK_DEF_TEMP="/tmp/task-definition-${PROJECT_NAME}.json"

cp "$TASK_DEF_FILE" "$TASK_DEF_TEMP"

# Replace placeholders in task definition
sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g" "$TASK_DEF_TEMP"
sed -i "s|{{AWS_REGION}}|${AWS_REGION}|g" "$TASK_DEF_TEMP"
sed -i "s|{{ACCOUNT_ID}}|${ACCOUNT_ID}|g" "$TASK_DEF_TEMP"

# Register task definition
echo -e "${YELLOW}Registering task definition...${NC}"
TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://"$TASK_DEF_TEMP" \
    --region "$AWS_REGION" \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

if [ $? -ne 0 ]; then
    echo -e "${RED}Failed to register task definition${NC}"
    exit 1
fi

echo -e "${GREEN}Task definition registered: $TASK_DEF_ARN${NC}"
echo ""

# Prepare service definition
echo -e "${YELLOW}Preparing service definition...${NC}"
SERVICE_DEF_FILE="../ecs/service-definition.json"
SERVICE_DEF_TEMP="/tmp/service-definition-${PROJECT_NAME}.json"

cp "$SERVICE_DEF_FILE" "$SERVICE_DEF_TEMP"

# Replace placeholders in service definition
sed -i "s|{{CLUSTER_NAME}}|${CLUSTER_NAME}|g" "$SERVICE_DEF_TEMP"
sed -i "s|{{SUBNET_1}}|${SUBNET_1}|g" "$SERVICE_DEF_TEMP"
sed -i "s|{{SUBNET_2}}|${SUBNET_2}|g" "$SERVICE_DEF_TEMP"
sed -i "s|{{SECURITY_GROUP}}|${SECURITY_GROUP}|g" "$SERVICE_DEF_TEMP"
sed -i "s|{{TARGET_GROUP_ARN}}|${TARGET_GROUP_ARN}|g" "$SERVICE_DEF_TEMP"

# Remove loadBalancers section if not needed
if [[ ! "$NEED_LB" =~ ^[Yy]$ ]]; then
    # Remove loadBalancers and healthCheckGracePeriodSeconds from service definition
    python3 -c "
import json
import sys

with open('$SERVICE_DEF_TEMP', 'r') as f:
    service_def = json.load(f)

if 'loadBalancers' in service_def:
    del service_def['loadBalancers']
if 'healthCheckGracePeriodSeconds' in service_def:
    del service_def['healthCheckGracePeriodSeconds']

with open('$SERVICE_DEF_TEMP', 'w') as f:
    json.dump(service_def, f, indent=2)
" 2>/dev/null || {
        # Fallback if python3 is not available
        sed -i '/"loadBalancers":/,/],/d' "$SERVICE_DEF_TEMP"
        sed -i '/"healthCheckGracePeriodSeconds":/d' "$SERVICE_DEF_TEMP"
    }
fi

# Check if service exists
echo -e "${YELLOW}Checking if service exists...${NC}"
EXISTING_SERVICE=$(aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[?status==`ACTIVE`].serviceName' \
    --output text 2>/dev/null)

if [ -z "$EXISTING_SERVICE" ] || [ "$EXISTING_SERVICE" == "None" ]; then
    # Create new service
    echo -e "${YELLOW}Creating new ECS service...${NC}"
    aws ecs create-service \
        --cli-input-json file://"$SERVICE_DEF_TEMP" \
        --region "$AWS_REGION" >/dev/null
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to create service${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}ECS service created successfully${NC}"
else
    # Update existing service
    echo -e "${YELLOW}Updating existing ECS service...${NC}"
    aws ecs update-service \
        --cluster "$CLUSTER_NAME" \
        --service "$SERVICE_NAME" \
        --task-definition "$TASK_DEF_ARN" \
        --force-new-deployment \
        --region "$AWS_REGION" >/dev/null
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to update service${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}ECS service updated successfully${NC}"
fi

echo ""

# Wait for service to stabilize
echo -e "${YELLOW}Waiting for service to stabilize (this may take a few minutes)...${NC}"
aws ecs wait services-stable \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION"

if [ $? -ne 0 ]; then
    echo -e "${RED}Service failed to stabilize. Check ECS console for details.${NC}"
    exit 1
fi

echo -e "${GREEN}Service is stable${NC}"
echo ""

# Verify deployment
echo -e "${BLUE}=== Deployment Summary ===${NC}"
SERVICE_INFO=$(aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[0].[runningCount,desiredCount,status]' \
    --output text)

echo -e "${YELLOW}Service Status:${NC} $SERVICE_INFO"
echo -e "${YELLOW}CloudWatch Logs:${NC} /ecs/${PROJECT_NAME}"

if [[ "$NEED_LB" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Load Balancer DNS:${NC} ${ALB_DNS}"
    echo -e "${GREEN}Application URL:${NC} http://${ALB_DNS}/health"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Cleanup temp files
rm -f "$TASK_DEF_TEMP" "$SERVICE_DEF_TEMP"

echo -e "${BLUE}Troubleshooting Tips:${NC}"
echo "1. View logs: aws logs tail /ecs/${PROJECT_NAME} --follow --region ${AWS_REGION}"
echo "2. Check tasks: aws ecs list-tasks --cluster ${CLUSTER_NAME} --region ${AWS_REGION}"
echo "3. Describe service: aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
echo ""
