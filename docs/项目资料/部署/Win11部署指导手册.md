# Win11 部署指导手册（保姆级）

> 项目：基于容器的云文件管理系统  
> 目标：在 Windows 11 上使用 Docker 部署并运行系统  
> 版本：v2.5
> 更新：数据存储迁移到 E 盘，避免占用 C 盘空间

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
| MySQL | 3306 | 3308 | 避免与本地冲突 |
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

### 1.3 Windows 功能检查

在部署前，确保以下 Windows 功能已启用：

1. 按 `Win + R`，输入 `optionalfeatures`，回车
2. 确保以下选项已勾选：
   - ✅ **适用于 Linux 的 Windows 子系统**（Windows Subsystem for Linux）
   - ✅ **虚拟机平台**（Virtual Machine Platform）
   - ✅ **Hyper-V**（如果有此选项）

3. 如果有变更，点击确定后重启电脑

**或者用命令行启用（以管理员身份运行 PowerShell）**：
```cmd
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
```

然后重启电脑。

### 1.4 关闭可能冲突的软件

以下软件可能与 Docker 冲突，建议在部署时关闭：

| 软件类型 | 示例 | 处理方式 |
|----------|------|----------|
| 虚拟机软件 | VMware, VirtualBox | 关闭或卸载 |
| 安全软件 | 360, 腾讯电脑管家 | 临时关闭或添加白名单 |
| 代理软件 | Clash, V2Ray | 确认端口不冲突 |
| 本地服务器 | XAMPP, WAMP, phpStudy | 停止服务，避免端口冲突 |

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

打开 **PowerShell** 或 **CMD**（任选其一），执行：

```cmd
docker --version
docker compose version
```

如果显示版本号，说明安装成功。

> 💡 **提示**：本手册中的所有命令在 PowerShell 和 CMD 中都可以执行，二者任选其一即可。

### 2.5 Docker Desktop 配置（重要）

首次使用前，建议进行以下配置：

#### 2.5.1 资源分配

1. 打开 Docker Desktop
2. 点击右上角 ⚙️ **Settings**
3. 左侧选择 **Resources** → **Advanced**
4. 调整资源配置：

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| CPUs | 4 或更多 | 加快构建速度 |
| Memory | 4GB 或更多 | 防止内存不足 |
| Swap | 1GB | 内存溢出缓冲 |
| Disk image size | 60GB+ | 存储镜像和数据 |

5. 点击 **Apply & Restart**

#### 2.5.2 镜像加速（解决下载慢）

1. 在 Settings 中选择 **Docker Engine**
2. 在 JSON 配置中添加镜像源：

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

3. 点击 **Apply & Restart**

#### 2.5.3 确认 WSL 2 后端

1. 在 Settings 中选择 **General**
2. 确保勾选 ✅ **Use the WSL 2 based engine**
3. 如果没有这个选项，说明 WSL 2 未安装，执行：
   ```cmd
   wsl --install
   wsl --update
   ```
4. 重启电脑后再打开 Docker Desktop

#### 2.5.4 验证配置

配置完成后，运行测试：

```cmd
docker run hello-world
```

如果看到 `Hello from Docker!` 说明配置成功。

---

## 三、安装·Git（用于拉取代码）

### 3.1 下载安装

1. 访问：https://git-scm.com/download/win
2. 下载 64-bit Git for Windows Setup
3. 安装时**注意以下选项**：

#### 安装选项说明

| 步骤 | 推荐选择 | 说明 |
|------|----------|------|
| Select Components | 默认即可 | 可勾选 "Add a Git Bash Profile to Windows Terminal" |
| Choosing the default editor | 选择你熟悉的编辑器 | 推荐 Notepad++ 或 VS Code |
| Adjusting PATH | **Git from the command line and also from 3rd-party software** | 让 CMD/PowerShell 也能用 git |
| Choosing HTTPS transport backend | Use the OpenSSL library | 默认即可 |
| Configuring line ending | **Checkout as-is, commit as-is** | 避免换行符问题 |
| Configuring the terminal emulator | Use Windows' default console window | 默认即可 |
| Default behavior of `git pull` | Default (fast-forward or merge) | 默认即可 |
| Choose a credential helper | Git Credential Manager | 默认即可 |
| Configuring extra options | 默认即可 | |

> 💡 **提示**：如果不确定，一路点 Next 使用默认选项也可以正常使用。

### 3.2 验证安装

```cmd
git --version
```

### 3.3 Git 基础配置（首次使用必须）

```cmd
# 配置用户名和邮箱（随便填，用于标识提交者）
git config --global user.name "YourName"
git config --global user.email "your@email.com"

# 解决中文文件名乱码
git config --global core.quotepath false

# 配置默认分支名
git config --global init.defaultBranch main
```

### 3.4 Git 网络优化（解决下载慢）

```cmd
# 增加缓冲区大小（解决大文件克隆问题）
git config --global http.postBuffer 524288000

# 增加超时时间
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999

# 禁用 SSL 验证（如遇证书问题，临时使用）
git config --global http.sslVerify false
```

---

## 四、安装 Node.js（用于构建前端）

### 4.1 下载安装

1. 访问：https://nodejs.org/
2. 下载 **LTS 版本**（推荐 20.x，至少 18.x）
3. 安装时**注意以下选项**：
   - ✅ 勾选 "Automatically install the necessary tools..."（可选，会安装编译工具）
   - 其他保持默认

### 4.2 验证安装

```cmd
node --version
npm --version
```

预期输出示例：
```
v20.11.0
10.2.4
```

### 4.3 npm 配置（重要）

```cmd
# 配置国内镜像（加速下载，必须配置！）
npm config set registry https://registry.npmmirror.com

# 验证镜像配置
npm config get registry
# 应该显示：https://registry.npmmirror.com/

# 配置缓存目录（可选，避免 C 盘空间不足）
npm config set cache "D:\npm-cache"

# 配置全局安装目录（可选）
npm config set prefix "D:\npm-global"
```

### 4.4 常见 npm 问题解决

**问题1：npm install 很慢或卡住**
```cmd
# 清除缓存
npm cache clean --force

# 使用淘宝镜像重新安装
npm install --registry=https://registry.npmmirror.com
```

**问题2：权限错误**
```cmd
# 以管理员身份运行 CMD/PowerShell
# 或者修改 npm 全局目录权限
```

**问题3：node-sass 或 gyp 编译错误**
```cmd
# 安装 Windows 编译工具（以管理员身份运行）
npm install --global windows-build-tools
```

---

## 五、获取项目代码

### 方式一：从 GitHub 下载（推荐）

1. 访问仓库：https://github.com/RexaNax/graduatejob
2. 点击绿色 "Code" 按钮 → "Download ZIP"
3. 解压到 C 盘根目录，会得到 `graduatejob-main` 文件夹
4. **重要**：重命名文件夹为 `yjxbishe`

最终目录结构应该是：
```
C:\yjxbishe\
├── docs\                    # 文档目录
│   ├── Win11部署指导手册.md
│   ├── 开题报告\
│   ├── 论文\
│   └── 流程图\
└── project\                 # 项目代码
    ├── backend\             # 后端 (Spring Boot)
    │   ├── Dockerfile
    │   ├── pom.xml
    │   ├── sql\             # 数据库初始化脚本
    │   └── src\
    ├── frontend\            # 前端 (Vue)
    │   ├── package.json
    │   └── src\
    └── deploy\              # 部署配置
        ├── deploy.sh        # 一键部署脚本
        ├── docker-compose.yml
        └── nginx\           # Nginx 配置
```

### 方式二：使用 Git 克隆

```cmd
cd C:\
git clone https://github.com/RexaNax/graduatejob.git yjxbishe
```

### ⚠️ Git Clone 超时解决方案

如果 `git clone` 报超时错误（如 `Failed to connect` 或 `Connection timed out`），尝试以下方法：

**方法1：使用 GitHub 镜像站（推荐）**
```cmd
cd C:\
git clone https://gitclone.com/github.com/RexaNax/graduatejob.git yjxbishe
```

**方法2：使用 Gitee 镜像**
```cmd
cd C:\
git clone https://gitee.com/mirrors_github/graduatejob.git yjxbishe
```

**方法3：直接下载 ZIP（最简单）**
1. 访问 https://github.com/RexaNax/graduatejob
2. 点击 **Code** → **Download ZIP**
3. 解压到 C 盘根目录，重命名为 `yjxbishe`

**方法4：配置 Git 代理（如有代理）**
```cmd
# 如果你有代理软件（如 Clash），配置 Git 使用代理
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy http://127.0.0.1:7890

# 然后再 clone
cd C:\
git clone https://github.com/RexaNax/graduatejob.git yjxbishe

# 用完后取消代理
git config --global --unset http.proxy
git config --global --unset https.proxy
```

**方法5：增加超时时间**
```cmd
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
cd C:\
git clone https://github.com/RexaNax/graduatejob.git yjxbishe
```

> 💡 **建议**：如果网络不好，直接用**方法3下载 ZIP** 是最快最稳定的方式。

---

## 六、项目部署

### 6.0 创建数据存储目录（重要！）

**首次部署前必须执行**，在 E 盘创建数据存储目录，避免占用 C 盘空间：

```cmd
mkdir E:\lfs-data
mkdir E:\lfs-data\mysql
mkdir E:\lfs-data\redis
mkdir E:\lfs-data\upload
```

> 💡 **说明**：
> - `mysql` - 数据库文件，约 500MB-2GB
> - `redis` - 缓存数据，约 10-50MB
> - `upload` - **用户上传的文件**，会持续增长
> 
> 如果没有 E 盘，可以改用 D 盘：`mkdir D:\lfs-data\mysql D:\lfs-data\redis D:\lfs-data\upload`
> 并修改 `docker-compose.yml` 中的路径。

### 6.1 进入部署目录

```cmd
cd C:\yjxbishe\project\deploy
```

### 6.2 构建前端

```cmd
cd ..\frontend
npm install
npm run build
cd ..\deploy
```

构建成功后会生成 `project\frontend\dist` 目录。

### 6.3 一键启动

**方式一：使用部署脚本**

在 Git Bash 中执行（因为 deploy.sh 是 bash 脚本）：

> 💡 **如何打开 Git Bash**：
> 1. 在文件资源管理器中进入 `yjxbishe\project\deploy` 目录
> 2. 在空白处**右键** → 选择 **"Open Git Bash here"** 或 **"Git Bash Here"**
> 3. 或者：开始菜单搜索 "Git Bash" 打开，然后 cd 到目录

```bash
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

**方式二：直接使用 docker compose（推荐，更简单）**

在 PowerShell 或 CMD 中执行：

```cmd
cd C:\yjxbishe\project\deploy
docker compose up -d --build
```

### 6.4 等待启动

首次启动需要：
1. 下载基础镜像（nginx:alpine、mysql:8.0、redis:7-alpine）
2. 构建后端镜像（Maven 编译 + 多阶段构建）
3. 初始化数据库（自动执行 sql 目录下的脚本）

整个过程约 5-15 分钟（取决于网络和电脑性能）。

### 6.5 查看启动状态

```cmd
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

```cmd
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

```cmd
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

```cmd
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

```cmd
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
1. 以管理员身份运行 PowerShell 或 CMD，执行：
   ```cmd
   wsl --install
   wsl --update
   ```
2. 重启电脑
3. 检查 BIOS 虚拟化设置

### 8.3 后端启动失败

**查看日志**：
```cmd
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
```cmd
dir ..\frontend\dist
```

### 8.5 无法连接数据库

**检查 MySQL 容器**：
```cmd
docker compose logs mysql
```

**检查健康状态**：
```cmd
docker inspect lfs-mysql --format='{{.State.Health.Status}}'
# 应该显示 "healthy"
```

**手动测试连接**：
```cmd
docker exec -it lfs-mysql mysql -u root -plfs123456 -e "SELECT 1;"
```

### 8.6 文件上传失败

**可能原因**：
1. 文件大小超限（默认 500MB）
2. 磁盘空间不足

**检查磁盘空间**：
```cmd
docker system df
```

**清理 Docker 缓存**：
```cmd
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

### 8.8 Windows 防火墙阻止

**错误信息**：容器间无法通信，或外部无法访问

**解决方法**：

1. **允许 Docker 通过防火墙**：
   - 打开 Windows 安全中心 → 防火墙和网络保护
   - 点击"允许应用通过防火墙"
   - 找到 Docker Desktop，勾选"专用"和"公用"

2. **手动添加防火墙规则**（以管理员身份运行 CMD）：
   ```cmd
   netsh advfirewall firewall add rule name="Docker" dir=in action=allow program="C:\Program Files\Docker\Docker\Docker Desktop.exe" enable=yes
   ```

3. **临时关闭防火墙测试**（不推荐长期使用）：
   ```cmd
   netsh advfirewall set allprofiles state off
   ```

### 8.9 杀毒软件干扰

部分杀毒软件（如 360、电脑管家）可能：
- 阻止 Docker 网络
- 删除 Docker 相关文件
- 拦截端口访问

**解决方法**：
1. 将以下目录添加到杀毒软件白名单：
   - `C:\Program Files\Docker\`
   - `C:\Users\{用户名}\.docker\`
   - `C:\yjxbishe\`
2. 或临时关闭杀毒软件进行测试

### 8.10 WSL 相关问题

**问题1：WSL 2 未安装或版本过旧**
```cmd
# 安装/更新 WSL
wsl --install
wsl --update

# 设置默认版本为 WSL 2
wsl --set-default-version 2

# 查看 WSL 状态
wsl --status
```

**问题2：WSL 内存占用过高**

创建文件 `C:\Users\{用户名}\.wslconfig`：
```ini
[wsl2]
memory=4GB
processors=4
swap=2GB
```

然后重启 WSL：
```cmd
wsl --shutdown
```

### 8.11 npm install 失败

**问题1：网络超时**
```cmd
# 确认使用国内镜像
npm config set registry https://registry.npmmirror.com

# 清除缓存后重试
npm cache clean --force
npm install
```

**问题2：node_modules 权限问题**
```cmd
# 删除 node_modules 后重新安装
rd /s /q node_modules
npm install
```

**问题3：package-lock.json 冲突**
```cmd
# 删除 lock 文件后重新安装
del package-lock.json
npm install
```

### 8.12 Docker 构建后端失败

**问题1：Maven 下载依赖超时**

查看日志确认是否卡在下载依赖：
```cmd
docker compose logs backend
```

解决方法 - 配置 Maven 镜像（已在 Dockerfile 中配置，但网络仍可能超时）：
```cmd
# 重新构建，不使用缓存
docker compose build --no-cache backend
docker compose up -d
```

**问题2：内存不足导致 Maven 编译失败**
- 错误信息可能包含 `OutOfMemoryError` 或 `Killed`
- 解决：增加 Docker Desktop 内存到 6GB+

**问题3：磁盘空间不足**
```cmd
# 查看 Docker 磁盘使用
docker system df

# 清理未使用的镜像和容器
docker system prune -a

# 清理构建缓存
docker builder prune
```

### 8.13 容器启动后访问 404 或 502

**404 错误（页面找不到）**：
1. 检查前端是否构建成功
   ```cmd
   dir C:\yjxbishe\project\frontend\dist
   ```
2. 如果 dist 目录不存在或为空，重新构建前端
   ```cmd
   cd C:\yjxbishe\project\frontend
   npm install
   npm run build
   ```

**502 错误（后端未就绪）**：
1. 检查后端容器状态
   ```cmd
   docker compose ps
   docker compose logs backend
   ```
2. 等待后端完全启动（首次可能需要 2-3 分钟）
3. 检查 MySQL 是否健康
   ```cmd
   docker inspect lfs-mysql --format='{{.State.Health.Status}}'
   ```

### 8.14 文件路径问题（Windows 特有）

**问题：路径中有中文或空格**

如果你的 Windows 用户名包含中文（如 `C:\Users\张三\Desktop`），可能导致问题。

**解决方法**：
1. 将项目放到无中文路径，如 `C:\yjxbishe\`
2. 或创建英文用户账户

**问题：路径过长**

Windows 默认路径长度限制 260 字符，node_modules 嵌套可能超限。

**解决方法**：
```cmd
# 以管理员身份运行，启用长路径支持
reg add "HKLM\SYSTEM\CurrentControlSet\Control\FileSystem" /v LongPathsEnabled /t REG_DWORD /d 1 /f
```
然后重启电脑。

### 8.15 Docker Desktop 无法启动

**问题1：提示 "Docker Desktop requires a newer WSL kernel version"**
```cmd
wsl --update
```

**问题2：提示 "Hardware assisted virtualization and data execution protection must be enabled"**
- 需要进入 BIOS 开启虚拟化
- 不同主板方式不同，搜索 "你的电脑型号 + 开启虚拟化"

**问题3：Docker Desktop 一直显示 "Starting..."**
1. 关闭 Docker Desktop
2. 以管理员身份运行 CMD：
   ```cmd
   wsl --shutdown
   net stop com.docker.service
   net start com.docker.service
   ```
3. 重新打开 Docker Desktop

**问题4：与 VMware/VirtualBox 冲突**
- 如果之前安装过 VMware 或 VirtualBox，可能需要禁用 Hyper-V 冲突
- 或者卸载其他虚拟化软件后重装 Docker Desktop

### 8.16 C 盘根目录写入权限不足

**错误信息**：`Access is denied` 或 `拒绝访问`

**原因**：部分企业/学校电脑限制了 C 盘根目录写入权限

**解决方法**：
1. **以管理员身份运行 CMD**：
   - 右键点击 CMD → "以管理员身份运行"
   - 然后执行 `mkdir C:\yjxbishe`

2. **或改用其他目录**：
   ```cmd
   # 放到 D 盘
   cd D:\
   git clone https://github.com/RexaNax/graduatejob.git yjxbishe
   
   # 后续命令中将 C:\yjxbishe 改为 D:\yjxbishe
   ```

3. **修改文件夹权限**：
   - 右键 C 盘 → 属性 → 安全 → 编辑
   - 给当前用户添加"完全控制"权限

### 8.17 Windows Defender 导致构建缓慢

**现象**：
- `npm install` 或 `docker build` 异常缓慢
- 磁盘 I/O 持续 100%

**解决方法**：

**方法1：添加排除目录（推荐）**
1. 打开 Windows 安全中心
2. 病毒和威胁防护 → 管理设置 → 排除项 → 添加排除项
3. 添加以下目录：
   - `C:\yjxbishe`
   - `C:\Program Files\Docker`
   - `C:\Users\{用户名}\AppData\Local\Docker`
   - `C:\Users\{用户名}\AppData\Roaming\npm-cache`

**方法2：临时禁用实时保护**（不推荐长期使用）
1. Windows 安全中心 → 病毒和威胁防护
2. 管理设置 → 关闭"实时保护"
3. 完成部署后记得重新开启

### 8.18 前端构建内存不足

**错误信息**：`FATAL ERROR: CALL_AND_RETRY_LAST Allocation failed - JavaScript heap out of memory`

**解决方法**：

```cmd
# 方法1：增加 Node.js 内存限制
set NODE_OPTIONS=--max-old-space-size=4096
npm run build

# 方法2：使用 cross-env（如果项目支持）
npm install -g cross-env
cross-env NODE_OPTIONS=--max-old-space-size=4096 npm run build
```

### 8.19 Docker 镜像拉取失败（网络问题）

**错误信息**：`error pulling image configuration` 或 `TLS handshake timeout`

**解决方法**：

**方法1：多次重试**
```cmd
# 手动拉取基础镜像
docker pull mysql:8.0
docker pull redis:7-alpine
docker pull nginx:alpine
docker pull maven:3.9-eclipse-temurin-17
docker pull eclipse-temurin:17-jre-alpine

# 然后再构建
docker compose up -d --build
```

**方法2：使用代理（如有）**
1. Docker Desktop → Settings → Resources → Proxies
2. 开启 "Manual proxy configuration"
3. 填入代理地址（如 `http://127.0.0.1:7890`）
4. Apply & Restart

**方法3：更换镜像源**
```json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerhub.azk8s.cn",
    "https://docker.mirrors.ustc.edu.cn"
  ]
}
```

### 8.20 Git 换行符问题导致脚本执行失败

**错误信息**：`/bin/bash^M: bad interpreter` 或 `$'\r': command not found`

**原因**：Windows 的 CRLF 换行符与 Linux 的 LF 不兼容

**解决方法**：

```cmd
# 配置 Git 保持原始换行符
git config --global core.autocrlf false

# 如果已经克隆，需要重新克隆
rd /s /q C:\yjxbishe
git clone https://github.com/RexaNax/graduatejob.git C:\yjxbishe
```

**或者手动转换**（在 Git Bash 中）：
```bash
cd /c/yjxbishe/project/deploy
sed -i 's/\r$//' deploy.sh
```

### 8.21 Docker Desktop 许可证提示

**现象**：首次启动弹出 "Docker Subscription Service Agreement" 对话框

**说明**：
- Docker Desktop 对**个人用户和教育用途免费**
- 对大型企业（250+ 员工或 $10M+ 收入）收费

**解决方法**：
- 毕设属于教育用途，直接点击 **"Accept"** 即可
- 无需注册账号，跳过登录

### 8.22 容器时区不正确

**现象**：日志时间或系统显示时间与本地不一致（差 8 小时）

**解决方法**：

已在 docker-compose.yml 中配置时区，如果仍有问题，检查配置：
```yaml
services:
  backend:
    environment:
      - TZ=Asia/Shanghai
```

或进入容器检查：
```cmd
docker exec -it lfs-backend date
```

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
   ```cmd
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
   - `project/deploy/docker-compose.yml` - 服务编排
   - `project/backend/Dockerfile` - 多阶段构建
   - `project/deploy/nginx/nginx.conf` - 反向代理配置

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
- [ ] 代码已推送到 GitHub
- [ ] 数据库初始化脚本完整（project/backend/sql 目录）

### 10.2 迁移后（Win11 端）

- [ ] Docker Desktop 安装并启动
- [ ] Node.js 18+ 安装完成
- [ ] Git 安装完成
- [ ] 从 GitHub 下载/克隆项目到 `C:\yjxbishe` 目录
- [ ] 进入 `project\deploy` 目录
- [ ] 前端构建成功（cd ..\frontend && npm install && npm run build）
- [ ] docker compose up 成功
- [ ] 浏览器访问 http://localhost 正常
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
      - ../frontend/dist:/usr/share/nginx/html:ro

  backend:        # Spring Boot 后端
    build:
      context: ../backend
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/lfs
      - SPRING_DATA_REDIS_HOST=redis
    volumes:
      - upload-data:/app/uploadFile  # 持久化上传文件

  mysql:          # 数据库
    image: mysql:8.0
    volumes:
      - ../backend/sql:/docker-entrypoint-initdb.d:ro  # 自动初始化
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
```cmd
cd C:\yjxbishe\project\deploy
docker compose up -d
```

### 停止系统
```cmd
docker compose down
```

### 查看状态
```cmd
docker compose ps
```

### 查看日志
```cmd
docker compose logs -f
```

### 重新部署
```cmd
docker compose down
docker compose up -d --build
```

### 访问地址
- 系统入口：http://localhost
- 账号：admin / 123456

---

## 附录 A：所有工具配置汇总

### A.1 配置检查清单

完成所有安装后，执行以下命令确认配置正确：

```cmd
# 1. Docker 检查
docker --version
docker compose version
docker run hello-world

# 2. Git 检查
git --version
git config --global user.name
git config --global user.email

# 3. Node.js 检查
node --version
npm --version
npm config get registry

# 4. WSL 检查
wsl --status
```

### A.2 环境变量确认

确保以下路径已添加到系统 PATH（通常安装时自动添加）：

| 工具 | 路径 |
|------|------|
| Docker | `C:\Program Files\Docker\Docker\resources\bin` |
| Git | `C:\Program Files\Git\cmd` |
| Node.js | `C:\Program Files\nodejs` |

**检查方法**：
```cmd
where docker
where git
where node
```

### A.3 推荐的完整配置脚本

将以下命令保存为 `setup.cmd`，以管理员身份运行可一次性完成所有配置：

```cmd
@echo off
echo ========== 配置 Git ==========
git config --global user.name "Student"
git config --global user.email "student@example.com"
git config --global core.quotepath false
git config --global init.defaultBranch main
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999

echo ========== 配置 npm ==========
call npm config set registry https://registry.npmmirror.com

echo ========== 配置 WSL ==========
wsl --update

echo ========== 配置完成 ==========
echo 请手动配置 Docker Desktop 的资源和镜像加速
pause
```

### A.4 端口占用快速检查

```cmd
# 检查项目需要的端口是否被占用
netstat -ano | findstr :80
netstat -ano | findstr :3308
netstat -ano | findstr :6380

# 如果有输出，说明端口被占用，需要结束相关进程
```

---

## 附录 B：部署前完整检查流程

按以下顺序执行，确保部署成功：

### B.1 第一步：系统环境检查

```cmd
# 1. 检查 Windows 版本（需要 Win10 2004+ 或 Win11）
winver

# 2. 检查虚拟化是否开启
# 打开任务管理器 → 性能 → 查看"虚拟化：已启用"

# 3. 检查磁盘空间（至少需要 20GB）
wmic logicaldisk get size,freespace,caption
```

### B.2 第二步：安装所有工具

按顺序安装：
1. Docker Desktop → 重启
2. Git
3. Node.js

### B.3 第三步：配置所有工具

```cmd
# Git 配置
git config --global user.name "Student"
git config --global user.email "student@example.com"
git config --global core.quotepath false

# npm 配置
npm config set registry https://registry.npmmirror.com

# 验证配置
docker --version
git --version
node --version
npm config get registry
```

### B.4 第四步：获取代码

```cmd
# 下载 ZIP 并解压到 C 盘根目录，重命名为 yjxbishe
# 或使用 git clone
cd C:\
git clone https://github.com/RexaNax/graduatejob.git yjxbishe
```

### B.5 第五步：构建前端

```cmd
cd C:\yjxbishe\project\frontend
npm install
npm run build
# 确认 dist 目录已生成
dir dist
```

### B.6 第六步：启动 Docker 服务

```cmd
cd C:\yjxbishe\project\deploy
docker compose up -d --build
```

### B.7 第七步：验证部署

```cmd
# 查看容器状态（所有容器应该是 Up 状态）
docker compose ps

# 查看日志（确认没有错误）
docker compose logs

# 测试访问
curl http://localhost
# 或直接在浏览器打开 http://localhost
```

### B.8 常见部署失败快速排查表

| 现象 | 可能原因 | 快速解决 |
|------|----------|----------|
| Docker 无法启动 | WSL 未安装 | `wsl --install` 后重启 |
| git clone 超时 | 网络问题 | 下载 ZIP 替代 |
| npm install 卡住 | 未配置镜像 | `npm config set registry https://registry.npmmirror.com` |
| npm run build 失败 | 依赖未安装 | 删除 node_modules 重新 `npm install` |
| docker compose up 失败 | 端口占用 | `netstat -ano \| findstr :80` 检查 |
| 访问 localhost 无响应 | 容器未启动 | `docker compose ps` 检查状态 |
| 显示 502 错误 | 后端未就绪 | 等待 2-3 分钟，查看 `docker compose logs backend` |
| 显示 404 错误 | 前端未构建 | 确认 dist 目录存在 |
| 登录失败 | 数据库未初始化 | `docker compose logs mysql` 检查 |

---

## 附录 C：紧急恢复方案

如果部署完全失败，可以按以下步骤重置：

### C.1 完全清理 Docker 环境

```cmd
# 停止所有容器
docker compose down

# 删除所有容器、镜像、数据卷
docker system prune -a --volumes

# 删除项目相关数据卷
docker volume rm deploy_lfs-mysql-data deploy_lfs-redis-data deploy_lfs-upload-data
```

### C.2 重新开始部署

```cmd
# 1. 删除并重新下载代码
cd C:\
rd /s /q yjxbishe
# 重新下载 ZIP 或 git clone

# 2. 重新构建前端
cd C:\yjxbishe\project\frontend
rd /s /q node_modules
npm install
npm run build

# 3. 重新启动 Docker
cd ..\deploy
docker compose up -d --build
```

### C.3 如果仍然失败

1. **截图错误信息**，发给我帮你分析
2. **导出日志**：
   ```cmd
   docker compose logs > deploy_log.txt 2>&1
   ```
3. 考虑使用**备用演示方案**（视频录制）

---

*最后更新：2026-01-15*
