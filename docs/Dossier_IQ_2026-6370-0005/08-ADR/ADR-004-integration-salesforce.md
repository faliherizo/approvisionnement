# ADR-004-integration-salesforce — Adaptateur Salesforce isolé

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Salesforce Grantmaking est explicitement le système maître de données de dossiers d'aide financière; API et flux exacts restent à confirmer.
- **Décision** : Créer un adaptateur d'intégration dédié plutôt que disperser les appels Salesforce dans les services métier.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Mapping isolé, idempotence, retries, réconciliation, tests contractuels et sandbox.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
