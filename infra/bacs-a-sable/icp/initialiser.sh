#!/bin/sh
# Crée, une seule fois, l'autorité racine, le certificat de cachet (PKCS#12), l'autorité d'horodatage et le
# répondeur OCSP. Tout vit dans /etat (monté depuis sandbox/icp/etat, ignoré par git).
set -eu
CNF=/icp/openssl.cnf
[ -f /etat/cachet.p12 ] && { echo "ICP déjà initialisée."; exit 0; }
mkdir -p /etat/ac/emis /etat/tsa /etat/ocsp
touch /etat/ac/index.txt
echo 1000 > /etat/ac/serial
echo 1000 > /etat/ac/crlnumber
echo 01 > /etat/tsa/serial
emettre() { # $1 nom  $2 sujet  $3 extension  $4 dossier
  openssl req -utf8 -new -newkey rsa:3072 -nodes -keyout "$4/$1.key" -subj "$2" -out "$4/$1.csr" -config $CNF 2>/dev/null
  openssl ca -batch -config $CNF -extensions "$3" -in "$4/$1.csr" -out "$4/$1.pem" -notext 2>/dev/null
}
openssl req -utf8 -x509 -new -newkey rsa:4096 -nodes -days 3650 -keyout /etat/ac/ac.key -out /etat/ac/ac.pem \
  -subj "/C=CA/O=ICP du bac à sable/CN=AC racine du bac à sable" -config $CNF -extensions v3_ac 2>/dev/null
openssl x509 -in /etat/ac/ac.pem -outform DER -out /etat/ac/ac.cer
emettre cachet "/C=CA/O=Organisation du bac à sable/CN=Cachet électronique du bac à sable" v3_cachet /etat
emettre tsa "/C=CA/O=ICP du bac à sable/CN=Autorité d'horodatage du bac à sable" v3_tsa /etat/tsa
emettre ocsp "/C=CA/O=ICP du bac à sable/CN=Répondeur OCSP du bac à sable" v3_ocsp /etat/ocsp
# Mot de passe propre à ce bac à sable, généré localement : jamais un secret de production.
openssl rand -hex 16 > /etat/mot-de-passe.txt
openssl pkcs12 -export -inkey /etat/cachet.key -in /etat/cachet.pem -certfile /etat/ac/ac.pem -name cachet \
  -passout file:/etat/mot-de-passe.txt -out /etat/cachet.p12
chmod 600 /etat/*.key /etat/*/*.key
# Lisibles par le serveur lancé hors du conteneur (sur un runner Linux, le conteneur écrit en root) : magasin et mot de
# passe propres au bac à sable, jamais des secrets de production.
chmod 644 /etat/mot-de-passe.txt /etat/cachet.p12
echo "ICP initialisée : $(openssl x509 -in /etat/cachet.pem -noout -subject)"
