#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成毕业论文黑白风格图表。

输出：
- SVG：始终生成，便于后续编辑和转换。
- PNG：优先使用本机 Chrome 按 SVG 真实画布导出；若不可用，再尝试 rsvg-convert、inkscape、magick/convert 或 sips。
"""

from __future__ import annotations

import html
import os
import re
import shutil
import subprocess
import tempfile
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional

from PIL import Image, PngImagePlugin

BASE_DIR = Path(__file__).resolve().parent

CANVAS_W = 2400
CANVAS_H = 1700
STROKE = "#000000"
FILL = "#ffffff"
FONT = "SimHei, Heiti SC, Songti SC, STSong, Times New Roman, serif"
LINE_W = 4.4
FONT_SCALE = 1.48
MIN_BODY_FONT = 34
MIN_LABEL_FONT = 31
MIN_BOX_FONT = 26


@dataclass
class Box:
    x: float
    y: float
    w: float
    h: float
    text: str
    kind: str = "rect"
    font_size: int = 30
    bold: bool = False

    @property
    def cx(self) -> float:
        return self.x + self.w / 2

    @property
    def cy(self) -> float:
        return self.y + self.h / 2

    @property
    def top(self) -> tuple[float, float]:
        return self.cx, self.y

    @property
    def bottom(self) -> tuple[float, float]:
        return self.cx, self.y + self.h

    @property
    def left(self) -> tuple[float, float]:
        return self.x, self.cy

    @property
    def right(self) -> tuple[float, float]:
        return self.x + self.w, self.cy


class Svg:
    def __init__(self, width: int = CANVAS_W, height: int = CANVAS_H):
        self.w = width
        self.h = height
        self.items: list[str] = []
        self.figure_title: Optional[str] = None
        self.defs = """
        <marker id="arrow" markerWidth="22" markerHeight="16" refX="20" refY="8" orient="auto" markerUnits="userSpaceOnUse">
          <path d="M 0 0 L 22 8 L 0 16 z" fill="#000000" />
        </marker>
        """

    def text(self, x: float, y: float, text: str, size: int = 30, anchor: str = "middle", bold: bool = False) -> None:
        size = self._scaled_font_size(size)
        weight = "700" if bold else "400"
        lines = str(text).split("\n")
        line_h = size * 1.15
        start_y = y - (len(lines) - 1) * line_h / 2
        self._emit_text_lines(x, start_y, lines, size, line_h, anchor, weight)

    def box_text(self, box: Box, padding_x: float = 30, padding_y: float = 18, max_width: Optional[float] = None) -> None:
        available_w = max_width if max_width is not None else box.w - padding_x * 2
        available_h = max(24, box.h - padding_y * 2)
        lines, size, line_h = self._fit_text(str(box.text), box.font_size, available_w, available_h)
        weight = "700" if box.bold else "400"
        start_y = box.cy - (len(lines) - 1) * line_h / 2
        self._emit_text_lines(box.cx, start_y, lines, size, line_h, "middle", weight)

    def _scaled_font_size(self, size: int) -> int:
        return max(MIN_LABEL_FONT if size <= 24 else MIN_BODY_FONT, int(round(size * FONT_SCALE)))

    def _emit_text_lines(self, x: float, start_y: float, lines: list[str], size: int, line_h: float, anchor: str, weight: str) -> None:
        for i, line in enumerate(lines):
            self.items.append(
                f'<text x="{x:.1f}" y="{start_y + i * line_h:.1f}" text-anchor="{anchor}" '
                f'font-family="{FONT}" font-size="{size}" font-weight="{weight}" '
                f'fill="#000000" dominant-baseline="middle">{html.escape(line)}</text>'
            )

    def _fit_text(self, text: str, requested_size: int, max_width: float, max_height: float) -> tuple[list[str], int, float]:
        base_size = self._scaled_font_size(requested_size)
        lower_bound = min(base_size, MIN_BOX_FONT)
        for size in range(base_size, lower_bound - 1, -1):
            lines = self._wrap_text(text, max_width, size)
            line_h = size * 1.12
            if lines and len(lines) * line_h <= max_height and all(self._visual_width(line, size) <= max_width for line in lines):
                return lines, size, line_h
        size = lower_bound
        lines = self._wrap_text(text, max_width, size)
        line_h = size * 1.10
        while len(lines) * line_h > max_height and size > 22:
            size -= 1
            lines = self._wrap_text(text, max_width, size)
            line_h = size * 1.08
        return lines or [text], size, line_h

    def _wrap_text(self, text: str, max_width: float, size: int) -> list[str]:
        wrapped: list[str] = []
        for raw_line in str(text).split("\n"):
            line = raw_line.strip()
            if not line:
                continue
            wrapped.extend(self._wrap_line(line, max_width, size))
        return wrapped or [""]

    def _wrap_line(self, line: str, max_width: float, size: int) -> list[str]:
        tokens = re.findall(r"[A-Za-z0-9_./{}?=&:+\-]+|\s+|.", line)
        lines: list[str] = []
        current = ""
        for token in tokens:
            if token.isspace():
                token = " " if current else ""
            candidate = current + token
            if candidate and self._visual_width(candidate.rstrip(), size) <= max_width:
                current = candidate
                continue
            if current.strip():
                lines.append(current.rstrip())
                current = ""
            token = token.strip()
            if not token:
                continue
            if self._visual_width(token, size) <= max_width:
                current = token
            else:
                pieces = self._split_long_token(token, max_width, size)
                lines.extend(pieces[:-1])
                current = pieces[-1] if pieces else ""
        if current.strip():
            lines.append(current.rstrip())
        return lines or [line]

    def _split_long_token(self, token: str, max_width: float, size: int) -> list[str]:
        pieces: list[str] = []
        current = ""
        for char in token:
            candidate = current + char
            if candidate and self._visual_width(candidate, size) <= max_width:
                current = candidate
            else:
                if current:
                    pieces.append(current)
                current = char
        if current:
            pieces.append(current)
        return pieces

    def _visual_width(self, text: str, size: int) -> float:
        width = 0.0
        for char in text:
            if char.isspace():
                width += size * 0.32
            elif unicodedata.east_asian_width(char) in ("F", "W"):
                width += size * 1.0
            elif char in "ilI.,:;!|'`":
                width += size * 0.28
            elif char in "()[]{}<>/\\-_+=&?":
                width += size * 0.46
            elif char.isupper():
                width += size * 0.64
            else:
                width += size * 0.56
        return width

    def rect(self, box: Box, radius: int = 4) -> None:
        self.items.append(
            f'<rect x="{box.x:.1f}" y="{box.y:.1f}" width="{box.w:.1f}" height="{box.h:.1f}" '
            f'rx="{radius}" ry="{radius}" fill="{FILL}" stroke="{STROKE}" stroke-width="{LINE_W}" />'
        )
        self.box_text(box)

    def process(self, box: Box) -> None:
        self.rect(box)

    def terminator(self, box: Box) -> None:
        r = min(32, box.h / 2)
        self.items.append(
            f'<rect x="{box.x:.1f}" y="{box.y:.1f}" width="{box.w:.1f}" height="{box.h:.1f}" '
            f'rx="{r:.1f}" ry="{r:.1f}" fill="{FILL}" stroke="{STROKE}" stroke-width="{LINE_W}" />'
        )
        self.box_text(box)

    def io(self, box: Box) -> None:
        skew = min(48, box.w * 0.12)
        points = [
            (box.x + skew, box.y),
            (box.x + box.w, box.y),
            (box.x + box.w - skew, box.y + box.h),
            (box.x, box.y + box.h),
        ]
        self.poly(points)
        self.box_text(box, padding_x=skew + 24)

    def diamond(self, box: Box) -> None:
        points = [(box.cx, box.y), (box.x + box.w, box.cy), (box.cx, box.y + box.h), (box.x, box.cy)]
        self.poly(points)
        self.box_text(box, max_width=box.w * 0.58, padding_y=28)

    def poly(self, points: Iterable[tuple[float, float]]) -> None:
        d = " ".join(f"{x:.1f},{y:.1f}" for x, y in points)
        self.items.append(f'<polygon points="{d}" fill="{FILL}" stroke="{STROKE}" stroke-width="{LINE_W}" />')

    def line(self, x1: float, y1: float, x2: float, y2: float, arrow: bool = False) -> None:
        marker = ' marker-end="url(#arrow)"' if arrow else ""
        self.items.append(
            f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" '
            f'stroke="{STROKE}" stroke-width="{LINE_W}" stroke-linecap="square" fill="none"{marker} />'
        )

    def path(self, pts: list[tuple[float, float]], arrow: bool = True) -> None:
        if not pts:
            return
        d = f"M {pts[0][0]:.1f} {pts[0][1]:.1f} " + " ".join(f"L {x:.1f} {y:.1f}" for x, y in pts[1:])
        marker = ' marker-end="url(#arrow)"' if arrow else ""
        self.items.append(
            f'<path d="{d}" stroke="{STROKE}" stroke-width="{LINE_W}" stroke-linecap="square" '
            f'stroke-linejoin="miter" fill="none"{marker} />'
        )

    def arrow(self, start: tuple[float, float], end: tuple[float, float], label: Optional[str] = None, label_pos: Optional[tuple[float, float]] = None) -> None:
        self.path([start, end], arrow=True)
        if label:
            lx, ly = label_pos or ((start[0] + end[0]) / 2, (start[1] + end[1]) / 2 - 24)
            self.text(lx, ly, label, 27, bold=True)

    def orth_arrow(self, pts: list[tuple[float, float]], label: Optional[str] = None, label_pos: Optional[tuple[float, float]] = None) -> None:
        self.path(pts, arrow=True)
        if label:
            lx, ly = label_pos or pts[len(pts) // 2]
            self.text(lx, ly - 24, label, 27, bold=True)

    def heading(self, title: str) -> None:
        self.figure_title = title

    def save(self, name: str) -> Path:
        out = BASE_DIR / f"{name}.svg"
        title_meta = f"  <title>{html.escape(self.figure_title)}</title>\n" if self.figure_title else ""
        desc_meta = (
            f"  <desc>{html.escape(self.figure_title)}；图名仅写入文件元数据，论文排版时请在图片下方另写图题。</desc>\n"
            if self.figure_title else ""
        )
        svg = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}" height="{self.h}" viewBox="0 0 {self.w} {self.h}" style="background-color:#ffffff">
{title_meta}{desc_meta}  <defs>{self.defs}</defs>
  <rect x="0" y="0" width="{self.w}" height="{self.h}" fill="#ffffff" stroke="none" />
  {''.join(self.items)}
</svg>
'''
        out.write_text(svg, encoding="utf-8")
        return out


def draw_layer(s: Svg, x: float, y: float, w: float, h: float, title: str, boxes: list[Box]) -> None:
    s.items.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" fill="#ffffff" stroke="#000000" stroke-width="{LINE_W}" />')
    s.text(x + 28, y + 36, title, 29, anchor="start", bold=True)
    for box in boxes:
        s.rect(box)


def draw_group(s: Svg, x: float, y: float, w: float, h: float, title: str) -> None:
    s.items.append(
        f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" '
        f'fill="#ffffff" stroke="#000000" stroke-width="{LINE_W}" />'
    )
    s.text(x + 28, y + 38, title, 27, anchor="start", bold=True)


def tree_connect(s: Svg, root: Box, children: list[Box], junction_y: float) -> None:
    s.line(root.cx, root.y + root.h, root.cx, junction_y)
    s.line(children[0].cx, junction_y, children[-1].cx, junction_y)
    for child in children:
        s.line(child.cx, junction_y, child.cx, child.y)


def figure_3_1() -> Path:
    s = Svg()
    s.heading("图 3.1  系统总体架构图")
    browser = Box(960, 135, 480, 90, "用户浏览器", font_size=32, bold=True)
    s.rect(browser)
    layer_x, layer_w = 200, 2000
    rows = [
        (280, 210, "表现层", ["Vue 3", "Element Plus", "Vite", "Vue Router"]),
        (550, 330, "业务层", ["Spring Boot 3.2.2", "用户认证", "文件管理", "预览", "分享", "回收站", "存储统计"]),
        (940, 220, "数据与资源层", ["MySQL 8", "Redis 7", "本地持久化目录"]),
        (1220, 310, "部署与代理层", ["Docker Compose 编排", "lfs-nginx", "lfs-backend", "lfs-mysql", "lfs-redis"]),
    ]
    prev = browser
    for idx, (y, h, title, labels) in enumerate(rows):
        boxes: list[Box] = []
        if idx == 0:
            gap = 45
            bw = 340
            start = 500
            for i, lab in enumerate(labels):
                boxes.append(Box(start + i * (bw + gap), y + 86, bw, 74, lab, font_size=27))
        elif idx == 1:
            boxes.append(Box(830, y + 72, 740, 78, labels[0], font_size=29, bold=True))
            bw, gap = 225, 22
            start = 400
            for i, lab in enumerate(labels[1:]):
                boxes.append(Box(start + i * (bw + gap), y + 205, bw, 70, lab, font_size=25))
        elif idx == 2:
            bw, gap = 390, 80
            start = 540
            for i, lab in enumerate(labels):
                boxes.append(Box(start + i * (bw + gap), y + 88, bw, 80, lab, font_size=28))
        else:
            boxes.append(Box(760, y + 70, 880, 78, labels[0], font_size=29, bold=True))
            bw, gap = 290, 45
            start = 445
            for i, lab in enumerate(labels[1:]):
                boxes.append(Box(start + i * (bw + gap), y + 197, bw, 70, lab, font_size=25))
        draw_layer(s, layer_x, y, layer_w, h, title, boxes)
        s.line(prev.bottom[0], prev.bottom[1], 1200, y)
        prev = Box(1000, y, 400, h, title)
    return s.save("fig_3_1_system_architecture")


def figure_3_2() -> Path:
    s = Svg(width=2500, height=1700)
    s.heading("图 3.2  系统网络访问关系图")
    browser = Box(115, 770, 290, 92, "浏览器", font_size=30, bold=True)
    nginx = Box(550, 770, 330, 92, "Nginx\n反向代理", font_size=28, bold=True)
    mids = [
        Box(1050, 360, 360, 90, "静态资源", font_size=28),
        Box(1050, 770, 360, 90, "JWT 鉴权拦截器", font_size=28),
        Box(1050, 1180, 360, 96, "签名校验\nsecret + expire", font_size=27),
    ]
    rights = [
        Box(1690, 345, 560, 120, "前端构建产物 dist", font_size=27),
        Box(1690, 720, 600, 190, "Spring Boot 业务接口\n/api/user  /api/file\n/api/share  /api/trash", font_size=25),
        Box(1690, 1135, 600, 190, "本地持久化目录\n/api/files  /api/thum\n/api/trans", font_size=25),
    ]
    for b in [browser, nginx, *mids, *rights]:
        s.rect(b)
    s.line(browser.right[0], browser.right[1], nginx.left[0], nginx.left[1])
    s.text(480, 732, "HTTP/HTTPS", 27, bold=True)
    for m, r, label in [(mids[0], rights[0], None), (mids[1], rights[1], "JWT 校验通过"), (mids[2], rights[2], "签名有效")]:
        s.path([nginx.right, (955, nginx.cy), (955, m.cy), m.left], arrow=False)
        s.line(m.right[0], m.right[1], r.left[0], r.left[1])
        if label:
            s.text(1555, m.cy - 40, label, 27, bold=True)
    return s.save("fig_3_2_network_access")


def figure_3_3() -> Path:
    s = Svg(width=2700, height=1700)
    s.heading("图 3.3  系统功能模块结构图")
    root = Box(1040, 140, 620, 90, "云文件管理系统", font_size=32, bold=True)
    s.rect(root)

    draw_group(s, 120, 315, 2460, 330, "功能模块层")
    draw_group(s, 120, 855, 2460, 420, "数据与资源映射层")

    labels = ["用户认证模块", "文件管理模块", "预览模块", "分享模块", "回收站模块", "存储统计模块", "兼容补丁模块"]
    children = [Box(160 + i * 350, 485, 270, 82, lab, font_size=23, bold=True) for i, lab in enumerate(labels)]
    for c in children:
        s.rect(c)
    tree_connect(s, root, children, 385)

    data_labels = [
        "sys_user 表",
        "lfs_file 表\n文件元数据",
        "本地资源目录\n预览读取",
        "lfs_file_share 表",
        "lfs_file_trash 表\ntrash_detail",
        "lfs_file 表\n容量统计",
        "JDBC Template\nALTER TABLE",
    ]
    data = [Box(145 + i * 350, 1030, 300, 112, lab, font_size=22) for i, lab in enumerate(data_labels)]
    for d in data:
        s.rect(d)
    for child, target in zip(children, data):
        s.line(child.bottom[0], child.bottom[1], target.top[0], target.top[1])
    s.text(1350, 780, "模块与数据资源一一映射", 26, bold=True)
    return s.save("fig_3_3_module_structure")


def er_entity(s: Svg, box: Box, fields: list[str]) -> None:
    min_h = 82 + len(fields) * 38
    if box.h < min_h:
        box.h = min_h
    s.items.append(f'<rect x="{box.x:.1f}" y="{box.y:.1f}" width="{box.w:.1f}" height="{box.h:.1f}" fill="#ffffff" stroke="#000000" stroke-width="{LINE_W}" />')
    s.line(box.x, box.y + 54, box.x + box.w, box.y + 54)
    s.text(box.cx, box.y + 31, box.text, 30, bold=True)
    y = box.y + 88
    for f in fields:
        s.text(box.x + 20, y, f, 24, anchor="start")
        y += 38


def figure_3_4() -> Path:
    s = Svg(width=2600, height=1700)
    s.heading("图 3.4  数据库 E-R 图")
    user = Box(110, 254, 430, 272, "sys_user")
    file = Box(990, 155, 660, 470, "lfs_file")
    share = Box(2020, 254, 500, 272, "lfs_file_share")
    detail = Box(110, 1112.5, 560, 220, "lfs_file_trash_detail")
    trash = Box(1040, 1080, 560, 285, "lfs_file_trash")
    thum = Box(2020, 780, 500, 234, "lfs_file_thum")
    er_entity(s, user, ["PK id", "username", "password", "role", "create_time"])
    er_entity(s, file, ["PK id", "FK user_id", "parent_id", "file_name", "file_size", "file_type", "is_dir", "in_trash", "deleted", "create_time"])
    er_entity(s, share, ["PK id", "FK file_id", "share_code", "expire_time", "access_count"])
    er_entity(s, detail, ["PK id", "FK trash_id", "FK file_id"])
    er_entity(s, trash, ["PK id", "FK file_id", "user_id", "delete_time", "expire_time"])
    er_entity(s, thum, ["PK id", "FK file_id", "thum_path", "create_time"])

    rels = {
        "归属": Box(675, 335, 150, 110, "归属", kind="diamond", font_size=24),
        "分享": Box(1765, 335, 150, 110, "分享", kind="diamond", font_size=24),
        "回收": Box(1245, 715, 150, 110, "回收", kind="diamond", font_size=24),
        "明细": Box(720, 1167.5, 150, 110, "明细", kind="diamond", font_size=24),
        "缩略图": Box(1765, 842, 150, 110, "缩略图", kind="diamond", font_size=22),
    }
    for r in rels.values():
        s.diamond(r)

    def er_line(pts: list[tuple[float, float]], labels: list[tuple[float, float, str]]) -> None:
        for (x1, y1), (x2, y2) in zip(pts, pts[1:]):
            if abs(x1 - x2) > 0.01 and abs(y1 - y2) > 0.01:
                raise ValueError("E-R 图关系线必须保持横竖直线")
        s.path(pts, arrow=False)
        for x, y, text in labels:
            s.text(x, y, text, 22)

    file_thum_start = (file.x + file.w, file.y + 350)
    er_line([user.right, rels["归属"].left], [(590, 360, "1"), (650, 360, "N")])
    er_line([rels["归属"].right, file.left], [(875, 360, "N"), (955, 360, "1")])
    er_line([file.right, rels["分享"].left], [(1685, 360, "1"), (1738, 360, "N")])
    er_line([rels["分享"].right, share.left], [(1948, 360, "N"), (1994, 360, "1")])
    er_line([file.bottom, rels["回收"].top], [(1365, 666, "1"), (1365, 704, "N")])
    er_line([rels["回收"].bottom, trash.top], [(1365, 865, "N"), (1365, 1052, "1")])
    er_line([detail.right, rels["明细"].left], [(645, 1193, "N"), (710, 1193, "1")])
    er_line([rels["明细"].right, trash.left], [(905, 1193, "1"), (1010, 1193, "N")])
    er_line([file_thum_start, (1700, file_thum_start[1]), (1700, rels["缩略图"].cy), rels["缩略图"].left], [(1685, file_thum_start[1] - 32, "1"), (1735, rels["缩略图"].cy - 28, "N")])
    er_line([rels["缩略图"].right, thum.left], [(1948, 860, "N"), (1994, 860, "1")])
    return s.save("fig_3_4_er_diagram")


def figure_3_5() -> Path:
    s = Svg(width=3400, height=1850)
    s.heading("图 3.5  系统接口与访问关系图")
    headers = [
        (535, "访问入口"),
        (850, "代理网关"),
        (1225, "校验层"),
        (1660, "控制/服务层"),
        (2140, "资源处理层"),
        (2860, "接口与模块映射"),
    ]
    for x, label in headers:
        s.text(x, 150, label, 25, bold=True)

    x_positions = [330, 750, 1110, 1535, 2045, 2580]
    widths = [340, 285, 390, 455, 450, 700]
    rows = [
        (
            395,
            "接口层级一：JWT 认证接口",
            [
                "客户端",
                "Nginx",
                "JWT 拦截器",
                "Spring Boot\n控制器",
                "业务服务\nUser / File\nShare / Trash",
                "接口与模块映射\n/api/user：用户认证模块\n/api/file：文件管理模块\n/api/share：分享模块\n/api/trash：回收站模块",
            ],
        ),
        (
            885,
            "接口层级二：签名校验接口",
            [
                "客户端\nsecret + expire",
                "Nginx",
                "签名校验\nsecret / expire",
                "预览控制器\n请求路径归一化",
                "本地文件读取\n预览 / 缩略 / 转码",
                "接口与模块映射\n/api/files：预览模块\n/api/thum：缩略图资源\n/api/trans：转码资源",
            ],
        ),
        (
            1375,
            "接口层级三：匿名分享接口",
            [
                "客户端\nshareCode",
                "Nginx",
                "分享码校验\n有效期/状态",
                "FileShareServiceImpl",
                "返回元信息\n并跳转预览",
                "接口与模块映射\n/api/share/access：分享模块\n/api/share/preview：预览模块",
            ],
        ),
    ]
    for center_y, title, labs in rows:
        group_x, group_y, group_w, group_h = 120, center_y - 205, 3160, 410
        s.items.append(
            f'<rect x="{group_x:.1f}" y="{group_y:.1f}" width="{group_w:.1f}" height="{group_h:.1f}" '
            f'fill="#ffffff" stroke="#000000" stroke-width="{LINE_W}" />'
        )
        s.text(group_x + 38, group_y + 38, title, 27, anchor="start", bold=True)
        boxes = []
        for x, w, lab in zip(x_positions, widths, labs):
            h = 112 if "\n" not in lab else 170
            if "接口与模块映射" in lab:
                h = 260
            b = Box(x, center_y - h / 2, w, h, lab, font_size=24 if "接口与模块映射" in lab else 27)
            boxes.append(b)
            s.rect(b)
        for a, b in zip(boxes, boxes[1:]):
            s.line(a.right[0], a.right[1], b.left[0], b.left[1])
    return s.save("fig_3_5_api_access")


def render_flow_node(s: Svg, box: Box) -> None:
    if box.kind == "start":
        s.terminator(box)
    elif box.kind == "io":
        s.io(box)
    elif box.kind == "diamond":
        s.diamond(box)
    else:
        s.rect(box)


def build_flow_canvas(title: str, specs: list[tuple[str, str]], height: int = 2100, gap: int = 72) -> tuple[Svg, list[Box]]:
    s = Svg(width=2200, height=height)
    s.heading(title)
    y = 150
    boxes: list[Box] = []
    for kind, text in specs:
        h = 76
        w = 650
        if kind == "diamond":
            h, w = 160, 560
        elif "\n" in text or len(text) > 24:
            h = 104
            w = 780
        b = Box((s.w - w) / 2, y, w, h, text, kind=kind, font_size=27 if len(text) < 24 else 24)
        boxes.append(b)
        render_flow_node(s, b)
        y += h + gap
    return s, boxes


def draw_main_flow(s: Svg, boxes: list[Box], skip_from: set[int]) -> None:
    for i in range(len(boxes) - 1):
        if i in skip_from:
            continue
        s.arrow(boxes[i].bottom, boxes[i + 1].top)


def right_branch_to(s: Svg, source: Box, target: Box, x_mid: float, label: str, label_pos: tuple[float, float]) -> None:
    s.orth_arrow([source.right, (x_mid, source.cy), (x_mid, target.cy), target.right], label, label_pos)


def down_branch_to(s: Svg, source: Box, target: Box, label: str, label_pos: tuple[float, float]) -> None:
    s.orth_arrow([source.bottom, target.top], label, label_pos)


def flow_node(s: Svg, x: float, y: float, w: float, h: float, text: str, kind: str = "rect", font_size: int = 34, bold: bool = False) -> Box:
    box = Box(x, y, w, h, text, kind=kind, font_size=font_size, bold=bold)
    render_flow_node(s, box)
    return box


def connect_down(s: Svg, boxes: list[Box]) -> None:
    for a, b in zip(boxes, boxes[1:]):
        s.arrow(a.bottom, b.top)


def figure_4_1() -> Path:
    s = Svg(width=2600, height=1250)
    s.heading("图 4.1  用户登录流程图")
    left_x, right_x = 250, 1600
    w, h = 760, 92
    start = flow_node(s, left_x, 160, w, h, "开始", "start", 30)
    input_box = flow_node(s, left_x, 320, w, h, "输入用户名和密码", "io", 30)
    api = flow_node(s, left_x, 480, w, h, "前端调用 /api/user/login", font_size=30)
    bcrypt = flow_node(s, left_x, 640, w, h, "后端使用 BCrypt 校验密码", font_size=30)
    check = flow_node(s, left_x + 80, 810, 600, 180, "密码是否正确", "diamond", 30)
    jwt = flow_node(s, right_x, 560, w, h, "生成 JWT 并返回前端", font_size=30)
    save = flow_node(s, right_x, 730, w, h, "前端保存 token 并跳转主页", font_size=30)
    end = flow_node(s, right_x, 900, w, h, "结束", "start", 30)

    connect_down(s, [start, input_box, api, bcrypt, check])
    s.orth_arrow([check.right, (1320, check.cy), (1320, jwt.cy), jwt.left], "是", (1390, check.cy - 35))
    connect_down(s, [jwt, save, end])
    s.orth_arrow([check.left, (125, check.cy), (125, input_box.cy), input_box.left], "否", (170, (check.cy + input_box.cy) / 2))
    return s.save("fig_4_1_login_flow")


def figure_4_3() -> Path:
    s = Svg(width=2850, height=1650)
    s.heading("图 4.3  文件上传流程图")
    left_x, right_x = 220, 1600
    w, h = 860, 92
    start = flow_node(s, left_x, 150, w, h, "开始", "start", 30)
    choose = flow_node(s, left_x, 300, w, h, "用户选择文件", "io", 30)
    upload = flow_node(s, left_x, 450, w, 118, "前端发起 /api/file/upload 请求\n携带 JWT", font_size=28)
    jwt_check = flow_node(s, left_x + 130, 640, 600, 180, "JWT 是否有效", "diamond", 30)

    parse = flow_node(s, right_x, 245, w, 118, "CurrentUserService\n解析当前用户 ID", font_size=28)
    write_dir = flow_node(s, right_x, 430, w, h, "写入本地持久化目录", font_size=30)
    insert_file = flow_node(s, right_x, 580, w, 118, "插入 lfs_file 记录\n含 user_id", font_size=28)
    thum_check = flow_node(s, right_x + 130, 770, 600, 180, "是否生成缩略图", "diamond", 30)
    write_thum = flow_node(s, right_x, 1010, w, h, "写入 lfs_file_thum 记录", font_size=30)
    result = flow_node(s, right_x, 1160, w, h, "返回上传结果给前端", font_size=30)
    end = flow_node(s, right_x, 1310, w, h, "结束", "start", 30)

    connect_down(s, [start, choose, upload, jwt_check])
    s.orth_arrow([jwt_check.right, (1320, jwt_check.cy), (1320, parse.cy), parse.left], "是", (1385, jwt_check.cy - 35))
    s.orth_arrow([jwt_check.left, (120, jwt_check.cy), (120, end.cy), end.left], "否", (165, (jwt_check.cy + end.cy) / 2))
    connect_down(s, [parse, write_dir, insert_file, thum_check])
    s.orth_arrow([thum_check.bottom, write_thum.top], "是", (thum_check.cx + 70, (thum_check.bottom[1] + write_thum.top[1]) / 2))
    connect_down(s, [write_thum, result, end])
    s.orth_arrow([thum_check.right, (2650, thum_check.cy), (2650, result.cy), result.right], "否", (2700, (thum_check.cy + result.cy) / 2))
    return s.save("fig_4_3_upload_flow")


def figure_4_8() -> Path:
    s = Svg(width=3050, height=1500)
    s.heading("图 4.8  文件预览访问链路图")
    left_x, right_x = 220, 1500
    w, h = 820, 92
    start = flow_node(s, left_x, 145, w, h, "开始", "start", 30)
    request = flow_node(s, left_x, 295, w, 130, "前端发起预览请求\n/api/files/{path}?secret=...&expire=...", "io", 27)
    nginx = flow_node(s, left_x, 500, w, h, "Nginx 反向代理转发", font_size=30)
    controller = flow_node(s, left_x, 650, w, 118, "FilePreviewController\n归一化请求路径", font_size=28)
    expire = flow_node(s, left_x + 110, 840, 600, 180, "expire 是否过期", "diamond", 30)

    secret = flow_node(s, right_x + 110, 285, 600, 180, "签名是否一致", "diamond", 30)
    read = flow_node(s, right_x, 545, w, h, "读取本地持久化目录中的文件", font_size=30)
    stream = flow_node(s, right_x, 705, w, h, "返回文件流给前端", font_size=30)
    end = flow_node(s, right_x, 900, w, h, "结束", "start", 30)
    error = flow_node(s, 2520, 650, 360, h, "返回 403", font_size=30)

    connect_down(s, [start, request, nginx, controller, expire])
    s.orth_arrow([expire.right, (1280, expire.cy), (1280, secret.cy), secret.left], "否", (1340, expire.cy - 35))
    s.orth_arrow([expire.bottom, (expire.cx, 1180), (error.cx, 1180), error.bottom], "是", (980, 1138))
    s.orth_arrow([secret.bottom, read.top], "是", (secret.cx + 70, (secret.bottom[1] + read.top[1]) / 2))
    connect_down(s, [read, stream, end])
    s.orth_arrow([secret.right, (2890, secret.cy), (2890, error.cy), error.right], "否", (2940, (secret.cy + error.cy) / 2))
    s.orth_arrow([error.bottom, (error.cx, end.cy), end.right])
    return s.save("fig_4_8_preview_chain")


def figure_5_1() -> Path:
    s = Svg(width=2600, height=1800)
    s.heading("图 5.1  测试范围与系统模块对照图")
    draw_group(s, 260, 120, 2080, 150, "测试范围层")
    draw_group(s, 110, 350, 2380, 285, "测试模块层")
    draw_group(s, 110, 730, 2380, 840, "测试用例层")

    root = Box(960, 160, 680, 88, "系统测试范围", font_size=32, bold=True)
    s.rect(root)
    labels = ["登录认证测试", "文件管理测试", "预览与分享测试", "回收站与\n存储统计测试", "容器化部署测试"]
    children = [Box(175 + i * 475, 485, 350, 92, lab, font_size=25, bold=True) for i, lab in enumerate(labels)]
    for c in children:
        s.rect(c)
    tree_connect(s, root, children, 380)

    cases = [
        ["双账号登录", "错误口令拒绝"],
        ["上传", "下载", "重命名", "移动", "删除"],
        ["图片预览", "分享码访问", "过期签名拒绝"],
        ["删除入回收站", "恢复", "彻底删除"],
        ["docker compose up 启动", "容器健康检查"],
    ]
    for i, child in enumerate(children):
        row = cases[i]
        base_x = child.cx - 165
        start_y = 810
        case_boxes = []
        for j, lab in enumerate(row):
            b = Box(base_x, start_y + j * 125, 330, 72, lab, font_size=23)
            case_boxes.append(b)
            s.rect(b)
        prev = child
        for b in case_boxes:
            s.line(prev.bottom[0], prev.bottom[1], b.top[0], b.top[1])
            prev = b
    return s.save("fig_5_1_test_scope")


def svg_size(svg_path: Path) -> tuple[int, int]:
    text = svg_path.read_text(encoding="utf-8", errors="ignore")
    width = re.search(r'<svg[^>]*\bwidth="([0-9.]+)"', text)
    height = re.search(r'<svg[^>]*\bheight="([0-9.]+)"', text)
    return int(float(width.group(1))) if width else CANVAS_W, int(float(height.group(1))) if height else CANVAS_H


def svg_title(svg_path: Path) -> str:
    text = svg_path.read_text(encoding="utf-8", errors="ignore")
    match = re.search(r"<title>(.*?)</title>", text, re.S)
    return html.unescape(match.group(1).strip()) if match else ""


def find_chrome() -> Optional[str]:
    candidates = [
        os.environ.get("CHROME_PATH", ""),
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        "/Applications/Chromium.app/Contents/MacOS/Chromium",
        "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
        "google-chrome",
        "chromium",
        "chrome",
    ]
    for candidate in candidates:
        if not candidate:
            continue
        resolved = shutil.which(candidate)
        if resolved:
            return resolved
        path = Path(candidate)
        if path.is_file() and os.access(path, os.X_OK):
            return str(path)
    return None


def convert_svg_with_chrome(svg_path: Path, png_path: Path, width: int, height: int) -> bool:
    chrome = find_chrome()
    if not chrome:
        return False
    with tempfile.TemporaryDirectory() as work_dir:
        profile_dir = Path(work_dir) / "profile"
        wrapper = Path(work_dir) / "page.html"
        wrapper.write_text(
            "<!doctype html>"
            "<html><head><meta charset='utf-8'>"
            f"<style>html,body{{margin:0;width:{width}px;height:{height}px;overflow:hidden;background:#fff;}}"
            f"img{{display:block;width:{width}px;height:{height}px;}}</style></head>"
            f"<body><img src='{svg_path.as_uri()}'></body></html>",
            encoding="utf-8",
        )
        cmd = [
            chrome,
            "--headless",
            "--disable-gpu",
            "--hide-scrollbars",
            "--no-first-run",
            "--no-default-browser-check",
            "--disable-dev-shm-usage",
            "--allow-file-access-from-files",
            "--force-device-scale-factor=1",
            f"--user-data-dir={profile_dir}",
            f"--window-size={width},{height}",
            f"--screenshot={png_path}",
            wrapper.as_uri(),
        ]
        subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=25)
    return png_path.exists() and png_path.stat().st_size > 0


def flatten_png_to_white_rgb(png_path: Path, title: str = "") -> None:
    """将 PNG 强制合成为白底 RGB，避免 Word 因透明通道显示异常。"""
    pnginfo = PngImagePlugin.PngInfo()
    if title:
        pnginfo.add_text("Title", title)
        pnginfo.add_text("Description", f"{title}；图名未绘制在图片画布中，请在论文图片下方另写图题。")
    with Image.open(png_path) as image:
        if image.mode in ("RGBA", "LA") or "transparency" in image.info:
            rgba = image.convert("RGBA")
            background = Image.new("RGBA", rgba.size, "WHITE")
            background.alpha_composite(rgba)
            background.convert("RGB").save(png_path, format="PNG", pnginfo=pnginfo)
        elif image.mode != "RGB":
            image.convert("RGB").save(png_path, format="PNG", pnginfo=pnginfo)
        elif title:
            image.save(png_path, format="PNG", pnginfo=pnginfo)


def convert_svg_to_png(svg_path: Path) -> Optional[Path]:
    png_path = svg_path.with_suffix(".png")
    width, height = svg_size(svg_path)
    title = svg_title(svg_path)
    if png_path.exists():
        png_path.unlink()
    try:
        if convert_svg_with_chrome(svg_path, png_path, width, height):
            flatten_png_to_white_rgb(png_path, title)
            return png_path
    except Exception:
        if png_path.exists():
            png_path.unlink()
    candidates = [
        ("rsvg-convert", ["rsvg-convert", "-w", str(width), "-h", str(height), "-f", "png", "-o", str(png_path), str(svg_path)]),
        ("inkscape", ["inkscape", str(svg_path), "--export-type=png", f"--export-width={width}", f"--export-height={height}", f"--export-filename={png_path}"]),
        ("magick", ["magick", "-background", "white", "-alpha", "remove", "-alpha", "off", str(svg_path), str(png_path)]),
        ("convert", ["convert", "-background", "white", "-alpha", "remove", "-alpha", "off", str(svg_path), str(png_path)]),
        ("sips", ["sips", "-s", "format", "png", str(svg_path), "--out", str(png_path)]),
    ]
    for exe, cmd in candidates:
        if shutil.which(exe):
            try:
                subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=25)
                if png_path.exists() and png_path.stat().st_size > 0:
                    flatten_png_to_white_rgb(png_path, title)
                    return png_path
            except Exception:
                if png_path.exists():
                    png_path.unlink()
    return None


def main() -> None:
    drawers = [figure_3_1, figure_3_2, figure_3_3, figure_3_4, figure_3_5, figure_4_1, figure_4_3, figure_4_8, figure_5_1]
    svg_paths = [fn() for fn in drawers]
    print("已生成 SVG：", flush=True)
    for p in svg_paths:
        print(f"- {p.name}", flush=True)
    png_paths = []
    for svg in svg_paths:
        print(f"正在生成 PNG：{svg.with_suffix('.png').name}", flush=True)
        try:
            png = convert_svg_to_png(svg)
        except Exception as exc:
            print(f"PNG 转换失败：{svg.name}: {exc}", flush=True)
            png = None
        if png:
            png_paths.append(png)
            print(f"PNG 已生成：{png.name}", flush=True)
        else:
            print(f"PNG 未生成：{svg.with_suffix('.png').name}", flush=True)
    manifest_lines = ["论文图表生成清单", ""]
    for svg in svg_paths:
        width, height = svg_size(svg)
        title = svg_title(svg)
        png = svg.with_suffix(".png")
        manifest_lines.append(f"- {svg.name}：{width}×{height}；图名备注：{title}；PNG：{'已生成' if png.exists() else '未生成'}")
    (BASE_DIR / "paper_figures_manifest.txt").write_text("\n".join(manifest_lines) + "\n", encoding="utf-8")
    if png_paths:
        print("已生成 PNG：")
        for p in png_paths:
            print(f"- {p.name}")
        print("已生成清单：paper_figures_manifest.txt")
    else:
        print("未检测到可用的 SVG→PNG 转换器，已保留 SVG 源图。")


if __name__ == "__main__":
    main()
