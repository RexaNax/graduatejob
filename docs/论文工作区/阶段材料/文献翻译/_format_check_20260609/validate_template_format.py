from __future__ import annotations

import json
import re
import sys
import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET

NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
W = "{" + NS["w"] + "}"

def q(tag: str) -> str:
    return W + tag

def wval(el, name: str = "val"):
    return None if el is None else el.get(q(name))

def text(p):
    return "".join(t.text or "" for t in p.iter(q("t")))

def norm(p):
    return re.sub(r"\s+", " ", text(p)).strip()

def has_page_break(p):
    return any(br.get(q("type")) == "page" for br in p.iter(q("br"))) or p.find(".//w:pageBreakBefore", NS) is not None

def snap(p):
    ppr = p.find("w:pPr", NS)
    spacing = ppr.find("w:spacing", NS) if ppr is not None else None
    ind = ppr.find("w:ind", NS) if ppr is not None else None
    jc = ppr.find("w:jc", NS) if ppr is not None else None
    rpr = None
    for r in p.findall("w:r", NS):
        if "".join(t.text or "" for t in r.iter(q("t"))).strip():
            rpr = r.find("w:rPr", NS)
            break
    fonts = rpr.find("w:rFonts", NS) if rpr is not None else None
    sz = rpr.find("w:sz", NS) if rpr is not None else None
    color = rpr.find("w:color", NS) if rpr is not None else None
    return {
        "text": norm(p),
        "jc": wval(jc),
        "line": spacing.get(q("line")) if spacing is not None else None,
        "lineRule": spacing.get(q("lineRule")) if spacing is not None else None,
        "before": spacing.get(q("before")) if spacing is not None else None,
        "after": spacing.get(q("after")) if spacing is not None else None,
        "firstLine": ind.get(q("firstLine")) if ind is not None else None,
        "firstLineChars": ind.get(q("firstLineChars")) if ind is not None else None,
        "size": wval(sz),
        "ascii": fonts.get(q("ascii")) if fonts is not None else None,
        "hAnsi": fonts.get(q("hAnsi")) if fonts is not None else None,
        "eastAsia": fonts.get(q("eastAsia")) if fonts is not None else None,
        "bold": (rpr.find("w:b", NS) is not None) if rpr is not None else None,
        "color": wval(color),
        "pageBreak": has_page_break(p),
    }

def load(path: Path):
    with zipfile.ZipFile(path) as z:
        ET.fromstring(z.read("[Content_Types].xml"))
        root = ET.fromstring(z.read("word/document.xml"))
        return list(root.iter(q("p")))

def classify(s, part):
    if s == "一、英文原文": return "section_en", "en"
    if s == "二、中文译文": return "section_cn", "cn"
    if s in {"指导教师（签字）：", "年 月 日"}: return "signature", part
    if part == "en" and (s.startswith("英文来源：") or s.startswith("起 止 页 码") or s.startswith("起止页码") or s.startswith("出版日期") or s.startswith("刊 物 名 称") or s.startswith("刊物名称") or s.startswith("DOI")):
        return "source", part
    if s in {"Cloud Container Technologies: a State-of-the-Art Review", "云容器技术：研究现状综述"}:
        return "article_title", part
    if part == "en" and (s in {"Abstract", "Keywords"} or re.match(r"^[IVX]+\.\s+", s) or re.match(r"^[A-Z]\.\s+", s) or re.match(r"^TABLE\s+[IVX]+", s)):
        return "subheading_en", part
    if part == "cn" and (s in {"摘要", "关键词"} or re.match(r"^[一二三四五六七八九十]+、", s) or re.match(r"^\d+(?:\.\d+)+\s+", s) or re.match(r"^表\s*\d+", s)):
        return "subheading_cn", part
    if part == "en": return "body_en", part
    if part == "cn": return "body_cn", part
    return "skip", part

def required(kind, s, part):
    base = {"line": "400", "lineRule": "exact", "color": "000000"}
    if kind in {"section_en", "section_cn"}:
        return {**base, "jc": "center", "before": "468", "after": "156", "size": "36", "bold": True}
    if kind == "source":
        return {**base, "jc": "both", "before": "156", "after": "156", "size": "24", "ascii": "Times New Roman", "eastAsia": "宋体", "bold": False}
    if kind == "article_title":
        return {**base, "jc": "center", "before": "0", "after": "0", "size": "28", "bold": False}
    if kind == "subheading_en":
        return {**base, "jc": "both", "before": "160", "after": "0", "size": "24", "ascii": "Times New Roman", "eastAsia": "Times New Roman", "bold": True}
    if kind == "subheading_cn":
        return {**base, "jc": "both", "before": "160", "after": "0", "size": "24", "ascii": "Times New Roman", "eastAsia": "宋体", "bold": True}
    if kind == "body_en":
        return {**base, "jc": "both", "before": "0", "after": "0", "firstLine": "480", "firstLineChars": "200", "size": "24", "ascii": "Times New Roman", "eastAsia": "Times New Roman", "bold": False}
    if kind == "body_cn":
        return {**base, "jc": "both", "before": "0", "after": "0", "firstLine": "480", "firstLineChars": "200", "size": "24", "ascii": "Times New Roman", "eastAsia": "宋体", "bold": False}
    if kind == "signature":
        return {**base, "jc": ("right" if s == "年 月 日" else "left"), "before": "0", "after": "120", "size": "28", "ascii": "Times New Roman", "eastAsia": "宋体", "bold": False}
    return {}

def main():
    current = Path(sys.argv[1])
    backup = Path(sys.argv[2])
    out = Path(sys.argv[3])
    cp = load(current)
    bp = load(backup)
    start = next(i for i, p in enumerate(cp, 1) if norm(p) == "一、英文原文")
    bstart = next(i for i, p in enumerate(bp, 1) if norm(p) == "一、英文原文")
    cover_current = [snap(p) for p in cp[:start-1]]
    cover_backup = [snap(p) for p in bp[:bstart-1]]
    part = None
    violations = []
    checked = 0
    examples = []
    for i, p in enumerate(cp, 1):
        s = norm(p)
        if i < start or not s:
            continue
        kind, part = classify(s, part)
        req = required(kind, s, part)
        if not req:
            continue
        actual = snap(p)
        checked += 1
        if len(examples) < 16:
            examples.append({"idx": i, "text": s[:80], "kind": kind, "format": actual})
        for k, v in req.items():
            if actual.get(k) != v:
                violations.append({"idx": i, "text": s[:80], "kind": kind, "field": k, "expected": v, "actual": actual.get(k)})
    result = {
        "current": str(current),
        "backup": str(backup),
        "start_paragraph": start,
        "backup_start_paragraph": bstart,
        "cover_paragraph_count_same": len(cover_current) == len(cover_backup),
        "cover_unchanged_vs_backup_snapshot": cover_current == cover_backup,
        "checked_content_paragraphs": checked,
        "violation_count": len(violations),
        "violations_first20": violations[:20],
        "examples": examples,
    }
    out.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({k: result[k] for k in ["cover_unchanged_vs_backup_snapshot", "checked_content_paragraphs", "violation_count"]}, ensure_ascii=False))

if __name__ == "__main__":
    main()
