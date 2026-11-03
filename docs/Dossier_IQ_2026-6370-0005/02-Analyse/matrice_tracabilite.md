# Matrice de traçabilité exigences → stories → tests

| Exigence | Description synthétique | Story(s) | Vérification principale |
|---|---|---|---|
| F-01 | Dossiers, trousses, documents multiples | ST-03, ST-04 | Tests API, droits, E2E |
| F-02 | Génération de contrats depuis données source | ST-05, ST-06 | Tests fusion, données, comparaison PDF |
| F-03 | Modèles réutilisables et partagés | ST-05 | Tests droits, version modèle |
| F-04 | Champs et règles conditionnelles | ST-05, ST-06 | Tests validation et rendu |
| F-05 | Validation et approbation séquentielle/parallèle/mixte | ST-07, ST-08 | Tests transitions et concurrence |
| F-06 | Signature et gestion des signataires | ST-09 | Tests intégration, identité, callbacks |
| F-07 | Notifications et rappels | ST-11 | Tests planification et envoi |
| F-08 | Suivi centralisé | ST-13 | Tests filtres, visibilité, statut |
| F-09 | Versions, audit, preuves, export | ST-04, ST-10, ST-14 | Tests hash, immutabilité, export |
| F-10 | Salesforce et API | ST-06, ST-12 | Tests contrat, idempotence, reprise |
| F-11 | SSO, rôles, groupes, administration | ST-01, ST-02, ST-14 | Tests sécurité et séparation |
| F-12 | Web/mobile, expérience utilisateur | ST-03 à ST-13 | Tests responsive et utilisabilité |
| F-13 | IA / CLM optionnel | ST-15 | POC, sécurité, contrôle humain |
| NF-01 | Hébergement / résidence des données | ST-14 / DevOps | Preuves de localisation et audit fournisseur |
| NF-03 | Sécurité | ST-01, ST-02, ST-09, ST-14 | Tests sécurité, audit indépendant |
| NF-04 | Auditabilité | ST-08 à ST-10, ST-14 | Vérification chronologie et intégrité |
| NF-05 | Disponibilité / SLA | ST-14 | Supervision et rapports SLA |
| NF-06 | 55–65k documents/an | ST-04, ST-06, ST-09 | Test de charge |
| NF-07 | 25 Mo minimum | ST-04 | Test upload 25 Mo+ |
| NF-13 | Portabilité | ST-10 | Export / import de contrôle |

## Sources documentaires
- Cahier des besoins et exigences, sections 1.4.2 à 1.4.12, 2 à 6.
- Document d'appel d'offres, sections 3.1 à 3.10, 4, annexes 2, 9 et 10.
- Les identifiants F/NF et ST sont construits dans le présent dossier; ils ne remplacent pas la matrice officielle Excel Annexe 10.
