# Sites vitrines des organisations, menu d'aide et vidéos

## Sites vitrines

Chaque organisation a son site de présentation, à l'adresse de son sous-domaine :

| Organisation | Vitrine |
|---|---|
| Investissement Québec | <https://iq.contrat.vinaitsolution.com/accueil> |
| Ville de Lévis | <https://levis.contrat.vinaitsolution.com/accueil> |
| Ville de Trois-Rivières | <https://v3r.contrat.vinaitsolution.com/accueil> |

Le domaine nu (<https://contrat.vinaitsolution.com/accueil>) garde la vitrine de la plateforme, avec le choix de
l'organisation. Une organisation dont la vitrine n'est pas publiée affiche aussi celle de la plateforme.

### Administration (superadministrateur)

<https://contrat.vinaitsolution.com/login> → Superadministration → **Site vitrine des organisations** :

1. choisir l'organisation ;
2. modifier le titre, l'accroche et les fonctionnalités présentées (ajouter, retirer, monter, descendre) ;
3. choisir la vidéo de présentation et ses sous-titres dans la médiathèque, ou téléverser une vidéo (MP4 ou WebM,
   30 Mo au plus) ;
4. cocher « Publiée », enregistrer, puis « Voir le site de l'organisation ».

API : `GET /api/vitrines/{organisation}` (public), `GET|PUT /api/superadmin/tenants/{id}/vitrine`,
`POST /api/superadmin/tenants/{id}/vitrine/video`, `GET /api/superadmin/mediatheque` (superadministrateur seulement).
Le contenu de départ de chaque organisation de démonstration vient de `backend/src/main/resources/data/vitrines.json`,
créé une seule fois : le superadministrateur le modifie ensuite, rien ne l'écrase au redémarrage.

## Menu d'aide

Le bouton **Aide** du bandeau ouvre, pour l'espace courant (CLM ou rédaction) :

- **Mise en route de l'organisation** (administrateurs) : une vidéo qui paramètre l'application de bout en bout avec le
  compte administrateur — listes de référence, groupes, notifications, paramètres, modèles et leur publication pour
  IQ ; comptes, paramètres de l'organisation, circuits, templates, gabarits et import de clauses pour Lévis et V3R ;
- **Pour cet écran** : les tutoriels qui passent par l'écran ouvert ;
- **Tous les tutoriels**, avec une recherche par titre ou par exigence.

Chaque tutoriel montre sa vidéo narrée et sous-titrée, puis ses étapes en texte (alternative à la vidéo).

## Vidéos

Les vidéos sont **tournées à partir des scénarios e2e** (`bdd/e2e/donnees/*.json`) : ce qu'elles montrent est ce que les
tests vérifient, et une vidéo se régénère dès qu'un écran change. Narration en français (voix macOS « Amélie ») avant
chaque geste, sous-titres WebVTT (WCAG 2.2 : 1.2.2 sous-titres, 1.2.5 audiodescription par la narration).

```bash
cd bdd
infra/bacs-a-sable/recette.sh                        # plateforme locale (base neuve)
node outils/videos.mjs voix                          # clips de narration (cache medias/.voix/)
node outils/videos.mjs tourner "E2E-(CB-IQ|ECR-IQ)-"  # tournage (une vidéo par scénario)
node outils/videos.mjs monter                        # voix + sous-titres → medias/aide/*.mp4|vtt, index de l'aide
node outils/videos.mjs tourner "E2E-(ECR-LEV|LEV)-" v3r   # mêmes parcours, comptes de V3R, sur v3r.e2e.localhost
node outils/videos.mjs monter
node outils/videos.mjs assembler iq                  # présentation de l'organisation → medias/vitrines/iq/
node outils/videos.mjs miseEnRoute iq                # mise en route → medias/aide/mise-en-route-iq.mp4 (une par organisation)
node outils/videos.mjs publier                       # rsync vers la médiathèque du serveur
```

- `medias/` reproduit la médiathèque servie par `/api/media/` ; il n'est pas versionné (volume
  `deploy/ovh/donnees/media` sur le serveur, `APP_MEDIA_REPERTOIRE`).
- La vidéo de présentation d'une organisation enchaîne, derrière un carton de titre, chacun de ses tutoriels.
- La mise en route assemble, dans l'ordre fixé par `miseEnRoute` dans les fichiers de scénarios, ceux qui paramètrent
  l'organisation.
- En CI, sans vidéo tournée, une médiathèque d'essai (`bdd/donnees/medias-essai/`) donne de quoi afficher le menu.
