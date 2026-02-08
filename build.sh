#!/bin/bash

# Scientific Paper Crawler Java Build Script
set -e

echo "Building Scientific Paper Crawler Java Edition..."

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven first."
    exit 1
fi

# 清理并编译项目
echo "Cleaning and compiling project..."
mvn clean compile

# 运行测试
echo "Running tests..."
mvn test

# 打包项目
echo "Packaging application..."
mvn package -DskipTests

echo "Build completed successfully!"
echo "JAR file location: target/scientific-paper-crawler-1.0.0.jar"
echo "Run with: java -jar target/scientific-paper-crawler-1.0.0.jar"