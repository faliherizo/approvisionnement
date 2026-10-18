# Tests de performance JMeter — API dossiers d'approvisionnement

Charge l'API des dossiers (P1 — art. 15) et **valide les SLO** de latence et de taux d'erreur
définis dans [SLI-SLO-SLA.md](SLI-SLO-SLA.md).

## Contenu

| Fichier | Rôle |
|---------|------|
| `contrat-dossiers-load.jmx` | Plan JMeter : login → liste → création → rapport, avec assertions |
| `data/users.csv` | Jeux d'utilisateurs (email, mot de passe) — pilote le login |
| `data/dossiers.csv` | Jeux de dossiers (titre, n° AO, type, mode) — pilote la création |
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

## Surcharge des seuils

Tous les seuils sont surchargeables : `SLO_READ_P95`, `SLO_READ_P99`, `SLO_WRITE_P95`,
`SLO_WRITE_P99`, `SLO_ERROR_PCT`, et les plafonds durs SLA côté plan JMeter via `-Jsla.read.ms`,
`-Jsla.write.ms`.

> `results/` est régénéré à chaque exécution (ne pas versionner).
