# -*- coding: utf-8 -*-
# 4图SVG修改: 3.1删浏览器 / 3.2统一字号+补线标签 / 4.1对齐菱形 / 3.5改标题
import re, sys

D="/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/图表与流程图/"

# ========== 图3.1 删除"用户浏览器"框+文字+连线 ==========
f=D+"fig_3_1_system_architecture.svg"; s=open(f).read()
removals=[
 '<rect x="960.0" y="135.0" width="480.0" height="90.0" rx="4" ry="4" fill="#ffffff" stroke="#000000" stroke-width="4.4" />',
 '<text x="1200.0" y="180.0" text-anchor="middle" font-family="SimHei, Heiti SC, Songti SC, STSong, Times New Roman, serif" font-size="47" font-weight="700" fill="#000000" dominant-baseline="middle">用户浏览器</text>',
 '<line x1="1200.0" y1="225.0" x2="1200.0" y2="280.0" stroke="#000000" stroke-width="4.4" stroke-linecap="square" fill="none" />',
]
for r in removals:
    assert s.count(r)==1, f"3.1 片段命中{s.count(r)}!=1: {r[:40]}"
    s=s.replace(r,"")
open(f,"w").write(s)
print("[3.1] 删除用户浏览器框+文字+连线 OK")

# ========== 图3.2 统一框内字号 + 补静态资源线标签 ==========
f=D+"fig_3_2_network_access.svg"; s=open(f).read()
# 框内文字: 浏览器44 / Nginx反向代理25,25 / 静态资源41 / JWT鉴权拦截器41 / 签名校验26,26 / dist40 / 业务接口37x3 / 本地目录37 / api37
# 统一框内主标签到 40。但"HTTP/HTTPS""JWT校验通过""签名有效"是线上标签(700粗体)保持不变。
# 策略: 把框内这些 font-size 统一改为 40 (仅针对框内节点文字)
# 框内文字内容列表(精确)
box_texts={
 "浏览器":44, "Nginx":25,"反向代理":25,"静态资源":41,"JWT 鉴权拦截器":41,
 "签名校验":26,"secret + expire":26,"前端构建产物 dist":40,
 "Spring Boot 业务接口":37,"/api/user /api/file":37,"/api/share /api/trash":37,
 "本地持久化目录":37,"/api/files /api/thum":37,"/api/trans":37,
}
cnt=0
for txt,oldfs in box_texts.items():
    # 匹配 font-size="oldfs" ...>txt<  把oldfs->40
    pat=re.compile(r'(font-size=")'+str(oldfs)+r'("[^>]*>)'+re.escape(txt)+r'(</text>)')
    def repl(m): return m.group(1)+"40"+m.group(2)+txt+m.group(3)
    s2,n=pat.subn(repl,s)
    if n>=1: s=s2; cnt+=n
print(f"[3.2] 统一框内字号到40: 改了{cnt}处")
# 补静态资源线标签: 线(1410,405)->(1690,405),标签放上方,与JWT标签(x=1555 y=775,线y=815)对称 => x=1555 y=365
m=re.search(r'<text x="1555.0" y="775.0"[^>]*>JWT 校验通过</text>', s)
assert m, "未找到JWT校验通过标签做模板"
tpl=m.group()
newlabel=tpl.replace('y="775.0"','y="365.0"').replace("JWT 校验通过","静态资源请求")
assert newlabel!=tpl
s=s.replace(tpl, tpl+newlabel)
print("[3.2] 补静态资源线标签(x=1555 y=365) OK")
open(f,"w").write(s)

# ========== 图4.1 菱形"密码是否正确"底部990->992对齐结束框 ==========
f=D+"fig_4_1_login_flow.svg"; s=open(f).read()
# 菱形polygon: 630.0,810.0 930.0,900.0 630.0,990.0 330.0,900.0  底部顶点630,990->630,992
old_poly="630.0,810.0 930.0,900.0 630.0,990.0 330.0,900.0"
assert s.count(old_poly)==1, f"4.1菱形命中{s.count(old_poly)}"
s=s.replace(old_poly, "630.0,810.0 930.0,900.0 630.0,992.0 330.0,900.0")
open(f,"w").write(s)
print("[4.1] 菱形底部990->992 对齐结束框 OK")

# ========== 图3.5 标题改"系统接口分层访问流程图" ==========
f=D+"fig_3_5_api_access.svg"; s=open(f).read()
old="图 3.5  系统接口与访问关系图"
assert s.count(old)>=1, "3.5标题未找到"
s=s.replace(old,"图 3.5  系统接口分层访问流程图")
open(f,"w").write(s)
print(f"[3.5] 标题改名 OK (改了{s.count('系统接口分层访问流程图')}处)")

print("\n全部SVG修改完成")
