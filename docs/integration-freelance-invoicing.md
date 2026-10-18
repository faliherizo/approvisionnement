# Note d'intégration — `freelance-invoicing` en micro-frontend / microservice

**Question :** peut-on intégrer l'application `freelance-invoicing` dans la solution
de rédaction contractuelle, en tant que **micro-frontend** et **microservice** ?

**Réponse courte : oui, c'est réaliste et peu risqué.** Les deux applications
partagent le même socle frontend (React + Vite) et le même mécanisme
d'authentification (JWT). Le principal travail d'intégration porte sur (1) le
partage de session, (2) le routage via une passerelle, et (3) la cohabitation de
deux stacks backend (Java + Node).

---

## 1. Comparaison des deux systèmes

| Aspect | contractuelle_contrat (hôte) | freelance-invoicing (à intégrer) |
|--------|------------------------------|----------------------------------|
| Frontend | React 18 + Vite | React + Vite (TypeScript) |
| Backend | Java 17 / Spring Boot (:8080) | Node / Express (:4000) |
| Auth | JWT HS256 (jjwt), BCrypt | JWT (jsonwebtoken), bcryptjs |
| BD | PostgreSQL / H2 | PostgreSQL / SQLite |
| IA | Ollama (local) | Anthropic Claude (cloud) |
| PDF | 🔜 OpenPDF/POI | pdfkit |

➡️ **Forte compatibilité frontend** (React+Vite des deux côtés) et **auth JWT commune**.

---

## 2. Volet MICRO-FRONTEND

Deux SPA React+Vite → l'approche recommandée est **Module Federation** (plugin
`@originjs/vite-plugin-federation`), qui évite l'isolation lourde d'un iframe et
permet de partager `react`/`react-dom`.

```
┌──────────────────────────────────────────────┐
│  SHELL (hôte) — contractuelle_contrat React    │
│  Barre de nav unifiée · session partagée       │
│                                                │
│  Route /facturation ──► remote monté à chaud   │
│      (expose ./InvoicingApp)                   │
└───────────────┬────────────────────────────────┘
                │ import fédéré (runtime)
        ┌───────▼───────────────────┐
        │ REMOTE — freelance-invoicing│
        │ expose son App / ses routes │
        └─────────────────────────────┘
```

- **Hôte** : déclarer le remote et une route `/facturation` (lazy-load).
- **Remote (freelance-invoicing)** : exposer son `App` via `vite-plugin-federation`.
- **Partage** : `react`, `react-dom` en `shared` (singleton) pour éviter deux
  instances de React.
- **Alternative** si on veut zéro couplage build : envelopper le remote en **Web
  Component** ou, au minimum, l'intégrer par **iframe** (repli le plus simple mais
  ergonomie et partage d'état moindres).

---

## 3. Volet MICROSERVICE

Les deux backends restent **indépendants** (bounded contexts distincts : contrats
vs facturation/paie), derrière une **passerelle** qui unifie l'origine.

```
                 ┌────────────────────────┐
   Navigateur ──►│ Passerelle (nginx /     │
                 │ Spring Cloud Gateway)   │
                 └───┬─────────────┬────────┘
        /api/contrat │             │ /api/facturation
                 ┌───▼────┐   ┌────▼─────┐
                 │ Java    │   │ Node/    │
                 │ :8080   │   │ Express  │
                 │         │   │ :4000    │
                 └───┬─────┘   └────┬─────┘
                 ┌───▼────┐    ┌────▼─────┐
                 │ PG      │    │ PG       │  (bases séparées)
                 └─────────┘    └──────────┘
```

- Routage par préfixe de chemin : `/api/contrat/**` → Java, `/api/facturation/**` → Node.
- **Communication inter-services** : REST (ou événements/message bus si besoin
  d'asynchrone). Chaque service possède sa base — pas de partage de schéma.

---

## 4. Point central : l'authentification unique (SSO)

C'est le vrai travail d'intégration. Deux stratégies :

**A. Secret JWT partagé (rapide).** Aligner l'algorithme (HS256), le **secret** et
la **structure des claims** (`sub` = courriel, `roles`) entre les deux backends.
Un jeton émis par l'un devient accepté par l'autre. Le filtre JWT Java existant
([JwtAuthenticationFilter](../backend/src/main/java/qc/levis/contrat/security/JwtAuthenticationFilter.java))
lit déjà `sub` + `roles` ; côté Node, aligner le middleware `jsonwebtoken`.

**B. Fournisseur d'identité central (robuste).** Introduire **Keycloak** (ou OIDC)
comme émetteur unique ; les deux services valident les jetons via JWKS. Recommandé
pour la cible production (gestion fine des rôles, révocation, MFA).

➡️ **Recommandation :** démarrer en **A** pour un POC d'intégration, migrer vers
**B** (Keycloak/OIDC) pour la production.

---

## 5. Points de vigilance

| Sujet | Enjeu | Action |
|-------|-------|--------|
| **Loi 25 / résidence des données** | freelance-invoicing appelle l'API **Claude (cloud)** ; l'hôte municipal impose l'IA **locale (Ollama)** | Pour un usage Ville, router l'IA du module facturation vers Ollama, ou cantonner Claude aux données non sensibles |
| Deux langages (Java + Node) | Ops plus complexe | Conteneuriser chacun ; orchestrer via `docker-compose` / Kubernetes |
| Versions React | Doublon d'instances React en micro-frontend | `shared` singleton dans Module Federation |
| TypeScript vs JS | Frontends hétérogènes | Isole par remote ; pas bloquant |
| Modèles de domaine distincts | Contrats ≠ facturation/paie | Garder les bases séparées, intégrer par API |

---

## 6. Verdict & prochaines étapes

**Faisable.** Effort estimé pour un POC d'intégration : **2–3 sprints**.

1. Passerelle unique (nginx) devant les deux backends + SSO par secret JWT partagé.
2. Exposer freelance-invoicing en remote (Module Federation) et le monter sous
   `/facturation` dans le shell.
3. Router l'IA du module facturation vers Ollama pour la conformité Loi 25 (contexte municipal).
4. Cible production : Keycloak/OIDC + orchestration conteneurs.
