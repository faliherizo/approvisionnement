# Déployer la démonstration sur le VPS OVH

> Pour qui exploite la démonstration publique <https://contrat.vinaitsolution.com> : comment elle est installée, comment
> le pipeline la met à jour, et comment la remettre en état, la mesurer, l'éprouver et revenir en arrière.

## 1. Ce qui tourne, et où

Le VPS OVH (`deploy@51.222.86.37`, `mail.vinaitsolution.com`) héberge une douzaine d'applications derrière **un seul
Nginx**, `vina_nginx`, qui termine TLS pour tous les sous-domaines. La plateforme n'y publie aucun port.

```
Internet :443 ──▶ vina_nginx (TLS, certificat partagé vinaitsolution.com)
                     │  réseau Docker partagé « web »
                     ▼
                  contrat-frontend :8080  (nginx non privilégié : SPA, relais /api)
                     │  réseau privé « contractuelle_contrat_contrat-network »
                     ▼
                  contrat-backend :8080  (Spring Boot, Java 17) ──▶ contrat-db (PostgreSQL 15)
                                                                     volume contractuelle_contrat_contrat_pg
```

| Élément | Emplacement |
|---|---|
| Composition et scripts serveur | [`deploy/ovh/`](../deploy/ovh/) |
| Clone du dépôt sur le serveur | `~/projets/VINA-DOC/approvisionnement` (clé de déploiement `github-approvisionnement`) |
| Secrets de la démonstration | `deploy/ovh/.env.contrat` sur le serveur (mode 600, jamais versionné ; modèle : [`env.contrat.exemple`](../deploy/ovh/env.contrat.exemple)) |
| Version en service, version précédente | `deploy/ovh/.version-courante`, `.version-precedente` |
| Sauvegardes avant chaque déploiement | `~/sauvegardes/contrat/contrat-*.dump` (`pg_dump -Fc`, les 10 dernières) |
| Bloc Nginx | `~/vina-cms/nginx/prod.conf` ; copie de référence : [`deploy/ovh/nginx-contrat.conf`](../deploy/ovh/nginx-contrat.conf) |
| Installation d'origine (avant le 2026-11-23) | `~/contractuelle_contrat` (images `vinasolution/contrat-*:latest`), remplacée ; son `.env.prod` a servi à écrire `.env.contrat` |

Trois choix tiennent au partage du serveur :

- **Les images ne se construisent pas sur le VPS.** GitHub Actions les construit, les analyse (Trivy) et les charge par
  SSH (`docker save | ssh … docker load`) : ni mémoire ni processeur pris aux autres applications, aucun registre.
- **Le backend est désigné par son nom de conteneur**, `contrat-backend`. Sur le réseau `web`, un autre service
  s'appelle déjà `backend` (freelance-invoicing).
- **La mémoire est plafonnée** : 1,25 Go pour le serveur Java (tas à 70 %), 512 Mo pour PostgreSQL, 128 Mo pour nginx.

**Mode démonstration.** `APP_TENANTS_DEMO=true` sème l'organisation IQ et ses comptes publiés
(`backend/src/main/resources/data/tenants-demo.json`) ; l'organisation de Lévis et ses comptes sont ceux de la base.
L'antivirus fonctionne par signatures et le défi anti-robot est simulé : pour les rendre réels, voir les variables
facultatives de `env.contrat.exemple` (ADR-026, ADR-027).

## 2. Le pipeline

| Workflow | Déclenchement | Ce qu'il fait |
|---|---|---|
| **Déployer contrat (démonstration)**, `deployer-contrat.yml` | chaque poussée sur **`develop`**, ou à la main | secrets → tests du serveur et couverture → images analysées → livraison → `deployer.sh` → recette |
| **Recette e2e (environnement déployé)**, `recette-e2e.yml` | appelé par le déploiement, ou à la main | chaque compte publié ouvre chaque écran de son espace : aucune erreur JavaScript, aucune 5xx ; contrôle d'écran complet (WCAG 2.2 AA, 4 largeurs) pour l'administrateur. Lecture seule |
| **CI**, `ci.yml` | poussée sur `develop`/`main`, demande de fusion | `mvn verify` ; puis `infra/bacs-a-sable/ci.sh` sur pile éphémère : BDD de tous les appels d'offres, e2e par exigence, **couverture des fonctionnalités écran par écran**, tests d'intrusion |
| **Performance (JMeter)**, `perf.yml` | à la main | `perf/run.sh` et verdict SLO, sur pile éphémère (tous les plans) ou sur l'environnement (charge réduite) |
| **Tests d'intrusion**, `intrusion.yml` | hebdomadaire (pile éphémère), ou à la main | `securite/lancer.sh` : OpenAPI, OWASP Top 10, ZAP, Trivy. Contre l'environnement : case « autorisation » obligatoire, car la suite écrit et ZAP attaque |

La porte retenue est « dès la construction » : le déploiement attend ses propres portes (secrets, `mvn verify`,
images), pas la chaîne complète de `ci.yml`, plus longue. Une démonstration de `develop` peut donc montrer un écart que
la CI relève ensuite.

```bash
gh workflow run "Déployer contrat (démonstration)" --ref develop
gh run watch $(gh run list --workflow deployer-contrat.yml -L1 --json databaseId -q '.[0].databaseId')
gh workflow run perf.yml -f cible=environnement -f utilisateurs=5 -f boucles=3
gh workflow run intrusion.yml -f cible=ephemere
```

### Secrets et variables

Posés le 23 novembre 2026. Aucune valeur n'est dans le dépôt.

| Nom | Portée | Contenu |
|---|---|---|
| `DEPLOY_HOST`, `DEPLOY_USER` | dépôt | `51.222.86.37`, `deploy` |
| `DEPLOY_SSH_KEY` | dépôt | clé ed25519 privée **dédiée** (`github-actions-contrat`), dont la publique est dans `~deploy/.ssh/authorized_keys` |
| `DEPLOY_KNOWN_HOSTS` | dépôt | clés d'hôte du VPS, comparées à celles d'un poste qui le connaissait |
| `APP_URL` | environnement `contrat-demo` | `https://contrat.vinaitsolution.com` |
| `DEMO_PASSWORD` | environnement, facultatif | mot de passe des comptes publiés, s'il diffère de `tenants-demo.json` |
| `SUPERADMIN_EMAIL`, `SUPERADMIN_PASSWORD` | environnement, facultatif | pour `intrusion.yml` contre l'environnement seulement |

Changer la clé du pipeline : en générer une, ajouter la publique à `authorized_keys`, `gh secret set DEPLOY_SSH_KEY < cle`,
puis **retirer l'ancienne ligne** `github-actions-contrat`.

## 3. Ce que fait `deployer.sh`

1. sauvegarde la base (`pg_dump`, sans arrêt du serveur) ;
2. aligne le mot de passe de PostgreSQL sur `.env.contrat` (`POSTGRES_PASSWORD` n'agit qu'à la création du volume) ;
3. démarre la version : Liquibase applique ses migrations ;
4. attend que le serveur **et** le relais du frontend répondent `/api/health` (300 s au plus) ;
5. en cas d'échec : **restaure la base sauvegardée** (une migration appliquée ne se défait pas par un retour d'image),
   redémarre la version précédente, et fait échouer le job ;
6. ne garde que les images courante et précédente.

Avant la première mise en service (2026-11-23), les 34 migrations ajoutées depuis la version d'origine ont été jouées
sur une copie de la base de production restaurée dans PostgreSQL 15, puis le schéma a été validé entité par entité
(`ddl-auto=validate`) : aucun écart.

## 4. Exploiter

```bash
ssh deploy@51.222.86.37
cd ~/projets/VINA-DOC/approvisionnement/deploy/ovh
cat .version-courante                                   # la version en service (sha du commit)
docker ps --filter name=contrat-                        # état et santé
docker logs --tail 100 -f contrat-backend               # journal du serveur
docker stats --no-stream contrat-backend contrat-frontend contrat-db
./deployer.sh --retour-arriere                          # version précédente (images) — voir ci-dessous pour la base
```

**Restaurer une sauvegarde** (avec un retour arrière manuel, quand la version précédente ne lit pas le schéma migré) :

```bash
docker stop contrat-backend
docker exec -i contrat-db pg_restore -U contrat -d contrat --clean --if-exists --no-owner < ~/sauvegardes/contrat/<archive>.dump
./deployer.sh "$(cat .version-courante)"
```

## 4 bis. Sous-domaines des organisations

Chaque organisation a son adresse : `levis.contrat.vinaitsolution.com`, `iq.contrat.vinaitsolution.com`,
`v3r.contrat.vinaitsolution.com`. Le frontend déduit l'organisation du premier libellé de l'hôte, et le serveur refuse
la connexion d'un compte d'une autre organisation. Le domaine nu (`contrat.vinaitsolution.com`) porte l'accueil public,
dont « Accéder à l'application » ouvre le choix de l'organisation (`GET /api/tenants`), et la connexion du
superadministrateur. Comptes de démonstration : [comptes-demo.md](comptes-demo.md).

- **DNS** : enregistrement générique `*.contrat.vinaitsolution.com` → 51.222.86.37 (OVH).
- **Nginx central** : `*.contrat.vinaitsolution.com` figure au bloc `listen 80` (défi ACME, redirection HTTPS) et au
  bloc 443 du contrat (`deploy/ovh/nginx-contrat.conf`). Sauvegarde : `prod.conf.avant-sous-domaines-contrat`.
- **Certificat** : lignée propre `contrat.vinaitsolution.com` (webroot, ECDSA), renouvelée par `vina_certbot`. Un
  certificat générique demanderait un défi DNS ; la lignée énumère donc les sous-domaines. Ajouter une organisation :

  ```bash
  docker exec vina_certbot certbot certonly --webroot -w /var/www/certbot --cert-name contrat.vinaitsolution.com \
    --key-type ecdsa --expand -d contrat.vinaitsolution.com -d levis.contrat.vinaitsolution.com \
    -d iq.contrat.vinaitsolution.com -d v3r.contrat.vinaitsolution.com -d <nouveau>.contrat.vinaitsolution.com
  docker exec vina_nginx nginx -s reload
  ```

  Reprendre **tous** les noms existants : `--expand` remplace la liste du certificat par celle donnée.
- **CORS** : `APP_CORS_ALLOWED_ORIGINS=https://contrat.vinaitsolution.com,https://*.contrat.vinaitsolution.com`
  (`.env.contrat`).

## 5. Première installation (faite le 23 novembre 2026)

À refaire seulement pour réinstaller sur un autre serveur.

```bash
ssh deploy@51.222.86.37
cd ~/projets/VINA-DOC/approvisionnement && git fetch && git checkout develop
deploy/ovh/preparer-serveur.sh      # .env.contrat repris de ~/contractuelle_contrat/.env.prod, secrets par défaut renouvelés
```

`preparer-serveur.sh` reprend `APP_CRYPTO_KEY` et `APP_CRYPTO_SALT` (ils chiffrent les secrets M365 déjà en base) et
**renouvelle `JWT_SECRET` et `DB_PASSWORD`** quand ils gardent une valeur par défaut. C'était le cas de l'installation
d'origine : n'importe qui pouvait forger un jeton de session.

Nginx central : le bloc `contrat` relaie vers `contrat-frontend:8080` (l'ancienne image écoutait sur 80), désigné par
une variable pour qu'un frontend arrêté donne un 502 sur ce seul site. Éditer `prod.conf` **sans changer son inode**
(`nano`, `vi`), jamais avec `sed -i` : le fichier est monté seul dans `vina_nginx`, qui continuerait de lire l'ancien.
Puis `docker exec vina_nginx nginx -t && docker exec vina_nginx nginx -s reload`.

## 6. Dépannage

| Symptôme | Cause probable | Geste |
|---|---|---|
| 502 sur contrat | `contrat-frontend` arrêté ou en redémarrage | `docker ps --filter name=contrat-`, `deployer.sh $(cat .version-courante)` |
| « image absente » | livraison interrompue | relancer le workflow |
| « ne répond pas après 300 s » puis retour arrière | le serveur ne démarre pas (variable, migration) | le job affiche les 80 dernières lignes du journal |
| `password authentication failed` au démarrage | `.env.contrat` modifié à la main, base non alignée | `deployer.sh $(cat .version-courante)` (l'alignement est rejoué) |
| Un compte publié est refusé | mot de passe changé à l'écran | le remettre, ou poser `DEMO_PASSWORD` dans l'environnement |
| `git fetch` échoue sur le serveur | clé de déploiement `github-approvisionnement` retirée | `ssh -T git@github-approvisionnement` sur le serveur |
