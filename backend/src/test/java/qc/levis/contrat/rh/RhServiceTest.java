package qc.levis.contrat.rh;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rh.application.RhService;
import qc.levis.contrat.rh.application.dto.EmployeRequest;
import qc.levis.contrat.rh.application.dto.EmployeVue;
import qc.levis.contrat.rh.application.repository.EmployeRepository;
import qc.levis.contrat.rh.domain.Employe;

@ExtendWith(MockitoExtension.class)
class RhServiceTest {

    @Mock EmployeRepository repo;
    RhService service;

    @BeforeEach void avant() { service = new RhService(repo); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    private EmployeRequest req() {
        return new EmployeRequest("Marie", "Tremblay", "123456789", "marie@levis.qc.ca", "418-000-0000",
                "2026-01-15", null, "ACTIF", "Analyste", "TI", 78000, "biweekly", "QC", "employee");
    }

    @Test void creer_genere_un_matricule_et_masque_le_nas() {
        when(repo.save(any())).thenAnswer(i -> { Employe e = i.getArgument(0); ReflectionTestUtils.setField(e, "id", 7L); return e; });
        EmployeVue v = service.creer(req());
        assertTrue(v.matricule().matches("EMP-\\d{4}-00007"), "matricule généré : " + v.matricule());
        assertEquals("***-***-789", v.nasMasque());   // NAS jamais en clair (Loi 25)
        assertEquals(78000, v.salaireAnnuel(), 0.01);
    }

    @Test void nas_absent_reste_null_masque() {
        EmployeRequest sansNas = new EmployeRequest("A", "B", null, null, null, null, null, "ACTIF", null, null, 0, "biweekly", "QC", "employee");
        when(repo.save(any())).thenAnswer(i -> { Employe e = i.getArgument(0); ReflectionTestUtils.setField(e, "id", 1L); return e; });
        assertNull(service.creer(sansNas).nasMasque());
    }

    @Test void archiver_passe_le_statut_a_inactif() {
        Employe e = new Employe();
        ReflectionTestUtils.setField(e, "id", 3L);
        e.setTenantId(1L); e.setPrenom("A"); e.setNom("B"); e.setStatut("ACTIF"); e.setActif(true);
        when(repo.findByIdAndTenantId(3L, 1L)).thenReturn(Optional.of(e));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        EmployeVue v = service.archiver(3L, false);
        assertFalse(v.actif());
        assertEquals("INACTIF", v.statut());
    }

    @Test void obtenir_introuvable_leve_une_exception() {
        when(repo.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.obtenir(99L));
    }
}
