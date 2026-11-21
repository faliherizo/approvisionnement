package qc.levis.contrat.vieprivee.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifiant qui remplace une personne effacée (Loi 25, art. 28.1). Tiré au hasard et jamais relié au courriel d'origine :
 * contrairement à une empreinte du courriel, il ne permet pas de réidentifier la personne par essai de courriels connus.
 */
public record Pseudonyme(String valeur) {

    private static final String PREFIXE = "personne-effacee-";

    public Pseudonyme {
        Objects.requireNonNull(valeur, "pseudonyme");
        if (!valeur.startsWith(PREFIXE)) throw new IllegalArgumentException("Pseudonyme mal formé : " + valeur);
    }

    public static Pseudonyme nouveau(UUID hasard) {
        return new Pseudonyme(PREFIXE + hasard.toString().substring(0, 12));
    }

    /** Adresse non routable (RFC 2606, domaine .invalid) qui garde l'unicité exigée sur les comptes. */
    public String courriel() {
        return valeur + "@efface.invalid";
    }
}
