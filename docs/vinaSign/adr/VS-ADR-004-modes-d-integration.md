# VS-ADR-004 — Modes d'intégration : API, webhooks, interface intégrée en iframe, SDK minces

- **Statut** : Proposé
- **Date** : 2026-11-22

## Contexte et problème

- **Des piles différentes** : React et Java 17 pour le contrat, Angular et Java 21 pour Télésoin, Node pour plusieurs
  applications, .NET pour quelques-unes.
- **Des parcours différents** : un professionnel connecté signe une note, un usager signe un consentement sur une
  tablette, des signataires externes reçoivent un courriel.

## Options envisagées

1. **Composants d'interface par framework** (Angular, React) qui embarquent la logique. Maintenance multipliée, et
   des règles de sécurité côté client.
2. **Web Component unique.** Isolation incomplète : la page hôte voit le DOM du signataire.
3. **Iframe d'une origine VinaSign distincte et petit SDK de navigateur sans framework (retenue)**, avec une API
   serveur et des webhooks.

## Décision

- **API REST v1** (OpenAPI est le contrat de référence), authentifiée par OAuth 2.0 client credentials.
  - Jetons JWT de courte durée, avec audience et scopes.
  - Authentification du client par `private_key_jwt` ; un secret partagé n'est accepté qu'en bac à sable.
  - mTLS en option pour les clients entreprise.
- **Clé d'idempotence** obligatoire sur toute création ou finalisation, et `externalReference` unique par
  application.
- **Webhooks** :
  - signés (HMAC-SHA256 avec horodatage, rotation de secret à deux clés actives) ;
  - émis par une boîte d'envoi transactionnelle ;
  - reprise avec délai croissant, journal des livraisons, contrôle anti-SSRF des destinations.
- **Interface intégrée** :
  - `@vinasign/embed`, environ 5 Ko sans dépendance, ouvre une iframe sur l'origine du signataire avec une session
    éphémère ;
  - les messages `postMessage` sont typés, avec contrôle strict de l'origine dans les deux sens.
  - Les applications React et Angular l'enveloppent en dix lignes.
- **SDK serveur minces**, générés à partir d'OpenAPI et complétés par :
  - la vérification des webhooks ;
  - l'aide au calcul d'empreinte PDF ([VS-ADR-005](VS-ADR-005-signature-d-empreinte.md)) ;
  - en Java, un module d'autoconfiguration Spring Boot.
  - Aucune règle de sécurité dans les SDK.
- **Connecteur MCP en lecture seule**, porté du CLM (ADR-023 du contrat), pour les assistants d'IA.

## Conséquences

- **Une seule implémentation** de l'expérience du signataire, sur une origine qui porte sa propre CSP.
- **Le SDK Java vise Java 17** pour le contrat, et il est testé en 17 et en 21.
- **Les tests de contrat OpenAPI** sont exécutés contre chaque SDK.
