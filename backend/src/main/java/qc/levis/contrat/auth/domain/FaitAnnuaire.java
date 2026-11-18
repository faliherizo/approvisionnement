package qc.levis.contrat.auth.domain;

import java.util.Locale;

/**
 * Fait d'annuaire d'un compte (addenda 3, Q43-Q44) : appartenance à un groupe (identifiant ou nom, ex. Microsoft Entra ID)
 * ou valeur d'un attribut (ex. {@code department}). Comparé sans égard à la casse.
 *
 * @param attribut nom de l'attribut ; nul pour un groupe
 */
public record FaitAnnuaire(Source source, String attribut, String valeur) {

    public enum Source { GROUPE, ATTRIBUT }

    public FaitAnnuaire {
        if (source == null || valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Fait d'annuaire : source et valeur requises.");
        }
        valeur = valeur.strip();
        attribut = source == Source.GROUPE || attribut == null ? null : attribut.strip();
    }

    public static FaitAnnuaire groupe(String valeur) {
        return new FaitAnnuaire(Source.GROUPE, null, valeur);
    }

    public static FaitAnnuaire attribut(String nom, String valeur) {
        return new FaitAnnuaire(Source.ATTRIBUT, nom, valeur);
    }

    /** Vrai si ce fait satisfait la condition (source, attribut, valeur) d'une correspondance. */
    public boolean correspond(Source s, String a, String v) {
        return source == s && valeur.equalsIgnoreCase(v)
                && (s == Source.GROUPE || attribut != null && attribut.equalsIgnoreCase(a));
    }

    /** Clé normalisée, pour comparer deux ensembles de faits. */
    public String cle() {
        return source + "|" + (attribut == null ? "" : attribut.toLowerCase(Locale.ROOT)) + "|" + valeur.toLowerCase(Locale.ROOT);
    }
}
