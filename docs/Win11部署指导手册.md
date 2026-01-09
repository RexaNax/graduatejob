# Win11 部署指导手册（保姆级）

> 项目：基于容器的云文件管理系统  
> 目标：在 Windows 11 上使用 Docker 部署并运行系统  
> 版本：v2.0

---

## 系统架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      用户浏览器                              │
│                    http://localhost                         │
└─────────────────────┬───────────────────────────────────────┘
                      │ :80
┌─────────────────────▼───────────────────────────────────────┐
│                   Nginx (lfs-nginx)                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  /          → 前端静态文件 (Vue)                     │    │
│  │  /api/*     → 后端服务 (反向代理)                    │    │
│  │  /file/*    → 文件下载/预览                         │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────┬───────────────────────────────────────┘
                      │ :8919
┌─────────────────────▼───────────────────────────────────────┐
│               Backend (lfs-backend)                         │
│         Spring Boot + JDK 17 + 多阶段构建                    │
└───────────┬─────────────────────────────┬───────────────────┘
            │ :3306                       │ :6379
┌───────────▼───────────┐     ┌───────────▼───────────┐
│   MySQL (lfs-mysql)   │     │   Redis (lfs-redis)   │
│      MySQL 8.0        │     │    Redis 7 Alpine     │
│   数据库: lfs          │     │      会话缓存          │
└───────────────────────┘     └───────────────────────┘
```

**端口映射：**
| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|---------|-----------|------|
| Nginx | 80 | 80 | 对外访问入口 |
| MySQL | 3306 | 3307 | 避免与本地冲突 |
| Redis | 6379 | 6380 | 避免与本地冲突 |

---

## 一、环境准备

### 1.1 系统要求

| 项目 | 要求 |
|------|------|
| 操作系统 | Windows 11 (64位) |
| 内存 | 8GB 以上（推荐16GB） |
| 硬盘 | 20GB 以上可用空间 |
| CPU | 支持虚拟化（需在BIOS开启） |

### 1.2 检查虚拟化是否开启

1. 按 `Ctrl + Shift + Esc` 打开任务管理器
2. 点击"性能"标签
3. 查看右下角"虚拟化"是否显示"已启用"
4. 如果未启用，需要进入BIOS开启（不同主板方式不同，搜索"你的主板型号 + 开启虚拟化"）

---

## 二、安装 Docker Desktop

### 2.1 下载 Docker Desktop

1. 打开浏览器，访问：https://www.docker.com/products/docker-desktop/
2. 点击 "Download for Windows" 下载安装包
3. 或直接下载：https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

### 2.2 安装步骤

1. 双击下载的 `Docker Desktop Installer.exe`
2. 安装选项：
   - ✅ 勾选 "Use WSL 2 instead of Hyper-V"（推荐）
   - ✅ 勾选 "Add shortcut to desktop"
3. 点击 "OK" 开始安装
4. 安装完成后点击 "Close and restart"（会重启电脑）

### 2.3 首次启动配置

1. 重启后，Docker Desktop 会自动启动
2. 如果弹出 WSL 2 更新提示，按提示完成更新
3. 等待 Docker 启动完成（右下角图标变为绿色）
4. 打开 Docker Desktop，跳过登录（可以不注册账号）

### 2.4 验证安装

打开 PowerShell 或 CMD，执行：

```powershell
docker --version
docker compose version
```

如果显示版本号，说明安装成功。

---

## 三、安装 Git（用于拉取代码）

### 3.1 下载安装

1. 访问：https://git-scm.com/download/win
2. 下载 64-bit Git for Windows Setup
3. 安装时一路点 Next（默认选项即可）

### 3.2 验证安装

```powershell
git --version
```

---

## 四、安装 Node.js（用于构建前端）

### 4.1 下载安装

1. 访问：https://nodejs.org/
2. 下载 LTS 版本（如 18.x 或 20.x）
3. 安装时一路点 Next

### 4.2 验证安装

```powershell
node --version
npm --version
```

### 4.3 配置国内镜像（加速下载）

```powershell
npm config set registry https://registry.npmmirror.com
```

---

## 五、获取项目代码

### 方式一：从 Mac 复制（推荐）

1. 在 Mac 上，将整个项目文件夹打包：
   ```bash
   cd /Users/rexan/Desktop/个人文件/
   zip -r yjxbishe.zip yjxbishe -x "*.git*" -x "*node_modules*" -x "*target*"
   ```

2. 通过以下方式传输到 Win11：
   - U盘拷贝
   - 网盘（百度网盘、阿里云盘等）
   - 局域网共享
   - 微信/QQ文件传输

3. 在 Win11 上解压到合适位置，如：`D:\Projects\yjxbishe`

### 方式二：从 Git 仓库拉取（如果有）

```powershell
cd D:\Projects
git clone <你的仓库地址> yjxbishe
```

---

## 六、项目部署

### 6.1 打开项目目录

```powershell
cd D:\Projects\yjxbishe
```

### 6.2 构建前端

```powershell
cd lfs-vue-master
npm install
npm run build
cd ..
```

构建成功后会生成 `lfs-vue-master/dist` 目录。

### 6.3 一键启动

**方式一：使用部署脚本（推荐）**

在 Git Bash 中执行（因为 deploy.sh 是 bash 脚本）：

```bash
# 打开 Git Bash，进入项目目录
cd /d/Projects/yjxbishe

# 添加执行权限并运行
chmod +x deploy.sh
./deploy.sh
```

**deploy.sh 支持的命令：**
| 命令 | 说明 |
|------|------|
| `./deploy.sh` 或 `./deploy.sh start` | 启动所有服务 |
| `./deploy.sh stop` | 停止所有服务 |
| `./deploy.sh restart` | 重启所有服务 |
| `./deploy.sh logs` | 查看实时日志 |
| `./deploy.sh status` | 查看服务状态 |
| `./deploy.sh rebuild` | 重新构建前端并启动 |
| `./deploy.sh clean` | 清理所有容器和数据（慎用） |

**方式二：直接使用 docker compose**

在 PowerShell 中执行：

```powershell
cd D:\Projects\yjxbishe
docker compose up -d --build
```

### 6.4 等待启动

首次启动需要：
1. 下载基础镜像（nginx:alpine、mysql:8.0、redis:7-alpine）
2. 构建后端镜像（Maven 编译 + 多阶段构建）
3. 初始化数据库（自动执行 sql 目录下的脚本）

整个过程约 5-15 分钟（取决于网络和电脑性能）。

### 6.5 查看启动状态

```powershell
docker compose ps
```

正常状态示例：
```
NAME           IMAGE                COMMAND                  STATUS
lfs-backend    yjxbishe-backend     "sh -c 'java $JAVA_O…"   Up
lfs-mysql      mysql:8.0            "docker-entrypoint.s…"   Up (healthy)
lfs-nginx      nginx:alpine         "/docker-entrypoint.…"   Up
lfs-redis      redis:7-alpine       "docker-entrypoint.s…"   Up
```

### 6.6 访问系统

1. 打开浏览器
2. 访问：http://localhost
3. 登录账号：`admin`
4. 登录密码：`123456`

---

## 七、常用命令

### 7.1 服务管理

```powershell
# 查看服务状态
docker compose ps

# 查看所有日志
docker compose logs -f

# 查看某个服务的日志
docker compose logs -f backend
docker compose logs -f mysql
docker compose logs -f nginx

# 停止服务
docker compose down

# 重启服务
docker compose restart

# 重新构建并启动
docker compose up -d --build

# 强制重新构建（不使用缓存）
docker compose build --no-cache
docker compose up -d
```

### 7.2 数据管理

```powershell
# 进入 MySQL 容器
docker exec -it lfs-mysql mysql -u root -plfs123456

# 查看数据库
docker exec -it lfs-mysql mysql -u root -plfs123456 -e "SHOW DATABASES;"

# 进入后端容器
docker exec -it lfs-backend sh

# 查看上传的文件
docker exec -it lfs-backend ls -la /app/uploadFile

# 清理所有数据（慎用！会删除所有上传文件和数据库数据）
docker compose down -v
```

### 7.3 数据卷管理

```powershell
# 查看所有数据卷
docker volume ls

# 项目使用的数据卷：
# - lfs-mysql-data   : MySQL 数据
# - lfs-redis-data   : Redis 数据
# - lfs-upload-data  : 上传的文件

# 查看数据卷详情
docker volume inspect lfs-mysql-data
```

---

## 八、常见问题排查

### 8.1 端口被占用

**错误信息**：`Bind for 0.0.0.0:80 failed: port is already allocated`

**解决方法**：

```powershell
# 查看占用端口的进程
netstat -ano | findstr :80

# 结束进程（替换 PID）
taskkill /PID <PID> /F

# 或者修改 docker-compose.yml 中的端口映射
# 将 "80:80" 改为 "8080:80"，然后访问 http://localhost:8080
```

### 8.2 Docker 启动失败

**可能原因**：
1. WSL 2 未正确安装
2. 虚拟化未开启
3. 内存不足

**解决方法**：
1. 以管理员身份运行 PowerShell，执行：
   ```powershell
   wsl --install
   wsl --update
   ```
2. 重启电脑
3. 检查 BIOS 虚拟化设置

### 8.3 后端启动失败

**查看日志**：
```powershell
docker compose logs backend
```

**常见原因及解决**：

| 错误信息 | 原因 | 解决方法 |
|---------|------|---------|
| `Connection refused` | MySQL 还没启动完成 | 等待 1-2 分钟，MySQL 有健康检查 |
| `Out of memory` | 内存不足 | 增加 Docker 内存限制 |
| `Access denied` | 数据库密码错误 | 检查环境变量配置 |

**增加 Docker 内存**：
1. 打开 Docker Desktop → Settings → Resources
2. 将 Memory 调整为 4GB 以上
3. 点击 Apply & Restart

### 8.4 前端页面空白

**可能原因**：
1. 前端未构建 → 执行 `npm run build`
2. dist 目录为空 → 检查构建是否成功
3. Nginx 配置错误 → 查看 `docker compose logs nginx`

**检查 dist 目录**：
```powershell
dir lfs-vue-master\dist
```

### 8.5 无法连接数据库

**检查 MySQL 容器**：
```powershell
docker compose logs mysql
```

**检查健康状态**：
```powershell
docker inspect lfs-mysql --format='{{.State.Health.Status}}'
# 应该显示 "healthy"
```

**手动测试连接**：
```powershell
docker exec -it lfs-mysql mysql -u root -plfs123456 -e "SELECT 1;"
```

### 8.6 文件上传失败

**可能原因**：
1. 文件大小超限（默认 500MB）
2. 磁盘空间不足

**检查磁盘空间**：
```powershell
docker system df
```

**清理 Docker 缓存**：
```powershell
docker system prune -a
```

### 8.7 镜像下载慢

**配置 Docker 镜像加速**：
1. 打开 Docker Desktop → Settings → Docker Engine
2. 添加镜像源：
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```
3. 点击 Apply & Restart

---

## 九、答辩演示准备

### 9.1 演示前检查清单

- [ ] Docker Desktop 已启动（右下角图标为绿色）
- [ ] 所有容器正常运行（`docker compose ps` 全部 Up）
- [ ] 浏览器能正常访问 http://localhost
- [ ] 能正常登录系统
- [ ] 准备几个测试文件（图片、视频、PDF、Word）
- [ ] 网络连接正常（如需演示在线预览）

### 9.2 演示流程建议

1. **展示系统架构图**（PPT）
   - 展示本手册开头的架构图
   - 说明各组件作用

2. **展示 Docker 容器**：
   ```powershell
   docker compose ps
   docker images
   ```

3. **演示核心功能**：
   - 用户登录
   - 文件上传（展示进度条、秒传功能）
   - 文件预览（图片、视频、PDF、Office文档）
   - 文件分享（生成分享链接）
   - 存储空间统计
   - 回收站功能

4. **展示容器化配置**：
   - `docker-compose.yml` - 服务编排
   - `lfs-master/Dockerfile` - 多阶段构建
   - `nginx/nginx.conf` - 反向代理配置

5. **展示技术亮点**：
   - 多阶段构建减小镜像体积
   - 健康检查确保服务依赖
   - 数据卷持久化
   - 一键部署脚本

### 9.3 备用方案

如果现场演示出问题：
1. 提前录制演示视频
2. 准备截图作为备份
3. 准备本地开发环境作为备选

### 9.4 常见问题应对

| 问题 | 应对 |
|------|------|
| 容器启动慢 | 提前启动，演示时已就绪 |
| 网络问题 | 使用本地离线功能演示 |
| 端口冲突 | 提前检查并释放端口 |

---

## 十、从 Mac 迁移到 Win11 检查清单

### 10.1 迁移前（Mac 端）

- [ ] 代码开发完成，功能正常
- [ ] 前端已构建（有 dist 目录）或准备在 Win11 构建
- [ ] 数据库初始化脚本完整（lfs-master/sql 目录）
- [ ] 打包项目（排除 node_modules、target）

**打包命令**：
```bash
cd /Users/rexan/Desktop/个人文件/
zip -r yjxbishe.zip yjxbishe -x "*.git*" -x "*node_modules*" -x "*target*" -x "*.DS_Store"
```

### 10.2 迁移后（Win11 端）

- [ ] Docker Desktop 安装并启动
- [ ] Node.js 18+ 安装完成
- [ ] Git 安装完成
- [ ] 项目解压到指定目录（如 `D:\Projects\yjxbishe`）
- [ ] 前端构建成功（npm run build）
- [ ] docker compose up 成功
- [ ] 浏览器访问正常
- [ ] 登录功能正常
- [ ] 文件上传下载正常

---

## 十一、配置文件说明

### 11.1 docker-compose.yml 关键配置

```yaml
services:
  nginx:          # 反向代理 + 静态文件服务
    ports: "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./lfs-vue-master/dist:/usr/share/nginx/html:ro

  backend:        # Spring Boot 后端
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/lfs
      - SPRING_DATA_REDIS_HOST=redis
    volumes:
      - upload-data:/app/uploadFile  # 持久化上传文件

  mysql:          # 数据库
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=lfs123456
      - MYSQL_DATABASE=lfs
    healthcheck:  # 健康检查，确保启动完成
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]

  redis:          # 缓存
    image: redis:7-alpine
```

### 11.2 Dockerfile 多阶段构建

```dockerfile
# 阶段1: 使用 Maven 编译
FROM maven:3.9-eclipse-temurin-17 AS builder
RUN mvn package -DskipTests

# 阶段2: 使用精简 JRE 运行
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
```

**优势**：
- 最终镜像不包含 Maven 和源码
- 镜像体积大幅减小（约 200MB vs 1GB+）

### 11.3 nginx.conf 核心配置

```nginx
# 前端路由（Vue Router history 模式）
location / {
    try_files $uri $uri/ /index.html;
}

# API 反向代理
location /api/ {
    proxy_pass http://backend/;
}

# 文件下载代理
location /file/ {
    proxy_pass http://backend/file/;
    proxy_buffering off;  # 大文件优化
}
```

---

## 十二、快速参考卡片

### 启动系统
```powershell
cd D:\Projects\yjxbishe
docker compose up -d
```

### 停止系统
```powershell
docker compose down
```

### 查看状态
```powershell
docker compose ps
```

### 查看日志
```powershell
docker compose logs -f
```

### 重新部署
```powershell
docker compose down
docker compose up -d --build
```

### 访问地址
- 系统入口：http://localhost
- 账号：admin / 123456

---

*最后更新：2026-01-09*
