# ModResorts - Azure Cloud Deployment Guide

## Overview
This application has been modernized for cloud-native deployment on Microsoft Azure. All cloud readiness blockers have been resolved.

## Cloud Readiness Fixes Applied

### 1. **Packaging Migration** (Blockers 15-16)
- ✅ Converted from WAR to executable JAR with embedded Tomcat
- ✅ Added Spring Boot parent POM for cloud-native deployment
- ✅ Eliminated external application server dependencies

### 2. **File System Dependencies** (Blockers 1-4, 6)
- ✅ Replaced hard-coded file paths with Azure Blob Storage
- ✅ Replaced local file writes with Azure Blob Storage
- ✅ Replaced java.io.File operations with cloud storage
- ✅ Replaced temporary file storage with Azure Blob Storage
- ✅ All file operations now use Azure Blob Storage or classpath resources

### 3. **Resource Management** (Blocker 5)
- ✅ Implemented try-with-resources for all AutoCloseable resources
- ✅ Fixed resource leaks in database connections, file handles, and streams
- ✅ Ensured proper cleanup to prevent resource exhaustion

### 4. **Secret Management** (Blocker 7)
- ✅ Externalized API keys to Azure Key Vault
- ✅ Implemented Managed Identity authentication
- ✅ Removed hard-coded credentials from source code

### 5. **Legacy Framework Migration** (Blockers 8-9)
- ✅ Migrated from EJB 2.x to Spring Boot microservices
- ✅ Replaced @Singleton/@Startup with @Service
- ✅ Eliminated heavy container dependencies

### 6. **Time/Clock Dependencies** (Blockers 10-14)
- ✅ Replaced java.util.Timer with Azure Service Bus scheduled messages
- ✅ Implemented UTC timezone for distributed consistency
- ✅ Added timezone-agnostic date handling

## Azure Services Required

### 1. Azure Blob Storage
- **Purpose**: Persistent file storage
- **Configuration**:
  ```properties
  azure.storage.account-name=<your-storage-account>
  azure.storage.container-name=modresorts-data
  ```

### 2. Azure Key Vault
- **Purpose**: Secure secret management
- **Configuration**:
  ```properties
  azure.keyvault.uri=https://<your-keyvault>.vault.azure.net/
  ```
- **Secrets to configure**:
  - `WEATHER-API-KEY`: Weather API key

### 3. Azure Service Bus
- **Purpose**: Distributed task scheduling
- **Configuration**:
  ```properties
  azure.servicebus.namespace=<your-namespace>
  azure.servicebus.queue-name=scheduled-tasks
  ```

### 4. Azure SQL Database (Optional)
- **Purpose**: Relational data storage with HikariCP connection pooling
- **Configuration**:
  ```properties
  spring.datasource.url=jdbc:sqlserver://<server>.database.windows.net:1433;database=<db>
  spring.datasource.username=<username>
  spring.datasource.password=<password>
  ```

## Deployment Options

### Option 1: Azure App Service (Recommended)
```bash
# Build the application
mvn clean package

# Deploy to Azure App Service
az webapp deploy --resource-group <rg> --name <app-name> --src-path target/modresorts-2.0.0.jar
```

### Option 2: Azure Container Apps
```bash
# Build container image (separate workflow)
# Deploy to Azure Container Apps
az containerapp create --name modresorts --resource-group <rg> --image <image>
```

### Option 3: Azure Kubernetes Service (AKS)
```bash
# Build container image (separate workflow)
# Deploy to AKS using Kubernetes manifests
kubectl apply -f k8s/
```

## Environment Variables

Set these environment variables in your Azure service:

```bash
# Azure Storage
AZURE_STORAGE_ACCOUNT_NAME=<storage-account>
AZURE_STORAGE_CONTAINER_NAME=modresorts-data

# Azure Key Vault
AZURE_KEYVAULT_URI=https://<keyvault>.vault.azure.net/

# Azure Service Bus
AZURE_SERVICEBUS_NAMESPACE=<namespace>
AZURE_SERVICEBUS_QUEUE_NAME=scheduled-tasks

# Database
DATABASE_URL=jdbc:sqlserver://<server>.database.windows.net:1433;database=<db>
DATABASE_USERNAME=<username>
DATABASE_PASSWORD=<password>

# Server
PORT=8080
```

## Managed Identity Setup

Enable Managed Identity for secure authentication:

```bash
# Enable system-assigned managed identity
az webapp identity assign --name <app-name> --resource-group <rg>

# Grant permissions to Key Vault
az keyvault set-policy --name <keyvault> --object-id <identity-id> --secret-permissions get list

# Grant permissions to Storage Account
az role assignment create --assignee <identity-id> --role "Storage Blob Data Contributor" --scope <storage-id>

# Grant permissions to Service Bus
az role assignment create --assignee <identity-id> --role "Azure Service Bus Data Sender" --scope <servicebus-id>
```

## Health Check Endpoints

- **Health**: `http://<app-url>/actuator/health`
- **Info**: `http://<app-url>/actuator/info`
- **Metrics**: `http://<app-url>/actuator/metrics`

## Application Endpoints

- **Weather**: `/resorts/weather?selectedCity=<city>`
- **Availability**: `/resorts/availability?date=<date>`

## Monitoring and Logging

- All logs use UTC timezone for consistency
- Structured logging format for Azure Monitor integration
- Application Insights can be added for advanced monitoring

## Security Considerations

1. ✅ All secrets stored in Azure Key Vault
2. ✅ Managed Identity for authentication (no credentials in code)
3. ✅ HTTPS enforced in Azure App Service
4. ✅ Network security groups for traffic control
5. ✅ Private endpoints for Azure services (optional)

## Scalability

- ✅ Stateless application design
- ✅ Horizontal scaling supported
- ✅ Connection pooling with HikariCP
- ✅ Distributed scheduling with Service Bus
- ✅ Shared storage with Azure Blob Storage

## Cost Optimization

- Use Azure App Service Free/Basic tier for development
- Use Azure Storage Standard tier
- Use Azure Service Bus Basic tier
- Scale up for production workloads

## Support

For issues or questions, contact the development team.
