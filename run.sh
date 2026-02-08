#!/bin/bash

# Scientific Paper Crawler Java Run Script
set -e

JAR_FILE="target/scientific-paper-crawler-1.0.0.jar"
CONFIG_FILE="config/config.toml"

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found. Please build the project first."
    echo "Run: ./build.sh"
    exit 1
fi

# 检查配置文件是否存在
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Warning: Config file not found at $CONFIG_FILE"
    echo "Using default configuration..."
fi

# 运行应用程序
echo "Starting Scientific Paper Crawler..."
java -jar "$JAR_FILE" "$@"