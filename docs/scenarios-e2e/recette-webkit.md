# Recette Safari et appareils mobiles (TI-11, RST-47)

> Document généré par `bdd/outils/recette-webkit.mjs` depuis le dernier résultat de `npm run e2e:webkit`
> (`bdd/test-results-webkit/resultats.json`). Ne pas modifier à la main, sauf le gabarit de recette manuelle, à
> recopier dans le procès-verbal de recette avant de le remplir.

## Essais automatisés sous WebKit (moteur de Safari)

Exécution : 2026-11-26 13:13 UTC. Parcours : besoins du cahier d'IQ (E2E-CB-IQ-*), écarts de l'annexe 10 (E2E-AX-IQ-*),
création d'une trousse à l'écran (E2E-ECR-IQ-01) et portail du signataire externe (E2E-ECR-IQ-03), chacun avec son
contrôle d'écran (axe-core WCAG 2.2 AA, cibles ≥ 24 px, sans débordement ni superposition aux quatre largeurs).
Chromium (Chrome, Edge) reste couvert par la campagne de référence (`npm run e2e`). Bilan : **99 réussis sur 99**.

| Appareil (projet Playwright) | Scénarios | Réussis | Échoués | Non exécutés |
|---|---:|---:|---:|---:|
| iPhone 13 (WebKit, 390 × 664, tactile) (`webkit-iphone`) | 33 | 33 | 0 | 0 |
| iPad 7e génération (WebKit, 810 × 1080, tactile) (`webkit-ipad`) | 33 | 33 | 0 | 0 |
| Safari de bureau (WebKit, 1280 × 720) (`webkit-bureau`) | 33 | 33 | 0 | 0 |

### Résultat par scénario

| Scénario | `webkit-iphone` | `webkit-ipad` | `webkit-bureau` |
|---|:---:|:---:|:---:|
| E2E-AX-IQ-01 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-02 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-03 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-04 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-05 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-06 | ✅ | ✅ | ✅ |
| E2E-AX-IQ-07 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-01 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-02 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-03 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-04 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-05 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-06 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-07 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-08 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-09 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-10 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-11 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-12 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-13 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-14 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-15 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-16 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-17 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-18 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-19 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-20 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-21 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-22 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-23 | ✅ | ✅ | ✅ |
| E2E-CB-IQ-24 | ✅ | ✅ | ✅ |
| E2E-ECR-IQ-01 | ✅ | ✅ | ✅ |
| E2E-ECR-IQ-03 | ✅ | ✅ | ✅ |

### Échecs

Aucun échec.

## Défauts révélés par la recette WebKit

| Défaut | Appareils | Correction |
|---|---|---|
| Listes déroulantes à 22 px de haut : Safari dessine la liste native à sa hauteur fixe et ignore marge intérieure et hauteur minimale, sous la cible de 24 px (WCAG 2.5.8) | iPhone, iPad, Safari de bureau | `frontend/src/styles.css` : listes sans apparence native (`appearance: none`), flèche redessinée ; même hauteur (44 px) que les autres champs |
| « Rétablir les paramètres de la plateforme » sans effet : le formulaire donnait le focus à son premier champ, et le toucher qui retire ce focus n'activait pas le bouton touché | iPhone, iPad | `AdminClm.jsx` (`FormulaireParametres`) : le focus va au formulaire ouvert (`tabIndex=-1`), plus à son premier champ |

Adaptations du harnais (sans effet sur l'application) : navigation repliée derrière « Menu » dépliée avant d'atteindre un
lien qu'elle cache (téléphone) ; requêtes interrompues par une navigation du harnais, que WebKit signale comme erreurs de
la page, écartées du relevé des erreurs JavaScript.

## Recette manuelle sur appareils réels — gabarit à remplir

Les essais automatisés ci-dessus emploient le moteur WebKit de Playwright avec l'émulation des appareils : ils ne
remplacent pas un essai sur l'appareil réel (clavier virtuel, sélecteur natif des listes, appareil photo, mémoire
des mots de passe, politiques de l'organisation). Chaque ligne est remplie par la personne qui exécute l'essai ; une
case vide signifie « non éprouvé ».

| # | Parcours (scénario automatisé de référence) | iPhone — Safari iOS | iPad — Safari iPadOS | Windows 10 — Edge | Windows 10 — Chrome | Windows 11 — Edge | Windows 11 — Chrome |
|---|---|---|---|---|---|---|---|
| 1 | Connexion, changement de langue (FR, EN), déconnexion | | | | | | |
| 2 | Modèle créé, configuré et publié (E2E-CB-IQ-01) | | | | | | |
| 3 | Trousse créée, document déposé, signataires ajoutés, parcours lancé (E2E-ECR-IQ-01) | | | | | | |
| 4 | Validation puis retour pour correction (E2E-CB-IQ-06) | | | | | | |
| 5 | Portail du signataire externe : identité, lecture, consentement, signature tracée au doigt ou à la souris (E2E-ECR-IQ-03) | | | | | | |
| 6 | Refus motivé au portail (E2E-ECR-IQ-03) | | | | | | |
| 7 | Suivi des trousses, relance, listes exportées (E2E-ECR-IQ-06) | | | | | | |
| 8 | Téléchargement du document signé et du dossier de preuve (E2E-CB-IQ-18) | | | | | | |
| 9 | Lecteur d'écran (VoiceOver sur iOS et iPadOS, Narrateur sur Windows) sur le portail du signataire | | | | | | |
| 10 | Zoom à 200 % et orientation portrait et paysage, sans perte de contenu | | | | | | |

Pour chaque appareil, consigner sous le tableau :

| Appareil | Modèle | Version du système | Version du navigateur | Date | Personne | Résultat global | Anomalies (référence) |
|---|---|---|---|---|---|---|---|
| iPhone | | | | | | | |
| iPad | | | | | | | |
| Windows 10 — Edge | | | | | | | |
| Windows 10 — Chrome | | | | | | | |
| Windows 11 — Edge | | | | | | | |
| Windows 11 — Chrome | | | | | | | |

Légende : ✅ conforme · ❌ anomalie (référence du billet) · ⏸ non éprouvé.
