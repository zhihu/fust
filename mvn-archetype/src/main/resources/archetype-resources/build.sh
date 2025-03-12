#!/bin/bash

# mvn build

echo "start process services ..."
# 临时文件
jars_file="/tmp/build-jars.txt"
non_app="/tmp/non-app.txt"

# 根目录
root_dir=$(pwd)

# mod 依赖，容易变的依赖
module_dir="${root_dir}/mod_lib"

# 通用依赖，不易变的依赖
lib_dir="${root_dir}/lib"

# 服务目录
services_dir="$(pwd)/services"

# 清理
rm -rf ${module_dir}
rm -rf ${lib_dir}
rm -rf ${services_dir}

# enter 服务目录
mkdir ${services_dir}
cd ${services_dir}

# 查找所有 jar
find ${root_dir} -name "*.jar" >${jars_file}

# 解压分层依赖
awk '{print $1}' ${jars_file} | xargs -I {} java -Djarmode=layertools -jar {} extract 2>${non_app}

# 合并分层依赖
mkdir -p ${lib_dir}
mkdir -p ${module_dir}

cp dependencies/BOOT-INF/lib/*.jar ${lib_dir}/

if [ -d "snapshot-dependencies/BOOT-INF/lib/" ]; then
  cp snapshot-dependencies/BOOT-INF/lib/*.jar ${lib_dir}/
fi

# 合并 application 依赖到 mod 依赖
if [ -d "application/BOOT-INF/lib/" ]; then
  cp application/BOOT-INF/lib/*.jar ${module_dir}/
fi

# 合并本地模块依赖到 mod 依赖
if [ -d "module-dependencies/BOOT-INF/lib/" ]; then
  cp module-dependencies/BOOT-INF/lib/*.jar ${module_dir}/
fi

# 清理分层
rm -rf application/
rm -rf dependencies/
rm -rf snapshot-dependencies/
rm -rf spring-boot-loader/
rm -rf module-dependencies/

# 创建服务执行目录
cat ${jars_file} | while read line; do #cat命令的输出作为read命令的输入,read读到的值放在line中。line为读取文件行内容的变量
  service_name=$(echo ${line} | awk -F/ '{print $(NF-2)}')
  if grep -q "${service_name}" ${non_app}; then
    echo "non app ${service_name}"
  else
    # 服务目录
    echo "process service ${service_name}"
    service_dir=$(pwd)/${service_name}
    mkdir ${service_dir}
    cd ${service_dir}
    # 解压到服务目录
    unzip -q ${line}
    rm -rf BOOT-INF/lib/*
    # link all lib jars
    cat "${service_dir}/BOOT-INF/classpath.idx" | while read layer_dep_path; do
      if [[ "${layer_dep_path}" == *".jar"* ]]; then
        # 移除双引号并提起 JAR 路径
        lib_path=$(echo $layer_dep_path | tr -d '"' | awk '{print $NF}')
        # 获取 jar 名称
        lib_name=$(echo $lib_path | awk -F/ '{print $NF}')
        # 创建软链接
        ln -s "${lib_dir}/$lib_name" $lib_path
      fi
    done
    target_dir=$(dirname "${line}")
    rm -rf ${target_dir}
    # 返回上层目录
    cd ..
  fi
done

# 清理临时文件
rm -rf $non_app ${jars_file}
