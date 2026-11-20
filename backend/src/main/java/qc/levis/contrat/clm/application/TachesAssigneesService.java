package qc.levis.contrat.clm.application;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.repository.TacheAssigneeRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.TacheAssignee;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Tâches confiées à la main à une personne de l'organisation (boîte de tâches, UI-12). L'assigné est choisi dans
 * l'annuaire de l'organisation, la priorité dans sa liste de référence ; il est prévenu par le modèle de message
 * « Action requise » que l'organisation configure (UI-17). Une tâche rattachée à une trousse laisse une trace dans le
 * journal scellé de celle-ci.
 */
@Service
public class TachesAssigneesService {

    public enum Filtre { ASSIGNEES, CREEES, TERMINEES }

    public enum Action { DEMARRER, TERMINER, ROUVRIR, ANNULER }

    private final TacheAssigneeRepository taches;
    private final TrousseRepository trousses;
    private final LectureTrousse lecture;
    private final ListesClm listes;
    private final AnnuaireOrganisation annuaire;
    private final MessagerieClm messagerie;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public TachesAssigneesService(TacheAssigneeRepository taches, TrousseRepository trousses, LectureTrousse lecture,
                                  ListesClm listes, AnnuaireOrganisation annuaire, MessagerieClm messagerie,
                                  JournalClm journal, ContexteClm ctx) {
        this.taches = taches;
        this.trousses = trousses;
        this.lecture = lecture;
        this.listes = listes;
        this.annuaire = annuaire;
        this.messagerie = messagerie;
        this.journal = journal;
        this.ctx = ctx;
    }

    public List<ClmDto.MembreVue> membres() {
        return annuaire.membres(ctx.tenant()).stream().map(m -> new ClmDto.MembreVue(m.courriel(), m.nom())).toList();
    }

    public List<ClmDto.TacheAssigneeVue> lister(Habilitations.Acteur acteur, String filtre) {
        Long tenant = ctx.tenant();
        List<TacheAssignee> trouvees = switch (filtre(filtre)) {
            case ASSIGNEES -> taches.findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(tenant, acteur.id(),
                    EnumSet.of(TacheAssignee.Statut.A_FAIRE, TacheAssignee.Statut.EN_COURS));
            case CREEES -> taches.findByTenantIdAndCreeParIgnoreCaseOrderByIdDesc(tenant, acteur.id());
            case TERMINEES -> taches.findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(tenant, acteur.id(),
                    EnumSet.of(TacheAssignee.Statut.TERMINEE));
        };
        return vues(trouvees, acteur);
    }

    /** Tâches d'une trousse : visibles de quiconque voit la trousse, comme son historique. */
    public List<ClmDto.TacheAssigneeVue> deLaTrousse(Long trousseId, Habilitations.Acteur acteur) {
        lecture.exigerVisible(trousseId, acteur);
        return vues(taches.findByTenantIdAndTrousseIdOrderByIdAsc(ctx.tenant(), trousseId), acteur);
    }

    @Transactional
    public ClmDto.TacheAssigneeVue creer(Habilitations.Acteur acteur, ClmDto.TacheAssigneeRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Tâche à créer manquante.");
        }
        Long tenant = ctx.tenant();
        Trousse trousse = req.trousseId() == null ? null : lecture.exigerContribution(req.trousseId(), acteur);
        exigerMembre(tenant, req.assigneA());
        String priorite = listes.exiger(ListesClm.Liste.PRIORITE_TACHE, req.priorite());
        TacheAssignee t = taches.save(TacheAssignee.creer(tenant, new TacheAssignee.Saisie(req.trousseId(), req.titre(),
                req.description(), req.assigneA(), priorite, req.echeance()), acteur.id(), ctx.maintenant()));
        tracer(trousse, "TACHE_CREEE", acteur, t.getTitre() + " → " + t.getAssigneA());
        prevenir(t, trousse, acteur);
        return vue(t, trousse, acteur);
    }

    @Transactional
    public ClmDto.TacheAssigneeVue agir(Long id, Habilitations.Acteur acteur, ClmDto.ActionTacheRequest req) {
        TacheAssignee t = exigerIntervenant(id, acteur);
        Action action = action(req);
        switch (action) {
            case DEMARRER -> {
                exiger(t.peutDemarrer(acteur.id()), "Seule la personne assignée démarre la tâche.");
                t.demarrer(ctx.maintenant());
            }
            case TERMINER -> t.terminer(ctx.maintenant());
            case ROUVRIR -> t.rouvrir(ctx.maintenant());
            case ANNULER -> {
                exiger(t.peutAnnuler(acteur.id()), "Seul le créateur annule la tâche.");
                t.annuler(ctx.maintenant());
            }
        }
        taches.save(t);
        Trousse trousse = trousse(t);
        tracer(trousse, "TACHE_" + t.getStatut().name(), acteur, t.getTitre());
        return vue(t, trousse, acteur);
    }

    @Transactional
    public ClmDto.TacheAssigneeVue reassigner(Long id, Habilitations.Acteur acteur, ClmDto.DelegationRequest req) {
        TacheAssignee t = exigerIntervenant(id, acteur);
        String vers = req == null ? null : req.vers();
        exigerMembre(t.getTenantId(), vers);
        Trousse trousse = transferer(t, vers, acteur, req == null ? null : req.motif());
        return vue(t, trousse, acteur);
    }

    /** Réassignation tracée et annoncée à la nouvelle personne ; sert aussi au départ d'un employé (P3). */
    Trousse transferer(TacheAssignee t, String vers, Habilitations.Acteur acteur, String motif) {
        String precedent = t.getAssigneA();
        t.reassigner(vers, ctx.maintenant());
        taches.save(t);
        Trousse trousse = trousse(t);
        String suffixe = motif == null || motif.isBlank() ? "" : " · " + motif.trim();
        tracer(trousse, "TACHE_REASSIGNEE", acteur, t.getTitre() + " : " + precedent + " → " + t.getAssigneA() + suffixe);
        prevenir(t, trousse, acteur);
        return trousse;
    }

    private TacheAssignee exigerIntervenant(Long id, Habilitations.Acteur acteur) {
        TacheAssignee t = taches.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new java.util.NoSuchElementException("Tâche introuvable : " + id));
        exiger(t.concerne(acteur.id()), "Seuls le créateur et la personne assignée agissent sur cette tâche.");
        return t;
    }

    private void exigerMembre(Long tenant, String courriel) {
        if (courriel != null && !courriel.isBlank() && !annuaire.estMembre(tenant, courriel)) {
            throw new IllegalArgumentException("« " + courriel.trim() + " » n'est pas une personne active de l'organisation.");
        }
    }

    private static void exiger(boolean condition, String refus) {
        if (!condition) {
            throw new AccessDeniedException(refus);
        }
    }

    private static Filtre filtre(String saisie) {
        try {
            return saisie == null || saisie.isBlank() ? Filtre.ASSIGNEES : Filtre.valueOf(saisie.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Filtre inconnu : " + saisie);
        }
    }

    private static Action action(ClmDto.ActionTacheRequest req) {
        String saisie = req == null || req.action() == null ? "" : req.action().trim().toUpperCase(java.util.Locale.ROOT);
        try {
            return Action.valueOf(saisie);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Action inconnue : " + saisie);
        }
    }

    private void tracer(Trousse trousse, String type, Habilitations.Acteur acteur, String detail) {
        if (trousse != null) {
            journal.consigner(trousse, type, acteur.id(), detail);
        }
    }

    /** Se confier une tâche à soi-même ne déclenche aucun message. */
    private void prevenir(TacheAssignee t, Trousse trousse, Habilitations.Acteur acteur) {
        if (t.estAssigneeA(acteur.id())) {
            return;
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("reference", trousse == null ? "—" : trousse.getReference());
        vars.put("titre", trousse == null ? t.getTitre() : trousse.getTitre());
        vars.put("etape", t.getTitre());
        vars.put("echeance", t.getEcheance() == null ? "—" : t.getEcheance().toString());
        messagerie.envoyer(t.getTenantId(), Evenement.TACHE, "COURRIEL", t.getAssigneA(), vars, List.of());
    }

    private Trousse trousse(TacheAssignee t) {
        return t.getTrousseId() == null ? null : trousses.findByIdAndTenantId(t.getTrousseId(), t.getTenantId()).orElse(null);
    }

    private List<ClmDto.TacheAssigneeVue> vues(List<TacheAssignee> liste, Habilitations.Acteur acteur) {
        Map<Long, Trousse> parId = new HashMap<>();
        return liste.stream().map(t -> vue(t, t.getTrousseId() == null ? null
                : parId.computeIfAbsent(t.getTrousseId(), x -> trousse(t)), acteur)).toList();
    }

    private ClmDto.TacheAssigneeVue vue(TacheAssignee t, Trousse trousse, Habilitations.Acteur acteur) {
        LocalDate aujourdhui = ctx.aujourdhui();
        return new ClmDto.TacheAssigneeVue(t.getId(), t.getTrousseId(), trousse == null ? null : trousse.getReference(),
                t.getTitre(), t.getDescription(), t.getAssigneA(), t.getPriorite(), t.getEcheance(), t.enRetard(aujourdhui),
                t.getStatut().name(), t.getCreePar(), t.getCreeLe(), t.getTermineeLe(),
                t.peutDemarrer(acteur.id()) && t.getStatut() == TacheAssignee.Statut.A_FAIRE,
                t.peutAnnuler(acteur.id()) && t.estOuverte());
    }
}
