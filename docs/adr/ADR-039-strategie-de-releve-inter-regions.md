# ADR-039 — Stratégie de relève inter-régions : réplique PostgreSQL promue et région de relève « pilot light »

- **Statut** : Accepté
- **Date** : 2026-11-26
- **Portée** :
  - infrastructure : `infra/terraform/azure` (`dr.tf`, `postgres.tf`, `storage.tf`, `registry.tf`, `keyvault.tf`,
    `frontdoor.tf`, variables et sorties `dr_*`) ;
  - exploitation : `infra/dr/scripts/bascule-azure.sh` (bascule, exercice, retour), `sauvegarder-cles-azure.sh` ;
  - essai : `.github/workflows/releve.yml`, `infra/dr/essai/` (essai de relève mesuré, deux scénarios).
- **Origine** : Annexe 10 d'IQ, SEC-11 (Obligatoire) : mécanisme de relève inter-régions et résultats d'essai ;
  SEC-20 à SEC-23. Complète ADR-020 (régions canadiennes) et ADR-037 (clé dédiée par organisation).

## Contexte

- **Niveaux annoncés** (document 05) : RTO ≤ 8 h, RPO ≤ 4 h, en heures écoulées ; objectifs de conception RTO ≤ 4 h,
  RPO ≤ 15 min.
- **Existant** : haute disponibilité inter-zones dans la région primaire (AKS sur trois zones, PostgreSQL en veille
  synchrone, stockage ZRS) ; sauvegardes géo-redondantes PostgreSQL sur 14 jours ; posture de relève dans des piles
  séparées (`infra/dr/azure/replication` et `failover`) : réplication d'objets, registre dédié, géo-restauration et
  Azure Container Apps créés à la bascule.
- **Défauts relevés dans l'existant** :
  - la géo-restauration de PostgreSQL Flexible Server repart de la dernière sauvegarde copiée dans la région jumelle :
    Microsoft annonce un RPO pouvant atteindre une heure, incompatible avec l'objectif de 15 minutes ;
  - la clé maîtresse du chiffrement par organisation (`CLM_CLE_MAITRESSE`, ADR-037) n'existait dans aucun coffre de
    relève : une bascule aurait restauré des documents chiffrés impossibles à ouvrir ;
  - le script de bascule lisait le mot de passe de la base dans le coffre primaire, celui de la région perdue ;
  - Front Door n'avait aucune origine ni route, et sondait `/actuator/health`, que le serveur n'expose pas ;
  - la relève sur Container Apps exécutait l'application sur une autre plateforme que la production : écart de
    configuration non exercé.
- **Contraintes** : résidence au Canada (Canada Central et Canada Est seulement) ; Canada Est n'a pas de zones de
  disponibilité ; coût de la région de relève proportionné ; aucune donnée d'IQ hors des deux régions ; destruction
  au terme du calendrier de conservation (ADR-012, Loi 25).

## Options envisagées

### Base de données

1. **Géo-restauration seule** (sauvegardes géo-redondantes) : aucun coût permanent de calcul ; RPO jusqu'à 1 h, RTO
   allongé par la restauration (taille de la base), aucune vérification continue de la copie.
2. **Réplique de lecture inter-régions, promue à la bascule** (retenue) : réplication asynchrone en continu, RPO de
   l'ordre de quelques secondes (retard de réplication surveillé), promotion en quelques minutes ; le point de
   terminaison virtuel d'écriture garde le même nom d'hôte avant et après promotion. Coût permanent d'un serveur sans
   haute disponibilité dans la région de relève.
3. **Réplication logique ou outil tiers** (pglogical, Debezium) : schéma à maintenir des deux côtés, séquences et
   objets volumineux non répliqués nativement, exploitation plus lourde ; aucun gain sur l'option 2.

### Calcul

1. **Aucun calcul en régime normal**, tout créé à la bascule (existant, Container Apps) : coût minimal ; création
   d'AKS ou d'un environnement Container Apps, des identités et du réseau pendant l'incident (30 à 60 min), plateforme
   différente de la production.
2. **« Pilot light »** (retenue par défaut) : grappe AKS de relève déjà créée, plan de contrôle gratuit, un nœud
   système, pool applicatif à zéro ; la bascule passe le plan de contrôle en Standard, agrandit le pool et déploie le
   même chart Helm que la production.
3. **Veille active** : grappe dimensionnée comme la production et application déployée, prête à servir ; bascule la
   plus rapide, coût proche d'une seconde production.

### Stockage et secrets

1. Réplication d'objets vers un second compte (existant) : asynchrone, configuration en deux comptes, versionnage
   exigé des deux côtés.
2. **RA-GZRS** (retenue) : trois zones dans la région primaire et copie gérée par Microsoft dans la région jumelle,
   lisible sans attendre la bascule du compte ; versionnage, suppression réversible et restauration à un instant
   donné sur le compte primaire.
3. Immuabilité (WORM) au niveau des versions : protège contre l'effacement malveillant, mais empêche la destruction au
   terme du calendrier de conservation et exclut la restauration à un instant donné des blobs.

## Décision

- **Base** : réplique de lecture dans la région de relève (`dr_strategie_base = "replique"`), point de terminaison
  virtuel d'écriture (`azurerm_postgresql_flexible_server_virtual_endpoint`) utilisé par la chaîne de connexion des
  pods ; sauvegardes géo-redondantes conservées **35 jours** (maximum du service) comme voie de secours, et seule
  défense contre une corruption logique que la réplique recopierait. Alerte si le retard de réplication dépasse
  300 s.
- **Calcul** : pilot light par défaut (`dr_calcul = "pilot_light"`), veille active en option selon l'appétit de
  risque d'IQ. Même plateforme (AKS), même chart, même image : le registre Premium est géo-répliqué dans la région de
  relève, sous le même nom de connexion.
- **Stockage** : RA-GZRS (RA-GRS si la région primaire n'a pas de zones), versionnage, flux de modifications,
  suppression réversible des blobs et des conteneurs (30 jours), restauration à un instant donné (29 jours).
  **Immuabilité désactivée par défaut** (`stockage_immutabilite_jours = 0`) : l'inaltérabilité des preuves est
  démontrée par le journal scellé et les empreintes SHA-256, et la destruction au terme doit rester possible ; elle
  s'active par variable si IQ l'exige pour une catégorie de documents.
- **Secrets et clés** : coffre de relève dans la région de relève, protection contre la purge, rétention de
  suppression 90 jours ; Terraform y copie `db-url`, `db-password`, `clm-cle-maitresse` et son identifiant ; la clé
  maîtresse est générée une fois (`random_bytes`, `prevent_destroy`) et sa valeur est la même dans les deux coffres.
  Les clés non exportables du profil `azure-kms` sont copiées par sauvegarde et restauration Key Vault
  (`sauvegarder-cles-azure.sh`), après chaque rotation et avant chaque exercice.
- **Entrée** : Front Door actif-passif, origine primaire de priorité 1, origine de relève de priorité 2, sonde sur
  `/api/health`, nom d'hôte public et certificat inchangés à la bascule.
- **Bascule et retour** : `bascule-azure.sh` (promotion forcée par permutation, bascule du compte de stockage,
  agrandissement d'AKS, déploiement, vérification, désactivation de l'origine primaire) ; retour planifié par
  permutation planifiée, sans perte ; exercice annuel sans toucher la production (géo-restauration vers un serveur
  temporaire et déploiement dans un espace de noms d'essai).
- **Le groupe de ressources de relève est dans la région de relève**, pour rester administrable si la région
  primaire est perdue. Les piles séparées `infra/dr/azure` restent la référence AWS et une variante ; la production de
  l'AO suit la posture intégrée.

## Conséquences

- **Positives** :
  - RPO de conception de quelques secondes (retard de réplication), mesuré à 0 s dans l'essai conteneurisé, bien
    sous l'objectif de 15 minutes ; la voie de secours (sauvegardes) reste sous le niveau annoncé de 4 h ;
  - la clé maîtresse ADR-037 survit à la perte de la région primaire : les documents restaurés se déchiffrent ;
  - la plateforme de relève est celle de la production, vérifiée en continu (grappe existante, image répliquée) ;
  - aucun secret ni chaîne de connexion ne change à la bascule.
- **Négatives** :
  - coût permanent de la réplique (un serveur GP_Standard_D4s_v3 sans haute disponibilité), du nœud système de
    relève, de la géo-réplique du registre et du supplément RA-GZRS : ordre de grandeur **7 000 à 9 000 $ par an**
    aux prix de liste publics, contre environ 11 000 $ de plus pour une veille active ; à confirmer par la
    soumission ferme de Microsoft ;
  - la promotion de la réplique est une décision humaine (pas de bascule automatique de la base) : elle évite une
    double écriture si la région primaire n'est que partiellement atteinte, au prix de quelques minutes de décision ;
  - la réplique recopie aussi une corruption logique : la restauration à un instant donné depuis les sauvegardes
    reste nécessaire, et elle est exercée (scénario « sauvegarde » de l'essai) ;
  - Canada Est n'a pas de zones : pendant une relève, la base et la grappe ne sont plus redondées entre zones ;
  - la valeur de la clé maîtresse vit dans l'état Terraform, qui doit être stocké chiffré, à accès restreint, dans un
    compte de stockage lui-même géo-redondant.
- **Vérification** :
  - `terraform fmt -check`, `terraform validate` (sans état distant), tflint (règles azurerm) et Checkov dans le job
    « infrastructure » du workflow `releve.yml` ;
  - essai de relève mesuré (`releve.yml`) : perte totale du site primaire, reprise sur un site neuf, contrôles des
    têtes de chaîne du journal scellé, des empreintes, du déchiffrement par la clé de l'organisation, du parcours
    principal ; RTO et RPO mesurés, porte bloquante sur les niveaux annoncés et les objectifs ;
  - tests de l'outil de mesure (`infra/dr/essai/test_rapport_releve.py`).
- **Reste à faire** : premier exercice sur l'abonnement Azure d'IQ avant la mise en production (`bascule-azure.sh
  exercice`, puis bascule et retour planifiés en fenêtre de maintenance) ; essai de charge de la région de relève
  (SEC-23) ; sonde de readiness du chart sur `/api/health` (le chart sonde aujourd'hui `/actuator/health/readiness`,
  absent du serveur).
