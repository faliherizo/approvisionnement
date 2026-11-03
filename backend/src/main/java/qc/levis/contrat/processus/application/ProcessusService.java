package qc.levis.contrat.processus.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.processus.application.dto.DecisionRequest;
import qc.levis.contrat.processus.application.dto.DecisionVue;
import qc.levis.contrat.processus.application.dto.TacheVue;
import qc.levis.contrat.processus.application.repository.DecisionRepository;
import qc.levis.contrat.processus.application.repository.TacheRepository;
import qc.levis.contrat.processus.application.repository.WorkflowRepository;
import qc.levis.contrat.processus.domain.Decision;
import qc.levis.contrat.processus.domain.EtapeDef;
import qc.levis.contrat.processus.domain.StatutTache;
import qc.levis.contrat.processus.domain.Tache;
import qc.levis.contrat.processus.domain.WorkflowDef;
import qc.levis.contrat.saisie.application.FicheEvent;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.SaisieVue;

/**
 * Orchestration des processus : à la soumission d'une demande, génère la première tâche selon le workflow
 * configuré (routage par montant/condition) ; sur décision (approuver/refuser/retourner), fait avancer le
 * workflow et pilote l'état de la demande. Découplé de la saisie par l'événement {@link FicheEvent}.
 * Gardé par le module « processus » (sinon la demande suit la validation manuelle d'AP-2).
 */
@Service
public class ProcessusService {

    private static final List<StatutTache> ACTIVES = List.of(StatutTache.OUVERTE, StatutTache.EN_COURS);

    private final WorkflowService workflows;
    private final WorkflowRepository workflowRepo;
    private final TacheRepository taches;
    private final DecisionRepository decisions;
    private final ModuleService modules;
    private final SaisieService saisies;
    private final FormulaireRepository formulaires;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public ProcessusService(WorkflowService workflows, WorkflowRepository workflowRepo, TacheRepository taches,
                            DecisionRepository decisions, ModuleService modules, SaisieService saisies,
                            FormulaireRepository formulaires) {
        this.workflows = workflows;
        this.workflowRepo = workflowRepo;
        this.taches = taches;
        this.decisions = decisions;
        this.modules = modules;
        this.saisies = saisies;
        this.formulaires = formulaires;
    }

    /** À la soumission, démarre le workflow (si le module est actif et un workflow est défini). */
    @EventListener
    @Transactional
    public void surFiche(FicheEvent e) {
        if (!"SOUMISSION".equals(e.declencheur())) return;
        if (!modules.estActif(e.tenantId(), "processus")) return;
        WorkflowDef def = workflows.extraire(definition(e.tenantId(), e.formulaireId()));
        List<EtapeDef> applicables = MoteurWorkflow.etapesApplicables(def, e.valeurs());
        if (applicables.isEmpty()) return; // aucun workflow → validation manuelle (AP-2)
        creerTache(e.tenantId(), e.saisieId(), e.formulaireId(), applicables.get(0), 0);
        try { saisies.prendreEnCharge(e.saisieId()); } catch (RuntimeException ignore) { /* déjà en traitement */ }
    }

    /** Tâches ouvertes assignées à l'un des rôles de l'utilisateur courant. */
    public List<TacheVue> mesTaches(List<String> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        List<TacheVue> res = new ArrayList<>();
        for (Tache t : taches.findByTenantIdAndAssigneRoleInAndStatutInOrderByEcheanceLeAsc(tenant(), roles, ACTIVES)) {
            res.add(vue(t));
        }
        return res;
    }

    /** Décision d'un gestionnaire sur une tâche : fait avancer le workflow et l'état de la demande. */
    @Transactional
    public void decider(Long tacheId, List<String> roles, String acteur, DecisionRequest req) {
        Tache t = taches.findByIdAndTenantId(tacheId, tenant())
                .orElseThrow(() -> new NoSuchElementException("Tâche introuvable : " + tacheId));
        if (!ACTIVES.contains(t.getStatut()))
            throw new IllegalStateException("Tâche déjà traitée.");
        if (t.getAssigneRole() != null && (roles == null || !roles.contains(t.getAssigneRole())))
            throw new IllegalStateException("Cette tâche n'est pas assignée à vos rôles.");
        String d = req.decision() == null ? "" : req.decision().toUpperCase();
        if ("REFUSE".equals(d) && t.isCommentaireRefusObligatoire() && (req.commentaire() == null || req.commentaire().isBlank()))
            throw new IllegalArgumentException("Un commentaire est obligatoire en cas de refus.");

        decisions.save(new Decision(tenant(), t.getId(), t.getSaisieId(), acteur, d, req.commentaire()));
        t.setStatut(StatutTache.TERMINEE);
        t.setTermineLe(Instant.now());
        taches.save(t);

        switch (d) {
            case "APPROUVE" -> avancer(t);
            case "REFUSE" -> { saisies.rejeter(t.getSaisieId()); annulerActives(t.getSaisieId()); }
            case "RETOUR" -> { saisies.demanderComplement(t.getSaisieId()); annulerActives(t.getSaisieId()); }
            default -> throw new IllegalArgumentException("Décision inconnue : " + req.decision());
        }
    }

    public List<DecisionVue> historique(Long saisieId) {
        List<DecisionVue> res = new ArrayList<>();
        for (Decision dec : decisions.findByTenantIdAndSaisieIdOrderByDecideLeAsc(tenant(), saisieId)) {
            res.add(new DecisionVue(dec.getTacheId(), dec.getActeur(), dec.getDecision(), dec.getCommentaire(), dec.getDecideLe()));
        }
        return res;
    }

    // --- interne ---

    private void avancer(Tache t) {
        WorkflowDef def = workflows.extraire(definition(t.getTenantId(), t.getFormulaireId()));
        List<EtapeDef> applicables = MoteurWorkflow.etapesApplicables(def, valeursDemande(t.getSaisieId()));
        int idx = indexEtape(applicables, t.getEtapeCode());
        if (idx >= 0 && idx + 1 < applicables.size()) {
            creerTache(t.getTenantId(), t.getSaisieId(), t.getFormulaireId(), applicables.get(idx + 1), t.getOrdre() + 1);
        } else {
            saisies.valider(t.getSaisieId()); // dernière étape approuvée
        }
    }

    private void creerTache(Long tenantId, Long saisieId, Long formulaireId, EtapeDef e, int ordre) {
        Instant echeance = e.delaiJours() != null ? Instant.now().plus(e.delaiJours(), ChronoUnit.DAYS) : null;
        taches.save(new Tache(tenantId, saisieId, formulaireId, e.code(), e.nom(), e.responsableRole(),
                e.responsableGroupe(), e.commentaireRefusObligatoire(), ordre, echeance));
    }

    private void annulerActives(Long saisieId) {
        for (Tache t : taches.findByTenantIdAndSaisieIdOrderByOrdreAsc(tenant(), saisieId)) {
            if (ACTIVES.contains(t.getStatut())) {
                t.setStatut(StatutTache.ANNULEE);
                t.setTermineLe(Instant.now());
                taches.save(t);
            }
        }
    }

    private int indexEtape(List<EtapeDef> etapes, String code) {
        for (int i = 0; i < etapes.size(); i++) if (etapes.get(i).code().equals(code)) return i;
        return -1;
    }

    private String definition(Long tenantId, Long formulaireId) {
        return workflowRepo.findByTenantIdAndFormulaireId(tenantId, formulaireId)
                .map(w -> w.getDefinitionJson()).orElse(null);
    }

    private Map<String, Object> valeursDemande(Long saisieId) {
        SaisieVue s = saisies.obtenir(saisieId);
        if (s.donneesJson() == null || s.donneesJson().isBlank()) return Map.of();
        try { return mapper.readValue(s.donneesJson(), new TypeReference<Map<String, Object>>() {}); }
        catch (Exception e) { return Map.of(); }
    }

    private TacheVue vue(Tache t) {
        SaisieVue s = saisies.obtenir(t.getSaisieId());
        Formulaire f = formulaires.findByIdAndTenantId(t.getFormulaireId(), tenant()).orElse(null);
        return new TacheVue(t.getId(), t.getSaisieId(), s.numero(), t.getFormulaireId(),
                f != null ? f.getNom() : null, f != null ? f.getDomaine() : null,
                t.getEtapeCode(), t.getEtapeNom(), t.getStatut().name(),
                t.isCommentaireRefusObligatoire(), t.getEcheanceLe(), t.getCreeLe());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
