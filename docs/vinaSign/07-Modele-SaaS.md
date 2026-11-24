# VinaSign — Modèle SaaS

- **Date** : 22 novembre 2026
- **Statut** : hypothèses de travail, à valider avec la direction commerciale avant J6.

## 1. Offre

| Plan | Pour qui | Contenu indicatif |
|---|---|---|
| Essai | Développeurs, prospects | Bac à sable illimité, certificats de test, aucune valeur probante |
| Essentiel | PME, travailleurs autonomes | Portail autonome, niveau simple, cachet VinaSign, quota mensuel d'enveloppes |
| Affaires | Organisations | API, webhooks, iframe, cachet propre à l'organisation, niveau renforcé (code, clé d'accès), marque |
| Entreprise et secteur public | Grands comptes, organismes publics | Ce qui précède, plus l'assertion SSO de l'application, le mode empreinte, la résidence garantie au Canada, des SLO contractuels, l'export de preuve, l'apport de clé et une instance dédiée (V2) |

- **Unité facturée** : l'enveloppe envoyée, comptée une fois au mois de son premier envoi. C'est la règle déjà
  appliquée par le CLM (addenda 3 d'IQ, Q39), ce qui donne des factures prévisibles.
- **Options facturées** : SMS, vérification d'identité et signature avancée (V2), refacturés au coût augmenté d'une
  marge.

## 2. Ce que le SaaS exige de plus que le pilote interne

| Exigence | Où dans le backlog |
|---|---|
| Libre-service et vérification de l'organisation | VS-122 |
| Paiement hébergé par un prestataire, sans aucun numéro de carte chez VinaSign | VS-121 ([03](03-Securite-et-conformite.md), PCI DSS hors périmètre) |
| Mesure de l'usage, quotas, factures | VS-015, VS-120 |
| Page d'état, SLO publiés, support | VS-124 |
| Rapport SOC 2 Type I, puis Type II | J6, puis VS-135 |
| Conditions d'utilisation, politique de confidentialité, entente de traitement des renseignements personnels (DPA) | J6 (conseiller juridique) |

## 3. Positionnement

- **Différenciation** :
  - expérience tactile soignée ;
  - mode empreinte, pour les données de santé et confidentielles ;
  - résidence au Canada et en français ;
  - preuves vérifiables avec des outils tiers ;
  - accessibilité SGQRI 008 démontrée par des tests.
- **Première référence** : nos propres applications (SAD, Télésoin, contrat). Ce sont des cas réels, sur des données
  sensibles, qui servent de démonstration commerciale.
- **Promesse à tenir** : la terminologie de [VS-ADR-003](adr/VS-ADR-003-plateforme-de-confiance-operee.md). On ne vend
  ni « signature qualifiée » ni « validité universelle ».
