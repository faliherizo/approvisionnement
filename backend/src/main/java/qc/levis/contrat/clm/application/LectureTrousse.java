package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DecisionEtape;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.Trousse;

/** Chargement d'une trousse sous contrôle d'accès, et construction des vues partagées entre cas d'utilisation. */
@Component
public class LectureTrousse {

    private final TrousseRepository trousses;
    private final ParticipantRepository participants;
    private final EtapeTrousseRepository etapes;
    private final DecisionEtapeRepository decisions;
    private final ContexteClm ctx;

    public LectureTrousse(TrousseRepository trousses, ParticipantRepository participants, EtapeTrousseRepository etapes,
                          DecisionEtapeRepository decisions, ContexteClm ctx) {
        this.trousses = trousses;
        this.participants = participants;
        this.etapes = etapes;
        this.decisions = decisions;
        this.ctx = ctx;
    }

    public Trousse exiger(Long id) {
        return trousses.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Trousse introuvable : " + id));
    }

    /** Une trousse hors périmètre est annoncée introuvable : son existence même n'est pas divulguée. */
    public Trousse exigerVisible(Long id, Habilitations.Acteur acteur) {
        Trousse t = exiger(id);
        if (!visible(t, acteur)) {
            throw new NoSuchElementException("Trousse introuvable : " + id);
        }
        return t;
    }

    public Trousse exigerGestion(Long id, Habilitations.Acteur acteur) {
        Trousse t = exigerVisible(id, acteur);
        if (!Habilitations.peutModifier(t, acteur)) {
            throw new org.springframework.security.access.AccessDeniedException("Action réservée à l'initiateur ou à l'administration.");
        }
        return t;
    }

    /** Commenter ou analyser exige d'intervenir sur la trousse : la consultation d'audit seule ne le permet pas. */
    public Trousse exigerContribution(Long id, Habilitations.Acteur acteur) {
        Trousse t = exigerVisible(id, acteur);
        if (!Habilitations.peutVoir(t, acteur, affectations(t), courriels(t))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Profil de consultation : la trousse se consulte sans intervention (auditeur).");
        }
        return t;
    }

    public boolean visible(Trousse t, Habilitations.Acteur acteur) {
        if (Habilitations.peutModifier(t, acteur)) {
            return true;
        }
        return Habilitations.peutConsulter(t, acteur, affectations(t), courriels(t));
    }

    private List<Parcours.Affectation> affectations(Trousse t) {
        List<Parcours.Affectation> toutes = new ArrayList<>();
        for (EtapeTrousse e : etapes.findByTenantIdAndTrousseIdOrderByPhaseAscIdAsc(t.getTenantId(), t.getId())) {
            toutes.addAll(ctx.affectations(e.getAffectationsJson()));
        }
        return toutes;
    }

    public Map<Long, String> courriels(Trousse t) {
        Map<Long, String> res = new HashMap<>();
        for (Participant p : participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            res.put(p.getId(), p.getCourriel());
        }
        return res;
    }

    public ClmDto.TrousseResume resume(Trousse t, List<EtapeTrousse> lesEtapes, Instant maintenant) {
        List<EtapeTrousse> actives = lesEtapes.stream().filter(e -> e.getStatut() == Parcours.StatutEtape.ACTIVE).toList();
        String prochaine = switch (t.getStatut()) {
            case BROUILLON -> "Compléter et lancer";
            case EN_TRAITEMENT -> actives.isEmpty() ? "—" : String.join(", ", actives.stream().map(EtapeTrousse::getLibelle).toList());
            case REJETEE -> "Reprendre ou annuler";
            default -> "—";
        };
        boolean retard = actives.stream().anyMatch(e -> Relances.enRetard(e.getEcheance(), maintenant))
                || (t.getEcheance() != null && !t.getStatut().estFinal() && t.getEcheance().isBefore(java.time.LocalDate.ofInstant(maintenant, java.time.ZoneOffset.UTC)));
        return new ClmDto.TrousseResume(t.getId(), t.getReference(), t.getTitre(), t.getStatut().name(), t.getSource().name(),
                t.getSfDossierId(), t.getUnite(), t.getProprietaire(), t.getEcheance(), t.getTypeContrat(), t.getMajLe(),
                prochaine, retard);
    }

    public ClmDto.EtapeVue etape(EtapeTrousse e, Instant maintenant) {
        List<ClmDto.DecisionVue> ds = new ArrayList<>();
        for (DecisionEtape d : decisions.findByTenantIdAndEtapeIdOrderByDecideLeAsc(e.getTenantId(), e.getId())) {
            ds.add(new ClmDto.DecisionVue(d.getActeur(), d.getNature().name(), d.getCommentaire(), d.getDecideLe()));
        }
        List<ClmDto.AffectationDto> affs = ctx.affectations(e.getAffectationsJson()).stream()
                .map(a -> new ClmDto.AffectationDto(a.type().name(), a.valeur())).toList();
        boolean retard = e.getStatut() == Parcours.StatutEtape.ACTIVE && Relances.enRetard(e.getEcheance(), maintenant);
        return new ClmDto.EtapeVue(e.getId(), e.getCle(), e.getLibelle(), e.getPhase(), e.getType().name(), affs, e.getQuorum(),
                e.getPolitique().name(), e.getDelaiJours(), ctx.conditions(e.getConditionsJson()), e.getStatut().name(),
                e.getActiveeLe(), e.getEcheance(), retard, ds);
    }

    public static ClmDto.DocumentVue document(DocumentTrousse d) {
        return new ClmDto.DocumentVue(d.getId(), d.getCleDocument(), d.getVersion(), d.getNom(), d.getType().name(), d.getTaille(),
                d.getEmpreinte(), d.getStatut().name(), d.getAnalyse().name(), d.getAuteur(), d.getMotif(), d.getCreeLe(),
                d.sensibilite() == null ? null : d.sensibilite().nom(), d.retention() == null ? null : d.retention().nom(), d.getPages());
    }

    public static ClmDto.ParticipantVue participant(Participant p) {
        return new ClmDto.ParticipantVue(p.getId(), p.getNom(), p.getCourriel(), p.getTelephone(), p.getRoleContrat(),
                p.isExterne(), p.getNiveauIdentite().name(), p.getReponseIdentiteHash() != null, p.getLangue().name(), p.getQualite(),
                p.getEntreprise(), p.isAutoriteRequise(), p.getNature().name());
    }

    public static ClmDto.EvenementVue evenement(EvenementTrousse e) {
        return new ClmDto.EvenementVue(e.getSequence(), e.getType(), e.getActeur(), e.getHorodatage(), e.getDetail(), e.getEmpreinte());
    }
}
