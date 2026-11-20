#!/bin/sh
# Signature à valeur probante de bout en bout (ST-09), par les vrais canaux :
#  1. le serveur invite un signataire externe par courriel SMTP (Mailpit) ; le lien et le code y sont lus comme dans
#     une boîte réelle ; le signataire vérifie son identité et signe au portail ;
#  2. l'attestation CAdES (cachet X.509 de l'ICP, horodatage RFC 3161) est vérifiée HORS plateforme par
#     « openssl cms -verify », avec la racine de l'ICP pour seule ancre ; la plateforme confirme l'intégrité de la preuve.
# Préalable : serveur démarré avec CLM_SIGNATURE_FOURNISSEUR=cachet, NOTIFICATION_COURRIEL_MODE=smtp,
# MAIL_HOST=localhost, MAIL_PORT=11025 (en plus des variables CLM_CACHET_* du guide).
set -eu
ICI=$(cd "$(dirname "$0")" && pwd)
API=${API:-http://localhost:8100/api}
BOITE=${BOITE:-http://localhost:18025/api/v1}
R="${RESULTATS:-$ICI/resultats}/signature"; rm -rf "$R"; mkdir -p "$R"
json() { python3 -c "import json,sys; d=json.load(sys.stdin); print($1)"; }
ecart() { echo "ÉCART : $1"; exit 1; }
curl -sf -c "$R/s.jar" -H 'Content-Type: application/json' -d "{\"email\":\"initiateur@iq.example\",\"motDePasse\":\"${MDP_DEMO:-Passw0rd!}\"}" "$API/auth/login" >/dev/null
appel() { if [ $# -ge 3 ]; then curl -sf -b "$R/s.jar" -X "$1" -H 'Content-Type: application/json' -d "$3" "$API$2"; else curl -sf -b "$R/s.jar" -X "$1" "$API$2"; fi; }
portail() { curl -sf -X POST -H 'Content-Type: application/json' -d "$2" "$API/clm/portail/$JETON$1"; }
attendre() { # $1 destinataire  $2 motif (regex Python) → première occurrence dans le plus récent message correspondant
  i=0; while [ $i -lt 30 ]; do
    id=$(curl -sf "$BOITE/search?query=to:$1" | json 'd["messages"][0]["ID"] if d["messages"] else ""' || true)
    if [ -n "$id" ]; then
      v=$(curl -sf "$BOITE/message/$id" | python3 -c "import json,re,sys; m=re.search(sys.argv[1], json.load(sys.stdin)['Text']); print(m.group(1) if m else '')" "$2")
      [ -n "$v" ] && { echo "$v"; return 0; }
    fi
    i=$((i+1)); sleep 1
  done; return 1; }

SIGNATAIRE="signataire.$(date +%s)@agrinord.example"
curl -sf -X DELETE "$BOITE/messages" >/dev/null || true
printf '%%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%%%EOF\n' > "$R/convention.pdf"
t=$(appel POST /clm/trousses '{"titre":"Convention signée en bac à sable","risque":"FAIBLE"}' | json 'd["resume"]["id"]')
curl -sf -b "$R/s.jar" -F "fichier=@$R/convention.pdf;type=application/pdf" "$API/clm/trousses/$t/documents" >/dev/null
p=$(appel POST "/clm/trousses/$t/participants" "{\"nom\":\"Marie Tremblay\",\"courriel\":\"$SIGNATAIRE\",\"telephone\":\"+15145550190\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":true}" | json 'd["id"]')
appel PUT "/clm/trousses/$t/parcours" "[{\"cle\":\"s1\",\"libelle\":\"Signature\",\"phase\":1,\"type\":\"SIGNATURE\",\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"$p\"}]}]" >/dev/null
appel POST "/clm/trousses/$t/lancer" '{}' >/dev/null
JETON=$(attendre "$SIGNATAIRE" '/portail/([A-Za-z0-9_-]+)') || ecart "aucune invitation reçue par courriel"
echo "Invitation reçue par SMTP (Mailpit)."
portail /verification '{}' >/dev/null
CODE=$(attendre "$SIGNATAIRE" '\b(\d{6})\b') || ecart "aucun code de vérification reçu"
portail /verification/reponse "{\"reponse\":\"$CODE\"}" >/dev/null
portail /signer '{"nomComplet":"Marie Tremblay","consentement":true}' > "$R/recu.json"
echo "Signé au portail : $(json 'd["attestation"]' < "$R/recu.json")"

appel GET "/clm/trousses/$t/preuve" > "$R/preuve.json"
python3 - "$R" <<'PY'
import base64, json, sys
r = sys.argv[1]
s = json.load(open(f"{r}/preuve.json"))["signatures"][0]
a = s["attestation"]
assert a["fournisseur"] == "clm-cachet", a["fournisseur"]
open(f"{r}/attestation.p7s", "wb").write(base64.b64decode(a["jetonHorodatage"]))
canon = "|".join([a["empreinteDocuments"], s["participant"], s["courriel"], s["niveau"], s["consentementLe"]])
open(f"{r}/attestation.txt", "w").write(canon)
print("Attestation :", a["algorithme"], a["identifiant"], "horodatée", a["horodatage"])
PY
docker cp "$R/attestation.p7s" iq-bac-icp:/tmp/attestation.p7s >/dev/null
docker cp "$R/attestation.txt" iq-bac-icp:/tmp/attestation.txt >/dev/null
docker exec iq-bac-icp openssl cms -verify -binary -inform DER -in /tmp/attestation.p7s -content /tmp/attestation.txt \
  -CAfile /etat/ac/ac.pem -purpose any -out /dev/null 2>&1 | tee "$R/openssl.txt"
grep -q "Verification successful" "$R/openssl.txt" || ecart "attestation refusée par OpenSSL"
printf 'altéré' >> "$R/attestation.txt"; docker cp "$R/attestation.txt" iq-bac-icp:/tmp/attestation.txt >/dev/null
docker exec iq-bac-icp openssl cms -verify -binary -inform DER -in /tmp/attestation.p7s -content /tmp/attestation.txt \
  -CAfile /etat/ac/ac.pem -purpose any -out /dev/null >/dev/null 2>&1 && ecart "attestation altérée acceptée"
echo "Attestation altérée refusée par OpenSSL."
appel POST "/clm/trousses/$t/preuve/integrite" '{}' | json '"Intégrité de la preuve : " + str(d["integre"])' | tee "$R/integrite.txt"
grep -q "True" "$R/integrite.txt" || ecart "la plateforme juge la preuve non intègre"
echo "Signature probante conforme : invitation SMTP, identité vérifiée, attestation CAdES vérifiée hors plateforme."
