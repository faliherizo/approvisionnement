# VS-ADR-003 — Plateforme de confiance opérée par Vina, ancrée sur une autorité reconnue

- **Statut** : Proposé
- **Date** : 2026-11-22
- **Remplace** : ADR-005 du dossier 1 (fournisseur spécialisé pour la confiance avancée)

## Contexte et problème

- **Nouvelle orientation** : Vina ne passe plus par un fournisseur de signature. VinaSign est la plateforme tierce qui
  gère et exécute la signature.
- **La question** : jusqu'où Vina opère-t-elle la confiance elle-même ?

## Facteurs de décision

- **Valeur probante reconnue hors de Vina** : un vérificateur tiers (Adobe, pyHanko, un tribunal) doit pouvoir
  remonter à une racine qu'il reconnaît.
- **Protection des clés** : une clé privée ne doit jamais être exportable.
- **Coût et délai** : une autorité de certification exige politique de certification, audits et infrastructure
  hors ligne.
- **Réversibilité** : changer d'autorité partenaire sans changer le produit.

## Options envisagées

1. **Fournisseur de signature tiers** (DocuSign, etc.). Contraire à l'orientation, et la marge comme la maîtrise
   partent chez le fournisseur.
2. **Vina autorité de certification.** Hors de portée avant plusieurs années : audits, exploitation hors ligne,
   reconnaissance des listes de confiance.
3. **Vina opère la plateforme ; les clés vivent dans un HSM ; les certificats viennent d'une autorité reconnue
   (retenue).**

## Décision

- **Rôle de Vina** : elle opère l'enveloppe, le parcours, l'authentification des signataires, la signature
  cryptographique, l'horodatage, la conservation et la vérification de la preuve.
- **Clés** : générées et utilisées **dans un HSM**, jamais exportées.
  - MVP : Azure Key Vault Premium (clés protégées par HSM, région Canada).
  - V1 commerciale : Managed HSM mono-locataire.
  - Le port `CleDeSignature` isole le choix ; un adaptateur PKCS#12 reste réservé au développement.
- **Certificats** : celui du cachet de chaque organisation cliente, et en V2 les certificats individuels pour la
  signature « avancée », sont émis par une autorité de certification reconnue partenaire. Notarius, au Québec, est à
  évaluer ; le choix est à faire en phase 0.
- **Horodatage** : une autorité d'horodatage externe reconnue, avec une autorité de repli. En V2, une autorité
  d'horodatage Vina adossée au HSM est envisageable, avec un certificat émis par l'autorité partenaire.
- **Niveaux de signature offerts** :

  | Niveau | Parcours | Preuve produite |
  |---|---|---|
  | Simple | Lien, consentement, tracé ou nom | Cachet PAdES-B-LTA de l'organisation, dossier de preuve |
  | Renforcé | Plus une seconde preuve (code, clé d'accès, assertion de l'application cliente avec seconde preuve) | Idem, avec le niveau d'authentification attesté (CAdES) |
  | Avancé (V2) | Identité vérifiée, certificat individuel émis à la volée par l'autorité partenaire | Signature PAdES personnelle et horodatage |

## Conséquences

- **La promesse commerciale doit rester exacte** : « signature électronique opérée par Vina, preuves vérifiables
  avec des outils tiers ». Elle n'est pas une « signature qualifiée » : la qualification juridique (LCCJTI, art. 2827
  C.c.Q.) est à valider par un conseiller juridique québécois avant toute publicité.
- **Contrat avec l'autorité partenaire** : cachets par organisation, certificats à la volée en V2, conditions de
  révocation.
- **Coûts récurrents** : HSM, certificats et horodatage, à refacturer dans les plans SaaS.
- **Certifications à préparer** pour la vente : SOC 2 Type II, ISO 27001, puis éventuellement une évaluation selon
  les normes ETSI de prestataire de services de confiance.
