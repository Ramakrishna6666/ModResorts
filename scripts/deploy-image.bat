@echo off
setlocal enabledelayedexpansion

REM ModResorts - GKE Deployment Script (Windows)
REM This script deploys the ModResorts application to Google Kubernetes Engine (GKE)

echo ==========================================
echo ModResorts - GKE Deployment Script
echo ==========================================
echo.

REM Prompt for GCP configuration
echo === GCP Configuration ===
set /p GCP_PROJECT="Enter GCP Project ID: "
set /p GCP_ZONE="Enter GCP Zone (e.g., us-central1-a): "
set /p CLUSTER_NAME="Enter GKE Cluster Name: "

echo.
echo === Docker Image Configuration ===
set /p IMAGE_URI="Enter Docker Image URI (with tag): "

echo.
echo === Application Configuration ===
echo Configure external service connections (press Enter to skip):
echo.

set /p REDIS_HOST="Enter Redis Host (default: localhost): "
if "!REDIS_HOST!"=="" set REDIS_HOST=localhost

set /p REDIS_PORT="Enter Redis Port (default: 6379): "
if "!REDIS_PORT!"=="" set REDIS_PORT=6379

set /p DB_URL="Enter Database URL (default: jdbc:h2:mem:testdb): "
if "!DB_URL!"=="" set DB_URL=jdbc:h2:mem:testdb

set /p DB_DRIVER="Enter Database Driver (default: org.h2.Driver): "
if "!DB_DRIVER!"=="" set DB_DRIVER=org.h2.Driver

set /p NAMING_FACTORY_INITIAL="Enter Naming Factory Initial (optional): "
if "!NAMING_FACTORY_INITIAL!"=="" set NAMING_FACTORY_INITIAL=

set /p NAMING_PROVIDER_URL="Enter Naming Provider URL (optional): "
if "!NAMING_PROVIDER_URL!"=="" set NAMING_PROVIDER_URL=

echo.
echo ==========================================
echo Authenticating with GCP...
echo ==========================================
call gcloud auth login
if !ERRORLEVEL! neq 0 (
    echo GCP authentication failed!
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
echo ==========================================
echo Configuring kubectl for GKE cluster...
echo ==========================================
call gcloud container clusters get-credentials !CLUSTER_NAME! --zone !GCP_ZONE! --project !GCP_PROJECT!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl for GKE cluster!
    exit /b 1
)

echo.
echo Verifying cluster connectivity...
call kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Failed to connect to cluster!
    exit /b 1
)

echo.
echo ==========================================
echo Updating Kubernetes manifests...
echo ==========================================

REM Update deployment.yaml with image URI and environment variables
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '%IMAGE_URI%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_HOST}}', '%REDIS_HOST%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_PORT}}', '%REDIS_PORT%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_URL}}', '%DB_URL%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_DRIVER}}', '%DB_DRIVER%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{NAMING_FACTORY_INITIAL}}', '%NAMING_FACTORY_INITIAL%' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{NAMING_PROVIDER_URL}}', '%NAMING_PROVIDER_URL%' | Set-Content kubernetes\deployment.yaml"

echo Manifests updated successfully

echo.
echo ==========================================
echo Deploying to GKE...
echo ==========================================

echo.
echo Creating namespace...
call kubectl apply -f kubernetes\namespace.yaml

echo.
echo Deploying application...
call kubectl apply -f kubernetes\deployment.yaml

echo.
echo Creating service...
call kubectl apply -f kubernetes\service.yaml

echo.
echo Creating ingress...
call kubectl apply -f kubernetes\ingress.yaml

echo.
echo ==========================================
echo Waiting for deployment rollout...
echo ==========================================
call kubectl rollout status deployment/modresorts -n modresorts --timeout=5m
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed!
    echo.
    echo Checking pod status...
    call kubectl get pods -n modresorts
    echo.
    echo Checking pod logs...
    call kubectl logs -n modresorts -l app=modresorts --tail=50
    exit /b 1
)

echo.
echo ==========================================
echo Verifying deployment...
echo ==========================================
call kubectl get pods,svc,ingress -n modresorts

echo.
echo ==========================================
echo Deployment completed successfully!
echo ==========================================
echo.
echo Application Details:
echo   Namespace: modresorts
echo   Deployment: modresorts
echo   Service: modresorts-service
echo   Ingress: modresorts-ingress
echo.
echo To check application status:
echo   kubectl get pods -n modresorts
echo   kubectl logs -n modresorts -l app=modresorts
echo.
echo To access the application:
echo   kubectl port-forward -n modresorts svc/modresorts-service 8080:80
echo   Then visit: http://localhost:8080
echo.
echo To get ingress IP address:
echo   kubectl get ingress modresorts-ingress -n modresorts
echo.

endlocal
