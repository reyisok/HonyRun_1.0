#!/bin/bash
# HonyRun Application Restart Script - Linux
# @author: Mr.Rey Copyright (c) 2025
# @created: 2025-06-28 23:00:00
# @modified: 2025-01-27 23:00:00
# @version: 1.0.3

set -e

echo "==============================================="
echo "HonyRun Application Restart Script"
echo "==============================================="
echo

# Get script directory and switch to backend project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

echo "[INFO] Restarting HonyRun application..."
echo

# Stop the application first
echo "[STEP 1/2] Stopping application..."
bash scripts/linux/stop.sh
STOP_EXIT_CODE=$?

if [ $STOP_EXIT_CODE -ne 0 ]; then
    echo "[WARNING] Stop script returned error code $STOP_EXIT_CODE"
fi

echo
echo "[INFO] Waiting 5 seconds before restart..."
sleep 5

# Start the application
echo "[STEP 2/2] Starting application..."
bash scripts/linux/start.sh
START_EXIT_CODE=$?

if [ $START_EXIT_CODE -eq 0 ]; then
    echo
    echo "[SUCCESS] HonyRun application restarted successfully!"
else
    echo
    echo "[ERROR] Failed to restart application (exit code: $START_EXIT_CODE)"
    echo "[INFO] Check logs/honyrun-dev.log for details"
    exit 1
fi

echo "==============================================="