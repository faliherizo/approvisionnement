# ADR-006-workflow — Moteur de workflow configurable

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Les workflows séquentiels, parallèles, mixtes et modifications en cours de processus sont requis.
- **Décision** : Modéliser étapes, assignations, branches, quorum et transitions comme configuration versionnée.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Chaque exécution référence une version publiée; changements en cours sont contrôlés et auditables.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
