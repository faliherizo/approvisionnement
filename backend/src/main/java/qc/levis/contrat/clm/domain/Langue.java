package qc.levis.contrat.clm.domain;

import java.util.Locale;

/**
 * Langue de communication d'un intervenant (addenda 2, Q19) : le français est obligatoire et vaut par défaut ; l'anglais
 * est offert lorsqu'IQ le requiert pour un signataire. Elle règle les messages reçus et la langue d'ouverture du portail.
 */
public enum Langue {
    FR, EN;

    /** Saisie vide : français. Toute autre valeur que FR ou EN est refusée. */
    public static Langue de(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return FR;
        }
        try {
            return valueOf(saisie.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Langue de communication : FR ou EN.", e);
        }
    }

    /** Code de langue de l'interface (« fr », « en »). */
    public String code() {
        return name().toLowerCase(Locale.ROOT);
    }
}
