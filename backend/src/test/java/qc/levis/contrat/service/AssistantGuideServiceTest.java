package qc.levis.contrat.service;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.ia.application.AssistantGuideService;
import qc.levis.contrat.ia.application.dto.AssistantContexteRequest;
import qc.levis.contrat.ia.application.dto.AssistantGenerationRequest;
import qc.levis.contrat.ia.application.dto.AssistantSuggestion;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.application.RuleEngine;
import qc.levis.contrat.redaction.application.TemplateService;
import qc.levis.contrat.redaction.application.dto.DocumentRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link AssistantGuideService} (recommandations + génération guidée). */
class AssistantGuideServiceTest {

    private static final Long TENANT = 7L;

    private DocumentTemplateRepository templates;
    private ClauseRepository clauses;
    private TemplateService templateService;
    private DocumentService documentService;
    private DossierService dossierService;
    private AuditService audit;
    private AssistantGuideService service;

    @BeforeEach
    void setUp() {
        templates = mock(DocumentTemplateRepository.class);
        clauses = mock(ClauseRepository.class);
        templateService = mock(TemplateService.class);
        documentService = mock(DocumentService.class);
        dossierService = mock(DossierService.class);
        audit = mock(AuditService.class);
        service = new AssistantGuideService(templates, clauses, new RuleEngine(),
                templateService, documentService, dossierService, audit);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
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

    private Clause clause(Long id, String code, Enums.ClauseCategorie cat, Enums.AttributCondition attr, String valeur) {
        Clause c = new Clause();
        c.setCode(code);
        c.setTitre("Titre " + code);
        c.setCategorie(cat);
        c.setNormative(cat == Enums.ClauseCategorie.STANDARD);
        setId(c, id);
        if (attr != null) {
            ClauseCondition cc = new ClauseCondition();
            cc.setAttribut(attr);
            cc.setValeur(valeur);
            c.addCondition(cc);
        }
        return c;
    }

    private DocumentTemplate template(Long id, String code, Enums.TypeContrat type,
                                      Enums.TypeAcquisition cat, List<String> clauseCodes) {
        DocumentTemplate t = new DocumentTemplate();
        t.setCode(code);
        t.setNom("Modèle " + code);
        t.setType(type);
        t.setCategorieAcquisition(cat);
        t.setClauseCodes(new java.util.ArrayList<>(clauseCodes));
        setId(t, id);
        return t;
    }

    private Document doc(Long id) {
        Document d = new Document();
        setId(d, id);
        return d;
    }

    // --- analyser ---

    @Test
    void analyser_recommandeMode_templatesEtClausesEligibles() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(
                clause(1L, "GEN-001", Enums.ClauseCategorie.STANDARD, null, null),                       // éligible
                clause(2L, "AOP-010", Enums.ClauseCategorie.OPTIONNELLE, Enums.AttributCondition.TYPE_CONTRAT, "GRE_A_GRE"))); // non éligible
        when(templates.findByTenantId(TENANT)).thenReturn(List.of(
                template(10L, "T-EXACT", Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.TypeAcquisition.TECHNOLOGIES, List.of("A", "B")),
                template(11L, "T-TYPE", Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.TypeAcquisition.BIENS, List.of("A")),
                template(12L, "T-CAT", Enums.TypeContrat.GRE_A_GRE, Enums.TypeAcquisition.TECHNOLOGIES, List.of()),
                template(13L, "T-AUCUN", Enums.TypeContrat.GRE_A_GRE, Enums.TypeAcquisition.BIENS, List.of())));

        AssistantSuggestion s = service.analyser(new AssistantContexteRequest(
                Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.TypeAcquisition.TECHNOLOGIES,
                new BigDecimal("200000"), "voirie"));

        assertEquals(Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, s.modeSuggere());

        // Seule la clause sans condition (ou compatible) est éligible.
        assertEquals(1, s.clausesEligibles().size());
        assertEquals("GEN-001", s.clausesEligibles().get(0).code());
        assertTrue(s.clausesEligibles().get(0).normative());

        // 3 templates pertinents (exact + type + catégorie) ; le T-AUCUN est exclu.
        assertEquals(3, s.templatesRecommandes().size());
        assertTrue(s.templatesRecommandes().get(0).correspondanceExacte(), "l'exact doit être en tête");
        assertEquals("T-EXACT", s.templatesRecommandes().get(0).code());
        assertEquals(2, s.templatesRecommandes().get(0).nombreClauses());
    }

    @Test
    void analyser_sansValeur_aucunModeSuggere() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of());
        when(templates.findByTenantId(TENANT)).thenReturn(List.of());
        AssistantSuggestion s = service.analyser(new AssistantContexteRequest(null, null, null, null));
        assertNull(s.modeSuggere());
        assertTrue(s.templatesRecommandes().isEmpty());
        assertTrue(s.clausesEligibles().isEmpty());
    }

    @Test
    void analyser_typeEtCategorieNuls_excluentTousLesTemplates() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of());
        when(templates.findByTenantId(TENANT)).thenReturn(List.of(
                template(10L, "T", Enums.TypeContrat.APPEL_OFFRES_PUBLIC, Enums.TypeAcquisition.TECHNOLOGIES, List.of())));
        // type et catégorie nuls : les deux courts-circuits (!= null → false) sont exercés.
        AssistantSuggestion s = service.analyser(new AssistantContexteRequest(null, null, null, null));
        assertTrue(s.templatesRecommandes().isEmpty());
    }

    // --- generer : chemin template ---

    @Test
    void generer_depuisTemplate_sansDossier() {
        Document produit = doc(50L);
        when(templateService.instancier(eq(20L), any(), eq("user"))).thenReturn(produit);

        Document r = service.generer(new AssistantGenerationRequest(
                "Devis TI", 20L, null, null, new BigDecimal("100000"), "ctx", null, null), "user");

        assertSame(produit, r);
        verify(dossierService, never()).rattacher(any(), any(), any());
        verify(audit).log(eq("user"), eq("ASSISTANT_GENERER"), eq("Document"), eq(50L), eq("template=20"));
    }

    @Test
    void generer_depuisTemplate_avecRattachementDossier() {
        Document produit = doc(50L);
        Document rattache = doc(50L);
        when(templateService.instancier(eq(20L), any(), eq("user"))).thenReturn(produit);
        when(dossierService.rattacher(eq(9L), eq(50L), eq("user"))).thenReturn(rattache);

        Document r = service.generer(new AssistantGenerationRequest(
                "Devis TI", 20L, null, null, null, null, 9L, null), "user");

        assertSame(rattache, r);
        verify(dossierService).rattacher(9L, 50L, "user");
    }

    // --- generer : chemin manuel ---

    @Test
    void generer_manuel_modeExplicite_avecDossier() {
        Document cree = doc(60L);
        Document assemble = doc(60L);
        when(documentService.creer(any(), eq("user"))).thenReturn(cree);
        when(documentService.assembler(eq(60L), anyList(), eq("user"))).thenReturn(assemble);

        Document r = service.generer(new AssistantGenerationRequest(
                "Contrat gré à gré", null, Enums.TypeContrat.GRE_A_GRE, Enums.ModeSollicitation.GRE_A_GRE,
                new BigDecimal("5000"), "ctx", 9L, List.of(1L, 2L)), "user");

        assertSame(assemble, r);
        ArgumentCaptor<DocumentRequest> captor = ArgumentCaptor.forClass(DocumentRequest.class);
        verify(documentService).creer(captor.capture(), eq("user"));
        assertEquals(Enums.ModeSollicitation.GRE_A_GRE, captor.getValue().modeSollicitation());
        assertEquals(9L, captor.getValue().dossierId());
        verify(documentService).assembler(60L, List.of(1L, 2L), "user");
        verify(audit).log(eq("user"), eq("ASSISTANT_GENERER"), eq("Document"), eq(60L), eq("manuel"));
    }

    @Test
    void generer_manuel_modeDeduitDeLaValeur() {
        Document cree = doc(61L);
        when(documentService.creer(any(), eq("user"))).thenReturn(cree);
        when(documentService.assembler(eq(61L), any(), eq("user"))).thenReturn(cree);

        service.generer(new AssistantGenerationRequest(
                "Appel d'offres", null, Enums.TypeContrat.APPEL_OFFRES_PUBLIC, null,
                new BigDecimal("500000"), "ctx", null, null), "user");

        ArgumentCaptor<DocumentRequest> captor = ArgumentCaptor.forClass(DocumentRequest.class);
        verify(documentService).creer(captor.capture(), eq("user"));
        // Mode non fourni → déduit par le moteur de règles (valeur ≥ seuil AOP).
        assertEquals(Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, captor.getValue().modeSollicitation());
    }
}
