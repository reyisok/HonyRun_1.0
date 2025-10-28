@echo off
:: ========================================
:: MySQL 9.4.0 Windows Startup Script
:: High Performance Development Environment - 32GB Memory Optimized
:: @author: Mr.Rey Copyright © 2025
:: @created: 2025-01-28 16:30:00
:: @modified: 2025-01-28 16:30:00
:: @version: 1.1.0
:: ========================================

setlocal enabledelayedexpansion

:: Set environment variables - using relative paths
set "PROJECT_ROOT=%~dp0..\..\..\..\..\"
set "MYSQL_DIR=%~dp0"
set "MYSQL_SERVER_DIR=%MYSQL_DIR%server"
set "MYSQL_DATA_DIR=%MYSQL_DIR%data"
set "MYSQL_LOGS_DIR=%MYSQL_DIR%logs"
set "MYSQL_TEMP_DIR=%MYSQL_DIR%temp"
set "MYSQL_CONFIG=%MYSQL_DIR%my.cnf"
set "MYSQL_PID_FILE=%MYSQL_DIR%mysql.pid"

:: Database configuration
set "MYSQL_PORT=8906"
set "MYSQL_USER=honyrunMysql"
set "MYSQL_PASSWORD=honyrun@sys"
set "MYSQL_DATABASE=honyrundb"

:: Check required files
if not exist "%MYSQL_SERVER_DIR%\bin\mysqld.exe" (
    echo [ERROR] MySQL server executable not found: %MYSQL_SERVER_DIR%\bin\mysqld.exe
    exit /b 1
)

if not exist "%MYSQL_CONFIG%" (
    echo [ERROR] MySQL configuration file not found: %MYSQL_CONFIG%
    exit /b 1
)

:: Create necessary directories (silent)
if not exist "%MYSQL_DATA_DIR%" mkdir "%MYSQL_DATA_DIR%" >nul 2>&1
if not exist "%MYSQL_LOGS_DIR%" mkdir "%MYSQL_LOGS_DIR%" >nul 2>&1
if not exist "%MYSQL_TEMP_DIR%" mkdir "%MYSQL_TEMP_DIR%" >nul 2>&1

:: Check port usage and terminate existing processes
netstat -ano | findstr ":%MYSQL_PORT%" >nul 2>&1
if !errorlevel! equ 0 (
    echo [WARNING] Port %MYSQL_PORT% is in use, attempting to terminate existing process...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%MYSQL_PORT%"') do (
        taskkill /PID %%a /F >nul 2>&1
    )
    timeout /t 2 >nul
)

:: Initialize data directory (if empty)
if not exist "%MYSQL_DATA_DIR%\mysql" (
    echo [INFO] Initializing MySQL data directory...
    "%MYSQL_SERVER_DIR%\bin\mysqld.exe" --defaults-file="%MYSQL_CONFIG%" --initialize-insecure >nul 2>&1
    if !errorlevel! neq 0 (
        echo [ERROR] Failed to initialize MySQL data directory
        exit /b 1
    )
    echo [SUCCESS] MySQL data directory initialized
)

:: Start MySQL server (background)
echo [INFO] Starting MySQL 9.4.0 server on port %MYSQL_PORT%...
powershell -WindowStyle Hidden -Command "Start-Process -FilePath '%MYSQL_SERVER_DIR%\bin\mysqld.exe' -ArgumentList '--defaults-file=%MYSQL_CONFIG%' -WindowStyle Hidden"

:: Wait for server startup
timeout /t 5 >nul

:: Check if server started successfully
netstat -an | findstr ":%MYSQL_PORT%" >nul 2>&1
if !errorlevel! equ 0 (
    echo [INFO] Creating database and user configuration...
    :: Create database and user (silent)
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "CREATE DATABASE IF NOT EXISTS %MYSQL_DATABASE%;" >nul 2>&1
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "CREATE USER IF NOT EXISTS '%MYSQL_USER%'@'localhost' IDENTIFIED BY '%MYSQL_PASSWORD%';" >nul 2>&1
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "GRANT ALL PRIVILEGES ON %MYSQL_DATABASE%.* TO '%MYSQL_USER%'@'localhost';" >nul 2>&1
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "CREATE USER IF NOT EXISTS '%MYSQL_USER%'@'127.0.0.1' IDENTIFIED BY '%MYSQL_PASSWORD%';" >nul 2>&1
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "GRANT ALL PRIVILEGES ON %MYSQL_DATABASE%.* TO '%MYSQL_USER%'@'127.0.0.1';" >nul 2>&1
    "%MYSQL_SERVER_DIR%\bin\mysql.exe" -u root -P %MYSQL_PORT% -e "FLUSH PRIVILEGES;" >nul 2>&1

    echo [SUCCESS] MySQL 9.4.0 started successfully on port %MYSQL_PORT%
    echo [INFO] Database: %MYSQL_DATABASE%, User: %MYSQL_USER%
    exit /b 0
) else (
    echo [ERROR] MySQL server startup failed - port %MYSQL_PORT% not listening
    exit /b 1
)
