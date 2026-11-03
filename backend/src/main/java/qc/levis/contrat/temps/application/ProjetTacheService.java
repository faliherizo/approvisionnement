package qc.levis.contrat.temps.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rh.application.repository.EmployeRepository;
import qc.levis.contrat.rh.domain.Employe;
import qc.levis.contrat.temps.application.dto.*;
import qc.levis.contrat.temps.application.repository.ProjetRepository;
import qc.levis.contrat.temps.application.repository.TacheProjetRepository;
import qc.levis.contrat.temps.domain.Projet;
import qc.levis.contrat.temps.domain.TacheProjet;

/** Projets et tâches (assignables à une personne). Tenant-scopé. Le temps saisi s'y rattache. */
@Service
public class ProjetTacheService {

    private final ProjetRepository projets;
    private final TacheProjetRepository taches;
    private final EmployeRepository employes;

    public ProjetTacheService(ProjetRepository projets, TacheProjetRepository taches, EmployeRepository employes) {
        this.projets = projets;
        this.taches = taches;
        this.employes = employes;
    }

    // --- Projets ---
    public List<ProjetVue> listerProjets() {
        List<ProjetVue> res = new ArrayList<>();
        for (Projet p : projets.findByTenantIdOrderByNomAsc(tenant())) res.add(vue(p));
        return res;
    }

    @Transactional
    public ProjetVue creerProjet(ProjetRequest r) {
        if (projets.existsByTenantIdAndCode(tenant(), r.code()))
            throw new IllegalArgumentException("Code de projet déjà utilisé : " + r.code());
        return vue(projets.save(new Projet(tenant(), r.code(), r.nom(), r.gestionnaire(), r.budgetHeures())));
    }

    @Transactional
    public ProjetVue modifierProjet(Long id, ProjetRequest r) {
        Projet p = projets.findByIdAndTenantId(id, tenant()).orElseThrow(() -> introuvable("Projet", id));
        p.setNom(r.nom()); p.setGestionnaire(r.gestionnaire()); p.setBudgetHeures(r.budgetHeures());
        return vue(projets.save(p));
    }

    // --- Tâches ---
    public List<TacheProjetVue> listerTaches(Long projetId, Long assigneId) {
        List<TacheProjet> src;
        if (projetId != null) src = taches.findByTenantIdAndProjetIdOrderByCreeLeDesc(tenant(), projetId);
        else if (assigneId != null) src = taches.findByTenantIdAndAssigneIdOrderByCreeLeDesc(tenant(), assigneId);
        else src = taches.findByTenantIdOrderByCreeLeDesc(tenant());
        List<TacheProjetVue> res = new ArrayList<>();
        for (TacheProjet t : src) res.add(vue(t));
        return res;
    }

    /** Tâches assignées à l'employé dont le courriel correspond à l'utilisateur courant. */
    public List<TacheProjetVue> mesTaches(String courriel) {
        Long employeId = employes.findByTenantIdOrderByNomAscPrenomAsc(tenant()).stream()
                .filter(e -> courriel != null && courriel.equalsIgnoreCase(e.getCourriel()))
                .map(Employe::getId).findFirst().orElse(null);
        if (employeId == null) return List.of();
        return listerTaches(null, employeId);
    }

    @Transactional
    public TacheProjetVue creerTache(TacheProjetRequest r) {
        if (r.projetId() == null || projets.findByIdAndTenantId(r.projetId(), tenant()).isEmpty())
            throw new IllegalArgumentException("Projet invalide.");
        return vue(taches.save(new TacheProjet(tenant(), r.projetId(), r.nom(), r.description(), r.typeTache(),
                r.statut(), r.assigneId(), r.heuresEstimees(), r.echeance())));
    }

    @Transactional
    public TacheProjetVue modifierTache(Long id, TacheProjetRequest r) {
        TacheProjet t = taches.findByIdAndTenantId(id, tenant()).orElseThrow(() -> introuvable("Tâche", id));
        if (r.nom() != null) t.setNom(r.nom());
        t.setDescription(r.description());
        t.setTypeTache(r.typeTache());
        if (r.statut() != null) t.setStatut(r.statut());
        t.setAssigneId(r.assigneId());          // assignation / réassignation / désassignation (null)
        t.setHeuresEstimees(r.heuresEstimees());
        t.setEcheance(r.echeance());
        return vue(taches.save(t));
    }

    @Transactional
    public void supprimerTache(Long id) {
        TacheProjet t = taches.findByIdAndTenantId(id, tenant()).orElseThrow(() -> introuvable("Tâche", id));
        taches.delete(t);
    }

    // --- interne ---
    private ProjetVue vue(Projet p) {
        return new ProjetVue(p.getId(), p.getCode(), p.getNom(), p.getGestionnaire(), p.getBudgetHeures(), p.isActif());
    }

    private TacheProjetVue vue(TacheProjet t) {
        String projetNom = projets.findByIdAndTenantId(t.getProjetId(), tenant()).map(Projet::getNom).orElse(null);
        String assigneNom = null;
        if (t.getAssigneId() != null) {
            assigneNom = employes.findByIdAndTenantId(t.getAssigneId(), tenant())
                    .map(e -> e.getPrenom() + " " + e.getNom()).orElse(null);
        }
        return new TacheProjetVue(t.getId(), t.getProjetId(), projetNom, t.getNom(), t.getDescription(),
                t.getTypeTache(), t.getStatut(), t.getAssigneId(), assigneNom, t.getHeuresEstimees(),
                t.getEcheance(), t.getCreeLe());
    }

    private NoSuchElementException introuvable(String quoi, Long id) {
        return new NoSuchElementException(quoi + " introuvable : " + id);
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
