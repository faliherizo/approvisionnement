package qc.levis.contrat.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.approbation.application.ApprovalWorkflowService;
import qc.levis.contrat.approbation.application.dto.WorkflowRequest;
import qc.levis.contrat.approbation.application.repository.ApprovalWorkflowRepository;
import qc.levis.contrat.approbation.domain.ApprovalWorkflow;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link ApprovalWorkflowService} (CRUD, isolation tenant, invariants). */
class ApprovalWorkflowServiceTest {

    private static final Long TENANT = 7L;

    private ApprovalWorkflowRepository repo;
    private AuditService audit;
    private ApprovalWorkflowService service;

    @BeforeEach
    void setUp() {
        repo = mock(ApprovalWorkflowRepository.class);
        audit = mock(AuditService.class);
        service = new ApprovalWorkflowService(repo, audit);
        TenantContext.set(TENANT);
        when(repo.save(any(ApprovalWorkflow.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private ApprovalWorkflow workflow(Long id, Long tenant) {
        ApprovalWorkflow w = new ApprovalWorkflow();
        w.setNom("Circuit AOP");
        w.setType(Enums.TypeContrat.APPEL_OFFRES_PUBLIC);
        w.setTenantId(tenant);
        w.setEtapes(new java.util.ArrayList<>(List.of(Enums.Role.APPRO, Enums.Role.DIRECTION)));
        try {
            var f = ApprovalWorkflow.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(w, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return w;
    }

    private WorkflowRequest req(Boolean actif) {
        return new WorkflowRequest("Circuit AOP", Enums.TypeContrat.APPEL_OFFRES_PUBLIC,
                List.of(Enums.Role.APPRO, Enums.Role.JURIDIQUE, Enums.Role.DIRECTION), actif);
    }

    @Test
    void lister_parTenant() {
        when(repo.findByTenantId(TENANT)).thenReturn(List.of(workflow(1L, TENANT)));
        assertEquals(1, service.lister().size());
    }

    @Test
    void creer_persisteEtActifParDefaut() {
        ApprovalWorkflow w = service.creer(req(null), "admin");
        assertEquals(TENANT, w.getTenantId());
        assertTrue(w.isActif(), "actif par défaut quand non précisé");
        assertEquals(3, w.getEtapes().size());
        verify(audit).log(eq("admin"), eq("CREER"), eq("ApprovalWorkflow"), any(), eq("Circuit AOP"));
    }

    @Test
    void creer_actifExpliciteFalse() {
        ApprovalWorkflow w = service.creer(req(false), "admin");
        assertFalse(w.isActif());
    }

    @Test
    void creer_sansEtape_refuse() {
        WorkflowRequest vide = new WorkflowRequest("X",
                Enums.TypeContrat.GRE_A_GRE, List.of(), true);
        assertThrows(IllegalArgumentException.class, () -> service.creer(vide, "admin"));
    }

    @Test
    void get_introuvable_leve() {
        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.get(9L));
    }

    @Test
    void get_autreTenant_masque() {
        when(repo.findById(1L)).thenReturn(Optional.of(workflow(1L, 999L)));
        assertThrows(NoSuchElementException.class, () -> service.get(1L));
    }

    @Test
    void get_superadmin_voitTout() {
        TenantContext.set(null);
        when(repo.findById(1L)).thenReturn(Optional.of(workflow(1L, 999L)));
        assertNotNull(service.get(1L));
    }

    @Test
    void modifier_metAJour() {
        when(repo.findById(1L)).thenReturn(Optional.of(workflow(1L, TENANT)));
        ApprovalWorkflow w = service.modifier(1L, req(true), "admin");
        assertEquals(3, w.getEtapes().size());
        verify(audit).log(any(), eq("MODIFIER"), eq("ApprovalWorkflow"), eq(1L), any());
    }

    @Test
    void supprimer_effaceEtJournalise() {
        when(repo.findById(1L)).thenReturn(Optional.of(workflow(1L, TENANT)));
        service.supprimer(1L, "admin");
        verify(repo).delete(any(ApprovalWorkflow.class));
        verify(audit).log(any(), eq("SUPPRIMER"), eq("ApprovalWorkflow"), eq(1L), any());
    }
}
