#!/bin/bash

# 定义颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 定义全局变量
declare -r ROOT_DIR=$(pwd)
declare -r EXTRACT_JAR_DIR="${ROOT_DIR}/extract_jar"
declare -r MODULE_DIR="${ROOT_DIR}/mod_lib"     # mod 依赖，容易变的依赖
declare -r LIB_DIR="${ROOT_DIR}/lib"            # 通用依赖，不易变的依赖
declare -r SERVICES_DIR="${ROOT_DIR}/services"   # 服务目录
declare -r JARS_FILE="/tmp/build-jars.txt"      # 临时文件存储 jar 列表
declare -r NON_APP="/tmp/non-app.txt"           # 临时文件存储非应用 jar

# 输出日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 清理旧文件和目录
cleanup_old_files() {
    log_info "清理旧文件和目录..."
    rm -rf "${MODULE_DIR}" "${LIB_DIR}" "${SERVICES_DIR}"
    mkdir -p "${SERVICES_DIR}"
}

# 查找所有 JAR 文件
find_all_jars() {
    log_info "查找所有 JAR 文件..."
    find "${ROOT_DIR}" -name "*.jar" > "${JARS_FILE}"
}

# 解压和合并依赖
extract_and_merge_dependencies() {
    log_info "解压和合并依赖..."
    
    # 使用新的推荐命令格式解压所有 JAR 文件
    awk '{print $1}' "${JARS_FILE}" | xargs -I {} java -Djarmode=tools -jar {} extract --layers --launcher --force --destination ${EXTRACT_JAR_DIR} 2>"${NON_APP}"
    
    # 创建依赖目录
    mkdir -p "${LIB_DIR}" "${MODULE_DIR}"
    
    # 复制依赖文件
    cp ${EXTRACT_JAR_DIR}/dependencies/BOOT-INF/lib/*.jar "${LIB_DIR}/"
    
    # 处理快照依赖
    if [ -d "${EXTRACT_JAR_DIR}/snapshot-dependencies/BOOT-INF/lib/" ]; then
        cp ${EXTRACT_JAR_DIR}/snapshot-dependencies/BOOT-INF/lib/*.jar "${LIB_DIR}/"
    fi
    
    # 处理应用依赖
    if [ -d "${EXTRACT_JAR_DIR}/application/BOOT-INF/lib/" ]; then
        cp ${EXTRACT_JAR_DIR}/application/BOOT-INF/lib/*.jar "${MODULE_DIR}/"
    fi
    
    # 清理临时目录
    rm -rf ${EXTRACT_JAR_DIR}
}

# 创建服务软链接
create_service_symlinks() {
    local service_name="$1"
    local service_dir="$2"
    
    cd "${service_dir}" || exit 1
    
    while read -r layer_dep_path; do
        if [[ "${layer_dep_path}" == *".jar"* ]]; then
            # 移除双引号并提取 JAR 路径
            local lib_path=$(echo "${layer_dep_path}" | tr -d '"' | awk '{print $NF}')
            # 获取 jar 名称
            local lib_name=$(echo "${lib_path}" | awk -F/ '{print $NF}')
            # 创建软链接，使用 -f 强制创建并忽略错误输出
            ln -sf "${LIB_DIR}/${lib_name}" "${lib_path}" 2>/dev/null
        fi
    done < "BOOT-INF/classpath.idx"
}

# 处理单个服务
process_service() {
    local jar_path="$1"
    local service_name=$(echo "${jar_path}" | awk -F/ '{print $(NF-2)}')
    
    if grep -q "${service_name}" "${NON_APP}"; then
        log_info "跳过非应用 JAR: ${service_name}"
        return
    fi
    
    log_info "处理服务: ${service_name}"
    local service_dir="${SERVICES_DIR}/${service_name}"
    mkdir -p "${service_dir}"
    
    # 解压 JAR 到服务目录
    unzip -q "${jar_path}" -d "${service_dir}"
    rm -rf "${service_dir}/BOOT-INF/lib/*"
    
    # 创建依赖软链接
    create_service_symlinks "${service_name}" "${service_dir}"
    
    # 清理原始 JAR 目录
    local target_dir=$(dirname "${jar_path}")
    rm -rf "${target_dir}"
}

# 处理所有服务
process_all_services() {
    log_info "开始处理所有服务..."
    cd "${SERVICES_DIR}" || exit 1
    
    while read -r jar_path; do
        process_service "${jar_path}"
    done < "${JARS_FILE}"
}

# 清理临时文件
cleanup_temp_files() {
    log_info "清理临时文件..."
    rm -f "${NON_APP}" "${JARS_FILE}"
}

# 显示目录结构
show_directory_structure() {
    log_info "当前目录结构:"
    echo -e "${GREEN}"
    tree -L 3 "${ROOT_DIR}" 2>/dev/null || {
        # 如果 tree 命令不可用，使用 ls 替代
        find "${ROOT_DIR}" -maxdepth 3 -type d | sed 's/[^/]*\//  /g'
    }
    echo -e "${NC}"
}

# 主函数
main() {
    log_info "开始构建服务..."
    
    cleanup_old_files
    find_all_jars
    extract_and_merge_dependencies
    process_all_services
    cleanup_temp_files    
    log_info "构建完成！"
}

# 执行主函数
main
