#!/bin/sh
# Contre-épreuve : l'autorité révoque le certificat de cachet ; un document scellé ensuite doit être refusé par pyHanko,
# sur la foi des données de révocation que le serveur a lui-même intégrées (liste et OCSP publiés par l'ICP).
# L'ICP reste révoquée : arrêter le service icp, effacer icp/etat, le relancer, puis redémarrer le serveur.
set -eu
ICI=$(cd "$(dirname "$0")" && pwd)
API=${API:-http://localhost:8100/api}
R="$ICI/resultats"; mkdir -p "$R"
docker exec iq-bac-icp sh /icp/revoquer.sh
curl -sf -c "$R/r.jar" -H 'Content-Type: application/json' -d "{\"email\":\"initiateur@iq.example\",\"motDePasse\":\"${MDP_DEMO:-Passw0rd!}\"}" "$API/auth/login" >/dev/null
t=$(curl -sf -b "$R/r.jar" -H 'Content-Type: application/json' -d '{"titre":"Après révocation","risque":"FAIBLE"}' "$API/clm/trousses" \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['resume']['id'])")
curl -sf -b "$R/r.jar" "$API/clm/trousses/$t/preuve/certificat" > "$R/certificat-revoque.pdf"
curl -sf -X POST -H "Content-Type: application/pdf" --data-binary "@$R/certificat-revoque.pdf" "http://localhost:18093/valider?profil=pades-lta" > "$R/pyhanko-revoque.json"
python3 -c "import json,sys; d=json.load(open(sys.argv[1])); print('refusé' if not d['valide'] else 'ACCEPTÉ À TORT'); print([l for l in d['rapport'].splitlines() if 'revoked' in l.lower()][:3]); sys.exit(1 if d['valide'] else 0)" "$R/pyhanko-revoque.json"
echo "Contre-épreuve conforme : un cachet révoqué est refusé."
