#!/usr/bin/env bash
# Essai d'horodatage RFC 3161 : empreinte SHA-256 d'un document, requête à une autorité d'horodatage (TSA), puis
# vérification du jeton. Valide le mécanisme avant le choix de l'autorité qualifiée d'IQ ; les TSA publiques ci-dessous
# servent aux essais, aucune n'est retenue. Usage : ./tester-tsa.sh <fichier> [url-tsa]
set -euo pipefail
fichier="${1:?Usage : $0 <fichier> [url-tsa]}"
tsa="${2:-http://timestamp.digicert.com}"
travail="$(mktemp -d)"
trap 'rm -rf "$travail"' EXIT

openssl ts -query -data "$fichier" -sha256 -cert -no_nonce -out "$travail/requete.tsq"
curl -sS --fail -H 'Content-Type: application/timestamp-query' --data-binary "@$travail/requete.tsq" "$tsa" -o "$travail/reponse.tsr"
echo "== Jeton reçu de $tsa"
openssl ts -reply -in "$travail/reponse.tsr" -text | grep -E 'Status:|Time stamp:|Hash Algorithm|TSA:|Serial number' || true

# Vérification : empreinte du document et chaîne de certification de l'autorité (magasin de confiance du système).
ca="${TSA_CA:-}"
if [[ -z "$ca" ]]; then
  for c in /etc/ssl/cert.pem /etc/ssl/certs/ca-certificates.crt; do [[ -f "$c" ]] && ca="$c" && break; done
fi
openssl ts -reply -in "$travail/reponse.tsr" -token_out -out "$travail/jeton.der" 2>/dev/null
openssl pkcs7 -inform DER -in "$travail/jeton.der" -print_certs -out "$travail/chaine.pem" 2>/dev/null
if openssl ts -verify -data "$fichier" -in "$travail/reponse.tsr" -CAfile "$ca" -untrusted "$travail/chaine.pem" >/dev/null 2>&1; then
  echo "== Vérification : OK (empreinte et signature de l'autorité valides)"
else
  echo "== Vérification : ÉCHEC (fournir la racine de l'autorité par TSA_CA=…)" && exit 1
fi
