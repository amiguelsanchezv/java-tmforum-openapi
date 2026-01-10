# Kubernetes Deployment

This directory contains Kubernetes manifests to deploy the Open API TMForum.

> 📖 **Complete documentation**: See the main [README.md](../README.md) for detailed information about Kubernetes, installation, configuration and deployment.

## Structure

- `namespace.yaml` - Namespace to isolate resources
- `configmap.yaml` - Application configuration
- `secret.yaml` - Secrets (JWT, passwords, etc.)
- `mongodb-deployment.yaml` - MongoDB with PersistentVolume
- `redis-deployment.yaml` - Redis with PersistentVolume
- `api-deployment.yaml` - API with HPA (Horizontal Pod Autoscaler)
- `ingress.yaml` - Ingress for external exposure (optional)
- `metrics-server.yaml` - Metrics server for HPA
- `deploy.sh` - Automated deployment script

## Quick Deployment

```bash
# Build image
docker build -t tmforum-openapi:latest .

# Load into kind (if using kind)
kind load docker-image tmforum-openapi:latest --name tmforum-openapi

# Deploy
chmod +x k8s/deploy.sh
./k8s/deploy.sh

# With Ingress
./k8s/deploy.sh --with-ingress
```

For more information, see the [Kubernetes - Orchestration](../README.md#kubernetes---orquestación) section in the main README.
