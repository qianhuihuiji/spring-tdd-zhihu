@echo off
setlocal EnableDelayedExpansion

REM ========================================
REM Spring TDD 知乎项目 - 测试运行脚本
REM ========================================
REM 用法:
REM   run-test.cmd                              - 运行所有测试（排除@Tag("online")）
REM   run-test.cmd -all                         - 运行所有测试（包含@Tag("online")）
REM   run-test.cmd ClassName                    - 运行指定测试类（排除 online）
REM   run-test.cmd ClassName -all               - 运行指定测试类（包含 online）
REM   run-test.cmd ClassName methodName         - 运行指定测试方法（排除 online）
REM   run-test.cmd ClassName methodName -all    - 运行指定测试方法（包含 online）
REM ========================================

REM 项目根目录
cd /d "%~dp0.."

REM 解析参数
set "TEST_CLASS="
set "TEST_METHOD="
set "RUN_MODE=default"

REM 检查第一个参数是否是 -all
if "%~1"=="-all" goto :run_all_with_online

REM 检查是否是 ClassName -all 格式
if "%~2"=="-all" (
    set "TEST_CLASS=%~1"
    set "INCLUDE_ALL=true"
    goto :run_class_with_online
)

REM 检查是否是 ClassName methodName -all 格式
if "%~3"=="-all" (
    set "TEST_CLASS=%~1"
    set "TEST_METHOD=%~2"
    set "INCLUDE_ALL=true"
    goto :run_method_with_online
)

REM 普通模式（排除 online）
if "%~1"=="" goto :run_all
if "%~2"=="" goto :run_class
set "TEST_CLASS=%~1"
set "TEST_METHOD=%~2"
goto :run_method

REM ========================================
REM 所有测试（排除 online）- 默认模式
REM ========================================
:run_all
echo ========================================
echo 运行所有测试（排除 @Tag("online")）
echo ========================================
mvn test -DexcludedGroups=online
goto :end

REM ========================================
REM 所有测试（包含 online）
REM ========================================
:run_all_with_online
echo ========================================
echo 运行所有测试（包含 @Tag("online")）
echo ========================================
mvn test
goto :end

REM ========================================
REM 单个测试类（排除 online）- 默认模式
REM ========================================
:run_class
set "TEST_CLASS=%~1"
echo ========================================
echo 运行测试类：%TEST_CLASS%（排除 @Tag("online")）
echo ========================================
mvn test -Dtest=%TEST_CLASS% -DexcludedGroups=online
goto :end

REM ========================================
REM 单个测试类（包含 online）
REM ========================================
:run_class_with_online
echo ========================================
echo 运行测试类：%TEST_CLASS%（包含 @Tag("online")）
echo ========================================
mvn test -Dtest=%TEST_CLASS%
goto :end

REM ========================================
REM 单个测试方法（排除 online）- 默认模式
REM ========================================
:run_method
echo ========================================
echo 运行测试方法：%TEST_CLASS%#%TEST_METHOD%（排除 @Tag("online")）
echo ========================================
mvn test -Dtest=%TEST_CLASS%#%TEST_METHOD% -DexcludedGroups=online
goto :end

REM ========================================
REM 单个测试方法（包含 online）
REM ========================================
:run_method_with_online
echo ========================================
echo 运行测试方法：%TEST_CLASS%#%TEST_METHOD%（包含 @Tag("online")）
echo ========================================
mvn test -Dtest=%TEST_CLASS%#%TEST_METHOD%
goto :end

:end
endlocal
