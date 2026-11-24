package qc.levis.contrat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.approbation.application.repository.ApprovalWorkflowRepository;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.application.repository.GabaritRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.tenant.application.ExportTenantService;
import qc.levis.contrat.tenant.domain.Tenant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link ExportTenantService} (archive ZIP de réversibilité). */
class ExportTenantServiceTest {

    private static final Long TENANT = 7L;

    private ClauseRepository clauses;
    private DocumentTemplateRepository templates;
    private GabaritRepository gabarits;
    private DossierRepository dossiers;
    private DocumentRepository documents;
    private ApprovalWorkflowRepository workflows;
    private AuditService audit;
    private ExportTenantService service;

    @BeforeEach
    void setUp() {
        clauses = mock(ClauseRepository.class);
        templates = mock(DocumentTemplateRepository.class);
        gabarits = mock(GabaritRepository.class);
        dossiers = mock(DossierRepository.class);
        documents = mock(DocumentRepository.class);
        workflows = mock(ApprovalWorkflowRepository.class);
        audit = mock(AuditService.class);
        service = new ExportTenantService(new ObjectMapper().findAndRegisterModules(),
                clauses, templates, gabarits, dossiers, documents, workflows, audit);
        TenantContext.set(TENANT);

        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(new Clause(), new Clause()));
        when(templates.findByTenantId(TENANT)).thenReturn(List.of());
        when(gabarits.findByTenantIdOrderByOrdreAscNomAsc(TENANT)).thenReturn(List.of());
        when(dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(TENANT)).thenReturn(List.of(new Dossier()));
        when(documents.findByTenantId(TENANT)).thenReturn(List.of());
        when(workflows.findByTenantId(TENANT)).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Map<String, String> lireZip(byte[] archive) throws Exception {
        Map<String, String> entrees = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                entrees.put(e.getName(), new String(zip.readAllBytes()));
            }
        }
        return entrees;
    }

    @Test
    void exporterZip_contientTousLesJeuxDeDonnees_etLeManifeste() throws Exception {
        byte[] archive = service.exporterZip("admin");
        Map<String, String> entrees = lireZip(archive);

        assertTrue(entrees.containsKey("clauses.json"));
        assertTrue(entrees.containsKey("templates.json"));
        assertTrue(entrees.containsKey("gabarits.json"));
        assertTrue(entrees.containsKey("dossiers.json"));
        assertTrue(entrees.containsKey("documents.json"));
        assertTrue(entrees.containsKey("workflows.json"));
        assertTrue(entrees.containsKey("manifeste.json"));

        assertTrue(entrees.get("manifeste.json").contains("\"tenantId\" : 7"));
        assertTrue(entrees.get("manifeste.json").contains("\"clauses.json\" : 2"));
        assertTrue(entrees.get("manifeste.json").contains("\"dossiers.json\" : 1"));

        verify(audit).log(eq("admin"), eq("REVERSIBILITE_EXPORT"), eq("Tenant"), eq(TENANT), any());
    }

    @Test
    void exporterZip_erreurSerialisation_estEnveloppee() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapperKo =
                mock(com.fasterxml.jackson.databind.ObjectMapper.class);
        com.fasterxml.jackson.databind.ObjectWriter writerKo =
                mock(com.fasterxml.jackson.databind.ObjectWriter.class);
        when(mapperKo.writerWithDefaultPrettyPrinter()).thenReturn(writerKo);
        when(writerKo.writeValueAsBytes(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});
        ExportTenantService ko = new ExportTenantService(mapperKo, clauses, templates, gabarits,
                dossiers, documents, workflows, audit);

        assertThrows(java.io.UncheckedIOException.class, () -> ko.exporterZip("admin"));
    }

    /** CHU 6.06 : le socle n'est exporté que s'il est souscrit ; chaque module souscrit verse ses jeux, préfixés. */
    @Test
    void exportUnifie_neRetientQueLesModulesSouscrits() throws Exception {
        var modules = mock(qc.levis.contrat.commun.module.application.ModuleService.class);
        when(modules.estActif(TENANT, "formulaire")).thenReturn(true);
        var formulaires = source("formulaire", "formulaires.json", List.of("F1", "F2"));
        var rh = source("rh", "employes.json", List.of("E1"));
        var unifie = new ExportTenantService(new ObjectMapper(),
                new ExportTenantService.Socle(clauses, templates, gabarits, dossiers, documents, workflows), audit,
                List.of(formulaires, rh), modules);

        Map<String, String> entrees = lireZip(unifie.exporterZip("admin"));

        assertTrue(entrees.containsKey("formulaire/formulaires.json"));
        assertTrue(entrees.get("formulaire/formulaires.json").contains("F2"));
        assertTrue(entrees.get("manifeste.json").contains("\"formulaire/formulaires.json\" : 2"));
        assertTrue(!entrees.containsKey("rh/employes.json"), "module non souscrit");
        assertTrue(!entrees.containsKey("clauses.json"), "socle de rédaction non souscrit");
    }

    private static qc.levis.contrat.commun.reversibilite.application.SourceReversibilite source(String module, String nom, List<?> jeu) {
        return new qc.levis.contrat.commun.reversibilite.application.SourceReversibilite() {
            @Override public String module() { return module; }
            @Override public Map<String, List<?>> jeuxDeDonnees(Long tenant) { return Map.of(nom, jeu); }
        };
    }
}
