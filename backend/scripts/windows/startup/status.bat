@echo off
REM ========================================
REM HonyRun Project Status Check Script - Batch Version
REM Development Environment: Windows 11 + 32GB RAM
REM Configuration Target: Maximize high-performance hardware resources
REM @author: Mr.Rey Copyright © 2025
REM @created: 2025-10-27 12:26:43
REM @modified: 2025-10-27 12:26:43
REM @version: 1.0.3
REM Description: Check MySQL, Redis and Spring Boot application service status
REM Note: This script calls the corresponding PowerShell script for unified maintenance
REM ========================================

echo [%date% %time%] [INFO] Calling PowerShell status check script...
echo [%date% %time%] [INFO] Script path: %~dp0status.ps1

REM Check if PowerShell script exists
if not exist "%~dp0status.ps1" (
    echo [%date% %time%] [ERROR] PowerShell status check script not found: %~dp0status.ps1
    echo [%date% %time%] [ERROR] Please ensure status.ps1 file exists in the same directory
    pause
    exit /b 1
)

REM Call PowerShell script with all parameters
powershell.exe -ExecutionPolicy Bypass -File "%~dp0status.ps1" %*

REM Get PowerShell script exit code
set PS_EXIT_CODE=%ERRORLEVEL%

echo [%date% %time%] [INFO] PowerShell script execution completed, exit code: %PS_EXIT_CODE%

REM If PowerShell script execution failed, pause to view error information
if %PS_EXIT_CODE% neq 0 (
    echo [%date% %time%] [WARNING] PowerShell script execution exception, please check above error information
    pause
)

exit /b %PS_EXIT_CODE%
