# ADR-002-api-rest — API REST versionnée et BFF React

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Le cahier demande des API/connecteurs et intégrations SaaS/on-premise; REST est une proposition standard interopérable.
- **Décision** : Exposer des API REST JSON versionnées, avec BFF si les parcours front l'exigent.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : OpenAPI, DTO versionnés, validation serveur, pagination, idempotence; contrats d'API documentés.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
