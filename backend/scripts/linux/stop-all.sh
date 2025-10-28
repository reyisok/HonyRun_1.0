#!/bin/bash
# HonyRun Application Stop All Services Script - Linux
# @author: Mr.Rey Copyright (c) 2025
# @created: 2025-06-28 23:00:00
# @modified: 2025-01-27 23:00:00
# @version: 1.0.3

set -e

echo "==============================================="
echo "HonyRun Stop All Services Script"
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
REDIS_PORT="${REDIS_PORT:-8902}"
MYSQL_PORT="${MYSQL_PORT:-8906}"

echo "[INFO] Stopping all HonyRun services..."
echo

# Stop HonyRun application
echo "[STEP 1/2] Stopping HonyRun application..."
if netstat -tuln | grep -q ":8901 "; then
    # Try to get PID from file first
    if [ -f "logs/honyrun.pid" ]; then
        PID=$(cat logs/honyrun.pid)
        echo "[INFO] Terminating HonyRun application (PID: $PID)..."

        if kill -0 "$PID" 2>/dev/null; then
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

            echo "[SUCCESS] HonyRun application stopped"
        else
            echo "[WARNING] PID file exists but process is not running"
        fi

        rm -f logs/honyrun.pid
    else
        # Try to find PID by port
        PID=$(lsof -ti:8901 2>/dev/null || echo "")
        if [ -n "$PID" ]; then
            echo "[INFO] Terminating HonyRun application (PID: $PID)..."
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

            echo "[SUCCESS] HonyRun application stopped"
        else
            echo "[WARNING] Could not find HonyRun application process"
        fi
    fi
else
    echo "[INFO] HonyRun application is not running"
fi

echo

# Stop Redis service
echo "[STEP 2/3] Stopping Redis service..."
if netstat -tuln | grep -q ":$REDIS_PORT "; then
    REDIS_PID=$(lsof -ti:$REDIS_PORT 2>/dev/null || echo "")
    if [ -n "$REDIS_PID" ]; then
        echo "[INFO] Terminating Redis service (PID: $REDIS_PID)..."
        kill -TERM "$REDIS_PID"

        # Wait for graceful shutdown
        count=0
        while [ $count -lt 10 ]; do
            if ! kill -0 "$REDIS_PID" 2>/dev/null; then
                break
            fi
            sleep 1
            count=$((count + 1))
        done

        # Force kill if still running
        if kill -0 "$REDIS_PID" 2>/dev/null; then
            echo "[WARNING] Graceful shutdown failed, forcing termination..."
            kill -KILL "$REDIS_PID" 2>/dev/null || true
        fi

        echo "[SUCCESS] Redis service stopped"
    else
        echo "[WARNING] Could not find Redis process"
    fi
else
    echo "[INFO] Redis service is not running"
fi

# Stop MySQL service
echo "[STEP 3/3] Stopping MySQL service..."
if netstat -tuln | grep -q ":$MYSQL_PORT "; then
    if [ -x "mysql/linux/stop.sh" ] || [ -f "mysql/linux/stop.sh" ]; then
        bash ./mysql/linux/stop.sh --port "$MYSQL_PORT" >/dev/null 2>&1 || echo "[WARNING] MySQL stop script reported errors"
    else
        echo "[WARNING] MySQL stop script not found at backend/mysql/linux/stop.sh"
        MYSQL_PID=$(lsof -ti:$MYSQL_PORT 2>/dev/null || echo "")
        if [ -n "$MYSQL_PID" ]; then
            echo "[INFO] Terminating MySQL service (PID: $MYSQL_PID)..."
            kill -TERM "$MYSQL_PID" || true
        fi
    fi
else
    echo "[INFO] MySQL service is not running"
fi

echo

# Wait for services to terminate
echo "[INFO] Waiting for services to stop completely..."
sleep 3

# Verify all services stopped
echo "[INFO] Verifying services status..."
SERVICES_RUNNING=0

if netstat -tuln | grep -q ":8901 "; then
    echo "[WARNING] HonyRun application may still be running"
    SERVICES_RUNNING=1
else
    echo "[SUCCESS] HonyRun application stopped completely"
fi

if netstat -tuln | grep -q ":$REDIS_PORT "; then
    echo "[WARNING] Redis service may still be running"
    SERVICES_RUNNING=1
else
    echo "[SUCCESS] Redis service stopped completely"
fi

if netstat -tuln | grep -q ":$MYSQL_PORT "; then
    echo "[WARNING] MySQL service may still be running"
    SERVICES_RUNNING=1
else
    echo "[SUCCESS] MySQL service stopped completely"
fi

echo

if [ $SERVICES_RUNNING -eq 0 ]; then
    echo "[SUCCESS] All services stopped successfully!"
else
    echo "[WARNING] Some services may still be running"
    echo "[INFO] You may need to manually check and terminate remaining processes"
fi

echo
echo "==============================================="
echo "Stop all services process completed"
echo "==============================================="
