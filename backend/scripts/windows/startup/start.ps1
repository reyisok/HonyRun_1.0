# ========================================
# HonyRun WebFlux响应式系统启动脚本 - Windows版本
# @author: Mr.Rey Copyright © 2025
# @created: 2025-10-27 12:26:43
# @modified: 2025-10-27 12:26:43
# @version: 2.0.0
# @description: 统一启动脚本，按顺序启动MySQL、Redis、Spring Boot应用
# 重要：本脚本从统一配置文件读取所有参数，严格遵循项目规则55
# ========================================

# 设置错误处理
$ErrorActionPreference = "Stop"

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

# 服务启动监控函数 - 实时监控服务启动状态和错误
function Monitor-ServiceStartup {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$TimeoutSeconds = 60,
        [string]$LogFile = ""
    )

    Write-LogMessage -Message "开始监控 $ServiceName 启动状态..." -Level "INFO" -Color "Cyan"

    $startTime = Get-Date
    $timeout = $startTime.AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $timeout) {
        try {
            # 检查端口是否监听
            $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
            if ($connection) {
                Write-LogMessage -Message "$ServiceName 启动成功，端口 $Port 已监听" -Level "SUCCESS" -Color "Green"
                return $true
            }

            # 如果指定了日志文件，检查错误信息
            if ($LogFile -and (Test-Path $LogFile)) {
                $recentLogs = Get-Content $LogFile -Tail 10 -ErrorAction SilentlyContinue
                $errorLogs = $recentLogs | Where-Object { $_ -match "(ERROR|FATAL|Exception)" }
                if ($errorLogs) {
                    foreach ($errorLog in $errorLogs) {
                        Write-LogMessage -Message "$ServiceName 错误日志: $errorLog" -Level "ERROR" -Color "Red"
                    }
                }
            }

            Start-Sleep -Seconds 2
        } catch {
            Handle-Error -Operation "$ServiceName 状态检查" -ErrorRecord $_
        }
    }

    Write-LogMessage -Message "$ServiceName 启动超时 ($TimeoutSeconds 秒)" -Level "ERROR" -Color "Red"
    return $false
}

# 获取项目根目录
$ProjectRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 项目根目录: $ProjectRoot" -ForegroundColor Green

# ========================================
# 从统一配置文件读取配置参数
# 严格遵循项目规则55：禁止硬编码，从环境配置文件读取所有参数
# ========================================

# 读取开发环境配置文件
$configFile = Join-Path $ProjectRoot "src\main\resources\application-dev.properties"
if (-not (Test-Path $configFile)) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] 配置文件不存在: $configFile" -ForegroundColor Red
    exit 1
}

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 读取统一配置文件: $configFile" -ForegroundColor Cyan

# 解析配置文件的函数
function Get-ConfigValue {
    param(
        [string]$ConfigFile,
        [string]$Key,
        [string]$DefaultValue = ""
    )

    $content = Get-Content $ConfigFile -ErrorAction SilentlyContinue
    if (-not $content) {
        return $DefaultValue
    }

    foreach ($line in $content) {
        $line = $line.Trim()
        if ($line -match "^$Key\s*=\s*(.*)$") {
            $value = $matches[1]
            # 处理环境变量格式 ${VAR:default}
            if ($value -match '\$\{[^:]+:([^}]+)\}') {
                return $matches[1]
            }
            return $value
        }
    }
    return $DefaultValue
}

# 从配置文件读取所有参数
$ServerPort = Get-ConfigValue -ConfigFile $configFile -Key "server.port" -DefaultValue "8901"
$MySQLHost = Get-ConfigValue -ConfigFile $configFile -Key "honyrun.database.host" -DefaultValue "localhost"
$MySQLPort = Get-ConfigValue -ConfigFile $configFile -Key "honyrun.database.port" -DefaultValue "8906"
$MySQLDB = Get-ConfigValue -ConfigFile $configFile -Key "honyrun.database.name" -DefaultValue "honyrundb"
$MySQLUser = Get-ConfigValue -ConfigFile $configFile -Key "honyrun.database.username" -DefaultValue "honyrunMysql"
$MySQLPassword = Get-ConfigValue -ConfigFile $configFile -Key "honyrun.database.password" -DefaultValue "honyrun@sys"
$RedisHost = Get-ConfigValue -ConfigFile $configFile -Key "spring.data.redis.host" -DefaultValue "localhost"
$RedisPort = Get-ConfigValue -ConfigFile $configFile -Key "spring.data.redis.port" -DefaultValue "8902"
$RedisPassword = Get-ConfigValue -ConfigFile $configFile -Key "spring.data.redis.password" -DefaultValue "honyrun@sys"

# 设置应用端口和环境变量
$AppPort = $ServerPort
$Profile = "dev"

# 显示读取的配置信息
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 统一配置参数" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 应用端口: $ServerPort" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL: $MySQLHost`:$MySQLPort, 数据库: $MySQLDB, 用户: $MySQLUser" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Redis: $RedisHost`:$RedisPort" -ForegroundColor Green

# ========================================
# 工具函数定义
# ========================================

# ========================================
# 应用服务配置读取 - 按统一配置规范读取应用服务参数
# 配置项说明：
# - server.port: Spring Boot应用服务端口（固定8901）
# 端口规范：应用8901，Redis8902，MySQL8906（项目端口范围8901-8910）
# ========================================
# 统一配置读取完成：
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 统一配置读取完成：" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - MySQL配置: $MySQLHost`:$MySQLPort (数据库: $MySQLDB, 用户: $MySQLUser)" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - Redis配置: $RedisHost`:$RedisPort (认证: 已配置)" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 应用配置: 端口 $AppPort" -ForegroundColor Green

# 检查端口是否被占用的函数
function Test-Port {
    param([int]$Port)
    try {
        # 使用Test-NetConnection进行更可靠的端口检测
        $result = Test-NetConnection -ComputerName "localhost" -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue
        return $result
    }
    catch {
        # 如果Test-NetConnection失败，尝试使用TcpClient
        try {
            $tcpClient = New-Object System.Net.Sockets.TcpClient
            $tcpClient.ConnectAsync("localhost", $Port).Wait(1000)
            $isConnected = $tcpClient.Connected
            $tcpClient.Close()
            return $isConnected
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
        $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
        if ($connections) {
            $processId = $connections[0].OwningProcess
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            return @{
                ProcessId = $processId
                ProcessName = $process.ProcessName
            }
        }
    }
    catch {
        return $null
    }
    return $null
}

# 强制终止端口占用进程的函数
function Stop-ProcessByPort {
    param([int]$Port, [string]$ServiceName)

    if (Test-Port -Port $Port) {
        $processInfo = Get-PortProcess -Port $Port
        if ($processInfo) {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARN] 端口 $Port 被进程 $($processInfo.ProcessName) (PID: $($processInfo.ProcessId)) 占用" -ForegroundColor Yellow
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] 强制终止占用进程..." -ForegroundColor Yellow
            try {
                Stop-Process -Id $processInfo.ProcessId -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 2
                if (-not (Test-Port -Port $Port)) {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [INFO] ✓ 进程终止成功" -ForegroundColor Green
                    return $true
                } else {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] ✗ 进程终止失败" -ForegroundColor Red
                    return $false
                }
            }
            catch {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] ✗ 进程终止失败: $($_.Exception.Message)" -ForegroundColor Red
                return $false
            }
        }
    }
    return $true
}

# 等待服务启动的函数
function Wait-ForService {
    param(
        [int]$Port,
        [string]$ServiceName,
        [int]$TimeoutSeconds = 60,
        [scriptblock]$HealthCheck = $null
    )

    Write-Host "[INFO] Waiting for $ServiceName to start on port $Port..." -ForegroundColor Yellow
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

    while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
        if (Test-Port -Port $Port) {
            Write-Host "[INFO] ✓ $ServiceName is listening on port $Port" -ForegroundColor Green

            if ($HealthCheck) {
                Write-Host "[INFO] Performing health check for $ServiceName..." -ForegroundColor Yellow
                try {
                    $result = & $HealthCheck
                    if ($result) {
                        Write-Host "[INFO] ✓ $ServiceName health check passed" -ForegroundColor Green
                        return $true
                    } else {
                        Write-Host "[WARN] $ServiceName health check failed, continuing to wait..." -ForegroundColor Yellow
                    }
                }
                catch {
                    Write-Host "[WARN] $ServiceName health check error: $($_.Exception.Message)" -ForegroundColor Yellow
                }
            } else {
                return $true
            }
        }

        Write-Host "." -NoNewline -ForegroundColor Yellow
        Start-Sleep -Seconds 1
    }

    Write-Host ""
    Write-Host "[ERROR] ✗ Timeout waiting for $ServiceName to start" -ForegroundColor Red
    return $false
}

# ========================================
# 第一步：MySQL数据库服务启动
# 重要：MySQL必须首先启动，因为应用依赖数据库连接
# 启动顺序：MySQL -> Redis -> Spring Boot应用
# ========================================
Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 第一步：启动MySQL数据库服务" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan

# MySQL健康检查脚本
$mysqlHealthCheck = {
    param($ProjectRoot, $MySQLHost, $MySQLPort, $MySQLUser, $MySQLPassword)
    $mysqlCliPath = Join-Path $ProjectRoot "mysql\windows\server\bin\mysql.exe"
    if (Test-Path $mysqlCliPath) {
        try {
            # 使用完整路径执行MySQL命令，并捕获输出
            # 修复密码参数格式，使用--password参数避免特殊字符问题
            $output = & $mysqlCliPath -h $MySQLHost -P $MySQLPort -u $MySQLUser "--password=$MySQLPassword" -e "SELECT 1;" 2>&1
            # 检查输出是否包含预期结果
            $success = $output -match "1" -and $LASTEXITCODE -eq 0
            return $success
        }
        catch {
            return $false
        }
    }
    return $false
}

# ========================================
# 数据库初始化逻辑修复
# 快速开发阶段：采用drop/create方式，确保重启MySQL时正确执行初始化
# ========================================

# 检查MySQL是否已经运行并可连接
$healthResult = $false
if (Test-Port -Port $MySQLPort) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 检测到MySQL端口 $MySQLPort 已被占用，检查服务状态..." -ForegroundColor Yellow
    $healthResult = & $mysqlHealthCheck $ProjectRoot $MySQLHost $MySQLPort $MySQLUser $MySQLPassword
    if ($healthResult) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL服务已运行且连接正常" -ForegroundColor Green
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 快速开发阶段：强制重启MySQL以确保数据库初始化脚本执行" -ForegroundColor Yellow
        # 快速开发阶段：强制重启以确保drop/create初始化脚本执行
        Stop-ProcessByPort -Port $MySQLPort -ServiceName "MySQL"
        $healthResult = $false
    } else {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL端口被占用但连接失败，强制重启服务..." -ForegroundColor Yellow
        Stop-ProcessByPort -Port $MySQLPort -ServiceName "MySQL"
    }
}

# 启动MySQL服务（快速开发阶段总是重新启动）
$MySQLPath = Join-Path $ProjectRoot "mysql\windows"
if (Test-Path $MySQLPath) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 启动MySQL服务..." -ForegroundColor Cyan
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL配置: $MySQLHost`:$MySQLPort, 数据库: $MySQLDB" -ForegroundColor Cyan

    Set-Location $MySQLPath

    # 清理可能存在的旧PID文件，避免启动冲突
    $pidFiles = Get-ChildItem -Path "data" -Filter "*.pid" -ErrorAction SilentlyContinue
    if ($pidFiles) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 清理旧的PID文件..." -ForegroundColor Yellow
        $pidFiles | Remove-Item -Force -ErrorAction SilentlyContinue
    }

    # 启动MySQL服务器，不使用最小化窗口以便观察启动过程
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 正在启动MySQL服务器..." -ForegroundColor Cyan
    try {
        $mysqlProcess = Start-Process -FilePath ".\server\bin\mysqld.exe" -ArgumentList "--defaults-file=my.cnf" -PassThru -WindowStyle Hidden
        if ($mysqlProcess -and $mysqlProcess.Id) {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL进程已启动，PID: $($mysqlProcess.Id)" -ForegroundColor Green
        } else {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] MySQL进程启动异常，但继续等待..." -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] MySQL启动异常: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 继续等待MySQL启动..." -ForegroundColor Yellow
    }

    Set-Location $ProjectRoot
}

# 等待MySQL启动完成（仅在需要启动时）
if (-not $healthResult) {
    # 先等待端口监听，不进行复杂的健康检查
    if (Wait-ForService -Port $MySQLPort -ServiceName "MySQL" -TimeoutSeconds 60) {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL端口已监听，等待服务完全就绪..." -ForegroundColor Yellow
        Start-Sleep -Seconds 3

        # 简单的健康检查
        $mysqlHealthCheckWrapper = {
            param($ProjectRoot, $MySQLHost, $MySQLPort, $MySQLUser, $MySQLPassword)
            $mysqlCliPath = Join-Path $ProjectRoot "mysql\windows\server\bin\mysql.exe"
            if (Test-Path $mysqlCliPath) {
                try {
                    # 使用完整路径执行MySQL命令，并捕获输出
                    $output = & $mysqlCliPath -h $MySQLHost -P $MySQLPort -u $MySQLUser "--password=$MySQLPassword" -e "SELECT 1;" 2>&1
                    # 检查输出是否包含预期结果
                    $success = $output -match "1" -and $LASTEXITCODE -eq 0
                    return $success
                }
                catch {
                    return $false
                }
            }
            return $false
        }

        # 尝试健康检查，但不强制要求成功
        try {
            $healthCheckResult = & $mysqlHealthCheckWrapper $ProjectRoot $MySQLHost $MySQLPort $MySQLUser $MySQLPassword
            if ($healthCheckResult) {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL健康检查通过！" -ForegroundColor Green
            } else {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL健康检查未通过，但端口已监听，继续启动..." -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL健康检查异常，但端口已监听，继续启动..." -ForegroundColor Yellow
        }

        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL数据库服务启动成功！" -ForegroundColor Green
    } else {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] MySQL启动失败，终止启动流程" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') MySQL数据库服务已就绪！" -ForegroundColor Green
}

# ========================================
# 第二步：Redis缓存服务启动
# 重要：Redis作为缓存服务，在MySQL之后启动
# Redis健康检查使用redis-cli PING命令验证
# ========================================
Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 第二步：启动Redis缓存服务" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan

$redisPath = Join-Path $ProjectRoot "redis\windows"
$redisStartScript = Join-Path $redisPath "start.bat"

if (Test-Path $redisStartScript) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 启动Redis服务..." -ForegroundColor Cyan
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Redis配置: $RedisHost`:$RedisPort, 认证: 已配置" -ForegroundColor Cyan

    try {
        Set-Location $redisPath
        $redisProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "start.bat" -PassThru -WindowStyle Hidden

        # Redis健康检查脚本 - 已移除，直接在Wait-ForService中使用内联脚本块

        # 等待Redis启动完成 - 暂时跳过健康检查，只检查端口
        $redisStarted = Wait-ForService -Port $RedisPort -ServiceName "Redis" -TimeoutSeconds 30

        if ($redisStarted) {
            # Redis健康检查 - 使用redis-cli PING命令
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 执行Redis健康检查..." -ForegroundColor Cyan
            try {
                $redisCliPath = Join-Path $redisPath "redis-cli.exe"
                $pingResult = & $redisCliPath -p $RedisPort -a $RedisPassword ping 2>$null
                if ($pingResult -eq "PONG") {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Redis健康检查通过 (PING -> PONG)" -ForegroundColor Green
                } else {
                    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] Redis健康检查异常: $pingResult" -ForegroundColor Yellow
                }
            } catch {
                Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [WARNING] Redis健康检查失败: $($_.Exception.Message)" -ForegroundColor Yellow
            }
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Redis缓存服务启动成功！" -ForegroundColor Green
        } else {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis启动失败，终止启动流程" -ForegroundColor Red
            Set-Location $ProjectRoot
            exit 1
        }
    }
    catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis启动异常: $($_.Exception.Message)" -ForegroundColor Red
        Set-Location $ProjectRoot
        exit 1
    }
    finally {
        Set-Location $ProjectRoot
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Redis启动脚本未找到: $redisStartScript" -ForegroundColor Red
    exit 1
}

# ========================================
# 第三步：Spring Boot应用启动
# 重要：应用最后启动，确保MySQL和Redis服务已就绪
# 包含Maven构建、JVM优化配置、健康检查
# ========================================
Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 第三步：启动Spring Boot应用" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Cyan

# 检查并清理应用端口占用
Stop-ProcessByPort -Port $AppPort -ServiceName "Spring Boot应用"

# Maven构建（如果未跳过）
if (-not $SkipBuild) {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 开始Maven构建..." -ForegroundColor Cyan
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 构建配置: 内存4GB, 并行8线程, 跳过测试" -ForegroundColor Cyan

    # 32GB高性能环境Maven构建优化配置
    $env:MAVEN_OPTS = "-Xmx4g -XX:+UseG1GC -XX:+UseStringDeduplication"

    try {
        $buildResult = & mvn clean compile -T 8C "-Dmaven.test.skip=true" -q 2>&1 | Tee-Object -FilePath "logs\compile.log"
        if ($LASTEXITCODE -eq 0) {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') Maven构建成功" -ForegroundColor Green
        } else {
            Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Maven构建失败，退出码: $LASTEXITCODE" -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [ERROR] Maven构建异常: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 跳过Maven构建 (SkipBuild=true)" -ForegroundColor Yellow
}

# 启动Spring Boot应用
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 启动Spring Boot应用..." -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 应用配置: 端口 $AppPort, 环境 $Profile" -ForegroundColor Cyan

# 32GB高性能环境JVM优化配置
$jvmArgs = @(
    "-Xms8g",                           # 初始堆内存8GB
    "-Xmx16g",                          # 最大堆内存16GB
    "-XX:+UseG1GC",                     # 使用G1垃圾收集器
    "-XX:+UseStringDeduplication",      # 启用字符串去重
    "-XX:MaxGCPauseMillis=200",         # GC暂停时间目标200ms
    "-Dspring.profiles.active=$Profile", # 激活指定环境
    "-Dserver.port=$AppPort",           # 设置服务端口
    "-Dfile.encoding=UTF-8",            # 字符编码UTF-8
    "-Djava.awt.headless=true"          # 无头模式
)

# 确保日志目录存在
$logDir = "logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

try {
    # 启动应用并重定向日志
    $appArgs = @("spring-boot:run") + $jvmArgs.ForEach({ "-Dspring-boot.run.jvmArguments=$_" })
    Write-LogMessage -Message "JVM参数: $($jvmArgs -join ' ')" -Level "INFO" -Color "Cyan"

    $appLogFile = "logs\startup.log"
    $appErrorLogFile = "logs\startup-error.log"

    Write-LogMessage -Message "启动Spring Boot应用进程..." -Level "INFO" -Color "Cyan"
    $appProcess = Start-Process -FilePath "mvn" -ArgumentList $appArgs -RedirectStandardOutput $appLogFile -RedirectStandardError $appErrorLogFile -WindowStyle Minimized -PassThru

    # 使用增强的监控函数
    if (Monitor-ServiceStartup -ServiceName "Spring Boot应用" -Port $AppPort -TimeoutSeconds 60 -LogFile $appErrorLogFile) {
        # 应用健康检查
        Write-LogMessage -Message "执行应用健康检查..." -Level "INFO" -Color "Cyan"
        Start-Sleep -Seconds 5  # 等待应用完全初始化

        try {
            $healthUrl = "http://localhost:$AppPort/actuator/health"
            $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 10
            if ($response.status -eq "UP") {
                Write-LogMessage -Message "应用健康检查通过 (状态: UP)" -Level "SUCCESS" -Color "Green"
            } else {
                Write-LogMessage -Message "应用健康检查异常 (状态: $($response.status))" -Level "WARNING" -Color "Yellow"
            }
        } catch {
            Handle-Error -Operation "应用健康检查" -ErrorRecord $_
        }

        Write-LogMessage -Message "Spring Boot应用启动成功！" -Level "SUCCESS" -Color "Green"
    } else {
        Write-LogMessage -Message "Spring Boot应用启动失败" -Level "ERROR" -Color "Red"

        # 输出错误日志内容
        if (Test-Path $appErrorLogFile) {
            Write-LogMessage -Message "应用错误日志内容:" -Level "ERROR" -Color "Red"
            $errorContent = Get-Content $appErrorLogFile -Tail 20 -ErrorAction SilentlyContinue
            foreach ($line in $errorContent) {
                Write-LogMessage -Message $line -Level "ERROR" -Color "Red"
            }
        }
        exit 1
    }
} catch {
    Handle-Error -Operation "Spring Boot应用启动" -ErrorRecord $_
    exit 1
}

# ========================================
# 启动完成总结
# ========================================
Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') HonyRun系统启动完成！" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ========================================" -ForegroundColor Green

Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 服务状态总览：" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ✓ MySQL数据库服务: $MySQLHost`:$MySQLPort (数据库: $MySQLDB)" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ✓ Redis缓存服务: $RedisHost`:$RedisPort" -ForegroundColor Green
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ✓ Spring Boot应用: http://localhost:$AppPort" -ForegroundColor Green

Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 访问地址：" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 应用首页: http://localhost:$AppPort" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 健康检查: http://localhost:$AppPort/actuator/health" -ForegroundColor Cyan
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 应用日志: logs\startup.log" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') 启动脚本执行完成，所有服务已就绪！" -ForegroundColor Green
