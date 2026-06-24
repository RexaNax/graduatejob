# yjx英文翻译修复说明

## 修复对象

- 工作文件：`/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/待打印/yjx英文翻译.docx`
- 原始论文：`/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/阶段材料/文献翻译/ContainerOrch.pdf`
- 原文件备份：`/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/待打印/_backup_20260609_yjx英文翻译补全/yjx英文翻译.docx`

## 按老师反馈修复的内容

1. **补回原始论文中的图**
   - 从原始 PDF 中提取并插入图片 25 张。
   - 修复后 Word 内图片/绘图对象共 26 个，其中 1 个为原文件已有图片，25 个为本次从原文补入。

2. **补回参考文献**
   - 已从原始 PDF 末尾补入 `REFERENCES`。
   - 按老师要求，参考文献未翻译，照抄原文。

3. **补全文献后半部分内容**
   - 英文原文部分已按原始 PDF 14 页完整补入，包括：
     - `IV. A CLASSIFICATION FRAMEWORK FOR CLOUD CONTAINER TECHNOLOGIES`
     - `V. RESULTS AND VISUALISATION`
     - `VI. CONCLUSIONS`
     - `ACKNOWLEDGEMENTS`
     - `REFERENCES`
   - 中文译文部分补充了后半部分对应译文，包括“四、云容器技术分类框架”“五、结果与可视化”“六、结论”“致谢”“参考文献（照抄原文，不翻译）”。

4. **修正英文首行缩进**
   - 英文正文首行缩进已改为 `240 twips / 12 pt`，约等于小四 Times New Roman 下两个英文字母宽度。
   - 不再按两个中文字符宽度 `480 twips` 处理英文段落。
   - 中文译文仍保留 `480 twips / 24 pt`，符合中文正文首行缩进 2 字符。

5. **补充图表中文说明**
   - 已补充 19 个图题中文说明。
   - 已补充 4 个表题中文说明。

## 参考文献排版追加修复（2026-06-10）

- 已将英文原文末尾 `REFERENCES` 和中文译文末尾“参考文献（照抄原文，不翻译）”两处参考文献重新排版。
- 每处参考文献均为 15 条，逐条独立成段，不再把多条文献挤在同一段里。
- `REFERENCES` 标题：居中、Times New Roman、小四、加粗、固定 20 磅行距。
- 参考文献条目：Times New Roman、小四、黑色、两端对齐、固定 20 磅行距、悬挂缩进 0.74cm。
- 参考文献内容按原文照抄，不翻译。
- 本次修复前备份：`/Users/rexanyang/Desktop/bishe/graduatejob-main/docs/论文工作区/待打印/_backup_20260610_参考文献排版/yjx英文翻译.docx`

## 校验结果

- DOCX 结构校验：通过。
- `word/document.xml` 解析：通过。
- 封面文字与备份对比：未改变。
- 后半部分英文关键章节：已存在。
- 后半部分中文译文关键章节：已存在。
- 图片对象数量：26。
- 中文图题数量：19。
- 中文表题数量：4。
- 参考文献命中：3 处。
- `REFERENCES` 排版块数量：2 处。
- 每处 `REFERENCES` 条目数：15 条。
- 参考文献条目格式：小四 Times New Roman、固定 20 磅行距、悬挂缩进。

## 重要说明

本次没有修改第一页封面字段，只替换并补全了正文部分。若需要回滚，可用备份目录中的 `yjx英文翻译.docx` 覆盖当前文件。
