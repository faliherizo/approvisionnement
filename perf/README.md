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

Restent à traiter de la même façon, hors du parcours mesuré ici : le tableau de bord (UI-01) et la liste « Mes trousses »
(UI-02), qui lisent encore toutes les trousses visibles. L'essai est à refaire sur l'infrastructure cible (PostgreSQL,
Azure Canada) : c'est là que la mesure engage la soumission, et IQ peut en exiger une autre avant l'acceptation finale.

## Surcharge des seuils

Tous les seuils sont surchargeables : `SLO_READ_P95`, `SLO_READ_P99`, `SLO_WRITE_P95`,
`SLO_WRITE_P99`, `SLO_ERROR_PCT`, et les plafonds durs SLA côté plan JMeter via `-Jsla.read.ms`,
`-Jsla.write.ms`.

> `results/` est régénéré à chaque exécution (ne pas versionner).
