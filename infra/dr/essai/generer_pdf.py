#!/usr/bin/env python3
"""Produit un PDF valide d'une page, d'environ la taille demandée, propre à chaque appel (contenu pseudo-aléatoire).

Usage : generer_pdf.py <titre> <taille en Kio> <sortie>

Chaque document de l'essai de relève a ainsi une empreinte distincte : une restauration qui mélangerait ou tronquerait
des documents serait détectée par la comparaison des empreintes.
"""
import secrets
import sys


def contenu_page(titre: str, taille_kio: int) -> bytes:
    texte = titre.replace("\\", "").replace("(", "").replace(")", "")
    lignes = [b"BT /F1 14 Tf 72 720 Td (" + texte.encode("latin-1", "replace") + b") Tj ET"]
    # Remplissage en commentaires PDF : ignoré à l'affichage, mais porté par l'empreinte du document.
    while sum(len(ligne) + 1 for ligne in lignes) < taille_kio * 1024:
        lignes.append(b"% " + secrets.token_hex(60).encode())
    return b"\n".join(lignes)


def pdf(titre: str, taille_kio: int) -> bytes:
    flux = contenu_page(titre, taille_kio)
    objets = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R "
        b"/Resources << /Font << /F1 5 0 R >> >> >>",
        b"<< /Length " + str(len(flux)).encode() + b" >>\nstream\n" + flux + b"\nendstream",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    ]
    sortie = bytearray(b"%PDF-1.4\n")
    positions = []
    for numero, corps in enumerate(objets, start=1):
        positions.append(len(sortie))
        sortie += str(numero).encode() + b" 0 obj\n" + corps + b"\nendobj\n"
    xref = len(sortie)
    sortie += b"xref\n0 " + str(len(objets) + 1).encode() + b"\n0000000000 65535 f \n"
    for position in positions:
        sortie += f"{position:010d} 00000 n \n".encode()
    sortie += b"trailer\n<< /Size " + str(len(objets) + 1).encode() + b" /Root 1 0 R >>\nstartxref\n"
    sortie += str(xref).encode() + b"\n%%EOF\n"
    return bytes(sortie)


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    with open(sys.argv[3], "wb") as f:
        f.write(pdf(sys.argv[1], int(sys.argv[2])))
