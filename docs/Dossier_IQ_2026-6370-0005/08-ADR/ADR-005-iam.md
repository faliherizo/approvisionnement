# ADR-005-iam — SSO interne et identité externe séparée

- **Statut** : proposé — à approuver par architecture, sécurité et propriétaire métier IQ.
- **Contexte** : Le cahier demande SSO et authentification adaptée aux signataires internes/externes et au risque.
- **Décision** : Fédérer les utilisateurs IQ via IdP; gérer les invités/signataires externes par un parcours à assurance variable.
- **Justification** : choix de conception aligné sur les capacités demandées sans supposer une technologie imposée par le DAO.
- **Conséquences** : Ne pas imposer un mécanisme non documenté; MFA et preuve d'identité configurables selon classification.
- **Alternatives considérées** : solution monolithique non modulaire / microservices dès le départ / service tiers spécialisé; choix à réévaluer selon coûts, capacités natives de la plateforme et contraintes contractuelles.
- **Validation requise** : confirmer avec IQ, éditeur de la solution et sécurité.
