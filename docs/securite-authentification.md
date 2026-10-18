# Sécurité & authentification

**Avis # 2026-50-92 — Ville de Lévis**

## 1. Facteurs d'authentification

### Mot de passe (1er facteur)
- Hachage **BCrypt**, vérification à débit constant.
- Jeton **JWT** (HS256) émis à la connexion.

### Session par cookie httpOnly (connexion « cookie-only »)
- Le jeton est déposé dans un cookie **`ACCESS_TOKEN`** : `HttpOnly`, `SameSite`
  (défaut `Lax`), `Secure` en production (HTTPS), `Path=/`.
- **Non accessible au JavaScript** → réduit le risque de vol de jeton par XSS.
- Le filtre serveur lit le jeton depuis le cookie (SPA) **ou** l'en-tête `Bearer`
  (intégrations/API).
- `POST /api/auth/logout` efface le cookie ; `GET /api/auth/me` restaure la session.

### MFA — TOTP (2e facteur)
- **RFC 6238** (HMAC-SHA1, pas de 30 s, 6 chiffres), compatible Google/Microsoft
  Authenticator. Aucune dépendance externe.
- `POST /api/auth/mfa/totp/setup` → renvoie le secret + l'URI `otpauth://` (QR code).
- `POST /api/auth/mfa/totp/enable` (code de confirmation) → active le TOTP.
- Au login : mot de passe → défi `mfaRequired: totp` → code TOTP → session.

### MFA — Question secrète **dynamique** (2e facteur)
- Plusieurs questions par utilisateur (réponses **hachées BCrypt**, normalisées).
- La question présentée **change chaque jour** (`jour de l'année % nombre de questions`)
  — d'où « dynamique » — tout en restant vérifiable de façon stateless.
- `POST /api/auth/mfa/question/setup` → enregistre les couples question/réponse.
- Au login : mot de passe → défi `mfaRequired: question` + la question du jour →
  réponse → session.

## 2. SSO fédéré — Microsoft Entra ID (Azure AD)

Intégration **OpenID Connect** via Spring Security OAuth2 Client, activée par le
profil `azure` (cf. `application-azure.yml`).

### Configuration
```bash
export AZURE_TENANT_ID=<id-locataire-entra>
export AZURE_CLIENT_ID=<id-application>
export AZURE_CLIENT_SECRET=<secret>
mvn spring-boot:run -Dspring-boot.run.profiles=azure
```
- **Redirect URI** à déclarer dans Entra : `https://<hôte>/login/oauth2/code/azure`.
- Le bouton **« Se connecter avec Microsoft »** du frontend redirige vers
  `/oauth2/authorization/azure` (proxifié vers le backend).
- `issuer-uri` : `https://login.microsoftonline.com/{tenant}/v2.0` (validation par JWKS).

### Bénéfices
- Authentification centralisée (comptes Ville), MFA géré par Entra, révocation,
  conditional access. Complète le MFA interne pour les comptes non fédérés.

## 3. Autorisation (RBAC)
- Rôles applicatifs : `ADMIN, REDACTEUR, JURIDIQUE, APPRO, DIRECTION, LECTEUR`.
- Contrôle par annotations `@PreAuthorize` sur les endpoints sensibles.

## 4. Conformité Loi 25
- Chiffrement en transit (TLS), cookies `HttpOnly`/`Secure`, MFA, hébergement Canada.
- IA **locale** (Ollama) — aucune donnée ne sort de l'infrastructure.
- Journal d'audit horodaté et attribué (`AuditLog`).

## 5. Comptes de démonstration

| Courriel | 2e facteur | Détail |
|----------|-----------|--------|
| redacteur@levis.qc.ca | — | connexion simple |
| totp@levis.qc.ca | TOTP | secret de test `JBSWY3DPEHPK3PXP` |
| question@levis.qc.ca | Question | réponse `montreal` |
| admin@levis.qc.ca | — | administrateur |

Mot de passe commun : `Passw0rd!` (à changer en production).
