@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ========================================
REM Flyway 数据库迁移脚本
REM ========================================
REM 用法:
REM   migrate.cmd         - 执行 Flyway 迁移
REM ========================================

REM 项目根目录
cd /d "%~dp0.."

echo ========================================
echo 执行 Flyway 数据库迁移
echo ========================================

mvn flyway:migrate

:end
endlocal
