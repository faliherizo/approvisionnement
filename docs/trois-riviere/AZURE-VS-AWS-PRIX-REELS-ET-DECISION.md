---
title: Pourquoi Azure ici et AWS ailleurs — prix réels validés et décision (AO 7000017905)
subtitle: Prix relevés aux API/catalogues officiels le 28 septembre 2026
---

# Azure vs AWS pour Trois-Rivières : ce que disent les prix réels

> **Contexte.** Sur le projet **RH scolaire (freelance-invoicing / CSSDC 2026-PLAT-GA)**, l'hébergement
> recommandé est **AWS** ; ici (Trois-Rivières) je recommandais **Azure**. La question légitime : pourquoi
> la divergence ? Est-ce le **SLA (99,5 % là-bas vs 99,99 % ici)** ou les **abonnements Twilio (textos)** ?
> Réponse courte : **ni l'un ni l'autre**. Ce document valide les **prix réels** (sources officielles) et
> explique la vraie raison — puis dit ce qu'on peut faire.

## 1. Méthode et sources (prix vérifiés, non estimés)

| Fournisseur | Source officielle interrogée | Devise |
|---|---|---|
| Azure | API prix de détail `prices.azure.com/api/retail/prices` (région `canadacentral`) | CAD |
| AWS | Fichiers de prix publics `pricing.us-east-1.amazonaws.com` (région `ca-central-1`) | USD→CAD (1,4145, BdC 25 sept.) |
| OVHcloud | Catalogue public `ca.api.ovh.com/1.0/order/catalog/public/cloud` | CAD |

## 2. Les trois hypothèses — et pourquoi deux sont fausses

**a) « C'est le niveau de SLA (99,5 % vs 99,99 %). »** → **Faux comme cause du choix.** À **99,99 %**,
les deux nuages exigent du **multi-zone** ; et **AWS reste moins cher qu'Azure aussi en 99,99 %** (voir
§4). Le SLA change le *dimensionnement*, pas le *gagnant sur le prix*.

**b) « L'autre projet a Twilio (textos). »** → **Sans rapport avec le choix d'hébergeur.** Twilio est un
**SaaS facturé à l'usage (par SMS)**, **identique sur Azure, AWS ou OVH**. Il n'entre pas dans le coût
d'hébergement et ne fait pencher aucun nuage. (Notre solution de rédaction n'envoie d'ailleurs pas de
SMS : notifications par **courriel via M365/Outlook** et dans l'application.)

**c) La vraie différence : la STRICTESSE de la résidence des données.**

| | RH scolaire (CSSDC) | **Trois-Rivières (nous)** |
|---|---|---|
| Exigence de résidence | **Québec seulement** (organisme scolaire, addenda réponse 11) | **Canada** (Addenda 1, exigence 1 : « Données hébergées au Canada ») |
| SLA | 99,5 % (sans HA, reprise 48 h) | **99,99 %** (HA multi-zone) |

## 3. Le fait technique qui décide (validé)

**Azure Canada East (ville de Québec) n'a AUCUNE zone de disponibilité, et sa région appariée est
Canada Central (Toronto, Ontario).** (Source : liste officielle des régions Azure.)

| Région | Zones de disponibilité | Région appariée | Emplacement |
|---|---|---|---|
| Azure **Canada Central** | **3** | Canada East | **Toronto (Ontario)** |
| Azure **Canada East** | **aucune** | Canada Central | Québec |
| AWS **ca-central-1** | **3** | — | **Montréal (Québec)** |

- **Pour RH scolaire (Québec obligatoire)** : Azure ne peut faire **ni** de zone-redondance **au Québec**
  (Canada East = 0 zone), **ni** de géo-redondance sans **sortir en Ontario** (interdit). **AWS Montréal
  (3 zones au Québec)** est donc le seul à satisfaire *résidence Québec + redondance*. Ce n'est pas un
  choix de prix, c'est une **contrainte de conformité**.
- **Pour nous (résidence Canada)** : **Azure Canada Central (Toronto, 3 zones)** est **autorisé** et tient
  le 99,99 %. Azure redevient donc **viable** — ce qui n'était pas le cas là-bas. **Bonus** : AWS
  Montréal étant **au Québec**, il offrirait même à Trois-Rivières une résidence *québécoise* (plus forte
  que « Canada »).

## 4. Prix réels pour NOTRE configuration 99,99 % (multi-zone)

Postes décisifs, prix relevés le 28 sept. 2026 (Azure `canadacentral`, AWS `ca-central-1`, OVH BHS) :

| Poste (config HA, 1 client) | **Azure Canada Central** | **AWS ca-central-1 (Montréal)** | **OVH Beauharnois** |
|---|---:|---:|---:|
| **Pare-feu applicatif (WAF OWASP)** | **Front Door Premium : 457,40 $/mois = 5 489 $/an** (WAF managé inclus) | **AWS WAF : 5 $/mois + ~5 règles + requêtes ≈ 212 $/an** | **aucun produit managé** (à bâtir) |
| **PostgreSQL HA** | Flexible GP 2 vCore 0,2744 $/h **× 2 (veille)** + stockage/backup ≈ **~5 200 $/an** | RDS `db.t4g.large` **Multi-AZ** 0,282 $/h = **3 494 $/an** | Managé **business** `db1-7` **2 nœuds** = **2 661 $/an** |
| **Calcul conteneurs (3 zones)** | Container Apps/AKS ≈ 7 000–8 400 $/an | Fargate **ARM** (0,03565 $/vCPU-h) ≈ 6 000–7 600 $/an | K8s (plan de contrôle **gratuit**) + nœuds ≈ 4 200 $/an |
| **Entrée / egress** | egress payant (~600–900 $/an) | egress payant (~660 $/an) | **egress gratuit** |
| **Supervision SLA synthétique** | App Insights (managé) ~1 560 $/an | CloudWatch Synthetics ~720 $/an | sonde externe ~600 $/an |
| **Ordre de grandeur total (PAYG)** | **~21 000 – 23 000 $/an** | **~14 000 – 16 000 $/an** | **~11 000 – 13 000 $/an** |

> **Les deux écarts qui font tout, et ils sont structurels :**
> 1. **WAF** : Azure facture une **présence horaire** (Front Door Premium **5 489 $/an**, quel que soit le
>    trafic) ; AWS facture des **requêtes** (**≈ 212 $/an** au volume d'un client). **≈ 25× moins cher.**
> 2. **Base HA** : Azure double le coût pour la veille (~5 200 $) ; RDS Multi-AZ (3 494 $) et OVH business
>    (2 661 $) sont nettement dessous.
> **Bilan : pour notre projet aussi, AWS est ~6 000–7 000 $/an moins cher qu'Azure** — le même ordre de
> grandeur que sur le projet RH scolaire.

## 5. Alors pourquoi recommander Azure ici ?

Parce que le choix, **une fois la conformité assurée par les deux** (ce qui est le cas chez nous, pas chez
RH scolaire), **ne se joue plus sur le prix ni la capacité, mais sur le risque d'exécution** :

1. **Intégration Microsoft 365 native** (art. 9 : publication SharePoint/Teams/Outlook) : sur Azure,
   **Entra ID** fournit SSO et **authentification Graph** dans le **même écosystème** que la Ville
   (déjà cliente M365). Sur AWS, l'intégration M365 marche mais passe par des **ponts d'identité** externes.
2. **Expertise du fondateur** : la stack maîtrisée est **Azure** (Azure DevOps, Entra ID/B2C, AKS, App
   Insights) — décisif pour un fondateur seul assurant N1→N5 et le 99,99 %.
3. **Dossier déjà rédigé autour d'Azure** (mémoire, architecture HA, démo) : bascule = tout réécrire.

**Ces avantages sont réels mais « doux ». Ils coûtent ~6–7 k$/an de plus (surtout le WAF).**

## 6. Ce qu'on peut faire — trois options

| Option | Description | Effet | Recommandation |
|---|---|---|---|
| **A. Azure tel quel** | Canada Central + Front Door Premium (WAF managé) | +5 489 $/an de WAF assumés ; reste dans le budget 68 k$ | acceptable |
| **B. Basculer sur AWS Montréal** | ca-central-1, 3 zones **au Québec**, AWS WAF | **−6–7 k$/an** + résidence **québécoise** renforcée ; coût = réécrire les sections Azure + intégration M365 par ponts d'identité | à considérer si la marge prime |
| **C. Azure sans le surcoût WAF** | **Front Door Standard** (pas Premium) *ou* **WAF OWASP (ModSecurity/Coraza) sur l'ingress Container Apps/AKS** ; anti-DDoS Azure conservé | **économise ~5 000 $/an** tout en gardant Azure + M365 + dossier | **recommandé si on garde Azure** |

> **Point à faire préciser (comme sur l'autre projet)** : la variante **« Application Gateway WAF v2 —
> Discounted »** (~2 914 $/an au lieu de 5 489 $) apparaît au catalogue Azure sans conditions d'accès
> publiées → **à confirmer auprès de Microsoft**.

## 7. Recommandation

- **Conserver Azure (Canada Central) pour la soumission**, pour le **risque d'exécution minimal** (M365
  natif, expertise, dossier prêt) — **mais appliquer l'option C** pour supprimer le surcoût WAF
  (~5 000 $/an) : Front Door Standard ou WAF sur l'ingress. On garde alors Azure **au coût d'AWS**.
- **Documenter AWS Montréal comme alternative forte** : moins cher **et** résidence *québécoise* (atout
  pour une ville du Québec). Le code Terraform AWS (`infra/terraform/aws/`) est déjà prêt et validé.
- **Corriger le dossier** : la mention « Canada Central **et** Canada Est » pour la HA/DR doit préciser que
  la **zone-redondance 99,99 % se fait en Canada Central** (Canada East n'a pas de zones) ; Canada East
  ne sert qu'à la **reprise inter-région** (DR), pas à la HA.
- **Twilio** : hors sujet pour l'hébergement — ne pas l'intégrer au raisonnement AWS/Azure.

---

### Sources

- [Azure — API prix de détail (Canada Central)](https://prices.azure.com/api/retail/prices) — relevé 28 sept. 2026 (Front Door Premium 457,40 $/mois ; PostgreSQL Flexible GP 2 vCore 0,2744 $/h ; stockage 0,1913 $/Go ; backup LRS 0,1455 $/Go/mois).
- [Azure — liste officielle des régions et zones](https://learn.microsoft.com/en-us/azure/reliability/regions-list) (Canada East : aucune zone ; appariée Toronto).
- [AWS — fichiers de prix `ca-central-1`](https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json) (RDS `db.t4g.large` Multi-AZ 0,282 $/h ; AWS WAF WebACL 5 $/mois + règles ; Fargate ARM 0,03565 $/vCPU-h) — relevé 27–28 sept. 2026.
- [OVHcloud — catalogue Public Cloud Canada](https://ca.api.ovh.com/1.0/order/catalog/public/cloud) (PostgreSQL business `db1-7` 2 nœuds 221,77 $/mois).
- Analyses associées : [`COMPARAISON-HEBERGEMENT-CLOUD.md`](COMPARAISON-HEBERGEMENT-CLOUD.md), [`ESTIMATION-COUTS-CLOUD-DETAILLEE.md`](ESTIMATION-COUTS-CLOUD-DETAILLEE.md).
