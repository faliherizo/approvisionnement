package qc.levis.contrat.rh.application;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rh.application.dto.EmployeRequest;
import qc.levis.contrat.rh.application.dto.EmployeVue;
import qc.levis.contrat.rh.application.repository.EmployeRepository;
import qc.levis.contrat.rh.domain.Employe;

/** Dossiers employés (RH). Tenant-scopé. Le NAS est masqué à la lecture (Loi 25). */
@Service
public class RhService {

    private final EmployeRepository repo;

    public RhService(EmployeRepository repo) {
        this.repo = repo;
    }

    public List<EmployeVue> lister() {
        List<EmployeVue> res = new ArrayList<>();
        for (Employe e : repo.findByTenantIdOrderByNomAscPrenomAsc(tenant())) res.add(vue(e));
        return res;
    }

    public EmployeVue obtenir(Long id) {
        return vue(exiger(id));
    }

    @Transactional
    public EmployeVue creer(EmployeRequest r) {
        Employe e = new Employe();
        e.setTenantId(tenant());
        appliquer(e, r);
        Employe saved = repo.save(e);
        saved.setMatricule("EMP-" + Year.now() + "-" + String.format("%05d", saved.getId()));
        return vue(repo.save(saved));
    }

    @Transactional
    public EmployeVue modifier(Long id, EmployeRequest r) {
        Employe e = exiger(id);
        appliquer(e, r);
        return vue(repo.save(e));
    }

    @Transactional
    public EmployeVue archiver(Long id, boolean actif) {
        Employe e = exiger(id);
        e.setActif(actif);
        e.setStatut(actif ? "ACTIF" : "INACTIF");
        return vue(repo.save(e));
    }

    private void appliquer(Employe e, EmployeRequest r) {
        e.setPrenom(r.prenom());
        e.setNom(r.nom());
        if (r.nas() != null && !r.nas().isBlank()) e.setNas(r.nas()); // conservé seulement si fourni
        e.setCourriel(r.courriel());
        e.setTelephone(r.telephone());
        e.setDateEmbauche(r.dateEmbauche());
        e.setDateFin(r.dateFin());
        if (r.statut() != null) e.setStatut(r.statut());
        e.setPoste(r.poste());
        e.setDepartement(r.departement());
        e.setSalaireAnnuel(r.salaireAnnuel());
        if (r.frequence() != null) e.setFrequence(r.frequence());
        if (r.province() != null) e.setProvince(r.province());
        if (r.typeTravailleur() != null) e.setTypeTravailleur(r.typeTravailleur());
    }

    private String masquer(String nas) {
        if (nas == null || nas.isBlank()) return null;
        String net = nas.replaceAll("\\s|-", "");
        return "***-***-" + net.substring(Math.max(0, net.length() - 3));
    }

    private Employe exiger(Long id) {
        return repo.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Employé introuvable : " + id));
    }

    private EmployeVue vue(Employe e) {
        return new EmployeVue(e.getId(), e.getMatricule(), e.getPrenom(), e.getNom(), masquer(e.getNas()),
                e.getCourriel(), e.getTelephone(), e.getDateEmbauche(), e.getDateFin(), e.getStatut(),
                e.getPoste(), e.getDepartement(), e.getSalaireAnnuel(), e.getFrequence(), e.getProvince(),
                e.getTypeTravailleur(), e.isActif());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
