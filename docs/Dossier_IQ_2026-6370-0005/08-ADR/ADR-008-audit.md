# ADR-008-audit — Journal d'audit append-only et preuve immuable

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Piste d'audit complète, intègre, horodatée et exportable exigée pour valeur probante.
- **Décision** : Séparer les événements d'audit du journal applicatif; protéger les preuves et documents signés contre altération.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Stockage immuable/WORM ou contrôles équivalents; hash, horodatage, accès restreint, export vérifiable.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
