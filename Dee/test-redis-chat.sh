#!/bin/bash

# Redis 聊天记录过期管理测试脚本

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Redis 聊天记录过期管理测试"
echo "=========================================="

# 测试 1: 短期过期测试（60秒）
echo ""
echo "测试 1: 发送消息并设置 60 秒过期"
CONV_ID="test-$(date +%s)"
echo "对话ID: $CONV_ID"

echo "发送第一条消息..."
curl -s "${BASE_URL}/chat/push?message=你好&conversationId=${CONV_ID}&expireSeconds=60"
echo ""

echo "发送第二条消息..."
curl -s "${BASE_URL}/chat/push?message=介绍一下Java&conversationId=${CONV_ID}&expireSeconds=60"
echo ""

echo "发送第三条消息..."
curl -s "${BASE_URL}/chat/push?message=谢谢&conversationId=${CONV_ID}&expireSeconds=60"
echo ""

echo "✓ 消息已保存到 Redis，60秒后将自动持久化"
echo ""

# 测试 2: 手动持久化测试
echo "=========================================="
echo "测试 2: 手动触发持久化"
CONV_ID_2="manual-$(date +%s)"
echo "对话ID: $CONV_ID_2"

echo "发送几条测试消息..."
curl -s "${BASE_URL}/chat/push?message=测试消息1&conversationId=${CONV_ID_2}&expireSeconds=3600" > /dev/null
curl -s "${BASE_URL}/chat/push?message=测试消息2&conversationId=${CONV_ID_2}&expireSeconds=3600" > /dev/null
curl -s "${BASE_URL}/chat/push?message=测试消息3&conversationId=${CONV_ID_2}&expireSeconds=3600" > /dev/null

echo "手动触发持久化..."
RESULT=$(curl -s -X POST "${BASE_URL}/chat/persist?conversationId=${CONV_ID_2}")
echo "结果: $RESULT"
echo ""

# 测试 3: 查询已持久化的记录
echo "=========================================="
echo "测试 3: 验证数据库记录"
echo "请检查数据库表："
echo "  - chat_record: 应包含对话记录"
echo "  - chat_record_zip: 应包含对话摘要"
echo ""

echo "=========================================="
echo "测试完成！"
echo ""
echo "提示："
echo "1. 测试1的对话将在60秒后自动持久化"
echo "2. 测试2的对话已立即持久化"
echo "3. 请查看应用日志确认持久化过程"
echo "4. 检查数据库验证数据是否正确保存"
echo "=========================================="
