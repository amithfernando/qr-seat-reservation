#!/bin/bash

set -e

echo "======================================"
echo "Generating API Client from OpenAPI Spec"
echo "======================================"

# Check if OpenAPI spec exists
if [ ! -f "src/main/resources/openapi.json" ]; then
    echo "Error: OpenAPI spec not found at src/main/resources/openapi.json"
    echo "Please run ./download-openapi-spec.sh first"
    exit 1
fi

# Clean old generated files (except custom ones)
echo "Cleaning old generated files..."
rm -rf src/main/java/com/amithfernando/qrseatreservation/client/api/
rm -rf src/main/java/com/amithfernando/qrseatreservation/client/model/
rm -rf src/main/java/com/amithfernando/qrseatreservation/client/invoker/

# Generate client
echo "Generating client code..."
mvn clean generate-sources

echo ""
echo "✓ Client generation completed successfully!"
echo ""
echo "Generated files are in:"
echo "  - src/main/java/com/amithfernando/qrseatreservation/client/api/"
echo "  - src/main/java/com/amithfernando/qrseatreservation/client/model/"
echo "  - src/main/java/com/amithfernando/qrseatreservation/client/invoker/"
echo ""
echo "To compile and install:"
echo "  mvn clean install"