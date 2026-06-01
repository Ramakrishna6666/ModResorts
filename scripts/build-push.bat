@echo off
setlocal enabledelayedexpansion

:: =============================================================================
:: build-push.bat - Build and push Docker image for newcontainertest
:: ModResorts Java EE Web Application
:: =============================================================================

echo ==============================================
echo   newcontainertest - Docker Build ^& Push
echo ==============================================
echo.

set PROJECT_NAME=newcontainertest
set IMAGE_NAME=newcontainertest

echo Select container registry:
echo   1. AWS ECR (Elastic Container Registry)
echo   2. Docker Hub
echo.
set /p REGISTRY_CHOICE="Enter choice [1 or 2]: "

set /p IMAGE_TAG_INPUT="Enter image tag [default: latest]: "
if "!IMAGE_TAG_INPUT!"=="" (
    set IMAGE_TAG=latest
) else (
    set IMAGE_TAG=!IMAGE_TAG_INPUT!
)
echo Using image tag: !IMAGE_TAG!

if "!REGISTRY_CHOICE!"=="1" (
    echo.
    echo --- AWS ECR Configuration ---
    set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
    set /p AWS_ACCOUNT_ID="Enter AWS Account ID (12-digit): "
    set /p ECR_REPO_INPUT="Enter ECR repository name [default: newcontainertest]: "
    if "!ECR_REPO_INPUT!"=="" (
        set ECR_REPO=newcontainertest
    ) else (
        set ECR_REPO=!ECR_REPO_INPUT!
    )

    set REGISTRY_URL=!AWS_ACCOUNT_ID!.dkr.ecr.!AWS_REGION!.amazonaws.com
    set FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!:!IMAGE_TAG!

    echo.
    echo Logging in to AWS ECR...
    aws ecr get-login-password --region !AWS_REGION! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ERROR: ECR login failed. Check your AWS credentials and region.
        exit /b 1
    )
    echo ECR login successful.

    echo Checking if ECR repository exists...
    aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository: !ECR_REPO!
        aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
        if !ERRORLEVEL! neq 0 (
            echo ERROR: Failed to create ECR repository.
            exit /b 1
        )
    )
    echo ECR repository ready.

) else if "!REGISTRY_CHOICE!"=="2" (
    echo.
    echo --- Docker Hub Configuration ---
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password or access token: "
    set /p DOCKER_NAMESPACE_INPUT="Enter Docker Hub namespace/org [default: same as username]: "
    if "!DOCKER_NAMESPACE_INPUT!"=="" (
        set DOCKER_NAMESPACE=!DOCKER_USERNAME!
    ) else (
        set DOCKER_NAMESPACE=!DOCKER_NAMESPACE_INPUT!
    )

    set FULL_IMAGE_NAME=!DOCKER_NAMESPACE!/!IMAGE_NAME!:!IMAGE_TAG!

    echo.
    echo Logging in to Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Docker Hub login failed. Check your credentials.
        exit /b 1
    )
    echo Docker Hub login successful.

) else (
    echo ERROR: Invalid choice. Please enter 1 or 2.
    exit /b 1
)

echo.
echo Building Docker image: !FULL_IMAGE_NAME!
echo Build context: . (project root)
echo.

docker build -f Dockerfile -t "!FULL_IMAGE_NAME!" .
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker build failed.
    exit /b 1
)
echo Docker build successful.

echo.
echo Pushing image: !FULL_IMAGE_NAME!
docker push "!FULL_IMAGE_NAME!"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker push failed.
    exit /b 1
)

echo.
echo ==============================================
echo   SUCCESS: Image pushed successfully!
echo   Image: !FULL_IMAGE_NAME!
echo ==============================================
echo.
echo Use this image URI in your ECS deployment:
echo   !FULL_IMAGE_NAME!

endlocal
