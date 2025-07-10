#!/bin/bash

# Build and run the Java Bridge for Digital Twin Dashboard

echo "Building Java Bridge..."

# Clean and compile
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Starting Java Bridge server..."

# Run the Spring Boot application
mvn spring-boot:run

echo "Java Bridge server stopped."
