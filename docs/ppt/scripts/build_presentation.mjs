import fs from 'fs';
import path from 'path';

const root = path.resolve('docs/ppt');
const slidesDir = path.join(root, 'slides');
const outputDir = path.join(root, 'output');

fs.mkdirSync(slidesDir, { recursive: true });
fs.mkdirSync(outputDir, { recursive: true });

const theme = [
  '1. 整体风格：本科毕业论文答辩的正式学术科技风，深蓝稳重、白底清晰，避免商业路演感。',
  '2. 背景：内容页以白色 #FFFFFF 和极浅蓝灰 #F5F8FC 为主，封面和致谢页使用深蓝 #0B1F3A。',
  '3. 字体：标题使用 Microsoft YaHei / PingFang SC，正文使用 Noto Sans SC / Arial，代码与命令使用 Consolas。',
  '4. 配色：主色深蓝 #0B1F3A，辅色学术蓝 #1F5EAB，点缀色青蓝 #1BA6A6，正文色 #1E293B，辅助灰 #64748B。',
  '5. 视觉元素：低装饰扁平化图表、细线框、学术蓝标签、流程箭头、表格与真实截图卡片；系统截图只裁剪标注，不 AI 重绘。'
].join('\\n');

const slides = [
  {
    file: 'slide_001.html',
    type: 'title',
    title: '基于容器化部署的云文件管理系统设计与实现',
    content_spec: '封面：展示论文题目、学生姓名杨佳星、学号04222088、学院计算机学院、专业网络工程、班级2203、指导教师王晓梅，视觉上突出云文件管理与容器化部署两个关键词。'
  },
  {
    file: 'slide_002.html',
    type: 'content',
    title: '研究背景与意义',
    content_spec: '说明高校课程设计、协作开发和日常学习中的文件存储、共享、预览需求；指出传统部署在环境依赖、数据库初始化、路径权限和演示迁移方面的问题；总结容器化部署的工程实践、教学答辩和技术应用意义。'
  },
  {
    file: 'slide_003.html',
    type: 'content',
    title: '国内外现状与课题定位',
    content_spec: '对比国外商业云盘和开源私有云方案、国内对象存储与企业网盘实践；明确本课题不与成熟商业平台竞争，而是聚焦本科毕业设计场景的环境快速复现、预览访问链路和轻量用户隔离。'
  },
  {
    file: 'slide_004.html',
    type: 'content',
    title: '研究目标与完成范围',
    content_spec: '展示系统完成范围：登录、上传、目录、预览、分享、回收站、统计、容器化部署与测试验证；同时明确系统边界：不表述为企业级多租户平台，不引入完整RBAC、组织架构、审批流等超出范围内容。'
  },
  {
    file: 'slide_005.html',
    type: 'content',
    title: '技术路线',
    content_spec: '概括前端Vue3与Element Plus、后端Spring Boot与MyBatis-Plus、MySQL、Redis、Nginx、Docker Compose等技术栈；展示从需求分析到总体设计、数据库与接口设计、核心模块实现、容器化部署、测试验证的路线。'
  },
  {
    file: 'slide_006.html',
    type: 'content',
    title: '系统总体设计',
    content_spec: '说明系统采用前后端分离B/S架构，按表现层、业务层、数据与资源层、部署与代理层组织；浏览器访问Nginx，Nginx转发/api请求到后端，后端连接MySQL、Redis和本地文件目录。'
  },
  {
    file: 'slide_007.html',
    type: 'content',
    title: '系统架构与容器化部署设计',
    content_spec: '说明Docker Compose编排lfs-nginx、lfs-backend、lfs-mysql、lfs-redis四个核心容器；展示统一入口、内部网络、健康检查、数据卷持久化和启动依赖关系。'
  },
  {
    file: 'slide_008.html',
    type: 'content',
    title: '数据库设计',
    content_spec: '以lfs_file为核心说明sys_user、lfs_file_share、lfs_file_trash、lfs_file_trash_detail、lfs_file_thum等表关系；强调lfs_file.user_id与idx_user_id对轻量双用户隔离和统计查询的作用。'
  },
  {
    file: 'slide_009.html',
    type: 'content',
    title: '核心功能实现一：文件管理',
    content_spec: '介绍文件上传、列表、目录、移动、重命名和删除；强调上传过程同时保存物理文件与lfs_file元数据，目录树按is_dir记录构建，移动通过parent_id调整。'
  },
  {
    file: 'slide_010.html',
    type: 'content',
    title: '核心功能实现二：预览与分享',
    content_spec: '介绍/api/files、/api/thum、/api/trans三类预览路径由FilePreviewController统一归一化处理；通过secret+expire签名、防路径穿越、响应头设置保障访问；分享采用分享码+有效期+匿名访问。'
  },
  {
    file: 'slide_011.html',
    type: 'content',
    title: '核心功能实现三：用户隔离、回收站与统计',
    content_spec: '说明admin和demo两类账号视角：管理员查看全量数据，demo仅查看自身文件；CurrentUserService识别当前用户，SQL追加user_id条件；回收站和存储统计与同一用户视角保持一致。'
  },
  {
    file: 'slide_012.html',
    type: 'content',
    title: '系统测试',
    content_spec: '展示测试环境、T1-T17功能与异常测试、五个自动化测试类及通过情况；强调测试重点是容器启动、数据库兼容补丁、登录、上传、预览、分享、回收站、双用户隔离和非法访问拒绝。'
  },
  {
    file: 'slide_013.html',
    type: 'content',
    title: '系统运行效果展示',
    content_spec: '用真实系统截图展示登录入口、文件列表、预览成功、分享访问、回收站/统计和Docker容器运行状态；强调系统可运行、可部署、可演示、可验证。'
  },
  {
    file: 'slide_014.html',
    type: 'title',
    title: '总结与展望',
    content_spec: '总结已完成的系统架构、核心业务闭环、轻量用户隔离、预览链路修复、容器化部署与测试验证；说明不足包括权限模型简单、本地目录存储、监控运维基础；展望团队空间、对象存储、分享策略、监控日志和预览缓存优化，并致谢。'
  }
];

const css = `
*{box-sizing:border-box}
body{width:1280px;height:720px;overflow:hidden;margin:0;padding:0;position:relative;font-family:"Microsoft YaHei","PingFang SC",Arial,sans-serif;color:#1e293b;background:#fff}
.page{width:1280px;height:720px;position:relative;overflow:hidden;background:#f5f8fc}
.deep{background:#0b1f3a;color:#eef6ff}
.content{position:relative;z-index:2;padding:48px 64px;width:100%;height:100%}
.kicker{font-size:16px;font-weight:700;color:#1ba6a6;letter-spacing:0}
.title{font-size:38px;line-height:1.16;font-weight:800;color:#0b1f3a;margin:8px 0 22px}
.deep .title{color:#fff}
.subtitle{font-size:21px;line-height:1.6;color:#cfe3ff;max-width:920px}
.grid{display:grid;gap:22px}
.cols2{grid-template-columns:1fr 1fr}
.cols3{grid-template-columns:repeat(3,1fr)}
.cols4{grid-template-columns:repeat(4,1fr)}
.card{background:#fff;border:1px solid #dbe7f5;border-radius:18px;padding:22px;box-shadow:0 14px 34px rgba(15,46,95,.08)}
.card h3{margin:0 0 10px;font-size:23px;color:#0b1f3a}
.card p,.card li{font-size:18px;line-height:1.45;color:#334155;margin:0}
.muted{color:#64748b}
.tag{display:inline-flex;align-items:center;justify-content:center;padding:6px 12px;border-radius:999px;background:#e7f6ff;color:#155e9d;font-size:16px;font-weight:700}
.accent{color:#1ba6a6}
.line{height:2px;background:#1f5eab;opacity:.16}
.band{position:absolute;background:#eaf3ff;border:1px solid #d8e8fb}
.band.dark{background:#133765;border-color:#2b5f9f}
.mesh{position:absolute;inset:0;background-image:linear-gradient(rgba(255,255,255,.05) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,.05) 1px,transparent 1px);background-size:36px 36px}
.chip{display:inline-block;padding:8px 12px;border-radius:10px;background:#eef6ff;color:#1f5eab;font-weight:700;font-size:17px;margin:4px 6px 4px 0}
.flow{display:flex;align-items:center;gap:12px}
.flow .node{flex:1;min-height:76px;background:#fff;border:1px solid #dbe7f5;border-radius:16px;padding:14px;text-align:center;box-shadow:0 10px 26px rgba(15,46,95,.07)}
.flow .node strong{display:block;font-size:20px;color:#0b1f3a;margin-bottom:6px}
.flow .node span{font-size:15px;color:#64748b;line-height:1.3}
.arrow{font-size:28px;color:#1f5eab;font-weight:800}
.imgbox{background:#fff;border:1px solid #dbe7f5;border-radius:16px;padding:12px;box-shadow:0 14px 34px rgba(15,46,95,.08);display:flex;align-items:center;justify-content:center;overflow:hidden}
.imgbox img{max-width:100%;max-height:100%;object-fit:contain}
.screenshot{border:1px solid #c7d6ea;border-radius:12px;overflow:hidden;background:#fff;box-shadow:0 12px 28px rgba(15,46,95,.16)}
.screenshot img{width:100%;height:100%;object-fit:cover;display:block}
.caption{font-size:14px;color:#64748b;margin-top:8px;text-align:center}
.table{width:100%;border-collapse:separate;border-spacing:0;background:#fff;border:1px solid #dbe7f5;border-radius:16px;overflow:hidden;box-shadow:0 14px 34px rgba(15,46,95,.08)}
.table th{background:#0f3b70;color:#fff;font-size:17px;text-align:left;padding:13px 16px}
.table td{font-size:16px;color:#334155;padding:12px 16px;border-top:1px solid #e5edf7;vertical-align:top}
.num{font-size:42px;font-weight:900;color:#1f5eab;line-height:1}
.small{font-size:15px;line-height:1.45}
.quote{font-size:28px;line-height:1.35;font-weight:800;color:#0b1f3a}
.avoid{background:#fff5f5;border-color:#fecaca}
.ok{background:#f0fbff;border-color:#bae6fd}
.mono{font-family:Consolas,Menlo,monospace}
.footer-mark{position:absolute;left:64px;bottom:42px;font-size:18px;color:#9fb9d9}
.deep .footer-mark{color:#b8cdeb}
`;

const htmlHead = (title) => `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    window.tailwind = window.tailwind || {};
    tailwind.config = { theme: { extend: { colors: { primary: '#0B1F3A', secondary: '#1F5EAB', accent: '#1BA6A6', 'text-primary': '#1E293B' }, fontFamily: { heading: ['Microsoft YaHei','PingFang SC','sans-serif'], body: ['Noto Sans SC','Arial','sans-serif'] } } } };
  </script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <title>${title}</title>
  <style>${css}</style>
</head>
<body class="w-[1280px] h-[720px] overflow-hidden m-0 p-0 relative">`;

const end = `<script>if(window.lucide){lucide.createIcons();}</script></body></html>`;
const fig = '../assets/figures/';
const media = '../assets/docx_media/';

const page = (title, body, cls = '') => `${htmlHead(title)}<div class="page ${cls}">${body}</div>${end}`;

const slideHtml = [
page(slides[0].title, `
  <div class="mesh"></div>
  <div class="band dark" style="width:520px;height:520px;right:-160px;top:-120px;transform:rotate(18deg);opacity:.7"></div>
  <div class="band" style="width:460px;height:120px;left:-80px;bottom:84px;transform:rotate(-10deg);opacity:.16"></div>
  <div class="content" style="padding:64px 78px">
    <div style="display:flex;align-items:center;gap:18px;margin-bottom:54px">
      <div style="width:76px;height:76px;border-radius:18px;background:#ffffff;display:flex;align-items:center;justify-content:center;padding:8px">
        <img src="${media}image1.png" style="width:100%;height:100%;object-fit:contain">
      </div>
      <div>
        <div class="kicker" style="color:#8be4ee">本科毕业论文答辩</div>
        <div style="font-size:19px;color:#bdd6f6;margin-top:6px">计算机学院 · 网络工程</div>
      </div>
    </div>
    <div style="max-width:900px">
      <div style="font-size:54px;line-height:1.18;font-weight:900;color:#fff">基于容器化部署的<br>云文件管理系统设计与实现</div>
      <div class="subtitle" style="margin-top:26px">围绕文件管理业务闭环、预览访问链路与 Docker Compose 部署复现展开设计与验证</div>
    </div>
    <div style="position:absolute;left:78px;bottom:62px;display:grid;grid-template-columns:repeat(3,auto);gap:18px 34px;color:#d8e7fb;font-size:19px">
      <div>学生：杨佳星</div><div>学号：04222088</div><div>班级：2203</div>
      <div>专业：网络工程</div><div>指导教师：王晓梅</div><div>答辩材料</div>
    </div>
  </div>
`, 'deep'),

page(slides[1].title, `
  <div class="content">
    <div class="kicker">01 选题背景</div>
    <div class="title">研究背景与意义</div>
    <div class="grid cols3" style="height:238px">
      <div class="card"><h3>文件需求增加</h3><p>课程设计、协作开发和日常学习中，文件存储、共享与在线预览需求持续增加。</p></div>
      <div class="card"><h3>传统部署易失控</h3><p>本机环境、数据库初始化、静态路径和目录权限会影响系统迁移与答辩演示。</p></div>
      <div class="card"><h3>容器化降低差异</h3><p>Docker Compose 将前端、后端、数据库、缓存和代理写入统一编排。</p></div>
    </div>
    <div style="margin-top:28px" class="flow">
      <div class="node"><strong>工程实践</strong><span>从功能实现推进到可运行、可复现的完整工程</span></div>
      <div class="arrow">→</div>
      <div class="node"><strong>教学答辩</strong><span>围绕上传、预览、分享、回收形成清晰演示链路</span></div>
      <div class="arrow">→</div>
      <div class="node"><strong>技术应用</strong><span>验证前后端分离、数据库、缓存、代理与容器编排组合</span></div>
    </div>
    <div class="quote" style="position:absolute;left:64px;bottom:52px;width:980px">本课题关注的不只是“能做页面”，而是系统能否稳定部署、运行、演示和验证。</div>
  </div>
`),

page(slides[2].title, `
  <div class="content">
    <div class="kicker">02 现状分析</div>
    <div class="title">国内外现状与课题定位</div>
    <table class="table">
      <tr><th style="width:27%">方向</th><th>代表特点</th><th style="width:34%">本课题取舍</th></tr>
      <tr><td><b>商业云盘</b><br><span class="muted">Dropbox / Google Drive / OneDrive</span></td><td>同步、在线预览、链接分享、权限控制成熟</td><td>不与成熟商业平台竞争，不强调海量用户和企业级规模</td></tr>
      <tr><td><b>开源私有云</b><br><span class="muted">Nextcloud / Seafile</span></td><td>私有化部署、存储自主可控、二次扩展能力强</td><td>吸收私有部署思路，收敛到毕设可实现范围</td></tr>
      <tr><td><b>国内对象存储</b><br><span class="muted">OSS / COS / OBS</span></td><td>支撑企业网盘、内容分发和多媒体平台</td><td>本系统采用本地目录持久化，保留后续对象存储扩展方向</td></tr>
    </table>
    <div class="grid cols3" style="margin-top:26px">
      <div class="card ok"><h3>环境快速复现</h3><p>解决答辩环境依赖多、配置复杂、迁移困难的问题。</p></div>
      <div class="card ok"><h3>预览访问链路</h3><p>Nginx、路径归一化、签名校验与本地文件读取保持一致。</p></div>
      <div class="card ok"><h3>轻量用户隔离</h3><p>通过 admin/demo 与 <span class="mono">lfs_file.user_id</span> 实现视角隔离。</p></div>
    </div>
  </div>
`),

page(slides[3].title, `
  <div class="content">
    <div class="kicker">03 研究内容</div>
    <div class="title">研究目标与完成范围</div>
    <div style="display:grid;grid-template-columns:1.25fr .75fr;gap:28px">
      <div class="card">
        <h3>完成的系统闭环</h3>
        <div class="grid cols4" style="margin-top:18px;gap:14px">
          ${['登录认证','文件上传','目录管理','在线预览','分享访问','回收站','存储统计','容器部署'].map((x,i)=>`<div style="background:#f3f8ff;border:1px solid #d7e8fb;border-radius:14px;padding:16px;text-align:center"><div class="num" style="font-size:28px">0${i+1}</div><div style="font-size:18px;font-weight:700;color:#0b1f3a;margin-top:7px">${x}</div></div>`).join('')}
        </div>
      </div>
      <div class="card avoid">
        <h3>明确不扩展</h3>
        <p style="margin-bottom:14px">答辩中不将系统包装为企业级多租户 SaaS。</p>
        <div class="chip">完整 RBAC</div><div class="chip">组织架构</div><div class="chip">审批流</div><div class="chip">企业级配额</div><div class="chip">海量用户</div><div class="chip">大规模分布式存储</div>
      </div>
    </div>
    <div class="card" style="margin-top:26px;background:#0f3b70;color:#e8f3ff">
      <div style="font-size:23px;font-weight:800;margin-bottom:8px">答辩口径</div>
      <div style="font-size:20px;line-height:1.45">面向本科毕业设计场景的轻量级云文件管理系统，重点强调可运行、可部署、可演示、可验证。</div>
    </div>
  </div>
`),

page(slides[4].title, `
  <div class="content">
    <div class="kicker">04 技术选型</div>
    <div class="title">技术路线</div>
    <div class="flow" style="margin-top:14px">
      ${[
        ['需求分析','明确功能闭环与系统边界'],
        ['概要设计','划分架构、模块和访问关系'],
        ['数据库与接口','设计核心表和REST接口'],
        ['核心实现','上传、预览、分享、隔离'],
        ['容器部署','Compose编排四容器'],
        ['测试验证','功能、异常与自动化测试']
      ].map((n,i)=>`<div class="node"><strong>${n[0]}</strong><span>${n[1]}</span></div>${i<5?'<div class="arrow">→</div>':''}`).join('')}
    </div>
    <div class="grid cols3" style="margin-top:34px">
      <div class="card"><h3>前端</h3><p><span class="chip">Vue 3</span><span class="chip">Element Plus</span><span class="chip">Vite</span><span class="chip">Vue Router</span></p></div>
      <div class="card"><h3>后端与数据</h3><p><span class="chip">Spring Boot 3.2.2</span><span class="chip">MyBatis-Plus</span><span class="chip">MySQL 8</span><span class="chip">Redis 7</span></p></div>
      <div class="card"><h3>部署与预览</h3><p><span class="chip">Nginx</span><span class="chip">Docker Compose</span><span class="chip">Aspose</span><span class="chip">PDFBox / JAVE</span></p></div>
    </div>
  </div>
`),

page(slides[5].title, `
  <div class="content">
    <div class="kicker">05 总体设计</div>
    <div class="title">系统总体设计</div>
    <div style="display:grid;grid-template-columns:.9fr 1.1fr;gap:28px;height:540px">
      <div class="grid" style="gap:14px">
        ${[
          ['表现层','登录页、文件管理页、分享访问页、回收站页、预览页'],
          ['业务层','登录认证、文件管理、预览、分享、回收站、统计接口'],
          ['数据与资源层','MySQL 保存元数据，Redis 保存短生命周期状态，本地目录保存物理文件'],
          ['部署与代理层','Nginx 统一访问入口，Docker Compose 编排核心容器']
        ].map(x=>`<div class="card" style="padding:18px"><h3>${x[0]}</h3><p>${x[1]}</p></div>`).join('')}
      </div>
      <div class="imgbox"><img src="${fig}fig_3_1_system_architecture.png" alt="系统总体架构图"></div>
    </div>
  </div>
`),

page(slides[6].title, `
  <div class="content">
    <div class="kicker">06 部署设计</div>
    <div class="title">系统架构与容器化部署设计</div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:28px">
      <div class="card" style="height:470px">
        <h3>Docker Compose 四容器编排</h3>
        <div class="grid cols2" style="margin-top:20px;gap:18px">
          ${[
            ['lfs-nginx','统一入口与反向代理','80'],
            ['lfs-backend','Spring Boot 业务接口','内部 8919'],
            ['lfs-mysql','业务数据持久化','内部 3306'],
            ['lfs-redis','临时状态与上传分片标识','内部 6379']
          ].map(x=>`<div style="border:1px solid #dbe7f5;background:#f6faff;border-radius:16px;padding:18px"><div style="font-size:22px;font-weight:800;color:#0b1f3a">${x[0]}</div><div style="font-size:16px;color:#64748b;margin-top:8px;height:42px">${x[1]}</div><div class="tag">${x[2]}</div></div>`).join('')}
        </div>
      </div>
      <div class="imgbox" style="height:470px"><img src="${fig}fig_3_2_network_access.png" alt="网络访问关系图"></div>
    </div>
    <div class="card" style="margin-top:22px;padding:18px 22px"><p><b>部署流程：</b>执行 <span class="mono">docker compose up -d --build</span> 后，MySQL 健康检查通过，后端启动并执行兼容补丁，最终通过 Nginx 入口访问系统。</p></div>
  </div>
`),

page(slides[7].title, `
  <div class="content">
    <div class="kicker">07 数据设计</div>
    <div class="title">数据库设计</div>
    <div style="display:grid;grid-template-columns:1.12fr .88fr;gap:28px;height:540px">
      <div class="imgbox"><img src="${fig}fig_3_4_er_diagram.png" alt="数据库ER图"></div>
      <div class="grid" style="gap:18px">
        <div class="card"><h3>核心表</h3><p><span class="mono">lfs_file</span> 保存文件和目录元数据，是分享、回收站、缩略图等关系的中心。</p></div>
        <div class="card ok"><h3>用户隔离字段</h3><p><span class="mono">lfs_file.user_id</span> 标识文件归属，<span class="mono">idx_user_id</span> 支持按用户过滤查询。</p></div>
        <div class="card"><h3>兼容性设计</h3><p>启动期自动检查并补齐 <span class="mono">user_id</span>、索引和 demo 演示账号，适配旧 MySQL 数据卷。</p></div>
      </div>
    </div>
  </div>
`),

page(slides[8].title, `
  <div class="content">
    <div class="kicker">08 核心实现</div>
    <div class="title">核心功能实现一：文件管理</div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:28px;height:506px">
      <div class="imgbox"><img src="${fig}fig_4_3_upload_flow.png" alt="文件上传流程图"></div>
      <div>
        <div class="card" style="height:214px;margin-bottom:22px">
          <h3>文件管理链路</h3>
          <p>上传时保存物理文件，并在 <span class="mono">lfs_file</span> 写入元数据；目录树按 <span class="mono">is_dir=1</span> 的层级记录返回；移动主要修改 <span class="mono">parent_id</span>。</p>
        </div>
        <div class="screenshot" style="height:270px"><img src="${media}image17.png" alt="文件列表截图"></div>
        <div class="caption">真实系统截图：文件列表与目录管理界面</div>
      </div>
    </div>
  </div>
`),

page(slides[9].title, `
  <div class="content">
    <div class="kicker">09 核心实现</div>
    <div class="title">核心功能实现二：预览与分享</div>
    <div style="display:grid;grid-template-columns:1.05fr .95fr;gap:28px;height:500px">
      <div class="imgbox"><img src="${fig}fig_4_8_preview_chain.png" alt="文件预览访问链路图"></div>
      <div class="grid" style="gap:16px">
        <div class="card" style="padding:18px"><h3>预览访问控制</h3><p>统一处理 <span class="mono">/api/files</span>、<span class="mono">/api/thum</span>、<span class="mono">/api/trans</span>，完成路径归一化、签名校验和越界检查。</p></div>
        <div class="card" style="padding:18px"><h3>分享边界</h3><p>采用“分享码 + 有效期 + 匿名访问”，不扩展为复杂协作权限系统。</p></div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px">
          <div class="screenshot" style="height:158px"><img src="${media}image22.png" alt="分享创建截图"></div>
          <div class="screenshot" style="height:158px"><img src="${media}image23.png" alt="分享访问截图"></div>
        </div>
      </div>
    </div>
  </div>
`),

page(slides[10].title, `
  <div class="content">
    <div class="kicker">10 核心实现</div>
    <div class="title">核心功能实现三：用户隔离、回收站与统计</div>
    <div style="display:grid;grid-template-columns:.9fr 1.1fr;gap:28px">
      <div class="grid" style="gap:18px">
        <div class="card ok"><h3>admin 管理员视角</h3><p>可查看和管理全量文件数据，用于展示历史文件与完整数据。</p></div>
        <div class="card ok"><h3>demo 普通用户视角</h3><p>查询时追加 <span class="mono">user_id = currentUserId</span>，仅命中自身文件空间。</p></div>
        <div class="card"><h3>统一视角</h3><p>文件列表、回收站、存储统计共享同一用户视角，避免可见范围与统计范围不一致。</p></div>
      </div>
      <div class="grid cols2" style="gap:16px">
        <div><div class="screenshot" style="height:224px"><img src="${media}image18.png" alt="隔离视角截图"></div><div class="caption">用户文件空间</div></div>
        <div><div class="screenshot" style="height:224px"><img src="${media}image25.png" alt="管理员视角截图"></div><div class="caption">文件列表/统计视角</div></div>
        <div style="grid-column:span 2"><div class="screenshot" style="height:190px"><img src="${media}image31.jpeg" alt="运行对比截图"></div><div class="caption">真实测试截图：多视角访问结果</div></div>
      </div>
    </div>
  </div>
`),

page(slides[11].title, `
  <div class="content">
    <div class="kicker">11 测试验证</div>
    <div class="title">系统测试</div>
    <div style="display:grid;grid-template-columns:.88fr 1.12fr;gap:28px;height:520px">
      <div class="imgbox"><img src="${fig}fig_5_1_test_scope.png" alt="测试范围图"></div>
      <div>
        <div class="grid cols3" style="gap:14px;margin-bottom:18px">
          <div class="card" style="padding:18px;text-align:center"><div class="num">17</div><p>功能与异常测试项</p></div>
          <div class="card" style="padding:18px;text-align:center"><div class="num">5</div><p>自动化测试类</p></div>
          <div class="card" style="padding:18px;text-align:center"><div class="num">24</div><p>自动化用例通过</p></div>
        </div>
        <table class="table">
          <tr><th>测试类</th><th>覆盖内容</th><th>通过</th></tr>
          <tr><td class="mono">CurrentUserServiceTest</td><td>token 校验、用户编号、isAdmin</td><td>5/5</td></tr>
          <tr><td class="mono">SchemaCompatibilityRunnerTest</td><td>字段、索引、demo 账号补齐</td><td>4/4</td></tr>
          <tr><td class="mono">FileShareServiceTest</td><td>分享码、冲突重生、过期访问</td><td>6/6</td></tr>
          <tr><td class="mono">FileServiceStorageStatsTest</td><td>管理员与演示用户统计</td><td>4/4</td></tr>
          <tr><td class="mono">FilePreviewControllerTest</td><td>路径归一化、签名失效、越界拒绝</td><td>5/5</td></tr>
        </table>
      </div>
    </div>
  </div>
`),

page(slides[12].title, `
  <div class="content">
    <div class="kicker">12 运行效果</div>
    <div class="title">系统运行效果展示</div>
    <div class="grid cols3" style="gap:18px">
      <div><div class="screenshot" style="height:156px"><img src="${media}image14.png" alt="登录页"></div><div class="caption">登录认证入口</div></div>
      <div><div class="screenshot" style="height:156px"><img src="${media}image17.png" alt="文件列表"></div><div class="caption">文件列表与目录管理</div></div>
      <div><div class="screenshot" style="height:156px"><img src="${media}image19.png" alt="预览"></div><div class="caption">文件预览返回正常</div></div>
      <div><div class="screenshot" style="height:156px"><img src="${media}image22.png" alt="分享创建"></div><div class="caption">分享创建与有效期</div></div>
      <div><div class="screenshot" style="height:156px"><img src="${media}image24.png" alt="回收站统计"></div><div class="caption">统计/访问视角验证</div></div>
      <div><div class="screenshot" style="height:156px"><img src="${media}image32.png" alt="Docker运行状态"></div><div class="caption">Docker Compose 运行状态</div></div>
    </div>
    <div class="card" style="margin-top:24px;padding:18px 22px;background:#0f3b70;color:#eef6ff">
      <div style="font-size:22px;font-weight:800;margin-bottom:6px">运行效果结论</div>
      <div style="font-size:19px">系统完成从登录、文件管理、预览、分享、回收站到容器化部署的可演示闭环。</div>
    </div>
  </div>
`),

page(slides[13].title, `
  <div class="mesh"></div>
  <div class="band dark" style="width:420px;height:420px;right:-130px;top:-110px;transform:rotate(15deg);opacity:.68"></div>
  <div class="content" style="padding:58px 74px">
    <div class="kicker" style="color:#8be4ee">13 总结与展望</div>
    <div class="title" style="font-size:48px">总结与展望</div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:28px;margin-top:22px">
      <div class="card" style="background:rgba(255,255,255,.96)">
        <h3>已完成工作</h3>
        <p>完成前端、后端、数据层、反向代理与容器编排的系统架构；实现登录、上传、预览、分享、回收站、统计等业务闭环；完成轻量用户隔离、预览链路修复和测试验证。</p>
      </div>
      <div class="card" style="background:rgba(255,255,255,.96)">
        <h3>不足与展望</h3>
        <p>权限模型较简单，存储仍采用本地目录，监控运维能力基础；后续可扩展团队空间、对象存储、分享策略、日志监控和预览转码缓存。</p>
      </div>
    </div>
    <div style="position:absolute;left:74px;bottom:86px;font-size:44px;font-weight:900;color:#fff">敬请各位老师批评指正</div>
    <div class="footer-mark">谢谢！</div>
  </div>
`, 'deep')
];

for (let i = 0; i < slides.length; i++) {
  fs.writeFileSync(path.join(slidesDir, slides[i].file), slideHtml[i], 'utf8');
}

fs.writeFileSync(path.join(root, 'presentation.json'), JSON.stringify({
  title: '基于容器化部署的云文件管理系统设计与实现',
  theme,
  slides
}, null, 2), 'utf8');

const scripts = [
  ['1. 封面', '各位老师好，我是网络工程 2203 班的杨佳星。我的毕业论文题目是《基于容器化部署的云文件管理系统设计与实现》。本课题围绕一个轻量级云文件管理系统展开，重点不是单纯做页面展示，而是完成从业务功能、访问链路到 Docker Compose 部署验证的一套可运行系统。下面我将从选题背景、系统设计、核心实现、测试验证和总结展望几个方面进行汇报。'],
  ['2. 研究背景与意义', '本课题的背景来自日常学习和课程设计中的文件管理需求。随着电子文档、图片、视频和项目资料越来越多，单纯依赖本地文件夹或个人网盘，容易出现文件分散、版本混乱、预览受限等问题。另一方面，毕业设计系统在答辩时还要面对部署环境变化的问题，比如数据库、缓存、静态资源路径和文件目录权限都可能影响演示。基于这些问题，我选择用容器化方式把前端、后端、数据库、缓存和反向代理统一编排，让系统更容易恢复运行状态。这个工作既有工程实践意义，也能支撑答辩现场的稳定演示。'],
  ['3. 国内外现状与课题定位', '从现有系统看，Dropbox、Google Drive、OneDrive 等商业云盘在文件同步、在线预览、链接分享和权限控制方面已经非常成熟；Nextcloud、Seafile 这类开源方案则更强调私有化部署和扩展能力。国内很多应用场景也依赖阿里云 OSS、腾讯云 COS、华为云 OBS 这类对象存储服务。本课题并不是要和这些成熟平台竞争，而是收敛到本科毕业设计场景，重点解决三个问题：系统环境能够快速复现，预览访问链路能够统一，用户视角能够做轻量隔离。'],
  ['4. 研究目标与完成范围', '本系统的目标是完成一个轻量级云文件管理系统的业务闭环。具体包括登录认证、文件上传、目录管理、在线预览、分享访问、回收站、存储统计和容器化部署。这里需要特别说明系统边界：论文和答辩中不把它表述为企业级多租户平台，也不引入完整 RBAC、组织架构、审批流或者大规模分布式存储等没有实现的数据和能力。我的答辩口径是可运行、可部署、可演示、可验证的本科毕业设计系统。'],
  ['5. 技术路线', '技术路线采用前后端分离和容器化部署。前端使用 Vue 3、Element Plus、Vite 和 Vue Router，主要实现登录、文件列表、上传、分享、回收站和预览页面。后端使用 Spring Boot 3.2.2 和 MyBatis-Plus 提供 REST 接口，MySQL 保存用户、文件、分享、回收站等结构化数据，Redis 保存上传分片状态和转码进度等短生命周期数据。部署层使用 Nginx 和 Docker Compose，把四个核心容器统一编排。整体流程是需求分析、概要设计、数据库与接口设计、核心模块实现、容器化部署，最后通过测试验证。'],
  ['6. 系统总体设计', '系统整体采用 B/S 架构和前后端分离模式。从层次上看，表现层由 Vue 和 Element Plus 构成，提供登录、文件管理、分享访问、回收站和预览界面；业务层由 Spring Boot 提供认证、文件、预览、分享、回收站和统计接口；数据与资源层由 MySQL、Redis 和本地文件目录组成；部署与代理层由 Nginx 和 Docker Compose 承担。浏览器统一访问 Nginx，Nginx 一方面返回前端静态资源，另一方面把 /api 请求转发给后端，后端再完成数据库访问、缓存读写和文件访问控制。'],
  ['7. 系统架构与容器化部署设计', '容器化部分是本课题的重点之一。系统通过 Docker Compose 编排四个核心容器：lfs-nginx 作为统一入口和反向代理，lfs-backend 运行 Spring Boot 业务服务，lfs-mysql 保存业务数据，lfs-redis 保存临时状态。MySQL、Redis 和上传文件目录都通过数据卷持久化，避免容器删除重建后业务数据丢失。启动时执行 docker compose up -d --build，MySQL 健康检查通过后后端再启动，并执行数据库兼容补丁，最后通过 Nginx 入口访问系统。'],
  ['8. 数据库设计', '数据库设计以 lfs_file 文件表为核心。sys_user 保存用户账号和身份信息；lfs_file 保存文件和目录的元数据；lfs_file_share 保存分享码、过期时间和访问记录；lfs_file_trash 和 lfs_file_trash_detail 保存回收站批次和明细；lfs_file_thum 保存缩略图信息。其中 lfs_file.user_id 是轻量用户隔离的核心字段，idx_user_id 用来支持按用户过滤查询。考虑到演示环境可能复用旧数据库卷，系统启动时还会自动检查并补齐 user_id 字段、索引和 demo 演示账号。'],
  ['9. 核心功能实现一：文件管理', '文件管理模块负责上传、列表、目录、移动、重命名和删除。上传时，系统不仅要把物理文件保存到本地持久化目录，还要在 lfs_file 表中写入文件名、大小、类型、父目录和用户归属等元数据。目录树本质上也是文件表中的目录记录，后端按层级返回 is_dir 等于 1 的记录。移动文件时主要修改 parent_id，重命名和同名校验则配合前后端逻辑完成。这样文件系统的页面操作和数据库元数据能够保持一致。'],
  ['10. 核心功能实现二：预览与分享', '预览模块主要解决路径和安全访问问题。系统把 /api/files、/api/thum、/api/trans 这三类资源路径统一交给 FilePreviewController 处理。控制器先做路径归一化，再校验 secret 和 expire 组成的签名，同时检查文件路径是否越界，最后根据文件类型设置响应头。收尾阶段修复的关键问题就是路径前缀错位和签名编码不一致。分享模块采用分享码加有效期的方式，访问分享页不需要登录，但它仍然只提供轻量匿名访问，不扩展成复杂协作权限系统。'],
  ['11. 核心功能实现三：用户隔离、回收站与统计', '用户隔离采用 admin 和 demo 两类账号。admin 表示管理员视角，可以查看和管理全量文件数据；demo 表示普通演示用户，只能查看自己的文件空间。这个隔离不是靠前端隐藏按钮，而是后端通过 CurrentUserService 解析当前用户，再在文件列表、回收站和存储统计等查询中追加 user_id 条件。回收站支持删除、恢复、彻底删除和清空，存储统计按用户视角聚合空间使用量，从而避免列表不可见但统计仍计入的问题。'],
  ['12. 系统测试', '测试部分主要覆盖容器层、业务层、访问控制层和异常回归层。论文中整理了 T1 到 T17 的测试项，包括四容器启动、数据库兼容补丁、admin 和 demo 登录、文件上传、原文件预览、缩略图预览、分享创建与访问、回收站恢复和彻底删除、双用户隔离、超大文件上传拒绝、过期分享码访问和非法路径预览拒绝。自动化测试包含五个测试类，共 24 个用例，覆盖当前用户识别、兼容补丁、分享服务、存储统计和预览控制器。这里的重点是验证关键链路稳定，而不是包装成正式性能评测。'],
  ['13. 系统运行效果展示', '这一页展示的是系统真实运行截图。可以看到系统具备登录入口、文件列表和目录管理页面，文件预览能够正常返回，分享创建和匿名访问能够形成闭环，统计和用户视角也能配合验证。右下角展示 Docker Compose 运行状态，说明前端入口、后端服务、MySQL 和 Redis 能够按编排启动。总体来看，系统已经完成从登录、文件管理、预览、分享、回收站到容器化部署的演示闭环。'],
  ['14. 总结与展望', '最后进行总结。本课题完成了前端、后端、数据层、反向代理和容器编排的整体架构，实现了登录认证、文件上传、目录管理、在线预览、分享访问、回收站和存储统计等核心业务；通过 user_id、CurrentUserService 和 SQL 条件实现轻量用户隔离；通过路径归一化和签名校验修复预览链路；并通过功能测试、异常测试和自动化测试完成验证。不足之处是权限模型还比较简单，存储仍以本地目录为主，监控和运维能力也比较基础。后续可以扩展团队空间、对象存储、分享策略、日志监控和预览缓存优化。我的汇报到此结束，感谢各位老师，敬请批评指正。']
];

const scriptMd = `# 《基于容器化部署的云文件管理系统设计与实现》答辩讲解稿

> 建议时长：8-10 分钟。讲解原则：本科毕业论文答辩口径，突出真实实现、部署验证和系统边界，不做商业路演式夸大。

${scripts.map(([h, t]) => `## ${h}

${t}
`).join('\n')}
`;

fs.writeFileSync(path.join(root, '基于容器化部署的云文件管理系统设计与实现-答辩讲解稿.md'), scriptMd, 'utf8');

console.log(`Wrote ${slides.length} slides to ${slidesDir}`);
console.log(`Wrote ${path.join(root, 'presentation.json')}`);
console.log(`Wrote speaker script markdown`);
