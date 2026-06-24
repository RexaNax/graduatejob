"""第三轮：放弃 SVG marker（Chrome headless 渲染兼容性差），改为给每条 path 末端
画一个内联 polygon（实心三角形箭头），保证任何渲染器都能正确显示。"""

from pathlib import Path
import math
import re
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT))

import generate_paper_figures as gpf  # noqa: E402


# 箭头三角形大小（用户坐标系单位 px，对应 viewBox 像素）
ARROW_LEN = 22.0   # 沿方向轴长度
ARROW_HALF = 11.0  # 垂直方向的半宽（总宽 22）


def parse_path_d(d: str):
    """把 path 的 d 字符串解析成 [(x, y), ...] 一系列点（仅支持 M/L 大写绝对坐标，忽略 z 等）。"""
    tokens = re.findall(r'[MLml]|-?\d+(?:\.\d+)?', d)
    points = []
    i = 0
    while i < len(tokens):
        tok = tokens[i]
        if tok in ("M", "L"):
            x = float(tokens[i + 1]); y = float(tokens[i + 2])
            points.append((x, y))
            i += 3
        elif tok in ("m", "l"):
            # 我们的 SVG 都是大写绝对，遇到小写就抛错提醒
            raise ValueError(f"unsupported relative cmd: {tok} in {d}")
        else:
            i += 1
    return points


def arrow_polygon(p_prev, p_end):
    """根据 path 倒数第二点 p_prev 和终点 p_end，构造一个朝向 (p_end - p_prev) 的实心三角形 polygon points 字符串。
    三角形以 p_end 为尖端，向后退 ARROW_LEN。"""
    x1, y1 = p_prev
    x2, y2 = p_end
    dx, dy = x2 - x1, y2 - y1
    length = math.hypot(dx, dy)
    if length == 0:
        return None
    ux, uy = dx / length, dy / length      # 单位方向向量
    nx, ny = -uy, ux                        # 垂直左方单位向量
    # 三角形 3 顶点
    tip = (x2, y2)
    base_center = (x2 - ux * ARROW_LEN, y2 - uy * ARROW_LEN)
    left = (base_center[0] + nx * ARROW_HALF, base_center[1] + ny * ARROW_HALF)
    right = (base_center[0] - nx * ARROW_HALF, base_center[1] - ny * ARROW_HALF)

    def fmt(p):
        return f"{p[0]:.1f},{p[1]:.1f}"
    return f"{fmt(tip)} {fmt(left)} {fmt(right)}"


# 匹配单条带 marker-end 的 path（整个标签）
PATH_RE = re.compile(
    r'<path\s+d="([^"]+)"([^/]*?)\s*marker-end="url\(#arrow\)"\s*/>'
)


def patch_svg(path: Path):
    s = path.read_text(encoding="utf-8")

    new_paths = []  # (orig, replace)
    polygons_to_append = []

    def handle(m):
        d = m.group(1)
        rest = m.group(2)  # 其它属性（stroke, fill 等）
        try:
            pts = parse_path_d(d)
        except Exception as e:
            print(f"  WARN: parse fail: {e}")
            return m.group(0)
        if len(pts) < 2:
            return m.group(0)
        p_prev, p_end = pts[-2], pts[-1]
        poly = arrow_polygon(p_prev, p_end)
        if poly is None:
            return m.group(0)
        # 新 path：去掉 marker-end，保留 fill/stroke 等
        new_path_tag = f'<path d="{d}"{rest} />'
        # 追加一个实心三角箭头 polygon（黑色填充）
        polygon_tag = (
            f'<polygon points="{poly}" fill="#000000" stroke="none" />'
        )
        polygons_to_append.append(polygon_tag)
        return new_path_tag

    new_s = PATH_RE.sub(handle, s)

    # 把所有 polygon 插入到 </svg> 之前
    if polygons_to_append:
        polys_str = "".join(polygons_to_append)
        new_s = new_s.replace("</svg>", polys_str + "</svg>", 1)

    # 把 <defs>...<marker.../></defs> 整段删掉（已不再需要）
    new_s = re.sub(r'<defs>[\s\S]*?</defs>\s*', '', new_s, count=1)

    path.write_text(new_s, encoding="utf-8")
    print(f"  [OK] {path.name}: replaced {len(polygons_to_append)} marker-end paths")


for fname in [
    "fig_4_1_login_flow.svg",
    "fig_4_3_upload_flow.svg",
    "fig_4_7_preview_chain.svg",
]:
    print(f"== inline arrows in {fname} ==")
    patch_svg(ROOT / fname)

# 重新渲染 PNG
print("\n== rendering PNGs ==")
for svg_name in [
    "fig_4_1_login_flow.svg",
    "fig_4_3_upload_flow.svg",
    "fig_4_7_preview_chain.svg",
]:
    out = gpf.convert_svg_to_png(ROOT / svg_name)
    if out and out.exists():
        print(f"  PNG OK: {out.name}  size={out.stat().st_size}")
    else:
        print(f"  PNG FAIL: {svg_name}")

print("\nALL DONE")
