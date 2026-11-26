#!/usr/bin/env bash
# =============================================================================
# Bascule inter-régions Azure de la posture intégrée à infra/terraform/azure (ADR-039, SEC-11).
#   Canada Central (primaire) → Canada Est (relève), ou l'inverse en hébergement au Québec (option B).
#
#   ./bascule-azure.sh bascule    sinistre : promotion de la réplique, stockage, AKS de relève, déploiement, trafic
#   ./bascule-azure.sh exercice   essai annuel SANS toucher la production : géo-restauration vers un serveur temporaire,
#                                 déploiement dans un espace de noms d'essai, vérification, mesure, nettoyage
#   ./bascule-azure.sh retour     retour planifié vers la région primaire rétablie (bascule planifiée, sans perte)
#
# Chaque jalon est horodaté dans bascule-<horodatage>.csv : base du RTO consigné au rapport d'essai (document 20).
# La bascule réelle demande la confirmation « OUI » du responsable de la relève (rôle R2 du document 20).
#
# Prérequis : az (connecté), terraform (état distant lisible), kubectl, helm ; sorties de infra/terraform/azure.
# Aucune exécution sur un abonnement Azure n'a encore eu lieu : l'essai mesuré qui fait foi au dépôt est le workflow
# .github/workflows/releve.yml (pile conteneurisée) ; le premier exercice sur l'Azure d'IQ précède la mise en production.
# =============================================================================
set -Eeuo pipefail

MODE="${1:-}"
ICI="$(cd "$(dirname "$0")" && pwd)"
TF_DIR="$(cd "$ICI/../../terraform/azure" && pwd)"
CHART="$(cd "$ICI/../../k8s/chart" && pwd)"
VALEURS_AZURE="$ICI/../../k8s/values-azure.yaml"
JOURNAL="${JOURNAL:-$PWD/bascule-$(date -u +%Y%m%dT%H%M%SZ).csv}"
NOEUDS_MIN="${NOEUDS_MIN:-3}"
NOEUDS_MAX="${NOEUDS_MAX:-9}"
ESPACE_ESSAI="${ESPACE_ESSAI:-releve-exercice}"

jalon() { echo "$1,$(date -u +%s%3N)" >> "$JOURNAL"; printf '\n\033[1;36m[%s] %s\033[0m\n' "$(date -u +%H:%M:%S)" "$1"; }
echec() { printf '\n\033[1;31mÉCHEC : %s\033[0m\n' "$*" >&2; jalon "echec"; exit 1; }
sortie() { terraform -chdir="$TF_DIR" output -raw "$1"; }

lire_sorties() {
  terraform -chdir="$TF_DIR" init -input=false >/dev/null
  DR_RG=$(sortie dr_resource_group)
  DR_LOCATION=$(sortie dr_location)
  RG=$(sortie resource_group)
  REPLIQUE=$(sortie dr_postgres_replique || true)
  PG_ID=$(sortie postgres_server_id)
  AKS_DR=$(sortie dr_aks_name || true)
  KV_DR=$(sortie dr_key_vault_uri | sed -E 's#https://([^.]+)\..*#\1#')
  AFD=$(sortie frontdoor_profile)
  AFD_GROUPE=$(sortie frontdoor_origin_group)
  ACR=$(sortie acr_login_server)
  [ -n "$AKS_DR" ] || echec "aucune grappe de relève (dr_calcul = aucun) : appliquer d'abord dr_calcul = pilot_light"
}

confirmer() {
  read -r -p "Confirmer la BASCULE RÉELLE vers ${DR_LOCATION} (taper OUI) : " r
  [ "$r" = "OUI" ] || echec "bascule annulée"
}

verifier_coffre() {
  local s
  for s in db-url db-password clm-cle-maitresse clm-cle-maitresse-id; do
    az keyvault secret show --vault-name "$KV_DR" --name "$s" --query id -o tsv >/dev/null \
      || echec "secret $s absent du coffre de relève : la clé maîtresse ADR-037 est indispensable au déchiffrement"
  done
  jalon "coffre_releve_verifie"
}

agrandir_aks() {
  az aks update -g "$DR_RG" -n "$AKS_DR" --tier standard --only-show-errors >/dev/null
  az aks nodepool update -g "$DR_RG" --cluster-name "$AKS_DR" -n app --update-cluster-autoscaler \
    --min-count "$NOEUDS_MIN" --max-count "$NOEUDS_MAX" --only-show-errors >/dev/null
  az aks get-credentials -g "$DR_RG" -n "$AKS_DR" --overwrite-existing --only-show-errors
  jalon "aks_releve_agrandi"
}

deployer() {  # deployer <espace de noms> [valeurs supplémentaires...]
  local espace=$1
  shift
  helm upgrade --install contrat "$CHART" -n "$espace" --create-namespace -f "$VALEURS_AZURE" \
    --set image.registry="$ACR" "$@" --wait --timeout 20m
  jalon "application_deployee"
}

verifier_service() {  # verifier_service <url>
  local i
  for i in $(seq 1 60); do
    curl -fsS --max-time 10 "$1/api/health" >/dev/null 2>&1 && { jalon "service_retabli"; return 0; }
    sleep 10
  done
  echec "le service de relève ne répond pas sur $1 : NE PAS rediriger le trafic"
}

bascule() {
  jalon "declaration_sinistre"
  lire_sorties
  confirmer
  verifier_coffre
  if [ -n "$REPLIQUE" ]; then
    # Promotion forcée (région primaire injoignable) ; le point de terminaison virtuel d'écriture suit la réplique, la
    # chaîne de connexion des pods ne change pas.
    az postgres flexible-server replica promote -g "$DR_RG" -n "$REPLIQUE" \
      --promote-mode switchover --promote-option forced --yes --only-show-errors >/dev/null
    jalon "base_promue"
  else
    az postgres flexible-server geo-restore -g "$DR_RG" -n "$(basename "$PG_ID")-dr" --source-server "$PG_ID" \
      --location "$DR_LOCATION" --only-show-errors >/dev/null
    jalon "base_geo_restauree"
    echo "Mettre à jour le secret db-url du coffre de relève vers le serveur géo-restauré avant le déploiement."
  fi
  az storage account failover --name "$(sortie storage_account_name)" \
    --yes --only-show-errors >/dev/null || echo "Bascule du compte de stockage à confirmer (lecture RA-GZRS disponible)."
  jalon "stockage_bascule"
  agrandir_aks
  deployer contrat
  verifier_service "https://${ORIGINE_SECOURS:?ORIGINE_SECOURS, hôte d’entrée de la relève, requis}"
  # Front Door bascule seule vers l'origine de priorité 2 ; l'origine primaire est désactivée pour qu'un retour
  # partiel de la région primaire ne reprenne pas le trafic avant le retour planifié.
  az afd origin update -g "$RG" --profile-name "$AFD" --origin-group-name "$AFD_GROUPE" --origin-name aks-primaire \
    --enabled-state Disabled --only-show-errors >/dev/null || echo "Origine primaire injoignable : désactivation à refaire."
  jalon "trafic_redirige"
}

exercice() {
  jalon "debut_exercice"
  lire_sorties
  verifier_coffre
  local temporaire
  temporaire="$(basename "$PG_ID")-exercice-$(date -u +%m%d)"
  az postgres flexible-server geo-restore -g "$DR_RG" -n "$temporaire" --source-server "$PG_ID" \
    --location "$DR_LOCATION" --only-show-errors >/dev/null
  jalon "base_geo_restauree"
  agrandir_aks
  echo "Déployer dans l'espace ${ESPACE_ESSAI} avec la chaîne de connexion de ${temporaire}, puis exécuter les contrôles"
  echo "du document 20 (§ 6) : têtes de chaîne, empreintes, déchiffrement, parcours principal."
  deployer "$ESPACE_ESSAI" --set ingress.host="exercice.${ORIGINE_SECOURS:?ORIGINE_SECOURS requis}"
  verifier_service "https://exercice.${ORIGINE_SECOURS}"
  jalon "fin_exercice"
  echo "Nettoyage : helm uninstall contrat -n ${ESPACE_ESSAI} ; az postgres flexible-server delete -g ${DR_RG} -n ${temporaire} --yes ;"
  echo "remettre le pool applicatif de relève à zéro (az aks nodepool update ... --min-count 0)."
}

retour() {
  jalon "debut_retour"
  lire_sorties
  echo "1. Recréer la réplique dans la région primaire depuis le serveur promu (terraform apply, dr_location inversée)."
  echo "2. Attendre le retard de réplication nul, annoncer la fenêtre de maintenance (document 05, § 6)."
  read -r -p "Réplique primaire synchronisée et fenêtre ouverte ? (taper OUI) : " r
  [ "$r" = "OUI" ] || echec "retour annulé"
  az postgres flexible-server replica promote -g "$RG" -n "$(basename "$PG_ID")" \
    --promote-mode switchover --promote-option planned --yes --only-show-errors >/dev/null
  jalon "base_rebasculee"
  az afd origin update -g "$RG" --profile-name "$AFD" --origin-group-name "$AFD_GROUPE" --origin-name aks-primaire \
    --enabled-state Enabled --only-show-errors >/dev/null
  jalon "trafic_rendu_au_primaire"
  echo "3. Ramener le pool applicatif de relève à zéro et le plan de contrôle en Free (pilot light)."
}

case "$MODE" in
  bascule) bascule ;;
  exercice) exercice ;;
  retour) retour ;;
  *) echo "Usage : $0 bascule|exercice|retour" >&2; exit 2 ;;
esac
echo "Chronologie : $JOURNAL"
