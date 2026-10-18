# 🕸️ GitNexus — Graphe de connaissances du dépôt

Outillage **dev** pour explorer ce projet (backend **Java / Spring Boot** ·
frontend **React** · tests **Playwright**) sous forme de **graphe de connaissances**
interrogeable, avec un agent **Graph RAG**.

## Qu'est-ce que GitNexus ?

[**GitNexus**](https://github.com/abhigyanpatwari/GitNexus) construit un graphe où
les **nœuds** sont les entités de code (classes, méthodes, fonctions, interfaces) et
les **arêtes** les relations (appels, imports, héritage). Il fonctionne en local
(CLI) ou 100 % dans le navigateur (aucun envoi serveur) — adapté au code sensible,
dans l'esprit **Loi 25** du projet.

## Index de ce dépôt

Le projet est indexé (dossier `.gitnexus/`) :

- **1 062 nœuds · 2 625 arêtes · 53 clusters · 87 flux**
- Nom du dépôt dans le registre GitNexus : `contractuelle_contrat`

Ré-indexer après des changements :

```bash
gitnexus analyze . --skip-git          # le projet n'est pas (encore) un dépôt git
```

## Interroger le graphe (CLI)

Plusieurs dépôts étant indexés sur ce poste, préciser `--repo contractuelle_contrat` :

```bash
# Flux d'exécution liés à un concept
gitnexus query "authentification MFA TOTP login" --repo contractuelle_contrat

# Vue 360° d'un symbole (appelants / appelés)
gitnexus context "AuthService" --repo contractuelle_contrat

# Rayon d'impact (blast radius) avant un refactor
gitnexus impact "DocumentService.assembler" --repo contractuelle_contrat

# Chemin le plus court entre deux symboles
gitnexus trace "AuthController.login" "JwtService.genererToken" --repo contractuelle_contrat
```

## Explorer dans le navigateur (hors-ligne)

```bash
./scripts/gitnexus-export.sh          # → gitnexus-export.zip (node_modules/target exclus)
```
Puis ouvrir **https://gitnexus.vercel.app** et **glisser-déposer** le ZIP. Tout le
traitement reste dans le navigateur.

## Pourquoi sur ce projet ?

- **Cartographier l'authentification** : tracer `login → MFA (TOTP / question) →
  cookie httpOnly` et le port SSO Entra.
- **Blast radius** : mesurer l'impact avant de toucher au **moteur de règles** ou à
  l'**assemblage** (`RuleEngine`, `DocumentService`).
- **Revue d'architecture** : visualiser les couches DDD / hexagonale (api → service →
  domain → repo) et le port `AssistantRedactionPort`.

> Note : `gitnexus analyze` a aussi (ré)généré `AGENTS.md`, `CLAUDE.md` et les skills
> `.claude/skills/gitnexus/` à la racine.
