@echo off
setlocal enabledelayedexpansion

:: =============================================================================
:: deploy-image.bat - Deploy newcontainertest to AWS ECS Fargate (Windows)
:: ModResorts Java EE Web Application
:: =============================================================================

echo ==============================================
echo   newcontainertest - AWS ECS Fargate Deploy
echo ==============================================
echo.

set PROJECT_NAME=newcontainertest
set SERVICE_NAME=newcontainertest-service
set TASK_FAMILY=newcontainertest-task
set LOG_GROUP=/ecs/newcontainertest

:: ---- Collect deployment parameters ----
set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
set /p CLUSTER_INPUT="Enter ECS Cluster name [default: newcontainertest-cluster]: "
if "!CLUSTER_INPUT!"=="" (
    set CLUSTER_NAME=newcontainertest-cluster
) else (
    set CLUSTER_NAME=!CLUSTER_INPUT!
)
set /p IMAGE_URI="Enter ECR Image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/newcontainertest:latest): "
set /p VPC_ID="Enter VPC ID (e.g. vpc-xxxxxxxx): "
set /p SUBNETS_INPUT="Enter Subnet IDs comma-separated (e.g. subnet-aaa,subnet-bbb): "
set /p SECURITY_GROUP="Enter Security Group ID (e.g. sg-xxxxxxxx): "

:: Parse subnets
for /f "tokens=1,2 delims=," %%a in ("!SUBNETS_INPUT!") do (
    set SUBNET_1=%%a
    set SUBNET_2=%%b
)
if "!SUBNET_2!"=="" set SUBNET_2=!SUBNET_1!

:: ---- Get AWS Account ID ----
echo.
echo Retrieving AWS Account ID...
for /f "tokens=*" %%i in ('aws sts get-caller-identity --query Account --output text') do set ACCOUNT_ID=%%i
echo Account ID: !ACCOUNT_ID!

:: ---- Ensure CloudWatch Log Group exists ----
echo.
echo Ensuring CloudWatch log group exists...
aws logs create-log-group --log-group-name "!LOG_GROUP!" --region "!AWS_REGION!" >nul 2>&1
echo Log group ready.

:: ---- Check / Create ECS Cluster ----
echo.
echo Checking ECS cluster...
for /f "tokens=*" %%i in ('aws ecs describe-clusters --clusters "!CLUSTER_NAME!" --region "!AWS_REGION!" --query "clusters[0].status" --output text 2^>nul') do set CLUSTER_STATUS=%%i
if not "!CLUSTER_STATUS!"=="ACTIVE" (
    echo Creating ECS cluster !CLUSTER_NAME!...
    aws ecs create-cluster --cluster-name "!CLUSTER_NAME!" --region "!AWS_REGION!"
    echo Cluster created.
) else (
    echo Cluster !CLUSTER_NAME! is ACTIVE.
)

:: ---- Load Balancer ----
echo.
set /p NEED_LB="Do you need an Application Load Balancer for this service? (y/n): "
set TARGET_GROUP_ARN=
set LB_DNS=

if /i "!NEED_LB!"=="y" (
    echo.
    echo Creating Application Load Balancer...
    set LB_NAME=!PROJECT_NAME!-alb
    set TG_NAME=!PROJECT_NAME!-tg

    for /f "tokens=*" %%i in ('aws elbv2 create-load-balancer --name "!LB_NAME!" --subnets "!SUBNET_1!" "!SUBNET_2!" --security-groups "!SECURITY_GROUP!" --scheme internet-facing --type application --region "!AWS_REGION!" --query "LoadBalancers[0].LoadBalancerArn" --output text') do set LB_ARN=%%i
    echo ALB created: !LB_ARN!

    for /f "tokens=*" %%i in ('aws elbv2 describe-load-balancers --load-balancer-arns "!LB_ARN!" --region "!AWS_REGION!" --query "LoadBalancers[0].DNSName" --output text') do set LB_DNS=%%i

    for /f "tokens=*" %%i in ('aws elbv2 create-target-group --name "!TG_NAME!" --protocol HTTP --port 8080 --vpc-id "!VPC_ID!" --target-type ip --health-check-path "/resorts/health" --health-check-interval-seconds 30 --healthy-threshold-count 2 --unhealthy-threshold-count 3 --region "!AWS_REGION!" --query "TargetGroups[0].TargetGroupArn" --output text') do set TARGET_GROUP_ARN=%%i
    echo Target Group created: !TARGET_GROUP_ARN!

    aws elbv2 create-listener --load-balancer-arn "!LB_ARN!" --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn="!TARGET_GROUP_ARN!" --region "!AWS_REGION!" >nul
    echo ALB Listener created on port 80.
)

:: ---- Prepare task definition ----
echo.
echo Preparing ECS task definition...
copy /Y ecs\task-definition.json %TEMP%\task-definition-deploy.json >nul

powershell -Command "(Get-Content '%TEMP%\task-definition-deploy.json') -replace '{{IMAGE_URI}}', '!IMAGE_URI!' -replace '{{AWS_REGION}}', '!AWS_REGION!' -replace '{{ACCOUNT_ID}}', '!ACCOUNT_ID!' | Set-Content '%TEMP%\task-definition-deploy.json'"

:: ---- Register Task Definition ----
echo Registering ECS task definition...
for /f "tokens=*" %%i in ('aws ecs register-task-definition --cli-input-json file://%TEMP%\task-definition-deploy.json --region "!AWS_REGION!" --query "taskDefinition.taskDefinitionArn" --output text') do set TASK_DEF_ARN=%%i
echo Task definition registered: !TASK_DEF_ARN!

:: ---- Prepare service definition ----
echo.
echo Preparing ECS service definition...
copy /Y ecs\service-definition.json %TEMP%\service-definition-deploy.json >nul

powershell -Command "(Get-Content '%TEMP%\service-definition-deploy.json') -replace '{{CLUSTER_NAME}}', '!CLUSTER_NAME!' -replace '{{SUBNET_1}}', '!SUBNET_1!' -replace '{{SUBNET_2}}', '!SUBNET_2!' -replace '{{SECURITY_GROUP}}', '!SECURITY_GROUP!' | Set-Content '%TEMP%\service-definition-deploy.json'"

:: ---- Check if service exists ----
echo.
echo Checking if ECS service exists...
for /f "tokens=*" %%i in ('aws ecs describe-services --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!" --query "services[?status!='INACTIVE'].serviceName" --output text 2^>nul') do set EXISTING_SERVICE=%%i

if "!EXISTING_SERVICE!"=="" (
    echo Creating new ECS service !SERVICE_NAME!...
    powershell -Command "(Get-Content '%TEMP%\service-definition-deploy.json') -replace '\"taskDefinition\": \"!TASK_FAMILY!\"', '\"taskDefinition\": \"!TASK_DEF_ARN!\"' | Set-Content '%TEMP%\service-definition-deploy.json'"
    aws ecs create-service --cli-input-json file://%TEMP%\service-definition-deploy.json --region "!AWS_REGION!"
    echo Service created.
) else (
    echo Updating existing ECS service !SERVICE_NAME!...
    aws ecs update-service --cluster "!CLUSTER_NAME!" --service "!SERVICE_NAME!" --task-definition "!TASK_DEF_ARN!" --region "!AWS_REGION!" >nul
    echo Service updated.
)

:: ---- Wait for service stability ----
echo.
echo Waiting for service to become stable...
aws ecs wait services-stable --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!"
echo Service is stable.

:: ---- Verify deployment ----
echo.
echo Verifying deployment...
aws ecs describe-services --cluster "!CLUSTER_NAME!" --services "!SERVICE_NAME!" --region "!AWS_REGION!" --query "services[0].{ServiceName:serviceName,Status:status,DesiredCount:desiredCount,RunningCount:runningCount}"

echo.
echo ==============================================
echo   DEPLOYMENT COMPLETE
echo ==============================================
echo   Cluster:   !CLUSTER_NAME!
echo   Service:   !SERVICE_NAME!
echo   Log Group: !LOG_GROUP!
if not "!LB_DNS!"=="" (
    echo   App URL:   http://!LB_DNS!/resorts/
)
echo.
echo Troubleshooting:
echo   View logs:  aws logs tail !LOG_GROUP! --follow --region !AWS_REGION!
echo   List tasks: aws ecs list-tasks --cluster !CLUSTER_NAME! --service-name !SERVICE_NAME! --region !AWS_REGION!

endlocal
