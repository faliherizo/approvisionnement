# Questions à poser au CHU (avis d'intention — manifestation d'intérêt)

> À clarifier avant/pendant la manifestation d'intérêt (clôture **2026-10-26**, contrat visé **2026-11-01**).
> Objectif : cadrer précisément le besoin pour démontrer notre capacité à « répondre à l'ensemble du besoin
> et assurer une transition sécuritaire, complète et sans interruption » (condition de l'avis).

## A. Périmètre & transition (gré à gré → appel d'offres possible)

1. Peut-on obtenir l'**export intégral structuré** actuel (schémas de formulaires, règles, workflows,
   données) de la plateforme Dexero, afin d'évaluer la reconstruction et la migration ?
2. Combien de **formulaires / applications** actifs, de **règles d'affaires** et de **workflows** existent
   aujourd'hui dans chaque portail (RH, Approvisionnements) ? Volume de **données** et de **fiches** ?
3. Quelles **intégrations externes** sont en place (systèmes CHU liés, API, SSO) et doivent être reprises ?
4. Quelle est la **fenêtre de transition** acceptable et la tolérance d'interruption (RPO/RTO attendus) ?
5. Les **4 applications Web** mentionnées : lesquelles, et quelle complexité (formulaire simple vs app) ?

## B. Hébergement, disponibilité, conformité (§1, §7)

6. « Hébergement des RP **au Québec** » : exigence stricte Québec, ou **Canada** acceptable pour certaines
   copies/journaux ? (impacte le choix Azure/AWS et le coût)
7. SLA **99,8 %** : périmètre exact (heures ouvrables vs 24/7), pénalités, fenêtres de maintenance exclues ?
8. **SSO** : quel fournisseur d'identité (Microsoft Entra ID, autre) et protocole (OIDC/SAML) ? MFA imposé ?
9. **Test d'intrusion** du portail RH : à notre charge (firme externe) ou au CHU ? périmètre/fréquence ?
10. Exigences d'**accessibilité** : niveau visé (SGQRI 008 3.0 / WCAG 2.2 **AA** ?) et preuve attendue
    (rapport axe-core, audit manuel) ?

## C. Formulaires & règles (§3, §4)

11. **Formulaires multilingues** : traduire le **contenu** des formulaires (libellés, choix) FR/EN, ou
    seulement l'interface d'administration ? Langues cibles ?
12. **Activation par plage réseau** : format attendu (CIDR, plages IP du CHU, VPN) ?
13. **Gestion d'inventaire/capacité** (« inscriptions à des activités ») : besoin réel ? décrire un cas
    (ex. places limitées par activité, liste d'attente ?).
14. **Formulaires liés parent-enfant** : exemples de processus multi-niveaux attendus ?
15. **Approbations** : les circuits sont-ils **multi-niveaux conditionnels** (par montant, type, secteur) ?
    délais/rappels/escalades/délégation attendus ?

## D. Notifications, documents, rapports (§5, §6)

16. **Production de documents** : quels gabarits PDF à générer depuis les fiches (ex. bon de commande,
    attestation RH) ? transmission par courriel et/ou dépôt au dossier ?
17. **Canaux** : SMS requis (fournisseur ?), messagerie interne (portée : fil par demande, ou messagerie
    complète ?) ?
18. **Tableaux de bord / graphiques** : indicateurs attendus, par portail ? temps réel ou périodiques ?
19. **Conservation des données** : durées de rétention par type de fiche (calendrier de conservation du
    CHU) ? gel légal applicable ?
20. Formats d'**export** exigés (Excel, CSV, PDF, autre) et contraintes de classification à l'export ?

## E. Interopérabilité & données (§8)

21. Quels **systèmes du CHU** doivent échanger avec la plateforme (noms, protocoles, volumétrie) ?
22. Standard d'**API** attendu (REST/OAuth2 ; conventions institutionnelles) et jeu de données pivot ?

## F. Licences, soutien, services (§9)

23. Nombre d'**administrateurs/concepteurs** à habiliter par portail ? volumétrie d'utilisateurs finaux ?
24. **Banque d'heures** : volume annuel estimé et nature des travaux prioritaires (config, dev, rapports,
    formation) ?
25. Attentes de **formation** (nombre de sessions, publics) et de documentation ?

## G. Décisions structurantes (pour notre proposition)

26. Cloisonnement RH/Approvisionnements : **logique** (un déploiement, séparation par domaine) ou
    **technique** (déploiements séparés) ? (impacte l'architecture et le prix)
27. Stockage objet des **pièces jointes** : région, chiffrement, rétention — contraintes spécifiques ?
28. Budget indicatif / structure de prix attendue (licence annuelle + hébergement + banque d'heures) pour
    cadrer l'offre sur les 12 mois + 2×12.
