# Essai de 500 utilisateurs concurrents — TI-12 (IQ, AO 2026-6370-0005)

> **Exigence TI-12 (Essentielle)** : « La Solution doit supporter minimalement 500 utilisateurs concurrents sans
> dégradation significative des niveaux de service convenus. »

**Résultat : 500 utilisateurs actifs simultanément pendant un palier de 599 s, 149 196 requêtes, 0,011 % d'erreur,
P95 de 31 ms au suivi et de 7 à 13 ms aux autres requêtes. Porte SLO respectée.** Exécution de référence :
<https://github.com/faliherizo/approvisionnement/actions/runs/45896141636> (commit `71224cf`, 2026-11-26), rapport HTML
JMeter et relevés dans l'artefact `perf-cible-45896141636`. Confirmé par un second run vert sur le même code
(<https://github.com/faliherizo/approvisionnement/actions/runs/45898471476>, § 3.1).

L'essai a eu lieu sur une pile de production conteneurisée, aux ressources bornées comme la cible (voir § 2), dans un
exécuteur GitHub Actions. Ce n'est pas l'infrastructure Azure d'IQ : la recette sur cette infrastructure reste à refaire
avant la mise en production (§ 6).

## 1. Méthode

Workflow [`.github/workflows/perf-cible.yml`](../../.github/workflows/perf-cible.yml), déclenché à la main
(`workflow_dispatch`) ou par une poussée sur une branche `perf/**`. Chaque exécution refait tout, sans état antérieur :

1. **Images** : l'image du serveur est construite avec `backend/Dockerfile`, le même fichier et le même cache que le
   déploiement (`deployer-contrat.yml`).
2. **Pile** : PostgreSQL 16 et le serveur (profil `prod` : Liquibase, PostgreSQL, contrôles de démarrage
   `ExigencesProduction`), chacun dans un conteneur borné en UC et en mémoire (`docker run --cpus --memory`, sans
   échange). Les mots de passe, la clé JWT, la clé de chiffrement et la clé de scellement sont tirés au hasard à chaque
   exécution (`openssl rand`) et masqués dans le journal ; rien n'est versionné.
3. **Données** : organisation IQ de démonstration amorcée par l'application ; **500 comptes d'essai** créés par l'API
   d'administration (`perf/outils/amorcer-comptes.sh`, rôle d'initiateur, mot de passe généré), un par utilisateur
   virtuel ; trousses tirées de `perf/data/clm-trousses.csv`. **Préchargement** de 1 000 trousses complètes (50 fils,
   20 boucles) pour ne pas mesurer une base vide.
4. **Charge** : JMeter 5.6.3, plan `perf/clm-trousses-load.jmx`, profil `perf/profils/cible.env` : 500 utilisateurs,
   **montée progressive de 300 s**, puis **palier soutenu** jusqu'à 900 s ; chaque utilisateur se connecte une fois puis
   enchaîne le cycle documentaire (création de trousse, dépôt de document, signataire, parcours, lancement, détail,
   suivi centralisé) avec **2 s de réflexion** entre deux gestes.
5. **Porte** : `perf/run.sh` applique les SLO de [`perf/SLI-SLO-SLA.md`](../../perf/SLI-SLO-SLA.md) et sort en erreur
   s'il en manque un (le job échoue) ; le plan applique aussi les plafonds du SLA par requête (3 s en lecture, 5 s en
   écriture). `perf/outils/synthese-essai.py` échoue à son tour si JMeter n'a jamais eu les 500 utilisateurs actifs en
   même temps.
6. **Relevés** : `docker stats` toutes les 5 s (UC, mémoire des conteneurs), charge de l'hôte, `pg_stat_statements`
   (requêtes SQL les plus coûteuses), `pg_stat_user_tables` (lectures complètes), journal du serveur, limites effectives
   des conteneurs ; résumé Markdown dans le résumé du job et en artefact.

Aucune charge ne quitte l'exécuteur : la cible est `localhost` (garde de `run.sh` : tout autre hôte exige
`CIBLE_AUTORISEE`).

## 2. Environnement et dimensionnement

| Élément | Essai | Cible (source) |
|---|---|---|
| Serveur | **1 réplique**, 2 vCPU, 2 Gio | 3 répliques au moins, limites 2 vCPU / 2 Gio par pod (`infra/k8s/chart/values.yaml` : `backend.replicas`, `backend.resources.limits`) |
| JVM | `-XX:MaxRAMPercentage=70` (tas ≈ 1,4 Gio) | même réglage, ajouté au chart (`templates/backend.yaml`) et déjà au déploiement OVH |
| Base | PostgreSQL 16, 1,5 vCPU, 4 Gio | Azure Database for PostgreSQL 16, serveur flexible `GP_Standard_D4s_v3` : 4 vCores, 16 Gio (`infra/terraform/azure/variables.tf`, `terraform.tfvars.example` ; document 01 de la réponse) |
| Pool de connexions | 30 (`DB_POOL_MAX` par défaut) | idem, par réplique |
| Entrée | HTTP direct au serveur | Front Door / entrée AKS, TLS terminé avant le pod (`/api` routé au service du serveur) |
| Hôte | exécuteur GitHub `ubuntu-24.04` : 4 vCPU, 16 Go, partagé par JMeter, Docker, le serveur et la base | nœuds `Standard_D4s_v5` dédiés au pool applicatif (`infra/terraform/azure/aks.tf`) |

Ce que l'essai représente : **les 500 utilisateurs sont portés par une seule réplique**, alors que la cible en exécute
au moins trois, et par une base qui dispose de 38 % des vCores et de 25 % de la mémoire de la cible. L'essai est donc
**plus sévère que la cible** pour le serveur comme pour la base. Il est en revanche moins représentatif pour le réseau
(aucun aller-retour Internet, aucun TLS, aucune passerelle) et pour la base gérée (pas de veille synchrone, stockage
local de l'exécuteur).

Écarts de configuration, déclarés : `COOKIE_SECURE=false` et `APP_HTTP_LOCAL_AUTORISE=true` (HTTP en clair dans
l'exécuteur ; en production, TLS est terminé à l'entrée, le pod ne le voit pas) ; `APP_TENANTS_DEMO=true` (organisation
IQ et administrateur de démonstration, pour créer les comptes d'essai) ; connexion locale par mot de passe (IQ se
connecte par Entra ID ; une connexion locale coûte un hachage BCrypt, plus cher qu'une validation de jeton OIDC) ;
conversion, antivirus, courriel et Salesforce en mode simulé ou par signatures (aucun appel sortant).

## 3. Résultats du run vert (45896141636)

Palier : 599 s à 500 utilisateurs actifs (maximum relevé par JMeter : 500).

| Requête | Nombre | Débit (req/s) | Erreurs | P50 (ms) | P95 (ms) | P99 (ms) | Max (ms) |
|---|---:|---:|---:|---:|---:|---:|---:|
| `GET /api/clm/suivi` (suivi centralisé, 50 lignes) | 21 310 | 35,6 | 0 | 14 | 31 | 50 | 185 |
| `GET /api/clm/trousses/{id}` | 21 306 | 35,6 | 0 | 2 | 7 | 15 | 81 |
| `POST /api/auth/login` | 4 | — | 0 | 73 | 75 | 75 | 75 |
| `POST /api/clm/trousses` | 21 331 | 35,6 | 0 | 3 | 10 | 24 | 153 |
| `POST /api/clm/trousses/{id}/documents` | 21 326 | 35,6 | 16 (0,08 %) | 3 | 9 | 17 | 80 |
| `POST /api/clm/trousses/{id}/lancer` | 21 298 | 35,6 | 0 | 5 | 13 | 27 | 94 |
| `POST /api/clm/trousses/{id}/participants` | 21 311 | 35,6 | 0 | 3 | 9 | 18 | 81 |
| `PUT /api/clm/trousses/{id}/parcours` | 21 310 | 35,6 | 0 | 3 | 8 | 17 | 87 |
| **Total au palier** | **149 196** | **249,2** | **16 (0,011 %)** | **3** | **18** | **31** | **185** |

Les connexions ont lieu pendant la montée (une par utilisateur) : 500 connexions, P95 71 ms, 0 erreur.

**Porte SLO (essai entier, montée comprise ; échantillons JMeter, transactions comprises)** :

| SLO | Objectif | Mesure |
|---|---|---|
| Taux d'erreur | < 0,1 % | **0,011 %** (40 / 374 213) |
| Lecture P95 / P99 | ≤ 800 / 1 500 ms | **25 / 65 ms** |
| Écriture P95 / P99 | ≤ 1 500 / 2 500 ms | **9 / 19 ms** |
| Plafond SLA par requête | 3 000 ms lecture, 5 000 ms écriture | maximum 185 ms |

**Ressources (essai entier)** : serveur 105 % d'UC en moyenne, 159 % au P95, 177 % au maximum (sur 200 %), mémoire
0,99 Gio au plus sur 2 Gio, aucun arrêt (OOM) ; base 46 % en moyenne, 84 % au P95 (sur 150 %), 0,56 Gio. Le serveur
garde de la marge, la base nettement : la réplique unique encaisse les 500 utilisateurs, la cible en aligne trois.

**Erreurs restantes** : 20 refus 400 au dépôt de document (0,07 % des dépôts), répartis sur tout l'essai, réponses en
quelques millisecondes. Le run de confirmation, qui consigne le corps des réponses en échec (`echecs.xml`), en donne la
cause : « Requête de fichier invalide : envoyez le fichier en multipart/form-data » — le corps multipart reçu ne se lit
pas (`MultipartException`). L'origine, client JMeter ou serveur, reste à établir (journal du gestionnaire en `DEBUG`) ;
elle n'affecte pas la porte (0,1 %).

### 3.1 Run de confirmation (45898471476)

Même code, même profil, autre exécuteur : palier de 599 s à 500 utilisateurs actifs, 144 383 requêtes, 241 req/s,
0,017 % d'erreur (25 refus 400 au dépôt, même cause) ; porte SLO : lecture P95 308 ms / P99 654 ms, écriture P95
258 ms / P99 617 ms, maximum 2,5 s ; **SLO respectés**. Les temps sont plus élevés que le run de référence parce que
l'exécuteur l'était aussi : charge moyenne de l'hôte 6,5 (P95 12,6) pour 4 vCPU, contre 2,0 (P95 3,9). Le serveur
(108 % d'UC en moyenne, 1,1 Gio) et la base (63 %) n'étaient pas saturés : l'écart vient de l'hôte partagé, où JMeter
tourne aussi. C'est la principale limite de cet environnement.

Intensité : 249 requêtes par seconde au palier, soit un geste toutes les 2 s par utilisateur. C'est bien au-delà de
l'usage attendu d'IQ (55 000 à 65 000 documents par an, addenda 2, Q4) : la mesure porte sur la tenue à 500 utilisateurs
actifs en même temps, non sur un débit moyen.

## 4. Défauts trouvés et corrigés en cours d'essai

L'essai a échoué trois fois avant le run vert. Chaque échec a été expliqué par les relevés et corrigé dans le code, sans
toucher aux SLO ni au profil de charge.

| Run | Palier (req/s) | Erreurs | Lecture P95 | Écriture P95 | Cause établie | Correction |
|---|---:|---:|---:|---:|---|---|
| [45886665629](https://github.com/faliherizo/approvisionnement/actions/runs/45886665629) | 20 | 16,6 % | 165 s | 30 s | Suivi d'un initiateur : toutes les trousses de l'organisation relues par tranches de 200, visibilité vérifiée trousse par trousse (9,8 millions de lectures d'étapes et autant de participants) ; base saturée. Titres de trousse refusés (422) par la garde anti-cartes : numéro de fil + horodatage passaient le contrôle de Luhn | `PerimetreVisible` : présélection en base des trousses visibles, décision exacte inchangée (`LectureTrousse.visible`) ; titres du plan sans longue suite de chiffres |
| [45891095467](https://github.com/faliherizo/approvisionnement/actions/runs/45891095467) | 164 | 4,8 % | 10,8 s | 2,0 s | Lecture complète de `clm_invitation` à chaque lancement (invitations d'une étape sans index) et de `clm_participant` à chaque suivi ; pool de connexions épuisé (30 actives, 170 en attente) | Changelog 085 : index `(tenant_id, etape_id)` sur les invitations, index d'expression sur `lower(courriel)` |
| [45893657839](https://github.com/faliherizo/approvisionnement/actions/runs/45893657839) | 227 | 0,08 % | 1,7 s | 0,8 s | Suivi encore à 172 ms en moyenne dans la base : `lower()` calculé sur chaque trousse et chaque étape à chaque page | Propriétaire par sous-requête indexée (`lower(proprietaire)`) ; groupes et rôles comparés à l'identique, minuscules réservées aux affectations nominatives |
| **[45896141636](https://github.com/faliherizo/approvisionnement/actions/runs/45896141636)** | **249** | **0,011 %** | **25 ms** | **9 ms** | — | — |

Défauts du harnais corrigés au passage :

- **Porte d'erreurs aveugle** : `run.sh` découpait le JTL sur la virgule ; un message d'échec contenant une virgule
  décalait les colonnes et l'échec comptait comme un succès. Lecture CSV véritable désormais.
- **Simultanéité jamais atteinte** : le profil (3 boucles) et l'essai Q38 du 2026-11-17 (500 utilisateurs montés en
  60 s, une boucle) ne mettaient jamais 500 utilisateurs actifs en même temps — au plus **14** pour Q38, d'après la
  colonne `allThreads` de son JTL. Le plan borne désormais une durée (palier soutenu) et la synthèse échoue si la
  simultanéité visée n'est pas atteinte.
- **Préchargement inopérant** : une durée nulle arrêtait les fils aussitôt ; `run.sh` laisse désormais les boucles
  décider de la fin.

## 5. Reproduire

```bash
git push origin HEAD:perf/ti-12                           # ou Actions › « Performance — profil cible TI-12 » › Run
gh run watch <id> && gh run download <id> -n perf-cible-<id>
```

Paramètres du déclenchement manuel : `utilisateurs`, `montee`, `duree`, `pause_ms`. Artefact : `results-cible/`
(`rapport/index.html`, `resume.md`, `synthese.md`, `results.jtl`, `echecs.xml`, `conteneurs.csv`, `pg-requetes.txt`,
`serveur.log`, `conteneurs-limites.txt`) et la synthèse du préchargement.

## 6. Limites et suite

- **Environnement représentatif, pas l'infrastructure d'IQ.** Conteneurs bornés sur un exécuteur partagé de 4 vCPU,
  sans réseau, sans TLS, sans base gérée en haute disponibilité. La recette de charge sur l'Azure d'IQ (région retenue,
  RST-18) reste à faire avant la mise en production, avec le même plan (`PROFIL=cible`, `perf/README.md`) et l'accord
  écrit de l'exploitant.
- **Connexion** : JMeter rejoue la connexion locale, pas Entra ID.
- **Charge de l'hôte** : JMeter partage l'exécuteur ; la charge moyenne de l'hôte (2,0, P95 3,9 pour 4 vCPU) montre qu'il
  n'a pas été le facteur limitant de ce run, mais les temps mesurés incluent cette contention.
- **Volumétrie** : 27 859 trousses en fin d'essai (préchargement compris), soit 40 % d'une année d'IQ. La présélection
  des trousses visibles parcourt encore les tables des trousses et des étapes à chaque page du suivi
  (`pg_stat_user_tables` : une lecture complète de chacune par appel), sans calcul coûteux par ligne : le coût croît
  linéairement avec le volume. À plusieurs années de conservation, refaire l'essai après un préchargement plus
  volumineux ; si le suivi s'en ressent, tenir une table des accès par trousse au lieu de lire les affectations.
- **Numérotation des trousses** : verrou par organisation et par année (changelog 059), tenu jusqu'à la fin de la
  création ; c'est la requête la plus longue en base au palier (17 ms en moyenne, attente du verrou comprise), sans effet
  sur les SLO à ce débit.
