# -*- coding: utf-8 -*-
# 单图渲染: 复用 generate_paper_figures 的 Chrome 渲染函数,只渲染指定svg
import sys
sys.path.insert(0, "/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/图表与流程图")
from pathlib import Path
import xml.etree.ElementTree as ET
from generate_paper_figures import convert_svg_with_chrome

D=Path("/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/图表与流程图")
figs=["fig_3_1_system_architecture","fig_3_2_network_access","fig_4_1_login_flow","fig_3_5_api_access"]
import time
for name in figs:
    svg=D/(name+".svg"); png=D/(name+".png")
    root=ET.parse(svg).getroot()
    w=int(float(root.get("width"))); h=int(float(root.get("height")))
    ok=False
    for attempt in range(6):
        try:
            ok=convert_svg_with_chrome(svg, png, w, h)
        except Exception as e:
            ok=False
        if ok: break
        time.sleep(2)
    print(f"{name}: {w}x{h} 渲染{'成功' if ok else '失败(6次)'} (第{attempt+1}次)")
print("渲染完成")
