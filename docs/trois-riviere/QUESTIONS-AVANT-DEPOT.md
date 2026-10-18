---
title: "Questions — Appel d'offres n° 7000017905"
subtitle: "Solution d'aide à la rédaction et à la gestion des documents d'approvisionnement — Ville de Trois-Rivières"
author: "VINA IT Solutions inc."
date: "Septembre 2026"
lang: fr-CA
---

**Objet : Questions préalables au dépôt (mises à jour après l'addenda n° 1) — Appel d'offres n° 7000017905 (solution d'aide à la rédaction et à la gestion des documents d'approvisionnement)**

**À :** Sarra Boulahmi, Direction de l'approvisionnement — SBoulahmi@v3r.net

Bonjour Madame Boulahmi,

Nous préparons actuellement notre soumission à l'appel d'offres n° 7000017905 et souhaiterions obtenir quelques précisions avant de la finaliser. Nous avons pris connaissance de l'**addenda n° 1 (17 septembre 2026)**, qui ajoute le tableau de conformité (Section III) et la formule de qualification (Section IV) : les questions qu'il a permis de résoudre ont été retirées de la présente liste. En particulier, l'addenda confirme que **les données doivent être hébergées au Canada** et que **la démonstration ne porte que sur les fonctionnalités réellement offertes**, transmise par enregistrement vidéo — nous n'y revenons donc pas. Vous trouverez ci-dessous les questions qui demeurent, regroupées par thème. Celles portant sur les certifications de sécurité nous sont particulièrement utiles, car elles peuvent influencer l'admissibilité et le chiffrage.

## Sécurité et certifications (ISO 27001 / SOC 2 Type II)

Pour ce mandat, la solution sera hébergée sur Microsoft Azure, dont l'infrastructure est certifiée ISO 27001 et SOC 2 Type II. Nous aimerions confirmer le niveau attendu à l'article 16 :

1. L'exigence de certification porte-t-elle sur l'hébergement (l'infrastructure) ou sur le fournisseur lui-même (le soumissionnaire) ?
2. S'il s'agit d'une certification du fournisseur, est-ce une condition d'admissibilité au moment du dépôt, ou une exigence à satisfaire avant la mise en production ?
3. L'hébergement sur une infrastructure certifiée ISO 27001 et SOC 2 Type II, documenté par les attestations de l'hébergeur et une matrice de responsabilité partagée, est-il jugé suffisant pour répondre à l'exigence ?
4. À défaut d'une certification du fournisseur, accepteriez-vous des preuves équivalentes, comme nos politiques de sécurité, les résultats de balayages de vulnérabilités ou un rapport de tests d'intrusion ?
5. Un rapport SOC 2 Type II récent de Microsoft Azure serait-il recevable comme preuve, au besoin sous entente de confidentialité ?

## Hébergement, résidence des données et continuité

6. Au-delà de l'hébergement au Canada confirmé par l'addenda n° 1, avez-vous des cibles contractuelles de reprise (RTO/RPO) et un taux de disponibilité mensuel minimal, par exemple 99,9 % ou 99,99 %, assorti de crédits de service ?
7. Attendez-vous des exigences particulières de sauvegarde ou de conservation au-delà des mécanismes offerts par Azure ?

## Intégrations Microsoft 365, SAP, GED et authentification

8. Pour l'activation des connecteurs Microsoft 365 (SharePoint, OneDrive, Outlook, Teams), la Ville fournira-t-elle les accès Entra ID nécessaires, et à quel moment : au paramétrage ou à la mise en production ?
9. Pour SAP, quels sont le module et la version en place, et quel protocole d'intégration privilégiez-vous ? Quelles données doivent circuler, et dans quel sens ?
10. Une gestion électronique de documents autre que SharePoint est-elle en usage et doit-elle être intégrée ?
11. L'authentification unique doit-elle reposer exclusivement sur Microsoft Entra ID, ou d'autres fournisseurs d'identité sont-ils envisagés ?
12. L'addenda classe les intégrations bureautiques comme « souhaitables » : confirmez-vous que la **capacité** à les réaliser suffit à l'évaluation, l'activation se faisant en phase d'implantation, plutôt qu'une intégration opérationnelle dès le dépôt ?

## Démonstration et modalités de dépôt

13. Quels sont le **format de fichier** et la **durée maximale** attendus pour la captation vidéo de démonstration ? (Le mode de transmission — enregistrement vidéo — est confirmé par l'addenda n° 1.)
14. Y a-t-il une limite de taille des pièces déposées dans SEAO et des formats imposés ?
15. Souhaitez-vous disposer d'accès nominatifs à notre environnement de démonstration pour les membres du comité, en complément de la vidéo ?

## Migration des contenus

16. Quel est le volume approximatif des contenus à migrer, sous quels formats, et de quels systèmes proviennent-ils ?
17. La migration d'essai doit-elle être réalisée sur un jeu de données que vous fournirez, et selon quels critères d'acceptation ?
18. Le nettoyage et la normalisation des contenus sources relèvent-ils de la Ville ou du fournisseur ?

## Bordereau, prix et durée

19. Confirmez-vous la structure du bordereau : trois années fermes suivies de sept années optionnelles, avec prix unitaire et montant, taxes en sus ?
20. Une indexation annuelle des prix des années optionnelles est-elle permise, et selon quel indice ?
21. L'addenda (exigence n° 17) demande de détailler les coûts ponctuels, récurrents et additionnels : comment souhaitez-vous que les services d'implantation, de formation et de migration apparaissent — inclus dans le forfait annuel du bordereau, ou détaillés à titre indicatif en pièce jointe ?
22. Attendez-vous des environnements distincts pour la production, l'essai et la formation, et sont-ils tarifés séparément ?

## Soutien, maintenance et gouvernance

23. Quelles plages de couverture du soutien et quels délais de prise en charge et de résolution attendez-vous, par niveau de priorité ?
24. Quelle fenêtre de maintenance planifiée est acceptable, et avec quel préavis ?
25. Des pénalités ou des crédits de service sont-ils rattachés au non-respect des délais de soutien ou du taux de disponibilité ?

## Réversibilité, propriété et gestion des vulnérabilités

26. Au-delà de l'exportation complète en fin de contrat (exigence n° 19), des formats ouverts précis sont-ils exigés pour la réversibilité, et attendez-vous des exports périodiques ?
27. L'addenda prévoit la possibilité de désactiver l'IA (exigence n° 24) : souhaitez-vous que l'assistant d'intelligence artificielle soit désactivé par défaut à la livraison, puis activé sur décision, ou activé dès le départ avec traçabilité ?
28. Concernant l'annexe A sur la gestion des vulnérabilités, prévoyez-vous des balayages conjoints, à quelle fréquence, et quels délais de correction exigez-vous selon la criticité ?

## Divers

29. Une rencontre de clarification ou une présentation est-elle prévue avant le dépôt ?
30. Y a-t-il des exigences contractuelles particulières, comme des assurances, un cautionnement ou des clauses de propriété intellectuelle, que nous devrions intégrer à notre chiffrage ?

Nous vous remercions à l'avance de ces précisions et vous saurions gré de nous indiquer la date limite de la période de questions. Nous demeurons à votre disposition pour tout échange.

Je vous prie d'agréer, Madame Boulahmi, mes salutations distinguées.

RANDRIAMIRINDRA Faliherizo
VINA IT Solutions inc.
faliherizo@vinaitsolution.com
https://vinaitsolution.com
