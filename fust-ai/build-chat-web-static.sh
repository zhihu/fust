#!/bin/bash

# 钱包AI代理前端构建脚本
# 构建前端并部署到 Spring Boot 静态资源目录

echo "🚀 开始构建前端应用..."

# 进入前端目录
cd fust-ai-chat-ui

# 安装依赖（如果需要）
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖..."
    npm install
fi

# 生成静态文件
echo "🔨 生成静态文件..."
npm run generate

# 复制文件到 Spring Boot 静态资源目录
echo "📁 复制文件到 Spring Boot 静态资源目录..."
rm -rf ../fust-ai-chat-web/src/main/resources/static/*
cp -r .output/public/* ../fust-ai-chat-web/src/main/resources/static/

echo "✅ 前端构建完成！"