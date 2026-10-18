---
title: Plan de reprise après sinistre (RPO / RTO) — Microsoft Azure — AO 7000017905
---

# Plan de reprise après sinistre (PRA) — cibles RPO ≤ 15 min, RTO ≤ 4 h

> Pièce jointe au dépôt (article 16 — sauvegarde, reprise, continuité). Hébergement **Microsoft Azure**,
> régions canadiennes. La procédure est **outillée en Terraform** et **automatisée par script**.

## 1. Définitions et acronymes

| Terme | Définition |
|---|---|
| **RPO** (*Recovery Point Objective*) | Perte de données maximale tolérée. **Cible : ≤ 15 min.** |
| **RTO** (*Recovery Time Objective*) | Délai maximal de rétablissement après sinistre. **Cible : ≤ 4 h.** |
| **PRA / DR** | Plan et moyens de reprise après un sinistre affectant une région entière. |
| **Pilot light** | Posture où seules la **donnée et les images** sont maintenues à chaud dans la région de reprise ; le calcul est instancié à la bascule (coût minimal en régime nominal). |
| **Zone de disponibilité (AZ)** | Centre de données isolé au sein d'une région. |
| **Région primaire** | **Canada Central** (Toronto) — trois zones de disponibilité. |
| **Région de reprise** | **Canada Est** (ville de Québec) — **données maintenues au Canada**. |
| **Front Door** | Point d'entrée mondial Azure assurant la **redirection du trafic** vers la région active. |

## 2. Deux niveaux de résilience

| Événement | Réponse | Cible |
|---|---|---|
| Panne d'une **zone** (AZ) | **Automatique** : HA multi-zone (Container Apps sur 3 zones, PostgreSQL Flexible primaire + veille synchrone, entrée réseau redondée). Aucune intervention. | **SLA 99,99 %** |
| Sinistre d'une **région complète** | **PRA** : bascule vers **Canada Est** (ce document). | **RPO ≤ 15 min, RTO ≤ 4 h** |

Le **SLA 99,99 %** est tenu par la redondance **multi-zone dans la région Canada Central** ; la bascule
inter-région n'intervient qu'en cas de **sinistre régional complet** (événement rare).

## 3. Posture de reprise permanente (« pilot light »)

En régime nominal, la région de reprise (Canada Est) ne porte **aucun calcul** ; seules la donnée et les
images y sont répliquées en continu.

```mermaid
flowchart TB
  subgraph P["PRODUCTION — Canada Central"]
    direction TB
    FD["Azure Front Door + WAF OWASP à l'ingress"]
    subgraph ACA["Container Apps (zone-redondant)"]
      direction LR
      A1["Réplique z1"]
      A2["Réplique z2"]
      A3["Réplique z3"]
    end
    PG["PostgreSQL Flexible<br/>HA primaire + veille"]
    ST["Storage ZRS<br/>documents / exports"]
    FD --> ACA --> PG
    ACA --> ST
  end
  subgraph DR["DR — Canada Est (pilot light, sans calcul)"]
    direction TB
    STDR["Storage DR<br/>documents / exports"]
    BKP["Sauvegardes géo-redondantes<br/>PostgreSQL (GeoRestore)"]
    TF["Code Terraform"]
  end
  ST -- "réplication d'objets" --> STDR
  PG == "sauvegardes géo-redondantes" ==> BKP
```

| Élément répliqué | Mécanisme (Azure) | Contribution au RPO |
|---|---|---|
| Documents / exports | Réplication d'objets vers le compte de stockage DR | ~minutes |
| Base PostgreSQL | Sauvegardes **géo-redondantes** → **GeoRestore** au failover | **≤ 15 min** |
| Images de conteneur | ACR (registre) alimenté par le CI | image déjà présente au failover |
| Infrastructure | code Terraform versionné | reconstruction en une commande |

## 4. Procédure de bascule vers Canada Est

En cas de sinistre régional, la séquence suivante rétablit le service. Elle est **automatisée** et
**journalisée** (mesure du RTO).

```mermaid
flowchart TD
  S1["1 · Détection de l'incident"]
  S2["2 · Terraform région DR (sorties de réplication)"]
  S3["3 · Réseau + sécurité (VNet, sous-réseaux)"]
  S4["4 · Calcul : Azure Container Apps + ingress"]
  S5["5 · Restaurer PostgreSQL (GeoRestore)"]
  S6["6 · Rattacher le stockage répliqué"]
  S7["7 · Déployer l'image (ACR)"]
  S8["8 · Tests de santé (/actuator/health)"]
  S9["9 · Redirection du trafic (Azure Front Door)"]
  S10["10 · Service rétabli (consigner l'heure — RTO)"]
  S1 --> S2 --> S3 --> S4 --> S5 --> S6 --> S7 --> S8
  S8 -- "OK" --> S9 --> S10
  S8 -- "échec" --> KO["NE PAS rediriger — diagnostic"]
```

| Étape | Durée typique |
|---|---:|
| Préparation / sorties | ~5 min |
| Réseau + Container Apps | ~15-25 min |
| Restauration PostgreSQL (GeoRestore) | ~30-90 min |
| Stockage + démarrage conteneurs | ~10 min |
| Tests de santé | ~5-10 min |
| Redirection Front Door | ~2-5 min |
| **Total** | **~1 h 15 – 2 h 30** (< 4 h, avec marge) |

## 5. Bascule inverse

Une fois Canada Central rétablie : reconstruire la production, resynchroniser la base et le stockage,
tester, puis rebasculer Front Door. Canada Est redevient « pilot light ».

## 6. Validation et exercices

- **Exercice de reprise semestriel** (mode simulation, sans rediriger le trafic) : le RTO réel est mesuré et consigné.
- **Surveillance continue** du retard de réplication (Azure Monitor) avec alertes.
- **Rapport post-exercice** : écarts au budget de temps, actions correctives.
