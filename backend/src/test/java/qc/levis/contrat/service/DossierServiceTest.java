package qc.levis.contrat.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.dossier.application.dto.DossierRapport;
import qc.levis.contrat.dossier.application.dto.DossierRequest;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link DossierService} (couverture des branches et invariants). */
class DossierServiceTest {

    private static final Long TENANT = 7L;

    private DossierRepository dossiers;
    private DocumentRepository documents;
    private AuditService audit;
    private DossierService service;

    @BeforeEach
    void setUp() {
        dossiers = mock(DossierRepository.class);
        documents = mock(DocumentRepository.class);
        audit = mock(AuditService.class);
        service = new DossierService(dossiers, documents, audit);
        TenantContext.set(TENANT);
        when(dossiers.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));
        when(documents.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Dossier dossier(Long id, Long tenant) {
        Dossier d = new Dossier();
        d.setTitre("Acquisition logiciel");
        d.setTenantId(tenant);
        setId(d, id);
        return d;
    }

    private void setId(Object entite, Long id) {
        try {
            var f = entite.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entite, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Document document(Long id, Long tenant, Long dossierId, Enums.StatutDocument statut) {
        Document doc = new Document();
        doc.setTitre("Devis");
        doc.setTenantId(tenant);
        doc.setDossierId(dossierId);
        doc.setStatut(statut);
        setId(doc, id);
        return doc;
    }

    private DossierRequest req() {
        return new DossierRequest("Acquisition logiciel", "7000017905", "Objet",
                Enums.TypeAcquisition.TECHNOLOGIES, Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, "Sarra");
    }

    @Test
    void lister_filtreParTenant() {
        when(dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(TENANT)).thenReturn(List.of(dossier(1L, TENANT)));
        assertEquals(1, service.lister().size());
        verify(dossiers).findByTenantIdAndActifTrueOrderByCreeLeDesc(TENANT);
    }

    @Test
    void creer_persisteEtJournalise() {
        Dossier saved = service.creer(req(), "user@levis");
        assertEquals(TENANT, saved.getTenantId());
        assertEquals(Enums.StatutDossier.OUVERT, saved.getStatut());
        assertEquals("user@levis", saved.getCreePar());
        assertEquals(Enums.TypeAcquisition.TECHNOLOGIES, saved.getTypeAcquisition());
        verify(audit).log(eq("user@levis"), eq("CREER"), eq("Dossier"), any(), eq("Acquisition logiciel"));
    }

    @Test
    void get_introuvable_leve() {
        when(dossiers.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.get(99L));
    }

    @Test
    void get_autreTenant_masque() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, 999L)));
        assertThrows(NoSuchElementException.class, () -> service.get(1L));
    }

    @Test
    void get_superadmin_voitTousLesTenants() {
        TenantContext.set(null);
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, 999L)));
        assertNotNull(service.get(1L));
    }

    @Test
    void modifier_metAJourLesChamps() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        Dossier m = service.modifier(1L, req(), "user@levis");
        assertEquals("7000017905", m.getNumeroAO());
        assertEquals("Sarra", m.getResponsable());
        verify(audit).log(any(), eq("MODIFIER"), eq("Dossier"), eq(1L), any());
    }

    @Test
    void changerStatut_transitionValide() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        Dossier d = service.changerStatut(1L, Enums.StatutDossier.ADJUGE, "user@levis");
        assertEquals(Enums.StatutDossier.ADJUGE, d.getStatut());
    }

    @Test
    void changerStatut_dossierTermine_refuse() {
        Dossier ferme = dossier(1L, TENANT);
        ferme.setStatut(Enums.StatutDossier.CLOTURE);
        when(dossiers.findById(1L)).thenReturn(Optional.of(ferme));
        assertThrows(IllegalStateException.class,
                () -> service.changerStatut(1L, Enums.StatutDossier.OUVERT, "user@levis"));
    }

    @Test
    void supprimer_detacheLesDocuments_etDesactive() {
        Dossier d = dossier(1L, TENANT);
        when(dossiers.findById(1L)).thenReturn(Optional.of(d));
        Document doc = document(10L, TENANT, 1L, Enums.StatutDocument.BROUILLON);
        when(documents.findByDossierIdAndActifTrue(1L)).thenReturn(List.of(doc));

        service.supprimer(1L, "user@levis");

        assertFalse(d.isActif());
        assertNull(doc.getDossierId());
        verify(documents).save(doc);
        verify(audit).log(any(), eq("SUPPRIMER"), eq("Dossier"), eq(1L), any());
    }

    @Test
    void documentsDu_retourneLesDocumentsRattaches() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findByDossierIdAndActifTrue(1L))
                .thenReturn(List.of(document(10L, TENANT, 1L, Enums.StatutDocument.BROUILLON)));
        assertEquals(1, service.documentsDu(1L).size());
    }

    @Test
    void rattacher_associeLeDocument() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        Document doc = document(10L, TENANT, null, Enums.StatutDocument.BROUILLON);
        when(documents.findById(10L)).thenReturn(Optional.of(doc));

        Document r = service.rattacher(1L, 10L, "user@levis");

        assertEquals(1L, r.getDossierId());
        verify(audit).log(any(), eq("RATTACHER"), eq("Document"), eq(10L), eq("Dossier=1"));
    }

    @Test
    void rattacher_documentIntrouvable_leve() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.rattacher(1L, 10L, "u"));
    }

    @Test
    void rattacher_documentAutreTenant_masque() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findById(10L)).thenReturn(Optional.of(document(10L, 999L, null, Enums.StatutDocument.BROUILLON)));
        assertThrows(NoSuchElementException.class, () -> service.rattacher(1L, 10L, "u"));
    }

    @Test
    void detacher_retireLeRattachement() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        Document doc = document(10L, TENANT, 1L, Enums.StatutDocument.BROUILLON);
        when(documents.findById(10L)).thenReturn(Optional.of(doc));

        Document r = service.detacher(1L, 10L, "user@levis");

        assertNull(r.getDossierId());
        verify(audit).log(any(), eq("DETACHER"), eq("Document"), eq(10L), any());
    }

    @Test
    void detacher_documentNonRattache_leve() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findById(10L)).thenReturn(Optional.of(document(10L, TENANT, 2L, Enums.StatutDocument.BROUILLON)));
        assertThrows(NoSuchElementException.class, () -> service.detacher(1L, 10L, "u"));
    }

    @Test
    void rapport_agregeLesStatuts() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findByDossierIdAndActifTrue(1L)).thenReturn(List.of(
                document(10L, TENANT, 1L, Enums.StatutDocument.APPROUVE),
                document(11L, TENANT, 1L, Enums.StatutDocument.APPROUVE),
                document(12L, TENANT, 1L, Enums.StatutDocument.SIGNE),
                document(13L, TENANT, 1L, Enums.StatutDocument.BROUILLON)));

        DossierRapport rapport = service.rapport(1L);

        assertEquals(4, rapport.nombreDocuments());
        assertEquals(2, rapport.documentsApprouves());
        assertEquals(1, rapport.documentsSignes());
        assertEquals(2L, rapport.parStatut().get("APPROUVE"));
        assertEquals(1L, rapport.parStatut().get("BROUILLON"));
        assertEquals(Enums.StatutDossier.OUVERT, rapport.statut());
    }

    @Test
    void rapport_dossierVide() {
        when(dossiers.findById(1L)).thenReturn(Optional.of(dossier(1L, TENANT)));
        when(documents.findByDossierIdAndActifTrue(1L)).thenReturn(List.of());
        DossierRapport rapport = service.rapport(1L);
        assertEquals(0, rapport.nombreDocuments());
        assertTrue(rapport.parStatut().isEmpty());
    }
}
