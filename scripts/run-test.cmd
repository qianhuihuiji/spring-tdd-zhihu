@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ========================================
REM Spring TDD Zhihu Project - Test Runner
REM ========================================
REM Usage:
REM   run-test.cmd                              - Run all tests (exclude @Tag("online"))
REM   run-test.cmd -all                         - Run all tests (include @Tag("online"))
REM   run-test.cmd ClassName                    - Run specified test class (exclude online)
REM   run-test.cmd ClassName -all               - Run specified test class (include online)
REM   run-test.cmd ClassName methodName         - Run specified test method (exclude online)
REM   run-test.cmd ClassName methodName -all    - Run specified test method (include online)
REM ========================================

cd /d "%~dp0.."

set "TEST_CLASS="
set "TEST_METHOD="
set "RUN_MODE=default"

if "%~1"=="-all" goto :run_all_with_online
if "%~2"=="-all" (
    set "TEST_CLASS=%~1"
    set "INCLUDE_ALL=true"
    goto :run_class_with_online
)
if "%~3"=="-all" (
    set "TEST_CLASS=%~1"
    set "TEST_METHOD=%~2"
    set "INCLUDE_ALL=true"
    goto :run_method_with_online
)

if "%~1"=="" goto :run_all
if "%~2"=="" goto :run_class
set "TEST_CLASS=%~1"
set "TEST_METHOD=%~2"
goto :run_method

:run_all
echo ========================================
echo Running all tests (exclude @Tag("online"))
echo ========================================
mvn test -DexcludedGroups=online
goto :end

:run_all_with_online
echo ========================================
echo Running all tests (include @Tag("online"))
echo ========================================
mvn test
goto :end

:run_class
set "TEST_CLASS=%~1"
echo ========================================
echo Running test class: %TEST_CLASS% (exclude @Tag("online"))
echo ========================================
mvn test -Dtest=%TEST_CLASS% -DexcludedGroups=online
goto :end

:run_class_with_online
echo ========================================
echo Running test class: %TEST_CLASS% (include @Tag("online"))
echo ========================================
mvn test -Dtest=%TEST_CLASS%
goto :end

:run_method
echo ========================================
echo Running test method: %TEST_CLASS%#%TEST_METHOD% (exclude @Tag("online"))
echo ========================================
mvn test -Dtest=%TEST_CLASS%#%TEST_METHOD% -DexcludedGroups=online
goto :end

:run_method_with_online
echo ========================================
echo Running test method: %TEST_CLASS%#%TEST_METHOD% (include @Tag("online"))
echo ========================================
mvn test -Dtest=%TEST_CLASS%#%TEST_METHOD%
goto :end

:end
endlocal
