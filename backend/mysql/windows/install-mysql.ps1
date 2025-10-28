# ========================================
# MySQL 9.4.0 Windows 自动下载安装脚本
# 32GB内存环境优化版本
# @author: Mr.Rey Copyright © 2025@created: 2025-06-28 18:26:00
# @modified: 2025-01-20 15:45:00@version: 1.0.3
# ========================================

param(
    [string]$Version = "9.4.0"
)

# 设置项目根目录
$PROJECT_ROOT = "d:\honyrun_mv20251013\HonyRun"
$MYSQL_DIR = "$PROJECT_ROOT\backend\mysql\windows"
$MYSQL_SERVER_DIR = "$MYSQL_DIR\server"
$MYSQL_DATA_DIR = "$MYSQL_DIR\data"
$MYSQL_TEMP_DIR = "$MYSQL_DIR\temp"
$MYSQL_LOGS_DIR = "$MYSQL_DIR\logs"

Write-Host "========================================" -ForegroundColor Green
Write-Host "MySQL $Version Windows 自动安装脚本" -ForegroundColor Green
Write-Host "32GB内存环境优化版本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# MySQL版本配置
$MYSQL_VERSION = "9.4.0"
$MYSQL_ZIP = "$MYSQL_DIR\mysql-$MYSQL_VERSION-winx64.zip"

Write-Host "检查MySQL安装包..." -ForegroundColor Yellow

# 检查是否已有MySQL安装包
if (-not (Test-Path $MYSQL_ZIP)) {
    Write-Host "未找到MySQL安装包，请手动下载：" -ForegroundColor Red
    Write-Host "1. 访问: https://dev.mysql.com/downloads/mysql/" -ForegroundColor Cyan
    Write-Host "2. 选择 'Windows (x86, 64-bit), ZIP Archive'" -ForegroundColor Cyan
    Write-Host "3. 下载后将文件重命名为: mysql-$MYSQL_VERSION-winx64.zip" -ForegroundColor Cyan
    Write-Host "4. 将文件放置到: $MYSQL_DIR" -ForegroundColor Cyan
    Write-Host "5. 重新运行此脚本" -ForegroundColor Cyan
    exit 1
}

Write-Host "找到MySQL安装包: $MYSQL_ZIP" -ForegroundColor Green

try {

    # 解压MySQL到server目录
    Write-Host "正在解压MySQL..." -ForegroundColor Yellow
    Expand-Archive -Path $MYSQL_ZIP -DestinationPath $MYSQL_SERVER_DIR -Force

    # 移动文件到正确位置
    $ExtractedDir = "$MYSQL_SERVER_DIR\mysql-$MYSQL_VERSION-winx64"
    if (Test-Path $ExtractedDir) {
        Get-ChildItem -Path $ExtractedDir | Move-Item -Destination $MYSQL_SERVER_DIR -Force
        Remove-Item -Path $ExtractedDir -Force
    }

    Write-Host "MySQL解压完成" -ForegroundColor Green

    # 删除下载的zip文件
    Remove-Item -Path $MYSQL_ZIP -Force

    # 初始化MySQL数据目录
    Write-Host "正在初始化MySQL数据目录..." -ForegroundColor Yellow
    $MySQLBin = "$MYSQL_SERVER_DIR\bin\mysqld.exe"

    if (Test-Path $MySQLBin) {
        # 初始化数据目录
        & $MySQLBin --initialize-insecure --basedir="$MYSQL_SERVER_DIR" --datadir="$MYSQL_DATA_DIR"
        Write-Host "MySQL数据目录初始化完成" -ForegroundColor Green

        Write-Host "========================================" -ForegroundColor Green
        Write-Host "MySQL安装完成！" -ForegroundColor Green
        Write-Host "服务器目录: $MYSQL_SERVER_DIR" -ForegroundColor Cyan
        Write-Host "数据目录: $MYSQL_DATA_DIR" -ForegroundColor Cyan
        Write-Host "配置文件: $MYSQL_DIR\my.cnf" -ForegroundColor Cyan
        Write-Host "端口: 8906" -ForegroundColor Cyan
        Write-Host "用户名: honyrunMysql" -ForegroundColor Cyan
        Write-Host "数据库: honyrundb" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Green
    } else {
        Write-Host "错误: 找不到mysqld.exe文件" -ForegroundColor Red
        exit 1
    }

} catch {
    Write-Host "安装过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
