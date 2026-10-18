# Captation vidéo — publication réelle Office 365 (à intégrer à la démo vitrine)

Produit un segment vidéo montrant la **publication réelle** d'un document vers **SharePoint, OneDrive,
Teams et Outlook**, en réutilisant **votre session Office 365** (votre profil Chrome), puis l'ajoute à
la fin de la vidéo de démonstration du site vitrine.

> **Confidentialité (Loi 25)** : le parcours publie des **documents de démonstration**, pas de vraies
> données municipales. La vidéo est publiable sur la vitrine. N'utilisez pas de contenu réel sensible.

## Pré-requis

1. **Chrome complètement fermé** (le profil est verrouillé s'il est ouvert). Sur macOS : `⌘Q` sur Chrome.
2. **Déjà connecté à Office 365** dans le profil choisi (SharePoint/Teams/OneDrive/Outlook accessibles
   sans nouvelle authentification).
3. **Application démarrée** avec la **config M365 réelle active** pour le tenant (Paramètres →
   Intégration Microsoft 365, « Activer » coché). Par défaut l'app est attendue sur `http://localhost:8082`.
4. `ffmpeg` installé (`ffmpeg -version`).

## Étape 1 — Captation

```bash
cd e2e
CHROME_PROFILE="Default" node tour/o365-capture.mjs
```

Le script : se connecte à l'app (admin), crée un document de démo, l'assemble et le valide, **publie
vers les 4 cibles**, puis **ouvre chaque application Office 365** pour montrer le fichier déposé. Il
filme tout en continu (lent) → `e2e/tour/o365-video/publication-o365.webm`.

Il **écrit dans la console** si un dépôt est réel (`✔ SHAREPOINT : dépôt réel → https://…`) ou retombé
en simulation (`⚠ … dépôt SIMULÉ …` = config M365 réelle inactive pour ce tenant).

### Variables reconnues

| Variable | Rôle | Défaut |
|----------|------|--------|
| `CHROME_PROFILE` | Profil Chrome à utiliser (session O365) | `Default` |
| `APP_URL` | URL de l'application | `http://localhost:8082` |
| `ADMIN_EMAIL` / `ADMIN_PASS` | Compte admin de l'app | `admin@levis.qc.ca` / `Passw0rd!` |
| `SLOW` | Facteur de ralentissement (2 = deux fois plus lent) | `1` |
| `SP_URL` | Bibliothèque SharePoint à montrer | site `Gestiondesapprovisionnements` |
| `ONEDRIVE_URL` | OneDrive à montrer | `…-my.sharepoint.com` |
| `TEAMS_URL` | Teams (canal Fichiers) | `teams.microsoft.com` |
| `OUTLOOK_URL` | Outlook (éléments envoyés) | `outlook.office.com/mail/sentitems` |

> 💡 Pour cadrer précisément sur le **dossier** où le fichier est déposé, passez l'URL profonde exacte,
> ex. `SP_URL="https://vinaitsolution.sharepoint.com/sites/Gestiondesapprovisionnements/Documents%20partages/Demonstration/2027"`.

Trouver le bon profil Chrome : `chrome://version` (ligne « Profile Path » : `…/Default` ou `…/Profile 3`).

## Étape 2 — Montage dans la vidéo vitrine

```bash
e2e/tour/monter-demo.sh
```

Ajoute le segment O365 **à la fin** de `frontend/public/media/demo-vina.mp4`, **conserve la narration**
de la démo, régénère `demo-vina.webm`, et **sauvegarde l'original** (`demo-vina.avant-o365-*.mp4`). La
page vitrine (`/accueil` → « Démonstration ») lit ces fichiers — aucun changement de code nécessaire.

## Dépannage

- **`Target profile in use` / échec de lancement** → Chrome n'est pas fermé.
- **Dépôts `sim://`** → la config M365 réelle n'est pas active pour le tenant (activez-la dans Paramètres).
- **Page Office 365 vide / redemande de connexion** → le profil choisi n'a pas de session O365 ; relancez
  avec le bon `CHROME_PROFILE`.
- **Teams n'affiche pas les fichiers** → passez `TEAMS_URL` pointant directement l'onglet *Fichiers* du
  canal (copiez l'URL depuis Teams web).

## Rejouer / annuler

- Relancer une captation : ré-exécutez l'étape 1 (nouvelle vidéo), puis l'étape 2.
- Revenir à la démo d'avant : `cp frontend/public/media/demo-vina.avant-o365-*.mp4 frontend/public/media/demo-vina.mp4`
  puis régénérez le WebM.
