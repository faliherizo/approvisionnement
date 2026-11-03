package qc.levis.contrat.commun.module;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.commun.module.application.dto.ModuleToggleResult;
import qc.levis.contrat.commun.module.application.repository.ModuleCatalogueRepository;
import qc.levis.contrat.commun.module.application.repository.TenantModuleRepository;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;
import qc.levis.contrat.commun.module.domain.TenantModule;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock ModuleCatalogueRepository catalogueRepo;
    @Mock TenantModuleRepository tenantRepo;
    ModuleService service;

    @BeforeEach void avant() { service = new ModuleService(catalogueRepo, tenantRepo); }

    private List<ModuleCatalogue> catalogue() {
        return List.of(
                new ModuleCatalogue("formulaire", "Formulaires", "P", 10, false, null),
                new ModuleCatalogue("saisie", "Saisies", "P", 30, false, "formulaire"));
    }

    @Test void est_actif_superadmin_voit_tout() {
        assertTrue(service.estActif(null, "formulaire"));
        verifyNoInteractions(tenantRepo);
    }

    @Test void est_actif_lit_la_ligne_tenant() {
        when(tenantRepo.findByTenantIdAndModuleCode(1L, "saisie"))
                .thenReturn(Optional.of(new TenantModule(1L, "saisie", true, "a")));
        assertTrue(service.estActif(1L, "saisie"));
    }

    @Test void est_actif_retombe_sur_actif_par_defaut() {
        when(tenantRepo.findByTenantIdAndModuleCode(1L, "formulaire")).thenReturn(Optional.empty());
        when(catalogueRepo.findByCode("formulaire"))
                .thenReturn(Optional.of(new ModuleCatalogue("formulaire", "F", "P", 10, true, null)));
        assertTrue(service.estActif(1L, "formulaire"));
    }

    @Test void activer_tire_les_prerequis_transitifs() {
        when(catalogueRepo.findAllByOrderByOrdreAsc()).thenReturn(catalogue());
        when(tenantRepo.findByTenantIdAndModuleCode(eq(1L), anyString())).thenReturn(Optional.empty());
        when(tenantRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ModuleToggleResult r = service.activer(1L, "saisie", "admin");
        assertTrue(r.codesAffectes().contains("saisie") && r.codesAffectes().contains("formulaire"));
        assertTrue(r.message().contains("formulaire"));
        verify(tenantRepo, times(2)).save(any()); // saisie + formulaire
    }

    @Test void desactiver_tire_les_dependants() {
        when(catalogueRepo.findAllByOrderByOrdreAsc()).thenReturn(catalogue());
        when(tenantRepo.findByTenantIdAndModuleCode(eq(1L), anyString())).thenReturn(Optional.empty());
        when(tenantRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        ModuleToggleResult r = service.desactiver(1L, "formulaire", "admin");
        assertTrue(r.codesAffectes().contains("saisie"), "saisie dépend de formulaire");
    }

    @Test void module_inconnu_est_refuse() {
        when(catalogueRepo.findAllByOrderByOrdreAsc()).thenReturn(catalogue());
        assertThrows(java.util.NoSuchElementException.class, () -> service.activer(1L, "inconnu", "a"));
    }
}
