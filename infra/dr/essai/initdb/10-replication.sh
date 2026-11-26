#!/bin/bash
# Compte de réplication de l'essai de relève : le mot de passe vient de l'environnement du conteneur (généré à chaque
# exécution par le workflow), jamais du dépôt.
set -euo pipefail
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v mdp="$REPLICATION_PASSWORD" <<'SQL'
CREATE ROLE replicateur WITH REPLICATION LOGIN PASSWORD :'mdp';
SQL
echo "host replication replicateur all scram-sha-256" >> "$PGDATA/pg_hba.conf"
