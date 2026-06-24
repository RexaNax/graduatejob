from __future__ import annotations
import json
import re
import sys
import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET

NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
W = "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"


def q(tag: str) -> str:
    return W + tag


def val(el, attr="val"):
    if el is None:
        return None
    return el.get(q(attr))


def read_xml(z: zipfile.ZipFile, name: str):
    try:
        return ET.fromstring(z.read(name))
    except KeyError:
        return None


def extract_styles(z: zipfile.ZipFile):
    root = read_xml(z, "word/styles.xml")
    styles = {}
    if root is None:
        return styles
    for st in root.findall("w:style", NS):
        sid = st.get(q("styleId"))
        if not sid:
            continue
        name_el = st.find("w:name", NS)
        pPr = st.find("w:pPr", NS)
        rPr = st.find("w:rPr", NS)
        styles[sid] = {
            "name": val(name_el) if name_el is not None else None,
            "type": st.get(q("type")),
            "basedOn": val(st.find("w:basedOn", NS)),
            "pPr": fmt_ppr(pPr),
            "rPr": fmt_rpr(rPr),
        }
    return styles


def fmt_ppr(pPr):
    if pPr is None:
        return {}
    spacing = pPr.find("w:spacing", NS)
    ind = pPr.find("w:ind", NS)
    jc = pPr.find("w:jc", NS)
    pStyle = pPr.find("w:pStyle", NS)
    return {
        "style": val(pStyle),
        "jc": val(jc),
        "line": spacing.get(q("line")) if spacing is not None else None,
        "lineRule": spacing.get(q("lineRule")) if spacing is not None else None,
        "before": spacing.get(q("before")) if spacing is not None else None,
        "after": spacing.get(q("after")) if spacing is not None else None,
        "firstLine": ind.get(q("firstLine")) if ind is not None else None,
        "left": ind.get(q("left")) if ind is not None else None,
        "right": ind.get(q("right")) if ind is not None else None,
        "pageBreakBefore": pPr.find("w:pageBreakBefore", NS) is not None,
    }


def fmt_rpr(rPr):
    if rPr is None:
        return {}
    rFonts = rPr.find("w:rFonts", NS)
    sz = rPr.find("w:sz", NS)
    szCs = rPr.find("w:szCs", NS)
    color = rPr.find("w:color", NS)
    return {
        "bold": rPr.find("w:b", NS) is not None,
        "italic": rPr.find("w:i", NS) is not None,
        "sz": val(sz),
        "szCs": val(szCs),
        "ascii": rFonts.get(q("ascii")) if rFonts is not None else None,
        "hAnsi": rFonts.get(q("hAnsi")) if rFonts is not None else None,
        "eastAsia": rFonts.get(q("eastAsia")) if rFonts is not None else None,
        "cs": rFonts.get(q("cs")) if rFonts is not None else None,
        "color": val(color),
    }


def paragraph_text(p):
    texts = []
    for t in p.iter(q("t")):
        if t.text:
            texts.append(t.text)
    return "".join(texts)


def run_text(r):
    return "".join((t.text or "") for t in r.iter(q("t")))


def extract_docx(path: Path):
    with zipfile.ZipFile(path) as z:
        styles = extract_styles(z)
        doc = read_xml(z, "word/document.xml")
        if doc is None:
            raise RuntimeError("no document.xml")
        paras = []
        for i, p in enumerate(doc.iter(q("p")), start=1):
            text = paragraph_text(p)
            pPr = p.find("w:pPr", NS)
            runs = []
            for r in p.findall("w:r", NS):
                rt = run_text(r)
                if rt:
                    runs.append({"text": rt[:60], "rPr": fmt_rpr(r.find("w:rPr", NS))})
            has_page_break = any(br.get(q("type")) == "page" for br in p.iter(q("br")))
            has_sect = pPr is not None and pPr.find("w:sectPr", NS) is not None
            paras.append({
                "idx": i,
                "text": text,
                "pPr": fmt_ppr(pPr),
                "runs": runs[:6],
                "runCount": len(runs),
                "hasPageBreak": has_page_break,
                "hasSectPr": has_sect,
            })
        return {"path": str(path), "styles": styles, "paras": paras}


def key_fmt(p):
    ppr = p["pPr"]
    # first non-empty run direct formatting
    rpr = {}
    for r in p["runs"]:
        if r["text"].strip():
            rpr = r["rPr"]
            break
    return {
        "style": ppr.get("style"), "jc": ppr.get("jc"), "line": ppr.get("line"), "lineRule": ppr.get("lineRule"),
        "before": ppr.get("before"), "after": ppr.get("after"), "firstLine": ppr.get("firstLine"),
        "sz": rpr.get("sz"), "ascii": rpr.get("ascii"), "eastAsia": rpr.get("eastAsia"), "bold": rpr.get("bold"),
    }


def summarize(doc):
    rows = []
    for p in doc["paras"]:
        text = re.sub(r"\s+", " ", p["text"]).strip()
        if not text and not p["hasPageBreak"]:
            continue
        rows.append({"idx": p["idx"], "text": text[:100], "fmt": key_fmt(p), "pageBreak": p["hasPageBreak"], "sect": p["hasSectPr"]})
    return rows


def count_formats(doc, start_idx=1):
    counts = {}
    examples = {}
    for p in doc["paras"]:
        if p["idx"] < start_idx:
            continue
        text = re.sub(r"\s+", " ", p["text"]).strip()
        if not text:
            continue
        k = json.dumps(key_fmt(p), ensure_ascii=False, sort_keys=True)
        counts[k] = counts.get(k, 0) + 1
        examples.setdefault(k, {"idx": p["idx"], "text": text[:80], "fmt": key_fmt(p)})
    return sorted(examples.values(), key=lambda x: (-counts[json.dumps(x["fmt"], ensure_ascii=False, sort_keys=True)], x["idx"]))[:30]


def find_text(doc, pattern):
    out = []
    pat = re.compile(pattern, re.I)
    for p in doc["paras"]:
        text = re.sub(r"\s+", " ", p["text"]).strip()
        if pat.search(text):
            out.append({"idx": p["idx"], "text": text[:200], "fmt": key_fmt(p), "pageBreak": p["hasPageBreak"], "sect": p["hasSectPr"]})
    return out


if __name__ == "__main__":
    target = Path(sys.argv[1])
    template = Path(sys.argv[2])
    target_doc = extract_docx(target)
    template_doc = extract_docx(template)
    result = {
        "target_summary_first120": summarize(target_doc)[:120],
        "template_summary_first160": summarize(template_doc)[:160],
        "target_format_counts_after_p20": count_formats(target_doc, 20),
        "template_format_counts_after_p20": count_formats(template_doc, 20),
        "template_instruction_hits": find_text(template_doc, r"宋体|Times|小四|四号|行距|段前|段后|首行|缩进|两端对齐|居中|正文|标题|原文|译文|关键词"),
        "target_instruction_hits": find_text(target_doc, r"宋体|Times|小四|四号|行距|段前|段后|首行|缩进|两端对齐|居中|正文|标题|原文|译文|关键词"),
    }
    out = Path(sys.argv[3])
    out.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(out)
