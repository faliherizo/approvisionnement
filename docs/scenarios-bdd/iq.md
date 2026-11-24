# Scénarios BDD — Investissement Québec — AO 2026-6370-0005

*Solution de signature électronique et de génération de contrats.* Source des exigences : `docs/Dossier_IQ_2026-6370-0005/02-Analyse/conformite_exigences_fonctionnelles.md`.

> Document généré par `bdd/outils/documenter.mjs` depuis le catalogue, les fonctionnalités Gherkin et les cas JSON ;
> ne pas le modifier à la main. Régénérer : `cd bdd && npm run documenter`.

## Synthèse

| Exigences | Éprouvées par scénario | Dont réussies à la dernière exécution | Documentaires | Écarts déclarés | Sans objet |
|---|---|---|---|---|---|
| 140 | 132 | 132 | 8 | 0 | 0 |

## Matrice des exigences

| Exigence | Libellé | Caractère | Vérification | Scénarios |
|---|---|---|---|---|
| `IQ-C-01` | Gérer le cycle de validation, d'approbation et de signature, orchestration comprise | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-C-02` | Contexte multi-juridictionnel, solution conforme, modulaire, évolutive | Obligatoire | Documentaire | Principe général (contexte multi-juridictionnel, modularité) : démontré par l'ensemble des scénarios et des modules activables. |
| `IQ-C-03` | Service de signature appelable, distinct de l'orchestration (couplage lâche) | Principe souhaité | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe)<br>✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-C-04` | Capacités avancées du cycle de vie des contrats | Optionnelle (addenda § 4.3) | Scénario BDD exécuté | ✅ [L'analyse automatisée d'un document est soumise à une révision humaine obligatoire](#lanalyse-automatisee-dun-document-est-soumise-a-une-revision-humaine-obligatoire) |
| `IQ-C-05` | Offerte en infonuagique, connectable aux SaaS | Obligatoire | Documentaire | Mode infonuagique : hébergement Azure (infrastructure). |
| `IQ-C-06` | Connexion aux applications « on premise » par API | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-C-07` | Protection et confidentialité des données | Obligatoire | Scénario BDD exécuté | ✅ [Les données d'IQ sont introuvables pour une autre organisation](#les-donnees-diq-sont-introuvables-pour-une-autre-organisation) |
| `IQ-C-08` | Échange avec les systèmes de référence : préalimenter, retourner les statuts | Obligatoire | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-C-09` | Récupération automatisée des données Salesforce Grantmaking | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-10` | Génération automatisée des contrats depuis les données Salesforce | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-11` | Préremplissage des modèles approuvés | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-12` | Acheminement des documents générés vers validation, approbation, signature | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-13` | Retour vers Salesforce des informations de signature | Obligatoire | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-C-14` | Traçabilité dossier source → document → preuve | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-15` | Erreurs d'intégration, reprises, transactions incomplètes | Obligatoire | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-C-16` | Interfaces standard, documentées et supportées par l'éditeur | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque)<br>✅ [Les interfaces sont standards et documentées (OpenAPI, SCIM)](#les-interfaces-sont-standards-et-documentees-openapi-scim) |
| `IQ-C-17` | Flexibilité et agilité tout au long de l'utilisation | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-18` | Circuits de validation et d'approbation séquentiels, parallèles, mixtes | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise)<br>✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours) |
| `IQ-C-19` | Attribution selon rôles, groupes ou règles d'affaires | Obligatoire | Scénario BDD exécuté | ✅ [Attribution par rôle et par groupe, étape conditionnelle selon le risque, simulation avant lancement](#attribution-par-role-et-par-groupe-etape-conditionnelle-selon-le-risque-simulation-avant-lancement) |
| `IQ-C-20` | Ajout, retrait, remplacement d'un validateur ou approbateur en cours | Obligatoire | Scénario BDD exécuté | ✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours) |
| `IQ-C-21` | Réassignation d'une étape sans redémarrage | Obligatoire | Scénario BDD exécuté | ✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours) |
| `IQ-C-22` | Approuver, rejeter, retourner pour correction | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise)<br>✅ [Un rejet exige un motif et arrête le parcours](#un-rejet-exige-un-motif-et-arrete-le-parcours) |
| `IQ-C-23` | Commentaires et historique des validations | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-C-24` | Notifications et rappels configurables pour validateurs et approbateurs | Obligatoire | Scénario BDD exécuté | ✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-C-25` | Suivi de l'état des validations en cours | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise)<br>✅ [Suivi centralisé paginé côté serveur, filtres, exceptions et export ; tableau de bord](#suivi-centralise-pagine-cote-serveur-filtres-exceptions-et-export-tableau-de-bord) |
| `IQ-C-26` | Signature séquentielle, parallèle, mixte | Obligatoire | Scénario BDD exécuté | ✅ [Signature séquentielle puis signature conjointe d'un groupe de signataires](#signature-sequentielle-puis-signature-conjointe-dun-groupe-de-signataires) |
| `IQ-C-27` | Groupes de signataires | Obligatoire | Scénario BDD exécuté | ✅ [Signature séquentielle puis signature conjointe d'un groupe de signataires](#signature-sequentielle-puis-signature-conjointe-dun-groupe-de-signataires) |
| `IQ-C-28` | Signature simultanée et consécutive configurable | Obligatoire | Scénario BDD exécuté | ✅ [Signature séquentielle puis signature conjointe d'un groupe de signataires](#signature-sequentielle-puis-signature-conjointe-dun-groupe-de-signataires) |
| `IQ-C-29` | Association automatique des étapes aux signataires | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-30` | Modification et ajout de signataires en cours de processus | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire est ajouté puis remplacé en cours de parcours ; une nouvelle version de document est tracée](#un-signataire-est-ajoute-puis-remplace-en-cours-de-parcours-une-nouvelle-version-de-document-est-tracee) |
| `IQ-C-31` | Réassignation de signataires sans reprise complète | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire est ajouté puis remplacé en cours de parcours ; une nouvelle version de document est tracée](#un-signataire-est-ajoute-puis-remplace-en-cours-de-parcours-une-nouvelle-version-de-document-est-tracee) |
| `IQ-C-32` | Modèles réutilisables | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-33` | Partage des modèles par groupe d'usagers | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-34` | Ancrage précis des champs | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-35` | Types : texte validé, liste, date, case à cocher | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-36` | Distinguer champs de formulaire, de saisie et de signature | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-37` | Attribution des champs à des signataires | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-38` | Règles conditionnelles d'affichage | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-C-39` | Saisie assistée (suggestions) | Obligatoire | Scénario BDD exécuté | ✅ [La saisie est assistée par les valeurs déjà retenues](#la-saisie-est-assistee-par-les-valeurs-deja-retenues) |
| `IQ-C-40` | Notifications activables et désactivables | Obligatoire | Scénario BDD exécuté | ✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-C-41` | Rappels automatiques de non-réponse | Obligatoire | Scénario BDD exécuté | ✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-C-42` | Destinataires en copie | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-C-43` | Vue de suivi centralisée : étapes, attentes, exceptions | Obligatoire | Scénario BDD exécuté | ✅ [Suivi centralisé paginé côté serveur, filtres, exceptions et export ; tableau de bord](#suivi-centralise-pagine-cote-serveur-filtres-exceptions-et-export-tableau-de-bord) |
| `IQ-C-44` | Plusieurs documents par trousse | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-C-45` | Dossiers, documents, versions, intervenants | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-C-46` | PDF et Word | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-C-47` | Téléversement d'au moins 25 Mo | Obligatoire | Scénario BDD exécuté | ✅ [Un document de 25 Mo est accepté ; au-delà du plafond configuré, il est refusé](#un-document-de-25-mo-est-accepte-au-dela-du-plafond-configure-il-est-refuse) |
| `IQ-C-48` | Historique des étapes de validation, d'approbation, de signature | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-C-49` | Journal d'audit des actions (consultation, validation, signature, rejet, annulation…) | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-C-50` | État et historique des transactions d'un document | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-C-51` | Conservation et consultation du dossier de preuve | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-C-52` | Minimiser le nombre d'étapes | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-C-53` | Exécution fluide web et mobile | Obligatoire | Scénario BDD exécuté | ✅ [Consultation et décision sur téléphone, sans débordement ni violation d'accessibilité](#consultation-et-decision-sur-telephone-sans-debordement-ni-violation-daccessibilite) |
| `IQ-C-54` | Limiter les dépendances entre signataires | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe)<br>✅ [Signature séquentielle puis signature conjointe d'un groupe de signataires](#signature-sequentielle-puis-signature-conjointe-dun-groupe-de-signataires) |
| `IQ-C-55` | Flexibilité des workflows en cours de processus | Obligatoire | Scénario BDD exécuté | ✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours) |
| `IQ-C-56` | Minimiser les interventions manuelles | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-C-57` | Expérience simple pour initiateurs, signataires internes et externes | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie)<br>✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe) |
| `IQ-C-58` | Personnalisation : image de marque, messages, parcours | Obligatoire (à préciser) | Scénario BDD exécuté | ✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-C-59` | Automatisation et IA (extraction, classification, anomalies) | Optionnelle | Scénario BDD exécuté | ✅ [L'analyse automatisée d'un document est soumise à une révision humaine obligatoire](#lanalyse-automatisee-dun-document-est-soumise-a-une-revision-humaine-obligatoire) |
| `IQ-C-60` | Identification et authentification des utilisateurs (SSO) | Obligatoire | Scénario BDD exécuté | ✅ [Authentification unique configurable et connexion locale réservable aux comptes de secours](#authentification-unique-configurable-et-connexion-locale-reservable-aux-comptes-de-secours)<br>✅ [La session se termine après l'inactivité et au plus tard à sa durée maximale, réglées par l'administration technique](#la-session-se-termine-apres-linactivite-et-au-plus-tard-a-sa-duree-maximale-reglees-par-ladministration-technique) |
| `IQ-C-61` | Authentification et vérification d'identité selon profil, document et risque | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe)<br>✅ [Le niveau d'identité exigé se configure par l'organisation](#le-niveau-didentite-exige-se-configure-par-lorganisation) |
| `IQ-C-62` | Méthodes internes et externes, authentification renforcée | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe) |
| `IQ-C-63` | Standards reconnus de signature électronique, validité juridique | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe) |
| `IQ-C-64` | Piste d'audit complète : événements, authentification, horodatages, consentements, intégrité | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe)<br>✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-C-65` | Conservation, export, portabilité durables pendant la période requise | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-C-66` | Protection du cycle de vie, dont la destruction au moment opportun (Loi sur les archives) | Obligatoire | Scénario BDD exécuté | ✅ [Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe](#conservation-par-type-de-contrat-gel-legal-exportation-en-lot-et-releve-dutilisation-par-enveloppe) |
| `IQ-C-67` | Accès, modèles, workflows, historiques selon rôles, groupes, unités, domaines | Obligatoire | Scénario BDD exécuté | ✅ [Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné](#moindre-privilege-administrations-separees-auditeur-en-lecture-seule-module-cloisonne)<br>✅ [Comptes créés avec un rôle, rattachés à un groupe, puis désactivés ; départ d'un employé consulté](#comptes-crees-avec-un-role-rattaches-a-un-groupe-puis-desactives-depart-dun-employe-consulte) |
| `IQ-C-68` | Compatible postes Windows, mobiles, navigateurs Edge, Chrome, Safari | Obligatoire | Documentaire | Compatibilité navigateurs : Chromium éprouvé par la suite ; Edge partage le moteur ; Safari et postes mobiles à confirmer en recette. |
| `IQ-C-69` | S'intégrer à Microsoft 365, Salesforce, Workday : API, connecteurs, protocoles | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-C-70` | Compatibilité maintenue avec l'évolution de l'environnement | Obligatoire | Documentaire | Compatibilité maintenue avec l'évolution de l'environnement : engagement de maintenance et versions d'API paramétrables. |
| `IQ-D-01` | Génération automatisée ou semi-automatisée depuis des modèles approuvés | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-D-02` | Données des systèmes sources, dont Salesforce | Obligatoire | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-D-03` | Préparation et gestion des documents à valider, approuver, signer | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-D-04` | Circuits séquentiels, parallèles, mixtes | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-D-05` | Utilisateurs, signataires, rôles, droits, délégations | Obligatoire | Scénario BDD exécuté | ✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours)<br>✅ [Comptes créés avec un rôle, rattachés à un groupe, puis désactivés ; départ d'un employé consulté](#comptes-crees-avec-un-role-rattaches-a-un-groupe-puis-desactives-depart-dun-employe-consulte) |
| `IQ-D-06` | Modèles et clauses contractuelles | Obligatoire | Scénario BDD exécuté | ✅ [Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie](#un-modele-reutilisable-porte-des-champs-types-et-positionnes-une-clause-conditionnelle-et-un-cycle-de-vie) |
| `IQ-D-07` | Automatisation des notifications, rappels, règles d'affaires | Obligatoire | Scénario BDD exécuté | ✅ [Attribution par rôle et par groupe, étape conditionnelle selon le risque, simulation avant lancement](#attribution-par-role-et-par-groupe-etape-conditionnelle-selon-le-risque-simulation-avant-lancement)<br>✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-D-08` | Suivi centralisé | Obligatoire | Scénario BDD exécuté | ✅ [Suivi centralisé paginé côté serveur, filtres, exceptions et export ; tableau de bord](#suivi-centralise-pagine-cote-serveur-filtres-exceptions-et-export-tableau-de-bord) |
| `IQ-D-09` | Versions et historique des actions | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-D-10` | Piste d'audit complète | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-D-11` | Conservation et exportation : documents, preuves, pistes, métadonnées | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-D-12` | Intégration aux systèmes d'IQ | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-D-13` | Accessibilité des interfaces, niveau maintenu aux mises à jour | Obligatoire | Scénario BDD exécuté | ✅ [Consultation et décision sur téléphone, sans débordement ni violation d'accessibilité](#consultation-et-decision-sur-telephone-sans-debordement-ni-violation-daccessibilite)<br>✅ [Les interfaces et la console d'administration sont en français, offertes en anglais](#les-interfaces-et-la-console-dadministration-sont-en-francais-offertes-en-anglais) |
| `IQ-D-14` | PCI DSS si des cartes de paiement sont traitées | Conditionnelle | Scénario BDD exécuté | ✅ [Aucune carte de paiement n'entre dans la plateforme, qui reste hors du périmètre PCI DSS](#aucune-carte-de-paiement-nentre-dans-la-plateforme-qui-reste-hors-du-perimetre-pci-dss) |
| `IQ-D-15` | Gestion des rôles et des droits d'accès | Obligatoire | Scénario BDD exécuté | ✅ [Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné](#moindre-privilege-administrations-separees-auditeur-en-lecture-seule-module-cloisonne) |
| `IQ-D-16` | Principe du moindre privilège | Obligatoire | Scénario BDD exécuté | ✅ [Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné](#moindre-privilege-administrations-separees-auditeur-en-lecture-seule-module-cloisonne) |
| `IQ-D-17` | Gestion distincte des utilisateurs et des administrateurs | Obligatoire | Scénario BDD exécuté | ✅ [Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné](#moindre-privilege-administrations-separees-auditeur-en-lecture-seule-module-cloisonne) |
| `IQ-D-18` | Révocation ou modification des accès | Obligatoire | Scénario BDD exécuté | ✅ [Comptes créés avec un rôle, rattachés à un groupe, puis désactivés ; départ d'un employé consulté](#comptes-crees-avec-un-role-rattaches-a-un-groupe-puis-desactives-depart-dun-employe-consulte) |
| `IQ-D-19` | Journalisation et traçabilité des activités | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-D-20` | Protection des renseignements et documents | Obligatoire | Scénario BDD exécuté | ✅ [Les données d'IQ sont introuvables pour une autre organisation](#les-donnees-diq-sont-introuvables-pour-une-autre-organisation) |
| `IQ-D-21` | Interfaces en français, obligatoirement | Obligatoire | Scénario BDD exécuté | ✅ [Les interfaces et la console d'administration sont en français, offertes en anglais](#les-interfaces-et-la-console-dadministration-sont-en-francais-offertes-en-anglais) |
| `IQ-D-22` | Offertes en anglais lorsque requis | Obligatoire | Scénario BDD exécuté | ✅ [Les interfaces et la console d'administration sont en français, offertes en anglais](#les-interfaces-et-la-console-dadministration-sont-en-francais-offertes-en-anglais) |
| `IQ-D-23` | Consoles d'administration en français | Obligatoire | Scénario BDD exécuté | ✅ [Les interfaces et la console d'administration sont en français, offertes en anglais](#les-interfaces-et-la-console-dadministration-sont-en-francais-offertes-en-anglais) |
| `IQ-D-24` | Mode SaaS principalement | Obligatoire | Documentaire | Mode SaaS : modèle de service. |
| `IQ-D-25` | Fonctionnalités natives privilégiées, peu de développements spécifiques | Préférence | Documentaire | Préférence pour les fonctionnalités natives : configuration par l'organisation, démontrée par les scénarios de paramétrage. |
| `IQ-D-26` | Intégrations avec l'écosystème, dont Salesforce | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-D-27` | Échanges de données, documents, modèles, statuts, signataires, instructions, preuves | Faculté (« pourront ») | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-D-28` | API et connecteurs sécurisés, documentés, maintenus | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-D-29` | Interopérabilité permettant l'évolution | Obligatoire | Scénario BDD exécuté | ✅ [Les interfaces sont standards et documentées (OpenAPI, SCIM)](#les-interfaces-sont-standards-et-documentees-openapi-scim) |
| `IQ-S-01` | Création : trousse, documents, participants, positionnement des champs, modèle, configuration | Obligatoire | Scénario BDD exécuté | ✅ [L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie](#linitiateur-constitue-une-trousse-documents-pdf-et-word-versions-signataires-internes-et-externes-copie) |
| `IQ-S-02` | Validation : réception, consultation, commentaires, validation, passage à l'étape suivante | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-S-03` | Approbation, rejet, retour pour correction, historique | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise) |
| `IQ-S-04` | Signature externe : invitation, authentification, consultation, signature, confirmation | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe) |
| `IQ-S-05` | Ajout, remplacement, délégation, traçabilité ; mise à jour d'une donnée et son impact | Obligatoire | Scénario BDD exécuté | ✅ [Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours](#deux-validations-en-parallele-reassignation-a-chaud-et-delegation-sans-relancer-le-parcours)<br>✅ [Un signataire est ajouté puis remplacé en cours de parcours ; une nouvelle version de document est tracée](#un-signataire-est-ajoute-puis-remplace-en-cours-de-parcours-une-nouvelle-version-de-document-est-tracee) |
| `IQ-S-06` | Rappels configurés, notifications, rappels manuels, alertes d'échéance, communications personnalisées | Obligatoire | Scénario BDD exécuté | ✅ [Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel](#messages-desactives-puis-reactives-invitation-et-rappel-manuel-recus-par-courriel) |
| `IQ-S-07` | Journal, horodatages, actions, authentification, dossier de preuve | Obligatoire | Scénario BDD exécuté | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe)<br>✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-S-08` | Consultation et décision sur mobile, navigation mobile | Obligatoire | Scénario BDD exécuté | ✅ [Consultation et décision sur téléphone, sans débordement ni violation d'accessibilité](#consultation-et-decision-sur-telephone-sans-debordement-ni-violation-daccessibilite) |
| `IQ-S-09` | Refus, commentaires justificatifs, retour à l'étape appropriée, reprise, historique | Obligatoire | Scénario BDD exécuté | ✅ [Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise](#validation-puis-approbation-avec-commentaire-retour-a-letape-de-validation-et-reprise)<br>✅ [Un rejet exige un motif et arrête le parcours](#un-rejet-exige-un-motif-et-arrete-le-parcours) |
| `IQ-S-10` | Téléchargement du signé, export du certificat, du journal, des métadonnées ; formats ; portabilité | Obligatoire | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-S-10b` | Gestion de bout en bout d'un CLM | Optionnelle (addenda § 4.3) | Scénario BDD exécuté | ✅ [L'analyse automatisée d'un document est soumise à une révision humaine obligatoire](#lanalyse-automatisee-dun-document-est-soumise-a-une-revision-humaine-obligatoire) |
| `IQ-A-01` | TI-22 : intégration Workday (déclenchement, statuts, document signé) | Obligatoire | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-A-02` | AF-47 : lot de signatures depuis Salesforce | Obligatoire (« doit ») | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-A-03` | AF-47 : lot depuis la plateforme | Faculté (« peut ») | Scénario BDD exécuté | ✅ [Signature en lot depuis la plateforme et approbation globale du lot](#signature-en-lot-depuis-la-plateforme-et-approbation-globale-du-lot) |
| `IQ-A-04` | SIG-07/08 : méthodes d'authentification standard et renforcée décrites | Obligatoire | Documentaire | ✅ [Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe](#un-signataire-externe-recoit-linvitation-par-courriel-prouve-son-identite-consent-et-signe) |
| `IQ-A-05` | CLM optionnel intégrable à Salesforce lorsqu'il est proposé | Conditionnelle | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-A-06` | Environ 30 champs Salesforce préremplis par modèle | Obligatoire (capacité) | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |
| `IQ-A-07` | Intégrations Microsoft 365, Workday, Entra ID | Selon l'Annexe 10 | Scénario BDD exécuté | ✅ [Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué](#un-systeme-externe-workday-obtient-un-jeton-oauth-cree-une-trousse-par-lapi-en-lit-le-statut-puis-est-revoque) |
| `IQ-A-08` | Provisionnement et déprovisionnement SCIM 2.0 (Saviynt) | Obligatoire (add. 3 : « doit prendre en charge ») | Scénario BDD exécuté | ✅ [Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate](#correspondance-dannuaire-et-provisionnement-scim-20-puis-desactivation-immediate) |
| `IQ-A-09` | Connecteur MCP | Non exigé | Scénario BDD exécuté | ✅ [Un assistant d'IA se connecte au connecteur MCP et lit, en lecture seule, le statut d'une trousse de son client](#un-assistant-dia-se-connecte-au-connecteur-mcp-et-lit-en-lecture-seule-le-statut-dune-trousse-de-son-client) |
| `IQ-A-10` | Authentification Salesforce de remplacement (JWT Bearer) décrite | Obligatoire (« doit également décrire ») | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-A-11` | Exportation ou transfert des documents et métadonnées vers un référentiel désigné par IQ | Obligatoire (« doit également permettre ») | Scénario BDD exécuté | ✅ [Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe](#conservation-par-type-de-contrat-gel-legal-exportation-en-lot-et-releve-dutilisation-par-enveloppe) |
| `IQ-A-12` | Retour des statuts lancée, retournée, rejetée, annulée, terminée | Obligatoire (« doit notamment permettre ») | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-A-13` | Seules les données nécessaires transférées depuis Salesforce | Obligatoire (« devront ») | Scénario BDD exécuté | ✅ [Seuls les champs Salesforce déclarés sont lus](#seuls-les-champs-salesforce-declares-sont-lus) |
| `IQ-A-14` | Limites d'API, surveillance, journalisation, reprise (8 tentatives, plafond 1 h) | Obligatoire (« doit ») | Scénario BDD exécuté | ✅ [Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé](#les-statuts-reviennent-vers-salesforce-resistent-a-une-panne-et-se-relancent-le-jwt-bearer-exige-sa-cle) |
| `IQ-A-15` | Interfaces internes et externes en français, en anglais sur demande d'IQ | Obligatoire (« doivent obligatoirement ») | Scénario BDD exécuté | ✅ [Les interfaces et la console d'administration sont en français, offertes en anglais](#les-interfaces-et-la-console-dadministration-sont-en-francais-offertes-en-anglais) |
| `IQ-A-16` | Démontrer la volumétrie (55 000 à 65 000 documents par an) et des variations raisonnables | Obligatoire (« doit démontrer ») | Documentaire | Volumétrie démontrée par le harnais de performance JMeter (plans, seuils SLI/SLO). |
| `IQ-A-18` | Génération Word modifiable avant l'approbation finale ; nouvelle version et reprise des validations ; document à signer figé | Obligatoire (« doit pouvoir ») | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele)<br>✅ [Le gabarit Word d'IQ est fusionné en conservant sa mise en page, en Word comme en PDF](#le-gabarit-word-diq-est-fusionne-en-conservant-sa-mise-en-page-en-word-comme-en-pdf) |
| `IQ-A-19` | Mappage configurable des groupes et attributs Entra ID vers les rôles, moindre privilège, traçabilité | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate](#correspondance-dannuaire-et-provisionnement-scim-20-puis-desactivation-immediate) |
| `IQ-A-20` | Domaine d'affaires alimenté par un groupe ou un attribut d'annuaire, séparation logique | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate](#correspondance-dannuaire-et-provisionnement-scim-20-puis-desactivation-immediate) |
| `IQ-A-21` | Conservation par catégorie, gel légal, trois sorts finaux, bordereau ou certificat de destruction | Obligatoire (« doit ») | Scénario BDD exécuté | ✅ [Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe](#conservation-par-type-de-contrat-gel-legal-exportation-en-lot-et-releve-dutilisation-par-enveloppe) |
| `IQ-A-22` | Mesure de l'utilisation par enveloppe envoyée, sans double comptage | Obligatoire (« doit être ») | Scénario BDD exécuté | ✅ [Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe](#conservation-par-type-de-contrat-gel-legal-exportation-en-lot-et-releve-dutilisation-par-enveloppe) |
| `IQ-A-23` | Niveaux d'authentification configurables selon le type de document et le processus | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Le niveau d'identité exigé se configure par l'organisation](#le-niveau-didentite-exige-se-configure-par-lorganisation) |
| `IQ-A-24` | Signature par un représentant autorisé ; preuve d'autorité ; qualité du signataire tracée | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Un représentant signe pour une entreprise après avoir attesté son autorité](#un-representant-signe-pour-une-entreprise-apres-avoir-atteste-son-autorite) |
| `IQ-A-25` | Parcours conditionnels selon type, unité, rôle, montant | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Attribution par rôle et par groupe, étape conditionnelle selon le risque, simulation avant lancement](#attribution-par-role-et-par-groupe-etape-conditionnelle-selon-le-risque-simulation-avant-lancement) |
| `IQ-A-26` | Séparation de l'administration d'affaires et de l'administration technique | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné](#moindre-privilege-administrations-separees-auditeur-en-lecture-seule-module-cloisonne) |
| `IQ-A-27` | Mappage des attributs et groupes SCIM, désactivation rapide | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate](#correspondance-dannuaire-et-provisionnement-scim-20-puis-desactivation-immediate) |
| `IQ-A-28` | Approbation adaptée au lot, traçabilité par dossier | Obligatoire (« doit permettre ») | Scénario BDD exécuté | ✅ [Signature en lot depuis la plateforme et approbation globale du lot](#signature-en-lot-depuis-la-plateforme-et-approbation-globale-du-lot) |
| `IQ-A-29` | Export dans des formats pérennes, PDF/PDF-A lorsque disponibles, export massif | Obligatoire (export) ; PDF/A conditionnel | Scénario BDD exécuté | ✅ [Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte](#le-dossier-de-preuve-dune-trousse-signee-se-consulte-se-verifie-sexporte-le-journal-se-consulte-et-sexporte) |
| `IQ-A-30` | Modèles Word d'IQ, structure et présentation préservées dans la mesure permise ; limites déclarées | Obligatoire avec réserve | Scénario BDD exécuté | ✅ [Le gabarit Word d'IQ est fusionné en conservant sa mise en page, en Word comme en PDF](#le-gabarit-word-diq-est-fusionne-en-conservant-sa-mise-en-page-en-word-comme-en-pdf) |
| `IQ-A-17` | FundingRequest lu par Id ou Name, correspondance configurable par modèle | Obligatoire (« devra ») | Scénario BDD exécuté | ✅ [Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle](#le-contrat-se-genere-depuis-le-dossier-salesforce-prerempli-en-pdf-puis-en-word-modifiable-avec-le-parcours-du-modele) |

## Exigences non éprouvées par un scénario

- `IQ-C-02` — Contexte multi-juridictionnel, solution conforme, modulaire, évolutive : **Documentaire.** Principe général (contexte multi-juridictionnel, modularité) : démontré par l'ensemble des scénarios et des modules activables.
- `IQ-C-05` — Offerte en infonuagique, connectable aux SaaS : **Documentaire.** Mode infonuagique : hébergement Azure (infrastructure). Référence : `docs/adr/ADR-020-region-hebergement-et-niveaux-de-service.md`.
- `IQ-C-68` — Compatible postes Windows, mobiles, navigateurs Edge, Chrome, Safari : **Documentaire.** Compatibilité navigateurs : Chromium éprouvé par la suite ; Edge partage le moteur ; Safari et postes mobiles à confirmer en recette.
- `IQ-C-70` — Compatibilité maintenue avec l'évolution de l'environnement : **Documentaire.** Compatibilité maintenue avec l'évolution de l'environnement : engagement de maintenance et versions d'API paramétrables.
- `IQ-D-24` — Mode SaaS principalement : **Documentaire.** Mode SaaS : modèle de service.
- `IQ-D-25` — Fonctionnalités natives privilégiées, peu de développements spécifiques : **Documentaire.** Préférence pour les fonctionnalités natives : configuration par l'organisation, démontrée par les scénarios de paramétrage.
- `IQ-A-04` — SIG-07/08 : méthodes d'authentification standard et renforcée décrites : **Documentaire.** Méthodes d'authentification décrites dans la soumission ; leur comportement est éprouvé par IQ-C-61 et IQ-C-62. Référence : `docs/Dossier_IQ_2026-6370-0005/02-Analyse/impact_addenda_1.md`.
- `IQ-A-16` — Démontrer la volumétrie (55 000 à 65 000 documents par an) et des variations raisonnables : **Documentaire.** Volumétrie démontrée par le harnais de performance JMeter (plans, seuils SLI/SLO). Référence : `perf/README.md`.

## Scénarios détaillés

### Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1)


#### L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-C-44`, `IQ-C-45`, `IQ-C-46`, `IQ-C-50`, `IQ-D-03`, `IQ-D-09`, `IQ-S-01`, `IQ-C-42`, `IQ-C-57`

```gherkin
Quand j'exécute le cas "iq.trousse.creation"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — détail de la trousse"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.trousse.creation`** — Création d'une trousse : PDF et Word, versions, participants internes et externes ; historique *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `annexe` ← `$.id` ; capture `annexeCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 4 | `POST /api/clm/trousses/${trousse}/documents/${documentCle}/versions?motif=Correction%20annexe` |  | 2xx | `$.version` egal 2 ; envoi multipart (fichier) |
| 5 | `GET /api/clm/trousses/${trousse}/documents/${documentCle}/versions` |  | 2xx | `$` longueurMin 2 ; `$[*].statut` inclut "REMPLACE" ; `$[*].motif` inclut "Correction annexe" |
| 6 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `externe` ← `$.id` |
| 7 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `interne` ← `$.id` |
| 8 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.documents` longueurMin 2 ; `$.documents[*].type` inclut "DOCX" ; `$.documents[*].type` inclut "PDF" ; `$.participants` longueurMin 2 ; `$.copies` contient "copie." |
| 9 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | `$` longueurMin 3 |

#### Un document de 25 Mo est accepté ; au-delà du plafond configuré, il est refusé

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-C-47`

```gherkin
Quand j'exécute le cas "iq.documents.taille"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.documents.taille`** — Un document de 25 Mo est accepté ; au-delà du plafond configuré, il est refusé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.taille` egal 26200000 ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/documents` |  | 413 | contient « Fichier trop volumineux » ; ne contient pas « Exception » ; envoi multipart (fichier) |

#### Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-C-32`, `IQ-C-33`, `IQ-C-34`, `IQ-C-35`, `IQ-C-36`, `IQ-C-37`, `IQ-C-38`, `IQ-D-06`, `IQ-C-17`

```gherkin
Quand j'exécute le cas "iq.modele.cycle"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — modèles"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.modele.cycle`** — Modèle réutilisable : champs typés et positionnés, clause conditionnelle, aperçu, soumission, publication, nouvelle version, retrait ; partage par groupe *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/modeles` |  | 2xx | `$.statut` egal "BROUILLON" ; `$.champs[*].type` inclut "SIGNATURE" ; `$.champs[*].type` inclut "LISTE" ; `$.champs[*].type` inclut "DATE" ; `$.champs[*].type` inclut "CASE" ; `$.champs[5].position.page` egal 1 ; `$.champs[5].roleDestinataire` egal "SIGNATAIRE_1" ; `$.groupes` egal "FINANCEMENT_ENTREPRISES" ; capture `modele` ← `$.id` |
| 2 | `GET /api/clm/modeles/${modele}/validation` |  | 2xx | `$.valide` egal true |
| 3 | `POST /api/clm/modeles/${modele}/apercu` |  | 2xx | `$.texte` contient "Coopérative Agri-Nord" ; `$.clausesRetenues` inclut "garantie" |
| 4 | `POST /api/clm/modeles/${modele}/apercu` |  | 2xx | `$.clausesRetenues` exclut "garantie" |
| 5 | `POST /api/clm/modeles/${modele}/soumettre` |  | 2xx | `$.statut` egal "EN_REVISION" |
| 6 | `POST /api/clm/modeles/${modele}/publier` |  | 403 | — |
| 7 | `POST /api/clm/modeles/${modele}/publier` | administration d'affaires IQ | 2xx | `$.statut` egal "PUBLIE" |
| 8 | `POST /api/clm/modeles/${modele}/nouvelle-version` |  | 2xx | `$.numero` egal 2 ; `$.statut` egal "BROUILLON" ; capture `version2` ← `$.id` |
| 9 | `POST /api/clm/modeles/${modele}/retirer` | administration d'affaires IQ | 2xx | `$.statut` egal "RETIRE" |
| 10 | `GET /api/clm/modeles?statut=PUBLIE` |  | 2xx | `$[*].id` exclut "${modele}" |

#### Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-C-09`, `IQ-C-10`, `IQ-C-11`, `IQ-C-12`, `IQ-C-14`, `IQ-C-29`, `IQ-D-01`, `IQ-D-02`, `IQ-A-06`, `IQ-A-17`, `IQ-A-18`, `IQ-C-56`

```gherkin
Quand j'exécute le cas "iq.generation.salesforce"
Et j'exécute le cas "iq.suggestions"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.generation.salesforce`** — Génération depuis Salesforce : dossier récupéré, modèle prérempli, contrat généré en PDF puis en Word modifiable, parcours du modèle copié *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/modeles` |  | 2xx | capture `modele` ← `$.id` |
| 2 | `POST /api/clm/modeles/${modele}/soumettre` |  | 2xx | — |
| 3 | `POST /api/clm/modeles/${modele}/publier` | administration d'affaires IQ | 2xx | — |
| 4 | `GET /api/clm/salesforce/dossiers/DEM-2026-0001` |  | 2xx | `$.numero` egal "DEM-2026-0001" ; `$.champs.Demandeur` egal "Coopérative Agri-Nord" |
| 5 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 6 | `POST /api/clm/trousses/${trousse}/preremplissage` |  | 2xx | `$.dossier` existe true ; `$.correspondances` longueurMin 1 |
| 7 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.valeurs.demandeur.valeur` egal "Coopérative Agri-Nord" ; `$.valeurs.demandeur.provenance` egal "SALESFORCE" ; `$.valeurs.montant.valeur` egal "250000.00" ; `$.valeurs.programme.provenance` different "SALESFORCE" ; `$.etapes[*].cle` inclut "v1" ; `$.resume.sfDossierId` egal "a0B5e00000GRT001" |
| 8 | `POST /api/clm/trousses/${trousse}/generer?format=PDF` |  | 2xx | `$.type` egal "PDF" ; capture `genere` ← `$.id` |
| 9 | `GET /api/clm/trousses/${trousse}/documents/${genere}/texte` |  | 2xx | contient « Coopérative Agri-Nord » ; contient « Le bénéficiaire fournit une garantie de prêt. » |
| 10 | `POST /api/clm/trousses/${trousse}/generer?format=DOCX` |  | 2xx | `$.type` egal "DOCX" ; `$.nom` contient ".docx" |

**Cas `iq.suggestions`** — Saisie assistée : les valeurs déjà retenues pour un champ sont proposées *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/trousses/${trousse}/suggestions/demandeur` |  | 2xx | `$` existe true |

#### La saisie est assistée par les valeurs déjà retenues

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-C-39`

```gherkin
Quand j'exécute le cas "iq.generation.salesforce"
Et j'exécute le cas "iq.suggestions"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.generation.salesforce`** — Génération depuis Salesforce : dossier récupéré, modèle prérempli, contrat généré en PDF puis en Word modifiable, parcours du modèle copié *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/modeles` |  | 2xx | capture `modele` ← `$.id` |
| 2 | `POST /api/clm/modeles/${modele}/soumettre` |  | 2xx | — |
| 3 | `POST /api/clm/modeles/${modele}/publier` | administration d'affaires IQ | 2xx | — |
| 4 | `GET /api/clm/salesforce/dossiers/DEM-2026-0001` |  | 2xx | `$.numero` egal "DEM-2026-0001" ; `$.champs.Demandeur` egal "Coopérative Agri-Nord" |
| 5 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 6 | `POST /api/clm/trousses/${trousse}/preremplissage` |  | 2xx | `$.dossier` existe true ; `$.correspondances` longueurMin 1 |
| 7 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.valeurs.demandeur.valeur` egal "Coopérative Agri-Nord" ; `$.valeurs.demandeur.provenance` egal "SALESFORCE" ; `$.valeurs.montant.valeur` egal "250000.00" ; `$.valeurs.programme.provenance` different "SALESFORCE" ; `$.etapes[*].cle` inclut "v1" ; `$.resume.sfDossierId` egal "a0B5e00000GRT001" |
| 8 | `POST /api/clm/trousses/${trousse}/generer?format=PDF` |  | 2xx | `$.type` egal "PDF" ; capture `genere` ← `$.id` |
| 9 | `GET /api/clm/trousses/${trousse}/documents/${genere}/texte` |  | 2xx | contient « Coopérative Agri-Nord » ; contient « Le bénéficiaire fournit une garantie de prêt. » |
| 10 | `POST /api/clm/trousses/${trousse}/generer?format=DOCX` |  | 2xx | `$.type` egal "DOCX" ; `$.nom` contient ".docx" |

**Cas `iq.suggestions`** — Saisie assistée : les valeurs déjà retenues pour un champ sont proposées *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/trousses/${trousse}/suggestions/demandeur` |  | 2xx | `$` existe true |

#### Le gabarit Word d'IQ est fusionné en conservant sa mise en page, en Word comme en PDF

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1) (`bdd/features/iq/01-trousses-modeles.feature`)
- **Exigences éprouvées :** `IQ-A-30`, `IQ-A-18`

```gherkin
Quand j'exécute le cas "iq.gabarit.word"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.gabarit.word`** — Gabarit Word d'IQ déposé sur le modèle : le contrat généré en Word et en PDF garde la mise en page (conversion par le service LibreOffice) *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/modeles` |  | 2xx | capture `modele` ← `$.id` |
| 2 | `POST /api/clm/modeles/${modele}/gabarit-word` |  | 2xx | `$.gabaritWord` egal "Convention IQ.docx" ; envoi multipart (fichier) |
| 3 | `POST /api/clm/modeles/${modele}/soumettre` |  | 2xx | — |
| 4 | `POST /api/clm/modeles/${modele}/publier` | administration d'affaires IQ | 2xx | — |
| 5 | `POST /api/clm/trousses` |  | 2xx | capture `trousse` ← `$.resume.id` |
| 6 | `PUT /api/clm/trousses/${trousse}/valeurs` |  | 2xx | — |
| 7 | `POST /api/clm/trousses/${trousse}/generer?format=DOCX` |  | 2xx | `$.motif` contient "gabarit Word" ; capture `word` ← `$.id` |
| 8 | `GET /api/clm/trousses/${trousse}/documents/${word}/texte` |  | 2xx | contient « Convention de financement — Coopérative Agri-Nord inc. » ; contient « Hypothèque mobilière de premier rang de 250 000 $. » ; ne contient pas « {{ » |
| 9 | `POST /api/clm/trousses/${trousse}/generer?format=PDF` |  | 2xx | `$.motif` contient "mise en page conservée" ; capture `pdf` ← `$.id` |
| 10 | `GET /api/clm/trousses/${trousse}/documents/${pdf}/texte` |  | 2xx | contient « Coopérative Agri-Nord inc. » |

### Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9)


#### Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9) (`bdd/features/iq/02-parcours.feature`)
- **Exigences éprouvées :** `IQ-C-01`, `IQ-C-18`, `IQ-C-22`, `IQ-C-23`, `IQ-C-25`, `IQ-C-48`, `IQ-D-04`, `IQ-S-02`, `IQ-S-03`, `IQ-S-09`, `IQ-C-52`

```gherkin
Quand j'exécute le cas "iq.parcours.validation-approbation"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — parcours de la trousse"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.parcours.validation-approbation`** — Validation puis approbation : commentaire, retour à l'étape de validation avec motif, reprise, approbation ; refus d'un tiers ; historique *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_v1` ← `$[0].id` ; capture `e_a1` ← `$[1].id` |
| 4 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 5 | `GET /api/clm/taches` | validateur IQ | 2xx | `$[*].etapeId` inclut "${e_v1}" |
| 6 | `POST /api/clm/etapes/${e_v1}/commentaires` | validateur IQ | 2xx | — |
| 7 | `POST /api/clm/etapes/${e_v1}/decider` | approbateur IQ | 403, 409 | — |
| 8 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 2xx | — |
| 9 | `POST /api/clm/etapes/${e_a1}/decider` | approbateur IQ | 2xx | `$.statut` existe true |
| 10 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[0].statut` egal "ACTIVE" |
| 11 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 2xx | — |
| 12 | `POST /api/clm/etapes/${e_a1}/decider` | approbateur IQ | 2xx | — |
| 13 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[*].statut` exclut "ACTIVE" |
| 14 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | contient « Clause 4 à préciser ${unique} » ; contient « Annexe B à revalider » |

#### Un rejet exige un motif et arrête le parcours

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9) (`bdd/features/iq/02-parcours.feature`)
- **Exigences éprouvées :** `IQ-C-22`, `IQ-S-09`

```gherkin
Quand j'exécute le cas "iq.parcours.rejet"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.parcours.rejet`** — Rejet avec motif obligatoire : le parcours s'arrête et le rejet est tracé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_v1` ← `$[0].id` |
| 4 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 5 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 400 | — |
| 6 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 2xx | — |
| 7 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.resume.statut` egal "REJETEE" |

#### Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9) (`bdd/features/iq/02-parcours.feature`)
- **Exigences éprouvées :** `IQ-C-18`, `IQ-C-20`, `IQ-C-21`, `IQ-C-55`, `IQ-D-05`, `IQ-S-05`

```gherkin
Quand j'exécute le cas "iq.parcours.parallele-reassignation"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.parcours.parallele-reassignation`** — Deux validations en parallèle, réassignation à chaud d'une étape et délégation, sans relancer le parcours *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_v1` ← `$[0].id` ; capture `e_v2` ← `$[1].id` ; capture `e_a1` ← `$[2].id` |
| 4 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 5 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[0].statut` egal "ACTIVE" ; `$.etapes[1].statut` egal "ACTIVE" ; `$.etapes[2].statut` egal "EN_ATTENTE" |
| 6 | `PUT /api/clm/etapes/${e_v2}/affectations` |  | 2xx | `$.affectations[*].valeur` inclut "validateur@iq.example" |
| 7 | `POST /api/clm/etapes/${e_v1}/deleguer` | validateur IQ | 2xx | — |
| 8 | `POST /api/clm/etapes/${e_v1}/decider` | approbateur IQ | 2xx | — |
| 9 | `POST /api/clm/etapes/${e_v2}/decider` | validateur IQ | 2xx | — |
| 10 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[2].statut` egal "ACTIVE" ; `$.resume.statut` egal "EN_TRAITEMENT" |
| 11 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | contient « REASSIGNATION » ; contient « Vacances » |

#### Attribution par rôle et par groupe, étape conditionnelle selon le risque, simulation avant lancement

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9) (`bdd/features/iq/02-parcours.feature`)
- **Exigences éprouvées :** `IQ-C-19`, `IQ-A-25`, `IQ-D-07`

```gherkin
Quand j'exécute le cas "iq.parcours.regles"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.parcours.regles`** — Attribution par rôle et groupe ; étape conditionnelle selon le risque et le montant ; simulation du parcours avant lancement *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | `$[*].affectations[0].type` inclut "ROLE" ; `$[*].affectations[0].type` inclut "GROUPE" |
| 4 | `POST /api/clm/trousses/${trousse}/parcours/tester` |  | 2xx | `$.valide` egal true |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[*].statut` inclut "NON_REQUISE" |

#### Un signataire est ajouté puis remplacé en cours de parcours ; une nouvelle version de document est tracée

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9) (`bdd/features/iq/02-parcours.feature`)
- **Exigences éprouvées :** `IQ-C-30`, `IQ-C-31`, `IQ-S-05`

```gherkin
Quand j'exécute le cas "iq.signataires.en-cours"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.signataires.en-cours`** — Signataire ajouté puis remplacé en cours de parcours, sans reprendre le processus ; nouvelle version de document : nouveau cycle annoncé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `alice` ← `$.id` |
| 4 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_v1` ← `$[0].id` ; capture `e_s1` ← `$[1].id` |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `bruno` ← `$.id` |
| 7 | `PUT /api/clm/etapes/${e_s1}/affectations` |  | 2xx | `$.affectations[0].valeur` egalTexte "${bruno}" |
| 8 | `POST /api/clm/trousses/${trousse}/documents/${documentCle}/versions?motif=Montant%20corrig%C3%A9` |  | 2xx | envoi multipart (fichier) |
| 9 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | contient « Remplacement du signataire » ; contient « Montant corrigé » |

### Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4)

Les invitations et les codes partent par courriel SMTP (bac à sable Mailpit) ; l'attestation est signée en CAdES.

#### Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4) (`bdd/features/iq/03-signature-identite.feature`)
- **Exigences éprouvées :** `IQ-S-04`, `IQ-C-61`, `IQ-C-62`, `IQ-C-63`, `IQ-C-64`, `IQ-C-03`, `IQ-C-57`, `IQ-S-07`, `IQ-A-04`, `IQ-C-54`

```gherkin
Quand j'exécute le cas "iq.signature.externe"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — preuve de la trousse"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.signature.externe`** — Signature externe : invitation reçue par courriel, vérification d'identité, consentement, signature ; attestation CAdES ; trousse terminée et document signé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `marie` ← `$.id` |
| 4 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `` |  | 2xx | — |
| 7 | `GET /api/clm/portail/${jeton}` | anonyme | 2xx | `$.verifiee` egal false ; `$.niveau` egal "QUESTION" |
| 8 | `POST /api/clm/portail/${jeton}/verification` | anonyme | 2xx | — |
| 9 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 400, 401, 403, 409 | — |
| 10 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 2xx | `$.verifiee` egal true ; `$.documents` longueurMin 1 |
| 11 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 400, 409 | — |
| 12 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 2xx | `$.empreinteDocuments` existe true ; `$.attestation` contient "CAC-" ; capture `attestation` ← `$.attestation` |
| 13 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 409, 410 | — |
| 14 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.resume.statut` egal "TERMINEE" ; `$.documents[*].statut` inclut "SIGNE" |
| 15 | `GET /api/clm/trousses/${trousse}/preuve` |  | 2xx | `$.signatures[0].niveau` egal "QUESTION" ; `$.signatures[0].consentementLe` existe true ; `$.signatures[0].attestation.fournisseur` egal "clm-cachet" ; `$.integrite.integre` egal true |

#### Signature séquentielle puis signature conjointe d'un groupe de signataires

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4) (`bdd/features/iq/03-signature-identite.feature`)
- **Exigences éprouvées :** `IQ-C-26`, `IQ-C-27`, `IQ-C-28`, `IQ-C-54`

```gherkin
Quand j'exécute le cas "iq.signature.sequentielle-parallele"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.signature.sequentielle-parallele`** — Signatures séquentielles puis parallèles (groupe de signataires) : la seconde phase n'est ouverte qu'après la première *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `paul` ← `$.id` |
| 4 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `lise` ← `$.id` |
| 5 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `noe` ← `$.id` |
| 6 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` ; capture `e_s2` ← `$[1].id` |
| 7 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 8 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[0].statut` egal "ACTIVE" ; `$.etapes[1].statut` egal "EN_ATTENTE" |
| 9 | `` |  | 2xx | — |
| 10 | `GET /api/clm/portail/${jetonPaul}` | anonyme | 2xx | `$.verifiee` egal false ; `$.niveau` egal "QUESTION" |
| 11 | `POST /api/clm/portail/${jetonPaul}/verification` | anonyme | 2xx | — |
| 12 | `POST /api/clm/portail/${jetonPaul}/verification/reponse` | anonyme | 400, 401, 403, 409 | — |
| 13 | `POST /api/clm/portail/${jetonPaul}/verification/reponse` | anonyme | 2xx | `$.verifiee` egal true ; `$.documents` longueurMin 1 |
| 14 | `POST /api/clm/portail/${jetonPaul}/signer` | anonyme | 400, 409 | — |
| 15 | `POST /api/clm/portail/${jetonPaul}/signer` | anonyme | 2xx | `$.empreinteDocuments` existe true ; `$.attestation` contient "CAC-" ; capture `attestation` ← `$.attestation` |
| 16 | `POST /api/clm/portail/${jetonPaul}/signer` | anonyme | 409, 410 | — |
| 17 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.etapes[1].statut` egal "ACTIVE" ; `$.invitations` longueurMin 3 |

#### Un représentant signe pour une entreprise après avoir attesté son autorité

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4) (`bdd/features/iq/03-signature-identite.feature`)
- **Exigences éprouvées :** `IQ-A-24`

```gherkin
Quand j'exécute le cas "iq.signature.representant"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.signature.representant`** — Signataire représentant une entreprise : qualité tracée, attestation d'autorité exigée avant signature *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `marc` ← `$.id` |
| 4 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `` |  | 2xx | — |
| 7 | `POST /api/clm/portail/${jeton}/verification` | anonyme | 2xx | — |
| 8 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 2xx | `$.entreprise` egal "Coopérative Agri-Nord inc." ; `$.autoriteRequise` egal true |
| 9 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 400 | — |
| 10 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 2xx | — |
| 11 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | contient « pour Coopérative Agri-Nord inc. (Président) » |

#### Le niveau d'identité exigé se configure par l'organisation

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4) (`bdd/features/iq/03-signature-identite.feature`)
- **Exigences éprouvées :** `IQ-A-23`, `IQ-C-61`

```gherkin
Quand j'exécute le cas "iq.identite.par-risque"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.identite.par-risque`** — Niveau d'identité configurable : par défaut selon le risque, relevé par type de document ; jamais abaissé *(données : `bdd/donnees/iq.json` ; profil : administration d'affaires IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/admin/parametres` |  | 2xx | `$.module.identiteExterne` existe true |
| 2 | `GET /api/clm/parametres` | initiateur IQ | 2xx | `$` existe true |

### Notifications, rappels et suivi centralisé (Cahier § 1.4.7 ; scénario 6 ; addenda 3, Q38)


#### Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Notifications, rappels et suivi centralisé (Cahier § 1.4.7 ; scénario 6 ; addenda 3, Q38) (`bdd/features/iq/04-notifications-suivi.feature`)
- **Exigences éprouvées :** `IQ-C-24`, `IQ-C-40`, `IQ-C-41`, `IQ-S-06`, `IQ-D-07`, `IQ-C-58`

```gherkin
Quand j'exécute le cas "iq.notifications"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — notifications"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.notifications`** — Messages par événement et par canal, désactivables et réactivables ; invitation reçue par courriel ; relance manuelle : nouvelle invitation avec un nouveau lien ; fréquence des rappels *(données : `bdd/donnees/iq.json` ; profil : administration d'affaires IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/admin/messages` |  | 2xx | `$[*].evenement` inclut "INVITATION" ; `$[*].canal` inclut "COURRIEL" ; capture `messages` ← `$` |
| 2 | `PUT /api/clm/admin/messages` |  | 2xx | `$.actif` egal false |
| 3 | `PUT /api/clm/admin/messages` |  | 2xx | `$.actif` egal true |
| 4 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 5 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 6 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `remi` ← `$.id` |
| 7 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` |
| 8 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 9 | `` |  | 2xx | — |
| 10 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.rappelFrequenceJours` existe true ; `$.rappelsMax` existe true |
| 11 | `POST /api/clm/etapes/${e_s1}/relancer` |  | 2xx | — |
| 12 | `` |  | 2xx | — |
| 13 | `GET /api/clm/trousses/${trousse}/historique` |  | 2xx | contient « RELANCE_MANUELLE » ; `$[*].type` inclut "INVITATION_ENVOYEE" |

#### Suivi centralisé paginé côté serveur, filtres, exceptions et export ; tableau de bord

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Notifications, rappels et suivi centralisé (Cahier § 1.4.7 ; scénario 6 ; addenda 3, Q38) (`bdd/features/iq/04-notifications-suivi.feature`)
- **Exigences éprouvées :** `IQ-C-43`, `IQ-C-25`, `IQ-D-08`

```gherkin
Quand j'exécute le cas "iq.suivi"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — suivi centralisé"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.suivi`** — Suivi centralisé paginé côté serveur : filtres, exceptions, export ; tableau de bord par profil *(données : `bdd/donnees/iq.json` ; profil : administration d'affaires IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/suivi?page=0&taille=50` |  | 2xx | `$.lignes` longueurMin 1 ; `$.taille` egal 50 ; `$.total` existe true |
| 2 | `GET /api/clm/suivi?statut=EN_TRAITEMENT&page=0&taille=10` |  | 2xx | `$.lignes[*].statut` exclut "BROUILLON" |
| 3 | `GET /api/clm/suivi?exceptions=true&page=0&taille=10` |  | 2xx | `$.lignes` existe true |
| 4 | `GET /api/clm/suivi/export?statut=EN_TRAITEMENT` |  | 2xx | contient « reference » |
| 5 | `GET /api/clm/tableau-de-bord` |  | 2xx | `$.indicateurs.terminees` existe true ; `$.activite` existe true |
| 6 | `GET /api/clm/trousses?page=0&taille=20` | initiateur IQ | 2xx | `$.lignes` existe true |

### Preuve, journal, conservation et exportation (Cahier § 1.4.11, § 1.4.12 ; scénarios 7 et 10 ; addenda 2, 3, 4)


#### Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Preuve, journal, conservation et exportation (Cahier § 1.4.11, § 1.4.12 ; scénarios 7 et 10 ; addenda 2, 3, 4) (`bdd/features/iq/05-preuve-conservation.feature`)
- **Exigences éprouvées :** `IQ-C-49`, `IQ-C-51`, `IQ-C-64`, `IQ-C-65`, `IQ-D-10`, `IQ-D-11`, `IQ-D-19`, `IQ-S-07`, `IQ-S-10`, `IQ-A-29`

```gherkin
Et j'exécute le cas "iq.signature.externe"
Quand j'exécute le cas "iq.preuve.export"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — journal"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.signature.externe`** — Signature externe : invitation reçue par courriel, vérification d'identité, consentement, signature ; attestation CAdES ; trousse terminée et document signé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `marie` ← `$.id` |
| 4 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `` |  | 2xx | — |
| 7 | `GET /api/clm/portail/${jeton}` | anonyme | 2xx | `$.verifiee` egal false ; `$.niveau` egal "QUESTION" |
| 8 | `POST /api/clm/portail/${jeton}/verification` | anonyme | 2xx | — |
| 9 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 400, 401, 403, 409 | — |
| 10 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 2xx | `$.verifiee` egal true ; `$.documents` longueurMin 1 |
| 11 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 400, 409 | — |
| 12 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 2xx | `$.empreinteDocuments` existe true ; `$.attestation` contient "CAC-" ; capture `attestation` ← `$.attestation` |
| 13 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 409, 410 | — |
| 14 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.resume.statut` egal "TERMINEE" ; `$.documents[*].statut` inclut "SIGNE" |
| 15 | `GET /api/clm/trousses/${trousse}/preuve` |  | 2xx | `$.signatures[0].niveau` egal "QUESTION" ; `$.signatures[0].consentementLe` existe true ; `$.signatures[0].attestation.fournisseur` egal "clm-cachet" ; `$.integrite.integre` egal true |

**Cas `iq.preuve.export`** — Dossier de preuve d'une trousse signée : consultation, intégrité, certificat PDF, export ZIP (documents, .p7s, journal, manifeste) ; journal d'audit exportable *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/trousses/${trousse}/preuve` |  | 2xx | `$.documentsFinaux` longueurMin 1 ; `$.evenements` longueurMin 5 ; `$.signatures` longueurMin 1 |
| 2 | `POST /api/clm/trousses/${trousse}/preuve/integrite` |  | 2xx | `$.integre` egal true ; `$.journalIntegre` egal true ; `$.documentsAlteres` egal [] |
| 3 | `GET /api/clm/trousses/${trousse}/preuve/certificat` |  | 2xx | contient « %PDF » |
| 4 | `GET /api/clm/trousses/${trousse}/preuve/export` |  | 2xx | contient « PK » ; contient « certificat.pdf » ; contient « manifeste.json » ; contient « .p7s » ; contient « journal.json » |
| 5 | `GET /api/clm/admin/journal?trousse=${trousseRef}` | administration d'affaires IQ | 2xx | contient « SIGNATURE » |
| 6 | `GET /api/clm/admin/journal/export?trousse=${trousseRef}` | auditeur IQ | 2xx | contient « SIGNATURE » |

#### Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Preuve, journal, conservation et exportation (Cahier § 1.4.11, § 1.4.12 ; scénarios 7 et 10 ; addenda 2, 3, 4) (`bdd/features/iq/05-preuve-conservation.feature`)
- **Exigences éprouvées :** `IQ-C-66`, `IQ-A-21`, `IQ-A-11`, `IQ-A-22`

```gherkin
Et j'exécute le cas "iq.signature.externe"
Quand j'exécute le cas "iq.conservation"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — conservation"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.signature.externe`** — Signature externe : invitation reçue par courriel, vérification d'identité, consentement, signature ; attestation CAdES ; trousse terminée et document signé *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/participants` |  | 2xx | capture `marie` ← `$.id` |
| 4 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_s1` ← `$[0].id` |
| 5 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 6 | `` |  | 2xx | — |
| 7 | `GET /api/clm/portail/${jeton}` | anonyme | 2xx | `$.verifiee` egal false ; `$.niveau` egal "QUESTION" |
| 8 | `POST /api/clm/portail/${jeton}/verification` | anonyme | 2xx | — |
| 9 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 400, 401, 403, 409 | — |
| 10 | `POST /api/clm/portail/${jeton}/verification/reponse` | anonyme | 2xx | `$.verifiee` egal true ; `$.documents` longueurMin 1 |
| 11 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 400, 409 | — |
| 12 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 2xx | `$.empreinteDocuments` existe true ; `$.attestation` contient "CAC-" ; capture `attestation` ← `$.attestation` |
| 13 | `POST /api/clm/portail/${jeton}/signer` | anonyme | 409, 410 | — |
| 14 | `GET /api/clm/trousses/${trousse}` |  | 2xx | `$.resume.statut` egal "TERMINEE" ; `$.documents[*].statut` inclut "SIGNE" |
| 15 | `GET /api/clm/trousses/${trousse}/preuve` |  | 2xx | `$.signatures[0].niveau` egal "QUESTION" ; `$.signatures[0].consentementLe` existe true ; `$.signatures[0].attestation.fournisseur` egal "clm-cachet" ; `$.integrite.integre` egal true |

**Cas `iq.conservation`** — Conservation par type de contrat : règle, échéance, gel légal posé puis levé ; exportation en lot ; relevé d'utilisation par enveloppe *(données : `bdd/donnees/iq.json` ; profil : administration d'affaires IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `PUT /api/clm/admin/conservation/regles` |  | 2xx | `$.dureeAns` egal 7 |
| 2 | `GET /api/clm/admin/conservation/regles` |  | 2xx | `$[*].typeContrat` inclut "GARANTIE" |
| 3 | `GET /api/clm/admin/conservation/trousses?echues=false` |  | 2xx | `$[*].trousseId` inclut "${trousse}" |
| 4 | `POST /api/clm/admin/conservation/trousses/${trousse}/gel` |  | 2xx | — |
| 5 | `POST /api/clm/admin/conservation/trousses/${trousse}/detruire` |  | 409 | — |
| 6 | `DELETE /api/clm/admin/conservation/trousses/${trousse}/gel` |  | 2xx | — |
| 7 | `GET /api/clm/admin/conservation/exportation?du=${aujourdhui}&au=${demain}` |  | 2xx | contient « PK » |
| 8 | `GET /api/clm/admin/utilisation?du=${aujourdhui}&au=${demain}` |  | 2xx | `$.envoyees` existe true ; `$.terminees` existe true ; `$.renvois` existe true |

### Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2)


#### Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-A-01`, `IQ-A-02`, `IQ-C-06`, `IQ-C-16`, `IQ-C-69`, `IQ-D-12`, `IQ-D-26`, `IQ-D-27`, `IQ-D-28`, `IQ-A-07`, `IQ-C-03`

```gherkin
Quand j'exécute le cas "iq.integration.api"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — intégrations"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.integration.api`** — API d'intégration (Workday, Salesforce) : client créé, secret remis une fois, jeton OAuth, trousse et lot créés par l'API, statut et preuve lus, client révoqué *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/admin/clients-integration` |  | 2xx | `$.secret` existe true ; capture `client` ← `$.client.id` ; capture `clientId` ← `$.client.clientId` ; capture `secret` ← `$.secret` |
| 2 | `GET /api/clm/admin/clients-integration` |  | 2xx | ne contient pas « ${secret} » |
| 3 | `POST /api/clm/integration/oauth/token` | anonyme | 2xx | `$.token_type` egal "Bearer" ; capture `jeton` ← `$.access_token` |
| 4 | `GET /api/clm/integration/v1/modeles` | anonyme | 2xx | `$` existe true |
| 5 | `POST /api/clm/integration/v1/trousses` | anonyme | 200, 201 | `$.referenceExterne` egal "WD-${UNIQUE}" ; capture `trousseApi` ← `$.id` |
| 6 | `GET /api/clm/integration/v1/trousses/${trousseApi}` | anonyme | 2xx | `$.statut` existe true |
| 7 | `GET /api/clm/trousses/${trousseApi}` | initiateur IQ | 2xx | `$.resume.source` contient "WORKDAY" |
| 8 | `POST /api/clm/admin/clients-integration/${client}/revoquer` |  | 2xx | — |
| 9 | `GET /api/clm/integration/v1/modeles` | anonyme | 401, 403 | — |

#### Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-C-08`, `IQ-C-13`, `IQ-C-15`, `IQ-A-05`, `IQ-A-10`, `IQ-A-12`, `IQ-A-14`

```gherkin
Quand j'exécute le cas "iq.salesforce.statuts"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.salesforce.statuts`** — Retour des statuts vers Salesforce (lancée, retournée), panne simulée puis reprise automatique ; JWT Bearer exige une clé privée *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `PUT /api/clm/trousses/${trousse}/parcours` |  | 2xx | capture `e_v1` ← `$[0].id` ; capture `e_a1` ← `$[1].id` |
| 4 | `POST /api/clm/trousses/${trousse}/lancer` |  | 2xx | `$.resume.statut` egal "EN_TRAITEMENT" |
| 5 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 2xx | — |
| 6 | `POST /api/clm/etapes/${e_a1}/decider` | approbateur IQ | 2xx | — |
| 7 | `POST /api/clm/admin/integrations/salesforce/relayer` | administration technique IQ | 2xx | — |
| 8 | `GET /api/clm/admin/integrations/salesforce` | administration technique IQ | 2xx | contient « LANCEE » ; contient « RETOURNEE » ; `$.config.reprisesMax` egal 8 |
| 9 | `PUT /api/clm/admin/integrations/salesforce/simulation/panne` | administration technique IQ | 2xx | — |
| 10 | `POST /api/clm/etapes/${e_v1}/decider` | validateur IQ | 2xx | — |
| 11 | `POST /api/clm/admin/integrations/salesforce/relayer` | administration technique IQ | 2xx | — |
| 12 | `GET /api/clm/admin/integrations/salesforce` | administration technique IQ | 2xx | `$.enEchec` different -1 |
| 13 | `PUT /api/clm/admin/integrations/salesforce/simulation/panne` | administration technique IQ | 2xx | — |
| 14 | `POST /api/clm/admin/integrations/salesforce/trousses/${trousse}/relancer` | administration technique IQ | 2xx | — |
| 15 | `PUT /api/clm/admin/integrations/salesforce/configuration` | administration technique IQ | 400 | contient « Clé privée requise » |

#### Seuls les champs Salesforce déclarés sont lus

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-A-13`

```gherkin
Quand j'exécute le cas "iq.salesforce.minimisation"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.salesforce.minimisation`** — Seuls les champs déclarés sont lus dans Salesforce *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/salesforce/dossiers/DEM-2026-0002` |  | 2xx | ne contient pas « NotesInternes__c » ; `$.champs.Montant` existe true |

#### Signature en lot depuis la plateforme et approbation globale du lot

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-A-03`, `IQ-A-28`

```gherkin
Quand j'exécute le cas "iq.lots"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — signature en lot"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.lots`** — Signature en lot depuis la plateforme : une trousse par dossier, chaque échec isolé ; approbation globale du lot tracée par dossier *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/modeles` |  | 2xx | capture `modele` ← `$.id` |
| 2 | `POST /api/clm/modeles/${modele}/soumettre` |  | 2xx | — |
| 3 | `POST /api/clm/modeles/${modele}/publier` | administration d'affaires IQ | 2xx | — |
| 4 | `POST /api/clm/lots` |  | 200, 201 | `$.elements` longueurMin 3 ; `$.reussis` egal 2 ; `$.echecs` egal 1 ; capture `lot` ← `$.id` |
| 5 | `POST /api/clm/lots/${lot}/approuver` | approbateur IQ | 2xx | `$.approuvees` egal 2 |
| 6 | `GET /api/clm/lots/${lot}` |  | 2xx | `$.libelle` egal "Lot ${unique}" |

#### Les interfaces sont standards et documentées (OpenAPI, SCIM)

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-D-29`, `IQ-C-16`

```gherkin
Quand j'exécute le cas "iq.api.documentee"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.api.documentee`** — Interfaces standards documentées : OpenAPI publie l'API CLM et l'API d'intégration ; SCIM découvrable *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /v3/api-docs` |  | 2xx | `$.paths./api/clm/integration/v1/trousses` existe true ; `$.paths./scim/v2/Users` existe true |

#### Un assistant d'IA se connecte au connecteur MCP et lit, en lecture seule, le statut d'une trousse de son client

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2) (`bdd/features/iq/06-integrations.feature`)
- **Exigences éprouvées :** `IQ-A-09`

```gherkin
Quand j'exécute le cas "iq.mcp"
Alors la plateforme se comporte comme attendu
Quand un assistant MCP de référence se connecte avec le jeton capturé
Alors l'assistant voit des outils en lecture seule et lit la trousse de son client
```

**Cas `iq.mcp`** — Connecteur MCP : un assistant d'IA s'authentifie avec le jeton d'un client d'intégration, liste des outils en lecture seule, lit le statut d'une trousse de ce client ; une origine étrangère et un jeton absent sont refusés *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/admin/clients-integration` |  | 2xx | capture `clientId` ← `$.client.clientId` ; capture `secret` ← `$.secret` |
| 2 | `POST /api/clm/integration/oauth/token` | anonyme | 2xx | capture `jeton` ← `$.access_token` |
| 3 | `POST /api/clm/integration/mcp` | anonyme | 401 | — |
| 4 | `POST /api/clm/integration/mcp` | anonyme | 403 | — |
| 5 | `POST /api/clm/integration/mcp` | anonyme | 2xx | `$.result.protocolVersion` egal "2025-06-18" ; `$.result.serverInfo.name` egal "vina-clm" |
| 6 | `POST /api/clm/integration/mcp` | anonyme | 2xx | `$.result.tools[*].name` inclut "lire_trousse" ; `$.result.tools[*].annotations.readOnlyHint` exclut false |
| 7 | `POST /api/clm/integration/v1/trousses` | anonyme | 201 | capture `trousse` ← `$.id` |
| 8 | `POST /api/clm/integration/mcp` | anonyme | 2xx | `$.result.isError` egal false ; `$.result.structuredContent.referenceExterne` egal "MCP-${UNIQUE}" |
| 9 | `POST /api/clm/integration/mcp` | anonyme | 2xx | `$.error.code` egal -32602 |

### Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4)


#### Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-D-15`, `IQ-D-16`, `IQ-D-17`, `IQ-A-26`, `IQ-C-67`

```gherkin
Quand j'exécute le cas "iq.acces.administrations"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.acces.administrations`** — Moindre privilège : l'administration technique ne lit aucune trousse ; l'administration d'affaires gère le contenu ; seule l'administration principale attribue les rôles ; l'auditeur consulte sans agir *(données : `bdd/donnees/iq.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clm/admin/integrations/salesforce` | administration technique IQ | 2xx | — |
| 2 | `GET /api/clm/trousses?page=0&taille=5` | administration technique IQ | 2xx | `$.lignes` egal [] |
| 3 | `GET /api/clm/admin/conservation/regles` | administration technique IQ | 403 | — |
| 4 | `GET /api/clm/admin/integrations/salesforce` | administration d'affaires IQ | 403 | — |
| 5 | `GET /api/admin/users/roles` | administration principale IQ | 2xx | contient « ADMIN_TECHNIQUE » ; contient « ADMIN_AFFAIRES » |
| 6 | `GET /api/admin/users/roles` | administration technique IQ | 403 | — |
| 7 | `GET /api/clm/suivi?page=0&taille=5` | auditeur IQ | 2xx | `$.lignes` existe true |
| 8 | `POST /api/clm/trousses` | auditeur IQ | 403 | — |
| 9 | `GET /api/documents` | initiateur IQ | 403, 404 | — |

#### Comptes créés avec un rôle, rattachés à un groupe, puis désactivés ; départ d'un employé consulté

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-D-05`, `IQ-D-18`, `IQ-C-67`

```gherkin
Quand j'exécute le cas "iq.acces.comptes"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — utilisateurs"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.acces.comptes`** — Gestion des comptes : création avec rôle, groupe du module, retrait d'accès tracé ; départ d'un employé *(données : `bdd/donnees/iq.json` ; profil : administration principale IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 2xx | capture `compte` ← `$.id` |
| 2 | `POST /api/clm/admin/groupes` | administration d'affaires IQ | 2xx | capture `groupe` ← `$.id` |
| 3 | `POST /api/clm/admin/groupes/${groupe}/membres` | administration d'affaires IQ | 2xx | — |
| 4 | `GET /api/clm/groupes` | administration d'affaires IQ | 2xx | contient « bdd.${unique}@iq.example » |
| 5 | `PUT /api/admin/users/${compte}` |  | 2xx | `$.actif` egal false |
| 6 | `POST /api/auth/login` | anonyme | 401, 403 | — |
| 7 | `GET /api/clm/admin/departs?courriel=validateur@iq.example` | administration d'affaires IQ | 2xx | `$.courriel` egal "validateur@iq.example" |

#### Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-A-08`, `IQ-A-19`, `IQ-A-20`, `IQ-A-27`

```gherkin
Quand j'exécute le cas "iq.annuaire.scim"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.annuaire.scim`** — Annuaire : correspondance groupe Entra ID → rôle et domaine ; provisionnement SCIM 2.0 (Saviynt) puis désactivation immédiate *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/annuaire/correspondances` |  | 2xx | capture `correspondance` ← `$.id` |
| 2 | `POST /api/admin/annuaire/scim/jeton` |  | 2xx | capture `jetonScim` ← `$.jeton` |
| 3 | `GET /scim/v2/ServiceProviderConfig` | anonyme | 2xx | contient « urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig » |
| 4 | `POST /scim/v2/Users` | anonyme | 201 | `$.active` egal true ; capture `scimId` ← `$.id` |
| 5 | `PATCH /scim/v2/Users/${scimId}` | anonyme | 2xx | `$.active` egal false |
| 6 | `DELETE /api/admin/annuaire/correspondances/${correspondance}` |  | 200, 204 | — |

#### Authentification unique configurable et connexion locale réservable aux comptes de secours

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-C-60`

```gherkin
Quand j'exécute le cas "iq.sso.connexion"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — authentification unique"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.sso.connexion`** — Authentification unique (OIDC, SAML) configurable ; connexion locale réservable aux comptes de secours *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/auth/config?slug=iq` | anonyme | 2xx | `$` existe true |
| 2 | `GET /api/admin/connexion-locale` |  | 2xx | `$.comptes` longueurMin 1 ; `$.reservee` existe true |

#### La session se termine après l'inactivité et au plus tard à sa durée maximale, réglées par l'administration technique

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-C-60`

```gherkin
Quand j'exécute le cas "iq.session.politique"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — authentification unique"
Alors je vois "Politique de session"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.session.politique`** — L'administration technique fixe la politique de session de l'organisation (inactivité, durée maximale) ; un réglage dangereux est refusé ; l'administration d'affaires n'y a pas accès *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/admin/session` |  | 2xx | `$.inactiviteMinutes` existe true ; `$.dureeMaxMinutes` existe true |
| 2 | `PUT /api/admin/session?inactiviteMinutes=30&dureeMaxMinutes=480` |  | 2xx | `$.propre` egal true ; `$.inactiviteMinutes` egal 30 ; `$.dureeMaxMinutes` egal 480 |
| 3 | `PUT /api/admin/session?inactiviteMinutes=1&dureeMaxMinutes=480` |  | 400 | contient « inactivité » |
| 4 | `PUT /api/admin/session?inactiviteMinutes=120&dureeMaxMinutes=60` |  | 400 | — |
| 5 | `GET /api/admin/session` | administration d'affaires IQ | 403 | — |
| 6 | `PUT /api/admin/session?inactiviteMinutes=60&dureeMaxMinutes=480` |  | 2xx | — |

#### Les données d'IQ sont introuvables pour une autre organisation

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-C-07`, `IQ-D-20`

```gherkin
Quand j'exécute le cas "iq.isolation"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.isolation`** — Protection des données : une trousse d'IQ est introuvable pour une autre organisation *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `GET /api/clm/trousses/${trousse}` | administrateur de Lévis | 403, 404 | — |

#### Aucune carte de paiement n'entre dans la plateforme, qui reste hors du périmètre PCI DSS

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4) (`bdd/features/iq/07-acces-securite.feature`)
- **Exigences éprouvées :** `IQ-D-14`

```gherkin
Quand j'exécute le cas "iq.cartes.pci"
Alors la plateforme se comporte comme attendu
```

**Cas `iq.cartes.pci`** — Aucun numéro de carte de paiement n'entre dans la plateforme : il est refusé à la frontière (422), sans être renvoyé ; la même requête sans numéro est acceptée *(données : `bdd/donnees/iq.json` ; profil : administration technique IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/admin/clients-integration` |  | 422 | `$.error` egal "carte_paiement_refusee" ; ne contient pas « 4111 » |
| 2 | `POST /api/clm/admin/clients-integration` |  | 422 | — |
| 3 | `GET /api/clm/admin/clients-integration?q=5555-5555-5555-4444` |  | 422 | — |
| 4 | `POST /api/clm/admin/clients-integration` |  | 201 | — |
| 5 | `GET /api/clm/admin/clients-integration` |  | 2xx | ne contient pas « 4111 » ; contient « Système ${unique} » |

### Interface, mobilité, langues, accessibilité et IA (Cahier § 1.4.8, § 1.4.9 ; DAO § 3.3.3 ; scénario 8)


#### Consultation et décision sur téléphone, sans débordement ni violation d'accessibilité

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Interface, mobilité, langues, accessibilité et IA (Cahier § 1.4.8, § 1.4.9 ; DAO § 3.3.3 ; scénario 8) (`bdd/features/iq/08-interface-ia.feature`)
- **Exigences éprouvées :** `IQ-C-53`, `IQ-S-08`, `IQ-D-13`

```gherkin
Quand j'ouvre l'écran "IQ — mes tâches (téléphone)"
Alors l'écran est accessible, sans débordement ni superposition
Quand j'ouvre l'écran "IQ — mes trousses (téléphone)"
Alors l'écran est accessible, sans débordement ni superposition
```

#### Les interfaces et la console d'administration sont en français, offertes en anglais

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Interface, mobilité, langues, accessibilité et IA (Cahier § 1.4.8, § 1.4.9 ; DAO § 3.3.3 ; scénario 8) (`bdd/features/iq/08-interface-ia.feature`)
- **Exigences éprouvées :** `IQ-D-21`, `IQ-D-23`, `IQ-D-22`, `IQ-A-15`, `IQ-D-13`

```gherkin
Quand j'ouvre l'écran "IQ — notifications"
Alors je vois "Notifications"
Quand je choisis la langue "English"
Alors je vois "Notifications"
Et l'écran est accessible, sans débordement ni superposition
Quand je choisis la langue "Français"
```

#### L'analyse automatisée d'un document est soumise à une révision humaine obligatoire

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Interface, mobilité, langues, accessibilité et IA (Cahier § 1.4.8, § 1.4.9 ; DAO § 3.3.3 ; scénario 8) (`bdd/features/iq/08-interface-ia.feature`)
- **Exigences éprouvées :** `IQ-C-59`, `IQ-C-04`, `IQ-S-10b`

```gherkin
Quand j'exécute le cas "iq.ia.analyse"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "IQ — accueil CLM"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `iq.ia.analyse`** — Analyse automatisée d'un document (catégorie, anomalies) soumise à révision humaine obligatoire *(données : `bdd/donnees/iq.json` ; profil : initiateur IQ)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clm/trousses` |  | 2xx | `$.resume.statut` egal "BROUILLON" ; capture `trousse` ← `$.resume.id` ; capture `trousseRef` ← `$.resume.reference` |
| 2 | `POST /api/clm/trousses/${trousse}/documents` |  | 2xx | `$.analyse` egal "PROPRE" ; capture `document` ← `$.id` ; capture `documentCle` ← `$.cleDocument` ; envoi multipart (fichier) |
| 3 | `POST /api/clm/trousses/${trousse}/documents/${document}/analyse` |  | 2xx | `$.statut` egal "A_REVISER" ; `$.resultat.anomalies` existe true ; capture `analyse` ← `$.id` |
| 4 | `POST /api/clm/analyses/${analyse}/revision` |  | 400 | — |
| 5 | `POST /api/clm/analyses/${analyse}/revision` |  | 2xx | `$.statut` egal "VALIDEE" ; `$.reviseePar` egal "initiateur@iq.example" |

