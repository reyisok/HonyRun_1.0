# ========================================
# HonyRun WebFlux响应式系统状态检查脚本 - PowerShell版本
# @author: Mr.Rey Copyright © 2025
# @created: 2025-10-26 01:36:12
# @modified: 2025-10-27 12:26:43
# @version: 2.2.0
# @description: 统一配置管理的状态检查脚本，检查MySQL、Redis、应用的运行状态
#              严格按照统一配置规范从环境配置读取所有检查参数
# ========================================

param(
    [switch]$Detailed = $false
)

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') HonyRun WebFlux响应式系统状态检查脚本 v2.2.0" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 开始检查HonyRun系统状态..." -ForegroundColor Yellow

# ========================================
# 【重要】统一配置读取 - 严格按照统一配置管理体系执行
# 按照项目统一配置规范，所有检查参数必须从环境配置文件读取
# 配置文件：application-dev.properties（开发环境）
# 配置前缀：honyrun.* 为项目统一配置前缀
# 禁止硬编码任何配置参数，必须通过配置文件管理
# ========================================

# 设置项目根目录
$ProjectRoot = Join-Path $PSScriptRoot "..\..\..\"
Set-Location $ProjectRoot
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 项目根目录: $(Get-Location)" -ForegroundColor Cyan

$ConfigFile = "src\main\resources\application-dev.properties"
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 正在从统一配置文件读取检查参数..." -ForegroundColor Yellow
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 配置文件路径: $ConfigFile" -ForegroundColor Cyan

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

    $Value = (Get-Content $ConfigFile | Where-Object { $_ -match "^$Key=" } | ForEach-Object { $_.Split('=', 2)[1] }) | Select-Object -First 1
    if ([string]::IsNullOrEmpty($Value)) {
        return $DefaultValue
    }

    # 处理环境变量格式 ${VAR_NAME:default_value}
    if ($Value -match '^\$\{[^}]+:([^}]+)\}$') {
        return $matches[1].Trim()
    }

    return $Value.Trim()
}

# ========================================
# MySQL配置读取 - 按统一配置规范读取数据库连接参数
# 重要：MySQL为项目必备组件，连接失败将导致应用启动终止
# 配置项说明：
# - honyrun.database.port: MySQL服务端口（固定8906）
# - honyrun.database.host: MySQL服务器地址
# - honyrun.database.username: 数据库用户名（统一使用honyrunMysql）
# - honyrun.database.password: 数据库密码（统一使用honyrun@sys）
# ========================================

$MySQLPort = Get-ConfigValue "honyrun.database.port" "8906"
$MySQLHost = Get-ConfigValue "honyrun.database.host" "localhost"
$MySQLUser = Get-ConfigValue "honyrun.database.username" "honyrunMysql"
$MySQLPassword = Get-ConfigValue "honyrun.database.password" "honyrun@sys"
$MySQLDB = Get-ConfigValue "honyrun.database.name" "honyrundb"

# ========================================
# Redis配置读取 - 按统一配置规范读取缓存服务连接参数
# 重要：Redis为项目必备组件，连接失败将导致应用启动终止
# 配置项说明：
# - spring.data.redis.port: Redis服务端口（固定8902）
# - spring.data.redis.host: Redis服务器地址
# - spring.data.redis.password: Redis认证密码（统一使用honyrun@sys）
# ========================================

$RedisPort = Get-ConfigValue "spring.data.redis.port" "8902"
$RedisHost = Get-ConfigValue "spring.data.redis.host" "localhost"
$RedisPassword = Get-ConfigValue "spring.data.redis.password" "honyrun@sys"

# ========================================
# 应用服务配置读取 - 按统一配置规范读取应用服务参数
# 配置项说明：
# - server.port: Spring Boot应用服务端口（固定8901）
# 端口规范：应用8901，Redis8902，MySQL8906（项目端口范围8901-8910）
# ========================================

$AppPort = Get-ConfigValue "server.port" "8901"

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 统一配置读取完成：" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - MySQL配置: ${MySQLHost}:${MySQLPort} (数据库: $MySQLDB, 用户: $MySQLUser)" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - Redis配置: ${RedisHost}:${RedisPort} (认证: 已配置)" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 应用配置: 端口 $AppPort" -ForegroundColor Cyan

# ========================================
# 服务状态检查 - 按最佳实践顺序检查各服务状态
# 检查顺序：MySQL -> Redis -> Spring Boot应用
# 每个服务包含：端口占用检查、进程检查、服务连接检查
# ========================================

# 初始化状态变量
$MySQLStatus = "UNKNOWN"
$RedisStatus = "UNKNOWN"
$AppStatus = "UNKNOWN"
$OverallStatus = "UNKNOWN"

Write-Host ""
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 开始服务状态检查..." -ForegroundColor Yellow
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green

# 检查端口是否被占用的函数
function Test-Port {
    param([int]$Port)
    try {
        # 使用Test-NetConnection进行更可靠的端口检测
        $result = Test-NetConnection -ComputerName "localhost" -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
        return $result
    }
    catch {
        # 回退到Get-NetTCPConnection方法
        try {
            $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
            return $connections.Count -gt 0
        }
        catch {
            return $false
        }
    }
}

# 获取端口对应的进程信息
function Get-PortProcess {
    param([int]$Port)
    try {
        $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
        if ($connections) {
            $processId = $connections[0].OwningProcess
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            return @{
                ProcessId = $processId
                ProcessName = $process.ProcessName
                WorkingSet = [math]::Round($process.WorkingSet64 / 1MB, 2)
            }
        }
    }
    catch {
        return $null
    }
    return $null
}

# ========================================
# MySQL服务状态检查
# 检查项：端口占用、进程存在、数据库连接
# ========================================

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 正在检查MySQL服务状态..." -ForegroundColor Yellow

if (Test-Port -Port $MySQLPort) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] MySQL端口 $MySQLPort 正在监听" -ForegroundColor Green

    # 检查MySQL进程
    $mysqlProcess = Get-Process -Name "mysqld" -ErrorAction SilentlyContinue
    if ($mysqlProcess) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] MySQL进程正在运行" -ForegroundColor Green

        # 尝试连接MySQL数据库（使用mysql客户端）
        $mysqlClientPath = Join-Path $ProjectRoot "mysql\windows\server\bin\mysql.exe"
        if (Test-Path $mysqlClientPath) {
            try {
                $result = & $mysqlClientPath "-h$MySQLHost" "-P$MySQLPort" "-u$MySQLUser" "--password=$MySQLPassword" -e "SELECT 1;" 2>&1
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] MySQL数据库连接成功" -ForegroundColor Green
                    $MySQLStatus = "RUNNING"
                } else {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] MySQL数据库连接失败，退出码: $LASTEXITCODE" -ForegroundColor Yellow
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [DEBUG] MySQL连接输出: $result" -ForegroundColor Gray
                    $MySQLStatus = "CONNECTION_FAILED"
                }
            } catch {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] MySQL数据库连接测试异常: $($_.Exception.Message)" -ForegroundColor Yellow
                $MySQLStatus = "CONNECTION_FAILED"
            }
        } else {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] MySQL客户端未找到，跳过连接测试" -ForegroundColor Yellow
            $MySQLStatus = "RUNNING"
        }
    } else {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] MySQL进程未运行" -ForegroundColor Red
        $MySQLStatus = "PROCESS_NOT_FOUND"
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] MySQL端口 $MySQLPort 未监听" -ForegroundColor Red
    $MySQLStatus = "PORT_NOT_LISTENING"
}

# ========================================
# Redis服务状态检查
# 检查项：端口占用、进程存在、Redis连接
# ========================================

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 正在检查Redis服务状态..." -ForegroundColor Yellow

if (Test-Port -Port $RedisPort) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] Redis端口 $RedisPort 正在监听" -ForegroundColor Green

    # 检查Redis进程
    $redisProcess = Get-Process -Name "redis-server" -ErrorAction SilentlyContinue
    if ($redisProcess) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] Redis进程正在运行" -ForegroundColor Green

        # 尝试连接Redis（使用redis-cli）
        $redisCliPath = Join-Path $ProjectRoot "redis\windows\redis-cli.exe"
        if (Test-Path $redisCliPath) {
            try {
                $result = & $redisCliPath -h $RedisHost -p $RedisPort -a $RedisPassword --no-auth-warning ping 2>$null
                if ($result -eq "PONG") {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] Redis服务连接成功" -ForegroundColor Green
                    $RedisStatus = "RUNNING"
                } else {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] Redis服务连接失败" -ForegroundColor Yellow
                    $RedisStatus = "CONNECTION_FAILED"
                }
            } catch {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] Redis连接测试异常: $($_.Exception.Message)" -ForegroundColor Yellow
                $RedisStatus = "CONNECTION_FAILED"
            }
        } else {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] Redis客户端未找到，跳过连接测试" -ForegroundColor Yellow
            $RedisStatus = "RUNNING"
        }
    } else {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis进程未运行" -ForegroundColor Red
        $RedisStatus = "PROCESS_NOT_FOUND"
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis端口 $RedisPort 未监听" -ForegroundColor Red
    $RedisStatus = "PORT_NOT_LISTENING"
}

# ========================================
# Spring Boot应用状态检查
# 检查项：端口占用、Java进程存在、健康检查端点
# ========================================

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 正在检查Spring Boot应用状态..." -ForegroundColor Yellow

if (Test-Port -Port $AppPort) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] 应用端口 $AppPort 正在监听" -ForegroundColor Green

    # 检查Java进程（Spring Boot应用）
    $javaProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcess) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] Java进程正在运行" -ForegroundColor Green

        # 检查健康检查端点
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:$AppPort/actuator/health" -TimeoutSec 30 -ErrorAction Stop
            if ($response.status -eq "UP") {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [OK] 应用健康检查通过" -ForegroundColor Green
                $AppStatus = "RUNNING"
            } else {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] 应用健康检查失败，状态: $($response.status)" -ForegroundColor Yellow
                $AppStatus = "HEALTH_CHECK_FAILED"
            }
        } catch {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] 应用健康检查超时或失败: $($_.Exception.Message)" -ForegroundColor Yellow
            $AppStatus = "HEALTH_CHECK_FAILED"
        }
    } else {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Java进程未运行" -ForegroundColor Red
        $AppStatus = "PROCESS_NOT_FOUND"
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] 应用端口 $AppPort 未监听" -ForegroundColor Red
    $AppStatus = "PORT_NOT_LISTENING"
}

# ========================================
# 综合状态评估和报告
# ========================================

Write-Host ""
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 系统状态检查报告" -ForegroundColor Yellow
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL服务状态: $MySQLStatus" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Redis服务状态: $RedisStatus" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 应用服务状态: $AppStatus" -ForegroundColor Cyan

# 判断整体状态
if ($MySQLStatus -eq "RUNNING" -and $RedisStatus -eq "RUNNING" -and $AppStatus -eq "RUNNING") {
    $OverallStatus = "ALL_RUNNING"
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [SUCCESS] 所有服务运行正常" -ForegroundColor Green
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 访问地址: http://localhost:$AppPort" -ForegroundColor Green
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 健康检查: http://localhost:$AppPort/actuator/health" -ForegroundColor Green
} else {
    $OverallStatus = "PARTIAL_RUNNING"
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] 部分服务存在问题，请检查上述状态报告" -ForegroundColor Yellow
}

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 整体状态: $OverallStatus" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 检查完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan

# 根据整体状态设置退出码
if ($OverallStatus -eq "ALL_RUNNING") {
    exit 0
} else {
    exit 1
}
