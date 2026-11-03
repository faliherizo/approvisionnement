# ADR-003-stockage-documents — Base relationnelle + stockage objet

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Les documents peuvent être volumineux (25 Mo minimum) et nécessitent portabilité, versioning et conservation de preuve.
- **Décision** : Stocker métadonnées et états dans une base relationnelle; fichiers dans un stockage objet chiffré et versionné.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Transactions distribuées à éviter; orchestrer via états/outbox; calculer SHA-256 et conserver références immuables.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
