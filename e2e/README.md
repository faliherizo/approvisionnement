# Tests e2e — architecture (Page Object Model)

Suite de tests **Playwright** organisée selon le **Page Object Model (POM)** pour une
maintenabilité maximale (SOLID). Les tests décrivent des scénarios ; toute connaissance
du DOM (sélecteurs) est encapsulée dans les Page Objects ; les jeux de données sont
externalisés en JSON.

## Arborescence

```
e2e/
├── data/            Données de test en JSON (source unique de vérité)
│   ├── users.json         comptes de test (rédacteur, admin, superadmin, MFA…)
│   ├── documents.json     gabarits de documents
│   └── tenants.json       libellés de tenants / templates
├── models/          Modèles de données (typage) + chargeur de fixtures
│   ├── types.ts           interfaces TestUser, TestDocument…
│   └── fixtures.ts        expose les JSON typés (import unique)
├── pages/           Page Object Models (une classe par page)
│   ├── BasePage.ts        navigation & session communes
│   ├── LoginPage.ts, DashboardPage.ts, DocumentEditorPage.ts,
│   ├── UsersPage.ts, SuperadminPage.ts, ProfilePage.ts, ChatIaPage.ts,
│   ├── CataloguePages.ts  (Clauses, Search, Stats)
│   └── index.ts           barrel d'export
├── tests/           Tests e2e FONCTIONNELS (comportement)
│   ├── auth.spec.ts, document.spec.ts, features.spec.ts, tenant.spec.ts
└── views/           Tests de VUES (mise en page / responsive)
    └── responsive.view.spec.ts
```

## Principes (SOLID)

- **SRP** : chaque Page Object encapsule une seule page ; chaque spec décrit un scénario.
- **DIP** : les specs dépendent d'**abstractions** (Page Objects) et non de sélecteurs.
- **DRY** : sélecteurs et données définis une seule fois (Page Objects + fixtures JSON).
- **OCP** : nouveaux scénarios/données sans modifier les Page Objects existants.
- **Séparation des préoccupations** : tests fonctionnels (`tests/`) vs tests de vues (`views/`).

## Exécution

```bash
npm run install:browsers   # une fois
npm test                   # tout (démarre backend H2 + frontend automatiquement)
npm run test:e2e           # seulement les tests fonctionnels
npm run test:views         # seulement les tests de vues (responsive)
```

Le backend démarre sur H2 (base fraîche) et le frontend via Vite ; voir
`playwright.config.ts` (section `webServer`).
