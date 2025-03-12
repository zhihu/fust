#!/bin/bash

service_dir=$1
arg1=$2
arg2=$3

# 本地运行
cp mod_lib/*.jar lib/

if [ "${service_dir}" = "${service_dir#/}" ]; then
  # 本地运行，非绝对路径，使用当前路径补全
  service_dir=`pwd`/$service_dir
fi

cd $service_dir

DOCKER_MEM_FILE='/sys/fs/cgroup/memory/memory.limit_in_bytes'
launcher="org.springframework.boot.loader.JarLauncher"

# add open for jdk11
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "java version: ${java_version}"
ADD_OPEN="--add-opens java.base/java.lang=ALL-UNNAMED"

if [ -e ${DOCKER_MEM_FILE} ]; then
  MEM=$(($(cat "$DOCKER_MEM_FILE") / 1048576))
  XMX=$((MEM / 2))
  XMS=$((XMX / 4))
  exec java ${ADD_OPEN} -Xmx"$XMX"m -Xms"$XMS"m ${launcher} ${arg1} ${arg2}
else
  exec java ${ADD_OPEN} ${launcher} ${arg1} ${arg2}
fi
