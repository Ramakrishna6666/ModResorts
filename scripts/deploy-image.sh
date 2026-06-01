#!/bin/bash
set -e
set -o pipefail

# =============================================================================
# deploy-image.sh - Deploy newcontainertest to AWS ECS Fargate
# ModResorts Java EE Web Application
# =============================================================================

echo "=============================================="
echo "  newcontainertest - AWS ECS Fargate Deploy"
echo "=============================================="
echo ""

PROJECT_NAME="newcontainertest"
SERVICE_NAME="${PROJECT_NAME}-service"
TASK_FAMILY="${PROJECT_NAME}-task"
LOG_GROUP="/ecs/${PROJECT_NAME}"

# ---- Collect deployment parameters ----
read -p "Enter AWS Region (e.g. us-east-1): " AWS_REGION
read -p "Enter ECS Cluster name [default: newcontainertest-cluster]: " CLUSTER_INPUT
CLUSTER_NAME="${CLUSTER_INPUT:-newcontainertest-cluster}"
read -p "Enter ECR Image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/newcontainertest:latest): " IMAGE_URI
read -p "Enter VPC ID (e.g. vpc-xxxxxxxx): " VPC_ID
read -p "Enter Subnet IDs comma-separated (e.g. subnet-aaa,subnet-bbb): " SUBNETS_INPUT
read -p "Enter Security Group ID (e.g. sg-xxxxxxxx): " SECURITY_GROUP

# Parse subnets
SUBNET_1=$(echo "$SUBNETS_INPUT" | cut -d',' -f1 | tr -d ' ')
SUBNET_2=$(echo "$SUBNETS_INPUT" | cut -d',' -f2 | tr -d ' ')
if [ -z "$SUBNET_2" ]; then
  SUBNET_2="$SUBNET_1"
fi

# ---- Get AWS Account ID ----
echo ""
echo "Retrieving AWS Account ID..."
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "Account ID: ${ACCOUNT_ID}"

# ---- Ensure CloudWatch Log Group exists ----
echo ""
echo "Ensuring CloudWatch log group '${LOG_GROUP}' exists..."
aws logs create-log-group --log-group-name "$LOG_GROUP" --region "$AWS_REGION" 2>/dev/null || true
echo "Log group ready."

# ---- Check / Create ECS Cluster ----
echo ""
echo "Checking ECS cluster '${CLUSTER_NAME}'..."
CLUSTER_STATUS=$(aws ecs describe-clusters --clusters "$CLUSTER_NAME" --region "$AWS_REGION" \
  --query "clusters[0].status" --output text 2>/dev/null || echo "MISSING")
if [ "$CLUSTER_STATUS" != "ACTIVE" ]; then
  echo "Creating ECS cluster '${CLUSTER_NAME}'..."
  aws ecs create-cluster --cluster-name "$CLUSTER_NAME" --region "$AWS_REGION"
  echo "Cluster created."
else
  echo "Cluster '${CLUSTER_NAME}' is ACTIVE."
fi

# ---- Load Balancer ----
echo ""
read -p "Do you need an Application Load Balancer for this service? (y/n): " NEED_LB

TARGET_GROUP_ARN=""
LB_DNS=""

if [ "$NEED_LB" = "y" ] || [ "$NEED_LB" = "Y" ]; then
  echo ""
  echo "Creating Application Load Balancer..."

  LB_NAME="${PROJECT_NAME}-alb"
  TG_NAME="${PROJECT_NAME}-tg"

  # Create ALB
  LB_ARN=$(aws elbv2 create-load-balancer \
    --name "$LB_NAME" \
    --subnets $SUBNET_1 $SUBNET_2 \
    --security-groups "$SECURITY_GROUP" \
    --scheme internet-facing \
    --type application \
    --region "$AWS_REGION" \
    --query "LoadBalancers[0].LoadBalancerArn" \
    --output text)
  echo "ALB created: ${LB_ARN}"

  LB_DNS=$(aws elbv2 describe-load-balancers \
    --load-balancer-arns "$LB_ARN" \
    --region "$AWS_REGION" \
    --query "LoadBalancers[0].DNSName" \
    --output text)

  # Create Target Group (target-type ip required for Fargate awsvpc)
  TARGET_GROUP_ARN=$(aws elbv2 create-target-group \
    --name "$TG_NAME" \
    --protocol HTTP \
    --port 8080 \
    --vpc-id "$VPC_ID" \
    --target-type ip \
    --health-check-path "/resorts/health" \
    --health-check-interval-seconds 30 \
    --healthy-threshold-count 2 \
    --unhealthy-threshold-count 3 \
    --region "$AWS_REGION" \
    --query "TargetGroups[0].TargetGroupArn" \
    --output text)
  echo "Target Group created: ${TARGET_GROUP_ARN}"

  # Create ALB Listener
  aws elbv2 create-listener \
    --load-balancer-arn "$LB_ARN" \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn="$TARGET_GROUP_ARN" \
    --region "$AWS_REGION" > /dev/null
  echo "ALB Listener created on port 80."
fi

# ---- Prepare task definition JSON ----
echo ""
echo "Preparing ECS task definition..."
cp ecs/task-definition.json /tmp/task-definition-deploy.json

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g" /tmp/task-definition-deploy.json
sed -i "s|{{AWS_REGION}}|${AWS_REGION}|g" /tmp/task-definition-deploy.json
sed -i "s|{{ACCOUNT_ID}}|${ACCOUNT_ID}|g" /tmp/task-definition-deploy.json

# ---- Register Task Definition ----
echo "Registering ECS task definition..."
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file:///tmp/task-definition-deploy.json \
  --region "$AWS_REGION" \
  --query "taskDefinition.taskDefinitionArn" \
  --output text)
echo "Task definition registered: ${TASK_DEF_ARN}"

# ---- Prepare service definition JSON ----
echo ""
echo "Preparing ECS service definition..."
cp ecs/service-definition.json /tmp/service-definition-deploy.json

sed -i "s|{{CLUSTER_NAME}}|${CLUSTER_NAME}|g" /tmp/service-definition-deploy.json
sed -i "s|{{SUBNET_1}}|${SUBNET_1}|g" /tmp/service-definition-deploy.json
sed -i "s|{{SUBNET_2}}|${SUBNET_2}|g" /tmp/service-definition-deploy.json
sed -i "s|{{SECURITY_GROUP}}|${SECURITY_GROUP}|g" /tmp/service-definition-deploy.json

# ---- Handle Load Balancer in service definition ----
if [ -n "$TARGET_GROUP_ARN" ]; then
  # Inject loadBalancers and healthCheckGracePeriodSeconds into service JSON
  python3 -c "
import json, sys
with open('/tmp/service-definition-deploy.json') as f:
    svc = json.load(f)
svc['loadBalancers'] = [{
    'targetGroupArn': '${TARGET_GROUP_ARN}',
    'containerName': 'newcontainertest',
    'containerPort': 8080
}]
svc['healthCheckGracePeriodSeconds'] = 300
with open('/tmp/service-definition-deploy.json', 'w') as f:
    json.dump(svc, f, indent=2)
print('Load balancer config injected.')
" 2>/dev/null || \
  sed -i "s|\"desiredCount\"|\"loadBalancers\": [{\"targetGroupArn\": \"${TARGET_GROUP_ARN}\", \"containerName\": \"newcontainertest\", \"containerPort\": 8080}], \"healthCheckGracePeriodSeconds\": 300, \"desiredCount\"|g" /tmp/service-definition-deploy.json
fi

# ---- Check if service exists ----
echo ""
echo "Checking if ECS service '${SERVICE_NAME}' exists..."
EXISTING_SERVICE=$(aws ecs describe-services \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_NAME" \
  --region "$AWS_REGION" \
  --query "services[?status!='INACTIVE'].serviceName" \
  --output text 2>/dev/null || echo "")

if [ -z "$EXISTING_SERVICE" ] || [ "$EXISTING_SERVICE" = "None" ]; then
  echo "Creating new ECS service '${SERVICE_NAME}'..."
  # Update task definition ARN in service JSON
  sed -i "s|\"taskDefinition\": \"${TASK_FAMILY}\"|\"taskDefinition\": \"${TASK_DEF_ARN}\"|g" /tmp/service-definition-deploy.json
  aws ecs create-service \
    --cli-input-json file:///tmp/service-definition-deploy.json \
    --region "$AWS_REGION"
  echo "Service created."
else
  echo "Updating existing ECS service '${SERVICE_NAME}'..."
  aws ecs update-service \
    --cluster "$CLUSTER_NAME" \
    --service "$SERVICE_NAME" \
    --task-definition "$TASK_DEF_ARN" \
    --region "$AWS_REGION" > /dev/null
  echo "Service updated."
fi

# ---- Wait for service stability ----
echo ""
echo "Waiting for service to become stable (this may take a few minutes)..."
aws ecs wait services-stable \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_NAME" \
  --region "$AWS_REGION"
echo "Service is stable."

# ---- Verify deployment ----
echo ""
echo "Verifying deployment..."
aws ecs describe-services \
  --cluster "$CLUSTER_NAME" \
  --services "$SERVICE_NAME" \
  --region "$AWS_REGION" \
  --query "services[0].{ServiceName:serviceName,Status:status,DesiredCount:desiredCount,RunningCount:runningCount,PendingCount:pendingCount}"

echo ""
echo "=============================================="
echo "  DEPLOYMENT COMPLETE"
echo "=============================================="
echo "  Cluster:      ${CLUSTER_NAME}"
echo "  Service:      ${SERVICE_NAME}"
echo "  Task Def:     ${TASK_DEF_ARN}"
echo "  Log Group:    ${LOG_GROUP}"
if [ -n "$LB_DNS" ]; then
  echo "  App URL:      http://${LB_DNS}/resorts/"
fi
echo ""
echo "Troubleshooting:"
echo "  View logs:    aws logs tail ${LOG_GROUP} --follow --region ${AWS_REGION}"
echo "  List tasks:   aws ecs list-tasks --cluster ${CLUSTER_NAME} --service-name ${SERVICE_NAME} --region ${AWS_REGION}"
echo "  Describe svc: aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
