#!/bin/bash
# ============================================
# 一键部署脚本
# ============================================
# 使用方法:
#   chmod +x deploy.sh
#   ./deploy.sh          # 启动所有服务
#   ./deploy.sh stop     # 停止所有服务
#   ./deploy.sh restart  # 重启所有服务
#   ./deploy.sh logs     # 查看日志
#   ./deploy.sh clean    # 清理所有容器和数据
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
info() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker"
    fi
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        error "Docker Compose 未安装，请先安装 Docker Compose"
    fi
    info "Docker 环境检查通过"
}

# 构建前端
build_frontend() {
    info "构建前端项目..."
    cd lfs-vue-master
    
    # 检查是否有 node_modules
    if [ ! -d "node_modules" ]; then
        info "安装前端依赖..."
        npm install --registry=https://registry.npmmirror.com
    fi
    
    # 构建
    npm run build
    cd ..
    success "前端构建完成"
}

# 启动服务
start() {
    check_docker
    
    info "开始部署云文件管理系统..."
    
    # 构建前端（如果 dist 不存在）
    if [ ! -d "lfs-vue-master/dist" ]; then
        build_frontend
    fi
    
    # 启动容器
    info "启动 Docker 容器..."
    docker compose up -d --build
    
    # 等待服务启动
    info "等待服务启动..."
    sleep 10
    
    # 检查服务状态
    if docker compose ps | grep -q "Up"; then
        success "=========================================="
        success "  部署成功！"
        success "=========================================="
        echo ""
        info "访问地址: http://localhost"
        info "默认账号: admin"
        info "默认密码: 123456"
        echo ""
        info "MySQL 端口: 3307 (外部访问)"
        info "Redis 端口: 6380 (外部访问)"
        echo ""
        info "查看日志: ./deploy.sh logs"
        info "停止服务: ./deploy.sh stop"
    else
        error "部署失败，请查看日志: docker compose logs"
    fi
}

# 停止服务
stop() {
    info "停止所有服务..."
    docker compose down
    success "服务已停止"
}

# 重启服务
restart() {
    info "重启所有服务..."
    docker compose restart
    success "服务已重启"
}

# 查看日志
logs() {
    docker compose logs -f --tail=100
}

# 清理所有数据
clean() {
    warn "此操作将删除所有容器和数据卷！"
    read -p "确认删除？(y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        info "清理容器和数据卷..."
        docker compose down -v --rmi local
        success "清理完成"
    else
        info "取消操作"
    fi
}

# 查看状态
status() {
    docker compose ps
}

# 重新构建
rebuild() {
    info "重新构建并启动..."
    build_frontend
    docker compose up -d --build --force-recreate
    success "重建完成"
}

# 主入口
case "${1:-start}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    logs)
        logs
        ;;
    clean)
        clean
        ;;
    status)
        status
        ;;
    rebuild)
        rebuild
        ;;
    build-frontend)
        build_frontend
        ;;
    *)
        echo "使用方法: $0 {start|stop|restart|logs|clean|status|rebuild}"
        exit 1
        ;;
esac
