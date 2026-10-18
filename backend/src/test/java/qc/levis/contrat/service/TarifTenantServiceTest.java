package qc.levis.contrat.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.tenant.application.TarifTenantService;
import qc.levis.contrat.tenant.application.dto.TarifTenantRequest;
import qc.levis.contrat.tenant.application.repository.TarifTenantRepository;
import qc.levis.contrat.tenant.domain.TarifTenant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests unitaires du service de tarification par tenant (validation des bornes et défauts). */
class TarifTenantServiceTest {

    private final TarifTenantRepository repo = mock(TarifTenantRepository.class);
    private final TarifTenantService service = new TarifTenantService(repo);

    private TarifTenantRequest req(BigDecimal forfait, String devise, BigDecimal index,
                                              Integer nbUsers, String notes, Boolean actif) {
        return new TarifTenantRequest(forfait, devise, index, nbUsers, notes, actif);
    }

    @Test
    void pour_absent_renvoieTarifViergeAvecTenant() {
        when(repo.findByTenantId(7L)).thenReturn(Optional.empty());
        TarifTenant t = service.pour(7L);
        assertEquals(7L, t.getTenantId());
        assertEquals(BigDecimal.ZERO, t.getForfaitAnnuel());
        assertEquals("CAD", t.getDevise());
    }

    @Test
    void pour_present_renvoieExistant() {
        TarifTenant existant = new TarifTenant();
        existant.setTenantId(3L);
        existant.setForfaitAnnuel(new BigDecimal("58000.00"));
        when(repo.findByTenantId(3L)).thenReturn(Optional.of(existant));
        assertSame(existant, service.pour(3L));
    }

    @Test
    void enregistrer_nouveau_appliqueValeurs() {
        when(repo.findByTenantId(1L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        TarifTenant t = service.enregistrer(1L, req(new BigDecimal("58000"), "cad",
                new BigDecimal("3"), 50, "tout inclus", null));

        assertEquals(1L, t.getTenantId());
        assertEquals(new BigDecimal("58000"), t.getForfaitAnnuel());
        assertEquals("CAD", t.getDevise());          // normalisée en majuscules
        assertEquals(new BigDecimal("3"), t.getIndexationPct());
        assertEquals(50, t.getNbUtilisateursInclus());
        assertEquals("tout inclus", t.getNotes());
        assertTrue(t.isActif());                      // null → true
    }

    @Test
    void enregistrer_existant_metAJour() {
        TarifTenant existant = new TarifTenant();
        existant.setTenantId(2L);
        existant.setForfaitAnnuel(new BigDecimal("10000"));
        when(repo.findByTenantId(2L)).thenReturn(Optional.of(existant));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        TarifTenant t = service.enregistrer(2L, req(new BigDecimal("72000"), "USD",
                new BigDecimal("2.5"), null, null, false));

        assertSame(existant, t);
        assertEquals(new BigDecimal("72000"), t.getForfaitAnnuel());
        assertEquals("USD", t.getDevise());
        assertFalse(t.isActif());                     // false conservé
    }

    @Test
    void enregistrer_montantNulOuNegatif_rameneAZero() {
        when(repo.findByTenantId(anyLong())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(BigDecimal.ZERO,
                service.enregistrer(1L, req(null, "CAD", null, null, null, true)).getForfaitAnnuel());
        assertEquals(BigDecimal.ZERO,
                service.enregistrer(1L, req(new BigDecimal("-5"), "CAD", null, null, null, true)).getForfaitAnnuel());
    }

    @Test
    void enregistrer_deviseInvalide_rameneCad() {
        when(repo.findByTenantId(anyLong())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("CAD",
                service.enregistrer(1L, req(BigDecimal.TEN, "XYZ", null, null, null, true)).getDevise());
        assertEquals("CAD",
                service.enregistrer(1L, req(BigDecimal.TEN, null, null, null, null, true)).getDevise());
    }

    @Test
    void enregistrer_indexation_borneeEntre0Et100() {
        when(repo.findByTenantId(anyLong())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(BigDecimal.ZERO,
                service.enregistrer(1L, req(BigDecimal.TEN, "CAD", null, null, null, true)).getIndexationPct());
        assertEquals(BigDecimal.ZERO,
                service.enregistrer(1L, req(BigDecimal.TEN, "CAD", new BigDecimal("-1"), null, null, true)).getIndexationPct());
        assertEquals(0, new BigDecimal("100").compareTo(
                service.enregistrer(1L, req(BigDecimal.TEN, "CAD", new BigDecimal("250"), null, null, true)).getIndexationPct()));
    }
}
