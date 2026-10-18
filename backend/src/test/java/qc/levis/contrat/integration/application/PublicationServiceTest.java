package qc.levis.contrat.integration.application;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.PublicationService;
import qc.levis.contrat.integration.application.dto.PublicationRequest;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.integration.application.repository.PublicationLogRepository;
import qc.levis.contrat.integration.domain.PublicationLog;
import qc.levis.contrat.redaction.application.DocumentRenderingService;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.infrastructure.DocumentRenderer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link PublicationService} (routage vers l'adaptateur, rendu, journalisation). */
class PublicationServiceTest {

    private static final Long TENANT = 7L;

    private DocumentService documents;
    private DocumentRenderingService rendu;
    private PublicationCiblePort adaptateur;
    private PublicationLogRepository journal;
    private AuditService audit;
    private PublicationService service;

    @BeforeEach
    void setUp() {
        documents = mock(DocumentService.class);
        rendu = mock(DocumentRenderingService.class);
        adaptateur = mock(PublicationCiblePort.class);
        journal = mock(PublicationLogRepository.class);
        audit = mock(AuditService.class);
        service = new PublicationService(documents, rendu, List.of(adaptateur), journal, audit, "Contrats");
        TenantContext.set(TENANT);

        Document doc = new Document();
        doc.setTitre("Contrat X");
        doc.setStatut(Enums.StatutDocument.APPROUVE); // document validé → publiable
        when(documents.get(1L)).thenReturn(doc);
        when(journal.save(any(PublicationLog.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void publier_pdf_parDefaut_versAdaptateurSupportant() {
        when(adaptateur.supporte(Enums.CiblePublication.SHAREPOINT)).thenReturn(true);
        when(rendu.rendre(1L, DocumentRenderer.Format.PDF, null)).thenReturn(new byte[]{1, 2, 3});
        when(adaptateur.deposer(eq(Enums.CiblePublication.SHAREPOINT), any()))
                .thenReturn(new PublicationCiblePort.ResultatPublication(true, "sim://sharepoint/contrat-1.pdf", "ok"));

        PublicationLog log = service.publier(1L,
                new PublicationRequest(Enums.CiblePublication.SHAREPOINT, null, null, null), "user");

        assertTrue(log.isSucces());
        assertEquals(Enums.CiblePublication.SHAREPOINT, log.getCible());
        assertEquals(TENANT, log.getTenantId());
        assertEquals("sim://sharepoint/contrat-1.pdf", log.getUrlCible());

        ArgumentCaptor<PublicationCiblePort.ContenuPublie> c =
                ArgumentCaptor.forClass(PublicationCiblePort.ContenuPublie.class);
        verify(adaptateur).deposer(eq(Enums.CiblePublication.SHAREPOINT), c.capture());
        assertEquals("contrat-1.pdf", c.getValue().nomFichier());
        assertEquals("application/pdf", c.getValue().mediaType());
        assertEquals("Contrats", c.getValue().destination());   // dossier par défaut (aucun fourni)
        verify(audit).log(eq("user"), eq("PUBLIER"), eq("Document"), eq(1L), contains("SHAREPOINT"));
    }

    @Test
    void publier_docx_choisitFormatEtMediaType() {
        when(adaptateur.supporte(any())).thenReturn(true);
        when(rendu.rendre(1L, DocumentRenderer.Format.DOCX, "GAB")).thenReturn(new byte[]{9});
        when(adaptateur.deposer(any(), any()))
                .thenReturn(new PublicationCiblePort.ResultatPublication(true, "sim://onedrive/contrat-1.docx", "ok"));

        service.publier(1L, new PublicationRequest(
                Enums.CiblePublication.ONEDRIVE, Enums.FormatPublication.DOCX, "GAB", "/Contrats/2027/"), "user");

        ArgumentCaptor<PublicationCiblePort.ContenuPublie> c =
                ArgumentCaptor.forClass(PublicationCiblePort.ContenuPublie.class);
        verify(adaptateur).deposer(eq(Enums.CiblePublication.ONEDRIVE), c.capture());
        assertEquals("contrat-1.docx", c.getValue().nomFichier());
        assertTrue(c.getValue().mediaType().contains("wordprocessingml"));
        assertEquals("Contrats/2027", c.getValue().destination());   // chemin saisi, normalisé
        verify(rendu).rendre(1L, DocumentRenderer.Format.DOCX, "GAB");
    }

    @Test
    void publier_aucunAdaptateur_leve() {
        when(adaptateur.supporte(Enums.CiblePublication.SAP)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> service.publier(1L,
                new PublicationRequest(Enums.CiblePublication.SAP, null, null, null), "user"));
        verify(journal, never()).save(any());
    }

    @Test
    void cheminValide_vide_utiliseDossierParDefaut() {
        assertEquals("Contrats", service.cheminValide(null));
        assertEquals("Contrats", service.cheminValide("   "));
    }

    @Test
    void cheminValide_personnalise_estNormalise() {
        assertEquals("Contrats/2027", service.cheminValide("/Contrats/2027/"));
        assertEquals("Rapports", service.cheminValide("\\Rapports\\"));
    }

    @Test
    void cheminValide_traverseeDeRepertoire_estRefusee() {
        assertThrows(IllegalArgumentException.class, () -> service.cheminValide("../secret"));
        assertThrows(IllegalArgumentException.class, () -> service.cheminValide("Contrats/../../etc"));
    }

    @Test
    void publier_documentNonValide_refuse() {
        Document brouillon = new Document();
        brouillon.setTitre("Brouillon");
        brouillon.setStatut(Enums.StatutDocument.BROUILLON);
        when(documents.get(9L)).thenReturn(brouillon);

        var req = new PublicationRequest(Enums.CiblePublication.SHAREPOINT, null, null, null);
        assertThrows(IllegalStateException.class, () -> service.publier(9L, req, "user"));
        verify(journal, never()).save(any());
    }

    @Test
    void historique_delegueAuJournal() {
        when(journal.findByDocumentIdOrderByPublieLeDesc(1L)).thenReturn(List.of(new PublicationLog()));
        assertEquals(1, service.historique(1L).size());
        verify(documents).get(1L);
    }
}
