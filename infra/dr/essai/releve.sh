#!/usr/bin/env bash
#
# Essai de relève (SEC-11, AO IQ 2026-6370-0005) : perte totale du site primaire et reprise sur un site secondaire neuf,
# mesurée. Chaque sous-commande est une étape du workflow .github/workflows/releve.yml ; elles partagent l'état par le
# dossier RESULTATS. Tout se joue dans l'exécuteur (conteneurs locaux) : aucun appel vers Azure ni vers un hôte public.
#
#   releve.sh site-primaire     PostgreSQL 16 (archivage continu des journaux) + serveur de production
#   releve.sh comptes           compte d'essai (rédacteur) créé par l'API d'administration
#   releve.sh jeu-de-donnees    trousses, documents chiffrés, versions, participants, parcours, lancements
#   releve.sh protection        exports de preuve répliqués + sauvegarde de base ou réplique selon SCENARIO
#   releve.sh ecriture          écritures continues (une trousse « Battement n » acquittée et horodatée en continu)
#   releve.sh sinistre          destruction du site primaire : conteneurs tués, volumes supprimés
#   releve.sh restauration      reprise sur le site secondaire jusqu'au service rétabli
#   releve.sh verification      contrôles bloquants des données, de la preuve et des parcours
#
# SCENARIO : « replique » (réplique asynchrone promue, option retenue par l'ADR-039) ou « sauvegarde » (sauvegarde de
# base + journaux archivés rejoués, restauration à un instant donné : voie de secours et protection contre la corruption).
#
set -euo pipefail
ICI="$(cd "$(dirname "$0")" && pwd)"
RACINE="$(cd "$ICI/../../.." && pwd)"
# shellcheck source=commun.sh
source "$ICI/commun.sh"

: "${SCENARIO:?SCENARIO = replique ou sauvegarde}"
: "${NB_TROUSSES:=30}"
: "${NB_EXPORTS:=5}"
: "${DUREE_ECRITURE:=90}"
: "${ARCHIVE_TIMEOUT:=60}"
: "${DOMAINE_ESSAI:=releve.iq.example}"
COURRIEL_ESSAI="essai-charge-001@${DOMAINE_ESSAI}"
JAR_PRIMAIRE="$TEMOIN/session-primaire"
JAR_SECOURS="$TEMOIN/session-secours"
TYPES=(SUBVENTION PRET CAPITAL GARANTIE)

case "$SCENARIO" in replique|sauvegarde) ;; *) echec "SCENARIO inconnu : $SCENARIO" ;; esac

# --- Site primaire ---------------------------------------------------------------------------------------------------

site_primaire() {
  docker network create "$RESEAU" >/dev/null
  for v in primaire-pgdata primaire-stockage secours-sauvegardes secours-stockage secours-pgdata; do
    docker volume create "$v" >/dev/null
  done
  # Le stockage des sauvegardes appartient au site secondaire (copie géo-redondante) : la perte du primaire l'épargne.
  docker run --rm -v secours-sauvegardes:/s "$POSTGRES_IMAGE" bash -c 'mkdir -p /s/wal /s/base && chown -R postgres:postgres /s'
  docker run -d --name primaire-db --network "$RESEAU" --cpus 1.5 --memory 3g --shm-size 256m \
    -v primaire-pgdata:/var/lib/postgresql/data -v secours-sauvegardes:/sauvegardes \
    -v "$ICI/initdb:/docker-entrypoint-initdb.d:ro" \
    -e POSTGRES_DB=contrat -e POSTGRES_USER=contrat -e POSTGRES_PASSWORD="$DB_PASSWORD" -e REPLICATION_PASSWORD \
    "$POSTGRES_IMAGE" \
    -c max_connections=200 -c wal_level=replica -c max_wal_senders=10 -c wal_keep_size=1GB \
    -c archive_mode=on -c "archive_command=test ! -f /sauvegardes/wal/%f && cp %p /sauvegardes/wal/%f" \
    -c archive_timeout="$ARCHIVE_TIMEOUT" >/dev/null
  attendre_postgres primaire-db 90 || echec "PostgreSQL primaire indisponible"
  # L'image redémarre PostgreSQL après les scripts d'initialisation : on attend la seconde disponibilité.
  sleep 3
  attendre_postgres primaire-db 60 || echec "PostgreSQL primaire indisponible après initialisation"
  demarrer_serveur primaire-app primaire-db 8080
  attendre_sante "$URL_PRIMAIRE" 240 || { docker logs primaire-app | tail -80; echec "serveur primaire indisponible"; }
  marquer site_primaire_pret
}

comptes() {
  local admin_mdp rc i
  admin_mdp=$(python3 -c 'import json,sys; print(next(t["motDePasse"] for t in json.load(open(sys.argv[1])) if t["slug"] == "iq"))' \
    "$RACINE/backend/src/main/resources/data/tenants-demo.json")
  # L'amorçage de l'organisation de démonstration suit la santé de quelques secondes : seul un refus de connexion de
  # l'administrateur (code 1) se réessaie.
  for i in $(seq 1 30); do
    rc=0
    BASE_URL="$URL_PRIMAIRE" ADMIN_EMAIL=admin.ti@iq.example ADMIN_MOT_DE_PASSE="$admin_mdp" MOT_DE_PASSE="$MOT_DE_PASSE_ESSAI" \
      NOMBRE=1 DOMAINE="$DOMAINE_ESSAI" SORTIE="$TEMOIN/comptes.csv" "$RACINE/perf/outils/amorcer-comptes.sh" || rc=$?
    [ "$rc" -eq 1 ] || return "$rc"
    sleep 5
  done
  return 1
}

# --- Jeu de données --------------------------------------------------------------------------------------------------

document() {  # document <jar> <trousse> <fichier> [clé] : téléverse, vérifie l'empreinte rendue, inscrit au manifeste
  local jar=$1 trousse=$2 fichier=$3 cle=${4:-} url reponse id empreinte attendue
  url="$URL_PRIMAIRE/api/clm/trousses/$trousse/documents"
  [ -n "$cle" ] && url="$url/$cle/versions"
  reponse=$(televerser "$jar" "$url" "$fichier") || echec "téléversement refusé ($fichier)"
  id=$(champ id <<<"$reponse")
  empreinte=$(champ empreinte <<<"$reponse")
  attendue=$(sha256 "$fichier")
  [ "$empreinte" = "$attendue" ] || echec "empreinte rendue $empreinte ≠ empreinte du fichier $attendue"
  echo "$trousse,$id,$attendue,$(stat -c %s "$fichier")" >> "$TEMOIN/documents.csv"
  champ cleDocument <<<"$reponse"
}

trousse_complete() {  # trousse_complete <numéro>
  local n=$1 type corps reponse id cle participant parcours tmp
  type=${TYPES[$((n % ${#TYPES[@]}))]}
  tmp=$(mktemp -d)
  corps=$(python3 -c 'import json,sys; print(json.dumps({"titre": "Convention de relève " + sys.argv[1], "risque": "FAIBLE",
    "typeContrat": sys.argv[2]}))' "$n" "$type")
  reponse=$(api "$JAR_PRIMAIRE" POST "$URL_PRIMAIRE/api/clm/trousses" "$corps")
  id=$(champ resume.id <<<"$reponse")
  python3 "$ICI/generer_pdf.py" "Convention $n - $type" 96 "$tmp/convention.pdf"
  python3 "$ICI/generer_pdf.py" "Annexe financiere $n" 320 "$tmp/annexe.pdf"
  cle=$(document "$JAR_PRIMAIRE" "$id" "$tmp/convention.pdf")
  [ -n "$cle" ] || echec "document principal de la trousse $id refusé"
  document "$JAR_PRIMAIRE" "$id" "$tmp/annexe.pdf" >/dev/null
  if [ $((n % 3)) -eq 0 ]; then
    python3 "$ICI/generer_pdf.py" "Convention $n - $type - version 2" 104 "$tmp/convention-v2.pdf"
    document "$JAR_PRIMAIRE" "$id" "$tmp/convention-v2.pdf" "$cle" >/dev/null
  fi
  corps=$(python3 -c 'import json,sys; print(json.dumps({"nom": "Signataire " + sys.argv[1], "courriel": "signataire" + sys.argv[1] + "@releve.example",
    "telephone": "+1514555" + sys.argv[1].zfill(4), "roleContrat": "SIGNATAIRE_1", "externe": True}))' "$n")
  participant=$(api "$JAR_PRIMAIRE" POST "$URL_PRIMAIRE/api/clm/trousses/$id/participants" "$corps" | champ id)
  parcours=$(python3 -c 'import json,sys; print(json.dumps([{"cle": "s1", "libelle": "Signature", "phase": 1, "type": "SIGNATURE",
    "affectations": [{"type": "PARTICIPANT", "valeur": sys.argv[1]}]}]))' "$participant")
  api "$JAR_PRIMAIRE" PUT "$URL_PRIMAIRE/api/clm/trousses/$id/parcours" "$parcours" >/dev/null
  if [ $((n % 2)) -eq 0 ]; then
    api "$JAR_PRIMAIRE" POST "$URL_PRIMAIRE/api/clm/trousses/$id/lancer" '{}' >/dev/null
  fi
  integre "$JAR_PRIMAIRE" "$URL_PRIMAIRE" "$id" || echec "trousse $id non intègre avant le sinistre"
  echo "$id" >> "$TEMOIN/trousses.txt"
  rm -rf "$tmp"
}

integre() {  # integre <jar> <url> <trousse> : vrai si le journal scellé et les empreintes des documents sont intacts
  api "$1" POST "$2/api/clm/trousses/$3/preuve/integrite" '{}' \
    | python3 -c 'import json,sys; d=json.load(sys.stdin); sys.exit(0 if d["integre"] and d["journalIntegre"] else 1)'
}

jeu_de_donnees() {
  : > "$TEMOIN/documents.csv"
  : > "$TEMOIN/trousses.txt"
  connexion "$URL_PRIMAIRE" "$COURRIEL_ESSAI" "$MOT_DE_PASSE_ESSAI" "$JAR_PRIMAIRE"
  local n
  for n in $(seq 1 "$NB_TROUSSES"); do trousse_complete "$n"; done
  journal "$NB_TROUSSES trousses, $(wc -l < "$TEMOIN/documents.csv") documents chargés"
  marquer jeu_de_donnees_charge
}

# Tête de la chaîne scellée de chaque trousse (nombre d'événements, dernière empreinte), relevée juste avant le sinistre.
photographier_journal() {  # photographier_journal <jar> <url> <sortie>
  local id
  : > "$3"
  while read -r id; do
    api "$1" GET "$2/api/clm/trousses/$id/historique" \
      | python3 -c 'import json,sys; e=json.load(sys.stdin); print(sys.argv[1] + "," + str(len(e)) + "," + e[-1]["empreinte"])' "$id" >> "$3"
  done < "$TEMOIN/trousses.txt"
}

# --- Protection (régime nominal) -------------------------------------------------------------------------------------

exports_repliques() {
  local id fichier tmp
  tmp=$(mktemp -d)
  : > "$TEMOIN/exports.csv"
  for id in $(head -n "$NB_EXPORTS" "$TEMOIN/trousses.txt"); do
    fichier="$tmp/preuve-$id.zip"
    curl -sS -f -b "$JAR_PRIMAIRE" -o "$fichier" "$URL_PRIMAIRE/api/clm/trousses/$id/preuve/export"
    deposer_dans_volume primaire-stockage "$fichier" "exports/preuve-$id.zip"
    echo "exports/preuve-$id.zip,$(sha256 "$fichier")" >> "$TEMOIN/exports.csv"
  done
  # Réplication du compte de stockage vers la région de relève (GZRS/RA-GZRS dans Azure, asynchrone).
  docker run --rm -v primaire-stockage:/src:ro -v secours-stockage:/dst "$POSTGRES_IMAGE" cp -a /src/. /dst/
  rm -rf "$tmp"
  marquer stockage_replique
}

sauvegarde_de_base() {
  docker run --rm --network "$RESEAU" --user postgres -v secours-sauvegardes:/sauvegardes -e PGPASSWORD="$REPLICATION_PASSWORD" \
    "$POSTGRES_IMAGE" pg_basebackup -h primaire-db -U replicateur -D /sauvegardes/base -Fp -X stream -c fast
  marquer sauvegarde_de_base_complete
}

replique_secours() {
  docker run --rm -v secours-pgdata:/donnees "$POSTGRES_IMAGE" bash -c 'chown postgres:postgres /donnees && chmod 700 /donnees'
  docker run --rm --network "$RESEAU" --user postgres -v secours-pgdata:/donnees -e PGPASSWORD="$REPLICATION_PASSWORD" \
    "$POSTGRES_IMAGE" pg_basebackup -h primaire-db -U replicateur -D /donnees -Fp -X stream -c fast -R
  docker run -d --name secours-db --network "$RESEAU" --cpus 1.5 --memory 3g --shm-size 256m \
    -v secours-pgdata:/var/lib/postgresql/data "$POSTGRES_IMAGE" -c max_connections=200 -c hot_standby=on >/dev/null
  attendre_postgres secours-db 90 || echec "réplique indisponible"
  [ "$(psql_site secours-db 'SELECT pg_is_in_recovery()')" = t ] || echec "la base secondaire n'est pas en réplication"
  local etat i
  for i in $(seq 1 30); do
    etat=$(psql_site primaire-db "SELECT state FROM pg_stat_replication LIMIT 1")
    [ "$etat" = streaming ] && break
    sleep 1
  done
  [ "$etat" = streaming ] || echec "réplication non établie (état : ${etat:-aucun})"
  marquer replique_synchronisee
}

protection() {
  exports_repliques
  case "$SCENARIO" in
    sauvegarde) sauvegarde_de_base ;;
    replique) replique_secours ;;
  esac
  photographier_journal "$JAR_PRIMAIRE" "$URL_PRIMAIRE" "$TEMOIN/journal-avant.csv"
}

# --- Écritures continues ---------------------------------------------------------------------------------------------

ecrivain() {
  local jar="$TEMOIN/session-ecrivain" n=0 reponse id
  connexion "$URL_PRIMAIRE" "$COURRIEL_ESSAI" "$MOT_DE_PASSE_ESSAI" "$jar"
  echo "numero,acquitte_ms,trousse" > "$TEMOIN/ecritures.csv"
  while true; do
    n=$((n + 1))
    if reponse=$(curl -sS -f --max-time 5 -b "$jar" -H 'Content-Type: application/json' \
        -d "{\"titre\":\"Battement $n\",\"risque\":\"FAIBLE\",\"typeContrat\":\"SUBVENTION\"}" \
        "$URL_PRIMAIRE/api/clm/trousses" 2>/dev/null); then
      id=$(champ resume.id <<<"$reponse" 2>/dev/null || echo "?")
      echo "$n,$(maintenant_ms),$id" >> "$TEMOIN/ecritures.csv"
    else
      echo "$n,$(maintenant_ms)" >> "$TEMOIN/ecritures-refusees.csv"
      sleep 0.2
    fi
    sleep 0.3
  done
}

ecriture() {
  nohup "$0" ecrivain > "$RESULTATS/ecrivain.log" 2>&1 &
  echo $! > "$TEMOIN/ecrivain.pid"
  sleep 5
  [ "$(tail -n +2 "$TEMOIN/ecritures.csv" | wc -l)" -gt 0 ] || echec "aucune écriture continue acquittée"
  marquer ecriture_continue_demarree
}

# --- Sinistre --------------------------------------------------------------------------------------------------------

sinistre() {
  journal "écritures continues pendant ${DUREE_ECRITURE} s avant le sinistre"
  sleep "$DUREE_ECRITURE"
  mkdir -p "$RESULTATS/journaux"
  marquer perte_site_primaire
  # Perte brutale et simultanée de la base et du serveur (SIGKILL, ni arrêt propre ni dernier archivage).
  docker kill primaire-db primaire-app >/dev/null
  sleep 3
  kill "$(cat "$TEMOIN/ecrivain.pid")" 2>/dev/null || true
  docker logs primaire-app > "$RESULTATS/journaux/primaire-app.log" 2>&1 || true
  docker logs primaire-db > "$RESULTATS/journaux/primaire-db.log" 2>&1 || true
  docker rm -f -v primaire-db primaire-app >/dev/null
  docker volume rm primaire-pgdata primaire-stockage >/dev/null
  if docker volume inspect primaire-pgdata >/dev/null 2>&1 || docker volume inspect primaire-stockage >/dev/null 2>&1; then
    echec "les volumes du site primaire existent encore"
  fi
  marquer site_primaire_detruit
}

# --- Restauration ----------------------------------------------------------------------------------------------------

attendre_fin_de_recuperation() {  # attendre_fin_de_recuperation <secondes>
  local i
  for i in $(seq 1 "$1"); do
    [ "$(psql_site secours-db 'SELECT pg_is_in_recovery()' 2>/dev/null)" = f ] && return 0
    sleep 1
  done
  return 1
}

restaurer_base() {
  case "$SCENARIO" in
    replique)
      psql_site secours-db 'SELECT pg_promote(true, 120)' >/dev/null
      attendre_fin_de_recuperation 120 || echec "promotion de la réplique non terminée"
      marquer base_promue
      ;;
    sauvegarde)
      # Restauration à un instant donné : sauvegarde de base + rejeu de tous les journaux archivés disponibles.
      docker run --rm -v secours-sauvegardes:/sauvegardes:ro -v secours-pgdata:/donnees "$POSTGRES_IMAGE" bash -c "
        cp -a /sauvegardes/base/. /donnees/ && touch /donnees/recovery.signal &&
        echo \"restore_command = 'cp /sauvegardes/wal/%f %p'\" >> /donnees/postgresql.auto.conf &&
        chown -R postgres:postgres /donnees && chmod 700 /donnees"
      docker run -d --name secours-db --network "$RESEAU" --cpus 1.5 --memory 3g --shm-size 256m \
        -v secours-pgdata:/var/lib/postgresql/data -v secours-sauvegardes:/sauvegardes:ro \
        "$POSTGRES_IMAGE" -c max_connections=200 >/dev/null
      attendre_postgres secours-db 300 || { docker logs secours-db | tail -50; echec "base restaurée indisponible"; }
      attendre_fin_de_recuperation 600 || { docker logs secours-db | tail -50; echec "rejeu des journaux non terminé"; }
      marquer base_restauree
      ;;
  esac
}

restauration() {
  marquer debut_restauration
  restaurer_base
  docker run --rm -v secours-stockage:/v:ro "$POSTGRES_IMAGE" test -d /v/exports || echec "stockage secondaire vide"
  marquer stockage_bascule
  # Même image, mêmes secrets : la clé maîtresse vient du coffre de la région de relève (ici l'environnement de l'essai),
  # jamais du site perdu.
  demarrer_serveur secours-app secours-db 8081
  attendre_sante "$URL_SECOURS" 300 || { docker logs secours-app | tail -80; echec "serveur secondaire indisponible"; }
  marquer serveur_secondaire_demarre
  local i
  for i in $(seq 1 120); do
    if connexion "$URL_SECOURS" "$COURRIEL_ESSAI" "$MOT_DE_PASSE_ESSAI" "$JAR_SECOURS" 2>/dev/null \
      && api "$JAR_SECOURS" GET "$URL_SECOURS/api/clm/trousses?q=Convention" | python3 -c 'import json,sys; sys.exit(0 if json.load(sys.stdin)["lignes"] else 1)'; then
      marquer service_retabli
      return 0
    fi
    sleep 1
  done
  echec "le service ne répond pas aux parcours de base sur le site secondaire"
}

# --- Vérification ----------------------------------------------------------------------------------------------------

controle() {  # controle <identifiant> <libellé> <attendu> <obtenu>
  python3 -c 'import json,sys; print(json.dumps({"id": sys.argv[1], "libelle": sys.argv[2], "attendu": sys.argv[3],
    "obtenu": sys.argv[4], "ok": sys.argv[3] == sys.argv[4]}, ensure_ascii=False))' "$@" >> "$RESULTATS/controles.jsonl"
  [ "$3" = "$4" ] && journal "✓ $2 ($4)" || journal "✗ $2 : attendu $3, obtenu $4"
}

verifier_journal() {
  photographier_journal "$JAR_SECOURS" "$URL_SECOURS" "$TEMOIN/journal-apres.csv"
  local total identiques
  total=$(wc -l < "$TEMOIN/journal-avant.csv")
  identiques=$(grep -cxFf "$TEMOIN/journal-avant.csv" "$TEMOIN/journal-apres.csv" || true)
  controle V1 "Têtes de chaîne du journal scellé identiques (nombre d'événements et dernière empreinte)" "$total" "$identiques"
}

verifier_documents() {
  local total=0 ok=0 trousse id attendue tmp
  tmp=$(mktemp)
  while IFS=, read -r trousse id attendue _; do
    total=$((total + 1))
    if curl -sS -f -b "$JAR_SECOURS" -o "$tmp" "$URL_SECOURS/api/clm/trousses/$trousse/documents/$id/contenu" \
      && [ "$(sha256 "$tmp")" = "$attendue" ]; then
      ok=$((ok + 1))
    fi
  done < "$TEMOIN/documents.csv"
  rm -f "$tmp"
  controle V2 "Documents déchiffrés par la clé de l'organisation, empreinte SHA-256 identique à l'original" "$total" "$ok"
  local enveloppes
  enveloppes=$(psql_site secours-db "SELECT count(*) FILTER (WHERE substring(contenu from 1 for 5) = '\\x0056434501'::bytea) || '/' || count(*) FROM clm_document")
  controle V3 "Documents chiffrés au repos dans la base restaurée (enveloppe ADR-037)" "$total/$total" "$enveloppes"
}

verifier_integrite() {
  local total=0 ok=0 id
  while read -r id; do
    total=$((total + 1))
    integre "$JAR_SECOURS" "$URL_SECOURS" "$id" && ok=$((ok + 1))
  done < "$TEMOIN/trousses.txt"
  controle V4 "Vérification d'intégrité de la preuve (chaîne scellée + empreintes) sur le site secondaire" "$total" "$ok"
}

verifier_exports() {
  local tmp total ok
  tmp=$(mktemp -d)
  docker run --rm -v secours-stockage:/v:ro -v "$tmp:/out" "$POSTGRES_IMAGE" bash -c 'cp -a /v/. /out/ && chmod -R a+r /out'
  total=$(wc -l < "$TEMOIN/exports.csv")
  ok=$(python3 - "$tmp" "$TEMOIN/exports.csv" <<'PY'
import csv, hashlib, sys, zipfile
racine, manifeste = sys.argv[1], sys.argv[2]
ok = 0
for chemin, empreinte in csv.reader(open(manifeste)):
    fichier = f"{racine}/{chemin}"
    try:
        intact = hashlib.sha256(open(fichier, "rb").read()).hexdigest() == empreinte
        lisible = zipfile.ZipFile(fichier).testzip() is None
    except (OSError, zipfile.BadZipFile):
        continue
    ok += intact and lisible
print(ok)
PY
)
  rm -rf "$tmp"
  controle V5 "Exports de preuve du stockage répliqué intacts et lisibles" "$total" "$ok"
}

verifier_parcours() {
  local corps id participant tmp etapes=0
  tmp=$(mktemp -d)
  corps='{"titre":"Convention créée après la relève","risque":"FAIBLE","typeContrat":"PRET"}'
  id=$(api "$JAR_SECOURS" POST "$URL_SECOURS/api/clm/trousses" "$corps" | champ resume.id) && etapes=$((etapes + 1))
  python3 "$ICI/generer_pdf.py" "Convention apres releve" 64 "$tmp/apres.pdf"
  [ "$(televerser "$JAR_SECOURS" "$URL_SECOURS/api/clm/trousses/$id/documents" "$tmp/apres.pdf" | champ empreinte)" = "$(sha256 "$tmp/apres.pdf")" ] \
    && etapes=$((etapes + 1))
  participant=$(api "$JAR_SECOURS" POST "$URL_SECOURS/api/clm/trousses/$id/participants" \
    '{"nom":"Signataire apres releve","courriel":"apres@releve.example","telephone":"+15145559999","roleContrat":"SIGNATAIRE_1","externe":true}' \
    | champ id) && etapes=$((etapes + 1))
  api "$JAR_SECOURS" PUT "$URL_SECOURS/api/clm/trousses/$id/parcours" \
    "[{\"cle\":\"s1\",\"libelle\":\"Signature\",\"phase\":1,\"type\":\"SIGNATURE\",\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"$participant\"}]}]" \
    >/dev/null && etapes=$((etapes + 1))
  api "$JAR_SECOURS" POST "$URL_SECOURS/api/clm/trousses/$id/lancer" '{}' >/dev/null && etapes=$((etapes + 1))
  integre "$JAR_SECOURS" "$URL_SECOURS" "$id" && etapes=$((etapes + 1))
  api "$JAR_SECOURS" GET "$URL_SECOURS/api/clm/suivi" >/dev/null && etapes=$((etapes + 1))
  rm -rf "$tmp"
  controle V6 "Parcours principal sur le site secondaire (créer, déposer, signataire, parcours, lancer, intégrité, suivi)" 7 "$etapes"
}

verifier_ecritures() {
  psql_site secours-db "SELECT substring(titre from 'Battement ([0-9]+)') FROM clm_trousse WHERE titre LIKE 'Battement %'" \
    | sort -n > "$RESULTATS/battements-restaures.txt"
  local nombre maximum
  nombre=$(wc -l < "$RESULTATS/battements-restaures.txt")
  maximum=$(tail -n 1 "$RESULTATS/battements-restaures.txt")
  # Cohérence transactionnelle : les écritures restaurées forment un préfixe sans trou (1..n).
  controle V7 "Écritures continues restaurées sans trou (préfixe 1..n)" "${maximum:-0}" "$nombre"
}

verification() {
  : > "$RESULTATS/controles.jsonl"
  local volet echecs=0
  # Ordre imposé : le journal est relevé avant toute lecture qui y inscrirait un événement (téléchargement, intégrité).
  for volet in verifier_journal verifier_documents verifier_integrite verifier_exports verifier_parcours verifier_ecritures; do
    "$volet" || controle "${volet}" "$volet terminé sans erreur" ok erreur
  done
  marquer service_verifie
  echecs=$(python3 -c 'import json,sys; print(sum(not json.loads(l)["ok"] for l in open(sys.argv[1])))' "$RESULTATS/controles.jsonl")
  [ "$echecs" -eq 0 ] || echec "$echecs contrôle(s) de vérification en échec"
}

case "${1:-}" in
  site-primaire) site_primaire ;;
  comptes) comptes ;;
  jeu-de-donnees) jeu_de_donnees ;;
  protection) protection ;;
  ecriture) ecriture ;;
  ecrivain) ecrivain ;;
  sinistre) sinistre ;;
  restauration) restauration ;;
  verification) verification ;;
  *) echec "étape inconnue : ${1:-} (voir l'en-tête de $0)" ;;
esac
