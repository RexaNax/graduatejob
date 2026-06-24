import json, sys, zipfile
from pathlib import Path
from xml.etree import ElementTree as ET
W='{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
NS={'w':'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
def q(x): return W+x
def attrs(el):
    if el is None: return None
    return {k.split('}',1)[-1]:v for k,v in el.attrib.items()}
def inspect(path):
    with zipfile.ZipFile(path) as z:
        out={'path':str(path),'docDefaults':{},'defaultStyles':[]}
        if 'word/styles.xml' not in z.namelist():
            out['missingStylesXml'] = True
            return out
        st=ET.fromstring(z.read('word/styles.xml'))
        dd=st.find('w:docDefaults', NS)
        if dd is not None:
            out['docDefaults']['rFonts']=attrs(dd.find('.//w:rFonts', NS))
            out['docDefaults']['sz']=attrs(dd.find('.//w:sz', NS))
            out['docDefaults']['spacing']=attrs(dd.find('.//w:spacing', NS))
            out['docDefaults']['jc']=attrs(dd.find('.//w:jc', NS))
        for style in st.findall('w:style', NS):
            if style.get(q('default'))=='1' or style.get(q('styleId')) in ('Normal','a3'):
                rfonts=attrs(style.find('.//w:rPr/w:rFonts', NS))
                sz=attrs(style.find('.//w:rPr/w:sz', NS))
                spacing=attrs(style.find('.//w:pPr/w:spacing', NS))
                jc=attrs(style.find('.//w:pPr/w:jc', NS))
                name=attrs(style.find('w:name', NS))
                out['defaultStyles'].append({'id':style.get(q('styleId')),'type':style.get(q('type')),'name':name,'rFonts':rfonts,'sz':sz,'spacing':spacing,'jc':jc})
        return out
print(json.dumps([inspect(Path(p)) for p in sys.argv[1:]], ensure_ascii=False, indent=2))
