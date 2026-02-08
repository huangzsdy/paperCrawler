@echo off
setlocal enabledelayedexpansion

set JAR_FILE=target\scientific-paper-crawler-1.0.0.jar
set CONFIG_FILE=config\config.toml

:: 检查JAR文件是否存在
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found. Please build the project first.
    echo Run: mvn clean package
    exit /b 1
)

:: 检查配置文件是否存在
if not exist "%CONFIG_FILE%" (
    echo Warning: Config file not found at %CONFIG_FILE%
    echo Using default configuration...
)

:: 运行应用程序
echo Starting Scientific Paper Crawler...
java -jar "%JAR_FILE%" %*