package qc.levis.contrat.clm.application;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.AnalyseIaRepository;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.RegleConservationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.RegleConservation;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Conservation et destruction au moment opportun (Cahier § 1.4.11 et § 1.4.12 ; Loi sur les archives). Le calendrier de
 * l'organisation fixe, par type de contrat, la durée après clôture et le sort final. La destruction n'est jamais
 * automatique : l'administrateur la confirme trousse par trousse, et un gel légal la suspend.
 */
@Service
public class ConservationService {

    private static final EnumSet<StatutTrousse> CLOSES = EnumSet.of(StatutTrousse.TERMINEE, StatutTrousse.ANNULEE);

    private final RegleConservationRepository regles;
    private final TrousseRepository trousses;
    private final DocumentTrousseRepository documents;
    private final ParticipantRepository participants;
    private final InvitationRepository invitations;
    private final DecisionEtapeRepository decisions;
    private final AnalyseIaRepository analyses;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ConservationService(RegleConservationRepository regles, TrousseRepository trousses, DocumentTrousseRepository documents,
                               ParticipantRepository participants, InvitationRepository invitations, DecisionEtapeRepository decisions,
                               AnalyseIaRepository analyses, JournalClm journal, ContexteClm ctx) {
        this.regles = regles;
        this.trousses = trousses;
        this.documents = documents;
        this.participants = participants;
        this.invitations = invitations;
        this.decisions = decisions;
        this.analyses = analyses;
        this.journal = journal;
        this.ctx = ctx;
    }

    // --- Calendrier ---

    public List<ClmDto.RegleConservationVue> regles() {
        return regles.findByTenantIdOrderByTypeContratAsc(ctx.tenant()).stream().map(ConservationService::vue).toList();
    }

    /** Crée ou remplace la règle d'un type de contrat (ou la règle par défaut, sans type). */
    @Transactional
    public ClmDto.RegleConservationVue definir(ClmDto.RegleConservationRequest req, String acteur) {
        Long tenant = ctx.tenant();
        String type = req.typeContrat() == null || req.typeContrat().isBlank() ? null : req.typeContrat().strip();
        RegleConservation r = regleDe(tenant, type).orElseGet(() -> new RegleConservation(tenant, type));
        r.definir(req.dureeAns() == null ? 0 : req.dureeAns(), sortFinal(req.sortFinal()), acteur, ctx.maintenant());
        r = regles.save(r);
        journal.configuration(acteur, "CONSERVATION", (type == null ? "défaut" : type) + " : " + r.getDureeAns() + " an(s), " + r.getSortFinal());
        return vue(r);
    }

    @Transactional
    public void supprimer(Long id, String acteur) {
        RegleConservation r = regles.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Règle introuvable."));
        regles.delete(r);
        journal.configuration(acteur, "CONSERVATION", "règle retirée : " + (r.getTypeContrat() == null ? "défaut" : r.getTypeContrat()));
    }

    // --- Suivi des trousses closes ---

    /** Trousses closes, échues d'abord ; {@code echuesSeulement} restreint aux sorts finaux à appliquer. */
    public List<ClmDto.ConservationVue> suivi(boolean echuesSeulement) {
        Long tenant = ctx.tenant();
        LocalDate aujourdhui = ctx.aujourdhui();
        return trousses.findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(tenant, CLOSES).stream()
                .map(t -> vue(t, regleApplicable(t), aujourdhui))
                .filter(v -> !echuesSeulement || v.echue())
                .toList();
    }

    @Transactional
    public ClmDto.ConservationVue geler(Long trousseId, String motif, String acteur) {
        Trousse t = exiger(trousseId);
        t.geler(motif);
        trousses.save(t);
        journal.consigner(t, "GEL_LEGAL", acteur, motif.strip());
        return vue(t, regleApplicable(t), ctx.aujourdhui());
    }

    @Transactional
    public ClmDto.ConservationVue leverGel(Long trousseId, String acteur) {
        Trousse t = exiger(trousseId);
        t.leverGel();
        trousses.save(t);
        journal.consigner(t, "GEL_LEVE", acteur, null);
        return vue(t, regleApplicable(t), ctx.aujourdhui());
    }

    /** Sort final « verser » : le dossier de preuve a été exporté et remis aux archives ; la destruction devient permise. */
    @Transactional
    public ClmDto.ConservationVue marquerVersee(Long trousseId, String acteur) {
        Trousse t = exiger(trousseId);
        RegleConservation r = exigerEchue(t);
        if (r.getSortFinal() != RegleConservation.SortFinal.VERSER) {
            throw new IllegalStateException("Le sort final de cette trousse n'est pas le versement.");
        }
        t.marquerVersee(ctx.maintenant());
        trousses.save(t);
        journal.consigner(t, "TROUSSE_VERSEE", acteur, "règle " + libelle(r));
        return vue(t, Optional.of(r), ctx.aujourdhui());
    }

    /**
     * Destruction confirmée : contenus des documents, coordonnées des participants, données de signature, commentaires et
     * analyses sont effacés. Le journal scellé reçoit le bordereau (règle, échéance, empreintes) et reste intact.
     */
    @Transactional
    public void detruire(Long trousseId, String acteur) {
        Trousse t = exiger(trousseId);
        RegleConservation r = exigerEchue(t);
        if (r.getSortFinal() == RegleConservation.SortFinal.VERSER && t.getVerseeLe() == null) {
            throw new IllegalStateException("Versement aux archives à confirmer avant la destruction.");
        }
        List<DocumentTrousse> docs = documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId());
        String empreintes = String.join(", ", docs.stream().map(d -> d.getNom() + " v" + d.getVersion() + " " + d.getEmpreinte().substring(0, 16)).toList());
        docs.forEach(d -> {
            d.effacerContenu();
            documents.save(d);
        });
        participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(p -> {
            p.anonymiser();
            participants.save(p);
        });
        invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(i -> {
            i.anonymiser();
            invitations.save(i);
        });
        decisions.findByTenantIdAndTrousseIdOrderByDecideLeAsc(t.getTenantId(), t.getId()).forEach(d -> {
            d.effacerCommentaire();
            decisions.save(d);
        });
        analyses.deleteAll(analyses.findByTenantIdAndTrousseIdOrderByCreeLeDesc(t.getTenantId(), t.getId()));
        LocalDate echeance = r.echeance(t.getClotureLe(), ctx.fuseau());
        t.detruire(ctx.maintenant());
        trousses.save(t);
        journal.consigner(t, "TROUSSE_DETRUITE", acteur, "règle " + libelle(r) + " · échéance " + echeance + " · " + docs.size()
                + " version(s) : " + empreintes);
    }

    private RegleConservation exigerEchue(Trousse t) {
        RegleConservation r = regleApplicable(t).orElseThrow(() ->
                new IllegalStateException("Aucune règle de conservation ne s'applique à ce type de contrat."));
        LocalDate echeance = r.echeance(t.getClotureLe(), ctx.fuseau());
        if (echeance == null || ctx.aujourdhui().isBefore(echeance)) {
            throw new IllegalStateException("Échéance de conservation non atteinte" + (echeance == null ? " (conservation permanente)." : " : " + echeance + "."));
        }
        return r;
    }

    private Optional<RegleConservation> regleApplicable(Trousse t) {
        Optional<RegleConservation> propre = t.getTypeContrat() == null ? Optional.empty() : regleDe(t.getTenantId(), t.getTypeContrat());
        return propre.isPresent() ? propre : regleDe(t.getTenantId(), null);
    }

    private Optional<RegleConservation> regleDe(Long tenant, String type) {
        return regles.findByTenantIdOrderByTypeContratAsc(tenant).stream()
                .filter(r -> type == null ? r.getTypeContrat() == null : type.equals(r.getTypeContrat())).findFirst();
    }

    private Trousse exiger(Long id) {
        return trousses.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Trousse introuvable."));
    }

    private ClmDto.ConservationVue vue(Trousse t, Optional<RegleConservation> regle, LocalDate aujourdhui) {
        LocalDate echeance = regle.map(r -> r.echeance(t.getClotureLe(), ctx.fuseau())).orElse(null);
        boolean echue = echeance != null && !aujourdhui.isBefore(echeance) && !t.isGelLegal();
        return new ClmDto.ConservationVue(t.getId(), t.getReference(), t.getTitre(), t.getTypeContrat(), t.getStatut().name(),
                t.getClotureLe(), echeance, regle.map(r -> r.getSortFinal().name()).orElse(null), echue, t.isGelLegal(),
                t.getGelMotif(), t.getVerseeLe());
    }

    private static String libelle(RegleConservation r) {
        return (r.getTypeContrat() == null ? "défaut" : r.getTypeContrat()) + " " + r.getDureeAns() + " an(s) " + r.getSortFinal();
    }

    private static RegleConservation.SortFinal sortFinal(String saisi) {
        try {
            return RegleConservation.SortFinal.valueOf(saisi == null ? "" : saisi.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Sort final : DETRUIRE, VERSER ou CONSERVER.", e);
        }
    }

    private static ClmDto.RegleConservationVue vue(RegleConservation r) {
        return new ClmDto.RegleConservationVue(r.getId(), r.getTypeContrat(), r.getDureeAns(), r.getSortFinal().name(),
                r.getModifiePar(), r.getModifieLe());
    }
}
