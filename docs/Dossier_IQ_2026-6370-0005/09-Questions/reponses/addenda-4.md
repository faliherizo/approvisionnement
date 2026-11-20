# Addenda no 4 — Solution de signature électronique et de génération de contrats

Dossier 2026-6370-0005 · émis à Montréal le 19 novembre 2026 par Investissement Québec. Transcription du PDF `ADDENDA 4_ 2026-6370-0005_FINAL.pdf` ; les coupures de ligne et de mot du PDF ont été rétablies.

**Récapitulatif des modifications :** dispositions générales · questions et réponses · liste de vérification avant le dépôt de la soumission · formulaire de non-participation · questions en suspens ou omises.

## 1. Dispositions générales

Investissement Québec (« IQ ») apporte les précisions suivantes aux Documents d’appel d’offres relatifs au dossier 2026-6370-0005 - Solution de signature électronique et de génération de contrats. Le présent addenda fait partie intégrante des Documents d’appel d’offres. En cas de divergence, il prévaut uniquement à l’égard des éléments qu’il modifie ou précise. Les réponses ci-dessous complètent les réponses déjà publiées dans les Addendas nos 1, 2 et 3. Sauf indication contraire, toutes les autres clauses, exigences, modalités et échéances des Documents d’appel d’offres demeurent inchangées.

## 2. Questions et réponses

### 2.1. Signature et preuve

**Q1 - Quels niveaux de signature sont acceptés selon le type de document et le montant?**

**Réponse :** Le niveau d’authentification et de preuve requis doit être adapté au niveau de risque associé au processus et au document concerné. La Solution doit permettre à IQ de configurer différents niveaux d’authentification et de signature en fonction du type de document, du processus d’affaires et, lorsque pertinent, des règles d’autorisation applicables. Le mécanisme retenu pour chaque processus sera précisé lors de la conception. Le soumissionnaire doit décrire les niveaux de signature et d’authentification supportés par sa Solution.

**Q2 - Quelles méthodes d’authentification doivent être supportées pour les signataires internes et externes?**

**Réponse :** Pour les utilisateurs internes, la Solution doit notamment permettre une intégration avec Microsoft Entra ID et l’authentification unique conformément aux exigences des Documents d’appel d’offres. Pour les signataires externes, la Solution doit permettre des mécanismes appropriés selon le niveau de risque, notamment un lien sécurisé transmis par courriel, un code à usage unique, le SMS ou toute autre méthode d’authentification renforcée appropriée. Le soumissionnaire doit préciser les méthodes proposées, leurs prérequis, leurs limites et les coûts applicables. Cette réponse complète les précisions déjà fournies aux Q23 et Q42 de l’Addenda no 3.

**Q3 - Les signataires externes doivent-ils créer un compte?**

**Réponse :** Non obligatoirement. La Solution doit permettre à un signataire externe de procéder à une signature sans devoir créer un compte permanent lorsque le scénario d’affaires et le niveau de risque le permettent. Le processus doit néanmoins assurer une authentification, une traçabilité et une preuve suffisantes conformément aux exigences applicables.

**Q4 - Quels formats de certificat, de preuve et d’export doivent être supportés?**

**Réponse :** La Solution doit permettre de produire et de conserver le document signé ainsi que les éléments de preuve permettant d’en démontrer l’intégrité, l’authenticité et la traçabilité. Le soumissionnaire doit préciser les formats supportés, notamment les formats PDF, les mécanismes de validation à long terme disponibles et les formats d’export des preuves et journaux. IQ n’impose pas un format propriétaire particulier.

**Q5 - La signature doit-elle apparaître visuellement dans le document?**

**Réponse :** La Solution doit permettre, lorsque requis par le modèle documentaire ou le processus d’affaires, l’affichage d’une représentation visible de la signature à un emplacement configurable. Cette représentation ne remplace pas les mécanismes électroniques assurant l’intégrité et la preuve de la transaction.

**Q6 - Un mandataire ou représentant peut-il signer pour une entreprise?**

**Réponse :** Oui, lorsque le processus applicable permet la signature par un représentant dûment autorisé. IQ doit pouvoir exiger ou conserver les renseignements permettant de démontrer l’autorité du signataire, selon le contexte et le niveau de risque. La Solution doit permettre la traçabilité de l’identité et de la qualité du signataire.

**Q7 - Une signature manuscrite ou sur papier doit-elle rester possible?**

**Réponse :** La Solution recherchée vise prioritairement les processus électroniques. IQ peut toutefois conserver, à titre exceptionnel, des processus externes ou manuels lorsque la nature du document, une contrainte opérationnelle ou une situation particulière le justifie. Cette possibilité ne constitue pas une exigence fonctionnelle additionnelle de la Solution, sauf disposition contraire des Documents d’appel d’offres.

**Q8 - Qui signe pour IQ selon les montants?**

**Réponse :** Les pouvoirs de signature et d’approbation sont déterminés conformément aux règles de délégation et aux autorisations internes d’IQ en vigueur. La Solution doit permettre de configurer des parcours conditionnels selon des paramètres tels que le type de processus, l’unité administrative, le rôle ou le montant, sans qu’IQ soit tenu de communiquer dans le présent appel d’offres l’ensemble de ses règles internes de délégation.

### 2.2. Accès, rôles et provisionnement

**Q9 - IQ fournira-t-elle des comptes et environnements de test?**

**Réponse :** Les accès, comptes techniques et environnements requis seront déterminés avec l’adjudicataire pendant le démarrage et la conception. Les essais devront être réalisés dans des environnements approuvés par IQ et avec des données fictives, anonymisées ou autrement protégées conformément aux exigences applicables.

**Q10 - Faut-il distinguer les administrateurs d’affaires des administrateurs techniques?**

**Réponse :** Oui. La Solution doit permettre une séparation appropriée des responsabilités et des privilèges entre les fonctions d’administration d’affaires et les fonctions d’administration technique, conformément au principe du moindre privilège. Les rôles définitifs seront précisés pendant la conception.

**Q11 - Comment les comptes doivent-ils être créés et désactivés?**

**Réponse :** La Solution doit permettre le provisionnement et le déprovisionnement automatisés au moyen de SCIM 2.0 ou d’un mécanisme équivalent approuvé par IQ. Elle doit permettre la désactivation des accès lorsqu’un utilisateur quitte l’organisation ou perd les droits associés à son rôle. La configuration finale sera établie avec les mécanismes de gestion des identités d’IQ.

**Q12 - Des comptes locaux de secours sont-ils permis?**

**Réponse :** Tout compte local ou mécanisme d’accès de secours devra être justifié, limité aux besoins opérationnels, protégé par des contrôles appropriés et approuvé par les équipes responsables de la sécurité d’IQ. Aucun mécanisme de secours ne doit permettre de contourner les contrôles d’identité et d’accès applicables.

### 2.3. Données, sécurité et conservation

**Q13 - Les accès d’administration ou de soutien doivent-ils être réalisés exclusivement depuis le Canada?**

**Réponse :** Tout accès administratif, opérationnel ou de soutien susceptible de permettre l’accès aux données d’IQ doit être déclaré par le soumissionnaire, incluant le territoire à partir duquel cet accès peut être effectué. Tout accès ou traitement hors Canada demeure assujetti aux exigences des Documents d’appel d’offres et, lorsque requis, à l’autorisation préalable d’IQ.

**Q14 - Quel format d’archivage à long terme doit être supporté?**

**Réponse :** La Solution doit permettre l’exportation des documents, preuves, métadonnées et journaux dans des formats pérennes et exploitables permettant leur conservation et leur réutilisation par IQ. Le soumissionnaire doit indiquer les formats supportés, notamment PDF/PDF-A lorsque disponibles, ainsi que les mécanismes d’export massif et de conservation à long terme.

**Q15 - Quelles exigences s’appliquent aux sauvegardes?**

**Réponse :** Le soumissionnaire doit décrire son mécanisme de sauvegarde, sa fréquence, sa durée de conservation, la redondance, les mécanismes de restauration et les contrôles de sécurité associés. Ces mécanismes doivent permettre de respecter les objectifs RTO et RPO prévus aux Documents d’appel d’offres et précisés dans l’Addenda no 3.

**Q16 - Quelles certifications ou normes sont obligatoires?**

**Réponse :** Le soumissionnaire doit respecter l’ensemble des exigences de sécurité prévues aux Documents d’appel d’offres et déclarer les certifications, rapports d’audit et attestations applicables à la Solution et à son hébergement. Toute certification ou mesure équivalente proposée doit être suffisamment documentée pour permettre à IQ d’en apprécier l’équivalence.

### 2.4. Parcours, approbations et règles d’affaires

**Q17 - Les étapes parallèles doivent-elles permettre différentes règles de quorum?**

**Réponse :** Oui. La Solution doit permettre de configurer les parcours d’approbation selon les besoins, y compris des traitements séquentiels, parallèles, conditionnels ou mixtes. Lorsque pertinent, les règles de décision ou de quorum doivent pouvoir être configurées selon le processus.

**Q18 - Que se passe-t-il lors d’un retour pour correction ou d’un rejet?**

**Réponse :** Les règles doivent être configurables selon le processus. IQ doit notamment pouvoir retourner une trousse pour correction, reprendre certaines étapes de validation ou mettre fin au processus lors d’un rejet. Toute modification substantielle apportée après une approbation finale doit entraîner les validations requises avant qu’un document soit de nouveau soumis à la signature.

**Q19 - Quelles règles doivent s’appliquer aux relances, expirations et délégations?**

**Réponse :** La Solution doit permettre de configurer les délais, rappels, expirations, annulations et, lorsque le processus le permet, les délégations. Les paramètres définitifs seront établis pendant la conception en fonction des processus d’affaires.

**Q20 - Des règles de montant peuvent-elles déterminer les approbateurs?**

**Réponse :** Oui. La Solution doit permettre des parcours conditionnels fondés sur des données ou règles d’affaires, notamment un montant, un type de document, un programme, une unité administrative ou un rôle. Les règles internes détaillées de délégation d’IQ seront configurées selon les besoins et les autorisations applicables.

### 2.5. Modèles et documents

**Q21 - Combien de modèles seront déployés?**

**Réponse :** Le nombre définitif sera déterminé pendant la conception. Comme indiqué à l’Addenda no 1, le soumissionnaire doit préciser le nombre de modèles inclus dans son prix forfaitaire, l’effort associé à un modèle additionnel ainsi que ses hypothèses de complexité.

**Q22 - Les modèles Word existants doivent-ils pouvoir être repris?**

**Réponse :** La Solution doit permettre de soutenir l'utilisation de modèles documentaires provenant de l'environnement d'IQ et de préserver, dans la mesure permise par l'architecture proposée, la structure, la présentation et les éléments nécessaires à la génération documentaire. Le soumissionnaire doit identifier toute limitation, conversion ou intervention requise.

**Q23 - Quelle taille maximale de document doit être supportée?**

**Réponse :** Le soumissionnaire doit indiquer les limites maximales supportées par sa Solution pour les documents, enveloppes ou trousses et préciser toute incidence sur la performance ou la tarification. IQ privilégie une Solution permettant d'ajuster ces limites en fonction des besoins opérationnels dans les limites de la plateforme proposée.

### 2.6. Exploitation et niveaux de service

**Q24 - Quelle grille de niveaux de service prévaut en cas de divergence entre les classifications d’incidents?**

**Réponse :** Les classifications doivent être interprétées selon leur niveau d’impact et de criticité. Lorsqu’une différence terminologique existe entre les Documents d’appel d’offres, le soumissionnaire ne doit pas en déduire une réduction des niveaux de service exigés. La classification définitive et la correspondance entre les niveaux seront harmonisées avec l’adjudicataire avant la mise en service, sans réduire les engagements de service compris dans la soumission.

**Q25 - Quels crédits de service ou pénalités seront appliqués?**

**Réponse :** Les obligations contractuelles et les recours applicables sont ceux prévus aux Documents d’appel d’offres et au contrat. Le soumissionnaire ne doit pas présumer de l’application de crédits ou de mécanismes différents de ceux qui y sont prévus. Toute proposition de niveaux de service additionnels peut être précisée dans la soumission sans restreindre les droits d’IQ.

### 2.7. Tarification

**Q26 - Comment doivent être traités les volumes excédentaires et les coûts additionnels?**

**Réponse :** Le soumissionnaire doit identifier clairement dans sa soumission tous les coûts pouvant s’ajouter aux prix inscrits au Bordereau, notamment les volumes excédentaires, licences additionnelles, environnements supplémentaires et autres composantes facturables. Aucun coût requis pour satisfaire au périmètre obligatoire ne doit être omis du prix applicable.

**Q27 - Qui assume les coûts liés au fournisseur de signature et à l’horodatage?**

**Réponse :** Tous les coûts nécessaires à la fourniture de la Solution proposée et au respect des exigences obligatoires doivent être inclus et clairement identifiés dans la proposition et le Bordereau de prix selon les lignes applicables. Le soumissionnaire doit déclarer tout recours à un fournisseur tiers et les coûts associés.

**Q28 - Les sous-traitants doivent-ils être déclarés?**

**Réponse :** Oui. Le soumissionnaire doit déclarer les sous-traitants et fournisseurs tiers participant matériellement à la prestation, notamment en matière d’hébergement, de signature, d’horodatage, d’authentification ou de services connexes, conformément aux exigences contractuelles applicables. Le soumissionnaire demeure responsable de l’ensemble des obligations contractuelles.

### 2.8. Workday, Saviynt et Microsoft 365

**Q29 - Dans Workday, où le document doit-il être généré et retourné?**

**Réponse :** L’architecture proposée doit pouvoir soutenir les processus Workday prévus aux Documents d’appel d’offres. Selon le scénario retenu, le document pourra provenir d’un système source ou être généré par la Solution à partir des données reçues. Le soumissionnaire doit préciser son architecture, les échanges de données, le retour des statuts ainsi que le mécanisme permettant de retourner le document signé ou une référence durable lorsque requis.

**Q30 - IQ fournira-t-elle un environnement Workday de test?**

**Réponse :** Les environnements, accès et comptes techniques requis seront définis avec l’adjudicataire pendant le démarrage et la conception. Le soumissionnaire doit préciser ses prérequis et dépendances techniques dans sa soumission.

**Q31 - Quels attributs doivent être gérés par SCIM?**

**Réponse :** Le mappage définitif sera établi pendant la conception. La Solution doit permettre le provisionnement et le déprovisionnement des comptes ainsi que le mappage des attributs et groupes nécessaires à la gestion des rôles et accès. Elle doit permettre la désactivation rapide des comptes lorsqu’un utilisateur perd ses droits.

**Q32 - Quels usages de Microsoft 365 sont attendus?**

**Réponse :** La Solution doit pouvoir interagir avec l’environnement Microsoft 365 lorsqu’un processus d’affaires le requiert. Toute intégration proposée avec SharePoint, Teams, Outlook ou un autre service Microsoft 365 doit être décrite, incluant les prérequis, dépendances, limites et coûts. Les intégrations spécifiques seront confirmées pendant la conception selon les processus retenus.

**Q33 - Quelle bibliothèque SharePoint doit être utilisée?**

**Réponse :** Aucune bibliothèque SharePoint ni nomenclature définitive n’est imposée à cette étape. Si une intégration SharePoint est requise, le référentiel, la structure documentaire, les métadonnées, les règles de classement et les droits d’accès seront déterminés avec l’adjudicataire pendant la conception.

### 2.9. Signature en lot

**Q34 - Quelle taille de lot maximale doit être supportée?**

**Réponse :** Le soumissionnaire doit indiquer la capacité maximale de traitement en lot de sa Solution, les limites techniques applicables ainsi que toute incidence sur les délais, la performance ou la tarification. La Solution doit permettre un traitement adapté aux volumes prévus aux Documents d’appel d’offres.

**Q35 - Les documents d’un même lot doivent-ils être identiques?**

**Réponse :** Non. Un processus en lot doit pouvoir traiter, lorsque requis, plusieurs dossiers provenant d’un même processus ou modèle tout en générant des documents individualisés à partir des données propres à chaque dossier.

**Q36 - Un lot doit-il pouvoir être approuvé globalement avant son envoi en signature?**

**Réponse :** La Solution doit permettre des mécanismes d’approbation adaptés au traitement en lot tout en maintenant la traçabilité de chaque dossier, document et transaction. Le soumissionnaire doit préciser les mécanismes d’approbation globale ou individuelle supportés ainsi que leurs limitations.

## 3. Liste de vérification avant le dépôt de la soumission

Afin d’éviter qu’une soumission soit déclarée non admissible ou non conforme en raison d’un élément pouvant être vérifié ou corrigé avant son dépôt, IQ invite fortement chaque soumissionnaire à effectuer une vérification complète de sa soumission avant sa transmission. Cette liste constitue un aide-mémoire seulement. Elle ne remplace pas les exigences prévues aux Documents d’appel d’offres, lesquels demeurent la référence applicable.

### Admissibilité

Avant le dépôt, veuillez notamment vérifier que :

- Le soumissionnaire satisfait à toutes les conditions d’admissibilité prévues aux Documents d’appel d’offres.
- Toutes les déclarations, attestations et autorisations requises sont fournies et valides à la date exigée.
- Les renseignements relatifs au soumissionnaire sont complets, exacts et à jour.
- La personne qui signe la soumission est dûment autorisée à engager le soumissionnaire.
- Toutes les exigences relatives aux autorisations, inscriptions ou attestations gouvernementales applicables, le cas échéant, ont été vérifiées.
- Toute autre condition expressément identifiée comme une condition d’admissibilité dans les Documents d’appel d’offres est respectée.

### Conformité de la soumission

Avant la transmission finale, veuillez également confirmer que :

- Tous les formulaires obligatoires ont été remplis.
- Tous les documents devant être signés l’ont été par une personne autorisée.
- Toutes les annexes obligatoires sont jointes à la soumission.
- Le Bordereau de prix est entièrement rempli conformément aux instructions.
- Aucun champ obligatoire du Bordereau de prix n’a été laissé vide.
- Les prix, unités, quantités et totaux ont été vérifiés avant le dépôt.
- Les exigences obligatoires du Cahier des besoins ont toutes été considérées.
- Les réponses aux exigences techniques sont complètes et permettent à IQ d’en vérifier la conformité.
- Les renseignements demandés relativement aux sous-traitants et fournisseurs tiers, le cas échéant, sont fournis.
- Les exigences relatives à la sécurité, à l’hébergement, à l’intégration, à l’authentification et à la protection des renseignements ont été traitées dans la soumission.

- Les documents transmis correspondent aux versions finales des Documents d’appel d’offres, incluant les modifications apportées par les Addendas nos 1, 2, 3 et 4. Les Addendas nos 1 à 3 déjà publiés font partie du dossier.
- Toute autre exigence expressément identifiée comme une condition de conformité dans les Documents d’appel d’offres a été respectée.

### Vérification des addendas

Avant le dépôt, veuillez-vous assurer que :

- Tous les addendas publiés ont été lus et pris en considération.
- Les modifications ou précisions apportées par les addendas ont été intégrées à la soumission.
- Les réponses fournies aux questions des soumissionnaires ont été prises en compte.
- Aucune question précédemment transmise à IQ ne demeure, à votre connaissance, sans réponse.
- Toute question qui aurait été omise, laissée en suspens ou insuffisamment répondue a été signalée à IQ sans délai. Cette dernière vérification est particulièrement importante puisque des questions complémentaires avaient été transmises relativement au dossier et ont nécessité des réponses additionnelles.

## 4. Formulaire de non-participation

Tout acquéreur des Documents d’appel d’offres qui prévoit ne pas déposer de soumission est prié de transmettre à IQ, dès que possible, le formulaire de non-participation dûment rempli, en indiquant la ou les raisons de sa décision de ne pas soumissionner. Cette information permettra à IQ d’assurer un suivi approprié de la participation au processus d’appel d’offres.

## 5. Questions en suspens, omises ou non répondues

IQ demande à tous les soumissionnaires de vérifier attentivement les réponses fournies dans les Addendas nos 1, 2, 3 et 4. Si un soumissionnaire constate qu’une question qu’il a précédemment transmise :

- demeure en suspens;
- n’a pas obtenu de réponse;
- a été omise des addendas publiés; ou
- nécessite une clarification en raison d’une réponse qui ne traite pas entièrement la question initialement soumise.

Le soumissionnaire est prié d’en informer IQ sans délai, en retransmettant clairement la question concernée et, lorsque possible, la référence à sa transmission initiale. IQ procédera à la vérification et, lorsque requis, apportera les précisions nécessaires conformément au processus d’appel d’offres.

### Rappel important aux soumissionnaires

Les soumissionnaires sont invités à effectuer une dernière vérification de l’ensemble des questions qu’ils ont transmises à IQ afin de s’assurer qu’une réponse a été fournie. Toute question précédemment transmise qui serait toujours en suspens, aurait été omise ou n’aurait pas reçu de réponse doit être signalée à IQ sans délai. Par ailleurs, tout acquéreur des Documents d’appel d’offres qui prévoit ne pas déposer de soumission est prié de transmettre à IQ son formulaire de non-participation dûment rempli, en précisant la raison de sa non-participation.

### Dispositions finales

Le présent Addenda no 4 fait partie intégrante des Documents d’appel d’offres du dossier 2026-6370-0005. Les soumissionnaires doivent en tenir compte dans la préparation de leur soumission. Sauf modification expressément prévue au présent addenda, toutes les dispositions, exigences, modalités, conditions et échéances prévues aux Documents d’appel d’offres et aux Addendas nos 1, 2 et 3 demeurent inchangées et continuent de s’appliquer.

Émis à Montréal, le 19 novembre 2026 Par Investissement Québec _____________________________________________ ERNST CLERVEAU Conseiller principal, Approvisionnement et gestion de catégorie Direction principale, Approvisionnement et gestion des contrats
