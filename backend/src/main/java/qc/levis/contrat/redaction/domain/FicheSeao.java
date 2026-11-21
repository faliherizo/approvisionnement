package qc.levis.contrat.redaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Métadonnées d'un avis destinées au SEAO (Lévis DAO 2.3, C-20). Le SEAO n'offre pas d'interface publique de dépôt : la
 * fiche est reprise dans le SEAO par la personne autorisée et versée au dossier. Ses éléments suivent le jeu de données
 * ouvertes des avis du SEAO (numéro, organisme, titre, type d'avis, nature du contrat, montant, adjudicataire).
 */
public record FicheSeao(String numero, String titre, String organisme, String typeAvis, String nature, BigDecimal valeurEstimee,
                        String statut, String adjudicataire, Long referenceInterne, Instant produiteLe) {

    /** Le numéro d'appel d'offres et le titre identifient l'avis dans le SEAO : sans eux, aucune fiche n'est produite. */
    public static FicheSeao de(Document d, String organismeParDefaut, Instant le) {
        if (blanc(d.getNumeroAO())) throw new ExportSeaoImpossible("Le numéro d'appel d'offres est requis pour l'export SEAO.");
        if (blanc(d.getTitre())) throw new ExportSeaoImpossible("Le titre du devis est requis pour l'export SEAO.");
        String organisme = blanc(d.getOrganismePublic()) ? organismeParDefaut : d.getOrganismePublic();
        return new FicheSeao(d.getNumeroAO().trim(), d.getTitre().trim(), organisme, typeAvis(d), nature(d.getType()),
                d.getValeurEstimee(), d.getStatut().name(), d.getPrestataire(), d.getId(), le);
    }

    static String typeAvis(Document d) {
        if (d.getType() == Enums.TypeContrat.AVIS_INTENTION) return "Avis d'intention";
        if (d.getModeSollicitation() == null) return "Avis d'appel d'offres";
        return switch (d.getModeSollicitation()) {
            case APPEL_OFFRES_PUBLIC -> "Avis d'appel d'offres";
            case DEMANDE_PRIX -> "Appel d'offres sur invitation";
            case GRE_A_GRE -> "Contrat conclu de gré à gré";
        };
    }

    /** Nature du contrat au sens du SEAO ; un type qui ne la détermine pas est signalé à préciser plutôt que deviné. */
    static String nature(Enums.TypeContrat type) {
        if (type == null) return NATURE_A_PRECISER;
        return switch (type) {
            case CONTRAT_SERVICES -> "Services";
            case BAIL, PRET_A_USAGE -> "Autre";
            default -> NATURE_A_PRECISER;
        };
    }

    public static final String NATURE_A_PRECISER = "À préciser";

    private static boolean blanc(String s) {
        return s == null || s.isBlank();
    }

    /** Le devis ne porte pas les renseignements qu'exige un avis SEAO. */
    public static class ExportSeaoImpossible extends IllegalStateException {
        public ExportSeaoImpossible(String message) {
            super(message);
        }
    }
}
