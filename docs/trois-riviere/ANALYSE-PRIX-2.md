# Deuxième analyse de prix — AO 7000017905 (Ville de Trois-Rivières)

> Analyse d'aide à la décision. Contrainte structurante : **la solution doit garantir un SLA de
> 99,99 %** — l'hébergement en haute disponibilité (zones redondantes, base HA, Front Door + WAF,
> sauvegardes géo-redondantes, plan de reprise) est un **coût récurrent incompressible** (~25-30 k$/an),
> à financer par la licence annuelle. Toute licence trop basse met en péril ce SLA ou la marge.

### Détail du coût récurrent d'hébergement HA (incluant la supervision de disponibilité)

Le coût récurrent inclut explicitement la **supervision** qui **mesure et atteste** le SLA 99,99 %
(Azure Monitor + Application Insights), estimée aux tarifs Azure (régions canadiennes, ordre de
grandeur annuel, avant taxes) :

| Poste récurrent (Azure) | Estimation / an |
|---|---:|
| Container Apps (répliques multi-zones, backend + frontend) | ~7 000 – 9 000 $ |
| PostgreSQL Flexible Server **HA redondante par zones** + sauvegardes géo-redondantes | ~9 000 – 12 000 $ |
| Front Door + WAF + protection DDoS | ~3 500 – 5 000 $ |
| Key Vault, stockage, réseau, sortie de données | ~1 500 – 2 500 $ |
| **Supervision — Azure Monitor + Application Insights** : ingestion des journaux/télémétrie (Log Analytics, ~5–10 Go/mois) **et tests de disponibilité synthétiques** (sondes URL multi-régions, exécution aux 5 min) | **~900 – 1 500 $** |
| **Total hébergement HA + supervision** | **~22 000 – 30 000 $** |

> **Oui, le monitoring est compté.** Le coût d'**Azure Monitor + Application Insights** (dont les
> **tests de disponibilité** qui prouvent le 99,99 %) est **modeste (~900–1 500 $/an, < 2 % du forfait)**
> et **déjà absorbé** dans le coût récurrent d'hébergement financé par la licence annuelle de 58 000 $ ;
> il ne modifie donc pas les prix soumis.

## 1. Repère de marché (fourni)

| Niveau | Licence annuelle | Implantation | Total 3 ans (≈) |
|---|---|---|---|
| Très agressif | 40 k$ | 100 k$ | 180 k$ |
| **Compétitif** | **55-65 k$** | **125-160 k$** | **290-355 k$** |
| Premium | 75-90 k$ | 175-225 k$ | 325-405 k$ |
| Enterprise | 100 k$+ | 250 k$+ | 450 k$+ |

## 2. Constat sur l'offre actuelle

L'offre déposée (**58 000 $/an tout inclus, sans frais d'implantation distincts** → **174 000 $ sur
3 ans**) se situe **sous le niveau « très agressif »** du marché sur le total 3 ans. Deux risques :

- **Crédibilité** : un prix nettement inférieur au marché peut être lu comme une sous-estimation de
  l'ampleur (migration, intégrations M365, SLA 99,99 %) plutôt que comme un avantage.
- **Marge / SLA** : à 58 k$/an tout inclus, l'hébergement HA (SLA 99,99 %) absorbe ~la moitié du
  forfait ; la marge pour le support, l'assistance juridique et l'évolution est très mince.

**Conclusion : il y a de la place pour se repositionner « compétitif » sans sortir du marché**, ce qui
renforce la crédibilité et sécurise le SLA — tout en restant sous le « premium ».

## 3. Recommandation — « le mieux » (positionnement compétitif)

Séparer clairement **récurrent** et **ponctuel** (ce que l'exigence 17 de l'addenda demande d'ailleurs) :

| Poste | Montant | Nature |
|---|---|---|
| **Licence annuelle tout inclus** (licences · hébergement Azure **HA SLA 99,99 %** · support · maintenance · assistance juridique) | **57 000 $/an** | Récurrent |
| **Implantation** (An 1) | **160 000 $** | Ponctuel |

- **Licence 57 k$/an** : conforme au bas de la fourchette « compétitif » (55-65 k$) et suffisante pour
  financer le SLA 99,99 % avec une marge raisonnable.
- **Implantation 160 k$** : haut de la fourchette « compétitif » (125-160 k$), justifiée par la
  migration des modèles Word, les intégrations M365 et la mise en production encadrée.

### 3.1 Détail de l'implantation (160 000 $)

| Activité | Prix |
|---|---:|
| Analyse / cadrage | 22 000 $ |
| Configuration | 32 000 $ |
| Modèles et clauses | 32 000 $ |
| Migration | 26 000 $ |
| M365 / intégrations | 22 000 $ |
| Formation | 12 000 $ |
| Mise en production | 8 000 $ |
| Gestion de projet | 6 000 $ |
| **Total** | **160 000 $** |

### 3.2 Reconstitution — période ferme (3 ans), avant taxes

| Année | Composition | Montant |
|---|---|---:|
| An 1 (2027) | Implantation 160 000 + Licence 57 000 | 217 000 $ |
| An 2 (2028) | Licence | 57 000 $ |
| An 3 (2029) | Licence | 57 000 $ |
| **Total ferme 3 ans** | | **331 000 $** |

**331 000 $ avant taxes → 380 567,25 $ TTC** (TPS 5 % + TVQ 9,975 %). Positionnement : **compétitif**
(fourchette 290-355 k$), légèrement au-dessus du milieu — crédible et défendable.

### 3.3 Années optionnelles (licence indexée +5 %/an)

| Option | Année | Licence |
|---|---|---:|
| 1 | 2030 | 59 850,00 $ |
| 2 | 2031 | 62 842,50 $ |
| 3 | 2032 | 65 984,63 $ |
| 4 | 2033 | 69 283,86 $ |
| 5 | 2034 | 72 748,05 $ |
| 6 | 2035 | 76 385,45 $ |
| 7 | 2036 | 80 204,72 $ |
| **Somme 7 options** | | **487 299,21 $** |

**Total 10 ans : 818 299,21 $ avant taxes (≈ 940 838,93 $ TTC)**, si toutes les options sont levées.

## 4. Contrainte de conformité — PAS de frais d'implantation distincts

⚠️ **Le cahier des charges interdit une facturation d'implantation séparée.** Il exige que tous les
services nécessaires à la réalisation du contrat — dont l'**implantation** (le fournisseur en est
explicitement responsable) — soient *« inclus dans les prix présentés et … fournis **sans frais
supplémentaires** »*, et le bordereau ne comporte que des **forfaits annuels** « pour l'ensemble des
licences **et services** », *« lesquels comprennent l'ensemble des frais exigibles »*.

**Conséquence** : ajouter une ligne « frais d'implantation non inclus de la licence » serait
**NON CONFORME** (motif de rejet). L'implantation doit être **absorbée dans le forfait annuel**.

**La seule façon conforme de refléter le coût d'implantation** : chaque année du bordereau ayant son
propre prix, on **charge davantage l'année 1** (implantation incluse), et on redescend en années 2-3
(récurrent seul) :

| Année | Forfait annuel (tout inclus) | Contenu |
|---|---:|---|
| An 1 (2027) | **217 000 $** | Licence + hébergement HA + support **+ implantation (160 000 $)** |
| An 2 (2028) | **57 000 $** | Récurrent (licence + hébergement HA + support) |
| An 3 (2029) | **57 000 $** | Récurrent |
| **Ferme 3 ans** | **331 000 $** | |

> Le détail « ponctuel vs récurrent » exigé par l'**exigence 17** de l'addenda est fourni **en
> description** (tableau §3.1), sans jamais constituer une facturation distincte : c'est de la
> transparence, pas un frais supplémentaire. Le pointage « Prix » (volet 4) porte sur le total du
> bordereau, qui **inclut** donc l'implantation — offre complète et conforme.

## 5. Synthèse comparée

| | Offre actuelle | **Recommandation** | Marché « compétitif » |
|---|---:|---:|---:|
| Licence/an | 58 000 $ | **57 000 $** | 55-65 k$ |
| Implantation | (incluse) | **160 000 $** | 125-160 k$ |
| Total 3 ans (av. taxes) | 174 000 $ | **331 000 $** | 290-355 k$ |
| SLA 99,99 % financé | à marge très mince | **oui, marge saine** | — |

> **Décision requise** : appliquer la recommandation (Option A) au **bordereau** et au **mémoire §10**
> (puis régénérer les PDF du dossier) ? Ou ajuster un paramètre (montant d'implantation, indexation) ?

## 6. Décision finale (retenue)

Le coût de **développement de la plateforme est un investissement de VINA** (déjà engagé), récupéré par
l'**abonnement récurrent** et mutualisé entre clients — il **ne se facture pas** en implantation. Seul se
facture le **travail d'onboarding spécifique à la Ville**. Ce travail a été **redimensionné à sa valeur
réelle** (~70-100 j-p) : **implantation = 100 000 $** (défendable ligne par ligne à l'exigence 17), et non
160 000 $ (qui aurait refacturé une partie du produit).

| Poste | Montant retenu |
|---|---:|
| Licence de base (récurrent tout inclus, SLA 99,99 %) | **58 000 $/an** |
| Implantation (onboarding, incluse au forfait An 1) | **100 000 $** |
| An 1 (2027) = 58 000 + 100 000 | **158 000 $** |
| Période ferme 3 ans | **274 000 $ / 315 031,50 $ TTC** |
| Options 7 ans (+5 %/an) | 495 848,31 $ / 570 101,60 $ TTC |
| **Total 10 ans** | **769 848,31 $ / 885 133,10 $ TTC** |

Positionnement : entrée de « compétitif », **honnête et défendable**, la valeur du produit étant portée
par le récurrent sur la durée du contrat.
