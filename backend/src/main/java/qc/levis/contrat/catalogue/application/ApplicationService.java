package qc.levis.contrat.catalogue.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.catalogue.application.dto.ApplicationRequest;
import qc.levis.contrat.catalogue.application.dto.ApplicationVue;
import qc.levis.contrat.catalogue.application.repository.ApplicationRepository;
import qc.levis.contrat.catalogue.domain.Application;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Gestion du catalogue d'applications (espaces fonctionnels). Tenant-scopé. */
@Service("catalogueApplicationService")
public class ApplicationService {

    private final ApplicationRepository repo;

    public ApplicationService(ApplicationRepository repo) {
        this.repo = repo;
    }

    public List<ApplicationVue> lister(String domaine) {
        List<Application> src = (domaine == null || domaine.isBlank())
                ? repo.findByTenantIdOrderByNomAsc(tenant())
                : repo.findByTenantIdAndDomaineOrderByNomAsc(tenant(), domaine);
        List<ApplicationVue> res = new ArrayList<>();
        for (Application a : src) res.add(vue(a));
        return res;
    }

    @Transactional
    public ApplicationVue creer(ApplicationRequest req) {
        if (repo.existsByTenantIdAndCode(tenant(), req.code()))
            throw new IllegalArgumentException("Code d'application déjà utilisé : " + req.code());
        Application a = repo.save(new Application(tenant(), req.code(), req.nom(), req.domaine(),
                env(req.environnement()), statut(req.statut()), req.proprietaire(), req.description()));
        return vue(a);
    }

    @Transactional
    public ApplicationVue modifier(Long id, ApplicationRequest req) {
        Application a = exiger(id);
        a.setNom(req.nom());
        a.setDomaine(req.domaine());
        a.setEnvironnement(env(req.environnement()));
        a.setStatut(statut(req.statut()));
        a.setProprietaire(req.proprietaire());
        a.setDescription(req.description());
        return vue(repo.save(a));
    }

    @Transactional
    public ApplicationVue archiver(Long id, boolean actif) {
        Application a = exiger(id);
        a.setActif(actif);
        return vue(repo.save(a));
    }

    @Transactional
    public ApplicationVue dupliquer(Long id, String nouveauCode, String nouveauNom) {
        Application src = exiger(id);
        if (repo.existsByTenantIdAndCode(tenant(), nouveauCode))
            throw new IllegalArgumentException("Code déjà utilisé : " + nouveauCode);
        Application copie = repo.save(new Application(tenant(), nouveauCode, nouveauNom, src.getDomaine(),
                src.getEnvironnement(), "BROUILLON", src.getProprietaire(), src.getDescription()));
        return vue(copie);
    }

    private String env(String e) {
        return List.of("DEV", "UAT", "PROD").contains(e) ? e : "DEV";
    }

    private String statut(String s) {
        return List.of("BROUILLON", "TEST", "ACTIF").contains(s) ? s : "BROUILLON";
    }

    private Application exiger(Long id) {
        return repo.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Application introuvable : " + id));
    }

    private ApplicationVue vue(Application a) {
        return new ApplicationVue(a.getId(), a.getCode(), a.getNom(), a.getDomaine(), a.getEnvironnement(),
                a.getStatut(), a.getProprietaire(), a.getDescription(), a.isActif());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
