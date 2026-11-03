# ADR-007-resilience — Outbox, idempotence et reprise

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Les documents exigent gestion sécurisée des erreurs d'intégration, reprises et transactions incomplètes.
- **Décision** : Utiliser outbox transactionnelle, clés d'idempotence, retries avec backoff et file d'exception.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Prévenir double génération/signature; visibilité des erreurs et reprise opérateur.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
