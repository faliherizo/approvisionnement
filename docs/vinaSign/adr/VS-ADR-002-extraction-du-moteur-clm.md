# VS-ADR-002 — Extraire le moteur de signature du CLM plutôt que le réécrire

- **Statut** : Proposé
- **Date** : 2026-11-22

## Contexte et problème

- **Ce qui existe** : le module CLM du contrat contient un moteur validé.
  - PAdES-B-LTA et CAdES, vérifiés par pyHanko, veraPDF et OpenSSL ;
  - horodatage RFC 3161 et journal chaîné ;
  - portail signataire avec code, rappels HMAC et mesure par enveloppe.
  - Inventaire au § 4 de l'[analyse](../01-Analyse-des-dossiers-et-existant.md).
- **Ce que proposent les dossiers** : tout réécrire.

## Options envisagées

1. **Réécriture complète.** 8 à 12 semaines-personnes de plus, et les validations sont à refaire.
2. **Copier le code tel quel.** Rapide, mais il emporte les dépendances du CLM (trousse, modèles, Salesforce) et la clé
   en fichier PKCS#12.
3. **Extraction guidée par des tests de caractérisation (retenue).**
   - On capture d'abord le comportement observable du moteur du CLM : documents signés, rapports pyHanko et veraPDF,
     attestations.
   - On porte ensuite les classes dans les contextes de VinaSign, en remplaçant la clé de fichier par le port
     `CleDeSignature` (HSM).
   - Les mêmes tests doivent passer contre l'ancien et le nouveau moteur : ce sont des tests de contrat, qui
     garantissent la substitution de Liskov.

## Décision

- **Option 3.**
- **Une copie temporaire** : le CLM garde son moteur tant qu'il n'a pas basculé. La bascule se fait par organisation,
  par un adaptateur VinaSign derrière les ports `FournisseurSignature` et `CachetElectronique` du CLM, qui existent
  déjà.
- **Le retrait de l'ancien moteur** se planifie au plus tard un trimestre après la dernière bascule. On ne maintient
  pas deux moteurs indéfiniment.

## Conséquences

- **Gain** : 6 à 9 semaines-personnes sur le moteur, et les validations externes sont rejouées automatiquement.
- **Exigence** : les bacs à sable du contrat (ICP, horodatage, pyHanko, veraPDF, Mailpit) deviennent des dépendances
  de test de VinaSign. On les copie ou on les publie comme images partagées.
- **IQ** : l'organisation IQ ne bascule qu'avec l'accord d'IQ et après la validation de VinaSign (hébergement,
  ADR-020 du contrat ; niveau de signature, ST-09).
