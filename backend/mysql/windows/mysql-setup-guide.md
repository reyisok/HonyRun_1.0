# MySQL Windows 手动安装指南

## 下载MySQL

1. 访问MySQL官方下载页面：https://dev.mysql.com/downloads/mysql/ <mcreference link="https://dev.mysql.com/downloads/mysql/" index="1">1</mcreference>

2. 选择操作系统：Microsoft Windows

3. 选择版本：Windows (x86, 64-bit), ZIP Archive

4. 点击下载按钮，选择"No thanks, just start my download"

5. 将下载的文件重命名为：`mysql-9.4.0-winx64.zip`

6. 将文件放置到：`d:\honyrun_mv20251013\HonyRun\backend\mysql\windows\`

## 自动安装

下载完成后，运行安装脚本：

```powershell
cd d:\honyrun_mv20251013\HonyRun\backend\mysql\windows
.\install-mysql.ps1
```

## 配置说明

- **端口**: 8906 (符合项目端口规范 8901-8910)
- **用户名**: honyrunMysql
- **密码**: honyrun@sys
- **数据库**: honyrundb
- **数据目录**: `d:\honyrun_mv20251013\HonyRun\backend\mysql\windows\data`
- **配置文件**: `d:\honyrun_mv20251013\HonyRun\backend\mysql\windows\my.cnf`

## 启动MySQL

```powershell
.\start.bat
```

## 验证安装

```powershell
# 连接到MySQL
.\server\bin\mysql.exe -u honyrunMysql -p -P 8906 -h 127.0.0.1
```
