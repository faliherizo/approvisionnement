#!/usr/bin/env bash
# =============================================================================
# Orchestration de la bascule après sinistre (Canada Central -> Canada Est).
# Reproduit la séquence : détection -> Terraform DR -> réseau/sécurité -> ECS/Fargate
#   -> restauration PostgreSQL -> S3 -> image Docker -> tests de santé -> redirection
#   -> service rétabli. Objectifs : RTO <= 4 h, RPO <= 15 min.
#
# Prérequis : le stack infra/dr/aws/replication est appliqué en continu (réplication
# S3 + sauvegardes RDS + images ECR). Ce script instancie infra/dr/aws/failover.
#
# Usage :
#   ./dr-failover.sh          # bascule réelle (demande confirmation)
#   ./dr-failover.sh --drill  # exercice (idem, mais laisse la production intacte)
# =============================================================================
set -Eeuo pipefail

DR_REGION="${DR_REGION:-ca-west-1}"
PRIMARY_REGION="${PRIMARY_REGION:-ca-central-1}"
PROJECT="${PROJECT:-vina-contrat}"
ENVIRONMENT="${ENVIRONMENT:-prod}"
HOSTED_ZONE_ID="${HOSTED_ZONE_ID:-}"          # zone Route 53 publique (redirection du trafic)
APP_FQDN="${APP_FQDN:-contrat.vinaitsolution.com}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
REPL_DIR="$(cd "$(dirname "$0")/../aws/replication" && pwd)"
FAILOVER_DIR="$(cd "$(dirname "$0")/../aws/failover" && pwd)"
DRILL=false
[[ "${1:-}" == "--drill" ]] && DRILL=true

log()  { printf '\n\033[1;36m[%(%H:%M:%S)T] %s\033[0m\n' -1 "$*"; }
fail() { printf '\n\033[1;31mÉCHEC : %s\033[0m\n' "$*" >&2; exit 1; }

confirm() {
  $DRILL && return 0
  read -r -p "Confirmer la BASCULE RÉELLE vers ${DR_REGION} ? (taper OUI) : " a
  [[ "$a" == "OUI" ]] || fail "Bascule annulée."
}

# 1. Détection de l'incident (confirmation opérateur / alerte).
log "1/10  Détection de l'incident sur la région primaire ${PRIMARY_REGION}."
confirm

# 2. Terraform région DR : récupérer les sorties de la réplication.
log "2/10  Lecture des sorties du stack de réplication."
pushd "$REPL_DIR" >/dev/null
terraform init -input=false >/dev/null
BACKUPS_ARN="$(terraform output -raw rds_replicated_backups_arn)"
KMS_ARN="$(terraform output -raw dr_kms_key_arn)"
popd >/dev/null
[[ -n "$BACKUPS_ARN" && -n "$KMS_ARN" ]] || fail "Sorties de réplication introuvables."

# Récupérer les images ECR répliquées et les secrets de la source.
ACCT="$(aws sts get-caller-identity --query Account --output text)"
REG="${ACCT}.dkr.ecr.${DR_REGION}.amazonaws.com"
BACKEND_IMAGE="${REG}/${PROJECT}-${ENVIRONMENT}-backend:latest"
FRONTEND_IMAGE="${REG}/${PROJECT}-${ENVIRONMENT}-frontend:latest"
DB_PASSWORD="$(aws secretsmanager get-secret-value --region "$PRIMARY_REGION" \
  --secret-id "${PROJECT}-${ENVIRONMENT}-db" --query SecretString --output text 2>/dev/null \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["password"])' || echo "")"
JWT_SECRET="$(aws secretsmanager get-secret-value --region "$PRIMARY_REGION" \
  --secret-id "${PROJECT}-${ENVIRONMENT}-app" --query SecretString --output text 2>/dev/null \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["JWT_SECRET"])' || echo "")"

# 3-8. Terraform apply : réseau + sécurité + ECS/Fargate + restauration RDS (PITR) + S3 + image Docker.
log "3-8/10  Reconstruction de l'infrastructure de reprise (réseau, sécurité, ECS, RDS PITR, S3, image)."
pushd "$FAILOVER_DIR" >/dev/null
terraform init -input=false >/dev/null
terraform apply -input=false -auto-approve \
  -var "dr_region=${DR_REGION}" \
  -var "source_automated_backups_arn=${BACKUPS_ARN}" \
  -var "dr_kms_key_arn=${KMS_ARN}" \
  -var "backend_image=${BACKEND_IMAGE}" \
  -var "frontend_image=${FRONTEND_IMAGE}" \
  -var "db_password=${DB_PASSWORD}" \
  -var "jwt_secret=${JWT_SECRET}"
ALB_DNS="$(terraform output -raw alb_dns_name)"
popd >/dev/null
[[ -n "$ALB_DNS" ]] || fail "ALB de reprise indisponible."

# 9. Tests de santé avant redirection du trafic.
log "9/10  Vérification de santé sur http://${ALB_DNS}${HEALTH_PATH}"
for i in $(seq 1 30); do
  if curl -fsS --max-time 10 "http://${ALB_DNS}${HEALTH_PATH}" >/dev/null 2>&1; then
    log "Santé OK (tentative ${i})."; HEALTHY=1; break
  fi
  sleep 20
done
[[ "${HEALTHY:-}" == "1" ]] || fail "Le service de reprise ne répond pas — NE PAS rediriger le trafic."

# 10. Redirection du trafic (Route 53) puis service rétabli.
if $DRILL; then
  log "10/10  [EXERCICE] Redirection NON appliquée. ALB de reprise : ${ALB_DNS}"
else
  [[ -n "$HOSTED_ZONE_ID" ]] || fail "HOSTED_ZONE_ID requis pour la redirection."
  log "10/10  Redirection de ${APP_FQDN} vers l'ALB de reprise."
  aws route53 change-resource-record-sets --hosted-zone-id "$HOSTED_ZONE_ID" \
    --change-batch "$(cat <<JSON
{"Changes":[{"Action":"UPSERT","ResourceRecordSet":{
  "Name":"${APP_FQDN}","Type":"CNAME","TTL":60,
  "ResourceRecords":[{"Value":"${ALB_DNS}"}]}}]}
JSON
)"
fi

log "Service rétabli dans ${DR_REGION}. Consigner l'heure de bascule (mesure du RTO)."
