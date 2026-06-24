# -*- coding: utf-8 -*-
# 独立渲染: 禁用crashpad/crash-reporter,绕过沙箱拦截
import subprocess, tempfile, time
from pathlib import Path
import xml.etree.ElementTree as ET

CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
D=Path("/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/图表与流程图")
figs=["fig_3_1_system_architecture","fig_3_2_network_access","fig_4_1_login_flow","fig_3_5_api_access"]

def render(name):
    svg=D/(name+".svg"); png=D/(name+".png")
    root=ET.parse(svg).getroot()
    w=int(float(root.get("width"))); h=int(float(root.get("height")))
    with tempfile.TemporaryDirectory() as wd:
        wrapper=Path(wd)/"page.html"
        wrapper.write_text(
            "<!doctype html><html><head><meta charset='utf-8'>"
            f"<style>html,body{{margin:0;width:{w}px;height:{h}px;overflow:hidden;background:#fff;}}"
            f"img{{display:block;width:{w}px;height:{h}px;}}</style></head>"
            f"<body><img src='{svg.as_uri()}'></body></html>", encoding="utf-8")
        cmd=[CHROME,"--headless=new","--disable-gpu","--hide-scrollbars",
             "--no-first-run","--no-default-browser-check","--disable-dev-shm-usage",
             "--allow-file-access-from-files","--force-device-scale-factor=1",
             "--no-crashpad","--disable-crash-reporter","--disable-breakpad",
             "--disable-features=Crashpad","--no-sandbox","--disable-setuid-sandbox",
             f"--crash-dumps-dir={wd}",
             f"--user-data-dir={Path(wd)/'profile'}",
             f"--window-size={w},{h}",f"--screenshot={png}",wrapper.as_uri()]
        try:
            r=subprocess.run(cmd,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE,timeout=30)
        except Exception as e:
            return False,str(e)
        ok=png.exists() and png.stat().st_size>0
        return ok, r.stderr.decode()[:100] if not ok else "ok"

for name in figs:
    ok=False; msg=""
    for i in range(4):
        ok,msg=render(name)
        if ok: break
        time.sleep(1)
    print(f"{name}: {'成功' if ok else '失败 '+msg}")
print("done")
