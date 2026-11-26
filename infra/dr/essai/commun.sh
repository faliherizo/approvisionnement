# shellcheck shell=bash
# Fonctions communes de l'essai de relève (SEC-11). Sourcé par releve.sh ; aucun secret n'est écrit sur disque hors
# des volumes éphémères de l'exécuteur.

: "${RESULTATS:?RESULTATS, dossier des résultats, est obligatoire}"
: "${TEMOIN:=$RESULTATS/temoin}"
: "${POSTGRES_IMAGE:=postgres:16}"
: "${IMAGE_SERVEUR:=contrat/backend:releve}"
: "${RESEAU:=releve}"
: "${URL_PRIMAIRE:=http://127.0.0.1:8080}"
: "${URL_SECOURS:=http://127.0.0.1:8081}"

# Le témoin est tenu HORS des deux sites (il joue le rôle des registres de l'exploitation) : il garde l'heure de chaque
# écriture acquittée, le manifeste des empreintes et la chronologie. La perte du site primaire ne l'atteint pas.
mkdir -p "$RESULTATS" "$TEMOIN"

maintenant_ms() { date +%s%3N; }

journal() { printf '[%s] %s\n' "$(date -u +%H:%M:%S)" "$*"; }

echec() { printf '::error::%s\n' "$*" >&2; exit 1; }

# Jalon de la chronologie (étape, horodatage en millisecondes) : base du calcul du RTO.
marquer() {
  local t
  t=$(maintenant_ms)
  echo "$1,$t" >> "$RESULTATS/chronologie.csv"
  journal "jalon $1"
}

# Requête JSON authentifiée : api <jar> <méthode> <url> [corps] — écrit la réponse sur la sortie standard, échoue si
# le code HTTP n'est pas 2xx.
api() {
  local jar=$1 methode=$2 url=$3 corps=${4:-} sortie code
  sortie=$(mktemp)
  if [ -n "$corps" ]; then
    code=$(curl -sS -o "$sortie" -w '%{http_code}' -b "$jar" -X "$methode" -H 'Content-Type: application/json' \
      --data-binary "$corps" "$url")
  else
    code=$(curl -sS -o "$sortie" -w '%{http_code}' -b "$jar" -X "$methode" "$url")
  fi
  case "$code" in
    2??) cat "$sortie"; rm -f "$sortie" ;;
    *) echo "HTTP $code sur $methode $url : $(head -c 300 "$sortie")" >&2; rm -f "$sortie"; return 1 ;;
  esac
}

# Téléversement multipart d'un fichier dans une trousse.
televerser() {
  local jar=$1 url=$2 fichier=$3
  curl -sS -f -b "$jar" -F "fichier=@$fichier;type=application/pdf" "$url"
}

connexion() {
  local url=$1 courriel=$2 mdp=$3 jar=$4 corps
  corps=$(python3 -c 'import json,sys; print(json.dumps({"email": sys.argv[1], "motDePasse": sys.argv[2]}))' "$courriel" "$mdp")
  curl -sS -f -o /dev/null -c "$jar" -H 'Content-Type: application/json' -d "$corps" "$url/api/auth/login"
}

champ() { python3 -c 'import json,sys; d=json.load(sys.stdin)
for k in sys.argv[1].split("."): d=d[int(k)] if k.isdigit() else d[k]
print(d)' "$1"; }

sha256() { sha256sum "$1" | cut -d' ' -f1; }

attendre_sante() {  # attendre_sante <url> <secondes>
  local url=$1 limite=$2 i
  for i in $(seq 1 "$limite"); do
    curl -fs "$url/api/health" >/dev/null 2>&1 && return 0
    sleep 1
  done
  return 1
}

attendre_postgres() {  # attendre_postgres <conteneur> <secondes>
  local conteneur=$1 limite=$2 i
  for i in $(seq 1 "$limite"); do
    docker exec "$conteneur" pg_isready -U contrat -d contrat >/dev/null 2>&1 && return 0
    sleep 1
  done
  return 1
}

psql_site() {  # psql_site <conteneur> <requête> — résultat brut, sans en-tête
  docker exec "$1" psql -U contrat -d contrat -tAX -v ON_ERROR_STOP=1 -c "$2"
}

# Serveur applicatif de production (même image, même profil que la cible) branché sur la base d'un site.
demarrer_serveur() {  # demarrer_serveur <nom> <hôte de la base> <port local>
  docker run -d --name "$1" --network "$RESEAU" -p "127.0.0.1:$3:8080" \
    --cpus 2 --memory 2g --memory-swap 2g \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e DB_URL="jdbc:postgresql://$2:5432/contrat" -e DB_USER=contrat -e DB_PASSWORD \
    -e JWT_SECRET -e APP_CRYPTO_KEY -e APP_CRYPTO_SALT -e CLM_PREUVE_CLE \
    -e CLM_ENVIRONNEMENT=PROD -e CLM_CLE_MAITRESSE -e CLM_CLE_MAITRESSE_ID=releve-1 \
    -e COOKIE_SECURE=false -e APP_HTTP_LOCAL_AUTORISE=true -e APP_TENANTS_DEMO=true \
    -e LOG_APP_LEVEL=INFO \
    -e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError" \
    "$IMAGE_SERVEUR" >/dev/null
}

# Copie d'un fichier de l'exécuteur vers un volume Docker (le volume est le « stockage objet » d'un site).
deposer_dans_volume() {  # deposer_dans_volume <volume> <fichier local> <chemin dans le volume>
  docker run --rm -v "$1:/v" -v "$(dirname "$2"):/in:ro" "$POSTGRES_IMAGE" \
    bash -c "mkdir -p \"/v/$(dirname "$3")\" && cp \"/in/$(basename "$2")\" \"/v/$3\""
}
