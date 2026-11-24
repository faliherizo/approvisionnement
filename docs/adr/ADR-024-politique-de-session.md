# ADR-024 — Politique de session par organisation : inactivité et durée maximale

- **Statut** : Accepté
- **Date** : 2026-11-22
- **Portée** :
  - `auth.domain.LimitesSession`, `PolitiqueSession` ;
  - `auth.application.PolitiqueSessionService` ;
  - `JwtAuthenticationFilter`, `CookiesSession` ;
  - `/api/admin/session` ;
  - migration 072 ;
  - écran « Authentification unique » du module CLM.
- **Origine** : ST-01 (IQ), « session sécurisée ; déconnexion ; expiration » ; exigence IQ-C-60.

## Contexte

- **Une seule règle** : la session durait 1 heure fixe à compter de la connexion, que l'utilisateur soit actif ou non.
- **Deux défauts** :
  - une session abandonnée restait ouverte une heure ;
  - une session active était coupée au bout d'une heure, en pleine rédaction.
- **Aucun réglage** : rien ne permettait à IQ d'adapter ces durées à sa politique.

## Options envisagées

1. **Durée fixe plus longue.** Aggrave l'exposition d'une session abandonnée.
2. **Jeton de rafraîchissement séparé** (OAuth). Un second secret à stocker, à faire tourner et à révoquer, pour une
   application dont le navigateur porte déjà un cookie httpOnly.
3. **Fenêtre glissante portée par le jeton de session (retenue).**
   - Le filtre vérifie l'inactivité et la durée maximale.
   - À chaque activité, il réémet le cookie, au plus une fois par minute.

## Décision

- **Limites** : un délai d'inactivité de 5 à 240 minutes et une durée maximale de 15 minutes à 24 heures, l'inactivité
  ne dépassant pas la durée maximale.
  - Bornes portées par le domaine (`LimitesSession`) : un réglage dangereux (session sans fin) ou inutilisable est
    refusé.
  - Défaut de la plateforme : 60 minutes et 8 heures (`APP_SESSION_*`).
- **Par organisation** : l'administration technique fixe sa politique (`ADMIN_TECHNIQUE`, ou l'administration
  principale par la hiérarchie de l'ADR-017). Le changement est journalisé et prend effet dans la minute (cache d'une
  minute).
- **Contrôle à chaque requête** :
  - la session est refusée si l'instant d'émission du jeton présenté remonte au-delà du délai d'inactivité ;
  - elle est refusée aussi si la connexion (revendication `debut`, conservée d'un renouvellement à l'autre) remonte
    au-delà de la durée maximale.
- **Renouvellement** :
  - il ne vise que le cookie de session, au plus une fois par minute ;
  - sa validité vaut le délai d'inactivité, sans jamais dépasser la durée maximale ;
  - un client par en-tête `Bearer` n'est pas renouvelé.
- **`JwtService` inchangé** pour les jetons de connexion : une surcharge additive émet les jetons renouvelés.
  L'analyse d'impact classait `JwtService` en risque CRITICAL par transitivité ; aucun comportement existant n'est
  modifié.

## Conséquences

- **Usage** : une session active ne coupe plus en pleine rédaction ; une session abandonnée se ferme au délai choisi par
  IQ.
- **Cas d'un jeton volé** : un jeton déjà émis reste utilisable jusqu'à la fin de son délai d'inactivité. La
  déconnexion le révoque (table `jeton_revoque`).
- **Vérification** :
  - `LimitesSessionTest` couvre les bornes, le verdict et le renouvellement plafonné ;
  - `PolitiqueSessionIntegrationTest` couvre l'inactivité, la durée maximale, le renouvellement, le cas Bearer et les
    rôles ;
  - le scénario BDD IQ-C-60 complète la vérification.
