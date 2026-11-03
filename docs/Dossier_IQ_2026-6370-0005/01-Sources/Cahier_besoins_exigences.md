# Cahier des besoins et exigences — conversion Markdown

> **Source** : PDF transmis par l'utilisateur — dossier 2026-6370-0005.  
> **Méthode** : extraction du texte PDF avec conservation des titres, listes et repères de pages autant que possible.  
> **Limite** : la mise en page, signatures manuscrites, tableaux graphiques et éléments visuels ne sont pas reproduits à l'identique. Les tableaux textuels sont conservés sous forme de texte lorsque l'extraction le permet.

---

APPEL D’OFFRES # 2026-6370-0005

   CAHIER DES BESOINS ET EXIGENCES

        DOSSIER 2026-6370-0005

SOLUTION DE SIGNATURE ÉLECTRONIQUE ET
     DE GÉNÉRATION DE CONTRATS

---

                                                                          APPEL D’OFFRES # 2026-6370-0005

TABLE DES MATIERES

1        DESCRIPTION DE LA SOLUTION ET DU PÉRIMÈTRE ________________________________________ 4
1.1      CONTEXTE GÉNÉRAL _____________________________________________________________ 4
1.2      DÉFINITIONS ET RÈGLES D’INTERPRÉTATION ____________________________________________ 4
1.3      SOLUTION ET SERVICES RECHERCHÉS _______________________________________________ 14
1.4      EXIGENCES DE LA SOLUTION RECHERCHÉE ___________________________________________ 14
      1.4.1     GÉNÉRALITÉS ___________________________________________________________ 14
      1.4.2    HÉBERGEMENT DES DONNÉES _______________________________________________ 15
      1.4.3    EXIGENCES D’ARCHITECTURE ________________________________________________ 15
      1.4.4    EXIGENCES D’INTÉGRATION _________________________________________________ 15
      1.4.5    GÉNÉRATION AUTOMATISÉE DE DOCUMENTS POUR SIGNATURE _______________________ 16
      1.4.6    EXIGENCES FONCTIONNELLES________________________________________________ 16
      1.4.7    EXIGENCES FONCTIONNELLES DÉTAILLÉES ______________________________________ 16
      1.4.8    EXIGENCES D’EFFICACITÉ OPÉRATIONNELLE _____________________________________ 18
      1.4.9    FONCTIONNALITÉS COMPLÉMENTAIRES (OPTIONNELLES) ____________________________ 18
      1.4.10   EXIGENCES TECHNOLOGIQUES _______________________________________________ 18
      1.4.11   CONFORMITÉ ET RECONNAISSANCE DE LA SIGNATURE ÉLECTRONIQUE __________________ 18
      1.4.12   EXIGENCES DE SÉCURITÉ ___________________________________________________ 19
1.5      NOMBRE D’ENVOIS _____________________________________________________________ 20
1.6      SOLUTION ÉPROUVÉE ___________________________________________________________ 20
1.7      ENVIRONNEMENTS TECHNOLOGIQUE D’IQ ____________________________________________ 20
      1.7.1    ENVIRONNEMENT TECHNOLOGIQUE ACTUEL______________________________________ 20
1.8      ENVIRONNEMENTS _____________________________________________________________ 21
2        SERVICES PROFESSIONNELS / RÉALISATION ___________________________________________ 23
2.1      SERVICES PROFESSIONNELS ______________________________________________________ 23
2.2      ÉCHÉANCIER GLOBAL ___________________________________________________________ 23
2.3      GESTION DU PROJET ____________________________________________________________ 23
2.4      ÉQUIPE PROJET _______________________________________________________________ 24
2.5      DOCUMENTATION ______________________________________________________________ 25
2.6      TESTS ______________________________________________________________________ 25
2.7      DÉMONSTRATION ______________________________________________________________ 26
2.8      PREUVE DE CONCEPT ___________________________________________________________ 26
3        LIVRAISON, DÉPLOIEMENT ET STABILISATION ___________________________________________ 27
3.1      PÉRIODE DE STABILISATION _______________________________________________________ 27
3.2      ÉVALUATION ET ACCEPTATION DES BIENS LIVRABLES ____________________________________ 28
3.3      TRANSITION __________________________________________________________________ 28
4        FORMATION ET TRANSFERT _______________________________________________________ 30
4.1      FORMATION __________________________________________________________________ 30
4.2      TRANSFERT DE CONNAISSANCES ___________________________________________________ 30
5        EXPLOITATION DE LA SOLUTION ____________________________________________________ 31

---

                                                                           APPEL D’OFFRES # 2026-6370-0005

5.1      MAINTENANCE APPLICATIVE ET MISE À JOUR DE LA SOLUTION ______________________________ 31
5.2      SUPPORT ET ENTRETIEN _________________________________________________________ 31
5.3      SLA MINIMAUX ET NIVEAUX DE SERVICE OBLIGATOIRES ___________________________________ 32
5.4      TAUX DE DISPONIBILITÉ __________________________________________________________ 33
5.5      RAPPORTS D’INTERVENTIONS _____________________________________________________ 33
5.6      ÉVOLUTION DE LA SOLUTION ______________________________________________________ 34
6        DURÉE, GARANTIE, COÛTS ________________________________________________________ 35
6.1      DURÉE DU CONTRAT ____________________________________________________________ 35
6.2      TABLEAU DES PRIX _____________________________________________________________ 35
      6.2.1    COÛTS DES SERVICES RÉCURRENTS ___________________________________________ 36
      6.2.2    AUTRES PRODUITS ET SERVICES ______________________________________________ 36
      6.2.3    FLUCTUATION DU NOMBRE D’UTILISATEURS, DE DOCUMENTS OU AUTRES ________________ 36
      6.2.4    COÛTS FORFAITAIRES POUR LA MISE EN PLACE DE LA SOLUTION_______________________ 37
      6.2.5    PRIX FORFAITAIRE POUR LA FORMATION ________________________________________ 37
      6.2.6    TAUX HORAIRES DES RESSOURCES (SUR DEMANDE D’IQ SEULEMENT) __________________ 38
      6.2.7    FRAIS QUOTIDIENS DES RESSOURCES EXTÉRIEURES _______________________________ 38
      6.2.8    COÛTS DE TRADUCTION (À LA DEMANDE D’IQ SEULEMENT) ___________________________ 38
6.3      FERMETÉ DES PRIX _____________________________________________________________ 38
6.4      GARANTIE DES SERVICES_________________________________________________________ 39

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

1        DESCRIPTION DE LA SOLUTION ET DU PÉRIMÈTRE
1.1      CONTEXTE GÉNÉRAL

Investissement Québec (ci-après appelée « IQ ») est une société d’État qui a pour mandat de participer
activement au développement économique du Québec conformément aux grandes orientations du
gouvernement en cette matière. Elle vise à stimuler l’innovation dans les entreprises, l’entrepreneuriat et le
repreneuriat ainsi que la croissance de l’investissement et des exportations et à promouvoir notamment les
emplois à haute valeur ajoutée dans toutes les régions du Québec.

Pour accomplir cette mission, IQ soutient la création et le développement des entreprises de toute taille par
des solutions financières adaptées et des investissements, ainsi que par des services conseils aux
entrepreneurs et d’autres mesures d’accompagnement, notamment technologiques, et ce, en cherchant à
présenter une offre complémentaire à celle de ses partenaires.

1.2      DÉFINITIONS ET RÈGLES D’INTERPRÉTATION

Aux fins du présent Cahier des besoins et exigences et de l’ensemble des Documents d’appel d’offres, les
termes et expressions ci-dessous, lorsqu’ils comportent une majuscule, ont la signification qui leur est
attribuée dans la présente section, à moins que le contexte n’indique clairement un sens différent.

Acceptation finale

Désigne la confirmation écrite par Investissement Québec que la Solution, ses Environnements, ses
Configurations, ses Paramétrages, ses Intégrations, les Services professionnels et les Biens livrables requis
ont été livrés, vérifiés et acceptés conformément aux Documents contractuels et aux critères d’acceptation
applicables.

La Mise en production, l’utilisation provisoire de la Solution, le paiement d’une facture ou l’acceptation d’un
Bien livrable particulier ne constituent pas, à eux seuls, l’Acceptation finale de la Solution.

L’Acceptation finale ne libère pas l’Adjudicataire de ses obligations de correction, de garantie, de sécurité,
de soutien, de maintenance, de continuité ou de Transition.

Administrateur d’affaires

Désigne un Utilisateur autorisé par IQ à configurer ou à administrer les paramètres fonctionnels et
opérationnels de la Solution, notamment les Modèles contractuels, les Clauses contractuelles, les
Workflows, les circuits de validation et d’approbation, les règles d’affaires, les groupes d’Utilisateurs et les
droits fonctionnels, selon les autorisations qui lui sont accordées.

Administrateur technique

Désigne un Utilisateur autorisé par IQ à administrer les composantes techniques de la Solution, notamment
les accès, les rôles, les Configurations techniques, les Intégrations, les Environnements, les mécanismes
de sécurité, les journaux techniques, les API et les paramètres d’exploitation.

Adjudicataire

Désigne le Soumissionnaire retenu par IQ à la suite du processus d’Appel d’offres et avec lequel le Contrat
est conclu pour fournir, configurer, implanter, intégrer, déployer, héberger, exploiter, maintenir, soutenir et
faire évoluer la Solution ainsi que pour fournir les Services professionnels et les services connexes requis.

Page 4                                                                                          Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

Lorsque le Soumissionnaire retenu est un Consortium ou un regroupement de co-soumissionnaires, le
terme « Adjudicataire » désigne collectivement ses membres, sous réserve des dispositions particulières
des Documents contractuels.

API ou interface de programmation applicative

Désigne un mécanisme permettant l’échange automatisé et sécurisé de données, de Documents, de
métadonnées, de statuts, d’événements ou d’instructions entre la Solution et les systèmes d’IQ ou ceux de
tiers autorisés.

Appel d’offres

Désigne le processus de sollicitation public entrepris par IQ relativement au dossier 2026-6370-0005 pour
l’acquisition, la Configuration, l’implantation, l’Intégration, le déploiement, l’hébergement, le soutien,
l’entretien et l’évolution d’une Solution de Génération de contrat et de signature électronique ainsi que pour
l’obtention des Services professionnels et des services connexes requis.

Authentification renforcée

Désigne une méthode d’authentification comportant une ou plusieurs mesures additionnelles destinées à
accroître le niveau d’assurance relatif à l’identité d’un Utilisateur ou d’un signataire, selon la nature du
Document, le niveau de risque de la transaction ou les exigences applicables.

Bien livrable ou Livrable

Désigne tout document, analyse, Configuration, Paramétrage, Intégration, rapport, plan, procédure,
développement, résultat d’essai, matériel de formation, donnée, fichier ou autre élément que l’Adjudicataire
doit produire, remettre ou rendre accessible à IQ dans le cadre du Contrat.

Bordereau de prix

Désigne le fichier Excel officiel fourni avec les Documents d’appel d’offres et devant obligatoirement être
rempli et déposé par le Soumissionnaire dans son format natif.

Le Bordereau de prix contient notamment les prix, les coûts forfaitaires et récurrents, les taux horaires, les
hypothèses de Volumétrie, les options de renouvellement, les coûts des Intégrations et les autres
renseignements financiers requis pour l’évaluation de la Soumission et l’administration éventuelle du
Contrat.

Cahier des besoins et exigences

Désigne le présent document, qui décrit notamment la Solution recherchée, son périmètre, les exigences
fonctionnelles, technologiques, opérationnelles, d’architecture, d’Intégration et de sécurité, les Services
professionnels, les Biens livrables, les niveaux de service, la formation, la Transition et les autres besoins
d’IQ.

Clause contractuelle

Désigne une stipulation, une disposition ou un texte juridique, administratif ou commercial pouvant être
intégré à un Modèle contractuel ou à un Document généré.

Une Clause contractuelle peut être fixe, obligatoire, facultative ou conditionnelle et peut être sélectionnée,
insérée ou adaptée selon des données, des règles d’affaires ou des paramètres configurés ou autorisés
par IQ.

Une Clause contractuelle intégrée à un Document généré ne doit pas être confondue avec une clause des
Documents contractuels régissant la relation entre IQ et l’Adjudicataire.

Page 5                                                                                        Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

Configuration

Désigne l’adaptation de la Solution au moyen des fonctions, paramètres et outils standards offerts dans sa
version commercialisée, sans ajout ni modification de code développé spécifiquement pour IQ.

Consortium

Désigne un regroupement de deux ou plusieurs personnes morales, personnes physiques ou sociétés
présentant conjointement une Soumission dans le cadre de l’Appel d’offres.

Le Consortium doit identifier ses membres, son représentant autorisé, la répartition des responsabilités, les
services exécutés par chacun de ses membres et les mécanismes de gouvernance proposés. Sauf
disposition contraire expressément acceptée par IQ, ses membres sont solidairement responsables de
l’exécution du Contrat.

Contrat

Désigne l’entente conclue entre IQ et l’Adjudicataire à la suite de l’adjudication, incluant l’ensemble des
Documents contractuels qui en font partie conformément à la clause intitulée « Ordre de priorité des
documents contractuels ».

Aux fins d’éviter toute ambiguïté, le terme « Contrat » ne désigne pas un contrat d’affaires, une entente,
une convention ou un autre Document généré par la Solution pour les activités d’IQ.

Date limite de réception des soumissions

Désigne la date et l’heure limites indiquées dans les Documents d’appel d’offres ou dans le dernier Addenda
applicable pour la réception des Soumissions.

Développement spécifique

Désigne toute programmation, modification de code, extension, interface, connecteur, composante ou
fonctionnalité créée ou adaptée spécifiquement pour IQ et qui ne fait pas partie des capacités standards de
la version commercialisée de la Solution à la Date limite de réception des soumissions.

Document

Désigne tout fichier, formulaire, contrat d’affaires, entente, convention, lettre, rapport, approbation, pièce
justificative ou autre contenu pris en charge par la Solution dans le cadre d’un processus de Génération de
contrat, de préparation, de validation, d’approbation, de transmission, de signature, de conservation,
d’exportation ou de consultation.

Aux seules fins de la tarification et du calcul de la Volumétrie, un Document est comptabilisé selon l’unité
de tarification précisée dans le Bordereau de prix, notamment par envoi, Trousse ou transaction,
indépendamment du nombre de fichiers ou de pages qu’il contient.

Le Soumissionnaire doit préciser clairement toute autre méthode de comptabilisation applicable à sa
Solution.

Document généré

Désigne un Document produit automatiquement ou semi-automatiquement par la Solution dans le cadre
d’un processus de Génération de contrat, à partir d’un Modèle contractuel, de Clauses contractuelles, de
données, de métadonnées, de règles configurées ou de renseignements provenant d’un Système source.

Page 6                                                                                        Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

Un Document généré peut notamment être un contrat d’affaires, une entente, une convention, une lettre,
un formulaire ou tout autre Document destiné à être révisé, validé, approuvé, signé, transmis, exporté ou
conservé.

Un Document généré ne constitue pas, du seul fait de sa génération, de son enregistrement, de son
approbation ou de sa transmission, un Document contractuel régissant la relation entre IQ et l’Adjudicataire.

Documents d’appel d’offres

Désigne collectivement le document principal d’appel d’offres, le présent Cahier des besoins et exigences,
l’annexe intitulée « Exigences d’affaires, technologiques et de sécurité », le Bordereau de prix, le Tableau
de réponses, les formulaires, les annexes, les grilles, les scénarios de démonstration, les Addendas et tout
autre document publié par IQ sur le SÉAO relativement au présent Appel d’offres.

Documents contractuels

Désigne exclusivement les documents qui constituent et régissent le Contrat conclu entre IQ et
l’Adjudicataire, selon l’ordre de priorité prévu dans le document principal d’appel d’offres.

Les Documents contractuels comprennent les documents expressément identifiés dans la clause « Ordre
de priorité des documents contractuels », notamment les amendements au Contrat, l’entente contractuelle,
la lettre d’adjudication, le bon de commande, les Addendas, les Documents d’appel d’offres, les
clarifications acceptées par IQ et la Soumission de l’Adjudicataire.

Aux fins d’éviter toute ambiguïté, un Document, un Document généré, un Modèle contractuel, une Clause
contractuelle ou un contrat d’affaires produit au moyen de la Solution ne constitue pas un Document
contractuel régissant la relation entre IQ et l’Adjudicataire, à moins d’y être expressément intégré
conformément aux modalités de modification du Contrat.

Environnement

Désigne une instance distincte et isolée de la Solution destinée à une utilisation déterminée, notamment le
développement, l’Intégration, la préproduction, l’acceptation, la production, la démonstration, la formation
ou l’expérimentation en mode Sandbox.

Environnement de développement ou DEV

Désigne l’Environnement utilisé pour réaliser ou préparer les Configurations, les Paramétrages, les Modèles
contractuels, les Workflows, les Intégrations et les autres éléments devant être vérifiés avant leur transfert
vers un Environnement de préproduction ou d’acceptation.

Environnement d’intégration ou INT

Désigne, lorsque requis, un Environnement permettant de développer, de configurer, d’exécuter ou de
vérifier les échanges entre la Solution, les Systèmes sources et les autres systèmes autorisés d’IQ.

Environnement de préproduction, d’acceptation ou ACC

Désigne un Environnement distinct de la production permettant à IQ d’effectuer les essais, les validations
et les activités d’acceptation avant le déploiement d’une Configuration, d’une Intégration, d’un Modèle
contractuel, d’un Workflow, d’un correctif ou d’une nouvelle version dans l’Environnement de production.

Environnement de production ou PRD

Désigne l’Environnement dans lequel la Solution est exploitée pour les activités réelles d’IQ et dans lequel
sont traités les Documents, les Documents générés, les signatures, les transactions et les données
opérationnelles.

Page 7                                                                                        Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

Exigence obligatoire

Désigne une condition, une caractéristique, une obligation ou un résultat que le Soumissionnaire,
l’Adjudicataire ou la Solution proposée doit respecter.

Le non-respect d’une Exigence obligatoire identifiée comme éliminatoire ou assortie d’un seuil minimal peut
entraîner le rejet de la Soumission, conformément aux Documents d’appel d’offres.

Génération de contrat

Désigne la capacité de la Solution de produire automatiquement ou semi-automatiquement un Document
généré à partir d’un Modèle contractuel, de Clauses contractuelles, de données structurées, de
métadonnées, de règles configurées ou de renseignements provenant d’un Système source d’IQ ou d’un
système tiers autorisé.

La Génération de contrat peut notamment comprendre :

    •    la récupération ou la réception de données provenant d’un Système source;
    •    l’association des données aux champs d’un Modèle contractuel;
    •    le pré remplissage et la fusion automatisée des données;
    •    la sélection ou l’insertion conditionnelle de Clauses contractuelles;
    •    l’assemblage du Document généré;
    •    l’application de règles de validation;
    •    la production d’une version préliminaire;
    •    la révision ou la correction contrôlée des données ou du contenu;
    •    la gestion des versions;
    •    la transmission du Document généré dans un Workflow de validation, d’approbation et de signature
         électronique;
    •    la conservation des statuts, des événements et de la Piste d’audit associée.

Aux fins du présent Appel d’offres, la Génération de contrat constitue une capacité ciblée de création
documentaire et d’intégration au processus de signature électronique. Elle ne doit pas être interprétée
comme exigeant l’ensemble des fonctionnalités d’une plateforme complète de gestion du cycle de vie des
contrats ou « CLM », sauf lorsqu’une telle fonctionnalité est expressément prévue dans les Documents
d’appel d’offres.

La Génération de contrat est distincte de la simple création d’une Trousse, du téléversement manuel d’un
fichier existant et de la gestion des Documents contractuels régissant le Contrat entre IQ et l’Adjudicataire.

Incident

Désigne tout événement, anomalie, panne, défaillance, interruption, dégradation ou comportement non
conforme affectant ou susceptible d’affecter la disponibilité, la sécurité, l’intégrité, la performance ou
l’utilisation normale de la Solution ou de l’un de ses Composants.

Incident de confidentialité

Désigne tout accès, utilisation ou communication non autorisés par la loi d’un Renseignement personnel,
sa perte ou toute autre atteinte à sa protection.

Le terme comprend également tout incident soupçonné impliquant des Renseignements personnels d’IQ
devant faire l’objet d’un avis conformément aux Documents contractuels.

Incident de sécurité

Page 8                                                                                        Initiales _____

---

                                                                             APPEL D’OFFRES # 2026-6370-0005

Désigne tout événement réel ou soupçonné susceptible d’affecter la confidentialité, l’intégrité, la
disponibilité, l’authenticité ou la traçabilité de la Solution, des données, des Documents, des systèmes, des
accès ou des renseignements d’IQ.

Intégration

Désigne la connexion et l’échange de données, de Documents, de métadonnées, de statuts, d’événements
ou d’instructions entre la Solution et un système d’IQ ou d’un tiers autorisé, au moyen notamment d’une
API, d’un connecteur, d’un service Web, d’un protocole standard ou de tout autre mécanisme reconnu par
l’industrie.

Investissement Québec ou IQ

Désigne Investissement Québec, société d’État du Québec et donneur d’ouvrage dans le cadre du présent
Appel d’offres.

Jour ouvrable

Désigne un jour autre qu’un samedi, un dimanche ou un jour férié observé par IQ au Québec.
Sauf indication contraire, tout délai exprimé en Jours ouvrables est calculé en fonction des heures normales
d’affaires applicables à IQ.

Mise en production

Désigne le déploiement autorisé de la Solution ou d’une modification de celle-ci dans l’Environnement de
production, après la réalisation des activités de Configuration, de Paramétrage, d’Intégration, d’essai, de
formation et d’acceptation requises.

Aucune Mise en production ne peut être réalisée sans l’autorisation écrite préalable d’IQ. La Mise en
production ne constitue pas, à elle seule, l’Acceptation finale.

Modèle contractuel

Désigne un modèle de Document approuvé ou autorisé par IQ et utilisé par la Solution pour produire un
Document généré.

Un Modèle contractuel peut comprendre du texte fixe, des champs variables, des Clauses contractuelles,
des règles conditionnelles, des métadonnées, des instructions de présentation et des espaces réservés à
des données provenant d’un Système source.

Un Modèle contractuel utilisé pour produire un contrat d’affaires ne constitue pas un Document contractuel
régissant la relation entre IQ et l’Adjudicataire.

Paramétrage

Désigne l’établissement ou la modification des paramètres standards de la Solution afin de répondre aux
besoins d’IQ, sans modification du code source et sans Développement spécifique.

Période de stabilisation

Désigne la période de trente jours ouvrables suivant la dernière Mise en production comprise dans le
déploiement initial complet de la Solution, pendant laquelle l’Adjudicataire doit maintenir une mobilisation
accrue de son équipe afin de soutenir IQ et de corriger les défauts, anomalies, incidents et non-conformités
constatées.

Page 9                                                                                       Initiales _____

---

                                                                                 APPEL D’OFFRES # 2026-6370-0005

Piste d’audit

Désigne l’ensemble chronologique, intègre, protégé et consultable des événements, actions, décisions,
modifications, versions, accès, validations, approbations, transmissions, authentifications et signatures
associés à un Document, à un Document généré, à une Trousse ou à un Workflow.

La Piste d’audit doit permettre d’identifier, selon les droits d’accès applicables, les personnes ou les
systèmes concernés, les actions effectuées, les statuts, les versions ainsi que les dates et heures
correspondantes.

Le terme « Piste d’audit » comprend, selon le contexte, le journal d’audit, le certificat de signature, le dossier
de preuve ou tout autre élément permettant d’établir la traçabilité et l’intégrité du processus.

Preuve de concept ou POC

Désigne un exercice de validation pouvant être exigé par IQ afin de confirmer, dans un Environnement
contrôlé, que la Solution proposée satisfait effectivement aux exigences, aux capacités et aux engagements
déjà présentés dans la Soumission.

La Preuve de concept ne constitue pas une occasion pour le Soumissionnaire de modifier, de bonifier, de
corriger ou de compléter substantiellement sa Soumission après la Date limite de réception des
soumissions.

Renseignement confidentiel

Désigne tout document, renseignement, donnée ou information de nature commerciale, technique,
financière, juridique, opérationnelle, contractuelle ou autre concernant IQ, qui n’est pas accessible au public
et auquel le Soumissionnaire ou l’Adjudicataire a accès dans le cadre de l’Appel d’offres ou du Contrat.

Renseignement personnel

Désigne tout renseignement qui concerne une personne physique et qui permet, directement ou
indirectement, de l’identifier, conformément aux lois applicables en matière de protection des
renseignements personnels.

Services professionnels

Désigne les services requis pour la planification, la gestion du projet, l’analyse, la Configuration, le
Paramétrage, la création ou l’adaptation des Modèles contractuels, l’Intégration, les Développements
spécifiques autorisés, les essais, la documentation, la formation, le transfert de connaissances, le
déploiement, la Mise en production, la stabilisation et la Transition de la Solution.

Services récurrents

Désigne les services fournis de manière continue ou périodique pendant la durée du Contrat, incluant
notamment les droits d’utilisation, les licences ou abonnements, l’hébergement, le soutien, l’entretien, la
maintenance, les mises à jour et les autres services nécessaires à l’exploitation de la Solution.

Signataire externe

Désigne une personne qui n’est pas un Utilisateur interne d’IQ et qui est invitée à consulter, à remplir, à
approuver ou à signer un Document au moyen de la Solution.

Signataire interne

Désigne un employé, un dirigeant, un représentant ou un autre Utilisateur autorisé d’IQ appelé à consulter,
à remplir, à valider, à approuver ou à signer un Document dans la Solution.

Page 10                                                                                          Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

Signature électronique

Désigne un mécanisme technologique permettant à une personne de manifester son consentement, son
approbation ou son engagement relativement à un Document électronique, tout en permettant d’établir le
lien entre le signataire, le Document et le processus de signature, conformément aux lois et exigences
applicables.

Solution

Désigne l’ensemble de la plateforme technologique proposée par le Soumissionnaire pour répondre aux
Documents d’appel d’offres, incluant notamment les logiciels, les modules, les fonctionnalités de Génération
de contrat, les interfaces, les API, les connecteurs, les Configurations, les Environnements, les mécanismes
de sécurité, les outils d’administration, la documentation, les droits d’utilisation et les services nécessaires
à son implantation, à son exploitation, à sa maintenance et à son évolution.

La Solution doit notamment permettre, dans la portée expressément prévue aux Documents d’appel d’offres
:
    • la Génération de contrat à partir de Modèles contractuels et de données provenant de Systèmes
        sources;
    • la gestion des cycles de validation, d’approbation et de signature électronique;
    • la gestion des modèles, des signataires, des notifications et des rappels;
    • la gestion des Documents, des versions, des dossiers et des Trousse;
    • la conservation des Pistes d’audit et des preuves de signature;
    • l’Intégration avec les systèmes d’IQ;
    • l’administration des accès, des rôles et des règles d’affaires.

Soumission

Désigne l’ensemble des documents, renseignements, déclarations, engagements, prix, formulaires et
pièces justificatives déposés par un Soumissionnaire en réponse à l’Appel d’offres, incluant notamment le
Bordereau de prix, le Tableau de réponses et les annexes requises.

Soumissionnaire

Désigne toute personne morale, personne physique, société, entreprise, Consortium ou regroupement de
co-soumissionnaires qui dépose une Soumission dans le cadre du présent Appel d’offres.

Le Soumissionnaire peut être l’éditeur de la Solution, un intégrateur, un revendeur, un distributeur, un
partenaire certifié ou toute autre entité dûment autorisée à commercialiser et à offrir la Solution proposée.

Lorsque le Soumissionnaire n’est pas l’éditeur de la Solution, il doit détenir les autorisations nécessaires
pour commercialiser, implanter, intégrer, soutenir, maintenir et faire évoluer la Solution pendant toute la
durée du Contrat.

Sous-traitant

Désigne toute personne ou entité à laquelle le Soumissionnaire ou l’Adjudicataire confie l’exécution d’une
partie de ses obligations, incluant notamment un fournisseur de services infonuagiques, un hébergeur, un
exploitant d’infrastructure, un fournisseur de soutien, un intégrateur, un fournisseur de sécurité ou un
prestataire ayant accès aux données d’IQ.

Le recours à un Sous-traitant ne libère pas l’Adjudicataire de ses obligations ni de sa responsabilité envers
IQ.

Page 11                                                                                         Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

Sous-traitant ultérieur

Désigne toute personne ou entité à laquelle un Sous-traitant confie à son tour l’exécution d’une partie des
services ou le traitement de données associés au Contrat.

Système source

Désigne un système d’information d’IQ ou d’un tiers autorisé à partir duquel la Solution reçoit, consulte ou
récupère des données, des métadonnées, des Documents ou des instructions nécessaires à la Génération
de contrat ou à l’exécution d’un Workflow.

Aux fins du présent Cahier des besoins et exigences, Salesforce Grantmaking constitue un Système source
visé, sans exclure les autres systèmes d’IQ pouvant être intégrés à la Solution conformément aux
Documents contractuels. Le cahier prévoit précisément la récupération de données provenant de
Salesforce Grantmaking, la génération automatisée de contrats ou d’ententes et le retour des
renseignements associés au processus de signature.

Tableau de réponses

Désigne le document officiel fourni en annexe des Documents d’appel d’offres dans lequel le
Soumissionnaire doit expliquer de quelle manière la Solution proposée satisfait aux exigences d’IQ et fournir
les renseignements, explications, références, efforts, coûts et pièces justificatives demandés.

Toute conformité partielle, limitation, dépendance, condition, exclusion ou fonctionnalité non offerte doit être
clairement déclarée dans le Tableau de réponses.

Traitement

Relativement à un Renseignement personnel, désigne notamment sa collecte, son utilisation, son accès,
sa consultation, sa communication, sa conservation, son hébergement, sa modification, son transfert, son
extraction, sa sauvegarde ou sa destruction.

Transition

Désigne l’ensemble des activités nécessaires pour assurer, à la fin ou à la résiliation du Contrat, le transfert
ordonné des données, des Documents, des Documents générés, des Documents signés, des Modèles
contractuels appartenant à IQ, des Clauses contractuelles, des métadonnées, des preuves, des Pistes
d’audit, des Configurations, des accès, des services et des connaissances vers IQ ou vers un prestataire
de remplacement.

Trousse ou Enveloppe de signature

Désigne un regroupement logique d’un ou de plusieurs Documents et des renseignements connexes
transmis ou traités conjointement dans le cadre d’un même processus de validation, d’approbation ou de
signature.
La création d’une Trousse ou d’une Enveloppe de signature est distincte de la Génération de contrat.

Utilisateur

Désigne toute personne autorisée à accéder à la Solution, notamment un initiateur, un rédacteur, un
validateur, un approbateur, un Signataire interne, un Administrateur d’affaires, un Administrateur technique
ou une personne disposant d’un droit de consultation.

Volumétrie

Désigne les quantités estimatives ou réelles servant à décrire, à dimensionner, à tarifer ou à évaluer
l’utilisation de la Solution, notamment le nombre d’Utilisateurs, d’Administrateurs, de Documents, de

Page 12                                                                                         Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

Documents générés, d’envois, de Trousses, de signatures,                  de   transactions,   d’appels   API,
d’authentifications renforcées et la capacité de stockage.

Les quantités estimatives ne constituent pas un engagement minimal d’achat, d’utilisation ou de
consommation de la part d’IQ.

Workflow ou flux de travail

Désigne une séquence configurable d’étapes, de règles, de conditions, de rôles, de notifications et d’actions
permettant d’orchestrer un processus de Génération de contrat, de préparation, de validation,
d’approbation, de transmission, de signature, de conservation, d’exportation ou de consultation d’un ou de
plusieurs Documents.

Règles d’interprétation

Sauf indication contraire découlant du contexte :

    •   les termes définis au singulier comprennent le pluriel et inversement;
    •   l’emploi d’un genre comprend tous les genres;
    •   les termes « notamment », « incluant », « comprend » et les expressions équivalentes ne sont pas
        limitatifs;
    •   toute référence à une loi, à un règlement, à une norme, à une politique ou à un cadre de référence
        comprend ses modifications, ses règlements d’application et ses textes de remplacement
        applicables;
    •   les titres et sous-titres sont utilisés uniquement pour faciliter la consultation des Documents d’appel
        d’offres et ne limitent pas la portée des dispositions;
    •   une obligation imposée au Soumissionnaire qui se poursuit après l’adjudication devient une
        obligation de l’Adjudicataire;
    •   les expressions « doit », « devra », « est tenu de », « obligatoire », « minimal » et les expressions
        équivalentes indiquent une obligation, sauf indication contraire expresse;
    •   les termes « peut », « pourra » et « se réserve le droit » indiquent une faculté ou un pouvoir
        discrétionnaire d’IQ;
    •   toute exigence applicable à l’Adjudicataire s’applique également à ses employés, représentants,
        partenaires, Sous-traitants et Sous-traitants ultérieurs lorsque ceux-ci participent à son exécution;
    •   l’utilisation du terme « Document contractuel » est réservée aux documents qui constituent le
        Contrat entre IQ et l’Adjudicataire;
    •   l’utilisation des termes « Document généré », « Modèle contractuel » ou « Clause contractuelle »
        se rapporte aux documents d’affaires produits ou traités par la Solution;
    •   la mention de Salesforce Grantmaking ne limite pas le droit d’IQ d’intégrer la Solution à d’autres
        Systèmes sources prévus aux Documents contractuels;
    •   toute référence à une fonctionnalité standard désigne une fonctionnalité disponible dans la version
        commercialisée de la Solution à la Date limite de réception des soumissions;
    •   toute fonctionnalité nécessitant une Configuration, un Paramétrage, une Intégration, un outil tiers
        ou un Développement spécifique doit être identifiée comme telle dans le Tableau de réponses;
    •   en cas de contradiction entre une définition de la présente section et une définition prévue dans le
        document principal d’appel d’offres, ce dernier prévaut conformément à l’ordre de priorité des
        Documents contractuels;
    •   une définition particulière prévue dans une autre section s’applique uniquement à cette section,
        sous réserve de l’ordre de priorité des Documents contractuels;
    •   les exemples fournis sont présentés à titre illustratif et ne limitent pas la portée des exigences
        auxquelles ils se rapportent;
    •   une référence à un Document comprend, lorsque le contexte le permet, ses données, ses
        métadonnées, ses versions, ses preuves et sa Piste d’audit;
    •   aucune disposition du présent Cahier ne doit être interprétée comme permettant au
        Soumissionnaire ou à l’Adjudicataire de modifier la portée, les prix ou les obligations du Contrat
        sans l’autorisation écrite préalable d’IQ et, lorsque requis, sans un amendement dûment signé.

Page 13                                                                                        Initiales _____

---

                                                                                 APPEL D’OFFRES # 2026-6370-0005

1.3     SOLUTION ET SERVICES RECHERCHÉS

IQ souhaite moderniser et optimiser ses processus de validation, d'approbation et de signature électronique
afin d'améliorer l'efficacité opérationnelle, de réduire les délais de traitement, d'accroître la traçabilité et la
conformité des processus, ainsi que d'offrir une expérience simplifiée aux employés, aux clients et aux
partenaires d'affaires.

À cette fin, IQ souhaite se doter d'une solution permettant de gérer le cycle d'approbation et de validation
des documents ainsi que le cycle de signature électronique (incluant l'orchestration des signatures), dans
un contexte multi-juridictionnel. La solution devra être conforme, intégrée à l'écosystème technologique
d'IQ, modulaire, évolutive et en mesure de soutenir la croissance des activités d'affaires.

À noter : d’un point de vue architecture, le principe applicable ici serait le principe de couplage lâche, le «
service de signature » devant être un service appelable, distinct de celui de l’orchestration. 3 scénarios sont
possibles cependant : signature seule + orchestration dédiée, solution unifiée « signature & orchestration
», ou gestion du cycle de vie d’un contrat.

Ainsi, sans modifier le mandat principal lié à la mise en place d’une Solution de signature électronique et
de génération de contrats et d’orchestration des processus associés, IQ souhaite également évaluer
certaines capacités avancées liées au cycle de gestion des documents contractuels, lorsque celles-ci sont
offertes par la Solution proposée ou ses modules complémentaires. Ces capacités permettront de
différencier les solutions principalement axées sur la signature des solutions plus complètes, évolutives et
mieux alignées sur les besoins d’affaires d’IQ.

Le volume estimé de documents signés électroniquement pour les 1 300 employés d’IQ se situe entre
55 000 et 65 000 documents par an, indépendamment du nombre de pages de ces dits documents.

Les exigences d’architecture, d’affaires, fonctionnelles, technologiques et de sécurité, applicables aux
services requis de la Solution sont énoncées aux présentes et en annexes de l’appel d’offres.

1.4     EXIGENCES DE LA SOLUTION RECHERCHÉE

Les exigences d’architecture, d’affaires, fonctionnelles, technologiques et de sécurité et des critères de la
Solution recherchée sont énoncés ci-après et en annexes.

Le soumissionnaire doit indiquer à sa proposition si la Solution proposée rencontre les critères et exigences
requis et il doit fournir toutes les informations qui peuvent être demandées en relation avec la Solution.

Pour tous les exigences et critères des tableaux, le soumissionnaire doit préciser de quelle façon sa
proposition permet de rencontrer ou de surpasser les exigences et critères recherchés par IQ. Advenant
que le soumissionnaire ne rencontre pas une exigence ou un critère, ou la rencontre en partie seulement,
il devra le préciser.

Le soumissionnaire doit fournir la documentation appropriée en annexe pour expliquer de quelle façon la
Solution proposée rencontre ces exigences et critères.

1.4.1        GÉNÉRALITÉS

La Solution doit à la fois rencontrer les exigences prévues dans ce document d’appel d’offres et être déjà
disponible sur le marché.

Page 14                                                                                           Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

L’adjudicataire devra garantir que la Solution fonctionnera en accord avec les spécifications indiquées dans
sa soumission (qui comprend le document d’appel d’offres) ainsi qu’avec la documentation de la Solution.

1.4.2        HÉBERGEMENT DES DONNÉES

L’hébergement, le traitement et toute communication des données, ainsi que la relève/redondance de la
Solution, doivent être déployés au Québec ou être déployés dans d’autres provinces canadiennes, pays ou
États ayant une législation concernant la protection des renseignements personnels équivalente ou
supérieure à celle du Québec, cela afin de garantir que les données personnelles qui s’y trouvent
bénéficieront des mêmes protections qu’au Québec en respect de la Loi sur l’accès aux documents des
organismes publics et sur la protection des renseignements personnels (ci-après « la Loi sur l’accès ») et
des autres lois applicables, le tout sujet l’évaluation et à l’approbation préalable d’IQ.

Aux fins de précision, sont personnels les renseignements qui concernent une personne physique et
permettent directement ou indirectement de l’identifier (art. 54 de la Loi sur l’accès) par exemple les
données suivantes : nom, prénom, adresse de résidence, adresse courriel et numéro de téléphone
personnels.

Le soumissionnaire doit indiquer au Tableau de réponses en annexe de quelle manière sa Solution, ainsi
que tous les environnements (exemple : serveur dédié ou autre), rencontre ou excède les exigences de la
présente clause advenant le cas où les données seraient communiquées hors Québec au sens de la Loi
sur l’accès.

Le soumissionnaire doit être en mesure de fournir, sur demande, des preuves documentées confirmant les
lieux d’hébergement et de traitement des données.

L’adjudicataire s'engage à obtenir une acceptation formelle d’IQ advenant un changement des lieux
d’hébergement et de relève de la Solution.

1.4.3        EXIGENCES D’ARCHITECTURE

La Solution doit rencontrer les exigences d’architecture suivantes :

        •   la Solution doit être offerte en mode infonuagique et devra pouvoir se connecter avec des
            applications infonuagiques « Software as a Service » (SaaS);
        •   la Solution doit permettre la connexion aux applications "on premise" d’IQ via API; et
        •   la Solution devra assurer la protection et la confidentialité des données conformément aux lois
            en vigueur et à la politique de sécurité d'IQ.

Le soumissionnaire doit présenter un schéma d’architecture complet d’intégration de la Solution proposée
montrant ses interactions avec les différents systèmes externes auxquels elle doit se connecter (ex. : Outlook,
TEAMS, etc.). Il doit également préciser sa plate-forme technologique (version de système, bases de données
de la plate-forme, etc.) le tout tel qu’exigé au Tableau de réponses en annexe.

1.4.4        EXIGENCES D’INTÉGRATION

La Solution devra permettre l’échange de données avec les systèmes de référence de l’organisation,
notamment afin de préalimenter certains champs des documents et de retourner, lorsque requis, des
informations ou statuts de signature vers les systèmes métiers concernés.

Le soumissionnaire doit décrire, au Tableau de réponses en annexe, l’approche d’intégration proposée pour
la mise en place de la Solution, incluant les mécanismes envisagés, les prérequis, les efforts requis, les
responsabilités des parties ainsi que les dépendances applicables.

Page 15                                                                                        Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

1.4.5        GÉNÉRATION AUTOMATISÉE DE DOCUMENTS POUR SIGNATURE

Intégration avec Salesforce Grantmaking

IQ utilise Salesforce Grantmaking (Salesforce Public Sector Solutions) comme système maître pour la
gestion des dossiers d'aide financière, des demandes, des analyses, des approbations, des ententes et des
programmes financiers. La Solution proposée doit permettre une intégration sécurisée avec Salesforce
Grantmaking afin de soutenir les processus de validation, d'approbation et de signature électronique.

La Solution doit permettre :

    •   la récupération automatisée des données, métadonnées et renseignements requis provenant de
        Salesforce Grantmaking;
    •   la génération automatisée des contrats, ententes ou autres documents à partir des données
        conservées dans Salesforce Grantmaking;
    •   le pré remplissage des modèles documentaires approuvés par IQ;
    •   l'acheminement des documents générés dans les processus de validation, d'approbation et de
        signature électronique;
    •   le retour vers Salesforce Grantmaking des informations pertinentes liées au processus de signature
        lorsque requis;
    •   la conservation de la traçabilité entre le dossier source dans Salesforce Grantmaking, le document
        généré et les preuves de signature;
    •   la gestion sécurisée des erreurs d'intégration, des reprises de traitement et des transactions
        incomplètes;
    •   l'utilisation d'interfaces standard, documentées et supportées par l'éditeur de la plateforme.

Le soumissionnaire doit décrire :

    •   le mode d'intégration proposé;
    •   les interfaces et API utilisées;
    •   les prérequis techniques;
    •   les limites fonctionnelles;
    •   les mécanismes de sécurité applicables;
    •   les efforts de configuration et de mise en œuvre;
    •   les coûts initiaux et récurrents associés à cette intégration.

Le soumissionnaire doit également préciser son expérience de réalisation d'intégrations avec Salesforce,
Salesforce Public Sector Solutions ou Salesforce Grantmaking, lorsque applicable.

1.4.6        EXIGENCES FONCTIONNELLES

La Solution doit répondre aux besoins identifiés à l’appel d’offres et s’arrimer à l’architecture actuelle des
systèmes d'IQ. Elle doit donc répondre aux besoins de flexibilité et d’agilité identifiés par IQ, et ce, non
seulement au moment de son implantation, mais tout au long de son utilisation par IQ.

Le soumissionnaire doit répondre aux exigences figurant au Tableau de réponses en annexes quant aux
capacités fonctionnelles de sa Solution.

1.4.7        EXIGENCES FONCTIONNELLES DÉTAILLÉES

La Solution devra couvrir les fonctionnalités suivantes :

Validation et approbation des documents :

Page 16                                                                                       Initiales _____

---

                                                                            APPEL D’OFFRES # 2026-6370-0005

    •   gestion des circuits de validation et d'approbation (avant cycle de signature) en séquence, en
        parallèle ou mixtes.
    •   attribution des validateurs et approbateurs selon les rôles, groupes ou règles d'affaires.
    •   ajout, retrait ou remplacement d'un validateur ou approbateur en cours de processus.
    •   réassignation d'une étape de validation ou d'approbation sans redémarrage complet du processus.
    •   approbation, rejet ou retour pour correction d'un document.
    •   saisie de commentaires et conservation de l'historique des validations et approbations.
    •   notifications et rappels configurables pour les validateurs et approbateurs.
    •   suivi de l'état des validations et approbations en cours.

Gestion de la signature

    •   signature en séquence, parallèle et mixte
    •   gestion des groupes de signataires
    •   signature simultanée et consécutive configurable
    •   association automatique des étapes de signature aux signataires
    •   modification et ajout de signataires en cours de processus
    •   réassignation de signataires sans reprise complète du processus de signature

Gestion des modèles

    •   création de modèles réutilisables
    •   partage des modèles par groupe d’usagers
    •   ancrage précis des champs dans les documents

Champs et saisie

    •   types de champs requis : texte avec validation, liste de valeurs, date, case à cocher
    •   permettre de distinguer clairement les champs de formulaire, les champs de saisie et les champs
        de signature afin de réduire les erreurs de configuration et d’utilisation.
    •   attribution des champs à des signataires spécifiques
    •   règles conditionnelles d’affichage
    •   saisie assistée (suggestions automatiques)

Workflow et notifications

    •   notifications configurables (activation/désactivation)
    •   rappels automatiques en cas de non-réponse
    •   gestion des destinataires en copie

La Solution devra offrir une vue de suivi centralisée permettant de consulter l’état d’avancement des
dossiers, documents, trousses et signatures en cours, incluant les étapes complétées, les actions en attente
et les exceptions nécessitant une intervention.

Documents

    •   gestion de plusieurs documents par trousse
    •   gestion des dossiers, documents, versions et intervenants associés au processus de validation,
        d’approbation et de signature.
    •   support PDF et Word
    •   téléversement minimal de 25 mb
    •   conservation de l'historique des étapes de validation, d'approbation et de signature associées aux
        documents.
    •   journal d'audit permettant de retracer les actions effectuées sur un document ou une trousse
        (consultation, validation, approbation, signature, rejet, annulation, etc.).

Page 17                                                                                     Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

    •    consultation de l'état et de l'historique des transactions associées à un document.
    •    conservation et consultation du dossier de preuve des documents signés.

1.4.8        EXIGENCES D’EFFICACITÉ OPÉRATIONNELLE

    •    minimiser le nombre d’étapes nécessaires à la complétion d’un processus de signature
    •    permettre une exécution fluide et sans interruption (web et mobile)
    •    limiter les dépendances entre les signataires lorsque possible
    •    offrir une flexibilité élevée dans la gestion des workflows (modifications en cours de processus)
    •    minimiser le besoin d’interventions manuelles ou de contournements

La Solution devra offrir une expérience utilisateur simple, intuitive et cohérente pour les initiateurs, les
signataires internes et les signataires externes afin de compléter le processus de signature de façon
autonome. Le soumissionnaire devra préciser les possibilités de personnalisation de l’expérience de
signature, incluant l’image de marque, les messages présentés aux signataires et les parcours utilisateurs.

1.4.9        FONCTIONNALITÉS COMPLÉMENTAIRES (OPTIONNELLES)

Le soumissionnaire devra indiquer si la Solution propose des capacités d’automatisation ou d’intelligence
artificielle, notamment pour l’extraction de métadonnées, la classification de documents, l’analyse de
contenu, la détection d’anomalies ou l’aide à la préparation des documents. Le cas échéant, il devra préciser
les limites, les garanties de confidentialité, les mécanismes de contrôle humain et les conditions d’utilisation
de ces fonctionnalités.

1.4.10       EXIGENCES TECHNOLOGIQUES

Le soumissionnaire doit décrire les moyens d’identification et d’authentification des utilisateurs (single sign-
on « SSO »).

La Solution devra offrir des mécanismes d’authentification et, lorsque requis, de vérification d’identité
adaptés au profil du signataire, à la nature du document et au niveau de risque du processus de signature.

Le soumissionnaire devra préciser les méthodes disponibles pour les signataires internes et externes,
incluant le cas échéant les mécanismes d’authentification renforcée.

1.4.11       CONFORMITÉ ET RECONNAISSANCE DE LA SIGNATURE ÉLECTRONIQUE

La Solution proposée doit reposer sur des standards reconnus de l’industrie en matière de signature
électronique et assurer la validité juridique des signatures produites.

La Solution devra générer et conserver une piste d’audit complète pour chaque processus de signature,
incluant les événements de signature, les métadonnées pertinentes, les mécanismes d’authentification
utilisés, les horodatages, les consentements exprimés ainsi que toute information nécessaire pour
démontrer l’intégrité du document et la validité du processus de signature.

La Solution devra permettre la conservation, l’exportation et la portabilité des documents signés ainsi que
des preuves associées, dans un format exploitable et durable, afin de préserver la valeur probante des
documents pendant toute la période de conservation requise par IQ.

Le soumissionnaire doit démontrer que sa Solution est utilisée dans des contextes organisationnels
exigeants (ex. institutions financières, organismes publics ou équivalents) et qu’elle répond aux normes et
cadres de référence applicables, notamment en matière de sécurité de l’information (ex. ISO 27001, SOC
2 Type II ou équivalent) et de signature électronique reconnue.

Page 18                                                                                         Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

Le soumissionnaire doit préciser les certifications, normes et reconnaissances obtenues ainsi que les
juridictions dans lesquelles la signature électronique est réputée valide.
1.4.12       EXIGENCES DE SÉCURITÉ

Le soumissionnaire doit préciser au Tableau de réponses en annexe les modalités et conditions de son offre
notamment les prérequis logiciels de la Solution proposée (navigateur, agent, etc.), le matériel de
performance (CPU, RAM, etc.), les versions et cadence des mises à jour, ainsi que les nouveautés à venir.
De plus, il doit démontrer que la Solution proposée:

    •    ne met pas en péril l’intégrité ou la stabilité des installations et systèmes d’IQ;
    •    protège l’information tout au long de son cycle de vie, en assurant sa confidentialité, son intégrité,
         sa disponibilité et sa destruction au moment opportun, et ce, en appliquant les meilleures pratiques
         de l’industrie et en conformité avec les lois et règlements applicables, notamment la Loi sur l’accès
         aux documents des organismes publics, la Loi sur la protection des renseignements personnels et
         la Loi sur les archives;
    •    applique des contrôles internes qui fonctionnent efficacement et qui ont été conçus et mis en place
         en considérant les besoins de sécurité dès la conception de la Solution.

Le soumissionnaire doit confirmer qu’à titre d’adjudicataire, il :

    •    accepte qu’IQ puisse effectuer des contrôles de suivi et de surveillance du niveau de la sécurité de
         l’information en regard de la conformité aux obligations légales et réglementaires ainsi qu’aux
         cadres de référence reconnus internationalement;
    •    fournira à IQ, chaque année, un rapport d’un auditeur indépendant du niveau de conformité de la
         sécurité de l’information en regard d’un cadre de référence reconnu internationalement. Ce rapport
         devra couvrir au minimum neuf (9) mois de l’exercice financier d’IQ et doit être réalisé à la
         satisfaction d’IQ et être conforme au niveau de services et aux exigences de performance prévues
         à la documentation;
    •    fournira à IQ un rapport d’audit tel que décrit précédemment pour chacun de ses sous-traitants qui
         seront impliqués dans le(s) service(s) rendus à IQ le cas échéant;
    •    s’engage à remédier à toute non-conformité ou irrégularité observée par IQ ou un auditeur externe,
         et ce, dans un délai raisonnable à partir de la date de l’observation et le confirmera à IQ, par écrit,
         lorsque la non-conformité ou problématique aura été résolue;
    •    s’engage à acquitter pleinement ses obligations à l’égard de la sécurité de l’information d’IQ et
         diffusera ces obligations de sécurité à l’ensemble de son personnel. Par conséquent, l’adjudicataire
         sera responsable de tout dommage résultant d’un bris de ses obligations en matière de sécurité de
         l’information;
    •    communiquera sans délai toute violation ou tentative de violation de l’une ou l’autre des exigences
         relatives à la sécurité de l’information d’IQ au responsable de l’entente à IQ, lequel devra pouvoir
         effectuer toute vérification relative à cette situation;
    •    IQ évaluera le degré de maturité de la Solution proposée en regard de la sécurité de l’information
         en prenant référence sur le cadre de contrôle de la cybersécurité pour l’infonuagique de la Cloud
         Security Alliance (CSA), plus spécifiquement sur le questionnaire « CAIQ ». Le soumissionnaire
         doit indiquer au Tableau de réponses en annexe, s’il a déjà complété le questionnaire CAIQ, et
         dans la positive, le joindre à sa soumission. À défaut, il doit indiquer s’il pourra rendre
         les informations disponibles à IQ dans le cadre de l’évaluation de sa proposition sur demande de
         cette dernière. Le soumissionnaire peut également joindre à sa proposition la version allégée du
         formulaire CAIQ (CAIQ-Lite);
    •    lors du processus d’évaluation des offres, IQ pourra communiquer avec le soumissionnaire au sujet
         des mécanismes possibles d’atténuation du risque pour des éléments jugés non conformes au
         cadre de contrôle.

La Solution devra permettre une gestion des accès, des modèles, des workflows et des historiques en
fonction des rôles, des groupes d’utilisateurs, des unités administratives ou des domaines d’affaires, afin
d’assurer une séparation adéquate des responsabilités et des informations.

Page 19                                                                                         Initiales _____

---

                                                                             APPEL D’OFFRES # 2026-6370-0005

Le soumissionnaire doit répondre aux exigences et requis de protection des données et de sécurité
informatique    des     présentes,      des     conditions     générales     du     présent       document
et des exigences de sécurité de l’annexe « Exigences d’affaires, technologiques et de sécurité ».

Lorsque l’adjudicataire accédera aux systèmes d’IQ, dans les bureaux d’IQ ou à distance, il s’engage à
respecter les façons de faire et les mesures de sécurité d’IQ, telles qu’énoncées à la Politique de sécurité
d’IQ jointe en annexe. La conformité par l’adjudicataire à la politique est obligatoire et est sujette à des
contrôles de suivi et de surveillance par IQ.

1.5          NOMBRE D’ENVOIS

La Solution sera utilisée pour la grande majorité des documents d’Investissement Québec nécessitant une
signature électronique. À titre indicatif, le volume annuel est actuellement estimé entre 55 000 et 65 000
documents.

Le volume de documents traités pourra varier pendant la durée du contrat. La Solution doit donc permettre,
sans contrainte majeure, l’augmentation ou la diminution du nombre de documents transmis pour signature.

Bien qu’Investissement Québec ne prévoie pas de croissance importante des volumes à court terme, le
soumissionnaire doit préciser les modalités applicables en cas de variation du nombre de documents,
d’utilisateurs, de transactions ou de tout autre paramètre influençant les coûts de la Solution.

Le soumissionnaire doit indiquer dans le Bordereau de prix l’impact financier de ces variations de volume
et présenter, le cas échéant, la structure tarifaire applicable selon les différents seuils ou paliers de
consommation. Il doit également préciser les mécanismes d’ajustement des coûts, les unités de facturation
utilisées ainsi que toute condition susceptible d’avoir une incidence sur les prix soumis.

Les renseignements fournis dans le Bordereau de prix serviront de référence pour l’évaluation des
soumissions ainsi que pour l’ajustement éventuel des coûts durant l’exécution du contrat.

1.6          SOLUTION ÉPROUVÉE

Au moment où le soumissionnaire dépose sa proposition, la Solution proposée doit avoir été implantée,
avec des exigences similaires que celles décrites au présent document d’appel d’offres chez un minimum
de trois (3) organisations, gouvernementales ou privées. Chacune de ces implantations doit avoir été
complétée depuis trois (3) mois au minimum avant le dépôt de la présente soumission.

Pour chacune de ces réalisations, il doit fournir, le nom des entreprises où il a implanté la solution, la
description et l’envergure de chaque projet, les rôles et les responsabilités de son équipe ainsi que le nom
et les coordonnées des personnes-ressources qui pourront fournir de l’information complémentaire à IQ, à
titre de référence.

Le soumissionnaire doit aussi compléter le Tableau de réponses en annexe et documenter comment sa
proposition permet de réaliser le mandat et satisfaire les besoins d’IQ.

1.7          ENVIRONNEMENTS TECHNOLOGIQUE D’IQ

L’adjudicataire sera responsable d’assurer l’intégration, la compatibilité et le maintien de sa Solution avec
les systèmes en place chez IQ ainsi qu’avec les systèmes des partenaires interfaçant avec celle-ci. À cet
effet, l’adjudicataire devra fournir, maintenir et faire évoluer des mécanismes d’intégration standards et
performants tout au long du contrat et de ses renouvellements.

1.7.1        ENVIRONNEMENT TECHNOLOGIQUE ACTUEL

Page 20                                                                                      Initiales _____

---

                                                                            APPEL D’OFFRES # 2026-6370-0005

L’adjudicataire est responsable de l’intégration, de l’exploitation et du maintien de la compatibilité de la
Solution avec l’environnement technologique d’Investissement Québec ainsi qu’avec les systèmes des
partenaires et fournisseurs de services qui interagissent avec celle-ci.

La Solution doit être compatible avec les postes de travail, appareils mobiles, navigateurs Web, solutions
infonuagiques et systèmes d’entreprise utilisés par Investissement Québec pendant toute la durée du
contrat.

À titre indicatif, l’environnement technologique d’Investissement Québec comprend notamment :

      •   des postes de travail Windows Entreprise;
      •   des appareils mobiles et téléphones intelligents;
      •   les principaux navigateurs Web modernes, notamment Microsoft Edge, Google Chrome et Safari;
      •   des solutions infonuagiques et systèmes d’entreprise, incluant notamment Microsoft 365,
          Salesforce, Workday ainsi que divers systèmes métiers;
      •   des infrastructures d’hébergement infonuagique de type « cloud », notamment Microsoft Azure ou
          toute solution équivalente.

Le soumissionnaire doit démontrer que la Solution peut s’intégrer efficacement à cet environnement
technologique et fournir les mécanismes d’intégration requis, incluant notamment des interfaces de
programmation applicatives (API), connecteurs, services Web, protocoles standards ou toute autre méthode
d’intégration reconnue par l’industrie.

Le soumissionnaire doit également préciser dans sa soumission les intégrations standard offertes, les
prérequis techniques, les limitations connues ainsi que les responsabilités respectives des parties
relativement à la mise en œuvre, à l’entretien et à l’évolution de ces intégrations.

Investissement Québec se réserve le droit de faire évoluer son environnement technologique pendant la
durée du contrat. La Solution devra maintenir sa compatibilité avec les versions supportées des
plateformes, navigateurs, systèmes d’exploitation et services utilisés par Investissement Québec, sans
interruption de service ni dégradation significative des fonctionnalités.

1.8           ENVIRONNEMENTS

La Solution doit inclure obligatoirement, pour l’usage d’IQ, trois (3) environnements (développement,
préproduction, production). Tous les environnements doivent être hébergés au même endroit que
l’environnement de production.

L’environnement de préproduction sera identique, à quelques exceptions près, à celui de production et il
doit permettre à IQ de tester les changements avant de les appliquer en production. De plus, cet
environnement devra contenir un jeu de données significatif qui permettrait d’obtenir des résultats
concluants lors des tests.

L’adjudicataire devra rafraîchir, sur une base régulière, un sous-ensemble des données intègres à partir
des données de production d’IQ. Ces données doivent être anonymisées ou à défaut, l’adjudicataire doit
gérer l’environnement de préproduction avec les mêmes processus et contrôles de sécurité que ceux
utilisés pour l’environnement de production.

Les environnements applicables à la Solution, incluant les environnements obligatoires et ceux pouvant
être requis par IQ, sont les suivants :

      •   développement (DEV);
      •   intégration (INT), lorsque requis;
      •   préproduction / Acceptation (ACC);
      •   production (PRD);
      •   environnement de démonstration ou Sandbox à la demande d’Investissement Québec.

Page 21                                                                                     Initiales _____

---

                                                                           APPEL D’OFFRES # 2026-6370-0005

Ces environnements devront être complètement isolés les uns des autres. L’environnement de production
devra être distinct des environnements inférieurs et bénéficier de mesures de sécurité, de surveillance et
de contrôle conformes aux meilleures pratiques de l’industrie.

Tout coût associé à ces environnements doit être indiqué au Bordereau de prix.

Page 22                                                                                   Initiales _____

---

                                                                                APPEL D’OFFRES # 2026-6370-0005

2             SERVICES PROFESSIONNELS / RÉALISATION
2.1           SERVICES PROFESSIONNELS

IQ désire obtenir les services de l’adjudicataire pour l’assister lors des étapes de mise en place
technologique et de l’utilisation de la Solution, pour effectuer notamment les services suivants :

      •   mettre en place la solution à la satisfaction de IQ;
      •   accompagner et supporter les administrateurs de système d’IQ pour la configuration, le
          paramétrage et l’intégration de la solution avec l’hébergeur et les solutions tierces d’IQ;
      •   assurer le développement de modules dans la solution;
      •   effectuer la formation et le transfert de connaissances aux administrateurs et utilisateurs affaires
          et T.I. appelés à gérer et à utiliser la solution;
      •   fournir les services de maintenance et de mise à jour de la solution;
      •   fournir les services de support de la solution pour IQ;
      •   fournir la documentation requise;
      •   assurer l’évolution de la solution; et
      •   fournir et assurer tous les livrables et services accessoires qui y sont reliés.

2.2           ÉCHÉANCIER GLOBAL

Le soumissionnaire doit fournir, avec sa soumission, un échéancier détaillé présentant l’ensemble des
activités requises pour la réalisation du projet, depuis l’adjudication du contrat jusqu’à l’acceptation finale
de la Solution en production. Cet échéancier doit notamment identifier les principales phases du projet, les
biens livrables, les jalons, les durées prévues, les interdépendances entre les activités ainsi que les
responsabilités respectives du soumissionnaire et d’IQ.

L’échéancier proposé doit démontrer une compréhension complète de la portée du mandat et des
exigences énoncées aux documents d’appel d’offres. Il doit également préciser les activités situées sur le
chemin critique ainsi que les principaux risques susceptibles d’avoir une incidence sur la réalisation du
projet.

Lorsqu’une exigence nécessite des travaux de développement, de configuration, d’intégration ou de
personnalisation, le soumissionnaire doit préciser les efforts estimés, les délais requis, les dépendances
associées ainsi que les impacts potentiels sur l’échéancier global du projet.

Le soumissionnaire doit également identifier les ressources et les contributions attendues de la part d’IQ
aux différentes étapes du projet, notamment pour les ateliers d’analyse, la validation des configurations,
les essais, la formation, la mise en production et l’acceptation des biens livrables.

L’adjudicataire devra mobiliser l’ensemble des ressources, services, expertises et biens livrables
nécessaires afin de respecter l’échéancier convenu et assurer la réalisation du projet dans les délais
prévus. Il devra être en mesure d’amorcer les travaux dès l’adjudication du contrat.

À la suite de l’adjudication, l’échéancier soumis servira de base à l’élaboration du plan détaillé de réalisation
du projet. Celui-ci sera révisé conjointement par IQ et l’adjudicataire afin de confirmer les activités, les
jalons, les responsabilités, les dépendances et les dates de livraison des biens livrables. Toute modification
importante à l’échéancier devra faire l’objet d’une approbation préalable d’IQ.

2.3           GESTION DU PROJET

L’adjudicataire est imputable de la gestion, des résultats et des livrables de son équipe. Il s’engage à
respecter les échéanciers et doit s’assurer de fournir tous les efforts nécessaires.

Page 23                                                                                          Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

L’adjudicataire s’engage à :

      •   établir conjointement avec IQ, la planification du projet;
      •   confirmer la planification du projet conformément à ce qui a été approuvé par IQ;
      •   participer à la validation de l’architecture pour la mise en place de la Solution;
      •   planifier, organiser et tenir des ateliers de travail durant lesquels les membres de l’équipe IQ
          définiront et confirmeront les règles d’affaires de la Solution ainsi que les aspects relatifs à la
          paramétrisation et à la configuration de la Solution;
      •   configurer, implanter et maintenir à jour les environnements de la Solution selon les
          caractéristiques et la configuration qui seront définies lors d’ateliers de travail entre IQ et
          l’adjudicataire; et
      •   s’assurer de l’interopérabilité de la Solution entre les systèmes d’IQ.

À l’étape de la livraison de la Solution pour son acceptation par IQ, l’adjudicataire devra démontrer que la
Solution répond aux exigences de l’appel d’offres.

Dans l’éventualité où la Solution ne rencontre pas les exigences, l’adjudicataire devra apporter les correctifs
et les modifications nécessaires. À défaut par l’adjudicataire de procéder aux correctifs et modifications
nécessaires pour rencontrer les exigences dans les délais requis, IQ pourra, sous réserve de ses autres
recours, mettre fin au contrat.

2.4           ÉQUIPE PROJET

Le soumissionnaire doit présenter, dans le Tableau de réponses en annexe, la composition de l’équipe de
projet proposée pour la réalisation du mandat. Il doit notamment identifier le chargé de projet principal qui
sera responsable de la coordination globale du projet et de la gestion des ressources affectées à l’exécution
du contrat.

L’adjudicataire s’engage à collaborer étroitement avec IQ et ses partenaires afin d’assurer la réalisation
efficace des travaux, le respect des échéanciers ainsi que l’atteinte des objectifs du projet.

Le chargé de projet proposé doit demeurer disponible pendant toute la période de mise en œuvre de la
Solution et agir à titre de principal interlocuteur auprès d’IQ. Il sera notamment responsable de :

      •   coordonner l’ensemble des activités réalisées par l’adjudicataire dans le cadre du projet;
      •   agir comme point de contact principal auprès du comité de projet d’IQ;
      •   planifier, superviser et coordonner les ressources affectées au mandat;
      •   assurer la disponibilité, la continuité et la performance de son équipe;
      •   maintenir une communication continue avec le représentant ou le chef de projet désigné par IQ;
      •   assurer la qualité des biens livrables et leur conformité aux exigences contractuelles;
      •   effectuer le suivi du projet relativement au budget, à l’échéancier, aux risques, aux livrables et au
          contrôle de la qualité;
      •   produire les rapports d’avancement requis et informer IQ de tout enjeu, risque ou élément
          susceptible d’avoir une incidence sur la réalisation du projet;
      •   coordonner les activités d’implantation, d’intégration, d’essais, de formation, de mise en production
          et de stabilisation de la Solution.

Le soumissionnaire doit fournir le curriculum vitæ du chargé de projet proposé en mettant en évidence son
expérience dans la réalisation de projets comparables en termes de nature, de complexité, d’intégration
technologique et de portée organisationnelle.

Le soumissionnaire doit également présenter l’ensemble de son équipe de projet, en précisant les rôles,
responsabilités et expertises de chacune des ressources affectées au mandat. Il devra compléter les grilles

Page 24                                                                                        Initiales _____

---

                                                                                APPEL D’OFFRES # 2026-6370-0005

d’expérience prévues en annexe et fournir les curriculums vitæ des ressources clés proposées, en mettant
l’accent sur leur expérience d’implantation, d’intégration, d’exploitation et de soutien de solutions similaires.

Les ressources appelées à interagir avec IQ devraient être en mesure de communiquer efficacement en
français, tant à l’oral qu’à l’écrit. Le soumissionnaire devra indiquer toute ressource pour laquelle cette
exigence ne pourrait être respectée et préciser les mesures d’atténuation proposées.

Tout remplacement d’une ressource clé affectée au projet devra être soumis à l’approbation préalable écrite
d’IQ. La ressource de remplacement devra posséder des qualifications, une expérience et un niveau
d’expertise équivalents ou supérieurs à ceux de la ressource remplacée.

Tout transfert de connaissances requis en raison d’un changement de ressource sera effectué par
l’adjudicataire à ses frais et ne pourra entraîner aucun coût additionnel ni aucun retard pour IQ.

2.5           DOCUMENTATION

La Solution doit comprendre une documentation générale à portée technique et fonctionnelle et un manuel
d’utilisation pour les utilisateurs (ci-après nommée la « Documentation de la Solution »). La Documentation
de la Solution devra être remise au début du contrat.

Le soumissionnaire doit énumérer à l’annexe Tableau de réponses en annexe, la liste de tous les éléments
de Documentation générique de la Solution qu’il propose et qu’il fournira à IQ et qui est incluse dans le prix
de la Solution; incluant la documentation relative à la mise en place et à la configuration de la Solution, le
manuel d’utilisation de la Solution ainsi que toutes autres documentations relatives au transfert de
connaissances.

La Documentation de la Solution à portée technique (administration, configuration, intégration, etc.) devra
être en français de qualité, si disponible. La Documentation de la Solution destinée aux utilisateurs devra
être fournie en français de qualité pour toute la documentation de la Solution, conformément aux exigences
applicables de la Charte de la langue française, telle que modifiée par la Loi 96.

2.6           TESTS

IQ devra pouvoir effectuer des tests fonctionnels et d’intégration, de non-régression, de performance et
d’acceptation durant toute la période contractuelle, et ce, sans impacter ou mettre à risque l’environnement
et les systèmes de production.

IQ doit être en mesure d’effectuer ces tests :

      •   durant les différentes étapes de mise en place de la solution;
      •   lors de son acceptation globale;
      •   lorsque la solution est en opération :
                    o      à la suite d’un correctif;
                    o      lors d’un test de relève;
                    o      à la suite d’une mise à jour ou une évolution; et
                    o      lorsqu’une intégration est ajoutée ou modifiée.
      •   les tests d’acceptation doivent permettre à IQ de vérifier la performance, les changements de
          fonctionnalités, les mises à jour et les changements de processus avant de les appliquer en
          production, de même que les interopérabilités de chacun des modules de la Solution, le cas
          échéant, avec les composants d’IQ ou de ses partenaires.
      •   l’adjudicataire devra effectuer les tests requis à la Solution selon les intégrations requises ou les
          mises à jour, et ce, avant tout déploiement afin d’assurer que la Solution soit conforme aux
          exigences et besoins d’IQ.

Page 25                                                                                          Initiales _____

---

                                                                                APPEL D’OFFRES # 2026-6370-0005

      •   dans l’éventualité où la Solution et/ou sa mise en opération ne rencontre pas les exigences
          spécifiées aux présentes, l’adjudicataire devra apporter les correctifs et les modifications
          nécessaires. À défaut par l’adjudicataire de procéder aux correctifs et modifications nécessaires
          pour rencontrer les exigences dans les délais requis, IQ pourra, sous réserve de ses autres
          recours, mettre fin au contrat.

2.7           DÉMONSTRATION

À la suite de l’analyse des soumissions, IQ se réserve le droit d’inviter, à sa seule discrétion, un ou plusieurs
soumissionnaires à participer à une démonstration de la Solution par vidéoconférence. Cette démonstration
visera à permettre à IQ d’évaluer ou de valider les fonctionnalités, les capacités et les engagements
présentés dans la soumission. Tous les frais associés à la préparation et à la réalisation de cette
démonstration sont à la charge du soumissionnaire. Cette exigence sert uniquement à vérifier les
engagements contenus dans la soumission.

Les soumissionnaires retenus seront avisés par écrit de la date, de l’heure, de la durée, des scénarios à
exécuter ainsi que de l’ordre du jour de la démonstration. Les démonstrations devraient se tenir vers la fin
de la période d’évaluation des soumissions.

Chaque soumissionnaire invité devra démontrer, directement dans la Solution proposée, que celle-ci est
pleinement fonctionnelle et qu’elle répond aux exigences d’affaires, technologiques et de sécurité énoncée
aux documents d’appel d’offres. La démonstration devra notamment permettre d’évaluer les processus de
validation, d’approbation et de signature, la gestion des modèles, les notifications, les mécanismes de suivi,
les pistes d’audit, les fonctionnalités d’administration ainsi que les intégrations requises.

La démonstration devra être effectuée à partir de la version commercialisée de la Solution. Le
soumissionnaire pourra utiliser les fonctions de configuration et de paramétrisation déjà disponibles dans
cette version, mais il ne pourra en aucun cas présenter une fonctionnalité reposant sur du code, des
développements ou des modifications créés spécifiquement pour les besoins de la démonstration.

Lorsque la démonstration fait partie intégrante de l’évaluation qualitative, tous les soumissionnaires
concernés seront soumis aux mêmes scénarios, aux mêmes critères d’évaluation, à la même durée, aux
mêmes instructions et aux mêmes conditions. La démonstration servira uniquement à confirmer ou à
évaluer les éléments annoncés dans les documents d’appel d’offres et déclarés dans la soumission.
Aucune fonctionnalité, intégration, capacité ou amélioration qui n’a pas été identifiée dans la soumission
déposée avant la date limite de réception des soumissions ne pourra être prise en compte dans l’évaluation.

IQ se réserve le droit d’écarter toute fonctionnalité ou tout engagement présenté lors de la démonstration
qui ne figure pas dans la soumission ou qui modifie substantiellement le contenu de celle-ci. La
démonstration ne constitue pas une occasion de bonifier, corriger ou compléter une soumission après sa
date de dépôt.
2.8           PREUVE DE CONCEPT

À la suite de l’évaluation des soumissions et, le cas échéant, de la démonstration prévue aux documents
d’appel d’offres, Investissement Québec (IQ) se réserve le droit, à son entière discrétion, d’exiger qu’un ou
plusieurs soumissionnaires participent à une preuve de concept (POC) visant à confirmer que la Solution
proposée répond effectivement aux besoins, exigences et engagements contenus dans leur soumission.

La preuve de concept pourra être réalisée auprès du soumissionnaire ayant obtenu le meilleur rang
provisoire à l’issue de l’évaluation qualité-prix, ou auprès de tout autre soumissionnaire qu’IQ juge pertinent
de retenir dans le cadre de son analyse.

IQ communiquera par écrit aux soumissionnaires concernés les modalités applicables à la preuve de
concept, incluant notamment :

Page 26                                                                                          Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

      •   les objectifs de validation;
      •   les scénarios fonctionnels, technologiques et d’intégration à exécuter;
      •   les critères d’évaluation et d’acceptation;
      •   les résultats attendus;
      •   la durée prévue de l’exercice;
      •   les ressources devant être mises à la disposition d’IQ;
      •   les environnements requis;
      •   les délais de réalisation;
      •   les éléments de preuve et la documentation à fournir.

      La preuve de concept pourra notamment permettre à IQ de valider :

      •   les fonctionnalités de validation, d’approbation et de signature électronique;
      •   les mécanismes de gestion des workflows et des modèles;
      •   les capacités d’intégration avec Microsoft 365, Salesforce, Workday et les autres systèmes visés;
      •   les mécanismes de sécurité, d’authentification et de gestion des accès;
      •   les capacités d’administration de la Solution;
      •   les performances, la stabilité et l’expérience utilisateur;
      •   les capacités de production de rapports, de traçabilité et de conservation des preuves;
      •   toute autre exigence jugée critique par IQ.

Tous les frais liés à la préparation, à la réalisation et au soutien de la preuve de concept sont à la charge
du soumissionnaire. Aucun coût additionnel ne pourra être réclamé à IQ relativement à cet exercice.

La preuve de concept a pour seul objectif de confirmer les déclarations, engagements et capacités déjà
présentés dans la soumission déposée. Elle ne constitue pas une occasion de modifier, bonifier, corriger
ou compléter une soumission après la date limite de réception des soumissions. En conséquence, aucune
nouvelle fonctionnalité, intégration, capacité, option, service, amélioration ou condition commerciale qui
n’était pas explicitement incluse dans la soumission initiale ne pourra être prise en considération dans
l’évaluation ou l’adjudication.

Dans l’éventualité où la preuve de concept ne permettrait pas de démontrer de manière satisfaisante que
la Solution répond aux exigences des documents d’appel d’offres ou aux engagements contenus dans la
soumission, IQ pourra :

      •   demander des précisions additionnelles au soumissionnaire;
      •   conclure que la Solution ne satisfait pas aux exigences applicables;
      •   déclarer la soumission non conforme;
      •   écarter la soumission du processus;
      •   poursuivre son analyse auprès du soumissionnaire suivant au classement, sans obligation de le
          faire.

La participation à une preuve de concept constitue une condition essentielle du processus lorsque celle-ci
est exigée par IQ. Le refus d’y participer ou l’incapacité du soumissionnaire à fournir les ressources, accès
ou environnements requis dans les délais prescrits pourra entraîner le rejet de sa soumission.

3             LIVRAISON, DÉPLOIEMENT ET STABILISATION
3.1           PÉRIODE DE STABILISATION

À la suite de la mise en production de la Solution, l’adjudicataire devra mobiliser son équipe de support
pour une période de stabilisation suivant la mise en production complète pour trente (30) jours ouvrables.

Page 27                                                                                       Initiales _____

---

                                                                                 APPEL D’OFFRES # 2026-6370-0005

L’adjudicataire devra également rendre à IQ des services d’accompagnement à la phase de stabilisation,
suite à la mise en production de la Solution et fournir le support technique et fonctionnel. L’adjudicataire
devra collaborer avec IQ à corriger les défauts et/ou non-conformités à la liste d’exigences de sécurité,
d’affaires, et technologiques d’IQ après la mise en production.

Le soumissionnaire doit présenter au Tableau de réponses en annexe comment il entend assurer les
services d’accompagnement à la phase de stabilisation.

3.2           ÉVALUATION ET ACCEPTATION DES BIENS LIVRABLES

IQ a le droit de procéder en tout temps à l’évaluation des biens livrés, ainsi qu’au déroulement des travaux
exécutés et des services rendus, selon le cas, par l’adjudicataire et de lui fournir les commentaires
appropriés, et l’adjudicataire s’oblige à en tenir compte au cours de l’exécution du présent contrat. De plus,
sans préjudice à son droit de résilier ce contrat, IQ se réserve le droit de refuser en tout ou en partie les
biens livrés, les travaux ou les services, selon le cas, qu’il jugera insatisfaisants. Dans le cas d’exécution
de travaux, IQ pourra, à sa discrétion et sans préjudice à ses autres droits, faire reprendre les travaux par
l’adjudicataire, aux frais de ce dernier, jusqu’à entière satisfaction ou faire compléter lesdits travaux à sa
satisfaction par un tiers, aux frais de l’adjudicataire.

3.3           TRANSITION

À la date d’expiration de la période initiale ou d’une option de renouvellement du contrat ou à la date de
résiliation le cas échéant, l’adjudicataire devra assurer la transition des données de sa Solution vers un
autre fournisseur et/ou une autre solution.

La période de transition se terminera à la date de transfert complet des données et services vers un autre
fournisseur et/ou une autre solution.

L’adjudicataire s’engage à fournir à IQ sa collaboration afin d’aider à assurer des services de transition
efficaces et ordonnés vers les services et/ou produits d’un autre fournisseur, afin de minimiser les
interruptions du service et de conserver l’accès à la Solution sans coûts additionnels pour IQ.
L’adjudicataire devra conserver les données d’IQ et, à la demande d’IQ, assister cette dernière pour le
transfert, la conversion et la migration des données à un ou plusieurs systèmes désignés par IQ selon une
méthode et un format devant faire l’objet d’un consensus.

Durant la période de transition, les mêmes prix, termes et conditions alors en vigueur s’appliqueront, sous
réserve que les frais seront réduits au prorata, en fonction du retrait progressif des services et de l’utilisation
dégressive de la Solution, le cas échéant.

L’adjudicataire accepte de collaborer avec IQ et avec ses partenaires, à la rédaction d’un plan prévoyant
la fin du présent contrat et le transfert de la responsabilité des services rendus par l’adjudicataire en vertu
de la présente à un autre fournisseur ou à IQ. Ce plan de transition devra être finalisé au plus tard soixante
(60) jours avant l’échéance du terme précité ou de la date de résiliation et devra prévoir toutes les étapes
de cette transition incluant, sans limiter la généralité de ce qui précède :

      •   la période et le calendrier de transition;
      •   les rôles et responsabilités des parties;
      •   s’il gardera disponibles les données pour IQ, pour une période déterminée après la fin de
          l’utilisation de la Solution;
      •   le mécanisme de transfert et de migration des données au fournisseur de remplacement et/ou à
          IQ; et
      •   la remise des données et de tout autre bien appartenant à IQ, dans les formats acceptés par IQ.

Page 28                                                                                            Initiales _____

---

                                                                               APPEL D’OFFRES # 2026-6370-0005

Le soumissionnaire doit indiquer au Tableau de réponses en annexe les modalités du plan de transition
qu’il propose à IQ.

À la fin du contrat ou à la demande d’Investissement Québec, l’adjudicataire devra remettre :

    •   l’ensemble des documents;
    •   les journaux d’audit;
    •   les certificats et preuves de signature;
    •   les métadonnées;
    •   les modèles;
    •   les workflows;
    •   les paramétrages;
    •   les configurations.

Les données devront être remises dans un format ouvert, structuré et exploitable par une autre solution.

Le soumissionnaire doit préciser les outils, les formats, les délais et les coûts applicables à la transition de
sortie.

Page 29                                                                                         Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

4             FORMATION ET TRANSFERT
4.1           FORMATION

L’adjudicataire devra offrir une formation aux administrateurs et super utilisateurs d’IQ. Le soumissionnaire
doit proposer un coût forfaitaire au Tableau des coûts, pour assurer cette formation aux ressources IQ. Le
coût devra comprendre tous les frais incluant les efforts, ressources nécessaires et documentation requise
indépendamment du nombre d’heures requis.

Le soumissionnaire doit décrire son plan et sa stratégie de formation en fonction des besoins d’IQ au
Tableau de réponse en annexe. De plus, à titre informatif pour IQ, il doit indiquer le nombre d’heures et le
nombre de ressources qu’il estime nécessaires et qu’il propose afin de répondre aux besoins d’IQ.

      •   la formation devra être donnée en visioconférence, sur ses environnements.

      •   la formation devra être donnée en français, par un formateur compétent qui maîtrise la matière
          enseignée.

      •   l’adjudicataire doit fournir pour chaque employé en formation le matériel nécessaire, entre autres :

             a) une documentation électronique en français;
             b) la version électronique modifiable de la documentation, pour l’usage d’IQ afin que celle-ci
                puisse utiliser le contenu de la formation par la suite pour former de nouveaux employés.

4.2           TRANSFERT DE CONNAISSANCES

L’adjudicataire devra transférer aux administrateurs de l’équipe d’IQ les connaissances nécessaires pour
maîtriser et administrer la Solution. Ce transfert de connaissances devra s’effectuer tout au long de la mise
en place de la Solution, de la phase de stabilisation et de la période contractuelle, afin que les utilisateurs
d’IQ deviennent fonctionnels et autonomes avec la Solution.

Le soumissionnaire doit indiquer au Tableau de réponses en annexe comment il rencontre les exigences
de la présente clause.

Page 30                                                                                        Initiales _____

---

                                                                                APPEL D’OFFRES # 2026-6370-0005

5             EXPLOITATION DE LA SOLUTION
5.1           MAINTENANCE APPLICATIVE ET MISE À JOUR DE LA SOLUTION

Durant toute la durée du contrat et des options de renouvellement le cas échéant, l’adjudicataire sera
responsable de la fourniture des services de maintenance de la Solution. Ce dernier sera responsable
d’intégrer tous les correctifs, les mises à jour et les nouvelles versions de la Solution. Il devra permettre à
IQ d’accéder électroniquement à toute la documentation.

À la suite des mises à jour et correctifs effectués à la Solution, certains problèmes d’exploitation seront
présumés être causés par une défaillance d’un composant de la Solution. Dans tous les cas de défaillance
et de problèmes, il appartiendra à l’adjudicataire de démontrer que la défaillance et le problème ne
proviennent pas de la Solution.

Considérant les requis opérationnels d’IQ, les maintenances devront préférablement être effectuées en
dehors des heures d’utilisation de la Solution par IQ. IQ doit être avisée au préalable dans un délai
raisonnable de toute interruption de service ainsi que de la durée prévue pour les entretiens, mises à jour
ou autres. Le soumissionnaire doit proposer à IQ une infrastructure permettant de rencontrer ce requis.

L’adjudicataire devra notamment :

      •   assurer que la Solution demeure conforme aux exigences du présent appel d’offres, aux lois et aux
          règlements applicables;
      •   planifier, communiquer, réaliser, mettre en production et coordonner tout changement nécessaire
          pour assurer la mise à jour et la maintenance de la Solution; et
      •   permettre à IQ et ses représentants autorisés d’accéder électroniquement à toute la documentation
          (électronique ou papier) concernant la maintenance et les changements envisagés en amont.
      •   assurer une gestion continue des vulnérabilités, des correctifs de sécurité et des mises à jour
          nécessaires afin de maintenir la Solution sécuritaire et conforme aux exigences d’IQ.

L’adjudicataire devra aviser IQ et obtenir son approbation minimalement 90 jours ouvrables à l’avance des
mises en production ou mises à jour majeures de la Solution.

Advenant qu’il y ait eu un accès, une divulgation, une modification, une perte ou une destruction
accidentelle, non autorisée ou illicite des données d’IQ, l’adjudicataire devra notamment respecter les
exigences de la clause « Gestion des incidents de sécurité ».

Le soumissionnaire doit indiquer au Tableau de réponses en annexe comment sa proposition respecte les
exigences et engagements de la présente clause, décrire la fréquence et la nature des mises à jour des
logiciels de la Solution et comment IQ sera avisée des plages de maintenance et des incidents de sécurité.

5.2           SUPPORT ET ENTRETIEN

La Solution doit inclure un service de support technique par téléphone, par courrier électronique ou par
Internet afin d’assister IQ lorsque survient un problème dans la Solution. Ce service doit être fourni en
français, si disponible et il doit inclure le diagnostic et la correction des problèmes.

Les appels pourront être effectués par l’un des représentants autorisés d’IQ cinq (5) jours par semaine du
lundi au vendredi entre 8 h 00 et 17 h 00 (heure de l’Est) excluant les jours fériés. Les retours d’appels se
feront selon le niveau de priorité qui aura été attribué au problème rapporté. L’adjudicataire doit être le point
de contact unique pour IQ lorsque cette dernière requiert les services de support. La prise en charge par
l’adjudicataire de toute demande de support devra être confirmée au plus tard dans les deux (2) heures
suivant la communication de IQ.

Page 31                                                                                          Initiales _____

---

                                                                                  APPEL D’OFFRES # 2026-6370-0005

De plus, l’adjudicataire doit intégrer à la Solution tous les correctifs, les mises à jour et les nouvelles
versions du logiciel et permettre à IQ d’accéder électroniquement à toute la documentation. IQ doit être
avisée au préalable de toute interruption de service ainsi que de la durée prévue pour des raisons de mise
à jour, entretien ou autre.

Les problèmes seront présumés être causés par une défaillance d'un composant de la Solution. Ainsi il
appartiendra à l’adjudicataire de démontrer qu’un problème ne provient pas de la Solution.
L’adjudicataire aura la responsabilité d'offrir un support adapté à la stabilité de la Solution et à la satisfaction
d’IQ.

Le soumissionnaire doit compléter le tableau ci-dessous selon les niveaux de gravité et les délais de
réponse et de résolution offerts à IQ. Les délais de résolution seront calculés à compter de la
communication de la demande de support par IQ, jusqu’à la pleine résolution du problème selon sa gravité.

                                                                                          Délais de
                                                               Délais de réponse
                                                                                          résolution
Gravité Description                                            (En heures ou jours
                                                                                          (En heures ou
                                                               ouvrables)
                                                                                          jours ouvrables)
            Un « Problème de gravité 1 » désigne une
            panne majeure, une indisponibilité complète
            de la Solution ou un défaut empêchant IQ ou
1           ses utilisateurs d’utiliser la Solution, ou
            limitant de façon significative son utilisation
            pour des fonctions essentielles.

            Un « Problème de gravité 2 », un défaut ou
            problème, tel que IQ ou un utilisateur peut
2           toujours utiliser la Solution à l’exception de
            certaines fonctions qui ne sont pas
            fondamentales pour IQ ou l’utilisateur.
            Un « Problème de gravité 3 », un défaut ou
            un problème qui n’empêche pas le
            fonctionnement normal de la Solution ou qui
3
            a été résolu temporairement par un
            contournement raisonnable et mis en œuvre
            avec succès.

Le soumissionnaire doit décrire au Tableau de réponses en annexe, toutes les modalités de son service de
support technique dont notamment, les heures de service, le processus de requête de service relié aux
niveaux de gravité des problèmes et aux délais de réponse et de prise en charge des demandes de support
et de résolution indiquées ci-haut, ainsi que fournir la procédure d’escalade. Il doit fournir les noms et les
coordonnées des personnes-ressources à rejoindre en cas de besoin et selon l’ordre de priorité.

5.3           SLA MINIMAUX ET NIVEAUX DE SERVICE OBLIGATOIRES

Afin d’assurer la disponibilité, la stabilité et la continuité des services, le soumissionnaire doit offrir un
service de soutien technique permettant la prise en charge et la résolution des incidents selon les niveaux
de gravité décrits ci-dessous.

Les délais indiqués constituent les niveaux de service minimaux acceptés par Investissement Québec. Le
soumissionnaire peut proposer des niveaux de service plus avantageux, lesquels devront être clairement
identifiés dans sa soumission.

Un incident est réputé pris en charge lorsque le soumissionnaire a confirmé sa réception, assigné les
ressources nécessaires et amorcé les travaux de diagnostic ou de résolution.

Page 32                                                                                             Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

Le délai de résolution correspond à la période maximale entre la déclaration de l’incident et le
rétablissement du service ou la mise en place d’une solution corrective acceptable pour Investissement
Québec.

                                                                                      Délai cible de
          Niveau de                                             Délai maximal de
                              Description                                             rétablissement ou
          gravité                                               prise en charge
                                                                                      de résolution
                      Indisponibilité complète de la Solution
                      ou interruption majeure affectant une
          Critique                                                      1 heure               4 heures
                      fonction essentielle sans solution de
                      contournement acceptable.
                      Dégradation importante d’une
                      fonctionnalité essentielle ou impact
          Majeure     significatif sur les opérations, avec             2 heures              8 heures
                      solution de contournement limitée ou
                      partielle.
                  Dysfonctionnement affectant une
                  fonctionnalité non critique permettant
                                                                        1 jour                5 jours
          Moyenne toutefois la poursuite des activités par
                                                                        ouvrable              ouvrables
                  une solution de contournement
                  raisonnable.
                      Anomalie sans impact significatif sur
                      les opérations ou demande                         2 jours               10 jours
          Mineure
                      d’assistance technique, d’information             ouvrables             ouvrables
                      ou d’amélioration.

Le soumissionnaire doit décrire dans sa soumission :

      •   les heures de couverture du service de soutien;
      •   les mécanismes de déclaration des incidents;
      •   les procédures d’escalade;
      •   les niveaux de support offerts;
      •   les outils de suivi des billets;
      •   les engagements de disponibilité du personnel de soutien;
      •   les modalités applicables en dehors des heures normales de bureau.

Le non-respect répété des niveaux de service pourra entraîner l’application des mécanismes de suivi de
performance, des crédits de service ou de tout autre recours prévu aux documents contractuels.

5.4           TAUX DE DISPONIBILITÉ

IQ désire obtenir un haut niveau de fiabilité et vise un taux de disponibilité de la Solution minimal de 99,5%
et ce 24h/24h, sept (7) jours par semaine et 365 jours par an.

Le soumissionnaire doit indiquer au Tableau de réponses en annexe le taux de disponibilité offert par sa
Solution, la fréquence et la durée des plages de maintenance et décrire comment il entend fournir à IQ une
Solution avec un haut niveau de fiabilité.

5.5           RAPPORTS D’INTERVENTIONS

Page 33                                                                                       Initiales _____

---

                                                                             APPEL D’OFFRES # 2026-6370-0005

Sur demande d'IQ, l’adjudicataire doit remettre à cette dernière un rapport décrivant le détail des
interventions qu’il aura réalisées dans le cadre de l’exécution des services de support technique. Ce rapport
contiendra les informations suivantes : le numéro du billet d’intervention ouvert suivant l’appel de service,
les intervenants impliqués, les heures de début et de fin des interventions, la description des problèmes
rencontrés, de même que les solutions apportées.

5.6          ÉVOLUTION DE LA SOLUTION

L’adjudicataire s’engage à assurer l’évolution de la Solution et de ses composants technologiques et à les
supporter durant toute la période du contrat, et le cas échéant, durant les périodes de renouvellement.

Le soumissionnaire doit indiquer au Tableau de réponse en annexe les principales orientations, les
engagements et la stratégie d’évolution des fonctionnalités, d’évolution technologique et d’architecture
logicielle des outils et composants incluant la correction d’erreurs et des vulnérabilités de la Solution
proposée. Le soumissionnaire doit démontrer que la Solution proposée est en pleine évolution et non pas
en fin de vie.

Les composants et outils de la Solution proposée doivent demeurer compatibles et répondre aux exigences
d’IQ en fonction des mises à jour et nouvelles versions.

Sans limiter les engagements qui précèdent, advenant que l’une des versions des outils ou composants
de la Solution proposée ne soit plus supportée par le manufacturier, l’adjudicataire s’engage à aviser par
écrit IQ, au moins un an (1) à l’avance, que le manufacturier prévoit abandonner cette version et que celle-
ci ne sera éventuellement plus supportée. L’adjudicataire devra proposer un plan de transition et de
remplacement et prendre toutes les mesures nécessaires pour assurer la transition de la nouvelle Solution
sans aucuns frais pour IQ.

Page 34                                                                                       Initiales _____

---

                                                                              APPEL D’OFFRES # 2026-6370-0005

6            DURÉE, GARANTIE, COÛTS
6.1          DURÉE DU CONTRAT

Le contrat débutera à la date déterminée par Investissement Québec (« IQ ») lors de l’adjudication du
contrat.

La durée initiale du contrat relativement aux services d’hébergement, aux droits d’utilisation de la Solution,
aux services de soutien, d’entretien, de maintenance ainsi qu’à tous les autres services récurrents est de
trois (3) ans, à compter de la date d’acceptation finale de la Solution en production par IQ.

À l’issue de cette période initiale, IQ pourra, à son entière discrétion, exercer deux (2) options de
renouvellement de douze (12) mois chacune, pour une durée contractuelle maximale de cinq (5) ans.
Chaque option de renouvellement sera exercée aux mêmes termes et conditions que ceux prévus au
contrat initial, sous réserve des ajustements de prix expressément autorisés aux documents contractuels.

L’exercice d’une option de renouvellement relève exclusivement de la discrétion d’IQ et ne pourra prendre
effet qu’à la suite de la transmission d’un avis écrit de renouvellement adressé à l’adjudicataire dans le
délai prévu au contrat. L’absence d’un avis de non-renouvellement ne constitue en aucun temps un
renouvellement automatique du contrat ni un engagement de renouvellement de la part d’IQ.

Les services professionnels forfaitaires requis pour la mise en place de la Solution débuteront à la date
d’adjudication du contrat. Les services professionnels facturés selon des taux horaires, lorsqu’ils sont
requis et autorisés préalablement par écrit par IQ, pourront être demandés pendant toute la durée du
contrat, incluant la période initiale de trois (3) ans ainsi que les deux (2) options de renouvellement de
douze (12) mois chacune.

La durée totale maximale du contrat est de cinq (5) ans, soit une période initiale ferme de trois (3) ans
suivis de deux (2) options de renouvellement de douze (12) mois chacune pouvant être exercées à la seule
discrétion d’IQ.

6.2          TABLEAU DES PRIX

Les coûts pour les licences et services offerts par le soumissionnaire en réponse au présent appel d’offres,
doivent être expressément indiqués dans l’un des tableaux suivants, à défaut de quoi, les outils et services
seront réputés offerts sans frais supplémentaires pour IQ.

Il appartient également au soumissionnaire de divulguer et d’offrir tout autre service qui pourrait être
nécessaire afin que IQ puisse bénéficier des services requis au présent appel d’offres. Les tarifs de tels
services doivent également être indiqués expressément dans les tableaux de tarification, à défaut de quoi,
les services seront réputés offerts sans frais supplémentaires pour IQ.

Le soumissionnaire doit indiquer et expliquer la structure de coûts qu’il soumet pour la Solution et les
services proposés.

Le soumissionnaire devra préciser clairement son modèle de tarification, notamment les coûts par
utilisateur, par transaction, par enveloppe, par document, par API, par méthode d’authentification, par
intégration, par environnement et par service de support, le cas échéant. Tout plafond, seuil, frais
additionnel ou restriction d’usage devra être explicitement indiqué.

Le soumissionnaire doit également énumérer et indiquer les éléments de coûts supplémentaires
nécessaires dont IQ pourrait devoir se prévaloir en regard du nombre d’utilisateurs, de licences, de capacité
logicielle, etc.

Page 35                                                                                       Initiales _____

---

                                                                                   APPEL D’OFFRES # 2026-6370-0005

        6.2.1        COÛTS DES SERVICES RÉCURRENTS

   Le soumissionnaire doit indiquer l’ensemble des coûts associés aux services récurrents de la Solution. Les
   coûts récurrents comprennent notamment, sans s’y limiter, les frais d’hébergement, les droits d’utilisation,
   le support et l’entretien, la maintenance, les mises à jour ainsi que tout autre coût récurrent nécessaire à
   l’exploitation de la Solution.

   Les prix des services récurrents seront fermes et applicables uniquement à compter de la mise en
   production de la Solution, suivant l’acceptation finale d’Investissement Québec. Par la suite, ces prix
   pourront être majorés annuellement à la date anniversaire du contrat, conformément aux modalités
   contractuelles applicables.

   La ventilation détaillée de la structure de coûts et les prix soumis doivent être complétés dans le Bordereau
   de prix fourni avec les documents d’appel d’offres. Le Bordereau de prix constitue l’unique document de
   référence pour l’évaluation financière des soumissions.

   Le soumissionnaire doit y présenter l’ensemble des coûts récurrents applicables pour chaque année de la
   période contractuelle, incluant les périodes optionnelles, le cas échéant. Si la structure tarifaire proposée
   diffère des catégories prévues au Bordereau de prix, le soumissionnaire doit fournir les précisions requises
   afin de permettre à Investissement Québec de comprendre clairement la composition des coûts soumis.
   Le soumissionnaire peut également ajouter les détails nécessaires dans les sections prévues à cette fin du
   Bordereau de prix.

   À titre indicatif seulement, les coûts récurrents pourraient comprendre les catégories suivantes :

        •   hébergement;
        •   droits d’utilisation (licences ou abonnements);
        •   support et entretien;
        •   maintenance et mises à jour;
        •   autres frais récurrents liés à la solution (à préciser);
        •   escomptes ou rabais applicables (à détailler)..

6.2.2           AUTRES PRODUITS ET SERVICES

   Le soumissionnaire doit décrire tout produit, service complémentaire ou option pouvant être offert en lien
   avec la Solution proposée, le cas échéant.

   La description des produits et services ainsi que leur ventilation financière doivent être présentées dans le
   Bordereau de prix. Le soumissionnaire doit y préciser, pour chaque produit ou service proposé, la nature
   de l’élément offert, les quantités applicables, les coûts forfaitaires, les coûts récurrents, s’il y a lieu, ainsi
   que toute information pertinente permettant à Investissement Québec de comprendre la portée et la valeur
   du produit ou du service proposé.

   Tout autre élément de coût ou service additionnel non expressément prévu au Bordereau de prix doit
   également y être identifié et décrit.

6.2.3           FLUCTUATION DU NOMBRE D’UTILISATEURS, DE DOCUMENTS OU AUTRES

   Le soumissionnaire doit indiquer l’impact d’une variation du nombre d’utilisateurs, de documents, de
   transactions ou de tout autre élément susceptible d’influencer les coûts de la Solution.

   L’ensemble des informations relatives à la volumétrie, aux seuils applicables, aux mécanismes
   d’ajustement et à la grille tarifaire correspondante doit être présenté dans le Bordereau de prix.

   Page 36                                                                                           Initiales _____

---

                                                                                 APPEL D’OFFRES # 2026-6370-0005

   Cette grille tarifaire servira de référence pour l’ajustement des coûts advenant des écarts importants entre
   les volumes estimés par Investissement Québec et les volumes réels observés pendant l’exécution du
   contrat.

   Le soumissionnaire doit également préciser les modalités applicables en cas d’augmentation ou de
   diminution des volumes ainsi que les mécanismes de crédit, de remboursement ou d’ajustement tarifaire
   qui pourraient s’appliquer.

   Aux fins de l’évaluation financière, Investissement Québec utilisera notamment les hypothèses suivantes :

        •   1 300 utilisateurs signataire;
        •   20 administrateurs d’affaires;
        •   3 administrateurs techniques principaux;
        •   15 administrateurs techniques utilisateurs;
        •   55 000 à 65 000 documents par année;
        •   120 000 signatures;
        •   70 à 100 utilisateurs CLM;
        •   10 000 authentifications renforcées;
        •   500 000 appels API;
        •   1 To de stockage actif;
        •   3 environnements.

   Ces valeurs doivent être identiques aux hypothèses du bordereau.

6.2.4        COÛTS FORFAITAIRES POUR LA MISE EN PLACE DE LA SOLUTION

   Le soumissionnaire doit soumettre un prix forfaitaire pour la mise en place de la Solution incluant l’ensemble
   des coûts associés à la réalisation du projet, à l’exception des coûts de formation et des services récurrents
   qui font l’objet de sections distinctes.

   La ventilation détaillée des coûts de mise en œuvre doit être complétée dans le Bordereau de prix.

   Cette ventilation doit notamment permettre d’identifier les coûts reliés aux activités de démarrage,
   d’analyse, de configuration, d’intégration, de développement, d’essais, de migration, de documentation, de
   transfert de connaissances, de stabilisation ainsi que tout autre service professionnel requis à la réalisation
   du mandat.

   Les prix soumis doivent comprendre l’ensemble des efforts, ressources, outils, licences temporaires, frais
   administratifs et livrables nécessaires à la réalisation complète du projet conformément aux exigences du
   présent appel d’offres.

   Les coûts soumis doivent exclure les frais de déplacement, d’hébergement et de subsistance. Advenant
   qu’Investissement Québec requière exceptionnellement une présence sur site, les modalités prévues à la
   section « Frais quotidiens des ressources extérieures » s’appliqueront.

   Les montants indiqués dans le Bordereau de prix sont fournis à titre de ventilation et n’ont pas pour effet
   de modifier la nature forfaitaire du prix soumis par le soumissionnaire.

6.2.5        PRIX FORFAITAIRE POUR LA FORMATION

   Le soumissionnaire doit soumettre un prix forfaitaire couvrant l’ensemble des activités de formation
   requises pour assurer le transfert de connaissances et l’utilisation adéquate de la Solution par les
   ressources d’Investissement Québec.

   Page 37                                                                                        Initiales _____

---

                                                                                   APPEL D’OFFRES # 2026-6370-0005

      Les détails relatifs aux formations offertes ainsi que les coûts correspondants doivent être inscrits dans le
      Bordereau de prix.

      Le soumissionnaire doit notamment préciser les différents profils visés, le contenu des formations, le
      nombre approximatif d’heures, le nombre de formateurs requis, les modalités de prestation ainsi que les
      coûts applicables à toute formation additionnelle ou optionnelle.

      Les prix soumis doivent comprendre tous les frais reliés à la préparation, à l’animation, à la documentation
      et    au     matériel     pédagogique     nécessaire         à    la     réalisation     des      formations.

6.2.6         TAUX HORAIRES DES RESSOURCES (SUR DEMANDE D’IQ SEULEMENT)

      Le soumissionnaire doit fournir les taux horaires applicables aux ressources qui pourraient être requises
      par Investissement Québec pour des services additionnels non inclus dans la portée de base du contrat.

      Les taux horaires doivent être indiqués dans le Bordereau de prix.

      Le soumissionnaire doit y préciser les catégories de ressources offertes ainsi que les taux correspondants.
      Ces taux doivent exclure tous les frais de déplacement, d’hébergement et de subsistance.

      Les taux demeureront fermes pour la première année du contrat. Par la suite, toute demande d’ajustement
      sera assujettie aux modalités contractuelles prévues au présent appel d’offres.

6.2.7           FRAIS QUOTIDIENS DES RESSOURCES EXTÉRIEURES

      Lorsque des ressources dont le lieu de provenance est situé à plus de 150 kilomètres des bureaux
      d’Investissement Québec à Montréal doit se déplacer à la demande d’Investissement Québec, seuls les
      frais admissibles prévus aux présentes pourront être remboursés.

      Le cas échéant, le soumissionnaire doit indiquer dans le Bordereau de prix le montant quotidien proposé
      pour les frais des ressources extérieures.

      Ce montant ne pourra excéder la limite maximale prévue au présent appel d’offres. Tout déplacement devra
      avoir été préalablement autorisé par écrit par Investissement Québec.

6.2.8         COÛTS DE TRADUCTION (À LA DEMANDE D’IQ SEULEMENT)

 Si le soumissionnaire offre des services de traduction, il doit indiquer les coûts applicables à la traduction
 des documents ou du matériel fourni dans une langue autre que le français.

 Les coûts de traduction doivent être inscrits dans le Bordereau de prix.

 Le soumissionnaire doit préciser l’unité tarifaire utilisée, les langues visées ainsi que toute condition
 particulière applicable à ce service optionnel.

6.3           FERMETÉ DES PRIX

      Les prix soumis sont fermes pour une période de 120 jours à compter de la date d’ouverture des
      soumissions et le demeureront pour toute la durée du contrat advenant adjudication. L’adjudicataire
      s’engage à faire bénéficier IQ de toutes baisses de prix applicables aux biens et services pendant la durée
      du contrat et de ses options, le cas échéant, si ces dernières sont plus avantageuses.

      Page 38                                                                                       Initiales _____

---

                                                                                    APPEL D’OFFRES # 2026-6370-0005

6.4           GARANTIE DES SERVICES

      Sans limiter les autres représentations et garanties contenues au présent appel d’offres, l’adjudicataire
      garantira à IQ, en tout temps :

         a) Que les services de mise en place de la Solution seront réalisés et livrés en conformité avec : i)
         toute loi et tout règlement applicable; ii) les dispositions des présentes et de manière professionnelle;
         iii) les normes de l’industrie et les règles de l’art; et v) les règles reconnues et appliquées par le
         manufacturier et la documentation afférente;

         b) Que les services de mise en place de la Solution seront exempts de tout vice de conception et de
         fonctionnement, virus et de tout autre élément affectant ou étant susceptible d’affecter la qualité, la
         fiabilité, la performance, l’opération et l’évolution de la Solution, conformément aux besoins et exigences
         énoncés aux présentes;

         c) Qu’il livrera à IQ des services de mise en place de la Solution qui n’enfreignent aucun droit de tiers,
         incluant les droits de propriété intellectuelle;

         d) Qu’il n’utilisera pas, dans le cadre des services de mise en place de la Solution ou n’inclura pas
         dans la Solution, du contenu dont les droits de propriété intellectuelle, en tout ou en partie, sont
         entièrement ou partiellement la propriété ou sous le contrôle d’un tiers sans le consentement préalable
         du manufacturier et sans avoir préalablement obtenu de la part du tiers en question, au bénéfice d’IQ,
         une licence irrévocable, non exclusive et transférable afin d’utiliser et de permettre l’utilisation dudit
         contenu sans restriction territoriale;

         e) Qu’il a les compétences requises pour fournir les services de mise en place de la Solution,
         notamment en termes de qualité et d’expertise de ses ressources;

         f) Qu’il détient toutes les certifications requises incluant celles du manufacturier et que les services
         de mise en place de la Solution seront fournis conformément aux normes et pratiques généralement
         reconnues par le manufacturier;

         g) Qu’il remédiera à toute non-conformité des services de mise en place de la Solution fournis en
         vertu du présent appel d’offres; et

         h) Qu’il respectera toutes et chacune de ses déclarations, garanties et obligations aux termes des
         documents contractuels.

      Sous réserve de ses autres recours, IQ peut demander à l’adjudicataire de reprendre à ses frais tous
      services, travail ou biens qui ne seraient pas en tous points conformes aux spécifications et exigences
      énoncées au présent appel d’offres et au plan de projet convenu.

      Sans restreindre les autres droits et recours d’IQ prévus aux présentes et en vertu de la loi, l’adjudicataire
      s’engage à indemniser IQ à l’égard de tous dommages et pertes résultant d’une violation d’une garantie
      par l’adjudicataire et à prendre fait et cause pour IQ contre tous recours, réclamations, demandes,
      poursuites et autres procédures prises par toute personne relativement à l’objet de ces garanties.

      Page 39                                                                                       Initiales _____

---
