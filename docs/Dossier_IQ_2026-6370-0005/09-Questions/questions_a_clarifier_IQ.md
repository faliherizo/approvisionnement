# Questions à adresser à Investissement Québec

## A. Périmètre et priorités
1. Le périmètre de base est-il limité à la génération documentaire ciblée et à la signature, ou un CLM complet est-il attendu dès la première livraison?
2. Le scénario 10 de démonstration évoque une gestion bout en bout d'un CLM alors que le cahier précise que le CLM complet n'est pas requis sauf mention expresse. Quelles capacités CLM sont obligatoires, optionnelles ou hors périmètre?
3. Quels processus contractuels doivent être déployés en premier (aide financière, ententes, lettres, autres)?
4. Quels sont les volumes mensuels et les pics, au-delà de l'estimation annuelle de 55 000 à 65 000 documents?

## B. Salesforce Grantmaking
5. Quels objets Salesforce, champs, relations et événements doivent être lus/écrits?
6. Quelle version / configuration de Salesforce Public Sector Solutions / Grantmaking est utilisée?
7. Quels mécanismes sont autorisés : REST, Platform Events, Change Data Capture, webhook, middleware?
8. Quel système est maître pour le statut contractuel, le document signé et les métadonnées?
9. Les fichiers signés doivent-ils être stockés dans Salesforce, dans la solution ou dans un référentiel documentaire tiers?
10. Quels environnements Salesforce (sandbox/UAT/prod), comptes techniques et jeux de données seront fournis?

## C. Workflow
11. Les règles de quorum des étapes parallèles sont-elles « tous », « un parmi », majorité ou paramétrables?
12. Qui peut modifier les signataires/validateurs après lancement et quelles modifications imposent une nouvelle approbation?
13. Que signifie précisément « retour pour correction » : à l'initiateur, à un validateur antérieur ou à une étape choisie?
14. Quelles règles d'expiration, d'annulation, de délégation et de relance s'appliquent?
15. Les commentaires sont-ils visibles par tous les participants ou restreints par rôle?

## D. Signature et identité
16. Quels niveaux de signature électronique sont acceptés selon types de documents et montants?
17. Quelles méthodes d'authentification sont requises pour internes et externes (SSO, courriel, SMS, MFA, vérification d'identité)?
18. Un fournisseur de signature est-il déjà privilégié ou autorisé? Les certificats et preuves doivent-ils être produits par la plateforme ou tiers?
19. Les signataires externes peuvent-ils signer sans créer de compte?
20. Quels formats de certificat, preuve, horodatage et export sont exigés?

## E. Données, sécurité et hébergement
21. Le Cahier autorise sous conditions certaines juridictions hors Québec, tandis que le document principal indique hébergement et traitement au Canada. Quelle clause et quelle localisation minimale prévalent?
22. Les accès d'administration et de soutien doivent-ils être exclusivement depuis le Canada?
23. Quelle classification des données, rétention, gel légal, destruction et archivage sont applicables?
24. Quels RTO/RPO, fréquence de sauvegarde et durée de conservation des sauvegardes sont exigés?
25. Quelles normes / certifications sont obligatoires, lesquelles sont acceptées comme équivalentes (ISO 27001, SOC 2 Type II)?
26. L'Annexe 10 Excel et les politiques de sécurité référencées peuvent-elles être fournies?
27. Quel niveau d'accessibilité numérique et quels navigateurs/appareils doivent être officiellement supportés?

## F. Exploitation et SLA
28. En cas de divergence entre les SLA du Cahier (critique/majeure/moyenne/mineure) et ceux du document principal (priorités 1 à 5), quelle grille contractuelle doit être appliquée?
29. Quelle définition de disponibilité est utilisée (période, exclusions, maintenance planifiée, fournisseur tiers)?
30. Quels crédits de service, pénalités et mécanismes de mesure sont applicables?
31. Quel processus de support de premier niveau IQ souhaite conserver en interne?

## G. Livraison et gouvernance
32. Quel est l'échéancier cible et la date souhaitée de mise en production?
33. Qui approuve les modèles, clauses, workflows, contrôles de sécurité et critères UAT?
34. Quels livrables sont requis pour chaque jalon et sous quels formats?
35. Quelles règles de migration de documents ou de modèles existants sont attendues?
36. Quels rôles IQ doivent participer au transfert de connaissances et à l'administration autonome?

## H. Tarification et options
37. Quelle unité de facturation doit être comparée (document, envoi, trousse, transaction, signataire)?
38. Comment sont facturés les volumes excédentaires, utilisateurs externes et environnements supplémentaires?
39. Confirmer que CLM avancé et CLM/IA sont des options distinctes et non incluses dans le prix obligatoire.
40. Existe-t-il des coûts de sortie, restitution, export massif ou assistance à la transition?
