package qc.levis.contrat.temps;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.temps.application.FeuilleTempsService;
import qc.levis.contrat.temps.application.dto.FeuilleTempsRequest;
import qc.levis.contrat.temps.application.repository.FeuilleTempsRepository;
import qc.levis.contrat.temps.domain.FeuilleTemps;

@ExtendWith(MockitoExtension.class)
class FeuilleTempsServiceTest {

    @Mock FeuilleTempsRepository repo;
    FeuilleTempsService service;

    @BeforeEach void avant() { service = new FeuilleTempsService(repo); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    private FeuilleTemps feuille(long id, String statut) {
        FeuilleTemps f = new FeuilleTemps(1L, "u@x.ca", null, "2026-06-01", "2026-06-07");
        f.setStatut(statut);
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }

    @Test void creer_calcule_le_total_des_heures() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new FeuilleTempsRequest("2026-06-01", "2026-06-07",
                List.of(Map.of("date", "2026-06-01", "categorie", "travail", "heures", 7.5),
                        Map.of("date", "2026-06-02", "categorie", "travail", "heures", 8)));
        assertEquals(15.5, service.creer(req, "u@x.ca").totalHeures(), 0.001);
    }

    @Test void maj_refusee_si_deja_soumise() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(feuille(5, "SOUMISE")));
        assertThrows(IllegalStateException.class,
                () -> service.majLignes(5L, new FeuilleTempsRequest(null, null, List.of())));
    }

    @Test void soumettre_puis_approuver_suivent_le_cycle() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(feuille(5, "BROUILLON")));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("SOUMISE", service.soumettre(5L).statut());

        when(repo.findByIdAndTenantId(6L, 1L)).thenReturn(Optional.of(feuille(6, "SOUMISE")));
        var v = service.approuver(6L, "chef@x.ca");
        assertEquals("APPROUVEE", v.statut());
        assertEquals("chef@x.ca", v.approbateur());
    }

    @Test void approuver_une_feuille_non_soumise_est_refuse() {
        when(repo.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(feuille(7, "BROUILLON")));
        assertThrows(IllegalStateException.class, () -> service.approuver(7L, "chef@x.ca"));
    }
}
