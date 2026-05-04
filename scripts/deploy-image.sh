#!/bin/bash
set -e
set -o pipefail

# ModResorts - GKE Deployment Script
# This script deploys the ModResorts application to Google Kubernetes Engine (GKE)

echo "=========================================="
echo "ModResorts - GKE Deployment Script"
echo "=========================================="
echo ""

# Prompt for GCP configuration
echo "=== GCP Configuration ==="
read -p "Enter GCP Project ID: " GCP_PROJECT
read -p "Enter GCP Zone (e.g., us-central1-a): " GCP_ZONE
read -p "Enter GKE Cluster Name: " CLUSTER_NAME

echo ""
echo "=== Docker Image Configuration ==="
read -p "Enter Docker Image URI (with tag): " IMAGE_URI

echo ""
echo "=== Application Configuration ==="
echo "Configure external service connections (press Enter to skip):"
echo ""

read -p "Enter Redis Host (default: localhost): " REDIS_HOST
REDIS_HOST=${REDIS_HOST:-localhost}

read -p "Enter Redis Port (default: 6379): " REDIS_PORT
REDIS_PORT=${REDIS_PORT:-6379}

read -p "Enter Database URL (default: jdbc:h2:mem:testdb): " DB_URL
DB_URL=${DB_URL:-jdbc:h2:mem:testdb}

read -p "Enter Database Driver (default: org.h2.Driver): " DB_DRIVER
DB_DRIVER=${DB_DRIVER:-org.h2.Driver}

read -p "Enter Naming Factory Initial (optional): " NAMING_FACTORY_INITIAL
NAMING_FACTORY_INITIAL=${NAMING_FACTORY_INITIAL:-}

read -p "Enter Naming Provider URL (optional): " NAMING_PROVIDER_URL
NAMING_PROVIDER_URL=${NAMING_PROVIDER_URL:-}

echo ""
echo "=========================================="
echo "Authenticating with GCP..."
echo "=========================================="
gcloud auth login

echo ""
echo "Setting GCP project..."
gcloud config set project "$GCP_PROJECT"

echo ""
echo "=========================================="
echo "Configuring kubectl for GKE cluster..."
echo "=========================================="
gcloud container clusters get-credentials "$CLUSTER_NAME" --zone "$GCP_ZONE" --project "$GCP_PROJECT"

if [ $? -ne 0 ]; then
    echo "Failed to configure kubectl for GKE cluster!"
    exit 1
fi

echo ""
echo "Verifying cluster connectivity..."
kubectl cluster-info || exit 1

echo ""
echo "=========================================="
echo "Updating Kubernetes manifests..."
echo "=========================================="

# Update deployment.yaml with image URI and environment variables
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" kubernetes/deployment.yaml
sed -i "s|{{REDIS_HOST}}|$REDIS_HOST|g" kubernetes/deployment.yaml
sed -i "s|{{REDIS_PORT}}|$REDIS_PORT|g" kubernetes/deployment.yaml
sed -i "s|{{DB_URL}}|$DB_URL|g" kubernetes/deployment.yaml
sed -i "s|{{DB_DRIVER}}|$DB_DRIVER|g" kubernetes/deployment.yaml
sed -i "s|{{NAMING_FACTORY_INITIAL}}|$NAMING_FACTORY_INITIAL|g" kubernetes/deployment.yaml
sed -i "s|{{NAMING_PROVIDER_URL}}|$NAMING_PROVIDER_URL|g" kubernetes/deployment.yaml

echo "✓ Manifests updated successfully"

echo ""
echo "=========================================="
echo "Deploying to GKE..."
echo "=========================================="

echo ""
echo "Creating namespace..."
kubectl apply -f kubernetes/namespace.yaml

echo ""
echo "Deploying application..."
kubectl apply -f kubernetes/deployment.yaml

echo ""
echo "Creating service..."
kubectl apply -f kubernetes/service.yaml

echo ""
echo "Creating ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "=========================================="
echo "Waiting for deployment rollout..."
echo "=========================================="
kubectl rollout status deployment/modresorts -n modresorts --timeout=5m

if [ $? -ne 0 ]; then
    echo "Deployment rollout failed!"
    echo ""
    echo "Checking pod status..."
    kubectl get pods -n modresorts
    echo ""
    echo "Checking pod logs..."
    kubectl logs -n modresorts -l app=modresorts --tail=50
    exit 1
fi

echo ""
echo "=========================================="
echo "Verifying deployment..."
echo "=========================================="
kubectl get pods,svc,ingress -n modresorts

echo ""
echo "=========================================="
echo "✓ Deployment completed successfully!"
echo "=========================================="
echo ""
echo "Application Details:"
echo "  Namespace: modresorts"
echo "  Deployment: modresorts"
echo "  Service: modresorts-service"
echo "  Ingress: modresorts-ingress"
echo ""
echo "To check application status:"
echo "  kubectl get pods -n modresorts"
echo "  kubectl logs -n modresorts -l app=modresorts"
echo ""
echo "To access the application:"
echo "  kubectl port-forward -n modresorts svc/modresorts-service 8080:80"
echo "  Then visit: http://localhost:8080"
echo ""
echo "To get ingress IP address:"
echo "  kubectl get ingress modresorts-ingress -n modresorts"
echo ""
