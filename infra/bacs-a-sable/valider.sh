#!/bin/sh
# Validation de bout en bout contre les bacs à sable (addenda 4, Q4, Q14, Q22) :
#  1. le serveur (démarré avec les variables de demarrer.sh) produit, par ses API ordinaires, un contrat depuis un
#     gabarit Word (LibreOffice réel), un contrat sans gabarit et un certificat de signature scellé (ICP réelle) ;
#  2. veraPDF REST juge la conformité PDF/A-2b de chacun ; le service pyHanko valide le scellement au profil
#     PAdES-B-LTA, révocation intégrée exigée, avec la seule racine de l'ICP pour ancre de confiance.
# Tout passe par le réseau, aux API des applications externes (infra/bacs-a-sable/docker-compose.yml).
# Préalable : docker compose -f infra/bacs-a-sable/docker-compose.yml up -d --build --wait, puis le serveur démarré
# avec les variables CLM_* données dans le guide (docs/Dossier_IQ_2026-6370-0005/12-Bacs-a-sable).
# Sortie non nulle au premier écart. Résultats dans sandbox/resultats/.
set -eu
ICI=$(cd "$(dirname "$0")" && pwd)
API=${API:-http://localhost:8100/api}
MDP=${MDP_DEMO:-Passw0rd!}
R="${RESULTATS:-$ICI/resultats}"; rm -rf "$R"; mkdir -p "$R"
curl -sf "$API/health" >/dev/null || { echo "Serveur injoignable : $API"; exit 2; }
json() { python3 -c "import json,sys; d=json.load(sys.stdin); print($1)"; }
session() { curl -sf -c "$R/$1.jar" -H 'Content-Type: application/json' -d "{\"email\":\"$2\",\"motDePasse\":\"$MDP\"}" "$API/auth/login" >/dev/null; }
appel() { # $1 session $2 méthode $3 chemin [$4 corps]
  if [ $# -ge 4 ]; then curl -sf -b "$R/$1.jar" -X "$2" -H 'Content-Type: application/json' -d "$4" "$API$3"
  else curl -sf -b "$R/$1.jar" -X "$2" "$API$3"; fi; }
ecart() { echo "ÉCART : $1"; exit 1; }

session redacteur initiateur@iq.example
session affaires admin.affaires@iq.example
python3 "$ICI/donnees/fabriquer_gabarit.py" "$R/gabarit.docx"

CHAMPS='[{"cle":"beneficiaire","libelle":"Bénéficiaire","type":"TEXTE","obligatoire":false},{"cle":"montant","libelle":"Montant","type":"TEXTE","obligatoire":false}]'
CLAUSES='[{"cle":"garantie","titre":"Garantie","texte":"Hypothèque mobilière de premier rang de {{montant}}.","combinateur":"ET","conditions":[]}]'
modele=$(appel redacteur POST /clm/modeles "{\"nom\":\"Convention bac à sable $(date +%s)\",\"corps\":\"Convention avec {{beneficiaire}} pour {{montant}}.\",\"champs\":$CHAMPS,\"clauses\":$CLAUSES,\"parcours\":[]}" | json 'd["id"]')
curl -sf -b "$R/redacteur.jar" -F "fichier=@$R/gabarit.docx;filename=Convention IQ.docx" "$API/clm/modeles/$modele/gabarit-word" >/dev/null
appel redacteur POST "/clm/modeles/$modele/soumettre" '{}' >/dev/null
appel affaires POST "/clm/modeles/$modele/publier" '{}' >/dev/null
trousse=$(appel redacteur POST /clm/trousses "{\"titre\":\"Convention Agri-Nord\",\"modeleId\":$modele,\"risque\":\"FAIBLE\"}" | json 'd["resume"]["id"]')
appel redacteur PUT "/clm/trousses/$trousse/valeurs" '{"valeurs":{"beneficiaire":"Coopérative Agri-Nord inc.","montant":"250 000 $"}}' >/dev/null

pdf=$(appel redacteur POST "/clm/trousses/$trousse/generer?format=PDF")
echo "$pdf" | json 'd["motif"]' | grep -q "mise en page conservée" || ecart "le PDF n'a pas été converti par le service LibreOffice : $(echo "$pdf" | json 'd["motif"]')"
appel redacteur GET "/clm/trousses/$trousse/documents/$(echo "$pdf" | json 'd["id"]')/contenu" > "$R/contrat-gabarit.pdf"
docx=$(appel redacteur POST "/clm/trousses/$trousse/generer?format=DOCX")
appel redacteur GET "/clm/trousses/$trousse/documents/$(echo "$docx" | json 'd["id"]')/contenu" > "$R/contrat-gabarit.docx"
appel redacteur GET "/clm/trousses/$trousse/preuve/certificat" > "$R/certificat-signature.pdf"
sansgabarit=$(appel redacteur POST /clm/trousses '{"titre":"Lettre sans gabarit","risque":"FAIBLE"}' | json 'd["resume"]["id"]')
appel redacteur GET "/clm/trousses/$sansgabarit/preuve/certificat" > "$R/certificat-sans-gabarit.pdf"

VERAPDF=${VERAPDF:-http://localhost:18092}
PYHANKO=${PYHANKO:-http://localhost:18093}
echo "— Contrat Word fusionné :"
python3 - "$R/contrat-gabarit.docx" > "$R/contrat-gabarit.txt" <<'PY'
import re, sys, zipfile
z = zipfile.ZipFile(sys.argv[1])
for partie in ("word/header1.xml", "word/document.xml"):
    xml = z.read(partie).decode()
    for p in re.findall(r"<w:p[ >].*?</w:p>", xml, re.S):
        print("".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", p)))
PY
cat "$R/contrat-gabarit.txt"
grep -q "Coopérative Agri-Nord inc." "$R/contrat-gabarit.txt" || ecart "variable non fusionnée dans le Word"
grep -q "dossier Coopérative Agri-Nord inc." "$R/contrat-gabarit.txt" || ecart "en-tête non fusionné"
grep -q "Hypothèque mobilière de premier rang de 250 000" "$R/contrat-gabarit.txt" || ecart "clause absente du Word"
grep -q "{{" "$R/contrat-gabarit.txt" && ecart "marqueur résiduel dans le Word"

echo "— veraPDF REST, PDF/A-2b :"
for f in contrat-gabarit.pdf certificat-signature.pdf certificat-sans-gabarit.pdf; do
  curl -sf -X POST -H "Accept: application/json" -F "file=@$R/$f" "$VERAPDF/api/validate/2b" > "$R/verapdf-$f.json"
  python3 -c "import json,sys; v=json.load(open(sys.argv[1]))['report']['jobs'][0]['validationResult'][0]; print(sys.argv[2], v['compliant'], v['details']['passedRules'], 'règles passées,', v['details']['failedRules'], 'en échec'); sys.exit(0 if v['compliant'] else 1)" \
    "$R/verapdf-$f.json" "$f" || ecart "$f non conforme PDF/A-2b (détail : resultats/verapdf-$f.json)"
done

echo "— pyHanko, profil PAdES-B-LTA, révocation intégrée exigée :"
for f in certificat-signature.pdf certificat-sans-gabarit.pdf; do
  curl -sf -X POST -H "Content-Type: application/pdf" --data-binary "@$R/$f" "$PYHANKO/valider?profil=pades-lta" > "$R/pyhanko-$f.json"
  python3 -c "import json,sys; d=json.load(open(sys.argv[1])); r=d['rapport']
ok = d['valide'] and 'The signer\'s certificate is trusted.' in r and 'This timestamp is backed by a time stamping authority.' in r
print(sys.argv[2], 'VALIDE' if ok else 'REFUSÉ', '·', d['profil']); sys.exit(0 if ok else 1)" "$R/pyhanko-$f.json" "$f" \
    || ecart "$f refusé par pyHanko (rapport : resultats/pyhanko-$f.json)"
done
echo "Tous les contrôles des bacs à sable sont conformes."
