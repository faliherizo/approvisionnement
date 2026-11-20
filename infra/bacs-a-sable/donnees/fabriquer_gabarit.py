"""Gabarit Word de démonstration, tel que Word l'enregistre : style de titre, variable répartie sur plusieurs runs
(révision), tableau, en-tête avec variable, marqueur de clauses. Bibliothèque standard seulement."""
import sys
import zipfile

W = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"'
CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/header1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>
</Types>"""
RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
DOC_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rIdEntete" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header" Target="header1.xml"/>
</Relationships>"""
ENTETE = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:hdr {W}><w:p><w:r><w:t xml:space="preserve">Investissement Québec — dossier {{{{beneficiaire}}}}</w:t></w:r></w:p></w:hdr>"""
DOCUMENT = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document {W}><w:body>
<w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="32"/></w:rPr><w:t xml:space="preserve">Convention de financement — {{{{bene</w:t></w:r><w:r><w:rPr><w:b/><w:sz w:val="32"/></w:rPr><w:t>ficiaire}}}}</w:t></w:r></w:p>
<w:p><w:r><w:t xml:space="preserve">Montant accordé : {{{{montant}}}}</w:t></w:r></w:p>
<w:tbl><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4"/><w:bottom w:val="single" w:sz="4"/><w:insideH w:val="single" w:sz="4"/></w:tblBorders></w:tblPr>
<w:tr><w:tc><w:p><w:r><w:t>Bénéficiaire</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>{{{{beneficiaire}}}}</w:t></w:r></w:p></w:tc></w:tr>
<w:tr><w:tc><w:p><w:r><w:t>Montant</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>{{{{montant}}}}</w:t></w:r></w:p></w:tc></w:tr></w:tbl>
<w:p><w:r><w:t>{{{{clauses}}}}</w:t></w:r></w:p>
<w:p><w:r><w:t>Fait à Québec.</w:t></w:r></w:p>
<w:sectPr><w:headerReference w:type="default" r:id="rIdEntete"/><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708"/></w:sectPr>
</w:body></w:document>"""

with zipfile.ZipFile(sys.argv[1], "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("[Content_Types].xml", CONTENT_TYPES)
    z.writestr("_rels/.rels", RELS)
    z.writestr("word/_rels/document.xml.rels", DOC_RELS)
    z.writestr("word/document.xml", DOCUMENT)
    z.writestr("word/header1.xml", ENTETE)
