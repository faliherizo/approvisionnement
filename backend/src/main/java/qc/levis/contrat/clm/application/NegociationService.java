package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.TourNegociation;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Négociation côté organisation (RST-55, IA-05) : chaque proposition du tour est acceptée, refusée ou contre-proposée par une
 * personne ; le tour produit une version numérotée ; la clôture, sur entente seulement, verse la version négociée au contrat
 * de la trousse, dont le parcours de validation et de signature reprend sur cette version.
 */
@Service
public class NegociationService {

    private final RegistreNegociation registre;
    private final VuesNegociation vues;
    private final LectureTrousse lecture;
    private final ClausesDuModele clausesDuModele;
    private final DocumentClmService documents;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public NegociationService(RegistreNegociation registre, VuesNegociation vues, LectureTrousse lecture, ClausesDuModele clausesDuModele,
                              DocumentClmService documents, JournalClm journal, ContexteClm ctx) {
        this.registre = registre;
        this.vues = vues;
        this.lecture = lecture;
        this.clausesDuModele = clausesDuModele;
        this.documents = documents;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Cycles de la trousse, le plus récent d'abord. */
    @Transactional(readOnly = true)
    public List<NegociationDto.NegociationVue> lister(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        boolean gestion = Habilitations.peutModifier(t, acteur);
        return registre.deLaTrousse(t).stream().map(n -> vues.interne(n, VuesNegociation.actions(n, gestion))).toList();
    }

    @Transactional(readOnly = true)
    public NegociationDto.ComparaisonVue comparer(Long id, int de, int a, Habilitations.Acteur acteur) {
        Negociation n = registre.exiger(id);
        lecture.exigerVisible(n.getTrousseId(), acteur);
        return vues.comparer(n, de, a);
    }

    /** Décisions du tour, toutes à la fois : la version suivante en découle ; l'entente si tout est accepté tel quel. */
    @Transactional
    public NegociationDto.NegociationVue decider(Long id, NegociationDto.DecisionsRequest req, Habilitations.Acteur acteur) {
        Negociation n = registre.exiger(id);
        Trousse t = lecture.exigerGestion(n.getTrousseId(), acteur);
        List<PropositionNegociation> tour = registre.duTour(n);
        arbitrer(t, tour, req == null || req.decisions() == null ? List.of() : req.decisions(), acteur);
        List<ClauseNegociee> clauses = TourNegociation.appliquer(registre.courantes(n), tour);
        boolean entente = TourNegociation.toutAccepte(tour);
        int numeroTour = n.getTour();
        n.conclureTour(entente);
        registre.enregistrer(n);
        registre.figer(t, n, clauses, "Réponse au tour " + numeroTour, acteur.id());
        journal.consigner(t, entente ? "NEGOCIATION_ENTENTE" : "NEGOCIATION_REPONSE", acteur.id(), "v" + n.getVersionCourante());
        return vues.interne(n, VuesNegociation.actions(n, true));
    }

    private void arbitrer(Trousse t, List<PropositionNegociation> tour, List<NegociationDto.DecisionRequest> decisions,
                          Habilitations.Acteur acteur) {
        Map<Long, PropositionNegociation> parId = tour.stream().collect(Collectors.toMap(PropositionNegociation::getId, Function.identity()));
        for (NegociationDto.DecisionRequest d : decisions) {
            PropositionNegociation p = parId.get(d.propositionId());
            if (p == null) {
                throw new NoSuchElementException("Proposition introuvable dans le tour en cours : " + d.propositionId());
            }
            p.decider(new PropositionNegociation.Arbitrage(PropositionNegociation.Decision.de(d.decision()), d.texte(), d.justification(),
                    acteur.id()), ctx.maintenant());
            registre.enregistrer(p);
            journal.consigner(t, "NEGOCIATION_DECISION", acteur.id(), p.getTitreClause() + " · " + p.getDecision()
                    + (p.getJustification() == null ? "" : " · " + p.getJustification()));
        }
    }

    /** Clôture sur entente : la version négociée devient le contrat de la trousse, que le parcours portera. */
    @Transactional
    public NegociationDto.NegociationVue clore(Long id, Habilitations.Acteur acteur) {
        Negociation n = registre.exiger(id);
        Trousse t = lecture.exigerGestion(n.getTrousseId(), acteur);
        n.clore(acteur.id(), ctx.maintenant());
        registre.enregistrer(n);
        String texte = clausesDuModele.contrat(t, registre.courantes(n));
        ClmDto.DocumentVue doc = documents.deposerVersionNegociee(t, texte, "Version négociée v" + n.getVersionCourante() + " avec "
                + n.getContrepartieNom(), acteur.id());
        journal.consigner(t, "NEGOCIATION_CLOSE", acteur.id(), "v" + n.getVersionCourante() + " → " + doc.nom() + " v" + doc.version());
        return vues.interne(n, VuesNegociation.actions(n, true));
    }

    @Transactional
    public NegociationDto.NegociationVue annuler(Long id, NegociationDto.AnnulationRequest req, Habilitations.Acteur acteur) {
        Negociation n = registre.exiger(id);
        Trousse t = lecture.exigerGestion(n.getTrousseId(), acteur);
        n.annuler(acteur.id(), req == null ? null : req.motif(), ctx.maintenant());
        registre.enregistrer(n);
        journal.consigner(t, "NEGOCIATION_ANNULEE", acteur.id(), n.getMotifFin());
        return vues.interne(n, VuesNegociation.actions(n, true));
    }
}
