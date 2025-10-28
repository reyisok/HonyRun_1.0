# HonyRun Application Stop Script - PowerShell Version
# @author: Mr.Rey Copyright © 2025
# @created: 2025-10-26 01:36:12
# @modified: 2025-10-27 12:26:43
# @version: 2.3.0 - 强制停止HonyRun项目端口版本

param(
    [switch]$Force = $false,
    [int]$Timeout = 30
)

# 增强日志输出函数 - 同步错误、警告、异常信息到控制台
function Write-LogMessage {
    param(
        [string]$Message,
        [string]$Level = "INFO",
        [string]$Color = "White"
    )

    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $logMessage = "$timestamp [$Level] $Message"

    # 输出到控制台
    Write-Host $logMessage -ForegroundColor $Color

    # 同时写入日志文件
    $logFile = "logs\system.log"
    if (-not (Test-Path "logs")) {
        New-Item -ItemType Directory -Path "logs" -Force | Out-Null
    }
    Add-Content -Path $logFile -Value $logMessage -Encoding UTF8
}

# 错误处理函数 - 捕获并同步所有异常信息
function Handle-Error {
    param(
        [string]$Operation,
        [System.Management.Automation.ErrorRecord]$ErrorRecord
    )

    $errorMessage = "$Operation 失败: $($ErrorRecord.Exception.Message)"
    Write-LogMessage -Message $errorMessage -Level "ERROR" -Color "Red"

    if ($ErrorRecord.Exception.InnerException) {
        Write-LogMessage -Message "内部异常: $($ErrorRecord.Exception.InnerException.Message)" -Level "ERROR" -Color "Red"
    }

    if ($ErrorRecord.ScriptStackTrace) {
        Write-LogMessage -Message "堆栈跟踪: $($ErrorRecord.ScriptStackTrace)" -Level "DEBUG" -Color "Gray"
    }
}

# 服务停止监控函数 - 实时监控服务停止状态和错误
function Monitor-ServiceShutdown {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$TimeoutSeconds = 30
    )

    Write-LogMessage -Message "开始监控 $ServiceName 停止状态..." -Level "INFO" -Color "Cyan"

    $startTime = Get-Date
    $timeout = $startTime.AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $timeout) {
        try {
            # 检查端口是否还在监听
            $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
            if (-not $connection) {
                Write-LogMessage -Message "$ServiceName 已成功停止，端口 $Port 已释放" -Level "SUCCESS" -Color "Green"
                return $true
            }

            Start-Sleep -Seconds 2
        } catch {
            Handle-Error -Operation "$ServiceName 状态检查" -ErrorRecord $_
        }
    }

    Write-LogMessage -Message "$ServiceName 停止超时 ($TimeoutSeconds 秒)，可能需要强制终止" -Level "WARNING" -Color "Yellow"
    return $false
}

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-LogMessage -Message "========================================" -Level "INFO" -Color "Red"
Write-LogMessage -Message "HonyRun Application Stop Script v2.3.0" -Level "INFO" -Color "Red"
Write-LogMessage -Message "========================================" -Level "INFO" -Color "Red"
Write-LogMessage -Message "Stopping HonyRun Application and Services..." -Level "INFO" -Color "Yellow"
Write-LogMessage -Message "Current directory: $(Get-Location)" -Level "INFO" -Color "Yellow"
Write-LogMessage -Message "Script location: $PSScriptRoot" -Level "INFO" -Color "Yellow"

# 设置项目根目录
$ProjectRoot = Join-Path $PSScriptRoot "..\..\..\"
Set-Location $ProjectRoot
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Project root directory: $(Get-Location)" -ForegroundColor Yellow

# ========================================
# 统一配置管理 - 从application-dev.properties读取配置
# 严格执行统一配置，禁止硬编码任何配置参数
# ========================================
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 正在从统一配置文件读取停止参数..." -ForegroundColor Green

$Profile = "dev"
$ConfigFile = "src\main\resources\application-$Profile.properties"

if (-not (Test-Path $ConfigFile)) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] 统一配置文件未找到: $ConfigFile" -ForegroundColor Red
    exit 1
}

# 配置读取函数
function Get-ConfigValue {
    param(
        [string]$Key,
        [string]$DefaultValue = ""
    )

    $line = Get-Content $ConfigFile | Where-Object { $_ -match "^$Key=" } | Select-Object -First 1
    if ($line) {
        $value = $line.Split('=', 2)[1].Trim()

        # 处理环境变量格式 ${VAR_NAME:default_value}
        if ($value -match '^\$\{[^}]+:([^}]+)\}$') {
            return $matches[1].Trim()
        }

        return $value
    }
    return $DefaultValue
}

# 从统一配置文件读取所有端口参数
$AppPort = Get-ConfigValue "server.port" "8901"
$MySQLPort = Get-ConfigValue "honyrun.database.port" "8906"
$RedisPort = Get-ConfigValue "spring.data.redis.port" "8902"

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 统一配置读取完成：" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 应用端口: $AppPort" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - MySQL端口: $MySQLPort" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - Redis端口: $RedisPort" -ForegroundColor Green
# ========================================
# 工具函数定义
# ========================================

# 检查端口是否被占用的函数
function Test-Port {
    param([int]$Port)
    try {
        $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        return $connections.Count -gt 0
    }
    catch {
        return $false
    }
}

# 获取端口对应的进程信息
function Get-PortProcess {
    param([int]$Port)
    try {
        $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        if ($connections) {
            $processId = $connections[0].OwningProcess
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            return @{
                ProcessId = $processId
                ProcessName = $process.ProcessName
                Process = $process
            }
        }
    }
    catch {
        return $null
    }
    return $null
}

# 强制停止进程的函数 - 针对HonyRun项目端口直接强制停止
function Stop-ProcessForcefully {
    param(
        [int]$ProcessId,
        [string]$ProcessName,
        [int]$Port
    )

    try {
        $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
        if (-not $process) {
            Write-LogMessage -Message "Process $ProcessName (PID: $ProcessId) is already stopped" -Level "INFO" -Color "Green"
            return $true
        }

        # 对于HonyRun项目端口直接强制停止（从统一配置读取）
        $honyrunPorts = @($AppPort, $RedisPort, $MySQLPort)
        if ($Port -in $honyrunPorts) {
            Write-LogMessage -Message "HonyRun project port detected ($Port), forcing immediate termination of $ProcessName (PID: $ProcessId)..." -Level "INFO" -Color "Yellow"

            try {
                $process.Kill()
                Write-LogMessage -Message "Process $ProcessName (PID: $ProcessId) terminated successfully" -Level "SUCCESS" -Color "Green"

                # 使用监控函数验证停止状态
                if (Monitor-ServiceShutdown -ServiceName $ProcessName -Port $Port -TimeoutSeconds 10) {
                    return $true
                } else {
                    Write-LogMessage -Message "Process $ProcessName may still be running after termination attempt" -Level "WARNING" -Color "Yellow"
                    return $false
                }
            } catch {
                Handle-Error -Operation "强制终止进程 $ProcessName" -ErrorRecord $_
                return $false
            }
            $process.WaitForExit(5000)
            Write-Host "[INFO] ✓ Process $ProcessName terminated forcefully" -ForegroundColor Green
            return $true
        }

        # 对于其他端口，保持原有的优雅关闭逻辑
        Write-Host "[INFO] Attempting graceful shutdown of $ProcessName (PID: $ProcessId)..." -ForegroundColor Yellow

        # 尝试优雅关闭
        $process.CloseMainWindow() | Out-Null

        # 等待进程退出
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        while ($stopwatch.Elapsed.TotalSeconds -lt 10) {
            if ($process.HasExited) {
                Write-Host "[INFO] ✓ Process $ProcessName stopped gracefully" -ForegroundColor Green
                return $true
            }
            Start-Sleep -Milliseconds 500
            try {
                $process.Refresh()
            }
            catch {
                Write-Host "[INFO] ✓ Process $ProcessName stopped gracefully" -ForegroundColor Green
                return $true
            }
        }

        # 如果优雅关闭失败，强制终止
        Write-Host "[WARN] Graceful shutdown timeout, forcing termination..." -ForegroundColor Yellow
        $process.Kill()
        $process.WaitForExit(5000)
        Write-Host "[INFO] ✓ Process $ProcessName terminated forcefully" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "[ERROR] Failed to stop process $ProcessName (PID: $ProcessId): $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 步骤1: 停止HonyRun应用
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[INFO] Step 1: Stopping HonyRun Application on port $AppPort..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

if (Test-Port -Port $AppPort) {
    $appProcess = Get-PortProcess -Port $AppPort
    if ($appProcess) {
        Write-Host "[INFO] Found HonyRun Application process: $($appProcess.ProcessName) (PID: $($appProcess.ProcessId))" -ForegroundColor Yellow
        Write-Host "[INFO] HonyRun project port detected, forcing immediate termination..." -ForegroundColor Yellow
        Stop-ProcessForcefully -ProcessId $appProcess.ProcessId -ProcessName $appProcess.ProcessName -Port $AppPort
    }
} else {
    Write-Host "[INFO] ✓ HonyRun Application is already stopped on port $AppPort" -ForegroundColor Green
}

# 步骤2: 停止Redis
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[INFO] Step 2: Stopping Redis on port $RedisPort..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

if (Test-Port -Port $RedisPort) {
    $redisProcess = Get-PortProcess -Port $RedisPort
    if ($redisProcess) {
        Write-Host "[INFO] Found Redis process: $($redisProcess.ProcessName) (PID: $($redisProcess.ProcessId))" -ForegroundColor Yellow
        Write-Host "[INFO] HonyRun project port detected, forcing immediate termination..." -ForegroundColor Yellow
        Stop-ProcessForcefully -ProcessId $redisProcess.ProcessId -ProcessName $redisProcess.ProcessName -Port $RedisPort
    }
} else {
    Write-Host "[INFO] ✓ Redis is already stopped on port $RedisPort" -ForegroundColor Green
}

# 步骤3: 停止MySQL
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[INFO] Step 3: Stopping MySQL on port $MySQLPort..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

if (Test-Port -Port $MySQLPort) {
    $mysqlProcess = Get-PortProcess -Port $MySQLPort
    if ($mysqlProcess) {
        Write-Host "[INFO] Found MySQL process: $($mysqlProcess.ProcessName) (PID: $($mysqlProcess.ProcessId))" -ForegroundColor Yellow
        Write-Host "[INFO] HonyRun project port detected, forcing immediate termination..." -ForegroundColor Yellow
        Stop-ProcessForcefully -ProcessId $mysqlProcess.ProcessId -ProcessName $mysqlProcess.ProcessName -Port $MySQLPort
    }
} else {
    Write-Host "[INFO] ✓ MySQL is already stopped on port $MySQLPort" -ForegroundColor Green
}

# 步骤4: 进程清理和最终状态检查
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Step 4: Process Cleanup and Final Status Verification..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# 清理任何残留的Java、Redis、MySQL进程
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Cleaning up any remaining processes..." -ForegroundColor Yellow

# 清理Java进程（Spring Boot应用）
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.MainWindowTitle -like "*spring-boot*" -or
    $_.ProcessName -eq "java" -and
    (Get-NetTCPConnection -LocalPort $AppPort -ErrorAction SilentlyContinue | Where-Object { $_.OwningProcess -eq $_.Id })
}
foreach ($process in $javaProcesses) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Terminating remaining Java process: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Yellow
    try {
        $process.Kill()
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] ✓ Java process terminated" -ForegroundColor Green
    }
    catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARN] Failed to terminate Java process: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 清理Redis进程
$redisProcesses = Get-Process -Name "redis-server" -ErrorAction SilentlyContinue
foreach ($process in $redisProcesses) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Terminating remaining Redis process: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Yellow
    try {
        $process.Kill()
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] ✓ Redis process terminated" -ForegroundColor Green
    }
    catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARN] Failed to terminate Redis process: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 清理MySQL进程
$mysqlProcesses = Get-Process -Name "mysqld" -ErrorAction SilentlyContinue
foreach ($process in $mysqlProcesses) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] Terminating remaining MySQL process: $($process.ProcessName) (PID: $($process.Id))" -ForegroundColor Yellow
    try {
        $process.Kill()
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] ✓ MySQL process terminated" -ForegroundColor Green
    }
    catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARN] Failed to terminate MySQL process: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 等待进程完全退出
Start-Sleep -Seconds 2

# 最终状态检查
$mysqlStopped = -not (Test-Port -Port $MySQLPort)
$redisStopped = -not (Test-Port -Port $RedisPort)
$appStopped = -not (Test-Port -Port $AppPort)

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 最终状态检查:" -ForegroundColor Yellow
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO]   MySQL (localhost:${MySQLPort}):     $(if($mysqlStopped){'已停止'}else{'运行中'})" -ForegroundColor $(if($mysqlStopped){"Green"}else{"Red"})
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO]   Redis (localhost:${RedisPort}):     $(if($redisStopped){'已停止'}else{'运行中'})" -ForegroundColor $(if($redisStopped){"Green"}else{"Red"})
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO]   Application (localhost:${AppPort}): $(if($appStopped){'已停止'}else{'运行中'})" -ForegroundColor $(if($appStopped){"Green"}else{"Red"})

if ($mysqlStopped -and $redisStopped -and $appStopped) {
    Write-Host ""
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [SUCCESS] ✓ 所有服务已成功停止" -ForegroundColor Green
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 系统状态: 完全停止" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "HonyRun应用停止完成" -ForegroundColor Green
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    exit 0
} else {
    Write-Host ""
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] ✗ 部分服务仍在运行" -ForegroundColor Red
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 系统状态: 部分停止" -ForegroundColor Yellow

    if (-not $mysqlStopped) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] MySQL仍在端口 $MySQLPort 上运行" -ForegroundColor Red
    }
    if (-not $redisStopped) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis仍在端口 $RedisPort 上运行" -ForegroundColor Red
    }
    if (-not $appStopped) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] 应用仍在端口 $AppPort 上运行" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 您可能需要手动停止剩余服务或使用 -Force 参数" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "HonyRun应用停止完成（有错误）" -ForegroundColor Red
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
