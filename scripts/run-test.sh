#!/bin/bash

# ========================================
# Spring TDD 知乎项目 - 测试运行脚本
# ========================================
# 用法:
#   ./run-test.sh                              - 运行所有测试（排除@Tag("online")）
#   ./run-test.sh -all                         - 运行所有测试（包含@Tag("online")）
#   ./run-test.sh ClassName                    - 运行指定测试类（排除 online）
#   ./run-test.sh ClassName -all               - 运行指定测试类（包含 online）
#   ./run-test.sh ClassName methodName         - 运行指定测试方法（排除 online）
#   ./run-test.sh ClassName methodName -all    - 运行指定测试方法（包含 online）
# ========================================

# 项目根目录（脚本所在目录的父目录）
cd "$(dirname "$0")/.."

# 解析参数
TEST_CLASS=""
TEST_METHOD=""

# 检查第一个参数是否是 -all
if [ "$1" == "-all" ]; then
    echo "========================================"
    echo "运行所有测试（包含 @Tag(\"online\")）"
    echo "========================================"
    mvn test
    exit 0
fi

# 检查是否是 ClassName -all 格式
if [ "$2" == "-all" ]; then
    TEST_CLASS="$1"
    echo "========================================"
    echo "运行测试类：$TEST_CLASS（包含 @Tag(\"online\")）"
    echo "========================================"
    mvn test -Dtest="$TEST_CLASS"
    exit 0
fi

# 检查是否是 ClassName methodName -all 格式
if [ "$3" == "-all" ]; then
    TEST_CLASS="$1"
    TEST_METHOD="$2"
    echo "========================================"
    echo "运行测试方法：${TEST_CLASS}#${TEST_METHOD}（包含 @Tag(\"online\")）"
    echo "========================================"
    mvn test -Dtest="${TEST_CLASS}#${TEST_METHOD}"
    exit 0
fi

# 默认模式（排除 online）
if [ $# -eq 0 ]; then
    echo "========================================"
    echo "运行所有测试（排除 @Tag(\"online\")）"
    echo "========================================"
    mvn test -DexcludedGroups=online
    exit 0
fi

if [ $# -eq 1 ]; then
    TEST_CLASS="$1"
    echo "========================================"
    echo "运行测试类：$TEST_CLASS（排除 @Tag(\"online\")）"
    echo "========================================"
    mvn test -Dtest="$TEST_CLASS" -DexcludedGroups=online
    exit 0
fi

if [ $# -eq 2 ]; then
    TEST_CLASS="$1"
    TEST_METHOD="$2"
    echo "========================================"
    echo "运行测试方法：${TEST_CLASS}#${TEST_METHOD}（排除 @Tag(\"online\")）"
    echo "========================================"
    mvn test -Dtest="${TEST_CLASS}#${TEST_METHOD}" -DexcludedGroups=online
    exit 0
fi

echo "用法错误！请使用以下格式之一："
echo "  ./run-test.sh"
echo "  ./run-test.sh -all"
echo "  ./run-test.sh ClassName"
echo "  ./run-test.sh ClassName -all"
echo "  ./run-test.sh ClassName methodName"
echo "  ./run-test.sh ClassName methodName -all"
