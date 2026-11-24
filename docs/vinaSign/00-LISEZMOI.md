# VinaSign — Dossier de cadrage

- **Date** : 22 novembre 2026
- **Statut** : proposé, à décider en phase 0 (7 au 18 décembre 2026)

VinaSign devient une **plateforme tierce de signature électronique opérée par Vina**, dans un projet séparé. Nos
applications l'appellent par API, SDK ou iframe (contrat, SAD et Télésoin, autres applications de `~/project`), puis
la plateforme se vend en SaaS.

## Recommandations

1. **Projet séparé** : dépôt `~/project/vinasign`, déploiement, domaines et HSM propres. Nos applications sont des
   clientes comme les autres ([VS-ADR-001](adr/VS-ADR-001-projet-separe.md)).
2. **Ne pas repartir de zéro** : le module CLM du contrat contient déjà le moteur (PAdES-B-LTA, CAdES, horodatage,
   journal chaîné, portail signataire, webhooks HMAC, mesure par enveloppe), validé par pyHanko et veraPDF. On
   l'extrait sous tests de caractérisation ([VS-ADR-002](adr/VS-ADR-002-extraction-du-moteur-clm.md)).
3. **Confiance opérée, mais ancrée** : Vina opère signature, clés (HSM), horodatage et preuve. Les certificats
   viennent d'une autorité reconnue partenaire. Vina ne devient pas autorité de certification
   ([VS-ADR-003](adr/VS-ADR-003-plateforme-de-confiance-operee.md)).
4. **Mode empreinte** pour les applications de santé : le document ne quitte jamais le SAD ni Télésoin
   ([VS-ADR-005](adr/VS-ADR-005-signature-d-empreinte.md)).
5. **Pilote interne en production le 4 juin 2027** (SAD et Télésoin), V1 commerciale le 30 septembre 2027. Le CLM
   bascule par organisation, et IQ seulement avec son accord.

## Documents

| # | Document | Contenu |
|---|---|---|
| — | [VinaSign_Dossier_Architecture_Backlog.md](VinaSign_Dossier_Architecture_Backlog.md) | Dossier d'origine : produit, architecture, backlog MVP |
| — | [VinaSign_Architecture_Integration_Reutilisable.md](VinaSign_Architecture_Integration_Reutilisable.md) | Dossier d'origine : moteur réutilisable, intégration |
| 01 | [Analyse des dossiers et de l'existant](01-Analyse-des-dossiers-et-existant.md) | Douze lacunes relevées, inventaire réutilisable du CLM, besoins des applications |
| 02 | [Dossier d'architecture](02-Dossier-d-architecture.md) | C4, contextes métier, modèle de données, API v1, séquences, clés, exigences non fonctionnelles, déploiement |
| 03 | [Sécurité et conformité](03-Securite-et-conformite.md) | STRIDE, contrôles, vérification continue, cadre juridique, certifications |
| 04 | [Intégration des applications internes](04-Integration-applications-internes.md) | SAD (US-408), Télésoin, CLM, applications suivantes |
| 05 | [Backlog et stories](05-Backlog-et-stories.md) | 14 épics, 77 stories, critères d'acceptation, points, sprint, définition de « terminé » |
| 06 | [Planning](06-Planning.md) | Jalons, sprints, phase 0, équipe, budget, risques |
| 07 | [Modèle SaaS](07-Modele-SaaS.md) | Plans, unité facturée, exigences propres au SaaS, positionnement |
| ADR | [adr/](adr/) | VS-ADR-001 à 007 (MADR) |

## Décisions attendues à J0 (18 décembre 2026)

- [ ] Accepter VS-ADR-001 à 007.
- [ ] Nommer l'équipe (≈ 4,25 équivalents temps plein) et réserver la capacité de `soins-virtuel` en S9.
- [ ] Présélectionner l'autorité de certification, les autorités d'horodatage, le relais courriel et la passerelle
  SMS.
- [ ] Mandater le conseiller juridique.
- [ ] Choisir entre le scénario complet et la variante réduite ([06](06-Planning.md), § 5).
