.PHONY: build docker-build docker-run docker-stop k8s-deploy k8s-delete k8s-logs k8s-status

# Variables
IMAGE_NAME = tmforum-openapi
IMAGE_TAG = latest
NAMESPACE = tmforum-openapi

# Docker commands
build:
	mvn clean package -DskipTests

docker-build:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .

docker-run:
	docker-compose up -d

docker-stop:
	docker-compose down

docker-logs:
	docker-compose logs -f api

# Kubernetes commands
k8s-deploy:
	@echo "🚀 Deploying to Kubernetes..."
	@./k8s/deploy.sh

k8s-deploy-ingress:
	@echo "🚀 Deploying to Kubernetes with Ingress..."
	@./k8s/deploy.sh --with-ingress

k8s-delete:
	@echo "🗑️  Deleting Kubernetes resources..."
	kubectl delete namespace $(NAMESPACE)

k8s-logs:
	kubectl logs -f deployment/$(IMAGE_NAME) -n $(NAMESPACE)

k8s-status:
	@echo "📊 Status of pods:"
	@kubectl get pods -n $(NAMESPACE)
	@echo ""
	@echo "🔗 Services:"
	@kubectl get svc -n $(NAMESPACE)
	@echo ""
	@echo "📈 HPA (Horizontal Pod Autoscaler):"
	@kubectl get hpa -n $(NAMESPACE)

k8s-port-forward:
	kubectl port-forward svc/$(IMAGE_NAME)-service 8080:80 -n $(NAMESPACE)

# Development
dev: docker-run
	@echo "✅ Services started. API available at http://localhost:8080/api"

# Clean
clean:
	mvn clean
	docker-compose down -v

# Backup
backup:
	@echo "📦 Creating backups..."
	@mkdir -p backups
	@docker-compose exec -T mongodb mongodump --archive --gzip --db=openapidb > backups/mongodb-$$(date +%Y%m%d-%H%M%S).gz
	@docker-compose exec -T redis redis-cli BGSAVE
	@docker cp tmforum-openapi-redis:/data/dump.rdb backups/redis-$$(date +%Y%m%d-%H%M%S).rdb
	@echo "✅ Backups created in backups/"

# Restore MongoDB
restore-mongo:
	@if [ -z "$(FILE)" ]; then \
		echo "❌ Specify the file: make restore-mongo FILE=backups/mongodb-20240101.gz"; \
	else \
		echo "📥 Restoring MongoDB from $(FILE)..."; \
		docker cp $(FILE) tmforum-openapi-mongodb:/tmp/backup.gz; \
		docker-compose exec -T mongodb mongorestore --archive=/tmp/backup.gz --gzip; \
		echo "✅ Restoration completed"; \
	fi

# See data status
data-status:
	@echo "📊 Status of persistent data:"
	@echo "MongoDB:"
	@du -sh data/mongodb 2>/dev/null || echo "  Directory does not exist"
	@echo "Redis:"
	@du -sh data/redis 2>/dev/null || echo "  Directory does not exist"
	@echo ""
	@echo "Containers:"
	@docker-compose ps

# Help
help:
	@echo "Available commands:"
	@echo "  make build              - Compile the application"
	@echo "  make docker-build       - Build Docker image"
	@echo "  make docker-run         - Run with docker-compose"
	@echo "  make docker-stop        - Stop docker-compose"
	@echo "  make docker-logs        - View logs of docker-compose"
	@echo "  make k8s-deploy         - Deploy to Kubernetes"
	@echo "  make k8s-deploy-ingress - Deploy with Ingress"
	@echo "  make k8s-delete         - Delete Kubernetes resources"
	@echo "  make k8s-logs          - View logs in Kubernetes"
	@echo "  make k8s-status        - View status in Kubernetes"
	@echo "  make k8s-port-forward  - Port-forward to the API"
	@echo "  make dev               - Start development environment"
	@echo "  make clean             - Clean everything"

