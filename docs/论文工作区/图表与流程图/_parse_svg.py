# -*- coding: utf-8 -*-
import xml.etree.ElementTree as ET
import sys
f=sys.argv[1]
ns="{http://www.w3.org/2000/svg}"
root=ET.parse(f).getroot()
title=root.find(ns+"title")
print("TITLE:", title.text if title is not None else "?")
rects=[]; lines=[]; texts=[]; polys=[]; paths=[]
for e in root.iter():
    tag=e.tag.replace(ns,"")
    if tag=="rect":
        x=float(e.get("x",0)); y=float(e.get("y",0)); w=float(e.get("width",0)); h=float(e.get("height",0))
        if w<2590:  # 排除背景
            rects.append((x,y,w,h))
    elif tag=="line":
        lines.append((float(e.get("x1",0)),float(e.get("y1",0)),float(e.get("x2",0)),float(e.get("y2",0))))
    elif tag=="text":
        texts.append((float(e.get("x",0)),float(e.get("y",0)),e.get("font-size","?"),e.get("font-weight","400"),(e.text or "").strip()))
    elif tag=="polygon":
        polys.append(e.get("points",""))
    elif tag=="path":
        paths.append(e.get("d","")[:60])
print(f"\n矩形{len(rects)}个:")
for r in rects: print(f"  rect x={r[0]:.0f} y={r[1]:.0f} w={r[2]:.0f} h={r[3]:.0f}")
print(f"\n横/竖线{len(lines)}条:")
for l in lines: print(f"  line ({l[0]:.0f},{l[1]:.0f})->({l[2]:.0f},{l[3]:.0f})")
print(f"\n文字{len(texts)}个:")
for t in texts: print(f"  ({t[0]:.0f},{t[1]:.0f}) fs={t[2]} w={t[3]}  「{t[4]}」")
print(f"\npolygon(箭头){len(polys)}个, path{len(paths)}条")
for p in paths: print("  path:",p)
