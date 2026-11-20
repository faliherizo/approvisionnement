package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Vérification d'identité du signataire selon le risque (RST-17, F-06/F-11). Les niveaux sont ordonnés :
 * un niveau imposé par l'initiateur ne peut jamais descendre sous le minimum exigé par le risque.
 */
public final class Identite {

    public enum Risque { FAIBLE, MOYEN, ELEVE }

    public enum Niveau { COURRIEL, CODE_SMS, QUESTION, FORTE }

    /**
     * Niveau minimum fixé pour un type de document (addenda 4, Q1), combiné au niveau de la politique : le plus exigeant
     * l'emporte. Un code de niveau inconnu ou absent ne change rien.
     */
    public static Niveau auMoins(Niveau niveau, String minimumDuType) {
        if (minimumDuType == null || minimumDuType.isBlank()) {
            return niveau;
        }
        for (Niveau n : Niveau.values()) {
            if (n.name().equalsIgnoreCase(minimumDuType.strip())) {
                return n.ordinal() > niveau.ordinal() ? n : niveau;
            }
        }
        return niveau;
    }

    private Identite() {
    }

    /**
     * Politique d'identité d'une organisation : niveau minimal par risque pour les signataires externes et internes,
     * tentatives permises et validité d'un code. Un risque plus élevé n'exige jamais un niveau plus faible ; un risque
     * sans niveau défini exige le plus fort (refus par défaut).
     */
    public record Politique(Map<Risque, Niveau> externes, Map<Risque, Niveau> internes, int tentativesMax, int codeValiditeMinutes) {

        public static final Politique DEFAUT = new Politique(
                Map.of(Risque.FAIBLE, Niveau.COURRIEL, Risque.MOYEN, Niveau.CODE_SMS, Risque.ELEVE, Niveau.FORTE),
                Map.of(Risque.FAIBLE, Niveau.COURRIEL, Risque.MOYEN, Niveau.COURRIEL, Risque.ELEVE, Niveau.CODE_SMS), 5, 10);

        public Politique {
            externes = copie(externes);
            internes = copie(internes);
        }

        private static Map<Risque, Niveau> copie(Map<Risque, Niveau> niveaux) {
            Map<Risque, Niveau> m = new EnumMap<>(Risque.class);
            if (niveaux != null) {
                niveaux.forEach((r, n) -> {
                    if (r != null && n != null) {
                        m.put(r, n);
                    }
                });
            }
            return Collections.unmodifiableMap(m);
        }

        public Niveau minimumRequis(Risque risque, boolean externe) {
            Risque r = risque == null ? Risque.MOYEN : risque;
            return (externe ? externes : internes).getOrDefault(r, Niveau.FORTE);
        }

        public Niveau niveauApplique(Risque risque, boolean externe, Niveau demande) {
            Niveau minimum = minimumRequis(risque, externe);
            return demande == null || demande.ordinal() < minimum.ordinal() ? minimum : demande;
        }

        public List<String> erreurs() {
            List<String> erreurs = new ArrayList<>();
            croissante(externes, "externes", erreurs);
            croissante(internes, "internes", erreurs);
            if (tentativesMax < 3 || tentativesMax > 10) {
                erreurs.add("Tentatives de vérification permises : entre 3 et 10.");
            }
            if (codeValiditeMinutes < 5 || codeValiditeMinutes > 60) {
                erreurs.add("Validité d'un code de vérification : entre 5 et 60 minutes.");
            }
            return erreurs;
        }

        private static void croissante(Map<Risque, Niveau> niveaux, String signataires, List<String> erreurs) {
            Niveau precedent = Niveau.COURRIEL;
            for (Risque r : Risque.values()) {
                Niveau n = niveaux.get(r);
                if (n == null) {
                    erreurs.add("Niveau d'identité requis pour les signataires " + signataires + " au risque " + r + ".");
                    return;
                }
                if (n.ordinal() < precedent.ordinal()) {
                    erreurs.add("Signataires " + signataires + " : le risque " + r + " ne peut exiger moins que le risque précédent.");
                    return;
                }
                precedent = n;
            }
        }
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
