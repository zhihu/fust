#!/bin/bash

service_dir=$1
arg1=$2
arg2=$3

# copy mod libs to lib
cp mod_lib/*.jar lib/

if [ "${service_dir}" = "${service_dir#/}" ]; then
  # 本地运行，非绝对路径，使用当前路径补全
  service_dir=`pwd`/$service_dir
fi

cd $service_dir


# spring boot launcher
launcher="org.springframework.boot.loader.launch.JarLauncher"

# Add opens
ADD_OPEN="--add-opens java.base/java.lang=ALL-UNNAMED"

# XMX and XMS
XMS=512
XMX=1024

# run it
exec java ${ADD_OPEN} -Xms"$XMS"  -Xmx"$XMX"m ${launcher} ${arg1} ${arg2}
