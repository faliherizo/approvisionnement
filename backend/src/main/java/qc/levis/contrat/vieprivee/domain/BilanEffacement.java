package qc.levis.contrat.vieprivee.domain;

/**
 * Résultat de l'effacement dans une source : renseignements effacés ou pseudonymisés, renseignements conservés et le motif
 * légal de leur conservation (Loi 25, art. 28.1 : la personne peut exiger l'effacement sauf si la loi impose de conserver).
 */
public record BilanEffacement(String source, int effaces, int conserves, String motifConservation) {
    public static BilanEffacement complet(String source, int effaces) {
        return new BilanEffacement(source, effaces, 0, null);
    }
}
