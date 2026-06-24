import fs from 'fs';
import path from 'path';
import {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  AlignmentType,
  PageNumber,
  Footer,
} from '../node_modules/docx/dist/index.mjs';

const root = path.resolve('docs/ppt');
const mdPath = path.join(root, '基于容器化部署的云文件管理系统设计与实现-答辩讲解稿.md');
const outPath = path.join(root, 'output', '基于容器化部署的云文件管理系统设计与实现_答辩讲解稿.docx');
const md = fs.readFileSync(mdPath, 'utf8');

const lines = md.split(/\r?\n/);
const children = [];
for (const line of lines) {
  if (line.startsWith('# ')) {
    children.push(new Paragraph({
      heading: HeadingLevel.TITLE,
      alignment: AlignmentType.CENTER,
      spacing: { after: 240 },
      children: [new TextRun({ text: line.slice(2), bold: true, size: 32, font: 'Microsoft YaHei' })],
    }));
  } else if (line.startsWith('## ')) {
    children.push(new Paragraph({
      heading: HeadingLevel.HEADING_1,
      spacing: { before: 260, after: 120 },
      children: [new TextRun({ text: line.slice(3), bold: true, size: 26, font: 'Microsoft YaHei', color: '0B1F3A' })],
    }));
  } else if (line.startsWith('> ')) {
    children.push(new Paragraph({
      spacing: { after: 180 },
      children: [new TextRun({ text: line.slice(2), italics: true, size: 21, font: 'Microsoft YaHei', color: '64748B' })],
    }));
  } else if (line.trim()) {
    children.push(new Paragraph({
      spacing: { after: 140 },
      indent: { firstLine: 420 },
      children: [new TextRun({ text: line, size: 22, font: 'Microsoft YaHei', color: '1E293B' })],
    }));
  } else {
    children.push(new Paragraph({ children: [] }));
  }
}

const doc = new Document({
  styles: {
    default: {
      document: { run: { font: 'Microsoft YaHei', size: 22 } },
    },
  },
  sections: [{
    properties: {
      page: {
        margin: { top: 1000, right: 1000, bottom: 1000, left: 1000 },
      },
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [
            new TextRun({ text: '第 ', size: 18, color: '64748B' }),
            PageNumber.CURRENT,
            new TextRun({ text: ' 页', size: 18, color: '64748B' }),
          ],
        })],
      }),
    },
    children,
  }],
});

fs.mkdirSync(path.dirname(outPath), { recursive: true });
const buf = await Packer.toBuffer(doc);
fs.writeFileSync(outPath, buf);
console.log(outPath);

