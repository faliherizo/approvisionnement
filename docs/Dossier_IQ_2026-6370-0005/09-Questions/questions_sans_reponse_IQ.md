# Questions encore sans réponse — AO 2026-6370-0005

État au 19 novembre 2026, après les addenda [1](reponses/addenda-1.md), [2](reponses/addenda-2.md),
[3](reponses/ADDENDA_3_2026-6370-0005_FINAL_REV1.md) et [4](reponses/addenda-4.md). Les questions viennent de
[questions_a_clarifier_IQ.md](questions_a_clarifier_IQ.md). L'addenda 4 répond à la plupart des questions C, D, E, F,
G, J, L, O, P, V et W. Il demande de **signaler sans délai** toute question restée sans réponse ou mal répondue.
**Dépôt : 4 décembre 2026, 11 h.**

La colonne **Hypothèse retenue** indique ce que la soumission présumera si IQ ne répond pas. Chaque hypothèse sera
déclarée comme telle : une hypothèse du soumissionnaire n'engage pas IQ (addenda 3, § 1).

## 0. À retransmettre à IQ sans délai

Les questions qui changent le prix, la portée ou la conformité, à renvoyer avec leur référence (addenda 4, § 5) :

| # | Question à retransmettre | Pourquoi elle compte |
|---|---|---|
| N1 | Une enveloppe dont l'invitation est **renvoyée après expiration** est-elle facturée de nouveau ? (add. 3 Q39) | Base de facturation |
| N2 | Le préavis des changements majeurs est-il de **90 jours ouvrables** ou de 90 jours ? (add. 3 Q33 : « 90 jour ouvrable ») | Engagement contractuel |
| N3 | Pour l'essai de **500 utilisateurs concurrents**, quel environnement et quelles données IQ exige-t-elle ? (add. 3 Q38) | Coût de l'essai |
| N5 | Saviynt pousse-t-il des **groupes** SCIM, ou des utilisateurs avec un attribut de rôle ? (add. 3 Q51, add. 4 Q31) | Mappage à configurer, coût de mise en service |
| E10 | Les colonnes destinées au soumissionnaire de l'**Annexe 10** sont-elles toutes à remplir pour chaque exigence, y compris celles hors de notre périmètre ? | Conformité du dépôt |
| G3 | Les modèles doivent-ils passer par une **double approbation** (affaires et juridique) avant publication ? | Parcours de publication des modèles |
| K1 | Échéancier cible et date de mise en production souhaitée | Plan de mise en œuvre et prix |

## 1. Répondues par l'addenda 4

| # | Réponse (addenda 4) | Suite dans la solution |
|---|---|---|
| C2 | Niveaux configurables par type de document, processus, règles (Q1) | RST-35 |
| C5 | Signature externe sans compte permanent (Q3) | ✅ |
| C6 | Formats PDF, validation à long terme, exports à préciser (Q4) | PAdES-B-LTA, CAdES, RFC 3161 ✅ (RST-37) ; autorité d'horodatage à désigner |
| C7 | Signature visible à un emplacement configurable (Q5) | ✅ |
| C8 | Représentant autorisé, preuve d'autorité et qualité tracées (Q6) | RST-32 |
| C9 | Papier : pas une exigence (Q7) | — |
| C10 | Délégation interne configurée par parcours conditionnels (Q8) | RST-36 |
| D2 | Comptes et environnements de test fixés au démarrage (Q9) | Prérequis déclarés |
| D5 | Administration d'affaires et technique séparées (Q10) | RST-31, ADR-017 |
| D8 | Comptes de secours justifiés, approuvés, sans contournement (Q12) | ✅ RST-39 : réservation par organisation, second facteur, journal ; approbation des comptes par IQ hors plateforme |
| E2 | Tout accès administratif ou de soutien déclaré avec son territoire (Q13) | Tableau des lieux |
| E6 | PDF/PDF-A et export massif (Q14) | Export en lot ✅ ; PDF/A-2b des documents produits ✅ (RST-37) ; PDF déposés : conformité selon leur source |
| E8 | Sauvegardes à décrire, RTO 8 h et RPO 4 h (Q15) | Plan de relève `infra/dr` |
| E9 | Certifications et attestations à déclarer (Q16) | Soumission |
| F1 | Quorum configurable (Q17) | ✅ |
| F2, F3, F6 | Retour, reprise d'étapes, fin sur rejet ; validations après modification substantielle (Q18) | ✅ |
| F4 | Délais, rappels, expirations, annulations, délégations configurables (Q19) | ✅ |
| F7 | Parcours selon montant, type, programme, unité, rôle (Q20) | RST-36 |
| G1 | Nombre de modèles inclus et effort additionnel à préciser (Q21) | Bordereau |
| G2 | Modèles Word d'IQ, structure et présentation préservées dans la mesure du possible (Q22) | ✅ RST-38 : gabarit Word d'origine fusionné, PDF par LibreOffice ; fidélité limitée aux fonctions Word non rendues par LibreOffice |
| G6 | Limites de taille à indiquer, ajustables (Q23) | ✅ |
| J1, J3 | Pas de réduction des niveaux de service ; recours du contrat (Q24, Q25) | Soumission |
| L2, L5, L8 | Tous les coûts, tiers et sous-traitants déclarés (Q26 à Q28) | Bordereau |
| O1, O2 | Microsoft 365 décrit si proposé ; SharePoint fixé en conception (Q32, Q33) | Soumission |
| P2 | Mappage des attributs et groupes établi en conception (Q31) | RST-34 |
| V1, V2, V3 | Capacité de lot à déclarer ; documents individualisés ; approbation de lot (Q34 à Q36) | RST-33 |
| W3, W4, W6 | Architecture Workday, document ou référence ; environnement au démarrage (Q29, Q30) | Soumission |

## 2. Encore partiellement répondues

| # | Ce qui reste à préciser | Hypothèse retenue |
|---|---|---|
| P3 | Réaffectation des trousses et tâches d'un employé qui part | Désactivation immédiate ; ✅ transfert en une opération par l'administration d'affaires (RST-41) |
| W5 | Candidat ou employé : signataire externe (lien) ou interne (SSO) ? | Candidat externe, employé par SSO |
| H1 | Fournisseur de SMS et son lieu de traitement | Fournisseur canadien à désigner |

## 3. Sans réponse, sans effet sur le prix

| # | Question | Hypothèse retenue |
|---|---|---|
| D7 | Second facteur assuré par Entra ID pour les comptes SSO ? | Oui ; pas de second facteur de la plateforme pour ces comptes |
| D9 | Gestionnaires d'unité : toutes les trousses de leur unité ? | Oui, par le domaine d'affaires |
| E7 | Classification des données et mesures par classe | Confidentiel ; chiffrement au repos et en transit |
| F5 | Visibilité des commentaires | Intervenants de la trousse |
| F8 | L'initiateur peut-il signer sa propre trousse ? | Signer oui, valider ou approuver non |
| G5 | Annexes de référence non signées dans une trousse | Oui |
| G7 | Saisie assistée depuis les référentiels d'IQ | Listes de référence administrables |
| H2 | Adresse d'expédition au domaine d'IQ (SPF, DKIM) | Domaine d'IQ si configuré |
| H4 | Destinataires des alertes de retard et des copies | Initiateur et copies déclarées |
| I1 à I4 | Indicateurs, Power BI, hébergement du modèle IA, analyses IA prioritaires | Tableau de bord et CSV ; IA dans l'environnement de la plateforme, au Canada |
| J2, J4 | Définition de la disponibilité ; soutien de premier niveau | 99,5 % hors maintenance annoncée ; 2e et 3e niveaux par le fournisseur |
| K2 à K5 | Approbations, livrables, migration, transfert de connaissances | Comité d'IQ ; livrables en français ; aucune migration |
| L4, L7 | Coûts de sortie ; propriété des modèles | Export complet inclus ; IQ propriétaire |
| M1, M3 | Accessibilité et appareils ; image de marque | SGQRI 008 3.0 / WCAG 2.2 AA ; logo d'IQ au portail |
| N4 | Certificat de destruction signé aussi par une personne d'IQ ? | Scellé par le cachet de la plateforme |
| N6 | Groupes Entra ID par identifiant d'objet ou par nom ? | Les deux acceptés par les correspondances |
| P4 | Comptes SCIM : SSO seulement ? | Oui, aucun mot de passe local exploitable |
