package qc.levis.contrat.service;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.audit.application.AuditQueryService;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link AuditQueryService} (filtres normalisés + échappement CSV). */
class AuditQueryServiceTest {

    private static final Long TENANT = 7L;

    private AuditLogRepository repo;
    private AuditQueryService service;

    @BeforeEach
    void setUp() {
        repo = mock(AuditLogRepository.class);
        service = new AuditQueryService(repo);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void rechercher_normaliseLesFiltresVides_enNull_etScopeTenant() {
        when(repo.rechercher(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        Instant depuis = Instant.parse("2026-01-01T00:00:00Z");

        service.rechercher("  ", "CREER", "", depuis, null);

        // "  " et "" deviennent null ; "CREER" conservé ; tenant courant appliqué.
        verify(repo).rechercher(eq(TENANT), isNull(), eq("CREER"), isNull(), eq(depuis), isNull());
    }

    @Test
    void rechercher_conserveLesFiltresRenseignes() {
        when(repo.rechercher(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        service.rechercher("alice", "SUPPRIMER", "Document", null, null);
        ArgumentCaptor<String> u = ArgumentCaptor.forClass(String.class);
        verify(repo).rechercher(eq(TENANT), u.capture(), eq("SUPPRIMER"), eq("Document"), isNull(), isNull());
        assertEquals("alice", u.getValue());
    }

    @Test
    void exporterCsv_entete_etEchappement() {
        when(repo.rechercher(any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                new AuditLog("alice", "CREER", "Document", 1L, "valeur, avec virgule"),
                new AuditLog("bob", "NOTE", "Clause", 2L, "dit \"bonjour\""),
                new AuditLog("carol", "MULTI", "Doc", 3L, "ligne1\nligne2"),
                new AuditLog("cr", "CR", "Doc", 6L, "retour\rchariot"),
                new AuditLog("dan", "VIDE", "Doc", null, null),
                new AuditLog("eve", "SIMPLE", "Doc", 4L, "ok")));

        String csv = service.exporterCsv(null, null, null, null, null);
        String[] lignes = csv.split("\n", -1);

        assertEquals("horodatage,utilisateur,action,entite,entiteId,details", lignes[0]);
        assertTrue(csv.contains("\"valeur, avec virgule\""), "virgule → encadré");
        assertTrue(csv.contains("\"dit \"\"bonjour\"\"\""), "guillemets doublés");
        assertTrue(csv.contains("\"ligne1\nligne2\""), "saut de ligne → encadré");
        assertTrue(csv.contains("\"retour\rchariot\""), "retour chariot → encadré");
        assertTrue(csv.contains("dan,VIDE,Doc,,"), "id et détails nuls → champs vides");
        assertTrue(csv.contains("eve,SIMPLE,Doc,4,ok"), "valeur simple non encadrée");
    }

    @Test
    void exporterCsv_journalVide_enteteSeule() {
        when(repo.rechercher(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        assertEquals("horodatage,utilisateur,action,entite,entiteId,details\n",
                service.exporterCsv(null, null, null, null, null));
    }
}
