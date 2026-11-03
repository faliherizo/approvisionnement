package qc.levis.contrat.clm.domain;

/**
 * Vérification d'identité du signataire selon le risque (RST-17, F-06/F-11). Les niveaux sont ordonnés :
 * un niveau imposé par l'initiateur ne peut jamais descendre sous le minimum exigé par le risque.
 */
public final class Identite {

    public enum Risque { FAIBLE, MOYEN, ELEVE }

    public enum Niveau { COURRIEL, CODE_SMS, QUESTION, FORTE }

    public static final int TENTATIVES_MAX = 5;
    public static final int CODE_VALIDITE_MINUTES = 10;

    private Identite() {
    }

    /** Canaux d'envoi du code : la vérification forte scinde le code entre courriel et SMS (double canal). */
    public static java.util.List<String> canauxDuCode(Niveau niveau) {
        return switch (niveau) {
            case COURRIEL -> java.util.List.of("COURRIEL");
            case CODE_SMS -> java.util.List.of("SMS");
            case FORTE -> java.util.List.of("COURRIEL", "SMS");
            case QUESTION -> java.util.List.of();
        };
    }

    public static Niveau minimumRequis(Risque risque, boolean externe) {
        Risque r = risque == null ? Risque.MOYEN : risque;
        if (!externe) {
            return r == Risque.ELEVE ? Niveau.CODE_SMS : Niveau.COURRIEL;
        }
        return switch (r) {
            case FAIBLE -> Niveau.COURRIEL;
            case MOYEN -> Niveau.CODE_SMS;
            case ELEVE -> Niveau.FORTE;
        };
    }

    public static Niveau niveauApplique(Risque risque, boolean externe, Niveau demande) {
        Niveau minimum = minimumRequis(risque, externe);
        return demande == null || demande.ordinal() < minimum.ordinal() ? minimum : demande;
    }

    /** Coordonnée exigée par le niveau : un code SMS sans téléphone ne peut pas être envoyé (UI-15). */
    public static void verifierCoordonnees(Niveau niveau, String courriel, String telephone, String question, String reponse) {
        if (courriel == null || !courriel.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Coordonnée invalide : courriel requis.");
        }
        boolean exigeTelephone = niveau == Niveau.CODE_SMS || niveau == Niveau.FORTE;
        if (exigeTelephone && (telephone == null || !telephone.replaceAll("[\\s().-]", "").matches("^\\+?\\d{10,15}$"))) {
            throw new IllegalArgumentException("Coordonnée invalide : téléphone requis pour la vérification par SMS.");
        }
        if (niveau == Niveau.QUESTION && (question == null || question.isBlank() || reponse == null || reponse.isBlank())) {
            throw new IllegalArgumentException("Question et réponse de vérification requises.");
        }
    }

    /** Comparaison tolérante d'une réponse à une question : casse, accents de surface et espaces ignorés. */
    public static String normaliserReponse(String reponse) {
        if (reponse == null) {
            return "";
        }
        String sansAccents = java.text.Normalizer.normalize(reponse, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", " ");
    }
}
