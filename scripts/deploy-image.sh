#!/bin/bash

# AWS ECS Fargate Deployment Script for ModResorts Application
# This script deploys the Docker image to AWS ECS Fargate

set -e  # Exit on any error
set -o pipefail  # Catch errors in pipes

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}ModResorts AWS ECS Fargate Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Project configuration
PROJECT_NAME="modresorts"
SERVICE_NAME="modresorts-service"
TASK_FAMILY="modresorts-task"

# Prompt for AWS configuration
echo -e "${YELLOW}=== AWS Configuration ===${NC}"
read -p "Enter AWS Region (e.g., us-east-1): " AWS_REGION
read -p "Enter ECS Cluster Name: " CLUSTER_NAME
echo ""

# Get AWS Account ID
echo -e "${GREEN}Retrieving AWS Account ID...${NC}"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo -e "${YELLOW}Account ID:${NC} $ACCOUNT_ID"
echo ""

# Check if cluster exists, create if not
echo -e "${GREEN}Checking ECS cluster...${NC}"
CLUSTER_EXISTS=$(aws ecs describe-clusters --clusters "$CLUSTER_NAME" --region "$AWS_REGION" --query 'clusters[0].clusterName' --output text 2>/dev/null || echo "None")

if [ "$CLUSTER_EXISTS" == "None" ] || [ -z "$CLUSTER_EXISTS" ]; then
    echo -e "${YELLOW}Cluster does not exist. Creating...${NC}"
    aws ecs create-cluster --cluster-name "$CLUSTER_NAME" --region "$AWS_REGION"
    echo -e "${GREEN}Cluster created successfully${NC}"
else
    echo -e "${GREEN}Cluster exists: $CLUSTER_EXISTS${NC}"
fi
echo ""

# Prompt for network configuration
echo -e "${YELLOW}=== Network Configuration ===${NC}"
read -p "Enter VPC ID: " VPC_ID
read -p "Enter Subnet IDs (comma-separated, at least 2): " SUBNETS_INPUT
read -p "Enter Security Group ID: " SECURITY_GROUP
echo ""

# Convert comma-separated subnets to array
IFS=',' read -ra SUBNETS <<< "$SUBNETS_INPUT"
SUBNET_1=$(echo "${SUBNETS[0]}" | xargs)
SUBNET_2=$(echo "${SUBNETS[1]}" | xargs)

# Prompt for Docker image
echo -e "${YELLOW}=== Docker Image Configuration ===${NC}"
read -p "Enter Docker Image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI
echo ""

# Ask about load balancer
echo -e "${YELLOW}=== Load Balancer Configuration ===${NC}"
read -p "Do you need a load balancer for this service? (y/n): " NEED_LB
echo ""

if [[ "$NEED_LB" =~ ^[Yy]$ ]]; then
    echo -e "${GREEN}Creating Application Load Balancer and Target Group...${NC}"
    
    # Create ALB
    ALB_NAME="modresorts-alb"
    echo "Creating Application Load Balancer: $ALB_NAME"
    ALB_ARN=$(aws elbv2 create-load-balancer \
        --name "$ALB_NAME" \
        --subnets "$SUBNET_1" "$SUBNET_2" \
        --security-groups "$SECURITY_GROUP" \
        --scheme internet-facing \
        --type application \
        --ip-address-type ipv4 \
        --region "$AWS_REGION" \
        --query 'LoadBalancers[0].LoadBalancerArn' \
        --output text 2>/dev/null || echo "")
    
    if [ -z "$ALB_ARN" ]; then
        echo -e "${YELLOW}Load balancer may already exist, attempting to retrieve...${NC}"
        ALB_ARN=$(aws elbv2 describe-load-balancers \
            --names "$ALB_NAME" \
            --region "$AWS_REGION" \
            --query 'LoadBalancers[0].LoadBalancerArn' \
            --output text 2>/dev/null || echo "")
    fi
    
    if [ -n "$ALB_ARN" ]; then
        echo -e "${GREEN}Load Balancer ARN: $ALB_ARN${NC}"
        
        # Get ALB DNS name
        ALB_DNS=$(aws elbv2 describe-load-balancers \
            --load-balancer-arns "$ALB_ARN" \
            --region "$AWS_REGION" \
            --query 'LoadBalancers[0].DNSName' \
            --output text)
        echo -e "${GREEN}Load Balancer DNS: $ALB_DNS${NC}"
    fi
    
    # Create Target Group with target-type ip (required for Fargate)
    TG_NAME="modresorts-tg"
    echo "Creating Target Group: $TG_NAME"
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
        --output text 2>/dev/null || echo "")
    
    if [ -z "$TARGET_GROUP_ARN" ]; then
        echo -e "${YELLOW}Target group may already exist, attempting to retrieve...${NC}"
        TARGET_GROUP_ARN=$(aws elbv2 describe-target-groups \
            --names "$TG_NAME" \
            --region "$AWS_REGION" \
            --query 'TargetGroups[0].TargetGroupArn' \
            --output text 2>/dev/null || echo "")
    fi
    
    if [ -n "$TARGET_GROUP_ARN" ]; then
        echo -e "${GREEN}Target Group ARN: $TARGET_GROUP_ARN${NC}"
    fi
    
    # Create listener if ALB and TG exist
    if [ -n "$ALB_ARN" ] && [ -n "$TARGET_GROUP_ARN" ]; then
        echo "Creating ALB Listener..."
        LISTENER_ARN=$(aws elbv2 create-listener \
            --load-balancer-arn "$ALB_ARN" \
            --protocol HTTP \
            --port 80 \
            --default-actions Type=forward,TargetGroupArn="$TARGET_GROUP_ARN" \
            --region "$AWS_REGION" \
            --query 'Listeners[0].ListenerArn' \
            --output text 2>/dev/null || echo "")
        
        if [ -n "$LISTENER_ARN" ]; then
            echo -e "${GREEN}Listener created successfully${NC}"
        else
            echo -e "${YELLOW}Listener may already exist${NC}"
        fi
    fi
    
    echo ""
else
    TARGET_GROUP_ARN=""
    echo -e "${YELLOW}Skipping load balancer creation${NC}"
    echo ""
fi

# Create CloudWatch Log Group
echo -e "${GREEN}Creating CloudWatch Log Group...${NC}"
aws logs create-log-group --log-group-name "/ecs/modresorts" --region "$AWS_REGION" 2>/dev/null || echo -e "${YELLOW}Log group may already exist${NC}"
echo ""

# Prepare task definition JSON
echo -e "${GREEN}Preparing task definition...${NC}"
TASK_DEF_FILE="../ecs/task-definition.json"

# Create temporary file with replacements
TEMP_TASK_DEF=$(mktemp)
sed "s|{{IMAGE_URI}}|$IMAGE_URI|g; s|{{AWS_REGION}}|$AWS_REGION|g; s|{{ACCOUNT_ID}}|$ACCOUNT_ID|g" "$TASK_DEF_FILE" > "$TEMP_TASK_DEF"

# Register task definition
echo -e "${GREEN}Registering task definition...${NC}"
TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://"$TEMP_TASK_DEF" \
    --region "$AWS_REGION" \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo -e "${GREEN}Task Definition ARN: $TASK_DEF_ARN${NC}"
rm "$TEMP_TASK_DEF"
echo ""

# Prepare service definition JSON
echo -e "${GREEN}Preparing service definition...${NC}"
SERVICE_DEF_FILE="../ecs/service-definition.json"

# Create temporary file with replacements
TEMP_SERVICE_DEF=$(mktemp)
sed "s|{{CLUSTER_NAME}}|$CLUSTER_NAME|g; s|{{SUBNET_1}}|$SUBNET_1|g; s|{{SUBNET_2}}|$SUBNET_2|g; s|{{SECURITY_GROUP}}|$SECURITY_GROUP|g; s|{{TARGET_GROUP_ARN}}|$TARGET_GROUP_ARN|g" "$SERVICE_DEF_FILE" > "$TEMP_SERVICE_DEF"

# Remove loadBalancers section if no load balancer
if [ -z "$TARGET_GROUP_ARN" ]; then
    # Remove loadBalancers and healthCheckGracePeriodSeconds from service definition
    python3 -c "
import json
import sys
with open('$TEMP_SERVICE_DEF', 'r') as f:
    data = json.load(f)
data.pop('loadBalancers', None)
data.pop('healthCheckGracePeriodSeconds', None)
with open('$TEMP_SERVICE_DEF', 'w') as f:
    json.dump(data, f, indent=2)
" 2>/dev/null || {
    # Fallback if python3 is not available
    jq 'del(.loadBalancers, .healthCheckGracePeriodSeconds)' "$TEMP_SERVICE_DEF" > "${TEMP_SERVICE_DEF}.tmp" && mv "${TEMP_SERVICE_DEF}.tmp" "$TEMP_SERVICE_DEF"
}
fi

# Check if service exists
echo -e "${GREEN}Checking if service exists...${NC}"
SERVICE_EXISTS=$(aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[0].serviceName' \
    --output text 2>/dev/null || echo "None")

if [ "$SERVICE_EXISTS" == "None" ] || [ -z "$SERVICE_EXISTS" ]; then
    # Create new service
    echo -e "${GREEN}Creating new ECS service...${NC}"
    aws ecs create-service \
        --cli-input-json file://"$TEMP_SERVICE_DEF" \
        --region "$AWS_REGION"
    
    echo -e "${GREEN}Service created successfully${NC}"
else
    # Update existing service
    echo -e "${GREEN}Updating existing ECS service...${NC}"
    aws ecs update-service \
        --cluster "$CLUSTER_NAME" \
        --service "$SERVICE_NAME" \
        --task-definition "$TASK_DEF_ARN" \
        --desired-count 2 \
        --region "$AWS_REGION"
    
    echo -e "${GREEN}Service updated successfully${NC}"
fi

rm "$TEMP_SERVICE_DEF"
echo ""

# Wait for service to stabilize
echo -e "${GREEN}Waiting for service to stabilize (this may take a few minutes)...${NC}"
aws ecs wait services-stable \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION"

echo -e "${GREEN}Service is stable${NC}"
echo ""

# Verify deployment
echo -e "${GREEN}Verifying deployment...${NC}"
aws ecs describe-services \
    --cluster "$CLUSTER_NAME" \
    --services "$SERVICE_NAME" \
    --region "$AWS_REGION" \
    --query 'services[0].[serviceName,status,runningCount,desiredCount]' \
    --output table

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Completed Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}Service Details:${NC}"
echo "  Cluster: $CLUSTER_NAME"
echo "  Service: $SERVICE_NAME"
echo "  Region: $AWS_REGION"
echo ""
if [ -n "$ALB_DNS" ]; then
    echo -e "${YELLOW}Application URL:${NC}"
    echo "  http://$ALB_DNS"
    echo ""
fi
echo -e "${YELLOW}CloudWatch Logs:${NC}"
echo "  Log Group: /ecs/modresorts"
echo "  Region: $AWS_REGION"
echo ""
echo -e "${YELLOW}Useful Commands:${NC}"
echo "  View service: aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $AWS_REGION"
echo "  View tasks: aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --region $AWS_REGION"
echo "  View logs: aws logs tail /ecs/modresorts --follow --region $AWS_REGION"
echo ""
