package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.clm.application.port.ConseillerNegociation;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.RecommandationNegociation;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.commun.module.application.ModuleNonSouscrit;

/**
 * Négociation assistée (RST-55, ADR-038) : pour chaque proposition en attente, un résumé des écarts et une position
 * recommandée au regard de la clause normative du modèle. L'IA locale répond si elle le peut ; sinon, les règles
 * déterministes. La suggestion est consignée avec sa source, et n'est jamais appliquée : une personne décide.
 */
@Service
public class AssistanceNegociationService {

    static final String SOURCE_REGLES = "Règles de la bibliothèque (repli déterministe)";

    private final RegistreNegociation registre;
    private final VuesNegociation vues;
    private final LectureTrousse lecture;
    private final ConseillerNegociation conseiller;
    private final ListesClm listes;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public AssistanceNegociationService(RegistreNegociation registre, VuesNegociation vues, LectureTrousse lecture,
                                        ConseillerNegociation conseiller, ListesClm listes, JournalClm journal, ContexteClm ctx) {
        this.registre = registre;
        this.vues = vues;
        this.lecture = lecture;
        this.conseiller = conseiller;
        this.listes = listes;
        this.journal = journal;
        this.ctx = ctx;
    }

    @Transactional
    public NegociationDto.NegociationVue suggerer(Long id, Habilitations.Acteur acteur) {
        Negociation n = registre.exiger(id);
        Trousse t = lecture.exigerGestion(n.getTrousseId(), acteur);
        Map<String, ClauseNegociee> normatives = registre.clauses(n, 1).stream()
                .collect(Collectors.toMap(ClauseNegociee::cle, Function.identity()));
        Map<String, List<String>> attendus = attendus();
        List<PropositionNegociation> enAttente = registre.duTour(n).stream().filter(p -> !p.estDecidee()).toList();
        if (enAttente.isEmpty()) {
            throw new IllegalStateException("Aucune proposition en attente de décision.");
        }
        for (PropositionNegociation p : enAttente) {
            PropositionNegociation.Suggestion s = suggestion(p, normatives.getOrDefault(p.getCleClause(), base(p)), attendus);
            p.suggerer(s, ctx.maintenant());
            registre.enregistrer(p);
            journal.consigner(t, "NEGOCIATION_SUGGESTION", acteur.id(), p.getTitreClause() + " · " + s.position() + " · " + s.source());
        }
        return vues.interne(n, VuesNegociation.actions(n, true));
    }

    /** La position nommée par l'IA prévaut ; à défaut, celle des règles. Le résumé de l'IA, s'il existe, accompagne celui des règles. */
    PropositionNegociation.Suggestion suggestion(PropositionNegociation p, ClauseNegociee normative, Map<String, List<String>> attendus) {
        RecommandationNegociation.Recommandation r = RecommandationNegociation.recommander(normative, p.getTextePropose(), attendus);
        return conseiller.conseiller(new ConseillerNegociation.Demande(p.getTitreClause(), normative.texte(), p.getTexteBase(),
                        p.getTextePropose(), p.getCommentaire()))
                .map(avis -> new PropositionNegociation.Suggestion(avis.position().orElse(r.position()),
                        tronquer(avis.resume() + "\n" + r.resume()), "IA locale (" + avis.modele() + ")"))
                .orElseGet(() -> new PropositionNegociation.Suggestion(r.position(), r.resume(), SOURCE_REGLES));
    }

    /** La liste « Clause attendue » relève de l'option IA d'analyse : sans elle, seul l'écart au texte normatif est mesuré. */
    private Map<String, List<String>> attendus() {
        try {
            return listes.motsCles(ListesClm.Liste.CLAUSE_ATTENDUE);
        } catch (ModuleNonSouscrit e) {
            return Map.of();
        }
    }

    /** Clause ajoutée en cours de négociation, sans texte normatif : la référence est son texte de départ. */
    private static ClauseNegociee base(PropositionNegociation p) {
        return new ClauseNegociee(p.getCleClause(), p.getTitreClause(), p.getTexteBase());
    }

    private static String tronquer(String s) {
        return s.length() <= 4000 ? s : s.substring(0, 4000);
    }
}
