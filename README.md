# Customers REST API with Spring Boot and MongoDB

REST API for customer management developed with Spring Boot and MongoDB, fully compliant with **TMF629 Customer Management API v5.0.1** specification. The API implements the standard TMForum Open API structure with PartyRef, ContactMedium, and proper TMF629 endpoints. Fully containerized with Docker and orchestrated with Kubernetes. But the model in database simulate a existing model to use mapper transformation.

## 📋 Table of Contents

1. [Features](#features)
2. [Prerequisites](#prerequisites)
3. [Configuration](#configuration)
4. [Execution](#execution)
   - [Docker Compose (Recommended)](#option-1-docker-compose-recommended)
   - [Local Execution (Without Docker)](#option-2-local-execution-without-docker)
   - [Kubernetes (Orchestration)](#option-3-kubernetes-orchestration)
5. [API Documentation](#api-documentation)
6. [Docker - Containerization](#docker---containerization)
7. [Kubernetes - Orchestration](#kubernetes---orchestration)
8. [Testing](#testing)
9. [Project Structure](#project-structure)
10. [Main Features](#main-features)
11. [Technologies Used](#technologies-used)

## Features

### CRUD Functionality
- ✅ Create customer (TMF629 createCustomer)
- ✅ Get customer by ID (TMF629 retrieveCustomer)
- ✅ List customers (TMF629 listCustomer with offset/limit pagination)
- ✅ Update customer fully (PUT)
- ✅ Update customer partially (PATCH - TMF629 patchCustomer)
- ✅ Delete customer (TMF629 deleteCustomer)
- ✅ TMF629 compliant data structure (PartyRef, ContactMedium, etc.)

### Security and Authentication
- ✅ Authentication and authorization with Spring Security and JWT
- ✅ Scope-based access control (customers:read, customers:write, customers:delete, customers:admin)
- ✅ Rate limiting per IP (configurable)

### Quality and Documentation
- ✅ Data validation (including custom validations)
- ✅ Global exception handling
- ✅ Automatic documentation with Swagger/OpenAPI
- ✅ Unit and integration tests

### Infrastructure and Deployment
- ✅ Containerization with Docker
- ✅ Orchestration with Kubernetes
- ✅ Auto-scaling with HPA
- ✅ Health checks and auto-recovery
- ✅ Data persistence

### Advanced Features
For more details on the following features, see the [Main Features](#main-features) section:
- **TMF629 Compliance**: Full implementation of TMForum Customer Management API v5.0.1
- Advanced logging with Logback
- Caching with Redis (optional)
- Custom validations
- API versioning (TMF629, v1)
- CORS configured
- Automatic date generation
- TMF629 data structures (PartyRef, ContactMedium, TimePeriod)

## Prerequisites

### For Local Execution
- Java JDK 25 or higher (Java 25 LTS recommended)
- Maven 3.6+
- MongoDB 4.4+ (local or MongoDB Atlas)
- Redis (optional, only if caching is enabled)

### For Docker
- Docker 20.10+
- Docker Compose 2.0+

### For Kubernetes
- Docker installed and running
- kubectl installed
- kind or minikube (for local development)
- WSL2 (if you're on Windows)

## Configuration

### 1. Install MongoDB

**Local MongoDB**
- Download and install MongoDB from [mongodb.com](https://www.mongodb.com/try/download/community)
- Make sure the MongoDB service is running on port 27017

### 2. Configure Database

Edit `src/main/resources/application.properties`:

**For Local MongoDB:**
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=openapidb
```

**For MongoDB Atlas:**
```properties
spring.data.mongodb.uri=mongodb+srv://user:password@cluster.mongodb.net/openapidb?retryWrites=true&w=majority
```

### 3. Configure JWT (Optional)

The API uses JWT authentication. You can configure the secret key in `application.properties`:

```properties
# Secret key for signing JWT tokens (minimum 256 bits)
jwt.secret=MyVerySecureSecretKeyForJWTThatMustBeVeryLongForHS512AtLeast256Bits
# Expiration time in seconds (3600 = 1 hour)
jwt.expiration=3600
```

### 4. Configure Redis (Optional)

To enable caching with Redis:

```properties
# Enable Redis
redis.enabled=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Note:** If Redis is not available, the application will work without caching.

### 5. Configure Rate Limiting (Optional)

Rate limiting is enabled by default. You can configure it:

```properties
# Enable/disable rate limiting
rate.limit.enabled=true
# Number of requests allowed per minute per IP
rate.limit.requests-per-minute=100
```

## Available Scopes

The API uses the following scopes to control access:

- **`customers:read`** - Allows reading/listing customers (GET)
- **`customers:write`** - Allows creating and updating customers (POST, PUT)
- **`customers:delete`** - Allows deleting customers (DELETE)
- **`customers:admin`** - Full access to all operations

## Execution

### Option 1: Docker Compose (Recommended)

The easiest way to run the application is using Docker Compose, which automatically starts all necessary services (MongoDB, Redis, and the API).

#### Prerequisites
- Docker 20.10+ installed
- Docker Compose 2.0+

#### Steps to Run

```bash
# 1. Build and start all services
docker-compose up -d

# 2. View application logs
docker-compose logs -f api

# 3. Verify all services are running
docker-compose ps
```

**Included services:**
- **MongoDB**: Database on port 27017
- **Redis**: Cache on port 6379
- **API**: Spring Boot application on port 8080

The application will be available at: `http://localhost:8080/api`

#### Useful Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (deletes data)
docker-compose down -v

# Rebuild API image
docker-compose build api
docker-compose up -d api

# View logs for a specific service
docker-compose logs -f mongodb
docker-compose logs -f redis
```

**Note:** MongoDB and Redis data are persisted in `./data/mongodb` and `./data/redis` respectively.

#### Using Makefile (Alternative)

You can also use the Makefile commands for easier management:

```bash
# Start all services
make docker-run

# View logs
make docker-logs

# Stop services
make docker-stop

# View data status
make data-status

# Create backups
make backup
```

### Option 2: Local Execution (Without Docker)

#### Compile and run

```bash
mvn clean install
mvn spring-boot:run
```

**Requirements:**
- MongoDB running on `localhost:27017`
- Redis running on `localhost:6379` (optional)

The application will be available at: `http://localhost:8080/api`

### Option 3: Kubernetes (Orchestration)

To deploy on Kubernetes, see the [Kubernetes - Orchestration](#kubernetes---orchestration) section below.

## API Documentation

### Swagger/OpenAPI

Once the application is running, you can access the interactive API documentation:

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/api/api-docs`

In Swagger UI you can:
- View all available endpoints
- Test endpoints directly from the browser
- View data models (Customer)
- View request and response samples

![Swagger UI](public/img/swagger-ui.png)

### Available Endpoints

#### Authentication Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| POST | `/api/auth/login` | Generate JWT token using clientId and clientSecret | No |
| GET | `/api/auth/validate` | Validate if a JWT token is valid | Yes (Bearer token) |

#### Health Check Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/api/actuator/health` | Application health check (used by Kubernetes) | No |

#### TMF629 Customer Management API (Current version - TMF629 compliant)

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/api/customer` | List or find Customer objects (TMF629 listCustomer) | Yes |
| POST | `/api/customer` | Creates a Customer (TMF629 createCustomer) | Yes |
| GET | `/api/customer/{id}` | Retrieves a Customer by ID (TMF629 retrieveCustomer) | Yes |
| PATCH | `/api/customer/{id}` | Updates partially a Customer (TMF629 patchCustomer) | Yes |
| DELETE | `/api/customer/{id}` | Deletes a Customer (TMF629 deleteCustomer) | Yes |

**Query Parameters (TMF629 standard):**
- `offset`: Requested index for start of resources (default: 0)
- `limit`: Requested number of resources (default: 10)
- `fields`: Comma-separated list of fields to include in response (optional)
- `sort`: Field to sort by (default: creationDate)
- `direction`: Sort direction - ASC or DESC (default: DESC)

#### API v1 (Legacy version - maintained for compatibility)

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/api/v1/customers` | Get all customers (paginated) | Yes |
| GET | `/api/v1/customers/{id}` | Get a customer by ID | Yes |
| POST | `/api/v1/customers` | Create a new customer | Yes |
| PUT | `/api/v1/customers/{id}` | Update a customer | Yes |
| DELETE | `/api/v1/customers/{id}` | Delete a customer | Yes |
| GET | `/api/v1/customers/search?name=xxx` | Search customers by first name or last name (paginated) | Yes |

### Usage Samples

**⚠️ IMPORTANT:** All endpoints require JWT authentication. You must include a valid JWT token in the `Authorization` header.

#### 1. Get Access Token

```bash
# Get JWT token using clientId and clientSecret
# Test applications are created automatically when the application starts
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "app-readwrite",
    "clientSecret": "secret-readwrite"
  }' | jq -r '.token')
```

**Available test applications:**
- `app-readonly` / `secret-readonly` - Read-only
- `app-readwrite` / `secret-readwrite` - Read and write
- `app-admin` / `secret-admin` - Full access

#### 2. Validate Token

```bash
# Validate if the token is still valid
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "valid": true
}
```

#### 3. Create a customer (TMF629)

**Note:** The API follows TMF629 Customer Management specification. The request uses `firstName` and `lastName` separately, and the response includes a concatenated `name` field.

**Validations:**
- `firstName` and `lastName` are required
- `engagedParty` is required
- Email must be unique (extracted from `contactMedium`)
- Phone and mobile must have valid format (7-15 digits)

```bash
curl -X POST http://localhost:8080/api/customer \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "@type": "Customer",
    "firstName": "John",
    "lastName": "Doe",
    "engagedParty": {
      "@type": "PartyRef",
      "name": "John Doe",
      "@referredType": "Individual"
    },
    "contactMedium": [
      {
        "@type": "EmailContactMedium",
        "emailAddress": "john.doe@example.com",
        "phoneNumber": "1234567890",
        "mobileNumber": "0987654321",
        "street1": "Main Street 123",
        "city": "New York",
        "postCode": "10001"
      }
    ],
    "status": "Active"
  }'
```

**Response (TMF629 format with automatically generated dates):**
```json
{
  "@type": "Customer",
  "@baseType": "PartyRole",
  "id": "...",
  "href": "/api/customer/...",
  "name": "John Doe",
  "engagedParty": {
    "@type": "PartyRef",
    "id": "...",
    "href": "/api/party/...",
    "name": "John Doe",
    "@referredType": "Individual"
  },
  "contactMedium": [
    {
      "@type": "EmailContactMedium",
      "emailAddress": "john.doe@example.com",
      "phoneNumber": "1234567890",
      "mobileNumber": "0987654321",
      "street1": "Main Street 123"
    }
  ],
  "status": "Active",
  "creationDate": "2024-12-31T12:00:00",
  "updateDate": "2024-12-31T12:00:00"
}
```

#### 4. List customers (TMF629 - with offset/limit)

```bash
# Offset 0, limit 10, sorted by creationDate descending (default)
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/customer?offset=0&limit=10&sort=creationDate&direction=DESC"

# Offset 20, limit 20, sorted by name ascending
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/customer?offset=20&limit=20&sort=name&direction=ASC"

# With fields selection
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/customer?fields=id,name,status&offset=0&limit=10"
```

**TMF629 Pagination parameters:**
- `offset`: Requested index for start of resources (default: 0)
- `limit`: Requested number of resources (default: 10)
- `fields`: Comma-separated list of fields to include (optional)
- `sort`: Field to sort by (default: creationDate)
- `direction`: Sort direction - ASC or DESC (default: DESC)

**Paginated response:**
```json
{
  "content": [
    {
      "@type": "Customer",
      "id": "...",
      "name": "John Doe",
      ...
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 50,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

#### 5. Retrieve customer by ID (TMF629)

```bash
# Get customer with all fields
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/customer/{id}"

# Get customer with specific fields only
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/customer/{id}?fields=id,name,status,contactMedium"
```

#### 6. Update customer partially (PATCH - TMF629)

```bash
curl -X PATCH http://localhost:8080/api/customer/{id} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "@type": "Customer",
    "firstName": "John",
    "lastName": "Smith",
    "contactMedium": [
      {
        "@type": "EmailContactMedium",
        "emailAddress": "john.smith@example.com"
      }
    ]
  }'
```

**Note:** PATCH only updates the fields provided. Fields not included in the request remain unchanged.

## Docker - Containerization

### Dockerfile (Multi-stage Build)

The project uses a multi-stage Dockerfile to optimize image size:

```dockerfile
# STAGE 1: Build (Compilation)
FROM maven:3.9-eclipse-temurin-25 AS build
# ↓ Download dependencies (cached)
# ↓ Compile code
# ↓ Generate JAR

# STAGE 2: Runtime (Execution)
FROM eclipse-temurin:25-jre-alpine
# ↓ Copy only compiled JAR
# ↓ Final image smaller (~150MB vs ~500MB)
```

**Multi-stage advantages:**
- Smaller final image (JRE only, not Maven)
- More secure (less attack surface)
- Faster build (dependency caching)

### docker-compose.yml

The `docker-compose.yml` file defines three services:

```yaml
services:
  mongodb:    # Database
  redis:      # Cache
  api:        # Spring Boot application
```

**How does it work?**
1. Creates a virtual network (`api-network`)
2. Each service has a DNS name (mongodb, redis, api)
3. Services communicate by name
4. Health checks ensure dependencies are ready

**Communication samples:**
```java
// In application-docker.properties
spring.data.mongodb.host=mongodb  // ← Service name
spring.data.redis.host=redis      // ← Service name
```

### Build Docker Image

```bash
# Build image
docker build -t tmforum-openapi:latest .

# Verify image
docker images | grep tmforum-openapi
```

## Kubernetes - Orchestration

- Manages multiple containers
- Distributes load among replicas
- Scales automatically
- Handles failures (automatic restart)
- Manages persistent storage

### General Architecture

```
                      ┌─────────────────────────────────────────────────────────────┐
                      │                    Kubernetes Cluster                       │
                      │                                                             │
                      │  ┌──────────────────────────────────────────────────────┐   │
                      │  │              Namespace: tmforum-openapi              │   │
                      │  │                                                      │   │
                      │  │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐   │   │
                      │  │  │   MongoDB    │  │    Redis     │  │    API    │   │   │
                      │  │  │  (Stateful)  │  │  (Stateful)  │  │(Stateless)│   │   │
                      │  │  │              │  │              │  │           │   │   │
                      │  │  │  PVC: 5GB    │  │  PVC: 1GB    │  │  HPA:     │   │   │
                      │  │  │  Port: 27017 │  │  Port: 6379  │  │  3-10 pods│   │   │
                      │  │  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘   │   │
                      │  │         │                 │                │         │   │
                      │  │         └─────────────────┴────────────────┘         │   │
                      │  │                           │                          │   │
                      │  │             ┌─────────────▼──────────────┐           │   │
                      │  │             │   ConfigMap + Secrets      │           │   │
                      │  │             │   (Configuration)          │           │   │
                      │  │             └────────────────────────────┘           │   │
                      │  └──────────────────────────────────────────────────────┘   │
                      │                                                             │
                      │  ┌──────────────────────────────────────────────────────┐   │
                      │  │              Ingress Controller                      │   │
                      │  │            (nginx / traefik / etc)                   │   │
                      │  │                                                      │   │
                      │  │      api.openapi.com ──► tmforum-openapi-service     │   │
                      │  └──────────────────────────────────────────────────────┘   │
                      └─────────────────────────────────────────────────────────────┘
```

### Installation and Configuration

#### Prerequisites

Before starting, make sure you have:
- ✅ Docker installed and running
- ✅ WSL2 (if you're on Windows)
- ✅ Internet access to download images

#### kubectl Installation

**Direct download (Recommended)**
```bash
# Download kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Make executable
chmod +x kubectl

# Install
sudo mv kubectl /usr/local/bin/

# Verify
kubectl version --client
```

#### kind Installation

```bash
# Download kind
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64

# Make executable
chmod +x ./kind

# Install
sudo mv ./kind /usr/local/bin/

# Verify
kind version
```

#### Create Kubernetes Cluster with kind

**Basic Configuration:**
```bash
# Create simple cluster
kind create cluster --name tmforum-openapi
```

#### Install Ingress Controller

```bash
# Apply nginx ingress manifest
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

# Wait for it to be ready
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

### Kubernetes Components

#### 1. Namespace (`namespace.yaml`)

Isolates resources (like folders in a file system).

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: tmforum-openapi
```

#### 2. ConfigMap (`configmap.yaml`)

Stores **non-sensitive** configuration (ports, hosts, etc.).

```yaml
apiVersion: v1
kind: ConfigMap
data:
  server.port: "8080"
  spring.data.mongodb.host: "mongodb-service"
```

#### 3. Secret (`secret.yaml`)

Stores **sensitive** information (passwords, tokens, keys).

```yaml
apiVersion: v1
kind: Secret
stringData:
  jwt.secret: "secret-key"
```

**Difference from ConfigMap:**
- Secrets are encrypted in etcd
- Not shown in logs
- Can be rotated easily

#### 4. Deployment (`api-deployment.yaml`)

Creates and manages **pods** (containers).

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  replicas: 3  # ← 3 copies of the application
  template:
    spec:
      containers:
      - name: tmforum-openapi
        image: tmforum-openapi:latest
```

**What does it do?**
- Maintains the specified number of replicas
- If a pod fails, automatically creates a new one
- Allows updates without downtime (rolling update)

#### 5. Service (`api-deployment.yaml`)

Exposes pods with a **stable DNS name**.

```yaml
apiVersion: v1
kind: Service
spec:
  selector:
    app: tmforum-openapi  # ← Selects pods with this label
  ports:
  - port: 80
    targetPort: 8080
```

**Why is it necessary?**
- Pods have dynamic IPs (change on restart)
- Service has a fixed IP
- Other services connect by name: `tmforum-openapi-service`

**Service types:**
- `ClusterIP`: Only accessible within the cluster (default)
- `NodePort`: Exposes on a node port
- `LoadBalancer`: Creates an external load balancer
- `Ingress`: For HTTP/HTTPS with routing

#### 6. PersistentVolumeClaim (PVC)

Requests **persistent** storage.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
spec:
  resources:
    requests:
      storage: 5Gi
```

**Why?**
- Containers are ephemeral (data is lost on restart)
- MongoDB and Redis need to persist data
- PVC creates a volume that survives restarts

#### 7. HorizontalPodAutoscaler (HPA)

Automatically scales based on CPU/Memory usage.

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        averageUtilization: 70
```

**Example:**
```
CPU < 70% → Maintains 3 pods
CPU > 70% → Creates more pods (up to 10)
CPU drops → Removes pods (minimum 3)
```

### Complete Deployment Flow with kind

#### Step 1: Build Docker Image

```bash
# From project directory
cd /mnt/e/openapi

# Build image
docker build -t tmforum-openapi:latest .

# Verify image
docker images | grep tmforum-openapi
```

#### Step 2: Load Image into kind

**IMPORTANT:** kind cannot access Docker host images directly. You need to load them:

```bash
# Load image into kind cluster
kind load docker-image tmforum-openapi:latest --name tmforum-openapi

# Verify it was loaded
docker exec tmforum-openapi-control-plane crictl images | grep tmforum-openapi
```

#### Step 3: Deploy to Kubernetes

```bash
# 1. Create namespace
kubectl apply -f k8s/namespace.yaml

# 2. Apply configuration
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# 3. Install metrics-server (required for HPA)
kubectl apply -f k8s/metrics-server.yaml

# 4. Deploy MongoDB
kubectl apply -f k8s/mongodb-deployment.yaml
kubectl wait --for=condition=ready pod -l app=mongodb -n tmforum-openapi --timeout=300s

# 5. Deploy Redis
kubectl apply -f k8s/redis-deployment.yaml
kubectl wait --for=condition=ready pod -l app=redis -n tmforum-openapi --timeout=300s

# 6. Deploy API
kubectl apply -f k8s/api-deployment.yaml

# 7. (Optional) Apply Ingress
kubectl apply -f k8s/ingress.yaml
```

#### Step 4: Verify Deployment

```bash
# View pods
kubectl get pods -n tmforum-openapi

# View services
kubectl get svc -n tmforum-openapi

# View HPA
kubectl get hpa -n tmforum-openapi

# View logs
kubectl logs -f deployment/tmforum-openapi -n tmforum-openapi
```

**Expected output:**
```
NAME                            READY   STATUS    RESTARTS   AGE
tmforum-openapi-xxxxx-1         1/1     Running   0          2m
tmforum-openapi-xxxxx-2         1/1     Running   0          2m
tmforum-openapi-xxxxx-3         1/1     Running   0          2m
mongodb-xxxxx                   1/1     Running   0          5m
redis-xxxxx                     1/1     Running   0          4m
```

#### Step 5: Access the API

**Option 1: Port Forward (Development)**

```bash
# Port forward to service
kubectl port-forward svc/tmforum-openapi-service 8080:80 -n tmforum-openapi
```

Then access: `http://localhost:8080/api`

**Option 2: Ingress**

If you configured Ingress, access through the configured domain:
- `https://api.openapi.com/api`

**Note:** In kind, Ingress is available on `localhost` (ports 80/443)

### Health Checks

Kubernetes uses three types of health checks:

#### 1. Liveness Probe (Is it alive?)

Detects if the container is "dead" (crash, deadlock).

**Action:** If it fails → Kubernetes **kills and recreates** the pod

```yaml
livenessProbe:
  httpGet:
    path: /api/actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
```

#### 2. Readiness Probe (Is it ready?)

Detects if the container can receive traffic.

**Action:** If it fails → Kubernetes **removes pod from Service** (doesn't receive traffic)

```yaml
readinessProbe:
  httpGet:
    path: /api/actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

#### 3. Startup Probe (Has it started?)

Gives extra time for applications that take time to start.

**Action:** While starting, ignores liveness/readiness

```yaml
startupProbe:
  httpGet:
    path: /api/actuator/health
    port: 8080
  failureThreshold: 30  # 30 * 10s = 5 minutes maximum
```

### Automatic Scaling (HPA)

HPA monitors CPU and Memory and scales automatically:

**Scenario 1: Normal Traffic**
```
3 pods × 30% CPU = OK
→ HPA maintains 3 pods
```

**Scenario 2: Traffic Peak**
```
3 pods × 85% CPU = HIGH
→ HPA creates 2 more pods
→ 5 pods × 50% CPU = OK
```

**Scenario 3: Low Traffic**
```
5 pods × 40% CPU = LOW
→ HPA removes 1 pod
→ 4 pods × 50% CPU = OK
→ Wait 5 minutes
→ 4 pods × 45% CPU = LOW
→ HPA removes 1 more pod
→ 3 pods (minimum)
```

### Data Persistence

**Without PVC:**
```
MongoDB Pod restarts
  ↓
Data is lost ❌
```

**With PVC:**
```
MongoDB Pod restarts
  ↓
PVC keeps data on disk
  ↓
New pod mounts same volume
  ↓
Data preserved ✅
```

**Flow:**
```
MongoDB Pod
  ↓
PVC (mongodb-pvc)
  ↓
PV (PersistentVolume)
  ↓
StorageClass
  ↓
Physical Disk / Cloud Storage
```

### Networks and Communication

Kubernetes creates an **internal DNS** for services:

```
Service Name → Internal IP
─────────────────────────
mongodb-service → 10.96.1.10
redis-service   → 10.96.1.11
tmforum-openapi-service → 10.96.1.12
```

**Usage in application:**
```properties
# In ConfigMap
spring.data.mongodb.host=mongodb-service
spring.data.redis.host=redis-service
```

**External Request Flow:**
```
User
  ↓
https://api.openapi.com/api/customers
  ↓
Ingress Controller (nginx)
  ↓
tmforum-openapi-service (ClusterIP)
  ↓
Internal Load Balancer
  ↓
Pod 1, Pod 2, or Pod 3 (random)
  ↓
Spring Boot Application
  ↓
MongoDB (mongodb-service)
Redis (redis-service)
```

### Useful Kubernetes Commands

#### View Status
```bash
# Pods
kubectl get pods -n tmforum-openapi

# Services
kubectl get svc -n tmforum-openapi

# Deployments
kubectl get deployments -n tmforum-openapi

# HPA
kubectl get hpa -n tmforum-openapi

# Everything together
kubectl get all -n tmforum-openapi
```

#### Debugging
```bash
# Describe a pod
kubectl describe pod <pod-name> -n tmforum-openapi

# Execute shell in a pod
kubectl exec -it <pod-name> -n tmforum-openapi -- /bin/sh

# View events
kubectl get events -n tmforum-openapi --sort-by='.lastTimestamp'

# View logs
kubectl logs -f deployment/tmforum-openapi -n tmforum-openapi
```

#### Manual Scaling
```bash
# Scale to 5 replicas
kubectl scale deployment tmforum-openapi --replicas=5 -n tmforum-openapi

# View rollout
kubectl rollout status deployment/tmforum-openapi -n tmforum-openapi

# Rollback
kubectl rollout undo deployment/tmforum-openapi -n tmforum-openapi
```

#### Update Application
```bash
# 1. Build new image
docker build -t tmforum-openapi:latest .

# 2. Load into kind
kind load docker-image tmforum-openapi:latest --name tmforum-openapi

# 3. Update deployment
kubectl set image deployment/tmforum-openapi tmforum-openapi=tmforum-openapi:latest -n tmforum-openapi

# 4. Restart
kubectl rollout restart deployment/tmforum-openapi -n tmforum-openapi

# 5. View rollout
kubectl rollout status deployment/tmforum-openapi -n tmforum-openapi
```

#### Clean Up Resources
```bash
# Delete entire namespace (includes all resources)
kubectl delete namespace tmforum-openapi

# Delete kind cluster
kind delete cluster --name tmforum-openapi
```

### Makefile Commands

The project includes a `Makefile` with convenient commands for common operations:

#### Build Commands
```bash
# Compile the application (skip tests)
make build

# Build Docker image
make docker-build
```

#### Docker Commands
```bash
# Run with docker-compose (start all services)
make docker-run

# Stop docker-compose
make docker-stop

# View logs of docker-compose
make docker-logs

# Start development environment
make dev
```

#### Kubernetes Commands
```bash
# Deploy to Kubernetes
make k8s-deploy

# Deploy with Ingress
make k8s-deploy-ingress

# View status (pods, services, HPA)
make k8s-status

# View logs
make k8s-logs

# Port forward to the API
make k8s-port-forward

# Delete Kubernetes resources
make k8s-delete
```

#### Backup and Restore Commands
```bash
# Create backups of MongoDB and Redis
make backup

# Restore MongoDB from backup
make restore-mongo FILE=backups/mongodb-20240101-120000.gz

# View data status (disk usage, containers)
make data-status
```

#### Utility Commands
```bash
# Clean everything (Maven + Docker volumes)
make clean

# Show all available commands
make help
```

**Note:** On Windows, you may need to use WSL2 or Git Bash to run Makefile commands. Alternatively, you can run the equivalent Docker/Kubernetes commands directly.

## Testing

### Run Tests

```bash
# Run all tests (unit + integration if Docker is available)
mvn test

# Run only unit tests (don't require Docker)
mvn test -Dtest=*Test

# Run integration tests with Docker/Testcontainers
# Requires Docker available or DOCKER_AVAILABLE=true environment variable
mvn test -Dtest=*IntegrationTest -DDOCKER_AVAILABLE=true

# Run with coverage (requires jacoco plugin)
mvn clean test jacoco:report
```

**Notes:**
- Unit tests don't require Docker or MongoDB (use `test` profile)
- Integration tests with Testcontainers (`*IntegrationTest`) require Docker
- The `test` profile disables `DataInitializer` and MongoDB/Redis connections
- Integration tests without Docker require MongoDB running on `localhost:27017`
- Code coverage reports are generated in `target/site/jacoco/index.html` after running tests with jacoco
- Minimum code coverage requirement: 80% (configured in `pom.xml`)

## Project Structure

```
tmforum-openapi/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tmforum/
│   │   │           └── openapi/
│   │   │               ├── ApiTmForumOpenApiApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── CustomerController.java
│   │   │               │   ├── AuthController.java
│   │   │               │   └── v1/
│   │   │               │       └── CustomerV1Controller.java
│   │   │               ├── model/
│   │   │               │   ├── Customer.java
│   │   │               │   └── Application.java
│   │   │               ├── repository/
│   │   │               │   ├── CustomerRepository.java
│   │   │               │   └── ApplicationRepository.java
│   │   │               ├── service/
│   │   │               │   ├── CustomerService.java
│   │   │               │   └── AuthService.java
│   │   │               ├── dto/
│   │   │               │   ├── CustomerRequest.java
│   │   │               │   ├── CustomerResponse.java
│   │   │               │   ├── LoginRequest.java
│   │   │               │   ├── LoginResponse.java
│   │   │               │   └── PageResponse.java
│   │   │               ├── mapper/
│   │   │               │   └── CustomerMapper.java
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── MongoConfig.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   └── ...
│   │   │               ├── filter/
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── RateLimitingFilter.java
│   │   │               ├── util/
│   │   │               │   └── JwtUtil.java
│   │   │               └── exception/
│   │   │                   └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-docker.properties
│   │       ├── application-kubernetes.properties
│   │       └── logback-spring.xml
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── tmforum/
│       │           └── openapi/
│       │               ├── service/
│       │               │   ├── CustomerServiceTest.java
│       │               │   └── AuthServiceTest.java
│       │               ├── util/
│       │               │   └── JwtUtilTest.java
│       │               └── integration/
│       │                   ├── CustomerControllerIntegrationTest.java
│       │                   └── AuthControllerIntegrationTest.java
│       └── resources/
│           └── application-test.properties
├── k8s/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── mongodb-deployment.yaml
│   ├── redis-deployment.yaml
│   ├── api-deployment.yaml
│   ├── ingress.yaml
│   ├── metrics-server.yaml
│   └── deploy.sh
├── scripts/
│   ├── init-docker.sh
│   ├── init-docker.ps1
│   └── install-k8s-kind.sh
├── Dockerfile
├── docker-compose.yml
├── Makefile
└── pom.xml
```

## Main Features

### TMF629 Customer Management API Compliance

The API implements a sample of the **TMForum Customer Management API v5.0.1** specification:

- **Endpoints**: Follow TMF629 standard (`/api/customer`)
- **Pagination**: Uses `offset`/`limit` (TMF629 standard) instead of `page`/`size`
- **Data Structure**: Implements TMF629 schemas:
  - `Customer` (response) with `@type`, `@baseType`, `@schemaLocation`
  - `Customer_FVO` (create request) with `firstName`, `lastName`, `engagedParty`, `contactMedium`
  - `Customer_MVO` (patch request) for partial updates
  - `PartyRef` for engaged party reference
  - `ContactMedium` for contact information (email, phone, address)
  - `TimePeriod` for validity periods
- **Name Handling**: 
  - Request receives `firstName` and `lastName` separately
  - Response concatenates them into `name` field (TMF629 standard)
- **PATCH Support**: Implements partial updates according to TMF629 specification

### DTOs and Mappers

The project uses DTOs (Data Transfer Objects) following TMF629 specification:

- **`CustomerRequest`** (Customer_FVO): DTO for creating customers with `firstName`, `lastName`, `engagedParty`, and `contactMedium`
- **`CustomerPatchRequest`** (Customer_MVO): DTO for partial updates (PATCH)
- **`CustomerResponse`** (Customer): DTO for responses with TMF629 structure (`@type`, `name` concatenated from firstName+lastName, etc.)
- **`CustomerMapper`**: Converts between TMF629 DTOs and MongoDB entities, handles name concatenation

### Automatic Date Generation

The system automatically generates creation and update dates using Spring Data MongoDB Auditing:

- **`creationDate`**: Automatically set when creating a new customer (using `@CreatedDate`)
- **`updateDate`**: Automatically updated whenever a customer is modified (using `@LastModifiedDate`)

**It's not necessary** to include these dates in creation or update requests. The system manages them automatically.

### Advanced Logging

The system includes structured logging with Logback:

- **Console logs**: Structured format with timestamp, level, logger and message
- **File logs**: Automatic rotation by size (10MB) and time (30 days of history)
- **Error logs**: Separate file for errors with 90 days retention
- **Automatic logging**: AOP aspects for automatic logging of service and controller methods
- **Configurable levels**: Different levels per package (DEBUG for app, INFO for Spring)

### CORS Configured

Detailed CORS configuration for development and production:

- **Allowed origins**: Configurable (localhost for dev, specific domain for production)
- **Allowed methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Allowed headers**: Authorization, Content-Type, and other standard headers
- **Credentials**: Support for cookies and authentication headers
- **Preflight caching**: 1 hour cache for OPTIONS requests

### Caching with Redis (Optional)

Configurable caching system using Redis:

- **Individual customer cache**: 5 minute TTL
- **List cache**: 2 minute TTL
- **Automatic invalidation**: Cache is automatically cleared when creating/updating/deleting customers
- **Flexible configuration**: Can be disabled if Redis is not available

### Custom Validations

Custom validators for business rules:

- **`@ValidPhoneNumber`**: Validates phone number format (7-15 numeric digits)
- **Unique email validation**: Handled in the service to allow updates of the same customer

### API Versioning

Support for multiple API versions:

- **TMF629** (`/api/customer`): Current version - fully compliant with TMForum Customer Management API v5.0.1
- **v1** (`/api/v1/customers`): Legacy version maintained for compatibility

The TMF629 version uses the standard TMForum structure with PartyRef, ContactMedium, and proper TMF629 endpoints. Legacy versions maintain the previous structure for backward compatibility.

### Rate Limiting

Protection against abuse with rate limiting per IP:

- **Configurable limit**: Default 100 requests per minute per IP
- **IP detection**: Supports proxies and load balancers (X-Forwarded-For, X-Real-IP)
- **HTTP 429 response**: When limit is exceeded
- **Configurable**: Can be enabled/disabled and limit adjusted from `application.properties`

## Technologies Used

- Java 25 (LTS)
- Spring Boot 3.5.9
- Spring Data MongoDB (with MongoDB Auditing)
- Spring Security
- JWT (JJWT)
- Lombok
- Jakarta Validation
- SpringDoc OpenAPI (Swagger)
- Spring AOP (for automatic logging)
- Spring Cache with Redis (optional)
- Bucket4j (for rate limiting)
- Logback (advanced logging)
- Maven
- Testcontainers (for integration tests)
- JUnit 5
- Mockito
- Docker
- Kubernetes
- kind (for local development with Kubernetes)
- **TMF629 Customer Management API v5.0.1** (TMForum specification compliance)