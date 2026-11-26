#!/usr/bin/env bash
# =============================================================================
# Copie des CLÉS du coffre primaire vers le coffre de relève (ADR-037, ADR-039, SEC-11).
#
# Les secrets (dont CLM_CLE_MAITRESSE) sont copiés par Terraform (dr.tf). Les clés non exportables — clé RSA-HSM du
# profil azure-kms qui enveloppe les clés de données des organisations — ne peuvent l'être que par sauvegarde et
# restauration Key Vault, possibles seulement entre coffres de la même géographie (Canada) et du même abonnement.
# Sans cette copie, une bascule sous le profil azure-kms rendrait illisibles tous les documents chiffrés.
#
# À exécuter après chaque création ou rotation de clé, et avant chaque exercice de relève (document 20, § 7).
# Les blobs de sauvegarde, chiffrés par Key Vault, ne transitent que par un dossier temporaire supprimé à la sortie.
#
#   KV_PRIMAIRE=<coffre> KV_RELEVE=<coffre> ./sauvegarder-cles-azure.sh
# =============================================================================
set -Eeuo pipefail
: "${KV_PRIMAIRE:?KV_PRIMAIRE, nom du coffre primaire, requis}"
: "${KV_RELEVE:?KV_RELEVE, nom du coffre de relève, requis}"

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

cles=$(az keyvault key list --vault-name "$KV_PRIMAIRE" --query '[].name' -o tsv)
[ -n "$cles" ] || { echo "Aucune clé dans $KV_PRIMAIRE (profil local : la clé maîtresse est un secret, copié par Terraform)."; exit 0; }

for cle in $cles; do
  az keyvault key backup --vault-name "$KV_PRIMAIRE" --name "$cle" --file "$tmp/$cle.bak" --only-show-errors
  if az keyvault key show --vault-name "$KV_RELEVE" --name "$cle" >/dev/null 2>&1; then
    echo "Clé $cle déjà présente dans $KV_RELEVE : vérifier que ses versions couvrent celles de $KV_PRIMAIRE."
    continue
  fi
  az keyvault key restore --vault-name "$KV_RELEVE" --file "$tmp/$cle.bak" --only-show-errors >/dev/null
  echo "Clé $cle restaurée dans $KV_RELEVE."
done
