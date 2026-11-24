# Comptes de démonstration par organisation

Chaque organisation (tenant) a sa propre adresse, sous le domaine de la plateforme. La page d'accueil
<https://contrat.vinaitsolution.com/accueil> propose, par « Accéder à l'application », la liste des organisations
actives (celles créées dans l'espace superadministrateur) et mène à la page de connexion de celle choisie.

Un compte ne se connecte que sur l'adresse de son organisation : un compte de Lévis est refusé sur
`iq.contrat.vinaitsolution.com`, et inversement.

**Mot de passe commun à tous les comptes de démonstration : `Passw0rd!`** — à changer avant toute démonstration
ouverte à des tiers (le mot de passe figure dans les données d'amorçage du dépôt).

## Ville de Lévis — <https://levis.contrat.vinaitsolution.com>

Rédaction contractuelle (approvisionnement). Comptes amorcés par `UserSeeder`, à partir de
`backend/src/main/resources/data/users.json`, sur une base vide.

| Courriel | Nom | Rôles | Usage en démonstration |
|---|---|---|---|
| `admin@levis.qc.ca` | Admin Système | ADMIN | Utilisateurs, référentiels, thème, journal d'audit |
| `redacteur@levis.qc.ca` | Rédacteur Appro | REDACTEUR, APPRO | Dossiers, devis, documents, bibliothèque de clauses |
| `juriste@levis.qc.ca` | Juriste | JURIDIQUE | Révision juridique, commentaires, clauses normatives |
| `direction@levis.qc.ca` | Directrice | DIRECTION | Approbation, tableau de bord |
| `totp@levis.qc.ca` | Agent TOTP | REDACTEUR | Double authentification par application (TOTP) |
| `question@levis.qc.ca` | Agent Question | REDACTEUR | Double authentification par question de sécurité |

- `totp@levis.qc.ca` : secret TOTP `JBSWY3DPEHPK3PXP`, à ajouter dans une application d'authentification
  (Google Authenticator, Microsoft Authenticator…).
- `question@levis.qc.ca` : réponse `montreal` aux questions de sécurité.

## Investissement Québec — <https://iq.contrat.vinaitsolution.com>

Gestion du cycle de vie des contrats et signature (appel d'offres IQ 2026-6370-0005), assistant IA du CLM.
Comptes amorcés par `TenantsDemoSeeder`, à partir de `backend/src/main/resources/data/tenants-demo.json`
(variable `APP_TENANTS_DEMO=true`).

| Courriel | Nom | Profil IQ | Rôle |
|---|---|---|---|
| `initiateur@iq.example` | Camille Tremblay | Initiateur | REDACTEUR |
| `validateur@iq.example` | Louis Gagnon | Validateur | JURIDIQUE |
| `approbateur@iq.example` | Sophie Roy | Approbateur | DIRECTION |
| `signataire@iq.example` | Étienne Côté | Signataire interne | LECTEUR |
| `admin.affaires@iq.example` | Julie Bouchard | Administrateur d'affaires | ADMIN_AFFAIRES |
| `admin.ti@iq.example` | Patrick Morin | Administrateur principal (affaires et technique) | ADMIN |
| `admin.technique@iq.example` | Nadia Ouellet | Administrateur technique | ADMIN_TECHNIQUE |
| `auditeur@iq.example` | Nadia Lavoie | Auditeur / conformité | AUDITEUR |

Parcours type : l'initiateur crée une trousse, le validateur la révise, l'approbateur l'approuve, le signataire
signe ; l'auditeur consulte la preuve et le journal.

## Ville de Trois-Rivières — <https://v3r.contrat.vinaitsolution.com>

Rédaction contractuelle (approvisionnement). Comptes amorcés par `TenantsDemoSeeder` (même fichier que IQ) ;
l'organisation reçoit à sa création la bibliothèque de départ (clauses et gabarits de `data/clauses.json` et
`data/templates.json`), pour que la démonstration ne commence pas sur un écran vide.

| Courriel | Nom | Profil | Rôles |
|---|---|---|---|
| `admin@v3r.example` | Isabelle Gagnon | Administrateur | ADMIN |
| `approvisionnement@v3r.example` | Marc Lemieux | Approvisionnement (rédaction) | REDACTEUR, APPRO |
| `juridique@v3r.example` | Sophie Bélanger | Services juridiques | JURIDIQUE |
| `direction@v3r.example` | Louis Tremblay | Direction (approbation) | DIRECTION |
| `consultation@v3r.example` | Julie Pépin | Consultation | LECTEUR |

## Administration de la plateforme — <https://contrat.vinaitsolution.com/login>

Le superadministrateur (`superadmin@plateforme.ca`) se connecte sur le domaine nu, sans organisation. Son mot de
passe n'est pas documenté ici : il est communiqué séparément. Il crée, modifie, désactive les organisations et leur
donne un administrateur ; une organisation active apparaît aussitôt dans le choix de la page d'accueil.

## Ajouter une organisation

1. La créer dans l'espace superadministrateur (son identifiant court devient le sous-domaine : `levis`, `iq`…).
2. Si son sous-domaine n'est pas couvert par le certificat, l'y ajouter (voir `docs/deploiement-ovh.md`,
   « Sous-domaines des organisations »).
3. Pour une organisation de démonstration permanente, l'ajouter à `tenants-demo.json` : ses comptes sont recréés
   au démarrage s'ils manquent, sans jamais écraser un compte existant.
