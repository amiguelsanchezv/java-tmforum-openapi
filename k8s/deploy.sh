#!/bin/bash

# Script to deploy the application in Kubernetes
set -e

echo "🚀 Deploying TMForum OpenAPI API in Kubernetes..."

# Install metrics-server (required for HPA)
echo "📊 Installing metrics-server for HPA..."
kubectl apply -f k8s/metrics-server.yaml 2>/dev/null || echo "  (metrics-server already exists or will be installed automatically)"
sleep 5

# Create namespace
echo "📦 Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# Apply ConfigMap and Secret
echo "🔐 Applying ConfigMap and Secret..."
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy MongoDB
echo "🍃 Deploying MongoDB..."
kubectl apply -f k8s/mongodb-deployment.yaml

# Wait for MongoDB to be ready
echo "⏳ Waiting for MongoDB to be ready..."
kubectl wait --for=condition=ready pod -l app=mongodb -n tmforum-openapi --timeout=300s

# Deploy Redis
echo "📦 Deploying Redis..."
kubectl apply -f k8s/redis-deployment.yaml

# Wait for Redis to be ready
echo "⏳ Waiting for Redis to be ready..."
kubectl wait --for=condition=ready pod -l app=redis -n tmforum-openapi --timeout=300s

# Deploy API
echo "🌐 Deploying API..."
kubectl apply -f k8s/api-deployment.yaml

# Wait for API to be ready
echo "⏳ Waiting for API to be ready..."
kubectl wait --for=condition=ready pod -l app=tmforum-openapi -n tmforum-openapi --timeout=300s

# Apply Ingress (optional)
if [ "$1" == "--with-ingress" ]; then
    echo "🌍 Applying Ingress..."
    kubectl apply -f k8s/ingress.yaml
fi

echo "✅ Deployment completed!"
echo ""
echo "📊 Pod status:"
kubectl get pods -n tmforum-openapi
echo ""
echo "🔗 Services:"
kubectl get svc -n tmforum-openapi
echo ""
echo "To view logs: kubectl logs -f deployment/tmforum-openapi -n tmforum-openapi"

