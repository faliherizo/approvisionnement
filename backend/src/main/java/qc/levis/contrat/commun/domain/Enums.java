package qc.levis.contrat.commun.domain;

/** Énumérations métier de la solution. */
public final class Enums {
    private Enums() {}

    /**
     * Rôles applicatifs (RBAC) - cf. organigramme. SUPERADMIN est global (hors tenant). AUDITEUR consulte tout le tenant
     * en lecture seule (profil « Auditeur / conformité » de l'AO IQ 2026-6370-0005).
     */
    public enum Role {
        SUPERADMIN, ADMIN, REDACTEUR, JURIDIQUE, APPRO, DIRECTION, LECTEUR, AUDITEUR
    }

    /** Catégorie de clause. */
    public enum ClauseCategorie {
        STANDARD, OPTIONNELLE, PERSONNALISEE
    }

    /** Type de contrat / document. */
    public enum TypeContrat {
        DEMANDE_PRIX, APPEL_OFFRES_PUBLIC, GRE_A_GRE,
        BAIL, PRET_A_USAGE, CONTRAT_SERVICES, AVIS_INTENTION, ADDENDA
    }

    /** Mode de sollicitation. */
    public enum ModeSollicitation {
        DEMANDE_PRIX, APPEL_OFFRES_PUBLIC, GRE_A_GRE
    }

    /** Statut d'un document dans le circuit. */
    public enum StatutDocument {
        BROUILLON, EN_REVISION, EN_APPROBATION, APPROUVE, EN_SIGNATURE, SIGNE, PUBLIE, REFUSE
    }

    /** Statut d'un signataire dans la cérémonie de signature électronique. */
    public enum StatutSignature {
        EN_ATTENTE, SIGNE, REFUSE
    }

    /** Statut d'une étape d'approbation. */
    public enum StatutApprobation {
        EN_ATTENTE, APPROUVE, REFUSE
    }

    /** Type d'obligation contractuelle (post-exécution). */
    public enum ObligationType {
        LIVRABLE, RENOUVELLEMENT, PAIEMENT, GARANTIE, AUTRE
    }

    /** Statut d'une obligation. */
    public enum ObligationStatut {
        A_VENIR, ECHU, COMPLETE
    }

    /** Méthode de second facteur d'authentification. */
    public enum MfaMethode {
        NONE, TOTP, QUESTION
    }

    /** Attribut de contexte évalué par le moteur de règles. */
    public enum AttributCondition {
        TYPE_CONTRAT, MODE_SOLLICITATION, VALEUR_MIN, VALEUR_MAX, CONTEXTE
    }

    /** Statut d'un dossier d'approvisionnement (regroupement de documents - art. 15). */
    public enum StatutDossier {
        OUVERT, EN_COURS, EN_APPROBATION, ADJUGE, CLOTURE, ANNULE
    }

    /** Type d'acquisition d'un dossier d'approvisionnement (classement - art. 7.1, 15). */
    public enum TypeAcquisition {
        BIENS, SERVICES, SERVICES_PROFESSIONNELS, TRAVAUX_CONSTRUCTION, TECHNOLOGIES, AUTRE
    }

    /** Cible de publication externe d'un document (intégrations M365/SAP - art. 9). */
    public enum CiblePublication {
        SHAREPOINT, ONEDRIVE, OUTLOOK, TEAMS, SAP
    }

    /** Format de rendu publié vers une cible externe. */
    public enum FormatPublication {
        PDF, DOCX
    }
}
