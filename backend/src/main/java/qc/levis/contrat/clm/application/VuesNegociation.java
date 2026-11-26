package qc.levis.contrat.clm.application;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.ComparaisonClauses;
import qc.levis.contrat.clm.domain.EtatNegociation;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.VersionNegociation;

/** Vues d'une négociation (RST-55) : l'organisation voit tout ; la contrepartie ne voit ni suggestion ni décideur. */
@Component
public class VuesNegociation {

    private final RegistreNegociation registre;

    public VuesNegociation(RegistreNegociation registre) {
        this.registre = registre;
    }

    public NegociationDto.NegociationVue interne(Negociation n, List<String> actions) {
        return new NegociationDto.NegociationVue(n.getId(), n.getEtat().name(), n.getContrepartieNom(), n.getContrepartieCourriel(),
                n.getContrepartieEntreprise(), n.getVersionCourante(), n.getTour(), n.getExpireLe(), n.getOuvertePar(), n.getOuverteLe(),
                n.getTermineeLe(), n.getMotifFin(), clauses(registre.courantes(n)), versions(n),
                registre.propositions(n).stream().map(p -> proposition(p, true)).toList(), actions);
    }

    /** Actions offertes à l'organisation selon l'état ; toutes exigent la gestion de la trousse. */
    public static List<String> actions(Negociation n, boolean gestion) {
        if (!gestion || !n.estOuverte()) {
            return List.of();
        }
        List<String> actions = new java.util.ArrayList<>();
        if (n.getEtat() == EtatNegociation.ATTENTE_INTERNE) {
            actions.addAll(List.of("SUGGERER", "DECIDER"));
        }
        if (n.getEtat() == EtatNegociation.ENTENTE) {
            actions.add("CLORE");
        }
        actions.add("ANNULER");
        return actions;
    }

    public List<NegociationDto.PropositionVue> propositionsExternes(Negociation n) {
        return registre.propositions(n).stream().map(p -> proposition(p, false)).toList();
    }

    public static List<NegociationDto.ClauseVue> clauses(List<ClauseNegociee> clauses) {
        return clauses.stream().map(c -> new NegociationDto.ClauseVue(c.cle(), c.titre(), c.texte())).toList();
    }

    /** Écarts entre deux versions de la négociation, dans un sens comme dans l'autre. */
    public NegociationDto.ComparaisonVue comparer(Negociation n, int de, int a) {
        List<NegociationDto.EcartVue> ecarts = ComparaisonClauses.comparer(registre.clauses(n, de), registre.clauses(n, a)).stream()
                .map(e -> new NegociationDto.EcartVue(e.cle(), e.titre(), e.changement().name(), e.segments().stream()
                        .map(s -> new NegociationDto.SegmentVue(s.nature().name(), s.texte())).toList()))
                .toList();
        return new NegociationDto.ComparaisonVue(de, a, ecarts);
    }

    private List<NegociationDto.VersionVue> versions(Negociation n) {
        return registre.versions(n).stream().map(VuesNegociation::version).toList();
    }

    static NegociationDto.VersionVue version(VersionNegociation v) {
        return new NegociationDto.VersionVue(v.getNumero(), v.getAuteur(), v.getMotif(), v.getCreeLe(), v.getEmpreinte(), v.estIntegre());
    }

    static NegociationDto.PropositionVue proposition(PropositionNegociation p, boolean interne) {
        return new NegociationDto.PropositionVue(p.getId(), p.getTour(), p.getCleClause(), p.getTitreClause(), p.getTexteBase(),
                p.getTextePropose(), p.getCommentaire(), p.getProposeeLe(), p.getDecision().name(), p.getTexteContre(),
                p.getJustification(), interne ? p.getDecidePar() : null, p.getDecideLe(), interne ? suggestion(p) : null);
    }

    private static NegociationDto.SuggestionVue suggestion(PropositionNegociation p) {
        if (p.getSuggestionPosition() == null) {
            return null;
        }
        return new NegociationDto.SuggestionVue(p.getSuggestionPosition().name(), p.getSuggestionResume(), p.getSuggestionSource(),
                p.getSuggereeLe());
    }
}
