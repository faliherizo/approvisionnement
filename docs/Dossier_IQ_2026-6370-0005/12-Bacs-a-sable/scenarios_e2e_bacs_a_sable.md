# Scénarios e2e sur les bacs à sable : ICP, LibreOffice, veraPDF, pyHanko

Ces scénarios éprouvent, de bout en bout et **depuis l'interface**, les exigences fonctionnelles qui dépendent de
services externes :

- autorité de certification et d'horodatage ;
- conversion Word ;
- conformité PDF/A ;
- validation des signatures.

Chaque contrôle est rendu par l'**application externe elle-même**, jamais par la plateforme qui a produit le
document :

- veraPDF juge le PDF/A ;
- pyHanko juge le PAdES ;
- OpenSSL juge le CAdES ;
- l'ICP répond en RFC 3161 et OCSP, et publie sa liste de révocation.

- **Implémentation :** [`e2e/views/clm.bacs-a-sable.spec.ts`](../../../e2e/views/clm.bacs-a-sable.spec.ts)
- **Données :** [`e2e/data/bacs-a-sable.json`](../../../e2e/data/bacs-a-sable.json)
- **Services :** [`infra/bacs-a-sable/docker-compose.yml`](../../../infra/bacs-a-sable/docker-compose.yml) ; guide
  [§ 2.3](guide_bacs_a_sable.md#23-icp-locale-conversion-et-conformité-addenda-4-q4-q14-q22)

## 1. Préalables

1. Démarrer les bacs à sable :

   ```bash
   docker compose -f infra/bacs-a-sable/docker-compose.yml up -d --build --wait
   ```

2. Démarrer le serveur avec les variables du guide :
   - `CLM_CACHET_*` : magasin de l'ICP et autorité d'horodatage ;
   - `CLM_CONVERSION_MODE=service` ;
   - `CLM_SIGNATURE_FOURNISSEUR=cachet` ;
   - `NOTIFICATION_COURRIEL_MODE=smtp`, avec `MAIL_PORT=11025`.
3. Lancer la suite :

   ```bash
   cd e2e && E2E_FRONTEND_URL=http://localhost:5175 E2E_BACKEND_URL=http://localhost:8100 \
     npx playwright test -c playwright.iso.config.ts views/clm.bacs-a-sable.spec.ts
   ```

**Si un service ne répond pas,** la suite s'ignore et l'indique. Elle ne passe jamais au vert sans avoir rien vérifié.

**Si le serveur n'est pas branché sur les bacs à sable,** le premier scénario échoue et dit pourquoi : le motif de
version annonce « mise en page simplifiée », ou le cachet est celui de démonstration.

## 2. Scénarios

| # | Scénario | Exigences | Service externe juge | Verdict attendu |
|---|---|---|---|---|
| BS-01 | Contrat généré depuis un modèle **sans gabarit**, téléchargé depuis l'onglet Documents | A-29 (Q14), C-46, D-01 | veraPDF REST, profil 2b | `compliant = true`, 0 règle en échec |
| BS-02 | Gabarit Word d'IQ déposé dans l'éditeur, contrat généré **en Word** | A-30 (Q22), A-18 (Q26), C-32, D-06 | Lecture du `.docx` produit | Variables (corps, tableau, en-tête) et clauses fusionnées ; aucun marqueur résiduel ; gras du titre conservé |
| BS-03 | Même gabarit, contrat généré **en PDF** | A-30 (Q22), A-29 (Q14) | Gotenberg (conversion) puis veraPDF | Motif « mise en page conservée » ; texte fusionné présent ; `compliant = true` |
| BS-04 | Certificat de signature téléchargé depuis l'onglet Preuve | C-63, C-64, S-10, A-29 ; Q4 (validation à long terme) | veraPDF ; pyHanko, profil PAdES-B-LTA avec révocation intégrée exigée | PDF/A-2b conforme ; signature VALID, cachet de confiance, jeton de l'autorité d'horodatage, LTA |
| BS-05 | Signature externe au portail : invitation et code reçus par **courriel SMTP**, identité vérifiée, signature | S-04, C-61, C-62, C-01, ST-09 | Mailpit (SMTP) ; OpenSSL `cms -verify` | Reçu `CAC-…` ; attestation CAdES valide pour la seule racine de l'ICP ; **refusée** si une donnée attestée change |
| BS-06 | Intégrité vérifiée depuis l'onglet Preuve, puis dossier complet exporté (ZIP) | C-64, C-65, D-11, S-07, S-10, A-11 | OpenSSL (`.p7s` du document signé) ; pyHanko (`certificat.pdf` de l'archive) | « Intégrité confirmée » ; `.p7s` valide sur le document de l'archive ; certificat de l'archive VALID B-LTA |
| BS-07 | L'autorité **suspend** le cachet ; un certificat scellé ensuite est soumis à pyHanko ; puis l'autorité **rétablit** le cachet | C-63 ; Q4 (révocation) | ICP (OCSP et liste) ; pyHanko | Pendant la suspension : refusé, « revoked » ; après le rétablissement : VALID |
| BS-08 | Horodatage RFC 3161 de l'ICP interrogé comme par la plateforme | C-63 | ICP `/tsa` ; OpenSSL `ts -verify` (pyHanko, BS-04) | Jeton accordé, chaîné à la racine ; aucune autre autorité acceptée |

Chaque scénario d'interface vérifie aussi l'écran parcouru, comme toute vue CLM : absence de débordement et de
superposition, cibles d'au moins 24 px, et axe-core sans violation (SGQRI 008 3.0, WCAG 2.2 AA).

## 3. Détail des scénarios

### BS-01 — PDF/A-2b d'un contrat sans gabarit

1. Préparation par l'API, au nom de l'initiateur puis de l'administration d'affaires : modèle simple (corps à
   variables, sans gabarit), soumis et publié ; trousse liée au modèle ; valeurs saisies.
2. Dans l'onglet **Documents**, l'initiateur clique **Générer depuis le modèle**.
3. Il télécharge le document généré, et le fichier téléchargé est envoyé à veraPDF REST (`/api/validate/2b`).
4. Attendu : `compliant` vrai et `failedRules` = 0.

### BS-02 et BS-03 — Gabarit Word d'IQ

1. L'initiateur crée un modèle avec les champs et la clause des données. Dans l'éditeur, il dépose le gabarit
   (`fabriquer_gabarit.py`), dont la variable du titre est répartie sur deux runs, comme Word l'enregistre. Le message
   « Gabarit en service » s'affiche.
2. Le modèle est soumis, puis publié. La trousse est créée et ses valeurs saisies.
3. Il clique **Générer en Word modifiable**. Le `.docx` téléchargé contient les valeurs fusionnées dans le titre, le
   tableau et l'en-tête, ainsi que la clause au marqueur. Aucun `{{` ne subsiste.
4. Il clique **Générer depuis le modèle** (PDF). La version porte le motif « mise en page conservée », signe que la
   conversion a été faite par Gotenberg. Le texte extrait contient les valeurs, et veraPDF juge le PDF/A-2b conforme.

### BS-04 — Certificat de signature PAdES-B-LTA

1. Dans l'onglet **Preuve** de la trousse de BS-03, l'utilisateur clique **Télécharger le certificat (PDF)**.
2. veraPDF valide le profil 2b. pyHanko valide le profil `pades-lta` avec `--force-revinfo` et la seule racine de
   l'ICP pour ancre.
3. Attendu : verdict VALID, « The signer's certificate is trusted », « backed by a time stamping authority ».

### BS-05 — Signature externe, attestation CAdES

1. L'initiateur prépare une trousse : un PDF, un signataire externe à l'adresse unique, une étape de signature. Il
   lance le parcours.
2. Le lien `/portail/<jeton>` est lu dans la boîte Mailpit du signataire, à l'aide de son API.
3. Au portail, le signataire clique **Recevoir mon code** ; le code est lu dans Mailpit et saisi, puis **Vérifier mon
   identité**. Il ouvre le document, coche le consentement, saisit son nom et clique **Signer**.
4. Le reçu affiche une attestation `CAC-…`. La preuve expose l'attestation, que l'on soumet à `POST /cms` avec le
   contenu attesté reconstitué : empreinte, nom, courriel, niveau d'identité et instant du consentement. OpenSSL rend
   « Verification successful ».
5. Contre-épreuve : le même envoi, avec un niveau d'identité modifié, est refusé.

### BS-06 — Intégrité et export du dossier de preuve

1. Dans l'onglet **Preuve** de la trousse signée, l'utilisateur clique **Vérifier l'intégrité** : « Intégrité
   confirmée ».
2. Il clique **Exporter le dossier complet (ZIP)**. Dans l'archive :
   - la signature `.p7s` du document signé est vérifiée par OpenSSL sur le document de l'archive ;
   - `certificat.pdf` est validé par pyHanko au profil B-LTA ;
   - le manifeste cite chaque fichier.

### BS-07 — Révocation : suspension puis rétablissement

1. `POST /admin/cachet/suspendre` sur l'ICP. Le certificat de la trousse est téléchargé de nouveau, puis soumis à
   pyHanko : refus, « revoked ». Le serveur a lui-même intégré la réponse OCSP qui le dit.
2. `POST /admin/cachet/retablir`, dans un bloc `finally`, pour que la suite reste rejouable. Un certificat téléchargé
   ensuite est VALID.

### BS-08 — Autorité d'horodatage

Une requête RFC 3161 est construite comme celle de la plateforme : SHA-256, demande du certificat de l'autorité, nonce.
Elle est envoyée à `POST /tsa` en `application/timestamp-query`.

Attendu : une réponse `application/timestamp-reply` au statut « accordé ». Son jeton est vérifié par OpenSSL contre la
racine de l'ICP, à l'aide du service de validation.

## 4. Résultats du 2026-11-20

8 scénarios sur 8 conformes, deux exécutions successives (31 s et 66 s), le serveur branché sur les bacs à sable :

| # | Verdict de l'application externe |
|---|---|
| BS-01 | veraPDF : PDF/A-2b conforme, 0 règle en échec |
| BS-02 | Word : titre, tableau, en-tête et clause fusionnés, gras d'origine conservé, aucun marqueur |
| BS-03 | Gotenberg : « mise en page conservée » ; veraPDF : conforme |
| BS-04 | veraPDF : conforme ; pyHanko : VALID, cachet de confiance, jeton de l'autorité d'horodatage, profil B-LTA |
| BS-05 | Mailpit : invitation et code reçus ; OpenSSL : attestation valide ; niveau d'identité modifié refusé |
| BS-06 | Interface : « Intégrité confirmée » ; OpenSSL : `.p7s` valide sur le document de l'archive ; pyHanko : certificat de l'archive VALID |
| BS-07 | Pendant la suspension, pyHanko refuse (« revoked ») ; après le rétablissement, VALID |
| BS-08 | OpenSSL : jeton accordé, vérifié contre la racine ; un autre document est refusé |

Le premier passage a révélé un défaut du test, et non de la plateforme : dans BS-07, la suspension était appliquée hors
du bloc `finally`, et un échec de lecture laissait le cachet suspendu. La suspension se fait désormais dans le bloc
protégé.

## 5. Hors de cette suite

| Exigence | Pourquoi | Où c'est couvert |
|---|---|---|
| A-21, C-66 : certificat de destruction scellé | La conservation dure au moins un an : l'échéance n'est pas atteignable sans horloge injectée | `CertificatDestructionServiceTest`, `ConservationServiceTest`. Même chaîne de scellement que BS-04 |
| PDF déposés par les utilisateurs | Conservés tels quels, leur conformité PDF/A dépend de leur source (limite déclarée) | — |
| Certificat d'une autorité reconnue, TSA qualifiée | Décision d'IQ ; l'ICP du bac à sable en tient lieu | Recette avec IQ, mêmes scénarios |
