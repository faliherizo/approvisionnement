package qc.levis.contrat.interop.domain;

import java.util.List;

/** Bilan d'une reprise de l'historique, ligne par ligne (CHU-8.04) ; en essai, rien n'est écrit. */
public record BilanReprise(boolean essai, int total, int acceptes, int rejetes, List<Ligne> lignes) {

    public enum Statut { VALIDE, REPRISE, REJETEE }

    /** Résultat d'un enregistrement : son rang (à partir de 1), le numéro de fiche attribué ou le motif du rejet. */
    public record Ligne(int rang, Statut statut, String numero, String motif) {
        public boolean acceptee() {
            return statut != Statut.REJETEE;
        }
    }

    public BilanReprise {
        lignes = List.copyOf(lignes);
    }

    public static BilanReprise de(boolean essai, List<Ligne> lignes) {
        int acceptes = (int) lignes.stream().filter(Ligne::acceptee).count();
        return new BilanReprise(essai, lignes.size(), acceptes, lignes.size() - acceptes, lignes);
    }
}
