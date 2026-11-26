# Tests de performance JMeter — API dossiers d'approvisionnement

Charge l'API des dossiers (P1 — art. 15) et **valide les SLO** de latence et de taux d'erreur
définis dans [SLI-SLO-SLA.md](SLI-SLO-SLA.md).

## Contenu

| Fichier | Rôle |
|---------|------|
| `contrat-dossiers-load.jmx` | Plan JMeter : login → liste → création → rapport, avec assertions |
| `data/users.csv` | Jeux d'utilisateurs (email, mot de passe) — pilote le login |
| `data/dossiers.csv` | Jeux de dossiers (titre, n° AO, type, mode) — pilote la création |
| `clm-trousses-load.jmx` | Plan CLM (IQ, addenda 2 Q4) : login → trousse → document → signataire → parcours → lancement → détail → suivi |
| `data/clm-initiateurs.csv`, `data/clm-trousses.csv`, `data/convention.pdf` | Données du plan CLM (comptes, trousses, document déposé) |
| `run.sh` | Exécute en mode non-graphique, produit le rapport HTML et gate sur les SLO |
| `profils/cible.env` | Profil « cible » : 500 utilisateurs sur l'infrastructure cible (voir « Profil cible ») |
| `SLI-SLO-SLA.md` | Indicateurs, objectifs internes et engagement contractuel |

Les **données de test sont dans des fichiers `.csv`** (lues par les *CSV Data Set Config* du plan),
séparées du plan lui-même (DRY / SRP).

## Prérequis

- JMeter 5.6+ (`brew install jmeter`)
- Le backend accessible (par défaut `http://localhost:8080`). Démarrage local H2 :
  ```bash
  cd ../backend && JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
    mvn -DskipTests spring-boot:run
  ```

## Exécution

```bash
cd perf
./run.sh                                  # charge nominale (20 threads), gate SLO
THREADS=10 RAMPUP=5 LOOPS=3 ./run.sh      # charge réduite (fumée)
BASE_PORT=8100 ./run.sh                   # backend natif sur port isolé
```

`run.sh` sort avec un **code non nul** si un SLO (P95/P99 lecture ou écriture, taux d'erreur) est
dépassé → utilisable comme porte de performance en CI. Le rapport HTML détaillé est dans
`results/rapport/index.html`.

## Volumétrie CLM d'IQ (addenda 2, Q4)

IQ prévoit 55 000 à 65 000 documents par an, pics inconnus. 65 000 ÷ 250 jours ouvrables ≈ 260 trousses par jour ;
une heure de pointe portant 20 % de la journée et multipliée par 5 donne ≈ 260 trousses par heure, soit moins d'une
trousse toutes les 10 secondes. La charge nominale du plan (20 initiateurs, 5 boucles) dépasse ce rythme : elle vérifie
les SLO avec une marge, et se recalibre (`THREADS`, `LOOPS`) dès que IQ fournit ses volumes mensuels.

```bash
PLAN=clm-trousses-load.jmx BASE_PORT=8100 ./run.sh
```

## Q38 — 500 utilisateurs concurrents (addenda 3)

Essai du 2026-11-17, poste de développement, base H2 en mémoire, plan `clm-trousses-load.jmx`, 500 utilisateurs
virtuels montés en 60 s, un cycle complet chacun, après préchargement de 1 000 trousses.

```bash
PLAN=clm-trousses-load.jmx BASE_PORT=8100 THREADS=50 RAMPUP=10 LOOPS=20 ./run.sh    # préchargement
PLAN=clm-trousses-load.jmx BASE_PORT=8100 THREADS=500 RAMPUP=60 LOOPS=1 ./run.sh    # 500 concurrents
```

**Résultat après corrections (dernier essai) : 500 utilisateurs concurrents, 1 500 trousses en base, 8 000 requêtes,
0 erreur.**

| Requête | P95 |
|---|---|
| Connexion (`POST /api/auth/login`) | 946 ms |
| Suivi centralisé (`GET /api/clm/suivi`, 50 lignes) | 130 ms |
| Détail d'une trousse | 20 ms |
| Création, dépôt, signataire, parcours, lancement | 19 à 51 ms |

Écriture P95 42 ms (SLO 1 500 ms). Lecture P95 **810 ms pour un SLO de 800 ms** : seul dépassement, dû uniquement à
la connexion locale (500 hachages BCrypt en 60 s sur un poste de développement). Hors connexion, toutes les lectures
restent sous 130 ms. En production, les employés d'IQ se connectent par Entra ID (SSO), sans hachage local.

Constats successifs de l'essai :

| Constat | Cause | Suite |
|---|---|---|
| Références de trousse en double sous concurrence (409, puis 403 vides) | Numérotation par comptage de toutes les trousses, sans verrou | **Corrigé** : compteur verrouillé par organisation et année (changelog 059) |
| Erreurs serveur affichées comme 403 vides | Renvoi interne vers `/error` refusé par Spring Security | **Corrigé** : renvoi `ERROR` permis |
| Connexions refusées en pointe (500 à la connexion) | Pool de 10 connexions | **Corrigé** : pool réglable, 30 par défaut (`DB_POOL_MAX`) |
| Suivi centralisé à 24 s puis 40 s (P95), qui entraîne tout le reste | Suivi qui relit toutes les trousses et 4 requêtes par ligne | **Corrigé** : API et écran paginés (50 lignes par page, 200 au plus), filtres en base, lignes calculées en 4 requêtes groupées par page : 130 ms |
| Relevé d'utilisation lent à forte volumétrie | Une requête par trousse | **Corrigé** : une requête |

Le tableau de bord (UI-01) et la liste « Mes trousses » (UI-02) ont reçu le même traitement (RST-40, `LecturePaginee`) :
page tirée de la base avec total pour l'administration et l'audit, tranches de 200 sinon, décomptes groupés. L'essai est à refaire sur l'infrastructure cible (PostgreSQL,
Azure Canada) : c'est là que la mesure engage la soumission, et IQ peut en exiger une autre avant l'acceptation finale.

## Profil cible — 500 utilisateurs concurrents sur l'infrastructure cible (TI-12 d'IQ, RST-48)

**Statut : plan prêt, essai sur l'infrastructure cible non réalisé.** Le résultat ci-dessus (Q38) vient d'un poste de
développement ; il ne vaut pas mesure sur l'infrastructure cible.

`PROFIL=cible ./run.sh` lit [`profils/cible.env`](profils/cible.env) : plan `clm-trousses-load.jmx`, **500 fils**, montée
de 300 s, 3 boucles, pause de 2 s entre deux gestes, HTTPS sur 443, comptes dans `data/cible/comptes.csv`, résultats dans
`results-cible/`. Toute variable d'environnement l'emporte sur le profil. Propriétés JMeter transmises (`-J`) :

| Propriété | Variable de `run.sh` | Profil cible | Rôle |
|---|---|---|---|
| `BASE_HOST` | `BASE_HOST` | **obligatoire, sans défaut** | hôte de l'infrastructure cible |
| `BASE_PORT`, `BASE_PROTOCOL` | idem | 443, https | point d'entrée (passerelle, répartiteur) |
| `threads`, `rampup`, `loops` | `THREADS`, `RAMPUP`, `LOOPS` | 500, 300 s, 3 | utilisateurs simultanés, montée, cycles par utilisateur |
| `pause.ms` | `PAUSE_MS` | 2000 | temps de réflexion entre deux requêtes (0 hors profil) |
| `comptes` | `COMPTES` | `data/cible/comptes.csv` | comptes d'essai (`email,motDePasse`), un par utilisateur virtuel |
| `trousses` | `TROUSSES` | `data/clm-trousses.csv` | jeux de trousses |
| `resultats` | `RESULTATS` | `results-cible` | dossier des résultats (JTL, rapport HTML, synthèse) |
| `sla.read.ms`, `sla.write.ms` | — (`-J` dans le plan) | 3000, 5000 | plafonds durs du SLA par requête |

**Garde-fou.** `run.sh` refuse tout hôte autre que `localhost` tant que `CIBLE_AUTORISEE` ne reprend pas exactement cet
hôte (sortie 3, aucune requête émise). Aucune charge n'est lancée contre la production ni contre un environnement
partagé sans l'accord écrit de son exploitant.

**Préalables.**

1. Environnement cible réservé à l'essai (région retenue, RST-18), dans la configuration de production : PostgreSQL géré,
   nombre d'instances, pool (`DB_POOL_MAX`), passerelle et pare-feu applicatif. Aucun autre usage pendant la fenêtre.
2. Accord écrit de l'exploitant et d'IQ (date, fenêtre, adresses sources à autoriser au pare-feu et au limiteur de débit).
3. Comptes d'essai : 500 comptes à connexion locale dans une organisation d'essai (la connexion d'IQ passe par Entra ID,
   que JMeter ne rejoue pas), rôle d'initiateur CLM, listés dans `data/cible/comptes.csv` (fichier ignoré par Git,
   détruit après l'essai). Les comptes et l'organisation d'essai sont supprimés à la fin.
4. Préchargement de la volumétrie (1 000 trousses ou davantage, comme l'essai Q38) par une première passe à faible
   charge : `PROFIL=cible THREADS=50 LOOPS=20 PAUSE_MS=0 ./run.sh`.
5. Supervision ouverte pendant l'essai (UC et mémoire des instances, connexions à la base, erreurs 5xx) pour expliquer
   tout écart.

**Exécution.**

```bash
cd perf
export BASE_HOST=<hôte de l'infrastructure cible> CIBLE_AUTORISEE=<le même hôte>
PROFIL=cible ./run.sh                       # 500 utilisateurs, porte SLO, synthèse
```

**Critères (SLO, [SLI-SLO-SLA.md](SLI-SLO-SLA.md) § 2).** Taux d'erreur < 0,1 % ; lecture P95 ≤ 800 ms et P99 ≤ 1 500 ms ;
écriture P95 ≤ 1 500 ms et P99 ≤ 2 500 ms ; aucune requête au-delà des plafonds du SLA (3 000 ms en lecture, 5 000 ms en
écriture). `run.sh` sort en erreur si un objectif est dépassé.

**Rapport.** `run.sh` écrit `results-cible/synthese.md` (paramètres réellement utilisés, P95/P99, erreurs, verdict) et le
rapport HTML `results-cible/rapport/`. Les déposer dans
`docs/Dossier_IQ_2026-6370-0005/` (dossier de la soumission ou de la démonstration), sous un dossier daté
`essai-charge-cible-AAAA-MM-JJ/`, avec la description de l'infrastructure éprouvée et les relevés de supervision.

**Validation du plan à faible charge (2026-11-26, poste de développement, base H2).** Profil cible, backend local :

```bash
PROFIL=cible BASE_HOST=localhost BASE_PORT=8210 BASE_PROTOCOL=http THREADS=10 RAMPUP=10 LOOPS=2 PAUSE_MS=200 ./run.sh
```

10 utilisateurs, 320 échantillons, 0 erreur ; lecture P95 697 ms, P99 903 ms ; écriture P95 243 ms, P99 309 ms : SLO
respectés. Hôte distant sans `CIBLE_AUTORISEE` : refusé (sortie 3) ; profil cible sans `BASE_HOST` : refusé (sortie 2).
Cette validation éprouve le plan et ses paramètres, pas la tenue en charge de l'infrastructure cible.

## Surcharge des seuils

Tous les seuils sont surchargeables : `SLO_READ_P95`, `SLO_READ_P99`, `SLO_WRITE_P95`,
`SLO_WRITE_P99`, `SLO_ERROR_PCT`, et les plafonds durs SLA côté plan JMeter via `-Jsla.read.ms`,
`-Jsla.write.ms`.

> `results/` est régénéré à chaque exécution (ne pas versionner).
