#!/usr/bin/env bash
# =============================================================================
# Orchestration de la bascule après sinistre AZURE (Canada Central -> Canada Est).
# Séquence : détection -> Terraform DR -> réseau/sécurité -> Container Apps -> GeoRestore PostgreSQL
#   -> stockage répliqué -> image -> tests de santé -> redirection Front Door -> service rétabli.
# Objectifs : RTO <= 4 h, RPO <= 15 min.
#
# Prérequis : le stack infra/dr/azure/replication est appliqué en continu (ACR DR + réplication
# d'objets + sauvegardes géo-redondantes PostgreSQL en production).
#
# Usage :
#   ./dr-failover-azure.sh          # bascule réelle (confirmation « OUI »)
#   ./dr-failover-azure.sh --drill  # exercice (laisse la production intacte)
# =============================================================================
set -Eeuo pipefail

PROJECT="${PROJECT:-vina-contrat}"
ENVIRONMENT="${ENVIRONMENT:-prod}"
DR_LOCATION="${DR_LOCATION:-canadaeast}"
PRIMARY_RG="${PRIMARY_RG:-${PROJECT}-${ENVIRONMENT}-rg}"
SOURCE_PG_ID="${SOURCE_PG_ID:-}"                 # ID du serveur PostgreSQL de production
AFD_PROFILE="${AFD_PROFILE:-}"                    # profil Front Door (redirection du trafic)
AFD_ORIGIN_GROUP="${AFD_ORIGIN_GROUP:-}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
REPL_DIR="$(cd "$(dirname "$0")/../azure/replication" && pwd)"
FAILOVER_DIR="$(cd "$(dirname "$0")/../azure/failover" && pwd)"
DRILL=false
[[ "${1:-}" == "--drill" ]] && DRILL=true

log()  { printf '\n\033[1;36m[%(%H:%M:%S)T] %s\033[0m\n' -1 "$*"; }
fail() { printf '\n\033[1;31mÉCHEC : %s\033[0m\n' "$*" >&2; exit 1; }

confirm() {
  $DRILL && return 0
  read -r -p "Confirmer la BASCULE RÉELLE vers ${DR_LOCATION} ? (taper OUI) : " a
  [[ "$a" == "OUI" ]] || fail "Bascule annulée."
}

# 1. Détection de l'incident.
log "1/10  Détection de l'incident sur la région primaire (Canada Central)."
confirm

# 2. Terraform DR : sorties de la réplication.
log "2/10  Lecture des sorties du stack de réplication."
pushd "$REPL_DIR" >/dev/null
terraform init -input=false >/dev/null
DR_ACR_NAME="$(terraform output -raw dr_acr_name)"
DR_RG="$(terraform output -raw dr_resource_group)"
popd >/dev/null
[[ -n "$DR_ACR_NAME" && -n "$DR_RG" ]] || fail "Sorties de réplication introuvables."

# ID du serveur PostgreSQL source + mot de passe depuis le Key Vault primaire.
if [[ -z "$SOURCE_PG_ID" ]]; then
  SOURCE_PG_ID="$(az postgres flexible-server show -g "$PRIMARY_RG" -n "${PROJECT}-${ENVIRONMENT}-pg" --query id -o tsv)"
fi
PRIMARY_KV="$(echo "${PROJECT}-${ENVIRONMENT}-kv" | tr -d '-')"
DB_PASSWORD="$(az keyvault secret show --vault-name "$PRIMARY_KV" --name db-password --query value -o tsv 2>/dev/null || echo "")"

# 3-8. Terraform apply : réseau + Container Apps + GeoRestore PostgreSQL + stockage + image.
log "3-8/10  Reconstruction de l'infrastructure de reprise (réseau, Container Apps, GeoRestore, stockage, image)."
pushd "$FAILOVER_DIR" >/dev/null
terraform init -input=false >/dev/null
terraform apply -input=false -auto-approve \
  -var "dr_location=${DR_LOCATION}" \
  -var "source_postgres_server_id=${SOURCE_PG_ID}" \
  -var "dr_acr_name=${DR_ACR_NAME}" \
  -var "dr_acr_resource_group=${DR_RG}" \
  -var "db_password=${DB_PASSWORD}"
FRONTEND_FQDN="$(terraform output -raw frontend_fqdn)"
popd >/dev/null
[[ -n "$FRONTEND_FQDN" ]] || fail "Frontend de reprise indisponible."

# 9. Tests de santé avant redirection.
log "9/10  Vérification de santé sur https://${FRONTEND_FQDN}${HEALTH_PATH}"
for i in $(seq 1 30); do
  if curl -fsS --max-time 10 "https://${FRONTEND_FQDN}${HEALTH_PATH}" >/dev/null 2>&1; then
    log "Santé OK (tentative ${i})."; HEALTHY=1; break
  fi
  sleep 20
done
[[ "${HEALTHY:-}" == "1" ]] || fail "Le service de reprise ne répond pas — NE PAS rediriger le trafic."

# 10. Redirection du trafic (Front Door) puis service rétabli.
if $DRILL; then
  log "10/10  [EXERCICE] Redirection NON appliquée. Frontend de reprise : ${FRONTEND_FQDN}"
else
  [[ -n "$AFD_PROFILE" && -n "$AFD_ORIGIN_GROUP" ]] || fail "AFD_PROFILE et AFD_ORIGIN_GROUP requis pour la redirection."
  log "10/10  Bascule de l'origine Front Door vers ${FRONTEND_FQDN}."
  az afd origin update --profile-name "$AFD_PROFILE" -g "$PRIMARY_RG" \
    --origin-group-name "$AFD_ORIGIN_GROUP" --origin-name primary \
    --host-name "$FRONTEND_FQDN" --origin-host-header "$FRONTEND_FQDN"
fi

log "Service rétabli dans ${DR_LOCATION}. Consigner l'heure de bascule (mesure du RTO)."
