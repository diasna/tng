#!/bin/bash

# Build and run the application with Docker Compose
set -e

echo "🚀 Starting Tracking Number Generator application..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose not found. Please install docker-compose."
    exit 1
fi

# Build the application first
echo "🔨 Building the application..."
./gradlew clean bootJar

# Start the services
echo "🐳 Starting Docker services..."
docker-compose down --remove-orphans
docker-compose up --build -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check health
echo "🔍 Checking service health..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is healthy!"
    echo ""
    echo "🌐 Application URLs:"
    echo "   - API: http://localhost:8080"
    echo "   - Health: http://localhost:8080/actuator/health"
    echo "   - Metrics: http://localhost:8080/actuator/metrics"
    echo "   - Jaeger UI: http://localhost:16686"
    echo "   - Prometheus: http://localhost:9090"
    echo "   - Grafana: http://localhost:3000 (admin/grafana_pass)"
    echo ""
    echo "📊 Generate a tracking number:"
    echo "   curl 'http://localhost:8080/api/v1/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&customer_id=550e8400-e29b-41d4-a716-446655440000&customer_name=Test%20Customer&customer_slug=test-customer'"
else
    echo "❌ Application is not healthy. Check logs with: docker-compose logs app"
    exit 1
fi
