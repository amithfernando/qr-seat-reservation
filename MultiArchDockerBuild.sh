# Enable buildx and build
docker-buildx create --use
docker-buildx bake -f docker-compose.yaml --set "*.platform=linux/amd64,linux/arm64" --push