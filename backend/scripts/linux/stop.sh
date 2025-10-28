#!/bin/bash
# HonyRun Application Stop Script - Linux
# @author: Mr.Rey Copyright (c) 2025
# @created: 2025-06-28 23:00:00
# @modified: 2025-01-27 23:00:00
# @version: 1.0.3

set -e

echo "==============================================="
echo "HonyRun Application Stop Script"
echo "==============================================="
echo

# Get script directory and switch to backend project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

# Load environment variables
REPO_ROOT="$(cd "$PROJECT_ROOT/.." && pwd)"
if [ -f "$REPO_ROOT/.env" ]; then
    set -a; . "$REPO_ROOT/.env"; set +a
fi
# 严格遵循统一端口规范：应用 8901，Redis 8902，MySQL 8906
REDIS_PORT="${REDIS_PORT:-8902}"
MYSQL_PORT="${MYSQL_PORT:-8906}"

# Check if application is running
echo "[INFO] Checking application status..."
if ! netstat -tuln | grep -q ":8901 "; then
    echo "[INFO] Application is not running on port 8901"
else
    # Try to get PID from file first
    if [ -f "logs/honyrun.pid" ]; then
        PID=$(cat logs/honyrun.pid)
        echo "[INFO] Found PID from file: $PID"
        
        # Verify the PID is still valid
        if kill -0 "$PID" 2>/dev/null; then
            echo "[INFO] Terminating application (PID: $PID)..."
            kill -TERM "$PID"
            
            # Wait for graceful shutdown
            count=0
            while [ $count -lt 10 ]; do
                if ! kill -0 "$PID" 2>/dev/null; then
                    break
                fi
                sleep 1
                count=$((count + 1))
            done
            
            # Force kill if still running
            if kill -0 "$PID" 2>/dev/null; then
                echo "[WARNING] Graceful shutdown failed, forcing termination..."
                kill -KILL "$PID" 2>/dev/null || true
            fi
            
            echo "[SUCCESS] Application stopped successfully"
        else
            echo "[WARNING] PID file exists but process is not running"
        fi
        
        # Remove PID file
        rm -f logs/honyrun.pid
    else
        # Try to find PID by port
        PID=$(lsof -ti:8901 2>/dev/null || echo "")
        if [ -n "$PID" ]; then
            echo "[INFO] Found application process ID: $PID"
            echo "[INFO] Terminating application..."
            kill -TERM "$PID"
            
            # Wait for graceful shutdown
            count=0
            while [ $count -lt 10 ]; do
                if ! kill -0 "$PID" 2>/dev/null; then
                    break
                fi
                sleep 1
                count=$((count + 1))
            done
            
            # Force kill if still running
            if kill -0 "$PID" 2>/dev/null; then
                echo "[WARNING] Graceful shutdown failed, forcing termination..."
                kill -KILL "$PID" 2>/dev/null || true
            fi
            
            echo "[SUCCESS] Application stopped successfully"
        else
            echo "[WARNING] Could not find application process"
        fi
    fi
fi

# Wait for process to terminate
echo "[INFO] Waiting for application to stop..."
sleep 3

# Verify application stopped
if netstat -tuln | grep -q ":8901 "; then
    echo "[WARNING] Application may still be running"
    echo "[INFO] You may need to manually terminate remaining processes"
else
    echo "[SUCCESS] Application stopped completely"
fi

# Check Redis service status
echo
echo "[INFO] Checking Redis service..."
if ! netstat -tuln | grep -q ":$REDIS_PORT "; then
    echo "[INFO] Redis is not running"
else
    echo "[INFO] Redis is still running on port $REDIS_PORT"
    echo "[INFO] Redis will continue running for other services"
    echo "[INFO] Use stop-all.sh to stop all services including Redis"
fi

# Optionally stop MySQL (not stopping here)
echo "[INFO] MySQL service is managed separately (use mysql/linux/stop.sh)"

echo
echo "==============================================="
echo "Application stop process completed"
echo "==============================================="