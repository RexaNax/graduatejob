# Win11 离线部署速查卡

> 无 AI 时可快速参考的部署步骤和问题解决方案
> 打印此文档或保存到手机

---

## 一、完整部署步骤（按顺序执行）

### 步骤 1：安装软件（按顺序）
1. **Git**：https://git-scm.com/download/win → 安装时全部默认
2. **Node.js 18**：https://nodejs.org → 选择 LTS 版本
3. **Docker Desktop**：https://www.docker.com/products/docker-desktop → 安装后重启

### 步骤 2：检查安装
```cmd
git --version
node --version
docker --version
```
如果任何命令报错"不是内部命令"，需要重新安装或添加环境变量。

### 步骤 3：克隆代码
```cmd
cd C:\
git clone https://github.com/RexaNax/graduatejob.git yjxbishe
```

### 步骤 4：创建 E 盘数据目录（重要！）
```cmd
mkdir E:\lfs-data
mkdir E:\lfs-data\mysql
mkdir E:\lfs-data\redis
mkdir E:\lfs-data\upload
```
> 如果没有 E 盘，改用 D 盘，并修改 docker-compose.yml 中的路径

### 步骤 5：构建前端
```cmd
cd C:\yjxbishe\project\frontend
npm install
npm run build
```

### 步骤 6：启动系统
```cmd
cd C:\yjxbishe\project\deploy
docker compose up -d --build
```

### 步骤 7：验证
- 浏览器打开：http://localhost:8917
- 登录：admin / 123456

---

## 二、常见错误及解决方案

### ❌ git clone 超时
```cmd
# 方案1：使用镜像
git clone https://gitclone.com/github.com/RexaNax/graduatejob.git C:\yjxbishe

# 方案2：直接下载 ZIP
# 访问 https://github.com/RexaNax/graduatejob
# 点击 Code → Download ZIP → 解压到 C:\yjxbishe
```

### ❌ npm install 失败
```cmd
# 清除缓存重试
npm cache clean --force
npm install

# 或使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

### ❌ npm run build 内存不足
```cmd
set NODE_OPTIONS=--max-old-space-size=4096
npm run build
```

### ❌ docker compose 报错 "not found"
```cmd
# 方案1：使用旧命令格式
docker-compose up -d --build

# 方案2：确保 Docker Desktop 已启动
# 任务栏右下角应有 Docker 图标
```

### ❌ 容器启动失败
```cmd
# 查看哪个容器有问题
docker ps -a

# 查看具体日志
docker logs lfs-backend
docker logs lfs-mysql
docker logs lfs-nginx

# 重启所有容器
docker compose down
docker compose up -d --build
```

### ❌ MySQL 容器一直重启
```cmd
# 清除数据重来
docker compose down -v
docker compose up -d --build
```

### ❌ 访问 localhost:8917 显示空白或404
```cmd
# 检查前端是否构建成功
dir C:\yjxbishe\project\frontend\dist

# 如果 dist 不存在，重新构建
cd C:\yjxbishe\project\frontend
npm run build

# 重启 nginx
docker restart lfs-nginx
```

### ❌ 登录时报 "网络错误"
```cmd
# 检查后端是否正常
docker logs lfs-backend

# 如果报数据库连接错误，等待 30 秒后重试
# MySQL 需要初始化时间
```

### ❌ C盘权限不足
```cmd
# 以管理员身份运行 CMD
# 右键 CMD → 以管理员身份运行
mkdir C:\yjxbishe
```

### ❌ Docker Desktop 无法启动
1. 检查任务管理器，关闭所有 Docker 进程
2. 运行：`wsl --shutdown`
3. 重启电脑
4. 重新打开 Docker Desktop

---

## 三、验证清单

部署完成后，逐项验证：

- [ ] http://localhost:8917 能打开
- [ ] 用 admin/123456 能登录
- [ ] 能上传文件
- [ ] 能预览/下载文件
- [ ] 能创建分享链接
- [ ] 能搜索文件

---

## 四、紧急恢复

如果一切都搞砸了，执行完全重置：

```cmd
# 1. 停止并删除所有容器和数据
cd C:\yjxbishe\project\deploy
docker compose down -v

# 2. 删除代码目录
cd C:\
rd /s /q yjxbishe

# 3. 重新克隆
git clone https://github.com/RexaNax/graduatejob.git yjxbishe

# 4. 重新构建前端
cd C:\yjxbishe\project\frontend
npm install
npm run build

# 5. 重新启动
cd C:\yjxbishe\project\deploy
docker compose up -d --build
```

---

## 五、关键命令速查

| 操作 | 命令 |
|------|------|
| 查看容器状态 | `docker ps` |
| 查看所有容器 | `docker ps -a` |
| 查看容器日志 | `docker logs 容器名` |
| 重启容器 | `docker restart 容器名` |
| 停止所有容器 | `docker compose down` |
| 启动所有容器 | `docker compose up -d` |
| 重建并启动 | `docker compose up -d --build` |
| 清除所有数据 | `docker compose down -v` |

---

## 六、联系方式备用

如遇到无法解决的问题，可搜索：
- "docker desktop windows 教程"
- "npm install 失败 解决方案"
- 具体错误信息直接搜索

---

**重要提示**：
1. 每一步都要等前一步完成再继续
2. Docker Desktop 启动需要 1-2 分钟，等图标显示"running"再执行命令
3. 第一次 `docker compose up` 会下载镜像，可能需要 10-20 分钟
4. 遇到问题先看日志：`docker logs 容器名`
