# ADR-020 — Région d'hébergement et grille des niveaux de service

- **Statut** : **Proposé** (décision d'IQ attendue : question E1 et grille J1 à J4)
- **Date** : 2026-11-20
- **Portée** : `infra/terraform/azure` (variables `location` et `zones`), `infra/dr`, chart Helm, offre de service
- **Origine** :
  - RST-18 ;
  - Cahier § 1.4.2 (hébergement au Québec ou dans une juridiction équivalente, preuves de localisation) ;
  - Cahier § 5 et DAO § 3.3.10 (disponibilité, niveaux de service) ;
  - addenda 4 : Q13 (lieux d'accès administratif), Q15 (sauvegardes, RTO 8 h, RPO 4 h).

## Contexte

- **Régions disponibles :** Azure offre deux régions au Canada. `canadacentral` est à Toronto ; `canadaeast` est à
  Québec.
- **Configuration actuelle :** l'infrastructure place la production à Toronto, sur trois zones de disponibilité
  (nœuds AKS répartis, PostgreSQL en haute disponibilité inter-zones), et la reprise après sinistre à Québec.
- **Exigence :** le Cahier admet une « juridiction équivalente ». Mais une communication de renseignements
  personnels hors du Québec impose à IQ une évaluation des facteurs relatifs à la vie privée (Loi 25, art. 17). Pour un
  organisme public québécois, un hébergement au Québec supprime cette dépendance.

## Options

| | A — Production à Toronto, reprise à Québec | B — Production à Québec, reprise à Toronto (recommandée si IQ lit « au Québec ») |
|---|---|---|
| Localisation des données en exploitation | Ontario (juridiction équivalente : évaluation art. 17 par IQ) | **Québec** |
| Copies de reprise (sauvegardes géo-redondantes, réplication) | Québec | Ontario (évaluation art. 17 limitée aux copies de secours chiffrées) |
| Zones de disponibilité | 3 zones : nœuds répartis, PostgreSQL inter-zones | Région sans zones à la date de rédaction (à vérifier au déploiement) : nœuds dans la région, PostgreSQL en veille synchrone dans la même zone |
| Disponibilité visée | ≥ 99,5 % mensuels, large marge (plan de contrôle AKS avec zones, base inter-zones) | ≥ 99,5 % mensuels, marge plus étroite. La reprise régionale (`infra/dr`) couvre la perte de la région |
| Terraform | `location = "canadacentral"`, `zones = ["1","2","3"]` (défaut) | `location = "canadaeast"`, `zones = []` |

Les deux options sont livrées : le choix se fait par variable, sans modifier le code. Une validation refuse toute
région hors du Canada.

## Décision proposée

- **Si IQ exige que les données d'exploitation restent au Québec : option B.**
- **Si IQ accepte une juridiction équivalente, avec son évaluation art. 17 : option A**, qui offre la meilleure
  disponibilité.

**Dans les deux cas :**

- tout accès administratif ou de soutien est déclaré avec son territoire (Q13 ; tableau des lieux, solutions § 3) ;
- les clés sont gérées dans Key Vault, dans la région de production ;
- aucune donnée n'est hébergée hors du Canada.

### Grille de niveaux de service proposée (J1 à J4)

| Indicateur | Engagement proposé | Exigence d'IQ |
|---|---|---|
| Disponibilité mensuelle, hors maintenance annoncée | ≥ 99,5 % | 99,5 % |
| Maintenance planifiée | Annoncée 5 jours ouvrables d'avance, hors heures d'affaires, ≤ 4 h par mois | — |
| Perte de données maximale (RPO) | ≤ 15 min (PITR PostgreSQL, réplication) | ≤ 4 h |
| Délai de rétablissement (RTO) | ≤ 4 h (bascule régionale `infra/dr`, exercée) | ≤ 8 h |
| Incident critique (service indisponible) | Prise en charge ≤ 1 h, 24/7 ; contournement ≤ 4 h | — |
| Incident majeur (fonction essentielle dégradée) | Prise en charge ≤ 4 h ouvrables ; correctif ≤ 2 jours ouvrables | — |
| Incident mineur | Prise en charge ≤ 1 jour ouvrable ; correctif à la version suivante | — |
| Rapport de service | Mensuel : disponibilité mesurée (sonde externe), incidents, maintenances, capacité | — |
| Exercice de reprise | Annuel, rapport remis à IQ | — |

## Conséquences

- **Coût :** l'option B réduit la redondance intra-régionale, à coût d'infrastructure à peu près égal. L'écart de
  disponibilité est compensé par la reprise régionale exercée.
- **Avant le dépôt :** confirmer, sur le portail Azure et à la date du déploiement, la disponibilité des zones et des
  services (AKS, PostgreSQL flexible, Front Door) en `canadaeast`, ainsi que les SLA publiés. La grille ci-dessus est
  un engagement de service ; elle ne recopie pas les SLA d'Azure.
- **Après la décision d'IQ :** passer cet ADR au statut « Accepté » avec l'option retenue, régler `location` et
  `zones` dans les variables de production, puis inverser `infra/dr` si l'option B est retenue.
