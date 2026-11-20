#!/bin/sh
# Contre-épreuve de révocation, réversible : l'autorité suspend le certificat de cachet (certificateHold, RFC 5280) ;
# un document scellé ensuite doit être refusé par pyHanko sur la foi des données de révocation que le serveur a
# lui-même intégrées (OCSP et liste publiés par l'ICP). L'autorité lève ensuite la suspension, quoi qu'il arrive, et un
# document scellé après doit être valide : le bac à sable reste rejouable.
set -eu
ICI=$(cd "$(dirname "$0")" && pwd)
API=${API:-http://localhost:8100/api}
ICP=${ICP:-http://localhost:18090}
PYHANKO=${PYHANKO:-http://localhost:18093}
R="${RESULTATS:-$ICI/resultats}/revocation"; rm -rf "$R"; mkdir -p "$R"
curl -sf -c "$R/r.jar" -H 'Content-Type: application/json' -d "{\"email\":\"initiateur@iq.example\",\"motDePasse\":\"${MDP_DEMO:-Passw0rd!}\"}" "$API/auth/login" >/dev/null
t=$(curl -sf -b "$R/r.jar" -H 'Content-Type: application/json' -d '{"titre":"Contre-épreuve de révocation","risque":"FAIBLE"}' "$API/clm/trousses" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['resume']['id'])")
verdict() { # $1 fichier → « valide » ou « refuse » selon pyHanko (profil B-LTA, révocation intégrée exigée)
  curl -sf -b "$R/r.jar" "$API/clm/trousses/$t/preuve/certificat" > "$R/$1.pdf"
  curl -sf -X POST -H "Content-Type: application/pdf" --data-binary "@$R/$1.pdf" "$PYHANKO/valider?profil=pades-lta" > "$R/$1.json"
  python3 -c "import json,sys; d=json.load(open(sys.argv[1])); print('valide' if d['valide'] else 'refuse')" "$R/$1.json"; }
retablir() { curl -sf -X POST "$ICP/admin/cachet/retablir" >/dev/null; }
trap retablir EXIT
curl -sf -X POST "$ICP/admin/cachet/suspendre" >/dev/null
[ "$(verdict suspendu)" = "refuse" ] || { echo "ÉCART : cachet suspendu accepté"; exit 1; }
grep -q "revoked" "$R/suspendu.json" || { echo "ÉCART : le refus ne cite pas la révocation"; exit 1; }
echo "Cachet suspendu : document refusé (« revoked »)."
retablir
[ "$(verdict retabli)" = "valide" ] || { echo "ÉCART : cachet rétabli refusé"; exit 1; }
echo "Cachet rétabli : document valide. Contre-épreuve conforme."
