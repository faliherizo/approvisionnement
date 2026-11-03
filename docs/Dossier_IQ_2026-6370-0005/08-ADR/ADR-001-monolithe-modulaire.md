# ADR-001-monolithe-modulaire — Monolithe modulaire Spring Boot

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : La solution comprend plusieurs domaines cohérents et une intégration externe importante. La complexité opérationnelle d'un parc microservices n'est pas justifiée sans données de charge ou d'organisation. Les modules doivent avoir des frontières claires et contrats internes.
- **Décision** : Adopter un monolithe modulaire au départ plutôt que des microservices systématiques.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Permet transactions locales et livraison simple; extraction ultérieure possible. Risques de couplage à contrôler par tests d'architecture et dépendances explicites.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
