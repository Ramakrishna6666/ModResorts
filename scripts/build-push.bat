@echo off
setlocal enabledelayedexpansion

REM ModResorts - Docker Build and Push Script (Windows)
REM This script builds the Docker image and pushes it to the selected registry

echo ==========================================
echo ModResorts - Docker Build and Push Script
echo ==========================================
echo.

REM Project configuration
set PROJECT_NAME=modresorts
set IMAGE_NAME=modresorts

REM Prompt for image tag
set /p IMAGE_TAG="Enter image tag (default: latest): "
if "!IMAGE_TAG!"=="" set IMAGE_TAG=latest

echo.
echo Select container registry:
echo 1. Google Artifact Registry (GCP)
echo 2. Docker Hub
echo.
set /p REGISTRY_CHOICE="Enter choice (1 or 2): "

if "!REGISTRY_CHOICE!"=="1" (
    REM Google Artifact Registry
    echo.
    echo === Google Artifact Registry Configuration ===
    set /p GCP_PROJECT="Enter GCP Project ID: "
    set /p GCP_REGION="Enter GCP Region (e.g., us-central1): "
    set /p AR_REPO="Enter Artifact Registry Repository Name: "
    
    set REGISTRY=!GCP_REGION!-docker.pkg.dev
    set FULL_IMAGE_NAME=!REGISTRY!/!GCP_PROJECT!/!AR_REPO!/!IMAGE_NAME!:!IMAGE_TAG!
    
    echo.
    echo Authenticating with Google Cloud...
    call gcloud auth login
    if !ERRORLEVEL! neq 0 (
        echo Google Cloud authentication failed!
        exit /b 1
    )
    
    echo.
    echo Setting GCP project...
    call gcloud config set project !GCP_PROJECT!
    if !ERRORLEVEL! neq 0 (
        echo Failed to set GCP project!
        exit /b 1
    )
    
    echo.
    echo Configuring Docker for Artifact Registry...
    call gcloud auth configure-docker !GCP_REGION!-docker.pkg.dev
    if !ERRORLEVEL! neq 0 (
        echo Artifact Registry authentication failed!
        exit /b 1
    )
    
) else if "!REGISTRY_CHOICE!"=="2" (
    REM Docker Hub
    echo.
    echo === Docker Hub Configuration ===
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password/token: "
    
    set FULL_IMAGE_NAME=!DOCKER_USERNAME!/!IMAGE_NAME!:!IMAGE_TAG!
    
    echo.
    echo Authenticating with Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo Docker Hub authentication failed!
        exit /b 1
    )
    
) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

echo.
echo ==========================================
echo Building Docker image...
echo Image: !FULL_IMAGE_NAME!
echo ==========================================
docker build -t !FULL_IMAGE_NAME! .

if !ERRORLEVEL! neq 0 (
    echo Docker build failed!
    exit /b 1
)

echo.
echo ==========================================
echo Pushing Docker image to registry...
echo ==========================================
docker push !FULL_IMAGE_NAME!

if !ERRORLEVEL! neq 0 (
    echo Docker push failed!
    exit /b 1
)

echo.
echo ==========================================
echo Build and push completed successfully!
echo ==========================================
echo Image: !FULL_IMAGE_NAME!
echo.
echo Next steps:
echo 1. Update kubernetes/deployment.yaml with the image URI
echo 2. Run deploy-image.bat to deploy to GKE
echo.

endlocal
