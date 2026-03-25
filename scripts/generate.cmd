@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ========================================
REM MyBatis Generator 代码生成脚本
REM ========================================
REM 用法:
REM   generate.cmd        - 运行 Generator 生成 MBG 代码
REM ========================================

REM 项目根目录
cd /d "%~dp0.."

echo ========================================
echo 运行 MyBatis Generator 生成代码
echo ========================================

mvn exec:java -Dexec.mainClass="com.nofirst.spring.tdd.zhihu.mbg.Generator" -Dexec.classpathScope=compile

:end
endlocal
