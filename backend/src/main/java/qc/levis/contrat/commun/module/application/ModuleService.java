package qc.levis.contrat.commun.module.application;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.module.application.dto.ModuleToggleResult;
import qc.levis.contrat.commun.module.application.dto.ModuleVue;
import qc.levis.contrat.commun.module.application.repository.ModuleCatalogueRepository;
import qc.levis.contrat.commun.module.application.repository.TenantModuleRepository;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;
import qc.levis.contrat.commun.module.domain.TenantModule;

/**
 * Registre des modules activables/désactivables par tenant (espace superadmin).
 * État effectif = réglage explicite du tenant, sinon valeur par défaut du catalogue.
 * L'activation tire les prérequis ; la désactivation retire les dépendants (voir {@link ModuleDependances}).
 */
@Service
public class ModuleService {

    private final ModuleCatalogueRepository catalogueRepo;
    private final TenantModuleRepository tenantRepo;

    public ModuleService(ModuleCatalogueRepository catalogueRepo, TenantModuleRepository tenantRepo) {
        this.catalogueRepo = catalogueRepo;
        this.tenantRepo = tenantRepo;
    }

    public List<ModuleCatalogue> catalogue() {
        return catalogueRepo.findAllByOrderByOrdreAsc();
    }

    /** État effectif d'un module pour un tenant (réglage explicite sinon défaut du catalogue). */
    public boolean estActif(Long tenantId, String code) {
        if (tenantId == null) return true; // superadmin voit tout
        return tenantRepo.findByTenantIdAndModuleCode(tenantId, code)
                .map(TenantModule::isActif)
                .orElseGet(() -> catalogueRepo.findByCode(code)
                        .map(ModuleCatalogue::isActifParDefaut)
                        .orElse(false));
    }

    /** Vue des modules et de leur état pour un tenant. */
    public List<ModuleVue> etatPourTenant(Long tenantId) {
        List<ModuleVue> res = new ArrayList<>();
        for (ModuleCatalogue m : catalogue()) {
            res.add(new ModuleVue(m.getCode(), m.getNom(), m.getFamille(),
                    estActif(tenantId, m.getCode()), ModuleDependances.parse(m.getRequiert())));
        }
        return res;
    }

    @Transactional
    public ModuleToggleResult activer(Long tenantId, String code, String par) {
        List<ModuleCatalogue> cat = catalogue();
        exigerExiste(cat, code);
        Set<String> aActiver = ModuleDependances.requisTransitifs(cat, code);
        for (String c : aActiver) appliquer(tenantId, c, true, par);
        List<String> deps = new ArrayList<>(aActiver);
        deps.remove(code);
        String msg = deps.isEmpty()
                ? "Module « " + code + " » activé."
                : "Module « " + code + " » activé avec ses prérequis : " + String.join(", ", deps) + ".";
        return new ModuleToggleResult(new ArrayList<>(aActiver), msg);
    }

    @Transactional
    public ModuleToggleResult desactiver(Long tenantId, String code, String par) {
        List<ModuleCatalogue> cat = catalogue();
        exigerExiste(cat, code);
        Set<String> aDesactiver = ModuleDependances.dependantsTransitifs(cat, code);
        for (String c : aDesactiver) appliquer(tenantId, c, false, par);
        List<String> deps = new ArrayList<>(aDesactiver);
        deps.remove(code);
        String msg = deps.isEmpty()
                ? "Module « " + code + " » désactivé."
                : "Module « " + code + " » désactivé avec ses dépendants : " + String.join(", ", deps) + ".";
        return new ModuleToggleResult(new ArrayList<>(aDesactiver), msg);
    }

    private void appliquer(Long tenantId, String code, boolean actif, String par) {
        TenantModule tm = tenantRepo.findByTenantIdAndModuleCode(tenantId, code)
                .orElseGet(() -> new TenantModule(tenantId, code, actif, par));
        tm.setActif(actif);
        tm.setActivePar(par);
        tm.setActiveLe(java.time.Instant.now());
        tenantRepo.save(tm);
    }

    private void exigerExiste(List<ModuleCatalogue> cat, String code) {
        boolean ok = cat.stream().anyMatch(m -> m.getCode().equals(code));
        if (!ok) throw new NoSuchElementException("Module inconnu : " + code);
    }
}
