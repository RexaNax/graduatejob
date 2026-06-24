from __future__ import annotations

import copy
import json
import re
import shutil
import sys
import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET

NS = {
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
    "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "pic": "http://schemas.openxmlformats.org/drawingml/2006/picture",
}
for prefix, uri in NS.items():
    ET.register_namespace(prefix, uri)
W = "{" + NS["w"] + "}"


def q(tag: str) -> str:
    return W + tag


def wval(el, name: str = "val") -> str | None:
    if el is None:
        return None
    return el.get(q(name))


def paragraph_text(p: ET.Element) -> str:
    return "".join(t.text or "" for t in p.iter(q("t")))


def norm_text(p: ET.Element) -> str:
    return re.sub(r"\s+", " ", paragraph_text(p)).strip()


def get_or_insert_ppr(p: ET.Element) -> ET.Element:
    ppr = p.find("w:pPr", NS)
    if ppr is None:
        ppr = ET.Element(q("pPr"))
        p.insert(0, ppr)
    elif list(p).index(ppr) != 0:
        p.remove(ppr)
        p.insert(0, ppr)
    return ppr


def clear_ppr_keep_sect(ppr: ET.Element) -> list[ET.Element]:
    sects = [copy.deepcopy(c) for c in list(ppr) if c.tag == q("sectPr")]
    for c in list(ppr):
        ppr.remove(c)
    return sects


def set_ppr(
    p: ET.Element,
    *,
    jc: str | None = None,
    before: str | None = None,
    after: str | None = None,
    line: str | None = "400",
    line_rule: str | None = "exact",
    first_line: str | None = None,
    first_line_chars: str | None = None,
    page_break_before: bool = False,
) -> None:
    ppr = get_or_insert_ppr(p)
    sects = clear_ppr_keep_sect(ppr)
    if page_break_before:
        ET.SubElement(ppr, q("pageBreakBefore"))
    if before is not None or after is not None or line is not None:
        spacing = ET.SubElement(ppr, q("spacing"))
        if before is not None:
            spacing.set(q("before"), before)
        if after is not None:
            spacing.set(q("after"), after)
        if line is not None:
            spacing.set(q("line"), line)
        if line_rule is not None:
            spacing.set(q("lineRule"), line_rule)
    if first_line is not None or first_line_chars is not None:
        ind = ET.SubElement(ppr, q("ind"))
        if first_line is not None:
            ind.set(q("firstLine"), first_line)
        if first_line_chars is not None:
            ind.set(q("firstLineChars"), first_line_chars)
    if jc is not None:
        jc_el = ET.SubElement(ppr, q("jc"))
        jc_el.set(q("val"), jc)
    for sect in sects:
        ppr.append(sect)


def set_run_format(
    p: ET.Element,
    *,
    size: str,
    ascii_font: str = "Times New Roman",
    east_asia_font: str = "宋体",
    bold: bool = False,
    color: str = "000000",
) -> int:
    changed_runs = 0
    for r in p.findall("w:r", NS):
        # Only style textual runs; leave pure break/drawing runs alone unless they also carry text.
        has_text = any(t.text is not None for t in r.iter(q("t")))
        if not has_text:
            continue
        rpr = r.find("w:rPr", NS)
        if rpr is None:
            rpr = ET.Element(q("rPr"))
            r.insert(0, rpr)
        else:
            for c in list(rpr):
                rpr.remove(c)
            if list(r).index(rpr) != 0:
                r.remove(rpr)
                r.insert(0, rpr)
        fonts = ET.SubElement(rpr, q("rFonts"))
        fonts.set(q("ascii"), ascii_font)
        fonts.set(q("hAnsi"), ascii_font)
        fonts.set(q("cs"), ascii_font)
        fonts.set(q("eastAsia"), east_asia_font)
        if bold:
            ET.SubElement(rpr, q("b"))
            ET.SubElement(rpr, q("bCs"))
        color_el = ET.SubElement(rpr, q("color"))
        color_el.set(q("val"), color)
        sz = ET.SubElement(rpr, q("sz"))
        sz.set(q("val"), size)
        szcs = ET.SubElement(rpr, q("szCs"))
        szcs.set(q("val"), size)
        changed_runs += 1
    return changed_runs


def has_page_break(p: ET.Element) -> bool:
    return any(br.get(q("type")) == "page" for br in p.iter(q("br"))) or p.find(".//w:pageBreakBefore", NS) is not None


def classify(text: str, current_part: str | None) -> tuple[str, str | None]:
    if text == "一、英文原文":
        return "section_en", "en"
    if text == "二、中文译文":
        return "section_cn", "cn"
    if text in {"指导教师（签字）：", "年 月 日"}:
        return "signature", current_part
    if current_part == "en" and (
        text.startswith("英文来源：")
        or text.startswith("起 止 页 码")
        or text.startswith("起止页码")
        or text.startswith("出版日期")
        or text.startswith("刊 物 名 称")
        or text.startswith("刊物名称")
        or text.startswith("DOI")
    ):
        return "source", current_part
    if text in {"Cloud Container Technologies: a State-of-the-Art Review", "云容器技术：研究现状综述"}:
        return "article_title", current_part
    if current_part == "en" and (
        text in {"Abstract", "Keywords"}
        or re.match(r"^[IVX]+\.\s+", text)
        or re.match(r"^[A-Z]\.\s+", text)
        or re.match(r"^TABLE\s+[IVX]+", text)
    ):
        return "subheading_en", current_part
    if current_part == "cn" and (
        text in {"摘要", "关键词"}
        or re.match(r"^[一二三四五六七八九十]+、", text)
        or re.match(r"^\d+(?:\.\d+)+\s+", text)
        or re.match(r"^表\s*\d+", text)
    ):
        return "subheading_cn", current_part
    if current_part == "en":
        return "body_en", current_part
    if current_part == "cn":
        return "body_cn", current_part
    return "skip", current_part


def key_snapshot(p: ET.Element) -> dict:
    text = norm_text(p)
    ppr = p.find("w:pPr", NS)
    spacing = ppr.find("w:spacing", NS) if ppr is not None else None
    ind = ppr.find("w:ind", NS) if ppr is not None else None
    jc = ppr.find("w:jc", NS) if ppr is not None else None
    first_rpr = None
    for r in p.findall("w:r", NS):
        if "".join(t.text or "" for t in r.iter(q("t"))).strip():
            first_rpr = r.find("w:rPr", NS)
            break
    fonts = first_rpr.find("w:rFonts", NS) if first_rpr is not None else None
    sz = first_rpr.find("w:sz", NS) if first_rpr is not None else None
    color = first_rpr.find("w:color", NS) if first_rpr is not None else None
    return {
        "text": text,
        "jc": wval(jc),
        "line": spacing.get(q("line")) if spacing is not None else None,
        "lineRule": spacing.get(q("lineRule")) if spacing is not None else None,
        "before": spacing.get(q("before")) if spacing is not None else None,
        "after": spacing.get(q("after")) if spacing is not None else None,
        "firstLine": ind.get(q("firstLine")) if ind is not None else None,
        "firstLineChars": ind.get(q("firstLineChars")) if ind is not None else None,
        "size": wval(sz),
        "ascii": fonts.get(q("ascii")) if fonts is not None else None,
        "eastAsia": fonts.get(q("eastAsia")) if fonts is not None else None,
        "bold": first_rpr.find("w:b", NS) is not None if first_rpr is not None else None,
        "color": wval(color),
        "pageBreak": has_page_break(p),
    }


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: apply_template_format.py <input.docx> <report.json>")
    path = Path(sys.argv[1])
    report_path = Path(sys.argv[2])
    tmp = path.with_suffix(".tmp.docx")

    with zipfile.ZipFile(path, "r") as zin:
        doc_xml = zin.read("word/document.xml")
        root = ET.fromstring(doc_xml)
        paras = list(root.iter(q("p")))
        start_idx = None
        for i, p in enumerate(paras, start=1):
            if norm_text(p) == "一、英文原文":
                start_idx = i
                break
        if start_idx is None:
            raise RuntimeError("未找到正文起点：一、英文原文")

        before_cover = [key_snapshot(p) for p in paras[: start_idx - 1]]
        changes = []
        current_part: str | None = None
        changed_runs_total = 0

        for i, p in enumerate(paras, start=1):
            text = norm_text(p)
            if i < start_idx or not text:
                continue
            kind, current_part = classify(text, current_part)
            if kind == "skip":
                continue
            before = key_snapshot(p)

            if kind in {"section_en", "section_cn"}:
                set_ppr(p, jc="center", before="468", after="156", line="400", line_rule="exact", page_break_before=(kind == "section_en"))
                changed_runs_total += set_run_format(p, size="36", ascii_font="Times New Roman", east_asia_font="黑体", bold=True)
            elif kind == "source":
                set_ppr(p, jc="both", before="156", after="156", line="400", line_rule="exact")
                changed_runs_total += set_run_format(p, size="24", ascii_font="Times New Roman", east_asia_font="宋体", bold=False)
            elif kind == "article_title":
                set_ppr(p, jc="center", before="0", after="0", line="400", line_rule="exact")
                east = "宋体" if current_part == "cn" else "Times New Roman"
                changed_runs_total += set_run_format(p, size="28", ascii_font="Times New Roman", east_asia_font=east, bold=False)
            elif kind == "subheading_en":
                set_ppr(p, jc="both", before="160", after="0", line="400", line_rule="exact")
                changed_runs_total += set_run_format(p, size="24", ascii_font="Times New Roman", east_asia_font="Times New Roman", bold=True)
            elif kind == "subheading_cn":
                set_ppr(p, jc="both", before="160", after="0", line="400", line_rule="exact")
                changed_runs_total += set_run_format(p, size="24", ascii_font="Times New Roman", east_asia_font="宋体", bold=True)
            elif kind == "body_en":
                set_ppr(p, jc="both", before="0", after="0", line="400", line_rule="exact", first_line="480", first_line_chars="200")
                changed_runs_total += set_run_format(p, size="24", ascii_font="Times New Roman", east_asia_font="Times New Roman", bold=False)
            elif kind == "body_cn":
                set_ppr(p, jc="both", before="0", after="0", line="400", line_rule="exact", first_line="480", first_line_chars="200")
                changed_runs_total += set_run_format(p, size="24", ascii_font="Times New Roman", east_asia_font="宋体", bold=False)
            elif kind == "signature":
                set_ppr(p, jc=("right" if text == "年 月 日" else "left"), before="0", after="120", line="400", line_rule="exact")
                changed_runs_total += set_run_format(p, size="28", ascii_font="Times New Roman", east_asia_font="宋体", bold=False)

            after = key_snapshot(p)
            if before != after:
                changes.append({"idx": i, "text": text[:100], "kind": kind, "before": before, "after": after})

        after_cover = [key_snapshot(p) for p in paras[: start_idx - 1]]
        cover_unchanged = before_cover == after_cover
        if not cover_unchanged:
            raise RuntimeError("封面段落快照发生变化，已中止写入")

        xml_bytes = ET.tostring(root, encoding="utf-8", xml_declaration=True)
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                data = xml_bytes if item.filename == "word/document.xml" else zin.read(item.filename)
                zi = zipfile.ZipInfo(item.filename)
                zi.date_time = item.date_time
                zi.compress_type = zipfile.ZIP_DEFLATED
                zi.external_attr = item.external_attr
                zout.writestr(zi, data)

    # Validate XML by reopening the generated DOCX.
    with zipfile.ZipFile(tmp, "r") as z:
        ET.fromstring(z.read("word/document.xml"))
        names = set(z.namelist())
        required = {"[Content_Types].xml", "word/document.xml"}
        missing = sorted(required - names)
        if missing:
            raise RuntimeError(f"DOCX 缺少关键文件：{missing}")

    shutil.move(str(tmp), str(path))
    report = {
        "target": str(path),
        "start_paragraph": start_idx,
        "cover_unchanged_by_snapshot": cover_unchanged,
        "changed_paragraphs": len(changes),
        "changed_runs": changed_runs_total,
        "changes": changes,
        "rules_applied": {
            "cover": "p1 至 p{0} 未修改；从“一、英文原文”开始套用模板正文格式".format(start_idx - 1),
            "section_titles": "居中、18pt、加粗、固定 20 磅行距；英文原文标题强制另起页",
            "source_fields": "小四号，中文宋体、数字字母 Times New Roman，黑色，两端对齐，固定 20 磅行距",
            "english_body": "小四号 Times New Roman，黑色，两端对齐，首行缩进 2 字符，固定 20 磅行距",
            "chinese_body": "小四号宋体，数字字母 Times New Roman，黑色，两端对齐，首行缩进 2 字符，固定 20 磅行距",
        },
    }
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"ok": True, "report": str(report_path), "changed_paragraphs": len(changes), "changed_runs": changed_runs_total}, ensure_ascii=False))


if __name__ == "__main__":
    main()
