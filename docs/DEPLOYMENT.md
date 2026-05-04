# ModResorts - Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Local Development with Docker](#local-development-with-docker)
4. [Building and Pushing Docker Images](#building-and-pushing-docker-images)
5. [GCP GKE Deployment](#gcp-gke-deployment)
6. [Configuration Management](#configuration-management)
7. [Troubleshooting](#troubleshooting)
8. [Security Considerations](#security-considerations)
9. [Technology-Specific Notes](#technology-specific-notes)

---

## Overview

ModResorts is a Java Spring Boot 2.7.18 application packaged as a WAR file but configured to run as an executable application. This guide provides comprehensive instructions for containerizing and deploying the application to Google Kubernetes Engine (GKE).

**Application Details:**
- **Technology Stack**: Java 8, Spring Boot 2.7.18, Maven
- **Package Type**: WAR (executable)
- **Application Port**: 8080
- **Health Endpoint**: `/actuator/health`
- **Main Class**: `com.acme.modres.ModResortsApplication`

---

## Prerequisites

### Required Tools

1. **Docker Desktop** (v20.10+)
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`

2. **Google Cloud SDK** (gcloud CLI)
   - Download: https://cloud.google.com/sdk/docs/install
   - Verify: `gcloud --version`
   - Initialize: `gcloud init`

3. **kubectl** (Kubernetes CLI)
   - Install via gcloud: `gcloud components install kubectl`
   - Verify: `kubectl version --client`

4. **Git** (for version control)
   - Download: https://git-scm.com/downloads
   - Verify: `git --version`

### GCP Requirements

1. **GCP Project**
   - Create a project: https://console.cloud.google.com/projectcreate
   - Note your Project ID

2. **Enable Required APIs**
   ```bash
   gcloud services enable container.googleapis.com
   gcloud services enable artifactregistry.googleapis.com
   gcloud services enable compute.googleapis.com
   ```

3. **GKE Cluster**
   - Create a cluster via Console or CLI:
   ```bash
   gcloud container clusters create modresorts-cluster \
     --zone us-central1-a \
     --num-nodes 3 \
     --machine-type n1-standard-2 \
     --enable-autoscaling \
     --min-nodes 2 \
     --max-nodes 5
   ```

4. **Artifact Registry Repository**
   ```bash
   gcloud artifacts repositories create modresorts-repo \
     --repository-format=docker \
     --location=us-central1 \
     --description="ModResorts Docker images"
   ```

### External Services (Optional)

- **Redis/Memorystore**: For distributed caching
- **Cloud SQL**: For production database
- **Secret Manager**: For sensitive configuration

---

## Local Development with Docker

### Step 1: Build the Application Locally

```bash
# Navigate to project directory
cd /path/to/Newchecktest

# Build with Maven (optional - Docker will build)
mvn clean package -DskipTests
```

### Step 2: Run with Docker Compose

```bash
# Start the application
docker-compose up -d

# View logs
docker-compose logs -f modresorts

# Check health
curl http://localhost:8080/actuator/health

# Stop the application
docker-compose down
```

### Step 3: Configure Environment Variables

Create a `.env` file in the project root:

```env
# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Database Configuration
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=
DB_DRIVER=org.h2.Driver

# Weather API
WEATHER_API_KEY=your-api-key-here
```

Then run:
```bash
docker-compose --env-file .env up -d
```

---

## Building and Pushing Docker Images

### Option 1: Using build-push.sh (Linux/macOS)

```bash
# Make script executable
chmod +x scripts/build-push.sh

# Run the script
./scripts/build-push.sh
```

**Script Workflow:**
1. Prompts for image tag (default: `latest`)
2. Asks to select registry:
   - Google Artifact Registry (recommended for GKE)
   - Docker Hub
3. Prompts for registry credentials
4. Builds Docker image
5. Pushes to selected registry

**Example - Artifact Registry:**
```
Enter image tag: v2.0.0
Select container registry:
1. Google Artifact Registry (GCP)
2. Docker Hub
Enter choice: 1
Enter GCP Project ID: my-gcp-project
Enter GCP Region: us-central1
Enter Artifact Registry Repository Name: modresorts-repo
```

### Option 2: Using build-push.bat (Windows)

```cmd
# Run the script
scripts\build-push.bat
```

Follow the same prompts as the Linux/macOS version.

### Option 3: Manual Build and Push

```bash
# Build the image
docker build -t modresorts:v2.0.0 .

# Tag for Artifact Registry
docker tag modresorts:v2.0.0 \
  us-central1-docker.pkg.dev/my-gcp-project/modresorts-repo/modresorts:v2.0.0

# Authenticate with Artifact Registry
gcloud auth configure-docker us-central1-docker.pkg.dev

# Push the image
docker push us-central1-docker.pkg.dev/my-gcp-project/modresorts-repo/modresorts:v2.0.0
```

---

## GCP GKE Deployment

### Step 1: Prepare GKE Cluster

```bash
# Authenticate with GCP
gcloud auth login

# Set project
gcloud config set project YOUR_PROJECT_ID

# Get cluster credentials
gcloud container clusters get-credentials modresorts-cluster \
  --zone us-central1-a \
  --project YOUR_PROJECT_ID

# Verify connectivity
kubectl cluster-info
```

### Step 2: Create Kubernetes Secrets (Optional)

For sensitive data like passwords and API keys:

```bash
# Create namespace first
kubectl create namespace modresorts

# Create secrets
kubectl create secret generic modresorts-secrets \
  --from-literal=redis-password='your-redis-password' \
  --from-literal=db-username='your-db-username' \
  --from-literal=db-password='your-db-password' \
  --from-literal=weather-api-key='your-weather-api-key' \
  -n modresorts
```

### Step 3: Deploy Using Scripts

#### Linux/macOS:

```bash
# Make script executable
chmod +x scripts/deploy-image.sh

# Run deployment script
./scripts/deploy-image.sh
```

#### Windows:

```cmd
# Run deployment script
scripts\deploy-image.bat
```

**Script Workflow:**
1. Prompts for GCP Project ID, Zone, and Cluster Name
2. Prompts for Docker Image URI
3. Prompts for application configuration:
   - Redis Host and Port
   - Database URL and Driver
   - Naming Service URLs
4. Authenticates with GCP
5. Configures kubectl
6. Updates Kubernetes manifests with provided values
7. Applies manifests in order:
   - Namespace
   - Deployment
   - Service
   - Ingress
8. Waits for deployment rollout
9. Verifies deployment

### Step 4: Manual Deployment (Alternative)

```bash
# Update deployment.yaml with your image URI
sed -i 's|{{IMAGE_URI}}|us-central1-docker.pkg.dev/my-project/modresorts-repo/modresorts:v2.0.0|g' \
  kubernetes/deployment.yaml

# Update environment variables
sed -i 's|{{REDIS_HOST}}|10.0.0.3|g' kubernetes/deployment.yaml
sed -i 's|{{REDIS_PORT}}|6379|g' kubernetes/deployment.yaml
sed -i 's|{{DB_URL}}|jdbc:postgresql://10.0.0.4:5432/modresorts|g' kubernetes/deployment.yaml
sed -i 's|{{DB_DRIVER}}|org.postgresql.Driver|g' kubernetes/deployment.yaml

# Apply manifests
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/modresorts -n modresorts

# Verify deployment
kubectl get pods,svc,ingress -n modresorts
```

### Step 5: Verify Deployment

```bash
# Check pod status
kubectl get pods -n modresorts

# View pod logs
kubectl logs -n modresorts -l app=modresorts

# Check service
kubectl get svc -n modresorts

# Check ingress
kubectl get ingress -n modresorts

# Port forward for local testing
kubectl port-forward -n modresorts svc/modresorts-service 8080:80

# Test health endpoint
curl http://localhost:8080/actuator/health
```

---

## Configuration Management

### Environment Variables

The application uses the following environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SERVER_PORT` | Application port | 8080 | No |
| `REDIS_HOST` | Redis server host | localhost | Yes (for caching) |
| `REDIS_PORT` | Redis server port | 6379 | Yes (for caching) |
| `REDIS_PASSWORD` | Redis password | - | No |
| `DB_URL` | Database JDBC URL | jdbc:h2:mem:testdb | Yes |
| `DB_USERNAME` | Database username | sa | Yes |
| `DB_PASSWORD` | Database password | - | No |
| `DB_DRIVER` | JDBC driver class | org.h2.Driver | Yes |
| `WEATHER_API_KEY` | Weather API key | - | No |
| `NAMING_FACTORY_INITIAL` | JNDI factory | - | No |
| `NAMING_PROVIDER_URL` | JNDI provider URL | - | No |

### Kubernetes ConfigMaps

For non-sensitive configuration:

```bash
kubectl create configmap modresorts-config \
  --from-literal=server.port=8080 \
  --from-literal=redis.host=10.0.0.3 \
  --from-literal=redis.port=6379 \
  -n modresorts
```

Update `deployment.yaml` to use ConfigMap:

```yaml
envFrom:
- configMapRef:
    name: modresorts-config
```

### GCP Secret Manager Integration

For production, use GCP Secret Manager:

```bash
# Create secrets
echo -n "my-redis-password" | gcloud secrets create redis-password --data-file=-
echo -n "my-db-password" | gcloud secrets create db-password --data-file=-

# Grant GKE service account access
gcloud secrets add-iam-policy-binding redis-password \
  --member="serviceAccount:PROJECT_ID.svc.id.goog[modresorts/default]" \
  --role="roles/secretmanager.secretAccessor"
```

---

## Troubleshooting

### Common Issues

#### 1. Pod CrashLoopBackOff

**Symptoms:**
```bash
kubectl get pods -n modresorts
# NAME                         READY   STATUS             RESTARTS   AGE
# modresorts-xxx-yyy           0/1     CrashLoopBackOff   5          5m
```

**Solutions:**
```bash
# Check pod logs
kubectl logs -n modresorts modresorts-xxx-yyy

# Check pod events
kubectl describe pod -n modresorts modresorts-xxx-yyy

# Common causes:
# - Missing environment variables
# - Database connection failure
# - Redis connection failure
# - Insufficient memory/CPU
```

#### 2. ImagePullBackOff

**Symptoms:**
```bash
kubectl get pods -n modresorts
# NAME                         READY   STATUS             RESTARTS   AGE
# modresorts-xxx-yyy           0/1     ImagePullBackOff   0          2m
```

**Solutions:**
```bash
# Verify image exists
gcloud artifacts docker images list us-central1-docker.pkg.dev/PROJECT_ID/modresorts-repo

# Check image pull secrets
kubectl get secrets -n modresorts

# Create image pull secret if needed
kubectl create secret docker-registry artifact-registry \
  --docker-server=us-central1-docker.pkg.dev \
  --docker-username=_json_key \
  --docker-password="$(cat key.json)" \
  -n modresorts

# Update deployment to use secret
kubectl patch deployment modresorts -n modresorts \
  -p '{"spec":{"template":{"spec":{"imagePullSecrets":[{"name":"artifact-registry"}]}}}}'
```

#### 3. Service Not Accessible

**Symptoms:**
- Cannot access application via LoadBalancer IP
- Ingress not working

**Solutions:**
```bash
# Check service
kubectl get svc -n modresorts
kubectl describe svc modresorts-service -n modresorts

# Check ingress
kubectl get ingress -n modresorts
kubectl describe ingress modresorts-ingress -n modresorts

# Check ingress controller logs
kubectl logs -n kube-system -l app.kubernetes.io/name=ingress-nginx

# Verify backend health
kubectl get pods -n modresorts
kubectl exec -it -n modresorts modresorts-xxx-yyy -- curl localhost:8080/actuator/health
```

#### 4. Health Check Failures

**Symptoms:**
- Pods restarting frequently
- Readiness probe failures

**Solutions:**
```bash
# Check health endpoint manually
kubectl exec -it -n modresorts modresorts-xxx-yyy -- curl localhost:8080/actuator/health

# Increase initialDelaySeconds in deployment.yaml
# Java applications need more time to start
livenessProbe:
  initialDelaySeconds: 120  # Increase from 90
  
readinessProbe:
  initialDelaySeconds: 90   # Increase from 60
```

### Debugging Commands

```bash
# Get all resources in namespace
kubectl get all -n modresorts

# Describe deployment
kubectl describe deployment modresorts -n modresorts

# View pod logs (last 100 lines)
kubectl logs -n modresorts -l app=modresorts --tail=100

# Follow logs in real-time
kubectl logs -n modresorts -l app=modresorts -f

# Execute commands in pod
kubectl exec -it -n modresorts modresorts-xxx-yyy -- /bin/sh

# Check resource usage
kubectl top pods -n modresorts
kubectl top nodes

# View events
kubectl get events -n modresorts --sort-by='.lastTimestamp'
```

---

## Security Considerations

### 1. Use Secrets for Sensitive Data

Never hardcode passwords or API keys in manifests:

```yaml
# BAD
env:
- name: DB_PASSWORD
  value: "my-password"

# GOOD
env:
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: modresorts-secrets
      key: db-password
```

### 2. Network Policies

Restrict pod-to-pod communication:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: modresorts-netpol
  namespace: modresorts
spec:
  podSelector:
    matchLabels:
      app: modresorts
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 6379  # Redis
    - protocol: TCP
      port: 5432  # PostgreSQL
```

### 3. Pod Security Standards

Apply security context:

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
  capabilities:
    drop:
    - ALL
  readOnlyRootFilesystem: true
```

### 4. Resource Limits

Always set resource limits to prevent resource exhaustion:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "500m"
    memory: "1Gi"
```

### 5. Image Security

- Use specific image tags (not `latest`)
- Scan images for vulnerabilities:
  ```bash
  gcloud artifacts docker images scan us-central1-docker.pkg.dev/PROJECT_ID/modresorts-repo/modresorts:v2.0.0
  ```
- Use minimal base images (eclipse-temurin:8-jre-alpine)

---

## Technology-Specific Notes

### Java 8 Considerations

1. **JVM Memory Settings**
   - Container-aware JVM flags are crucial:
     ```
     -XX:+UseContainerSupport
     -XX:MaxRAMPercentage=75.0
     ```
   - Set explicit heap sizes for predictable behavior:
     ```
     -Xmx512m -Xms256m
     ```

2. **Startup Time**
   - Java applications take longer to start
   - Increase `initialDelaySeconds` for probes
   - Consider using Spring Boot's lazy initialization:
     ```properties
     spring.main.lazy-initialization=true
     ```

3. **Garbage Collection**
   - For containerized environments, use G1GC:
     ```
     -XX:+UseG1GC
     ```

### Spring Boot 2.7.18 Features

1. **Actuator Endpoints**
   - Health: `/actuator/health`
   - Info: `/actuator/info`
   - Metrics: `/actuator/metrics`
   - Configure in `application.properties`:
     ```properties
     management.endpoints.web.exposure.include=health,info,metrics
     management.endpoint.health.show-details=when-authorized
     ```

2. **Graceful Shutdown**
   - Enable in `application.properties`:
     ```properties
     server.shutdown=graceful
     spring.lifecycle.timeout-per-shutdown-phase=30s
     ```
   - Set `terminationGracePeriodSeconds` in deployment.yaml

3. **Spring Profiles**
   - Use profiles for environment-specific configuration:
     ```
     SPRING_PROFILES_ACTIVE=production
     ```

### Maven Build Optimization

1. **Dependency Caching**
   - Dockerfile copies `pom.xml` first
   - Downloads dependencies before copying source
   - Reduces build time on code changes

2. **Skip Tests in Docker**
   - Use `-DskipTests` for faster builds
   - Run tests in CI/CD pipeline separately

3. **Multi-Module Projects**
   - Build parent POM first: `mvn clean install -N`
   - Build all modules: `mvn clean package`

---

## Scaling and Performance

### Horizontal Pod Autoscaling (HPA)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: modresorts-hpa
  namespace: modresorts
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: modresorts
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

Apply:
```bash
kubectl apply -f hpa.yaml
kubectl get hpa -n modresorts
```

### Vertical Pod Autoscaling (VPA)

```bash
# Install VPA
kubectl apply -f https://github.com/kubernetes/autoscaler/releases/download/vertical-pod-autoscaler-0.13.0/vpa-v0.13.0.yaml

# Create VPA
kubectl apply -f - <<EOF
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: modresorts-vpa
  namespace: modresorts
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: modresorts
  updatePolicy:
    updateMode: "Auto"
EOF
```

---

## Monitoring and Observability

### GCP Cloud Monitoring

```bash
# Enable monitoring
gcloud services enable monitoring.googleapis.com

# View metrics in Cloud Console
# https://console.cloud.google.com/monitoring
```

### Prometheus and Grafana

```bash
# Install Prometheus Operator
kubectl create namespace monitoring
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring

# Access Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# Visit: http://localhost:3000 (admin/prom-operator)
```

### Application Logs

```bash
# View logs in Cloud Logging
gcloud logging read "resource.type=k8s_container AND resource.labels.namespace_name=modresorts" --limit 50

# Stream logs
kubectl logs -n modresorts -l app=modresorts -f --tail=100
```

---

## Rollback and Updates

### Rolling Updates

```bash
# Update image
kubectl set image deployment/modresorts modresorts=NEW_IMAGE_URI -n modresorts

# Check rollout status
kubectl rollout status deployment/modresorts -n modresorts

# View rollout history
kubectl rollout history deployment/modresorts -n modresorts
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/modresorts -n modresorts

# Rollback to specific revision
kubectl rollout undo deployment/modresorts --to-revision=2 -n modresorts

# Verify rollback
kubectl rollout status deployment/modresorts -n modresorts
```

---

## Cleanup

### Delete Deployment

```bash
# Delete all resources in namespace
kubectl delete namespace modresorts

# Or delete individual resources
kubectl delete -f kubernetes/ingress.yaml
kubectl delete -f kubernetes/service.yaml
kubectl delete -f kubernetes/deployment.yaml
kubectl delete -f kubernetes/namespace.yaml
```

### Delete GKE Cluster

```bash
gcloud container clusters delete modresorts-cluster --zone us-central1-a
```

### Delete Artifact Registry Repository

```bash
gcloud artifacts repositories delete modresorts-repo --location=us-central1
```

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)
- [GKE Documentation](https://cloud.google.com/kubernetes-engine/docs)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Documentation](https://maven.apache.org/guides/)

---

## Support

For issues or questions:
1. Check application logs: `kubectl logs -n modresorts -l app=modresorts`
2. Review pod events: `kubectl describe pod -n modresorts POD_NAME`
3. Verify configuration: `kubectl get configmap,secret -n modresorts`
4. Check GKE cluster health: `gcloud container clusters describe CLUSTER_NAME`

---

**Last Updated**: 2024
**Version**: 2.0.0
