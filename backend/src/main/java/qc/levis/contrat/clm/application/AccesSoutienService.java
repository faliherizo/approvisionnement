package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.IdentiteOrganisation;
import qc.levis.contrat.clm.application.repository.AccesSoutienRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.AccesSoutien;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Accès de soutien de l'exploitant (Annexe 10, SEC-17 ; ADR-031). Le superadministrateur n'a aucun droit sur les données
 * d'une organisation ; pour diagnostiquer un incident, il demande un accès motivé et limité dans le temps, que
 * l'administration d'affaires de l'organisation accorde ou refuse. Pendant l'accès, il ne lit qu'un diagnostic (états,
 * étapes, types d'événements, intégrité), jamais le contenu ; chaque demande, décision et consultation est inscrite au
 * journal d'audit de l'organisation, et chaque consultation au journal scellé de la trousse.
 */
@Service
public class AccesSoutienService {

    private final AccesSoutienRepository acces;
    private final TrousseRepository trousses;
    private final EtapeTrousseRepository etapes;
    private final EvenementTrousseRepository evenements;
    private final IdentiteOrganisation organisations;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public AccesSoutienService(AccesSoutienRepository acces, TrousseRepository trousses, EtapeTrousseRepository etapes,
                               EvenementTrousseRepository evenements, IdentiteOrganisation organisations, JournalClm journal, ContexteClm ctx) {
        this.acces = acces;
        this.trousses = trousses;
        this.etapes = etapes;
        this.evenements = evenements;
        this.organisations = organisations;
        this.journal = journal;
        this.ctx = ctx;
    }

    // --- Exploitant (superadministrateur), hors organisation ------------------------------------------------------------

    @Transactional
    public ClmDto.AccesSoutienVue demander(Long tenantId, ClmDto.DemandeSoutien req, String exploitant) {
        exigerOrganisation(tenantId);
        int duree = req.dureeHeures() == null ? 2 : req.dureeHeures();
        return ctx.dansTenant(tenantId, () -> {
            AccesSoutien a = acces.save(AccesSoutien.demander(tenantId, exploitant, req.motif(), duree, ctx.maintenant()));
            journal.soutien(exploitant, a.getId(), "demande · " + duree + " h · " + a.getMotif());
            return vue(a, ctx.maintenant());
        });
    }

    public List<ClmDto.AccesSoutienVue> mesAcces(Long tenantId, String exploitant) {
        exigerOrganisation(tenantId);
        Instant maintenant = ctx.maintenant();
        return acces.findByTenantIdAndDemandeurIgnoreCaseOrderByDemandeLeDesc(tenantId, exploitant).stream().map(a -> vue(a, maintenant)).toList();
    }

    /** Sans accès accordé et en cours, refus ; le refus est lui aussi inscrit au journal de l'organisation. */
    @Transactional(noRollbackFor = AccessDeniedException.class)
    public ClmDto.DiagnosticSoutien diagnostic(Long tenantId, String reference, String exploitant) {
        exigerOrganisation(tenantId);
        return ctx.dansTenant(tenantId, () -> {
            Instant maintenant = ctx.maintenant();
            AccesSoutien a = acces.findByTenantIdAndDemandeurIgnoreCaseOrderByDemandeLeDesc(tenantId, exploitant).stream()
                    .filter(x -> x.actif(maintenant)).findFirst().orElse(null);
            if (a == null) {
                journal.soutien(exploitant, null, "consultation refusée · " + reference + " · aucun accès accordé en cours");
                throw new AccessDeniedException("Aucun accès de soutien accordé par l'organisation, ou accès expiré.");
            }
            Trousse t = trousses.findByTenantIdAndReference(tenantId, reference)
                    .orElseThrow(() -> new NoSuchElementException("Trousse introuvable : " + reference));
            a.consulter();
            acces.save(a);
            journal.consigner(t, "CONSULTATION_SOUTIEN", exploitant, "accès de soutien n° " + a.getId() + " · " + a.getMotif());
            journal.soutien(exploitant, a.getId(), "consultation · " + reference);
            List<EvenementTrousse> evts = evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(tenantId, t.getId());
            return new ClmDto.DiagnosticSoutien(t.getReference(), t.getStatut().name(), t.getMajLe(),
                    etapes.findByTenantIdAndTrousseIdOrderByPhaseAscIdAsc(tenantId, t.getId()).stream()
                            .map(e -> new ClmDto.EtapeDiagnostic(e.getCle(), e.getLibelle(), e.getType().name(), e.getStatut().name(), e.getActiveeLe()))
                            .toList(),
                    evts.stream().map(e -> new ClmDto.EvenementDiagnostic(e.getSequence(), e.getType(), e.getHorodatage())).toList(),
                    ChaineScellee.verifier(t.getId(), evts.stream().map(EvenementTrousse::maillon).toList()).integre());
        });
    }

    private void exigerOrganisation(Long tenantId) {
        if (tenantId == null || organisations.de(tenantId).nom() == null) {
            throw new NoSuchElementException("Organisation introuvable.");
        }
    }

    // --- Organisation (administration d'affaires) ---------------------------------------------------------------------

    public List<ClmDto.AccesSoutienVue> lister() {
        Instant maintenant = ctx.maintenant();
        return acces.findTop100ByTenantIdOrderByDemandeLeDesc(ctx.tenant()).stream().map(a -> vue(a, maintenant)).toList();
    }

    @Transactional
    public ClmDto.AccesSoutienVue accorder(Long id, String acteur) {
        AccesSoutien a = exiger(id);
        a.accorder(acteur, ctx.maintenant());
        journal.soutien(acteur, id, "accordé à " + a.getDemandeur() + " jusqu'au " + a.getExpireLe());
        return vue(acces.save(a), ctx.maintenant());
    }

    @Transactional
    public ClmDto.AccesSoutienVue refuser(Long id, String acteur) {
        AccesSoutien a = exiger(id);
        a.refuser(acteur, ctx.maintenant());
        journal.soutien(acteur, id, "refusé à " + a.getDemandeur());
        return vue(acces.save(a), ctx.maintenant());
    }

    @Transactional
    public ClmDto.AccesSoutienVue revoquer(Long id, String acteur) {
        AccesSoutien a = exiger(id);
        a.revoquer(acteur, ctx.maintenant());
        journal.soutien(acteur, id, "révoqué pour " + a.getDemandeur());
        return vue(acces.save(a), ctx.maintenant());
    }

    private AccesSoutien exiger(Long id) {
        return acces.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Accès de soutien introuvable."));
    }

    private static ClmDto.AccesSoutienVue vue(AccesSoutien a, Instant maintenant) {
        return new ClmDto.AccesSoutienVue(a.getId(), a.getDemandeur(), a.getMotif(), a.getDureeHeures(), a.statut(maintenant).name(),
                a.getDemandeLe(), a.getDecidePar(), a.getDecideLe(), a.getExpireLe(), a.getConsultations());
    }
}
