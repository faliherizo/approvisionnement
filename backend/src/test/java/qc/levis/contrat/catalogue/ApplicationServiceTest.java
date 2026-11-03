package qc.levis.contrat.catalogue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.catalogue.application.ApplicationService;
import qc.levis.contrat.catalogue.application.dto.ApplicationRequest;
import qc.levis.contrat.catalogue.application.dto.ApplicationVue;
import qc.levis.contrat.catalogue.application.repository.ApplicationRepository;
import qc.levis.contrat.catalogue.domain.Application;
import qc.levis.contrat.commun.infrastructure.TenantContext;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository repo;
    ApplicationService service;

    @BeforeEach void avant() { service = new ApplicationService(repo); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    private ApplicationRequest req(String code, String env, String statut) {
        return new ApplicationRequest(code, "Portail", "APPROVISIONNEMENT", env, statut, "SA", "desc");
    }

    @Test void creer_normalise_environnement_et_statut_invalides() {
        when(repo.existsByTenantIdAndCode(1L, "p1")).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApplicationVue v = service.creer(req("p1", "ZZZ", "???"));
        assertEquals("DEV", v.environnement());     // valeur invalide → DEV
        assertEquals("BROUILLON", v.statut());       // valeur invalide → BROUILLON
        assertEquals("APPROVISIONNEMENT", v.domaine());
    }

    @Test void creer_refuse_un_code_duplique() {
        when(repo.existsByTenantIdAndCode(1L, "dup")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.creer(req("dup", "PROD", "ACTIF")));
        verify(repo, never()).save(any());
    }

    @Test void lister_filtre_par_domaine_si_fourni() {
        when(repo.findByTenantIdAndDomaineOrderByNomAsc(1L, "RH")).thenReturn(List.of(new Application(1L, "rh1", "RH", "RH", "PROD", "ACTIF", null, null)));
        assertEquals(1, service.lister("RH").size());
        verify(repo).findByTenantIdAndDomaineOrderByNomAsc(1L, "RH");
        verify(repo, never()).findByTenantIdOrderByNomAsc(anyLong());
    }

    @Test void lister_sans_domaine_prend_tout() {
        when(repo.findByTenantIdOrderByNomAsc(1L)).thenReturn(List.of());
        service.lister(null);
        verify(repo).findByTenantIdOrderByNomAsc(1L);
    }

    @Test void archiver_bascule_le_drapeau_actif() {
        Application a = new Application(1L, "p1", "P", "RH", "PROD", "ACTIF", null, null);
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(java.util.Optional.of(a));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertFalse(service.archiver(5L, false).actif());
    }

    @Test void hors_tenant_est_refuse() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, () -> service.lister(null));
    }
}
