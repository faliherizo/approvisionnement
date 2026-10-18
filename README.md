# Logiciel de rédaction contractuelle — Ville de Lévis

Solution clé en main de rédaction de clauses administratives et de contrats
(demandes de prix, appels d'offres publics, contrats de gré à gré, addendas).
Réponse à l'**Avis # 2026-50-92**.

## Contenu du dépôt

| Dossier | Description |
|---------|-------------|
| [docs/](docs/) | Architecture, organigramme, **BPMN 2.0**, roadmap, note d'intégration |
| [backend/](backend/) | API Java **Spring Boot** (auth JWT, RBAC, clauses, assemblage, approbation, addendas, rendu PDF/DOCX, IA Ollama) — schéma géré par **Liquibase** |
| [frontend/](frontend/) | SPA **React** (login/mot de passe, bibliothèque, éditeur de devis, addendas, export, IA) |
| [e2e/](e2e/) | Tests **Playwright** (bout en bout, sur H2) |
| [docker-compose.yml](docker-compose.yml) | Ollama (IA locale) + PostgreSQL + backend |

## Documentation

1. [Architecture de la solution](docs/architecture.md)
2. [Architecture technique — DDD / Clean Architecture / SOLID / OpenAPI](docs/architecture-technique.md)
3. [Sécurité & authentification — MFA, cookie httpOnly, SSO Azure](docs/securite-authentification.md)
4. [Organigramme organique + RACI](docs/organigramme.md)
5. [Diagramme BPMN 2.0](docs/bpmn.md) · fichier importable [`processus-redaction.bpmn`](docs/processus-redaction.bpmn)
6. [Roadmap de mise en œuvre](docs/roadmap.md)
7. [Analyse concurrentielle CLM + roadmap des écarts](docs/analyse-concurrentielle.md)
7b. [Analyse de conformité au cahier (EF/ENF) + roadmap](docs/analyse-conformite-roadmap.md)
8. [Note d'intégration freelance-invoicing (micro-frontend / microservice)](docs/integration-freelance-invoicing.md)
9. [Communications inter-applications sécurisées (VINA ↔ rédaction)](docs/comms-inter-apps.md)
10. [Conception — production de contrats formels + roadmap](docs/conception-contrats-formels.md)
11. [Champs requis (analyse & validation front/back)](docs/champs-requis.md)
12. [Déploiement Azure (SaaS) — pipeline CI/CD + IaC](docs/deploiement-azure.md)
13. [GitNexus — graphe de connaissances du dépôt](docs/GITNEXUS.md)

## Démarrage rapide (développement)

### Backend (Java 17, port 8080)
```bash
cd backend
mvn spring-boot:run
```
Base H2 en mémoire, données de démo chargées automatiquement.

### Frontend (React, port 5173)
```bash
cd frontend
npm install
npm run dev
```
Ouvrir http://localhost:5173

### Assistance IA (Ollama)
```bash
docker compose up -d ollama
docker exec -it vina-ia ollama pull llama3.1
```

### Pile complète en Docker (frontend Nginx + backend + PostgreSQL + Ollama)
```bash
docker compose up -d --build
# Frontend :  http://localhost:8081   (Nginx sert la SPA, proxifie /api → backend)
# Backend  :  http://localhost:8080   (profil prod → PostgreSQL, schéma Liquibase)
```

### Documentation d'API (OpenAPI 3.0)
- Swagger UI : http://localhost:8080/swagger-ui.html
- Spécification : http://localhost:8080/v3/api-docs

### SSO Azure Entra ID (profil `azure`)
```bash
export AZURE_TENANT_ID=... AZURE_CLIENT_ID=... AZURE_CLIENT_SECRET=...
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=azure
```
Voir [docs/securite-authentification.md](docs/securite-authentification.md).

### Production (PostgreSQL)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
# variables: DB_URL, DB_USER, DB_PASSWORD (défaut: postgres local)
```
Le schéma est créé/migré par **Liquibase** ([changelogs](backend/src/main/resources/db/changelog/)),
identiques en H2 (dév/test) et PostgreSQL (prod).

### Tests e2e (Playwright, sur H2)
```bash
cd e2e
npm install
npx playwright install chromium
npx playwright test          # démarre backend + frontend automatiquement
```

## Comptes de démonstration

> **Démonstration en ligne** — Application : <https://contrat.vinaitsolution.com/login> ·
> Vidéo guidée : <https://contrat.vinaitsolution.com/accueil#demonstration>

| Courriel | Rôles | Mot de passe |
|----------|-------|--------------|
| admin@levis.qc.ca | ADMIN | `Passw0rd!` |
| redacteur@levis.qc.ca | REDACTEUR, APPRO | `Passw0rd!` |
| juriste@levis.qc.ca | JURIDIQUE | `Passw0rd!` |
| direction@levis.qc.ca | DIRECTION | `Passw0rd!` |
| totp@levis.qc.ca | MFA **TOTP** (secret test `JBSWY3DPEHPK3PXP`) | `Passw0rd!` |
| question@levis.qc.ca | MFA **question** (réponse `montreal`) | `Passw0rd!` |
| superadmin@plateforme.ca | **SUPERADMIN** (global, gestion des tenants) | `SuperPassw0rd!` |

### Réponses MFA (comptes de démonstration)

Seuls **`totp@`** et **`question@`** exigent un second facteur. Tous les autres comptes
(`admin`, `redacteur`, `juriste`, `direction`, `superadmin`) se connectent avec **le mot de passe seul**
(aucune question, aucun code).

| Compte | Second facteur | Réponse / secret |
|--------|----------------|------------------|
| `totp@levis.qc.ca` | **TOTP** (RFC 6238) | Secret `JBSWY3DPEHPK3PXP`. Générer le code à 6 chiffres avec une app d'authentification (Google/Microsoft Authenticator…) ou en ligne de commande : `oathtool --totp -b JBSWY3DPEHPK3PXP` |
| `question@levis.qc.ca` | **Question secrète** | Réponse : **`montreal`** (insensible à la casse et aux espaces). La question affichée **change selon le jour**, mais la réponse configurée reste la même. |

> ⚠️ Si vous **activez le MFA** sur un compte via la page **Sécurité**, la réponse est **hachée
> (BCrypt) et non récupérable** : en cas d'oubli, désactivez/réinitialisez le MFA depuis la page
> Sécurité. En base (dépannage) : `DELETE FROM user_security_question WHERE user_id = …;` puis
> `UPDATE app_user SET mfa_active=false, mfa_methode='NONE' WHERE email='…';`.

## Fonctionnalités couvertes

- ✅ **Multi-tenant** : isolation totale des données par tenant (`TenantContext` + claim JWT)
- ✅ **Superadmin** : gestion des tenants (titre/login configurables) + création des admins de tenant
- ✅ **Gestion des utilisateurs** (admin) : liste, recherche avancée, pagination, édition, suppression, **RBAC** — le superadmin reste invisible pour les admins
- ✅ **Page profil** self-service (infos + changement de mot de passe)
- ✅ **Assistant IA chat** spécialisé + **base de connaissances (RAG)** alimentable par documents/données
- ✅ Authentification login/mot de passe (JWT, BCrypt) + **session cookie httpOnly**
- ✅ **MFA** : TOTP (RFC 6238) et **question secrète dynamique** ; **SSO Azure Entra ID** (OIDC)
- ✅ RBAC par rôle (`@PreAuthorize`)
- ✅ **Signature électronique** (cérémonie par jeton, signataires externes)
- ✅ **Recherche plein texte** + **tableau de bord analytique**
- ✅ **OpenAPI 3.0** (`/v3/api-docs`) + **Swagger UI** (`/swagger-ui.html`)
- ✅ Bibliothèque de clauses (standards, optionnelles, personnalisées) versionnées
- ✅ **Moteur d'adaptation** : clauses filtrées selon type, mode, valeur, contexte
- ✅ Assemblage automatique du devis
- ✅ Commentaires en ligne + circuit d'approbation hiérarchisé
- ✅ Assistance IA à la rédaction (Ollama, modèle local — conforme Loi 25)
- ✅ Journal d'audit
- ✅ **Addendas** interactifs + réintégration au document (cf. BPMN)
- ✅ **Rendu documentaire PDF (OpenPDF) et DOCX (Apache POI)**, addendas intégrés
- ✅ **Migrations Liquibase** — H2 (dév/test) et **PostgreSQL** (prod)
- ✅ **Tests e2e Playwright**

## Sécurité

Chiffrement en transit (TLS), hachage BCrypt, JWT signé, hébergement Canada,
IA locale (aucune donnée ne sort de l'infrastructure) — cf. Loi 25.
