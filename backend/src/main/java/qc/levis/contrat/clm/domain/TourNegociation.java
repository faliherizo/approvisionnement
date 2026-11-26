package qc.levis.contrat.clm.domain;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fin d'un tour de négociation (RST-55) : chaque proposition décidée donne le texte retenu de sa clause ; les autres clauses
 * sont reprises telles quelles. Le tour n'est conclu que lorsque toutes ses propositions sont décidées.
 */
public final class TourNegociation {

    private TourNegociation() {
    }

    public static List<ClauseNegociee> appliquer(List<ClauseNegociee> courantes, List<PropositionNegociation> propositions) {
        exigerToutesDecidees(propositions);
        Map<String, PropositionNegociation> parCle = propositions.stream()
                .collect(Collectors.toMap(PropositionNegociation::getCleClause, Function.identity(), (a, b) -> b));
        return courantes.stream().map(c -> parCle.containsKey(c.cle()) ? c.avecTexte(parCle.get(c.cle()).texteRetenu()) : c).toList();
    }

    /** L'entente est atteinte quand l'organisation accepte chaque proposition du tour telle quelle. */
    public static boolean toutAccepte(List<PropositionNegociation> propositions) {
        return propositions.stream().allMatch(p -> p.getDecision() == PropositionNegociation.Decision.ACCEPTEE);
    }

    private static void exigerToutesDecidees(List<PropositionNegociation> propositions) {
        long restantes = propositions.stream().filter(p -> !p.estDecidee()).count();
        if (restantes > 0) {
            throw new IllegalArgumentException("Décidez chaque proposition du tour : " + restantes + " restante(s).");
        }
    }
}
