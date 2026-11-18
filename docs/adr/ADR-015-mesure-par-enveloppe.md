# ADR-015 — Mesure de l'utilisation par enveloppe envoyée

- **Statut** : Accepté
- **Date** : 2026-11-17
- **Portée** : `clm.domain.Enveloppes`, `clm.application.UtilisationService`, `clm.infrastructure.web.UtilisationController`,
  bloc « Utilisation par enveloppe » du journal d'audit
- **Origine** : addenda 3, Q39 (unité de référence : l'enveloppe envoyée ; annulée avant envoi non comptée ; pas de
  double facturation ; expliquer renvois, expirations, annulations après envoi, corrections)

## Contexte

IQ validera les factures sur le nombre d'enveloppes envoyées. La plateforme n'avait pas de compteur, et un compteur
séparé du journal pourrait diverger de ce qui s'est réellement passé.

## Options envisagées

1. **Compteur incrémenté à l'envoi** : simple, mais non vérifiable et sujet aux doubles comptes lors des renvois.
2. **Compter les invitations** : facture plusieurs fois une même trousse (plusieurs signataires, rappels, nouveaux cycles).
3. **Dériver la mesure du journal scellé (retenue)** : chaque enveloppe facturée correspond à un événement
   `INVITATION_ENVOYEE` horodaté et chaîné, vérifiable par IQ.

## Décision

- Une **enveloppe** est une trousse dont une première invitation à signer est partie, quel que soit le nombre de
  documents ou de signataires. Elle est comptée **une fois**, au mois de ce premier envoi.
- **Non comptés** : la trousse annulée avant tout envoi ; les renvois (rappel manuel, signataire remplacé, nouveau cycle
  après correction, retour à une étape) sur une enveloppe déjà comptée.
- **Comptée** : l'enveloppe annulée, rejetée ou échue après envoi (l'invitation est partie).
- Le relevé distingue enveloppes envoyées, renvois non facturés, terminées, rejetées, annulées avant et après envoi,
  échéances dépassées, et la ventilation par mois.
- La règle est une fonction pure du domaine (`Enveloppes.compter`) : testée à 100 %, sans horloge ni base.

## Conséquences

- Question N1 posée à IQ : une invitation **renvoyée après expiration** compte-t-elle de nouveau ? La règle actuelle
  répond non ; une réponse contraire ne change qu'une branche de `Enveloppes.compter`.
- Le relevé est accessible à l'administration et à l'audit (`/api/clm/admin/utilisation`).
- Tests : `Addenda3DomaineTest`, `Addenda3IntegrationTest`, e2e « Addenda 3 — Q39 / Q48 ».
