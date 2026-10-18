package qc.levis.contrat.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.ia.application.AiTraceabiliteService;
import qc.levis.contrat.ia.application.repository.AiSuggestionRepository;
import qc.levis.contrat.ia.domain.AiSuggestion;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link AiTraceabiliteService} (activation par tenant + traçabilité). */
class AiTraceabiliteServiceTest {

    private static final Long TENANT = 7L;

    private TenantRepository tenants;
    private AiSuggestionRepository suggestions;
    private AuditService audit;
    private AiTraceabiliteService service;

    @BeforeEach
    void setUp() {
        tenants = mock(TenantRepository.class);
        suggestions = mock(AiSuggestionRepository.class);
        audit = mock(AuditService.class);
        service = new AiTraceabiliteService(tenants, suggestions, audit);
        TenantContext.set(TENANT);
        when(suggestions.save(any(AiSuggestion.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Tenant tenant(boolean ia) {
        Tenant t = new Tenant("levis", "Lévis", "");
        t.setIaActivee(ia);
        return t;
    }

    @Test
    void estActivee_superadmin_toujoursVrai() {
        TenantContext.set(null);
        assertTrue(service.estActivee());
        verifyNoInteractions(tenants);
    }

    @Test
    void estActivee_selonLeTenant() {
        when(tenants.findById(TENANT)).thenReturn(Optional.of(tenant(true)));
        assertTrue(service.estActivee());
        when(tenants.findById(TENANT)).thenReturn(Optional.of(tenant(false)));
        assertFalse(service.estActivee());
    }

    @Test
    void estActivee_tenantIntrouvable_defautActive() {
        when(tenants.findById(TENANT)).thenReturn(Optional.empty());
        assertTrue(service.estActivee());
    }

    @Test
    void verifierActivee_leveSiDesactivee() {
        when(tenants.findById(TENANT)).thenReturn(Optional.of(tenant(false)));
        assertThrows(IllegalStateException.class, () -> service.verifierActivee());
    }

    @Test
    void verifierActivee_okSiActivee() {
        when(tenants.findById(TENANT)).thenReturn(Optional.of(tenant(true)));
        assertDoesNotThrow(() -> service.verifierActivee());
    }

    @Test
    void activer_metAJourLeTenant_etJournalise() {
        Tenant t = tenant(true);
        when(tenants.findById(TENANT)).thenReturn(Optional.of(t));
        assertFalse(service.activer(false, "admin"));
        assertFalse(t.isIaActivee());
        verify(audit).log("admin", "IA_DESACTIVEE", "Tenant", TENANT, null);
    }

    @Test
    void activer_superadmin_refuse() {
        TenantContext.set(null);
        assertThrows(IllegalStateException.class, () -> service.activer(true, "admin"));
    }

    @Test
    void activer_tenantIntrouvable_leve() {
        when(tenants.findById(TENANT)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.activer(true, "admin"));
    }

    @Test
    void journaliser_tronqueLesChampsLongs_etConserveLeTenant() {
        String longue = "x".repeat(9000);
        AiSuggestion s = service.journaliser("CHAT", longue, longue, "llama3.1", "user");
        assertEquals(4000, s.getInvite().length());
        assertEquals(8000, s.getReponse().length());
        assertEquals(TENANT, s.getTenantId());
        assertEquals("CHAT", s.getTypeInteraction());
    }

    @Test
    void journaliser_supporteLesValeursNulles() {
        AiSuggestion s = service.journaliser("REDIGER", null, null, "m", "u");
        assertNull(s.getInvite());
        assertNull(s.getReponse());
    }

    @Test
    void lister_parTenant() {
        when(suggestions.findByTenantIdOrderByCreeLeDesc(TENANT)).thenReturn(List.of(new AiSuggestion()));
        assertEquals(1, service.lister().size());
    }
}
