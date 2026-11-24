# ADR-027 — Analyse antivirus des fichiers reçus par ClamAV

- **Statut** : Accepté
- **Date** : 2026-11-22
- **Portée** :
  - port `commun.antivirus.application.Antivirus` ;
  - adaptateurs `AntivirusClamd` (exploitation) et `AntivirusSignatures` (poste, défaut) ;
  - pièces jointes des fiches (`document.application.DocumentService`) ;
  - documents du CLM (`AdaptateursClm.AntivirusSignatures`, port `AnalyseAntivirus`, qui délègue désormais au port
    commun).
- **Origine** : CHU 7.08 (protection contre les maliciels) ; UI-05 d'IQ.

## Contexte

- **Une seule signature** : le CLM ne reconnaissait que la signature de test EICAR, faute d'antivirus réel.
- **Des fichiers non analysés** : les pièces jointes des fiches (formulaires du CHU) étaient conservées sans analyse.
- **Des fichiers de l'extérieur** : ils viennent de répondants et de signataires externes, et sont ensuite ouverts par
  le personnel. C'est la voie d'entrée classique d'un maliciel.

## Options envisagées

1. **ClamAV, démon clamd, protocole INSTREAM** : libre, image officielle, signatures mises à jour par `freshclam`. Le
   protocole est publié et stable ; aucun fichier temporaire n'est écrit.
2. **Service infonuagique d'analyse** (Microsoft Defender for Storage, VirusTotal) : l'analyse est asynchrone, après
   dépôt, et le fichier sort de l'hébergement canadien pour VirusTotal.
3. **Antivirus de l'organisation cliente** (ICAP) : il dépend de chaque client ; il reste possible plus tard, comme un
   nouvel adaptateur du même port.

## Décision

Option 1, derrière un port commun à toute la plateforme.

- **Sélection par configuration** : `ANTIVIRUS_MODE=clamav`, avec `ANTIVIRUS_CLAMAV_HOTE` et `ANTIVIRUS_CLAMAV_PORT`.
  Sur un poste, `signatures` (défaut) reconnaît EICAR, ce qui suffit à démontrer le refus.
- **Analyse avant conservation** : un fichier infecté n'est jamais enregistré. L'utilisateur reçoit un 400 qui nomme le
  fichier.
- **Fermé par défaut** : si l'analyse ne peut pas être menée (clamd injoignable, délai, erreur), le fichier est refusé
  avec un 503 « réessayez plus tard ». Il n'est jamais accepté sans analyse.
- **Un seul antivirus** : le port du CLM (`AnalyseAntivirus`) garde son contrat et délègue au port commun. Le module
  n'est pas modifié au-delà de son adaptateur.
- **Démarrage** : un hôte absent en mode `clamav` fait échouer le démarrage.

## Conséquences

- **Positives** : les pièces jointes des formulaires et les documents du CLM sont analysés par le même moteur, à jour.
  Changer d'antivirus revient à ajouter un adaptateur.
- **Négatives** :
  - l'hébergement exploite un conteneur clamd (environ 1 Go de mémoire, signatures chargées au démarrage) ;
  - une panne de clamd bloque les téléversements, ce qui est voulu.
- **Vérification** :
  - `AntivirusTest` : contrat des deux adaptateurs, protocole INSTREAM contre un faux clamd, erreur, démon injoignable,
    configuration incomplète ;
  - `DocumentServiceTest` : un fichier infecté n'est pas enregistré ;
  - le scénario BDD @CHU-7.08, contre le vrai ClamAV du bac à sable (`iq-bac-antivirus`, port 13310).
