package qc.levis.contrat.clm.domain;

/**
 * Clause soumise à la négociation avec un tiers (RST-55, IA-05) : sa clé la relie à la clause du modèle d'où elle vient,
 * son texte est celui de la version où elle figure. Valeur immuable : une version négociée est une liste de clauses.
 */
public record ClauseNegociee(String cle, String titre, String texte) {

    public ClauseNegociee {
        if (cle == null || cle.isBlank()) {
            throw new IllegalArgumentException("Clé de clause requise.");
        }
        cle = cle.strip();
        titre = titre == null || titre.isBlank() ? cle : titre.strip();
        texte = texte == null ? "" : texte.strip();
    }

    /** Même clause, autre texte : la clé et le titre ne se négocient pas. */
    public ClauseNegociee avecTexte(String nouveau) {
        return new ClauseNegociee(cle, titre, nouveau);
    }
}
