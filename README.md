# 毕设项目：基于容器化部署的云文件管理系统

本仓库是一个用于毕业设计、课程验收和演示答辩的云文件管理系统项目。系统以 `Spring Boot 3 + Vue 3 + MySQL + Redis + Nginx + Docker Compose` 为核心技术栈，围绕文件上传、预览、分享、回收站和容器化部署完成了一套可运行的项目实现。

## 项目简介

项目当前以 `project/` 目录下的代码为主，包含前端、后端和部署配置三部分。整体目标是实现一个适合课程设计场景的轻量级云文件管理系统，并支持本地部署、演示账号登录和常见文件管理操作。

## 核心功能

- 文件上传、分片上传和秒传
- 图片、视频、文档在线预览
- 文件夹创建、移动、重命名、删除和回收站管理
- 文件分享与访问控制
- 管理员与演示用户的轻量隔离
- 基于 Docker Compose 的整体部署

## 技术栈

- 后端：Spring Boot 3.2、MyBatis-Plus、MySQL 8、Redis
- 前端：Vue 3、Vite、Element Plus
- 部署：Nginx、Docker Compose
- 运行环境：JDK 17+

## 目录结构

```text
graduatejob-main
├─ project                     # 项目代码
│  ├─ frontend                 # 前端项目（Vue 3 + Vite）
│  ├─ backend                  # 后端项目（Spring Boot 3）
│  └─ deploy                   # Docker Compose 与 Nginx 部署配置
├─ docs
│  ├─ 正式提交                  # 毕业设计正式提交件（论文/英文翻译/归档版 PDF）
│  ├─ 论文工作区                # 论文写作工作区（待打印终稿、图表、阶段材料等）
│  ├─ 参考文献                  # 论文引用的文献 PDF
│  ├─ 项目资料                  # 开发记录、部署/测试说明
│  ├─ ppt                       # 答辩 PPT（answer_defence.pptx 为答辩使用版）
│  └─ ppt_v2                    # 答辩 PPT 第二版工程
├─ 01_项目入口.md
└─ 02_论文入口.md
```

> 说明：查重报告、往届参考资料、论文加工历史中间版（归档）等内容仅本地保留，未纳入本仓库（见 `.gitignore`）。

## 快速启动

推荐使用 Docker Compose 进行整体启动（以仓库根目录为基准，路径按实际克隆位置调整）。

1. 先确认本机已安装并启动 Docker Desktop。
2. 前端首次构建：

```bash
cd project/frontend
npm install
npm run build
```

3. 启动整套服务：

```bash
cd project/deploy
docker compose up -d --build
```

## 多设备协作约定

本仓库由两台设备协作维护，按目录分工，提交信息也按来源区分，避免论文与代码互相覆盖：

| 内容 | 权威设备 | 说明 |
|---|---|---|
| `project/` 项目代码 | Windows（真实 Docker 环境） | 实际部署、跑通在 Windows；代码改动以该侧为准 |
| `docs/` 论文与资料 | Mac（写作终稿） | 论文终稿、图表、答辩材料以该侧为准 |

详细的同步流程见 `docs/两台设备同步指引.md`。

### 提交信息（commit message）约定

为区分不同来源的改动，提交信息统一使用前缀：

- `paper:` 论文正文、英文翻译、图表、正式提交件等 `docs/` 下论文相关改动
- `code:` `project/` 下的前端、后端、部署配置等项目代码改动
- `docs:` 仓库说明、协作指引等非论文文档改动
- `chore:` 仓库整理、`.gitignore`、归档等杂务

示例：
- `paper: 修订第四章图题与参考文献排版`
- `code: 修复 Windows Docker 环境下的 compose 卷路径`
- `docs: 更新 README 与多设备协作约定`

## 默认演示账号

- `admin / 123456`
- `demo / 123456`

## 使用说明

- 项目主代码位于 `project/`
- 项目入口说明见 `01_项目入口.md`
- 论文写作入口见 `02_论文入口.md`
- 毕业设计正式提交件见 `docs/正式提交/`
- 多设备同步流程见 `docs/两台设备同步指引.md`

## 注意事项

- `project/deploy/docker-compose.yml` 中的数据卷路径如使用本地磁盘绝对路径，首次部署前请按实际环境调整。
- 后端本地独立运行需要 Maven 环境；如果只走容器化部署，可直接使用 Docker 构建。
- 前端构建产物位于 `project/frontend/dist/`，由 Nginx 挂载；该目录不入库，需本地构建生成。
- `node_modules/`、`target/`、`logs/`、`dist/` 等生成物及查重报告、往届参考资料均不纳入仓库（见 `.gitignore`）。

## 当前状态

- 项目代码：Mac 与 Windows 两侧 `project/` 经 tree hash 校验完全一致，GitHub 版即 Windows 真实跑通版。
- 论文材料：以 Mac 最新终稿为准，正式提交件见 `docs/正式提交/`。

**更新时间：2026-06-24**
