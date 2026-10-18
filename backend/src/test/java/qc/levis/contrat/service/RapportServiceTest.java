package qc.levis.contrat.service;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.RapportService;
import qc.levis.contrat.dossier.application.dto.RapportSuivi;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.application.ObligationService;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.Obligation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link RapportService} (agrégation read-model). */
class RapportServiceTest {

    private static final Long TENANT = 7L;

    private DocumentRepository documents;
    private DossierRepository dossiers;
    private ObligationService obligations;
    private AuditLogRepository audit;
    private RapportService service;

    @BeforeEach
    void setUp() {
        documents = mock(DocumentRepository.class);
        dossiers = mock(DossierRepository.class);
        obligations = mock(ObligationService.class);
        audit = mock(AuditLogRepository.class);
        service = new RapportService(documents, dossiers, obligations, audit);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Document doc(Enums.TypeContrat type, Enums.StatutDocument statut) {
        Document d = new Document();
        d.setType(type);
        d.setStatut(statut);
        return d;
    }

    private AuditLog audit(String utilisateur) {
        return new AuditLog(utilisateur, "CREER", "Document", 1L, null);
    }

    @Test
    void suivi_agregeStatutsTypesApprobationsDossiersObligationsEtActivite() {
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of(
                doc(Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.StatutDocument.BROUILLON),
                doc(Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.StatutDocument.EN_APPROBATION),
                doc(Enums.TypeContrat.GRE_A_GRE, Enums.StatutDocument.EN_APPROBATION),
                doc(Enums.TypeContrat.GRE_A_GRE, Enums.StatutDocument.APPROUVE),
                doc(null, Enums.StatutDocument.BROUILLON)));  // type nul : ignoré du regroupement par type
        when(dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(TENANT))
                .thenReturn(List.of(new Dossier(), new Dossier()));
        when(obligations.aVenir(30)).thenReturn(List.of(new Obligation()));
        when(audit.rechercher(eq(TENANT), any(), any(), any(), any(), any())).thenReturn(List.of(
                audit("alice"), audit("alice"), audit("bob"), audit(null)));

        RapportSuivi r = service.suivi();

        assertEquals(5, r.totalDocuments());
        assertEquals(2L, r.documentsParStatut().get("EN_APPROBATION"));
        assertEquals(2L, r.documentsParType().get("APPEL_OFFRES_PUBLIC"));
        assertEquals(2, r.documentsParType().values().size()); // type nul non regroupé
        assertEquals(2, r.approbationsEnAttente());
        assertEquals(2, r.dossiersActifs());
        assertEquals(1, r.obligationsAVenir());
        assertEquals("alice", r.activiteParUtilisateur().get(0).utilisateur());
        assertEquals(2, r.activiteParUtilisateur().get(0).actions());
        // L'utilisateur null n'est pas comptabilisé.
        assertTrue(r.activiteParUtilisateur().stream().noneMatch(a -> a.utilisateur() == null));
    }

    @Test
    void suivi_tenantSansDonnees() {
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of());
        when(dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(TENANT)).thenReturn(List.of());
        when(obligations.aVenir(30)).thenReturn(List.of());
        when(audit.rechercher(eq(TENANT), any(), any(), any(), any(), any())).thenReturn(List.of());

        RapportSuivi r = service.suivi();
        assertEquals(0, r.totalDocuments());
        assertTrue(r.documentsParStatut().isEmpty());
        assertTrue(r.activiteParUtilisateur().isEmpty());
    }
}
