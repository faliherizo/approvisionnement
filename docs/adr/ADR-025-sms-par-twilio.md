# ADR-025 — Canal SMS par Twilio

- **Statut** : Accepté (fournisseur à faire approuver par IQ comme sous-traitant)
- **Date** : 2026-11-22
- **Portée** :
  - `notification.infrastructure.SmsTwilio` (port `CanalNotification`) ;
  - `SmsSimulation` (défaut) ;
  - propriétés `app.notification.sms.*`.
- **Origine** : RST-17 (code de vérification du signataire par SMS) ; questions H1 (fournisseur de SMS) et L8
  (sous-traitants).

## Contexte

- **Un canal simulé** : le canal SMS n'existait qu'en simulation, faute de fournisseur.
- **Un compte existant** : Vina exploite déjà un compte Twilio pour ses autres applications (`urgences-sante`), avec
  les variables `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` et `TWILIO_FROM`.

## Décision

- **Adaptateur Twilio** du port `CanalNotification`, choisi par configuration (`NOTIFICATION_SMS_MODE=twilio`) ; la
  simulation reste le défaut.
  - Il appelle l'API REST publiée (`POST /2010-04-01/Accounts/{sid}/Messages.json`, formulaire, Basic), en passant par
    la garde anti-SSRF.
- **Secrets** : les identifiants sont lus dans l'environnement seulement, jamais écrits dans le dépôt ni journalisés.
- **Journalisation** : un refus de Twilio rend « ECHEC » avec son code (ex. 21211), que la messagerie consigne et
  relance. Le numéro n'est journalisé que par ses deux derniers chiffres (Loi 25).
- **Démarrage** : en mode `twilio`, l'absence d'un des trois réglages empêche le démarrage.

## Conséquences

- **Pour IQ** : Twilio traite les messages hors du Québec. C'est un **sous-traitant à déclarer** (réponse à L8) et un
  transfert à évaluer (Loi 25, EFVP), d'autant qu'IQ a demandé un fournisseur canadien (H1).
  - Si IQ le refuse, un autre fournisseur s'ajoute par un adaptateur du même port, sans toucher au métier.
- **Vérification** : `SmsTwilioTest` exerce l'adaptateur contre un faux Twilio qui répond comme le vrai. Il vérifie le
  chemin, l'authentification et le formulaire, ainsi que les refus 400 et le fournisseur injoignable. Le choix du
  canal par configuration est éprouvé aussi.
- **À faire** : un envoi réel demande un numéro destinataire de recette, désigné par Vina.
