package qc.levis.contrat.clm.domain;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Comptage des documents et des pages signés (Annexe 10, AF-49), pour la reddition de comptes : au total, par unité et par
 * mois. Fonction pure ; un document cité deux fois n'est compté qu'une fois.
 */
public final class ComptageSignes {

    /** Clé des documents dont la trousse n'a pas d'unité. */
    public static final String SANS_UNITE = "";

    private ComptageSignes() {
    }

    /** {@code sansPagination} : documents signés dont le nombre de pages n'a pas pu être compté (format non paginable). */
    public record Compte(int documents, long pages, int sansPagination) {

        public static final Compte ZERO = new Compte(0, 0, 0);

        Compte plus(DocumentSigne d) {
            return d.pages() == null ? new Compte(documents + 1, pages, sansPagination + 1)
                    : new Compte(documents + 1, pages + d.pages(), sansPagination);
        }
    }

    /** Totaux de la période, par unité (ordre alphabétique) et par mois (ordre chronologique). */
    public record Releve(Compte total, Map<String, Compte> parUnite, Map<YearMonth, Compte> parMois) {
    }

    public static Releve compter(List<DocumentSigne> signes, ZoneId fuseau) {
        Map<Long, DocumentSigne> uniques = new LinkedHashMap<>();
        signes.forEach(d -> uniques.putIfAbsent(d.documentId(), d));
        Compte total = Compte.ZERO;
        Map<String, Compte> parUnite = new TreeMap<>();
        Map<YearMonth, Compte> parMois = new TreeMap<>();
        for (DocumentSigne d : uniques.values()) {
            total = total.plus(d);
            parUnite.compute(d.unite() == null ? SANS_UNITE : d.unite(), (k, c) -> (c == null ? Compte.ZERO : c).plus(d));
            parMois.compute(YearMonth.from(d.signeLe().atZone(fuseau)), (k, c) -> (c == null ? Compte.ZERO : c).plus(d));
        }
        return new Releve(total, parUnite, parMois);
    }
}
