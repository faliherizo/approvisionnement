# ADR-029 — Microsoft 365 pour le module de signature : dépôt SharePoint/OneDrive et avis Teams

- **Statut** : Accepté
- **Date** : 2026-11-25
- **Portée** :
  - port `clm.application.port.DepotMicrosoft365`, adaptateur `clm.infrastructure.DepotMicrosoft365Plateforme` ;
  - cas d'utilisation `clm.application.DepotsM365Service` (abonné de `DiffusionStatut` par `AbonneTransition`) ;
  - entités `DiffusionM365` (réglage) et `DepotM365` (dépôt avec reprises), changelog 076 ;
  - `integration.infrastructure.web.IntegrationM365Controller`, ouvert au module `clm`.
- **Origine** : Annexe 10 d'IQ, TI-01 (Obligatoire) ; story RST-45.

## Contexte

- **Un connecteur existant, réservé à un module** : la plateforme publie déjà vers SharePoint, OneDrive, Outlook et Teams
  (ADR-002 : port `PublicationCiblePort`, adaptateur Microsoft Graph prioritaire, simulation de repli). Mais le réglage de
  la connexion (`/api/parametres/integration-m365`) exigeait le module « rédaction », qu'IQ ne souscrit pas (ADR-010).
- **Un besoin de dépôt, pas de publication manuelle** : IQ veut retrouver le document signé et sa preuve dans sa
  bibliothèque, classés, sans geste, et être avisé dans Teams.
- **Des pannes à prévoir** : Microsoft Graph peut refuser ou être injoignable ; un dépôt perdu serait une preuve absente
  du référentiel d'IQ.

## Options envisagées

1. **Réutiliser le port `PublicationCiblePort` derrière un port du module** : le CLM déclare ce dont il a besoin
   (`DepotMicrosoft365`) ; son adaptateur délègue aux adaptateurs de publication existants, dans leur ordre de priorité.
2. **Appeler `PublicationService` du module de rédaction** : il exige un `Document` de rédaction, un rendu et un statut
   d'approbation qui n'existent pas pour une trousse.
3. **Écrire un second client Microsoft Graph dans le CLM** : duplication du jeton, du chemin de dépôt, de la gestion des
   canaux Teams et des secrets.

## Décision

Option 1.

- **Une seule connexion Microsoft 365 par organisation** : le contrôleur de réglage accepte désormais `redaction` *ou*
  `clm` (`@ModuleRequis`). La connexion reste simulée tant que l'organisation ne l'active pas : aucun appel externe par
  défaut, les dépôts simulés sont écrits dans le magasin du simulateur.
- **Déclenchement à l'achèvement** : `DiffusionStatut` devient une liste d'abonnés (`AbonneTransition`) ; Salesforce et
  les rappels d'intégration en sont, le dépôt Microsoft 365 s'y ajoute sans modifier les autres (OCP). Sur
  `SIGNATURE_TERMINEE`, si l'organisation a activé la diffusion, un `DepotM365` est inscrit par document signé, puis pour
  le dossier de preuve et l'avis Teams si demandés, **dans la transaction qui termine la trousse** (boîte d'envoi).
- **Classement** : un sous-dossier par trousse, nommé d'après sa référence, sous le dossier choisi ; la traversée de
  répertoire est refusée par le domaine.
- **Reprise** : le planificateur du module relaie les dépôts dus, avec recul exponentiel et abandon après 8 essais ;
  l'administration technique voit chaque dépôt et le relance. La clé d'idempotence (tenant, trousse, objet) empêche tout
  double dépôt.
- **Avis Teams par le même port** : l'avis est un court fichier texte (référence, titre, emplacements des documents)
  déposé dans le dossier du canal Teams configuré (cible `TEAMS`). Un message de canal *publié* exigerait soit des droits
  délégués, soit un webhook « Workflows » ; il s'ajoutera, si IQ le préfère, comme un nouvel adaptateur du port sans
  toucher au cas d'utilisation.
- **Données** : seul l'administration technique règle la diffusion (addenda 4, Q10). Les journaux applicatifs ne portent
  que l'identifiant du dépôt et le message d'erreur, jamais de contenu ni de secret ; chaque dépôt réussi est consigné au
  journal scellé de la trousse (`DEPOT_M365`).

## Conséquences

- **Positives** : TI-01 est couvert pour le module de signature sans nouveau client Graph ; le passage au réel est le même
  geste que pour le module de rédaction (configurer, activer). Un nouveau système externe à aviser s'ajoute comme abonné.
- **Négatives** :
  - le dépôt du dossier de preuve consigne un événement `PREUVE_EXPORTEE` par l'acteur `systeme` ;
  - l'avis Teams est un fichier du canal, pas une publication : à valider avec IQ ;
  - la recette réelle contre un locataire Microsoft 365 d'IQ (ou un bac à sable Graph) reste à faire.
- **Vérification** :
  - `DiffusionM365DomaineTest` : bornes du réglage, reprises et relance d'un dépôt ;
  - `DepotsM365ServiceTest` : quand déposer, quoi, où, ordre de l'avis, échecs ;
  - `DepotMicrosoft365PlateformeTest` : délégation aux adaptateurs de la plateforme, panne ;
  - `AnnexeDixIntegrationTest` : dépôt simulé de bout en bout, panne de Graph puis relance ;
  - scénario e2e `E2E-AX-IQ-01` (écran « Microsoft 365 », quatre largeurs).
