# Win11 部署指导手册（保姆级）

> 项目：基于容器的云文件管理系统  
> 目标：在 Windows 11 上使用 Docker 部署并运行系统

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

**方式二：直接使用 docker compose**

在 PowerShell 中执行：

```powershell
cd D:\Projects\yjxbishe
docker compose up -d --build
```

### 6.4 等待启动

首次启动需要：
1. 下载基础镜像（nginx、mysql、redis）
2. 构建后端镜像（编译 Java 代码）
3. 初始化数据库

整个过程约 5-15 分钟（取决于网络和电脑性能）。

### 6.5 查看启动状态

```powershell
docker compose ps
```

所有服务状态应为 `Up` 或 `running`。

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

# 查看日志
docker compose logs -f

# 查看某个服务的日志
docker compose logs -f backend
docker compose logs -f mysql

# 停止服务
docker compose down

# 重启服务
docker compose restart

# 重新构建并启动
docker compose up -d --build
```

### 7.2 数据管理

```powershell
# 进入 MySQL 容器
docker exec -it lfs-mysql mysql -u root -plfs123456

# 进入后端容器
docker exec -it lfs-backend sh

# 清理所有数据（慎用！）
docker compose down -v
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

**常见原因**：
1. MySQL 还没启动完成 → 等待几分钟后重试
2. 内存不足 → 增加 Docker 内存限制

### 8.4 前端页面空白

**可能原因**：
1. 前端未构建 → 执行 `npm run build`
2. dist 目录为空 → 检查构建是否成功

### 8.5 无法连接数据库

**检查 MySQL 容器**：
```powershell
docker compose logs mysql
```

**手动初始化数据库**：
```powershell
docker exec -it lfs-mysql mysql -u root -plfs123456 -e "source /docker-entrypoint-initdb.d/lfs.sql"
```

---

## 九、答辩演示准备

### 9.1 演示前检查清单

- [ ] Docker Desktop 已启动（右下角图标为绿色）
- [ ] 所有容器正常运行（`docker compose ps` 全部 Up）
- [ ] 浏览器能正常访问 http://localhost
- [ ] 能正常登录系统
- [ ] 准备几个测试文件（图片、视频、PDF、Word）

### 9.2 演示流程建议

1. **展示系统架构图**（PPT）
2. **展示 Docker 容器**：`docker compose ps`
3. **演示核心功能**：
   - 用户登录
   - 文件上传（展示进度条、秒传）
   - 文件预览（图片、视频、PDF）
   - 文件分享（生成链接）
   - 存储空间统计
   - 回收站功能
4. **展示容器化配置**：
   - docker-compose.yml
   - Dockerfile（多阶段构建）
   - nginx.conf（反向代理）

### 9.3 备用方案

如果现场演示出问题：
1. 提前录制演示视频
2. 准备截图作为备份

---

## 十、从 Mac 迁移到 Win11 检查清单

### 10.1 迁移前（Mac 端）

- [ ] 代码开发完成，功能正常
- [ ] 前端已构建（有 dist 目录）或准备在 Win11 构建
- [ ] 数据库初始化脚本完整（sql 目录）
- [ ] 打包项目（排除 node_modules、target）

### 10.2 迁移后（Win11 端）

- [ ] Docker Desktop 安装并启动
- [ ] Node.js 安装完成
- [ ] 项目解压到指定目录
- [ ] 前端构建成功（npm run build）
- [ ] docker compose up 成功
- [ ] 浏览器访问正常
- [ ] 登录功能正常
- [ ] 文件上传下载正常

---

*最后更新：2025-12-30*

