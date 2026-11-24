# ADR-026 — Défi anti-robot des formulaires par le protocole siteverify

- **Statut** : Accepté (fournisseur à retenir par l'organisation cliente)
- **Date** : 2026-11-22
- **Portée** :
  - `saisie.infrastructure.CaptchaSiteverify` (port `CaptchaVerifier`) et `CaptchaSimulation` (défaut) ;
  - `GET /api/saisie/captcha` (fournisseur et clé de site) ;
  - composant `DefiAntiRobot` du formulaire ;
  - variable `CSP_SOURCES_CAPTCHA` de la politique de contenu nginx.
- **Origine** : CHU 3.07, mécanisme anti-automatisation des formulaires.

## Contexte

- **Un port sans adaptateur réel** : le port `CaptchaVerifier` n'avait qu'une simulation, qui accepte tout jeton non
  vide. Le formulaire affichait une simple case « Je ne suis pas un robot ».
- **Une politique de contenu stricte** : nginx n'admet aucun script tiers (`script-src 'self'`). Or tout widget
  anti-robot se charge depuis l'origine de son fournisseur.
- **Une donnée de santé** : le fournisseur voit l'adresse IP et des signaux du navigateur du répondant. Il doit donc
  être retenu par l'organisation, comme tout sous-traitant.

## Options envisagées

1. **Google reCAPTCHA** : répandu, mais repose sur le profilage publicitaire du navigateur. Il est difficile à défendre
   au regard de la Loi 25 sur un formulaire de santé.
2. **Cloudflare Turnstile ou hCaptcha par le protocole siteverify** : même protocole de vérification côté serveur, défi
   le plus souvent invisible, sans profilage publicitaire.
3. **Défi maison** (calcul, question) : aucun sous-traitant. Mais il est vite contourné et souvent inaccessible
   (WCAG 2.2, 3.3.8 authentification accessible).

## Décision

Option 2, sans lier le code à un fournisseur.

- **Adaptateur unique** : `CaptchaSiteverify` envoie `secret`, `response` et `sitekey` en formulaire encodé et lit
  `success` dans la réponse JSON.
  - Il sert Turnstile comme hCaptcha : `CAPTCHA_FOURNISSEUR=TURNSTILE|HCAPTCHA`.
  - L'adresse par défaut est celle publiée par le fournisseur ; `CAPTCHA_URL` la remplace pour un bac à sable.
- **Sélection par configuration** : `CAPTCHA_MODE=siteverify`. La simulation reste le défaut, et le code métier ne
  change pas.
- **Fermé par défaut** : un refus du fournisseur, un statut autre que 200, une réponse illisible ou une panne valent
  un échec. Le jeton n'est jamais journalisé.
- **Secrets** : `CAPTCHA_SECRET` est lu dans l'environnement seulement. La clé de site est publique par nature ; le
  serveur la sert au navigateur avec le fournisseur, sans jamais le secret.
- **Démarrage** : une configuration incomplète (fournisseur inconnu, clé ou secret absent) fait échouer le démarrage.
  Un défi qui accepterait tout ne passe donc pas inaperçu.
- **Politique de contenu** : `CSP_SOURCES_CAPTCHA`, vide par défaut, ajoute l'origine du fournisseur à `script-src`,
  `connect-src` et `frame-src`.
  - Turnstile : `https://challenges.cloudflare.com`.
  - hCaptcha : `https://hcaptcha.com https://*.hcaptcha.com`.
  - Sans captcha réel, la politique reste celle d'origine.

## Conséquences

- **Positives** : le formulaire public résiste à l'automatisation, sans dépendance au code d'un fournisseur ; changer
  de fournisseur est une affaire de configuration.
- **Négatives** :
  - la plateforme dépend d'un service externe au moment de la soumission ; une panne du fournisseur bloque les
    soumissions qui exigent le défi, ce qui est le comportement voulu ;
  - le fournisseur s'ajoute au registre des sous-traitants (Loi 25).
- **Vérification** :
  - `CaptchaSiteverifyTest`, contre un faux fournisseur qui répond comme le vrai : protocole, refus, statut, réponse
    illisible, panne, interruption, configuration incomplète ;
  - `CaptchaIntegrationTest` : configuration servie, refusée sans le module « saisie » ;
  - le scénario BDD @CHU-3.07 (soumission refusée sans jeton) ;
  - en recette, les clés d'essai publiées par Turnstile (`1x00000000000000000000AA`) et hCaptcha
    (`10000000-ffff-ffff-ffff-000000000001`) valident le parcours complet.
