#!/bin/bash
# HonyRun Application Start Script - Linux
# @author: Mr.Rey Copyright (c) 2025
# @created: 2025-06-28 23:00:00
# @modified: 2025-01-27 23:00:00
# @version: 1.0.3

set -e

echo "==============================================="
echo "HonyRun Application Start Script"
echo "==============================================="
echo

# Get script directory and switch to backend project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

# Check if application is already running
echo "[INFO] Checking if application is already running..."
if netstat -tuln | grep -q ":8901 "; then
    echo "[WARNING] Application is already running on port 8901"
    PID=$(lsof -ti:8901 2>/dev/null || echo "")
    if [ -n "$PID" ]; then
        echo "[INFO] Process ID: $PID"
    fi
    echo "[INFO] Use stop.sh to stop the application first"
    exit 1
fi

# Load environment variables
REPO_ROOT="$(cd "$PROJECT_ROOT/.." && pwd)"
if [ -f "$REPO_ROOT/.env" ]; then
    echo "[INFO] Loading environment from $REPO_ROOT/.env"
    set -a
    . "$REPO_ROOT/.env"
    set +a
fi

# Resolve service ports
# 严格遵循统一端口规范：应用 8901，Redis 8902，MySQL 8906
REDIS_PORT="${REDIS_PORT:-8902}"
MYSQL_PORT="${MYSQL_PORT:-8906}"

# Ensure Redis
echo "[INFO] Checking Redis service on port $REDIS_PORT..."
if ! netstat -tuln | grep -q ":$REDIS_PORT "; then
    echo "[WARNING] Redis is not running on port $REDIS_PORT"
    echo "[INFO] Starting Redis via backend/redis/linux/start.sh ..."
    if [ -f "$PROJECT_ROOT/redis/linux/start.sh" ]; then
        cd "$PROJECT_ROOT/redis/linux"
        ./start.sh
        cd "$PROJECT_ROOT"
    else
        echo "[ERROR] Redis start script not found at $PROJECT_ROOT/redis/linux/start.sh"
        exit 1
    fi
    
    # Wait for Redis to start
    sleep 3
    if ! netstat -tuln | grep -q ":$REDIS_PORT "; then
        echo "[ERROR] Failed to start Redis on port $REDIS_PORT"
        exit 1
    fi
    echo "[SUCCESS] Redis started on port $REDIS_PORT"
else
    echo "[INFO] Redis is already running on port $REDIS_PORT"
fi

# Ensure MySQL
echo "[INFO] Checking MySQL service on port $MYSQL_PORT..."
if ! netstat -tuln | grep -q ":$MYSQL_PORT "; then
    echo "[WARNING] MySQL is not running on port $MYSQL_PORT"
    echo "[INFO] Starting MySQL via backend/mysql/linux/start.sh ..."
    if [ -f "$PROJECT_ROOT/mysql/linux/start.sh" ]; then
        cd "$PROJECT_ROOT/mysql/linux"
        ./start.sh
        cd "$PROJECT_ROOT"
    else
        echo "[ERROR] MySQL start script not found at $PROJECT_ROOT/mysql/linux/start.sh"
        exit 1
    fi
    
    # Wait for MySQL to start
    sleep 5
    if ! netstat -tuln | grep -q ":$MYSQL_PORT "; then
        echo "[ERROR] Failed to start MySQL on port $MYSQL_PORT"
        exit 1
    fi
    echo "[SUCCESS] MySQL started on port $MYSQL_PORT"
else
    echo "[INFO] MySQL is already running on port $MYSQL_PORT"
fi
# Check config file exists
if [ ! -f "config/jvm-args-dev.txt" ]; then
    echo "[ERROR] Config file config/jvm-args-dev.txt not found"
    exit 1
fi

# Read JVM arguments from config file
JVM_ARGS=$(cat config/jvm-args-dev.txt)

# Check if JAR file exists
JAR_FILE=$(find target -name "honyrun-reactive-*.jar" -type f 2>/dev/null | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "[ERROR] JAR file not found in target directory"
    echo "[INFO] Building application..."
    if command -v mvn >/dev/null 2>&1; then
        mvn clean package -DskipTests -q
        if [ $? -ne 0 ]; then
            echo "[ERROR] Failed to build application"
            exit 1
        fi
        JAR_FILE=$(find target -name "honyrun-reactive-*.jar" -type f 2>/dev/null | head -n 1)
    else
        echo "[ERROR] Maven not found. Please install Maven or build the application manually"
        exit 1
    fi
fi

# Create logs directory if not exists
mkdir -p logs

# Start application
echo "[INFO] Starting HonyRun application..."
echo "[INFO] JAR File: $JAR_FILE"
echo "[INFO] Profile: dev"
echo "[INFO] Log file: logs/honyrun-dev.log"
echo

LOG_FILE="logs/honyrun-dev-$(date +%Y%m%d-%H%M%S).log"
nohup java $JVM_ARGS -jar "$JAR_FILE" --spring.profiles.active=dev > "$LOG_FILE" 2>&1 &
APP_PID=$!

# Save PID to file
echo $APP_PID > logs/honyrun.pid

# Wait for log file to be created
echo "[INFO] Waiting for log file to be created..."
while [ ! -f "$LOG_FILE" ]; do
    sleep 1
done

# Wait for application to start
echo "[INFO] Waiting for application to start..."
count=0
while [ $count -lt 30 ]; do
    sleep 2
    if netstat -tuln | grep -q ":8901 "; then
        break
    fi
    count=$((count + 1))
done

if netstat -tuln | grep -q ":8901 "; then
    echo "[SUCCESS] HonyRun application started successfully!"
    echo "[INFO] Application URL: http://localhost:8901"
    echo "[INFO] Health check: http://localhost:8901/actuator/health"
    echo "[INFO] API documentation: http://localhost:8901/swagger-ui.html"
    echo "[INFO] Log file: $LOG_FILE"
    echo "[INFO] PID file: logs/honyrun.pid"
    echo
    echo "Use stop.sh to stop the application"
    echo "Use status.sh to check application status"
    echo "==============================================="
else
    echo "[ERROR] Application failed to start within 60 seconds"
    echo "[INFO] Check $LOG_FILE for details"
    if [ -f logs/honyrun.pid ]; then
        rm logs/honyrun.pid
    fi
    exit 1
fi