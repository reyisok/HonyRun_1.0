#!/bin/bash
# HonyRun Application Status Check Script - Linux
# @author: Mr.Rey Copyright (c) 2025
# @created: 2025-06-28 23:00:00
# @modified: 2025-01-27 23:00:00
# @version: 1.0.0

echo "==============================================="
echo "HonyRun Application Status Check"
echo "==============================================="
echo

# Get script directory and switch to backend project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

# Check application status
echo "[INFO] Checking HonyRun application status..."
if netstat -tuln | grep -q ":8901 "; then
    echo "[STATUS] Application: RUNNING"
    PID=$(lsof -ti:8901 2>/dev/null || echo "")
    if [ -n "$PID" ]; then
        echo "[INFO] Process ID: $PID"
        echo "[INFO] Port: 8901"
        echo "[INFO] URL: http://localhost:8901"
    fi
    
    # Check PID file
    if [ -f "logs/honyrun.pid" ]; then
        FILE_PID=$(cat logs/honyrun.pid)
        echo "[INFO] PID file: logs/honyrun.pid ($FILE_PID)"
        if [ "$PID" != "$FILE_PID" ]; then
            echo "[WARNING] PID file does not match running process"
        fi
    else
        echo "[WARNING] PID file not found"
    fi
else
    echo "[STATUS] Application: NOT RUNNING"
    if [ -f "logs/honyrun.pid" ]; then
        echo "[WARNING] PID file exists but application is not running"
        echo "[INFO] Cleaning up stale PID file..."
        rm -f logs/honyrun.pid
    fi
fi

echo

# Load environment variables
REPO_ROOT="$(cd "$PROJECT_ROOT/.." && pwd)"
if [ -f "$REPO_ROOT/.env" ]; then
    set -a; . "$REPO_ROOT/.env"; set +a
fi
REDIS_PORT="${REDIS_PORT:-8902}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

# Check Redis status
echo "[INFO] Checking Redis service status..."
if netstat -tuln | grep -q ":$REDIS_PORT "; then
    echo "[STATUS] Redis: RUNNING"
    REDIS_PID=$(lsof -ti:$REDIS_PORT 2>/dev/null || echo "")
    if [ -n "$REDIS_PID" ]; then
        echo "[INFO] Process ID: $REDIS_PID"
        echo "[INFO] Port: $REDIS_PORT"
    fi
else
    echo "[STATUS] Redis: NOT RUNNING"
fi

# Check MySQL status
echo "[INFO] Checking MySQL service status..."
if netstat -tuln | grep -q ":$MYSQL_PORT "; then
    echo "[STATUS] MySQL: RUNNING"
    MYSQL_PID=$(lsof -ti:$MYSQL_PORT 2>/dev/null || echo "")
    if [ -n "$MYSQL_PID" ]; then
        echo "[INFO] Process ID: $MYSQL_PID"
        echo "[INFO] Port: $MYSQL_PORT"
    fi
else
    echo "[STATUS] MySQL: NOT RUNNING"
fi

echo

# Check log files
echo "[INFO] Checking log files..."
if [ -f "logs/honyrun-dev.log" ]; then
    echo "[INFO] Application log: logs/honyrun-dev.log"
    LOG_SIZE=$(stat -f%z "logs/honyrun-dev.log" 2>/dev/null || stat -c%s "logs/honyrun-dev.log" 2>/dev/null || echo "unknown")
    echo "[INFO] Log size: $LOG_SIZE bytes"
    echo "[INFO] Last 5 lines of log:"
    echo "----------------------------------------"
    tail -n 5 "logs/honyrun-dev.log" 2>/dev/null || echo "Could not read log file"
    echo "----------------------------------------"
else
    echo "[INFO] Application log: NOT FOUND"
fi

echo

# Check JAR file
echo "[INFO] Checking application JAR file..."
JAR_FILE=$(find target -name "honyrun-reactive-*.jar" -type f 2>/dev/null | head -n 1)
if [ -n "$JAR_FILE" ]; then
    echo "[INFO] JAR file: $JAR_FILE"
    JAR_SIZE=$(stat -f%z "$JAR_FILE" 2>/dev/null || stat -c%s "$JAR_FILE" 2>/dev/null || echo "unknown")
    echo "[INFO] JAR size: $JAR_SIZE bytes"
    JAR_DATE=$(stat -f%Sm "$JAR_FILE" 2>/dev/null || stat -c%y "$JAR_FILE" 2>/dev/null || echo "unknown")
    echo "[INFO] JAR date: $JAR_DATE"
else
    echo "[INFO] JAR file: NOT FOUND"
    echo "[WARNING] Application needs to be built"
fi

echo

# Check config files
echo "[INFO] Checking configuration files..."
if [ -f "config/jvm-args-dev.txt" ]; then
    echo "[INFO] JVM config: config/jvm-args-dev.txt"
else
    echo "[WARNING] JVM config: NOT FOUND"
fi

APP_CONFIG_YML="src/main/resources/application-dev.yml"
APP_CONFIG_PROP="src/main/resources/application-dev.properties"
if [ -f "$APP_CONFIG_YML" ]; then
    echo "[INFO] App config (yml): $APP_CONFIG_YML"
elif [ -f "$APP_CONFIG_PROP" ]; then
    echo "[INFO] App config (properties): $APP_CONFIG_PROP"
else
    echo "[WARNING] App config: NOT FOUND"
fi

echo

# Health check if application is running
if netstat -tuln | grep -q ":8901 "; then
    echo "[INFO] Performing health check..."
    if command -v curl >/dev/null 2>&1; then
        HEALTH_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/health_response.txt "http://localhost:8901/actuator/health" 2>/dev/null || echo "000")
        if [ "$HEALTH_RESPONSE" = "200" ]; then
            echo "[HEALTH] Status: 200 OK"
            echo "[HEALTH] Response: $(cat /tmp/health_response.txt 2>/dev/null || echo 'Could not read response')"
        else
            echo "[HEALTH] Health check failed with status: $HEALTH_RESPONSE"
        fi
        rm -f /tmp/health_response.txt
    elif command -v wget >/dev/null 2>&1; then
        if wget -q --timeout=5 -O /tmp/health_response.txt "http://localhost:8901/actuator/health" 2>/dev/null; then
            echo "[HEALTH] Status: 200 OK"
            echo "[HEALTH] Response: $(cat /tmp/health_response.txt 2>/dev/null || echo 'Could not read response')"
        else
            echo "[HEALTH] Health check failed"
        fi
        rm -f /tmp/health_response.txt
    else
        echo "[HEALTH] curl or wget not available for health check"
    fi
fi

echo
echo "==============================================="
echo "Status check completed"
echo "==============================================="