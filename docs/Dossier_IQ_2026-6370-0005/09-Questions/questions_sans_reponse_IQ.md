# Questions encore sans réponse — AO 2026-6370-0005

État au 17 novembre 2026, après les addenda [1](reponses/addenda-1.md), [2](reponses/addenda-2.md) et
[3](reponses/ADDENDA_3_2026-6370-0005_FINAL_REV1.md). Ces questions viennent de
[questions_a_clarifier_IQ.md](questions_a_clarifier_IQ.md) ; les addenda n'y répondent pas, ou seulement en partie.

L'addenda 3 précise qu'IQ a répondu à toutes les questions reçues : celles-ci n'ont donc pas été posées, ou l'ont été
sous une autre forme. **Date limite de dépôt : 4 décembre 2026, 11 h.** Les questions marquées ★ changent le prix ou la
portée : à poser en priorité, avant la date limite des questions fixée par les Documents d'appel d'offres.

La colonne **Hypothèse retenue** indique ce que la soumission présumera si IQ ne répond pas. Chaque hypothèse devra
être déclarée comme telle dans la soumission : selon l'addenda 3, une hypothèse du soumissionnaire n'engage pas IQ.

## 1. Partiellement répondues

| # | Question | Ce que disent les addenda | Ce qui reste à préciser | Hypothèse retenue |
|---|---|---|---|---|
| E8 ★ | RTO, RPO, fréquence et conservation des sauvegardes | Add. 3 Q35 : RTO 8 h, RPO 4 h en heures écoulées | Fréquence et durée de conservation des sauvegardes ; essais de relève exigés | Sauvegarde toutes les 4 h, conservée 35 jours ; essai de relève annuel |
| E11 | Qui réalise l'ÉFVP, et quand ? | Add. 3 Q15 : selon les processus d'IQ ; le soumissionnaire fournit l'information | Gabarit d'IQ et échéance | Dossier d'information fourni au démarrage |
| F2 | Quelles modifications imposent une nouvelle approbation ? | Add. 3 Q26 : une modification substantielle après approbation finale | Définition de « substantielle » ; modifications mineures admises sans reprise | Toute nouvelle version d'un document reprend les validations |
| F3 | « Retour pour correction » : vers qui ? | Add. 3 Q26 : reprise des validations requises | Étape visée par défaut | L'approbateur choisit l'étape ; par défaut, l'initiateur |
| F7 | Règles de montant ou de programme pour les approbateurs | Add. 3 Q36 : Salesforce Flow accepté | Seuils et programmes concernés | Règles conditionnelles configurées par IQ |
| L2 ★ | Facturation des volumes excédentaires, utilisateurs externes, environnements supplémentaires | Add. 3 Q22, Q39 : enveloppe envoyée ; authentification standard incluse | Traitement des volumes au-delà du bordereau ; prix d'un environnement de plus | Prix unitaire par tranche d'enveloppes au-delà du volume annuel |
| O1, O2 | Usages Microsoft 365, bibliothèque SharePoint | Add. 3 Q13 : copie vers SharePoint = intégration distincte si IQ la requiert | Si IQ la requiert, et laquelle | Hors base ; option chiffrée à part |
| P2 | Attributs et groupes transmis par Saviynt | Add. 3 Q51 : mappage des attributs et groupes inclus | Liste des attributs (unité, direction, statut) | `userName`, nom, `active`, groupes ; unité par groupe |
| P3 | Départ : désactivation immédiate, réaffectation des trousses ? | Add. 3 Q51 : désactivation incluse | Réaffectation des trousses et tâches en cours | Désactivation immédiate ; réaffectation par l'administrateur |
| W3, W4, W5 | Origine du document RH, retour dans Workday, signataire interne ou externe | Add. 3 Q25 : document signé **ou** référence durable | Préférence d'IQ ; candidat externe ou SSO | Référence durable ; candidat externe, employé par SSO |

## 2. Sans réponse

### Signature et preuve

| # | Question | Hypothèse retenue |
|---|---|---|
| C2 | Niveaux de signature acceptés selon le type de document et le montant | Niveau d'identité selon le risque de la trousse |
| C5 | Les signataires externes signent-ils sans compte ? | Oui, lien à jeton et vérification d'identité |
| C6 ★ | Formats exigés : PDF/A, PAdES à validation longue (LTV), XML ? | PDF signé PAdES, CAdES détaché, horodatage RFC 3161 ; LTV à confirmer avec l'autorité retenue |
| C7 | Signature visible à un emplacement défini | Zones de signature positionnées dans le modèle |
| C8 | Mandataire ou représentant d'une entreprise : preuve de pouvoir ? | Non traité ; à prévoir si IQ l'exige |
| C9 | Signature manuscrite ou papier en dernier recours | Non prévue |
| C10 | Délégation de signature d'IQ selon les montants | Signataires désignés par étape |

### Annuaire, accès et rôles

| # | Question | Hypothèse retenue |
|---|---|---|
| D2 ★ | Inscription d'application Entra ID et comptes de test fournis par IQ ? | Requis au démarrage pour la recette OIDC ou SAML et SCIM |
| D5 | Séparer l'administration d'affaires de l'administration technique ? | Rôle `ADMIN` unique ; rôles distincts réalisables |
| D7 | Second facteur assuré par l'annuaire, désactivé dans la plateforme pour les comptes SSO ? | Second facteur délégué à Entra ID pour les comptes SSO |
| D8 | Comptes locaux de secours permis ? | Désactivés par défaut, activables |
| D9 | Gestionnaires d'unité : voient-ils toutes les trousses de leur unité ? | Oui, par le domaine d'affaires |

### Données, sécurité et hébergement

| # | Question | Hypothèse retenue |
|---|---|---|
| E2 | Accès d'administration et de soutien exclusivement depuis le Canada ? (add. 3 Q28 : tout accès hors Canada déclaré) | Oui, depuis le Canada |
| E6 | Format d'archivage long terme (PDF/A) et dépôt cible | PDF et Word d'origine, empreintes scellées ; export en lot |
| E7 | Classification des données et mesures par classe | Confidentiel ; chiffrement au repos et en transit |
| E9 ★ | Normes obligatoires et équivalences (ISO 27001, SOC 2 Type II) | Attestations de l'hébergeur ; audit indépendant annuel |
| E10 ★ | Annexe 10 (Excel) et politiques de sécurité citées | Non reçues ; l'addenda 3 (Q19, Q20) confirme leur existence en Excel natif |

### Parcours et règles d'affaires

| # | Question | Hypothèse retenue |
|---|---|---|
| F1 | Quorum des étapes parallèles | Paramétrable par étape |
| F4 | Expiration, annulation, délégation et relance | Invitation 14 jours ; rappels tous les 2 jours, 3 au plus |
| F5 | Visibilité des commentaires | Intervenants de la trousse |
| F6 | Un rejet arrête-t-il la trousse ? | Oui ; un retour vise une étape antérieure |
| F8 | L'initiateur peut-il signer sa propre trousse ? | Signer oui, valider ou approuver non |

### Modèles et documents

| # | Question | Hypothèse retenue |
|---|---|---|
| G1 ★ | Nombre de modèles et de clauses existants, format | Inclus : nombre fixé en ateliers (add. 1 Q7) ; effort unitaire au bordereau |
| G2 | Import des modèles Word en conservant leur mise en page | Corps de texte et variables ; mise en page non reprise |
| G3 | Rédaction, approbation, publication d'un modèle ; double approbation ? | Soumission puis publication par l'administration |
| G5 | Annexes de référence non signées dans une trousse | Oui |
| G6 | Documents de plus de 25 Mo | 25 Mo, réglable |
| G7 | Saisie assistée à partir des référentiels d'IQ | Listes de référence administrables |

### Notifications

| # | Question | Hypothèse retenue |
|---|---|---|
| H1 ★ | Canaux et fournisseur de SMS (lieu de traitement, add. 3 Q28) | Courriel, SMS, notification interne ; fournisseur canadien à désigner |
| H2 | Adresse d'expédition au domaine d'IQ (SPF, DKIM) | Adresse de la plateforme, domaine d'IQ si configuré |
| H3 | Messages aux externes bilingues ? | Langue choisie par signataire (FR par défaut, EN sur demande) |
| H4 | Destinataires des alertes de retard et des copies | Initiateur et copies déclarées |

### Suivi, rapports et IA

| # | Question | Hypothèse retenue |
|---|---|---|
| I1 | Indicateurs attendus et destinataires | Tableau de bord, suivi centralisé, export CSV |
| I2 | Alimentation de Power BI ou d'un entrepôt | Exports CSV ; connecteur en option |
| I3 | IA : modèle hébergé par le fournisseur permis ? Modèle infonuagique interdit ? | Modèle exécuté dans l'environnement de la plateforme, au Canada |
| I4 | Analyses IA prioritaires | Classification, extraction, anomalies, validation humaine |

### Exploitation et niveaux de service

| # | Question | Hypothèse retenue |
|---|---|---|
| J1 ★ | Grille de niveaux de service : cahier ou document principal ? | La plus exigeante des deux |
| J2 | Définition de la disponibilité (exclusions, maintenance, tiers) | 99,5 % hors maintenance annoncée |
| J3 | Crédits de service et pénalités | À convenir |
| J4 | Soutien de premier niveau gardé par IQ | Deuxième et troisième niveaux par le fournisseur |

### Livraison, gouvernance et migration

| # | Question | Hypothèse retenue |
|---|---|---|
| K1 ★ | Échéancier cible et mise en production | Démarrage à l'adjudication ; production en 16 semaines |
| K2 | Qui approuve modèles, parcours, contrôles et critères d'acceptation ? | Comité d'IQ désigné au démarrage |
| K3 | Livrables par jalon et formats | Documentation, guides, rapports de tests, en français |
| K4 | Migration de documents ou dossiers en cours | Aucune |
| K5 | Rôles d'IQ au transfert de connaissances | Administrateurs d'affaires et techniques |

### Contrat et tarification

| # | Question | Hypothèse retenue |
|---|---|---|
| L4 | Coûts de sortie, restitution, export massif | Export complet inclus (export en lot, dossiers de preuve) |
| L7 | Propriété des modèles, clauses et paramètres créés par IQ | IQ |

### Accessibilité et image

| # | Question | Hypothèse retenue |
|---|---|---|
| M1 | Niveau d'accessibilité, navigateurs et appareils | SGQRI 008 3.0 / WCAG 2.2 AA ; navigateurs récents, téléphone, tablette, poste |
| M3 | Image de marque et sous-domaine | Nom et logo d'IQ sur le portail du signataire |

### Workday, provisionnement, signature en lot

| # | Question | Hypothèse retenue |
|---|---|---|
| W6 ★ | Locataire Workday de test et compte d'intégration | Requis au démarrage (add. 3 Q50 : le fournisseur réalise le côté Workday) |
| P4 | Comptes SCIM : connexion par SSO uniquement ? | Oui ; aucun mot de passe local exploitable |
| V1 | Taille et fréquence des lots | 500 dossiers par lot |
| V2 | Document identique ou propre à chaque dossier | Propre à chaque dossier, même modèle |
| V3 | Approbation globale d'un lot avant envoi | Chaque trousse suit son parcours |

## 3. Nouvelles questions soulevées par l'addenda 3

| # | Question | Pourquoi |
|---|---|---|
| N1 ★ | Q39 : une enveloppe **renvoyée après expiration** de l'invitation compte-t-elle de nouveau ? | La solution ne la recompte pas ; à confirmer pour la facturation |
| N2 | Q33 : le préavis est-il de 90 jours **ouvrables** ou de 90 jours ? | Le texte dit « 90 jour ouvrable » |
| N3 ★ | Q38 : environnement et données exigés pour l'essai de 500 utilisateurs concurrents (production d'IQ, préproduction, données synthétiques) | Fixe le coût de l'essai |
| N4 | Q48 : le certificat de destruction doit-il être signé par une personne d'IQ en plus du cachet de la plateforme ? | Le certificat actuel est scellé par le cachet |
| N5 | Q51 : Saviynt pousse-t-il des **groupes** SCIM, ou seulement des utilisateurs avec un attribut de rôle ? | Détermine le mappage (groupe ou attribut) |
| N6 | Q43 : Entra ID émettra-t-il les groupes par identifiant d'objet ou par nom (`groups` vs nom de groupe synchronisé) ? | Les correspondances acceptent l'un ou l'autre ; à fixer pour la configuration |
