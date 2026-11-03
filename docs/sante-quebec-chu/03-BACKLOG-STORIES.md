# Backlog — Épopées et user stories (CHU)

Convention : chaque story porte un tag **♻️/🔁/🔨** et **CA** = critères d'acceptation. Règle transverse
appliquée à **toutes** les stories : *aucun champ/colonne figé — la structure est une donnée de
référence* ; *tout module est activable/désactivable par tenant depuis le superadmin*.

---

## Épopée A — Socle configurable (P0)

**A1 — Registre de modules (superadmin)** 🔁
> En tant que **superadmin**, je veux **activer/désactiver des modules par tenant** afin d'ajuster l'offre
> (RH, Approvisionnements, 4 applications Web) sans déploiement.
- **CA** : un module a un état par tenant (activé/désactivé) ; activer un module active ses **dépendances**
  et l'opérateur en est informé ; désactiver bloque l'accès (**refus par défaut côté serveur**, 403) et
  masque les vues ; journalisé à l'audit ; aucune donnée perdue à la réactivation.

**A2 — Dictionnaire de types de champs & ensembles d'options** 🔨
> En tant qu'**administrateur**, je veux que **types de champs et listes d'options** soient des **données
> de référence** afin d'étendre la plateforme sans code.
- **CA** : ajouter un type de champ ou une liste d'options = configuration (pas de migration) ;
  surchargeable par tenant (global `tenant_id = null` + override par `code`).

**A3 — Environnement d'acceptation** 🔨
> En tant qu'**équipe CHU**, je veux un **3ᵉ environnement (acceptation)** distinct de prod/dév afin de
> valider les changements avant mise en production.
- **CA** : environnement isolé, même code, données cloisonnées ; promotion de configuration prod←acceptation tracée.

**A4 — Gestion des sessions & responsive** ♻️🔨
- **CA** : durée de session configurable, expiration/renouvellement ; affichage validé desktop/tablette/mobile (390/768/1280/1680 px, zéro débordement, cibles ≥ 24 px).

---

## Épopée B — Moteur de formulaires (P1)

**B1 — Composer un formulaire multi-pages** 🔁🔨
> En tant qu'**administrateur**, je veux **créer un formulaire à une ou plusieurs pages** avec des champs
> configurables afin de numériser un processus.
- **CA** : structure stockée en JSON (Formulaire→Version→Page→Champ) ; ordre des champs/pages
  configurable ; libellés **multilingues** (FR/EN) ; aucun champ en dur.

**B2 — Palette complète de types de champs** 🔁🔨
> En tant qu'**administrateur**, je veux les types : texte, zone-texte, case à cocher, bouton radio, liste,
> date, courriel, hyperlien, nombre, décimale, **montant**, **pourcentage**, **fichier**, **matrice**,
> **valeur calculée/dynamique**.
- **CA** : chaque type a ses propriétés configurables (décimales, options, min/max, format) ; **fichier**
  = téléversement avec **analyse antivirus** et types/ taille autorisés configurables ; **matrice** =
  lignes/colonnes configurables ; **calculé** = en lecture seule, jamais accepté du client.

**B3 — Aperçu avant soumission** 🔨
- **CA** : l'utilisateur voit un récapitulatif fidèle des réponses avant de soumettre.

**B4 — Versions, approbation, retour arrière** ♻️🔁
> En tant qu'**administrateur**, je veux **versionner** un formulaire, **approuver** avant mise en ligne et
> **revenir** à une version antérieure.
- **CA** : chaque publication crée une version (snapshot) ; une version ne passe « en ligne » qu'après
  approbation (circuit configurable) ; restauration d'une version antérieure sans perte d'historique.

**B5 — Duplication / export / import de structure** 🔨
- **CA** : dupliquer un formulaire ; exporter sa définition (JSON) ; réimporter dans un autre
  environnement (dév→acceptation→prod).

---

## Épopée C — Règles, validation, automatisation (P2)

**C1 — Logique conditionnelle de champ** 🔨
> En tant qu'**administrateur**, je veux **afficher/masquer/rendre obligatoire** un champ selon les
> réponses.
- **CA** : conditions `champ / opérateur / valeur` combinables (ET/OU) ; appliquées côté client **et
  revalidées côté serveur** ; configurables sans code.

**C2 — Validation croisée & de format** 🔁
- **CA** : règles de format (courriel, nombre, date, intervalle) et croisées (champ A < champ B) ;
  messages d'erreur configurables ; refus serveur si invalide.

**C3 — Champs calculés par formules** 🔁
- **CA** : formules (somme/différence/produit/quotient/pourcentage) ; **détection de cycle** ; recalcul
  automatique ; valeurs calculées non modifiables par l'utilisateur.

**C4 — Routage par règles** 🔁
- **CA** : routage multi-niveaux selon `champ/opérateur/valeur` → rôle/étape ; configurable par tenant.

**C5 — Déclencheurs d'événements** 🔨
- **CA** : à la saisie/modification d'une fiche, déclencher une action (notification, calcul, changement d'état) selon règle configurable.

**C6 — Inventaire / capacité** 🔨
- **CA** : un formulaire d'inscription a une **capacité maximale** ; choix restreints/fermés à capacité atteinte ; compteur fiable en concurrence.

---

## Épopée D — Soumissions & cycle de vie (P3)

**D1 — Soumettre et sauvegarder automatiquement** 🔁🔨
- **CA** : brouillon **autosauvegardé** ; reprise ultérieure par l'utilisateur ; soumission change l'état en « soumis ».

**D2 — Fenêtres d'activation** 🔨
- **CA** : ouverture/fermeture par **date**, **nb max de répondants**, **plage IP** ; configurables ; message adapté quand fermé.

**D3 — Anti-robot (captcha)** 🔨
- **CA** : captcha configurable sur formulaires **publics** ; soumissions automatisées bloquées.

**D4 — Formulaires parent-enfant** 🔨
- **CA** : un formulaire enfant est lié à une fiche parent ; navigation et agrégation multi-niveaux ; droits hérités configurables.

**D5 — Workflow d'approbation daté** ♻️🔁
- **CA** : circuit résolu selon l'organigramme **à la date** ; décisions conservées au retour arrière ; états brouillon/partagé/validé/rejeté.

---

## Épopée E — Notifications & documents (P4)

**E1 — Notifications multicanal par déclencheur** 🔁
- **CA** : courriel, **SMS (Twilio/Telnyx)**, **messagerie interne** ; modèles configurables ; déclencheurs (soumission, changement d'état, échéance) ; journal d'envoi.

**E2 — Confirmations & alertes** 🔨
- **CA** : confirmation au répondant à la soumission ; alerte aux administrateurs à la création/modif d'une fiche.

**E3 — Document depuis les données** ♻️
- **CA** : génération PDF/DOCX à partir des réponses (gabarit configurable) ; transmission au répondant/administrateur.

---

## Épopée F — Sécurité fine & conformité (P5)

**F1 — Groupes & droits** 🔨
- **CA** : groupes d'utilisateurs avec droits lecture/modification/suppression distincts.

**F2 — Contrôle d'accès granulaire** 🔁🔨
- **CA** : permissions par **utilisateur/groupe/rôle** × **formulaire/champ/page/fiche/bouton/élément** ; refus par défaut ; configurable par tenant.

**F3 — Classification & masquage** 🔨
- **CA** : classer champs/données (**public/interne/confidentiel/secret**) ; **masquage/chiffrement/
  restriction d'affichage ou d'export** selon rôle/contexte ; appliqué à l'export aussi.

**F4 — Protection anti-abus** 🔁🔨
- **CA** : WAF (signatures OWASP), **rate-limit par IP**, **verrouillage** après N échecs, antivirus sur fichiers, en-têtes de durcissement.

**F5 — Traçabilité & rétention** ♻️🔁
- **CA** : audit des accès et modifications (option **chaîné SHA-256**) ; **durée de conservation** et effacement (droit à l'oubli, Loi 25).

**F6 — Test d'intrusion** 🔨 (prestation)
- **CA** : pentest du portail RH par firme externe ; rapport + plan de correction suivi.

---

## Épopée G — Rapports, exports, API, interop (P6)

**G1 — Recherche & critères sauvegardés** ♻️🔨
- **CA** : recherche multi-critères ; **sauvegarde/rappel** des critères ; résultats filtrés par droits.

**G2 — Exports & tableaux de bord** 🔁🔨
- **CA** : export **Excel/CSV/PDF** (toutes fiches ou sélection) ; **graphiques et tableaux de bord
  configurables** ; export intégral structuré (réversibilité).

**G3 — API REST publique tokenisée** 🔨
- **CA** : endpoints consulter/récupérer/créer/compléter des fiches ; auth **OAuth2 client_credentials /
  clé d'API / PAT** ; quotas et journalisation ; documentée (OpenAPI).

**G4 — Interopérabilité CHU** ♻️🔨
- **CA** : échanges formulaires ↔ systèmes externes CHU ; **reprise** des échanges/automatisations/
  configurations existants ; connecteurs configurables.

---

### Priorisation (MoSCoW synthétique)

- **Must** : A1, A2, B1, B2, B4, C1, C2, C3, D1, D5, E1, F2, F3, F4, G2, G3.
- **Should** : A3, B3, B5, C4, C5, C6, D2, D3, D4, E2, E3, F1, F5, G1, G4.
- **Could** : raffinements d'éditeur visuel (glisser-déposer), WebAuthn, BI avancé.
- **Won't (pour l'instant)** : miroir de production iso-HA pour l'acceptation (non exigé).