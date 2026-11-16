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

## Surcharge des seuils

Tous les seuils sont surchargeables : `SLO_READ_P95`, `SLO_READ_P99`, `SLO_WRITE_P95`,
`SLO_WRITE_P99`, `SLO_ERROR_PCT`, et les plafonds durs SLA côté plan JMeter via `-Jsla.read.ms`,
`-Jsla.write.ms`.

> `results/` est régénéré à chaque exécution (ne pas versionner).
