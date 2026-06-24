import fs from 'fs';
import path from 'path';
import pptxgen from '../node_modules/pptxgenjs/dist/pptxgen.cjs.js';

const root = path.resolve('docs/ppt');
const out = path.join(root, 'output', '基于容器化部署的云文件管理系统设计与实现_答辩PPT.pptx');
const fig = path.join(root, 'assets', 'figures');
const media = path.join(root, 'assets', 'docx_media');

const pptx = new pptxgen();
pptx.layout = 'LAYOUT_WIDE';
pptx.author = '杨佳星';
pptx.company = '西安邮电大学';
pptx.subject = '本科毕业论文答辩';
pptx.title = '基于容器化部署的云文件管理系统设计与实现';
pptx.lang = 'zh-CN';
pptx.theme = {
  headFontFace: 'Microsoft YaHei',
  bodyFontFace: 'Microsoft YaHei',
  lang: 'zh-CN',
};
pptx.defineLayout({ name: 'LAYOUT_WIDE', width: 13.333, height: 7.5 });
pptx.layout = 'LAYOUT_WIDE';
pptx.margin = 0;

const W = 13.333;
const H = 7.5;

const C = {
  refBlue: '0F4C82',
  refBlue2: '1F497D',
  dark: '0A2442',
  ink: '1E293B',
  muted: '64748B',
  pale: 'EEF4FA',
  pale2: 'F7FAFD',
  line: 'D8E4EF',
  panel: 'FFFFFF',
  accent: '2A9FD6',
  good: 'EAF7FF',
  warn: 'FFF6E8',
};

function asset(name, dir = fig) {
  return path.join(dir, name);
}

function imageExists(name, dir = fig) {
  const p = asset(name, dir);
  return fs.existsSync(p) ? p : null;
}

function setBg(slide, mode = 'light') {
  const dark = mode === 'dark';
  slide.background = { color: dark ? C.dark : C.pale2 };
  slide.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: W, h: H,
    fill: { color: dark ? C.dark : C.pale2 },
    line: { color: dark ? C.dark : C.pale2 },
  });
  if (dark) {
    for (let x = 0; x < W; x += 0.45) {
      slide.addShape(pptx.ShapeType.line, { x, y: 0, w: 0, h: H, line: { color: 'FFFFFF', transparency: 94, width: 0.35 } });
    }
    for (let y = 0; y < H; y += 0.45) {
      slide.addShape(pptx.ShapeType.line, { x: 0, y, w: W, h: 0, line: { color: 'FFFFFF', transparency: 94, width: 0.35 } });
    }
    slide.addShape(pptx.ShapeType.rect, {
      x: 8.8, y: -0.35, w: 4.2, h: 1.15, rotate: 0,
      fill: { color: C.refBlue, transparency: 8 },
      line: { color: C.refBlue, transparency: 100 },
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: 9.65, y: 0.58, w: 4.4, h: 0.5, rotate: 0,
      fill: { color: '2D6EA3', transparency: 20 },
      line: { color: '2D6EA3', transparency: 100 },
    });
  } else {
    slide.addShape(pptx.ShapeType.rect, {
      x: 0, y: 0, w: 0.16, h: H,
      fill: { color: C.refBlue },
      line: { color: C.refBlue },
    });
    slide.addShape(pptx.ShapeType.rect, {
      x: 0.16, y: 0, w: W - 0.16, h: 0.1,
      fill: { color: 'FFFFFF' },
      line: { color: 'FFFFFF' },
    });
  }
}

function addHeader(slide, idx, title, chapter = '') {
  slide.addText(title, {
    x: 0.48, y: 0.17, w: 7.2, h: 0.42,
    fontFace: 'Microsoft YaHei',
    fontSize: 24,
    bold: true,
    color: C.refBlue,
    margin: 0,
    fit: 'shrink',
  });
  slide.addText(String(idx).padStart(2, '0'), {
    x: 11.75, y: 0.17, w: 0.48, h: 0.24,
    fontFace: 'Verdana',
    fontSize: 12,
    bold: true,
    color: C.refBlue,
    align: 'right',
    margin: 0,
  });
  if (chapter) {
    slide.addText(chapter, {
      x: 8.2, y: 0.2, w: 3.35, h: 0.2,
      fontFace: 'Microsoft YaHei',
      fontSize: 8.5,
      color: C.muted,
      align: 'right',
      margin: 0,
      fit: 'shrink',
    });
  }
}

function addFooter(slide, num) {
  slide.addText('基于容器化部署的云文件管理系统设计与实现', {
    x: 0.48, y: 7.13, w: 4.9, h: 0.15,
    fontFace: 'Microsoft YaHei',
    fontSize: 7.5,
    color: '8AA1B8',
    margin: 0,
  });
  slide.addText(String(num).padStart(2, '0'), {
    x: 12.05, y: 7.08, w: 0.45, h: 0.2,
    fontFace: 'Verdana',
    fontSize: 8.5,
    color: '8AA1B8',
    align: 'right',
    margin: 0,
  });
}

function addCard(slide, x, y, w, h, title, body = '', opt = {}) {
  slide.addShape(pptx.ShapeType.rect, {
    x, y, w, h,
    fill: { color: opt.fill || C.panel },
    line: { color: opt.line || C.line, width: opt.lineWidth || 0.8 },
    shadow: opt.shadow === false ? undefined : { type: 'outer', color: '102A43', opacity: 0.08, blur: 2, angle: 45, distance: 1 },
  });
  if (title) {
    slide.addText(title, {
      x: x + 0.16, y: y + 0.13, w: w - 0.32, h: 0.24,
      fontFace: 'Microsoft YaHei',
      fontSize: opt.titleSize || 12.5,
      bold: true,
      color: opt.titleColor || C.refBlue,
      margin: 0,
      fit: 'shrink',
    });
  }
  if (body) {
    slide.addText(body, {
      x: x + 0.16, y: y + (title ? 0.48 : 0.14), w: w - 0.32, h: h - (title ? 0.62 : 0.26),
      fontFace: 'Microsoft YaHei',
      fontSize: opt.bodySize || 9,
      color: opt.bodyColor || C.ink,
      valign: opt.valign || 'mid',
      breakLine: false,
      fit: 'shrink',
      margin: 0,
    });
  }
}

function addTag(slide, x, y, text, opt = {}) {
  const w = opt.w || Math.max(0.78, text.length * 0.1 + 0.36);
  slide.addShape(pptx.ShapeType.roundRect, {
    x, y, w, h: 0.28,
    fill: { color: opt.fill || 'E9F4FC' },
    line: { color: opt.line || 'D2E6F5', width: 0.45 },
  });
  slide.addText(text, {
    x: x + 0.06, y: y + 0.06, w: w - 0.12, h: 0.14,
    fontFace: 'Microsoft YaHei',
    fontSize: opt.fontSize || 7.6,
    bold: true,
    color: opt.color || C.refBlue,
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  return w;
}

function addImageBox(slide, imagePath, x, y, w, h, opt = {}) {
  slide.addShape(pptx.ShapeType.rect, {
    x, y, w, h,
    fill: { color: opt.fill || C.panel },
    line: { color: opt.line || C.line, width: opt.lineWidth || 0.8 },
    shadow: opt.shadow === false ? undefined : { type: 'outer', color: '102A43', opacity: 0.08, blur: 2, angle: 45, distance: 1 },
  });
  const pad = opt.pad ?? 0.12;
  slide.addImage({
    path: imagePath,
    x: x + pad, y: y + pad, w: w - pad * 2, h: h - pad * 2,
    sizing: { type: opt.cover ? 'cover' : 'contain', x: x + pad, y: y + pad, w: w - pad * 2, h: h - pad * 2 },
  });
}

function addScreenshot(slide, imagePath, x, y, w, h, caption = '', opt = {}) {
  addImageBox(slide, imagePath, x, y, w, h, { pad: opt.pad ?? 0.03, cover: opt.cover ?? true, shadow: opt.shadow, line: opt.line || 'C5D6E8' });
  if (caption) {
    slide.addText(caption, {
      x, y: y + h + 0.05, w, h: 0.15,
      fontFace: 'Microsoft YaHei',
      fontSize: 7.2,
      color: C.muted,
      align: 'center',
      margin: 0,
      fit: 'shrink',
    });
  }
}

function addArrow(slide, x, y, label = '') {
  slide.addShape(pptx.ShapeType.line, {
    x, y: y + 0.1, w: 0.42, h: 0,
    line: { color: C.refBlue, width: 1.2, endArrowType: 'triangle' },
  });
  if (label) {
    slide.addText(label, {
      x: x - 0.25, y: y + 0.24, w: 0.88, h: 0.12,
      fontFace: 'Microsoft YaHei',
      fontSize: 6.6,
      color: C.muted,
      align: 'center',
      margin: 0,
      fit: 'shrink',
    });
  }
}

function addDownArrow(slide, x, y, h = 0.42, label = '') {
  slide.addShape(pptx.ShapeType.line, {
    x, y, w: 0, h,
    line: { color: C.refBlue, width: 1.2, endArrowType: 'triangle' },
  });
  if (label) {
    slide.addText(label, {
      x: x - 0.45, y: y + h + 0.06, w: 0.9, h: 0.12,
      fontFace: 'Microsoft YaHei',
      fontSize: 6.6,
      color: C.muted,
      align: 'center',
      margin: 0,
      fit: 'shrink',
    });
  }
}

function addNotes(slide, text) {
  if (typeof slide.addNotes === 'function') slide.addNotes(text);
}

function addTinyList(slide, items, x, y, w, opt = {}) {
  items.forEach((item, i) => {
    slide.addShape(pptx.ShapeType.rect, {
      x, y: y + i * (opt.gap || 0.44), w: 0.08, h: 0.08,
      fill: { color: opt.color || C.accent },
      line: { color: opt.color || C.accent },
    });
    slide.addText(item, {
      x: x + 0.18, y: y + i * (opt.gap || 0.44) - 0.02, w, h: 0.18,
      fontFace: 'Microsoft YaHei',
      fontSize: opt.fontSize || 8.2,
      color: opt.textColor || C.ink,
      margin: 0,
      fit: 'shrink',
    });
  });
}

const notes = [
  '各位老师好，我是网络工程 2203 班的杨佳星。我的毕业论文题目是《基于容器化部署的云文件管理系统设计与实现》。本课题面向本科毕业设计场景，完成了一套可以运行、可以部署、可以演示的轻量级云文件管理系统。汇报中我会重点说明系统为什么要做、怎么设计、如何通过 Docker Compose 编排运行，以及最终实现了哪些核心功能和运行效果。',
  '本次汇报分为四个部分。第一部分说明选题背景和课题目标，第二部分说明系统设计和容器化部署，第三部分说明文件管理、预览分享、用户隔离与回收站等核心实现，第四部分说明测试验证、系统运行界面和总结展望。整体汇报不会把系统包装成企业级云盘，而是按照本科毕业设计的真实实现范围进行说明。',
  '本课题的背景来自学习和课程设计中的文件管理需求。文件数量增多后，单纯依赖本地目录容易出现文件分散、版本混乱和预览不便。另一方面，毕业设计系统在答辩时还要面对部署环境变化，比如数据库初始化、缓存、静态资源路径和文件目录权限等问题。基于这些问题，我把课题定位为通过容器化方式支撑一个轻量级云文件管理系统，让系统能够在统一环境中快速启动和稳定演示。',
  '本系统的目标可以概括为两个方面：一是完成文件管理业务闭环，包括登录、上传、目录管理、下载预览、分享访问、回收站和存储统计；二是完成容器化部署闭环，把 Nginx、后端服务、MySQL 和 Redis 统一到 Docker Compose 编排中。技术路线采用 Vue 3 和 Element Plus 构建前端，Spring Boot 和 MyBatis-Plus 构建后端，MySQL 保存业务元数据，Redis 保存临时状态，最终通过 Nginx 对外提供统一入口。',
  '系统总体采用前后端分离的 B/S 架构。浏览器首先访问 Nginx，Nginx 一方面返回前端静态页面，另一方面把 /api 请求转发给后端。后端负责用户认证、文件管理、分享、回收站、统计和预览访问控制。MySQL 用于保存用户、文件、分享和回收站等元数据，Redis 用于保存短生命周期状态，本地持久化目录保存真实上传文件。这样系统各层职责比较清晰，便于部署和排查问题。',
  '这一页展示容器化部署流程。执行 docker compose up -d --build 后，Compose 会构建后端镜像，创建内部网络和数据卷，然后启动 MySQL 与 Redis。MySQL 健康检查通过后，后端服务再启动，并执行数据库兼容补丁，例如补齐 user_id 字段、索引和 demo 演示账号。最后 Nginx 作为统一入口提供前端访问和接口代理。这个流程的意义是降低环境差异，方便答辩现场恢复系统运行状态。',
  '容器编排关系是本课题的一个重点。系统中 lfs-nginx 对外暴露 80 端口，负责静态资源和反向代理；lfs-backend 运行 Spring Boot 业务接口；lfs-mysql 保存业务数据；lfs-redis 保存上传分片状态和转码进度等临时信息；上传文件目录通过数据卷持久化。文件上传、下载、预览、分享、回收站和统计功能都依赖这些容器之间的协作，而不是某一个单独页面完成。',
  '文件管理模块主要负责上传、列表、目录、重命名、移动和删除。上传时，系统既要把物理文件保存到持久化目录，也要在 lfs_file 表中写入文件名、大小、类型、父目录和用户归属等元数据。目录记录同样存储在文件表中，通过 is_dir 字段区分。移动文件主要更新 parent_id，删除操作会进入回收站，而不是立即物理删除。这样前端页面操作、数据库元数据和文件目录能够保持一致。',
  '预览与分享模块主要解决访问链路和边界控制问题。系统将 /api/files、/api/thum 和 /api/trans 三类预览路径统一交给 FilePreviewController 处理，控制器负责路径归一化、secret 与 expire 签名校验、路径越界检查和响应头设置。分享模块采用分享码加有效期的方式，访问分享页不需要登录，但仍然保持轻量匿名访问边界，不扩展为复杂协作权限系统。',
  '用户隔离采用 admin 和 demo 两类账号。admin 是管理员视角，可以查看全量文件数据；demo 是普通演示用户，只能查看自己的文件空间。这个隔离不是单纯依赖前端隐藏按钮，而是后端通过 CurrentUserService 识别当前用户，再在文件列表、回收站和统计查询中追加 user_id 条件。lfs_file.user_id 字段被放在这里说明，因为它服务于用户隔离和统计视角，而不是单独展开成表结构页面。',
  '测试部分覆盖容器层、业务层、访问控制层和异常回归层。论文中整理了 T1 到 T17 的测试项，包括容器启动、数据库兼容补丁、登录、上传、预览、分享、回收站、双用户隔离和非法访问拒绝等。自动化测试包括五个测试类，共 24 个用例，覆盖当前用户识别、兼容补丁、分享服务、存储统计和预览控制器。测试目标是验证关键链路稳定，而不是包装成没有数据支撑的性能评测。',
  '这一页展示第一组真实运行界面，包括登录入口、文件空间和文件列表。登录后用户进入文件管理页面，可以看到目录结构、文件列表、文件类型、时间等信息。这里展示的是系统作为一个可运行 Web 应用的基础状态，也为后面的上传、预览、分享和回收站操作提供入口。',
  '这一页展示第二组运行界面，重点是预览和分享。文件预览可以通过统一路径正常返回，分享弹窗可以生成分享链接和有效期，匿名访问页能够打开分享内容。这里对应前面讲到的预览访问链路和分享码机制，说明系统不仅有页面，而且完成了从创建分享到访问分享的业务闭环。',
  '这一页展示第三组运行界面，重点是用户视角、统计信息和容器运行状态。文件列表、回收站和统计按照当前用户视角展示，避免出现列表不可见但统计仍然计入的问题。右侧终端截图展示 Docker Compose 相关容器处于运行状态，说明前端入口、后端、MySQL 和 Redis 能够按编排协同启动。',
  '最后进行总结。本课题完成了云文件管理系统的总体架构设计，实现了登录认证、文件上传、目录管理、在线预览、分享访问、回收站、存储统计和容器化部署；通过 user_id 和 CurrentUserService 实现轻量用户隔离；通过路径归一化和签名校验统一预览链路；并通过功能测试、异常测试和自动化测试完成验证。不足之处是权限模型仍比较简单，存储以本地目录为主，监控运维能力也较基础。后续可以继续扩展团队空间、对象存储、分享策略和日志监控。我的汇报到此结束，感谢各位老师，敬请批评指正。',
];

// 1. Cover
{
  const slide = pptx.addSlide();
  setBg(slide, 'dark');
  const logo = imageExists('image1.png', media);
  if (logo) slide.addImage({ path: logo, x: 5.48, y: 0.72, w: 2.36, h: 0.48, sizing: { type: 'contain', x: 5.48, y: 0.72, w: 2.36, h: 0.48 } });
  slide.addText('本科毕业论文答辩', {
    x: 5.28, y: 1.58, w: 2.8, h: 0.25,
    fontFace: 'Microsoft YaHei',
    fontSize: 14,
    bold: true,
    color: 'BBD7F2',
    align: 'center',
    margin: 0,
  });
  slide.addText('基于容器化部署的云文件管理系统设计与实现', {
    x: 2.18, y: 2.28, w: 8.98, h: 0.68,
    fontFace: 'Microsoft YaHei',
    fontSize: 30,
    bold: true,
    color: 'FFFFFF',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addText('Cloud File Management System Based on Containerized Deployment', {
    x: 3.0, y: 3.05, w: 7.33, h: 0.2,
    fontFace: 'Calibri Light',
    fontSize: 11,
    color: 'CFE0F2',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addText('Docker Compose 一键部署 Spring Boot + Vue 3 真实可运行系统', {
    x: 3.23, y: 3.34, w: 6.9, h: 0.18,
    fontFace: 'Microsoft YaHei',
    fontSize: 9.4,
    color: 'D9EAF9',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addShape(pptx.ShapeType.rect, {
    x: 4.12, y: 3.68, w: 5.08, h: 0.01,
    fill: { color: 'A7CBE8', transparency: 20 },
    line: { color: 'A7CBE8', transparency: 20 },
  });
  slide.addText('答辩学生：杨佳星    指导教师：王晓梅老师', {
    x: 3.56, y: 4.25, w: 6.2, h: 0.24,
    fontFace: 'Microsoft YaHei',
    fontSize: 12,
    color: 'E7F0FA',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addText('计算机学院 · 网络工程 2203 · 学号 04222088', {
    x: 3.92, y: 4.62, w: 5.48, h: 0.18,
    fontFace: 'Microsoft YaHei',
    fontSize: 9,
    color: 'BDD2E9',
    align: 'center',
    margin: 0,
  });
  addNotes(slide, notes[0]);
}

// 2. Contents
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  slide.addText('目录', {
    x: 1.55, y: 1.6, w: 1.75, h: 0.7,
    fontFace: 'Microsoft YaHei',
    fontSize: 52,
    bold: true,
    color: C.refBlue,
    margin: 0,
  });
  slide.addText('Contents', {
    x: 1.58, y: 2.5, w: 1.75, h: 0.32,
    fontFace: 'Calibri Light',
    fontSize: 23,
    color: C.refBlue,
    margin: 0,
  });
  const sections = [
    ['01', '选题背景与研究目标', '背景意义、课题定位、完成范围与技术路线'],
    ['02', '系统设计与容器化部署', '总体架构、部署流程、容器编排关系'],
    ['03', '核心功能实现', '文件管理、预览分享、用户隔离与回收站'],
    ['04', '测试、运行效果与总结', '测试验证、系统界面展示、总结展望'],
  ];
  sections.forEach((s, i) => {
    const y = 1.0 + i * 1.18;
    slide.addText(s[0], {
      x: 5.12, y: y + 0.07, w: 0.48, h: 0.28,
      fontFace: 'Verdana',
      fontSize: 21,
      bold: true,
      color: C.refBlue,
      margin: 0,
    });
    slide.addText(s[1], {
      x: 5.95, y, w: 3.6, h: 0.32,
      fontFace: 'Microsoft YaHei',
      fontSize: 18,
      bold: true,
      color: C.ink,
      margin: 0,
      fit: 'shrink',
    });
    slide.addText(s[2], {
      x: 5.95, y: y + 0.38, w: 4.72, h: 0.2,
      fontFace: 'Microsoft YaHei',
      fontSize: 8.8,
      color: C.muted,
      margin: 0,
      fit: 'shrink',
    });
  });
  addFooter(slide, 2);
  addNotes(slide, notes[1]);
}

// 3. Background
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 3, '研究背景与意义', '选题背景与目标');
  addImageBox(slide, asset('fig_3_2_network_access.png'), 0.58, 0.92, 5.25, 4.42, { pad: 0.16, cover: false });
  addCard(slide, 6.18, 1.0, 5.78, 0.88, '现实需求', '学习资料、课程设计、项目文档持续增多，文件管理需要上传、共享、预览和归档能力。', { bodySize: 9 });
  addCard(slide, 6.18, 2.13, 5.78, 0.88, '部署痛点', '传统 Web 系统依赖本机环境、数据库初始化、路径权限和静态资源配置，迁移演示成本较高。', { bodySize: 9 });
  addCard(slide, 6.18, 3.26, 5.78, 0.88, '课题价值', '通过容器化编排把前端、后端、数据库、缓存和代理统一起来，提升复现和演示稳定性。', { bodySize: 9 });
  addCard(slide, 0.58, 5.47, 11.38, 0.86, '课题定位', '本课题不是做大型商业网盘，而是面向本科毕业设计场景，完成一个可运行、可部署、可演示、可验证的轻量级云文件管理系统。', {
    fill: C.refBlue,
    line: C.refBlue,
    titleColor: 'FFFFFF',
    bodyColor: 'EAF4FF',
    bodySize: 8.8,
  });
  addFooter(slide, 3);
  addNotes(slide, notes[2]);
}

// 4. Goal + tech route
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 4, '研究目标与技术路线', '选题背景与目标');
  addCard(slide, 0.58, 0.95, 4.12, 3.9, '完成的功能范围', '', { fill: 'FFFFFF' });
  const features = ['登录认证', '文件上传', '目录管理', '下载预览', '分享访问', '回收站', '存储统计', '容器部署'];
  features.forEach((t, i) => {
    const x = 0.92 + (i % 2) * 1.78;
    const y = 1.62 + Math.floor(i / 2) * 0.72;
    slide.addShape(pptx.ShapeType.rect, { x, y, w: 1.42, h: 0.42, fill: { color: 'F0F6FC' }, line: { color: C.line, width: 0.55 } });
    slide.addText(t, { x, y: y + 0.12, w: 1.42, h: 0.14, fontFace: 'Microsoft YaHei', fontSize: 8.2, bold: true, color: C.refBlue, align: 'center', margin: 0, fit: 'shrink' });
  });
  addCard(slide, 0.58, 5.18, 4.12, 0.98, '答辩边界', '系统按本科毕设范围实现，不包装为企业级多租户平台，也不引入完整 RBAC、审批流或大规模分布式存储。', { fill: C.warn, line: 'F0D4A5', bodySize: 8.1 });
  const route = [
    ['需求分析', '确定业务闭环'],
    ['总体设计', '划分系统层次'],
    ['核心实现', '文件/预览/分享'],
    ['容器部署', 'Compose 编排'],
    ['测试验证', '功能与异常测试'],
  ];
  route.forEach((r, i) => {
    const x = 5.12 + i * 1.48;
    addCard(slide, x, 1.28, 1.18, 1.0, r[0], r[1], { titleSize: 8.2, bodySize: 6.6, fill: 'FFFFFF', shadow: false });
    if (i < route.length - 1) addArrow(slide, x + 1.22, 1.63);
  });
  addCard(slide, 5.1, 3.0, 6.96, 2.26, '技术栈组合与实现思路', '前端负责文件空间和操作界面，后端提供 REST 接口，MySQL 保存业务元数据，Redis 保存临时状态，Nginx 与 Docker Compose 负责统一访问和部署。', { fill: 'FFFFFF', bodySize: 8.3 });
  [['前端', 'Vue 3 / Element Plus / Vite'], ['后端', 'Spring Boot 3.2.2 / MyBatis-Plus / JWT'], ['数据', 'MySQL 8 / Redis 7 / 本地持久化目录'], ['部署', 'Nginx / Docker / Docker Compose']].forEach((row, i) => {
    slide.addText(row[0], { x: 5.42, y: 4.18 + i * 0.34, w: 0.72, h: 0.16, fontSize: 8, bold: true, color: C.refBlue, margin: 0 });
    slide.addText(row[1], { x: 6.22, y: 4.18 + i * 0.34, w: 5.2, h: 0.16, fontSize: 8, color: C.ink, margin: 0, fit: 'shrink' });
  });
  addFooter(slide, 4);
  addNotes(slide, notes[3]);
}

// 5. Overall system design
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 5, '系统总体设计', '系统设计与容器化部署');
  addImageBox(slide, asset('fig_3_1_system_architecture.png'), 4.82, 0.88, 7.15, 4.62, { pad: 0.12, cover: false });
  const layers = [
    ['表现层', 'Vue 3 页面、文件列表、分享访问、预览界面'],
    ['业务层', '认证、文件、分享、回收站、统计与预览接口'],
    ['数据与资源层', 'MySQL 元数据、Redis 临时状态、本地文件目录'],
    ['部署与代理层', 'Nginx 统一入口，Compose 编排核心容器'],
  ];
  layers.forEach((l, i) => addCard(slide, 0.58, 1.0 + i * 1.05, 3.78, 0.78, l[0], l[1], { titleSize: 10.5, bodySize: 7.4, fill: i % 2 ? 'FFFFFF' : 'F0F6FC' }));
  addCard(slide, 0.58, 5.68, 11.38, 0.62, '设计说明', '系统采用前后端分离 B/S 架构：浏览器访问 Nginx，Nginx 分发前端页面并代理 /api 请求，后端再连接 MySQL、Redis 和本地文件目录。', { bodySize: 8.5, fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'FFFFFF' });
  addFooter(slide, 5);
  addNotes(slide, notes[4]);
}

// 6. Container deployment flow
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 6, '容器化部署流程', '系统设计与容器化部署');
  const nodes = [
    ['执行命令', 'docker compose up -d --build'],
    ['创建资源', '内部网络、数据卷、镜像构建'],
    ['基础服务', 'MySQL 与 Redis 启动'],
    ['后端启动', '健康检查后执行兼容补丁'],
    ['统一入口', 'Nginx 代理前端与接口'],
  ];
  nodes.forEach((n, i) => {
    const x = 0.62 + i * 2.35;
    addCard(slide, x, 1.16, 1.76, 1.05, n[0], n[1], { titleSize: 10, bodySize: 7.2, fill: 'FFFFFF' });
    if (i < nodes.length - 1) addArrow(slide, x + 1.84, 1.54);
  });
  addCard(slide, 0.62, 2.82, 3.12, 1.22, '为什么容器化', '答辩环境可能变化，容器化能固化启动顺序、网络、数据卷和依赖版本。', { fill: 'F0F6FC', bodySize: 8.2 });
  addCard(slide, 4.0, 2.82, 3.12, 1.22, '启动依赖', 'MySQL 健康检查通过后后端再启动，并自动补齐字段、索引和 demo 演示账号。', { fill: 'F0F6FC', bodySize: 8.2 });
  addCard(slide, 7.38, 2.82, 3.12, 1.22, '持久化设计', 'MySQL 数据、Redis 状态和上传文件目录通过数据卷挂载，避免容器重建后数据丢失。', { fill: 'F0F6FC', bodySize: 8.2 });
  const dockerShot = imageExists('image32.png', media);
  if (dockerShot) addScreenshot(slide, dockerShot, 0.62, 4.42, 10.92, 1.1, '真实运行截图：Docker Compose 启动与容器状态', { cover: false, pad: 0.05 });
  addCard(slide, 0.62, 5.98, 10.92, 0.42, '', '传统部署也能运行，但容器化更适合毕业答辩现场复现：执行 compose 启动后即可恢复前端入口、后端服务、数据库和缓存。', { bodySize: 7.9, fill: C.refBlue, line: C.refBlue, bodyColor: 'FFFFFF' });
  addFooter(slide, 6);
  addNotes(slide, notes[5]);
}

// 7. Container orchestration
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 7, '容器编排关系与功能支撑', '系统设计与容器化部署');
  addCard(slide, 4.84, 1.02, 2.2, 0.82, 'lfs-nginx', '统一入口 / 反向代理', { fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'EAF4FF', titleSize: 12, bodySize: 7.5 });
  addCard(slide, 4.84, 2.48, 2.2, 0.82, 'lfs-backend', 'Spring Boot 业务接口', { fill: 'FFFFFF', titleSize: 12, bodySize: 7.5 });
  addCard(slide, 1.25, 4.06, 2.08, 0.82, 'lfs-mysql', '用户、文件、分享、回收站元数据', { fill: 'F0F6FC', titleSize: 11, bodySize: 7.2 });
  addCard(slide, 4.92, 4.06, 2.08, 0.82, 'lfs-redis', '上传分片与转码进度状态', { fill: 'F0F6FC', titleSize: 11, bodySize: 7.2 });
  addCard(slide, 8.42, 4.06, 2.08, 0.82, 'volume', '上传文件与数据库持久化目录', { fill: 'F0F6FC', titleSize: 11, bodySize: 7.2 });
  slide.addText('Browser', { x: 4.98, y: 0.52, w: 1.9, h: 0.18, fontSize: 9.5, bold: true, color: C.refBlue, align: 'center', margin: 0 });
  addDownArrow(slide, 5.94, 0.72, 0.25);
  addDownArrow(slide, 5.94, 1.9, 0.38);
  [['数据读写', 3.1, 3.66], ['临时状态', 5.52, 3.66], ['文件读写', 8.96, 3.66]].forEach(([t, x, y]) => {
    slide.addText(t, { x, y, w: 1.0, h: 0.12, fontSize: 6.8, color: C.muted, align: 'center', margin: 0 });
    addDownArrow(slide, x + 0.5, y + 0.18, 0.24);
  });
  addCard(slide, 0.6, 5.56, 10.86, 0.76, '功能支撑关系', '上传/下载依赖后端与文件卷；预览依赖 Nginx 代理、后端签名校验和本地资源读取；分享、回收站和统计依赖 MySQL 元数据与 user_id 查询过滤。', { fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'EAF4FF', bodySize: 8.1 });
  addFooter(slide, 7);
  addNotes(slide, notes[6]);
}

// 8. File management
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 8, '核心功能实现一：文件管理', '核心功能实现');
  addImageBox(slide, asset('fig_4_3_upload_flow.png'), 0.58, 0.88, 5.86, 3.9, { pad: 0.12, cover: false });
  const listShot = imageExists('image17.png', media);
  if (listShot) addScreenshot(slide, listShot, 6.78, 1.02, 4.82, 2.5, '真实系统截图：文件列表与目录管理');
  addCard(slide, 6.78, 4.12, 4.82, 1.02, '实现要点', '上传时同步保存物理文件与 lfs_file 元数据；目录通过 is_dir 记录构建；移动主要更新 parent_id。', { bodySize: 8.8, fill: 'F0F6FC' });
  addCard(slide, 0.58, 5.32, 11.0, 0.84, '功能讲解', '文件管理模块形成“上传、列表、目录、移动、重命名、删除”的基础链路。删除操作先进入回收站，保留恢复空间，不会立即清理物理文件。', { bodySize: 8.4, fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'FFFFFF' });
  addFooter(slide, 8);
  addNotes(slide, notes[7]);
}

// 9. Preview and share
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 9, '核心功能实现二：预览与分享', '核心功能实现');
  addImageBox(slide, asset('fig_4_8_preview_chain.png'), 0.58, 0.9, 5.92, 3.42, { pad: 0.12, cover: false });
  const preview = imageExists('image19.png', media);
  const share = imageExists('image22.png', media);
  const access = imageExists('image23.png', media);
  if (preview) addScreenshot(slide, preview, 6.78, 0.96, 4.85, 1.55, '文件预览返回正常');
  if (share) addScreenshot(slide, share, 6.78, 2.86, 2.28, 1.28, '分享创建');
  if (access) addScreenshot(slide, access, 9.35, 2.86, 2.28, 1.28, '分享访问');
  addCard(slide, 0.58, 4.58, 5.92, 0.86, '预览控制', 'FilePreviewController 统一处理 /api/files、/api/thum、/api/trans，先做路径归一化，再校验 secret + expire，并检查路径是否越界。', { bodySize: 8.1, fill: 'F0F6FC' });
  addCard(slide, 6.78, 4.58, 4.85, 0.86, '分享边界', '分享采用“分享码 + 有效期 + 匿名访问”。它用于轻量演示访问，不扩展为复杂协作权限系统。', { bodySize: 8.1, fill: 'F0F6FC' });
  addCard(slide, 0.58, 5.75, 11.05, 0.46, '', '这页重点说明：预览不是简单返回文件，而是把路径处理、安全校验和文件输出收敛在后端统一入口。', { bodySize: 8, fill: C.refBlue, line: C.refBlue, bodyColor: 'FFFFFF' });
  addFooter(slide, 9);
  addNotes(slide, notes[8]);
}

// 10. Isolation, trash, statistics
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 10, '核心功能实现三：用户隔离、回收站与统计', '核心功能实现');
  addCard(slide, 0.58, 0.95, 3.54, 0.86, 'admin 管理员视角', '可查看和管理全量文件数据，用于展示系统完整数据。', { fill: 'F0F6FC', bodySize: 8.3 });
  addCard(slide, 0.58, 2.02, 3.54, 0.86, 'demo 普通用户视角', '查询时追加 user_id = currentUserId，仅命中自身文件空间。', { fill: 'F0F6FC', bodySize: 8.3 });
  addCard(slide, 0.58, 3.09, 3.54, 0.86, '隔离落点', '隔离不是前端隐藏按钮，而是后端识别当前用户并追加 SQL 归属条件。', { fill: 'FFFFFF', bodySize: 8.3 });
  addCard(slide, 0.58, 4.16, 3.54, 0.86, '设计取舍', '本课题定位轻量级演示系统，暂不引入完整 Spring Security / RBAC。', { fill: C.warn, line: 'F0D4A5', bodySize: 8.1 });
  addTag(slide, 0.78, 5.34, 'CurrentUserService', { w: 1.72 });
  addTag(slide, 2.65, 5.34, 'lfs_file.user_id', { w: 1.52 });
  const s1 = imageExists('image18.png', media);
  const s2 = imageExists('image24.png', media);
  const s3 = imageExists('image25.png', media);
  if (s1) addScreenshot(slide, s1, 4.55, 1.0, 3.38, 1.55, 'demo 用户文件空间');
  if (s2) addScreenshot(slide, s2, 8.2, 1.0, 3.38, 1.55, '统计/视角验证');
  if (s3) addScreenshot(slide, s3, 4.55, 3.42, 7.03, 1.55, '真实系统截图：文件列表与用户视角');
  addCard(slide, 4.55, 5.52, 7.03, 0.52, '', '文件列表、回收站和存储统计共享同一用户视角，避免“列表不可见但统计仍计入”的问题。', { bodySize: 8, fill: C.refBlue, line: C.refBlue, bodyColor: 'FFFFFF' });
  addFooter(slide, 10);
  addNotes(slide, notes[9]);
}

// 11. Testing
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 11, '系统测试与验证', '测试、运行效果与总结');
  addImageBox(slide, asset('fig_5_1_test_scope.png'), 0.58, 0.92, 4.55, 4.28, { pad: 0.12, cover: false });
  [['17', '功能与异常测试项'], ['5', '自动化测试类'], ['24', '自动化用例通过']].forEach((stat, i) => {
    addCard(slide, 5.58 + i * 2.02, 1.02, 1.6, 0.82, '', '', { fill: 'FFFFFF' });
    slide.addText(stat[0], { x: 5.58 + i * 2.02, y: 1.15, w: 1.6, h: 0.24, fontFace: 'Verdana', fontSize: 19, bold: true, color: C.refBlue, align: 'center', margin: 0 });
    slide.addText(stat[1], { x: 5.58 + i * 2.02, y: 1.48, w: 1.6, h: 0.14, fontSize: 7.2, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  });
  const rows = [
    [
      { text: '测试类别', options: { fill: { color: C.refBlue }, color: 'FFFFFF', bold: true, align: 'center', margin: 0.05 } },
      { text: '覆盖内容', options: { fill: { color: C.refBlue }, color: 'FFFFFF', bold: true, align: 'center', margin: 0.05 } },
      { text: '用例数', options: { fill: { color: C.refBlue }, color: 'FFFFFF', bold: true, align: 'center', margin: 0.05 } },
      { text: '结果', options: { fill: { color: C.refBlue }, color: 'FFFFFF', bold: true, align: 'center', margin: 0.05 } },
    ],
    ['容器层', 'Compose 编排、容器启动、数据卷与健康检查', '4', '通过'],
    ['业务层', '登录、上传、列表、预览、分享、回收站', '5', '通过'],
    ['访问控制', 'admin/demo 双用户隔离与分享边界', '4', '通过'],
    ['异常回归', '非法路径、过期分享码、超大文件上传拒绝', '4', '通过'],
  ];
  slide.addTable(rows, {
    x: 5.58, y: 2.18, w: 6.15, h: 2.42,
    colW: [1.1, 3.48, 0.72, 0.85],
    fontFace: 'Microsoft YaHei',
    fontSize: 7.6,
    border: { type: 'solid', color: C.line, pt: 0.55 },
    fill: { color: 'FFFFFF' },
    color: C.ink,
    margin: 0.06,
    valign: 'mid',
    autoFit: true,
  });
  addCard(slide, 0.58, 5.55, 11.15, 0.7, '测试口径', '17 项测试覆盖容器层、业务层、访问控制和异常回归；24 个自动化用例覆盖关键链路。这里不虚报性能指标、用户规模或覆盖率百分比。', { fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'FFFFFF', bodySize: 8.2 });
  addFooter(slide, 11);
  addNotes(slide, notes[10]);
}

// 12. Runtime UI 1
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 12, '系统运行界面一：登录与文件空间', '测试、运行效果与总结');
  const login = imageExists('image14.png', media);
  const list = imageExists('image17.png', media);
  const demo = imageExists('image18.png', media);
  if (login) addScreenshot(slide, login, 0.58, 1.0, 5.05, 2.15, '登录认证入口');
  if (list) addScreenshot(slide, list, 6.02, 1.0, 5.55, 2.15, '文件列表与目录管理');
  if (demo) addScreenshot(slide, demo, 0.58, 3.68, 5.05, 1.65, '普通用户文件空间');
  addCard(slide, 6.02, 3.68, 5.55, 1.65, '运行说明', '登录后进入文件管理页面，系统从后端读取目录结构、文件类型和创建时间等元数据。该页面是上传、预览、分享和回收站操作的基础入口。', { bodySize: 8.6 });
  addCard(slide, 0.58, 5.78, 11.0, 0.42, '', '这一组截图用于证明系统具备真实登录入口和文件空间，不是静态页面展示。', { bodySize: 8, fill: C.refBlue, line: C.refBlue, bodyColor: 'FFFFFF' });
  addFooter(slide, 12);
  addNotes(slide, notes[11]);
}

// 13. Runtime UI 2
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 13, '系统运行界面二：预览与分享', '测试、运行效果与总结');
  const preview = imageExists('image19.png', media);
  const share = imageExists('image22.png', media);
  const access = imageExists('image23.png', media);
  const preview2 = imageExists('image33.png', media);
  if (preview) addScreenshot(slide, preview, 0.58, 0.96, 5.25, 2.18, '文件预览界面');
  if (share) addScreenshot(slide, share, 6.18, 0.96, 2.55, 1.45, '分享创建弹窗');
  if (access) addScreenshot(slide, access, 9.02, 0.96, 2.55, 1.45, '分享访问页面');
  if (preview2) addScreenshot(slide, preview2, 0.58, 3.62, 5.25, 1.58, '预览链路调试与页面结果');
  addCard(slide, 6.18, 3.38, 5.38, 1.2, '运行效果', '分享弹窗生成链接和有效期，匿名访问页通过分享码打开内容；文件预览由统一接口返回，和签名校验、路径归一化逻辑对应。', { bodySize: 8.5, fill: 'F0F6FC' });
  addCard(slide, 6.18, 5.1, 5.38, 0.42, '', '这一页证明系统完成“创建分享 → 校验分享 → 访问分享内容”的业务闭环。', { bodySize: 8, fill: C.refBlue, line: C.refBlue, bodyColor: 'FFFFFF' });
  addFooter(slide, 13);
  addNotes(slide, notes[12]);
}

// 14. Runtime UI 3
{
  const slide = pptx.addSlide();
  setBg(slide, 'light');
  addHeader(slide, 14, '系统运行界面三：统计与容器状态', '测试、运行效果与总结');
  const stat = imageExists('image24.png', media);
  const files = imageExists('image25.png', media);
  const terminal = imageExists('image32.png', media);
  const terminal2 = imageExists('image29.png', media);
  if (stat) addScreenshot(slide, stat, 0.58, 1.02, 5.25, 1.85, '用户视角与统计验证');
  if (files) addScreenshot(slide, files, 0.58, 3.55, 5.25, 1.85, '文件列表与回收站相关视角');
  if (terminal) addScreenshot(slide, terminal, 6.18, 1.02, 5.28, 2.25, 'Docker Compose 容器运行状态', { cover: false, pad: 0.05 });
  if (terminal2) addScreenshot(slide, terminal2, 6.18, 4.18, 5.28, 0.68, '终端测试结果片段', { cover: false, pad: 0.03 });
  addCard(slide, 6.18, 5.28, 5.28, 0.72, '运行结论', '右侧终端截图说明 Nginx、后端、MySQL、Redis 按 Compose 编排启动；左侧页面说明统计与文件列表使用同一用户视角。', { bodySize: 8.1, fill: C.refBlue, line: C.refBlue, titleColor: 'FFFFFF', bodyColor: 'FFFFFF' });
  addFooter(slide, 14);
  addNotes(slide, notes[13]);
}

// 15. Summary
{
  const slide = pptx.addSlide();
  setBg(slide, 'dark');
  slide.addText('总结与展望', {
    x: 4.15, y: 0.95, w: 4.9, h: 0.55,
    fontFace: 'Microsoft YaHei',
    fontSize: 34,
    bold: true,
    color: 'FFFFFF',
    align: 'center',
    margin: 0,
  });
  [
    ['01', '容器化一键部署', 'Docker Compose 编排 Nginx、后端、MySQL、Redis，降低演示环境差异。'],
    ['02', '预览链路统一治理', 'FilePreviewController 统一处理路径归一化、签名校验和越界检查。'],
    ['03', '双用户视角隔离', 'CurrentUserService + user_id 过滤支撑 admin/demo 两类访问视角。'],
  ].forEach((item, i) => {
    const x = 1.05 + i * 3.85;
    addCard(slide, x, 2.02, 3.35, 1.55, item[1], item[2], { bodySize: 8.1, fill: 'FFFFFF', line: 'FFFFFF' });
    slide.addText(item[0], {
      x: x + 0.18, y: 2.18, w: 0.42, h: 0.18,
      fontFace: 'Verdana',
      fontSize: 10,
      bold: true,
      color: C.accent,
      margin: 0,
    });
  });
  addCard(slide, 1.05, 4.02, 10.95, 0.72, '不足与展望', '权限模型较简单，存储仍以本地目录为主；后续可扩展团队空间、对象存储、分享策略、日志监控和预览缓存。', { bodySize: 8.8, fill: 'F0F6FC', line: 'F0F6FC' });
  slide.addText('感谢各位老师批评指正', {
    x: 3.65, y: 5.32, w: 6.02, h: 0.45,
    fontFace: 'Microsoft YaHei',
    fontSize: 27,
    bold: true,
    color: 'FFFFFF',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addText('THANK YOU FOR YOUR CRITICISM', {
    x: 4.28, y: 5.9, w: 4.82, h: 0.18,
    fontFace: 'Calibri Light',
    fontSize: 10,
    color: 'BCD3EA',
    align: 'center',
    margin: 0,
    fit: 'shrink',
  });
  slide.addText('答辩学生：杨佳星    指导教师：王晓梅老师', {
    x: 3.9, y: 6.55, w: 5.55, h: 0.2,
    fontFace: 'Microsoft YaHei',
    fontSize: 10.5,
    color: 'DDEBFA',
    align: 'center',
    margin: 0,
  });
  addNotes(slide, notes[14]);
}

fs.mkdirSync(path.dirname(out), { recursive: true });
await pptx.writeFile({ fileName: out, compression: true });
console.log(out);
