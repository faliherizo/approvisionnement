package qc.levis.contrat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.redaction.application.ClauseService;
import qc.levis.contrat.redaction.application.ImportService;
import qc.levis.contrat.redaction.application.TemplateService;
import qc.levis.contrat.redaction.application.dto.ClauseRequest;
import qc.levis.contrat.redaction.application.dto.ImportRapport;
import qc.levis.contrat.redaction.application.dto.ImportWordRapport;
import qc.levis.contrat.redaction.application.dto.TemplateRequest;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.DocumentTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link ImportService} (analyse CSV, validation, migration d'essai vs réelle). */
class ImportServiceTest {

    private ClauseService clauses;
    private TemplateService templates;
    private qc.levis.contrat.redaction.infrastructure.WordTemplateParser wordParser;
    private AuditService audit;
    private ImportService service;

    private static final String ENTETE = "code,titre,categorie,corps\n";

    @BeforeEach
    void setUp() {
        clauses = mock(ClauseService.class);
        templates = mock(TemplateService.class);
        wordParser = mock(qc.levis.contrat.redaction.infrastructure.WordTemplateParser.class);
        audit = mock(AuditService.class);
        service = new ImportService(clauses, templates, wordParser, audit);
    }

    @Test
    void analyser_valideSansPersister() {
        String csv = ENTETE
                + "GEN-1,Objet,STANDARD,Le corps\n"
                + "GEN-2,Durée,optionnelle,Autre corps\n"; // catégorie insensible à la casse

        ImportRapport r = service.analyser(csv);

        assertTrue(r.essai());
        assertEquals(2, r.lignesLues());
        assertEquals(2, r.valides());
        assertEquals(0, r.importes());
        assertTrue(r.erreurs().isEmpty());
        verify(clauses, never()).creer(any(), any());
    }

    @Test
    void importer_persisteLesLignesValides() {
        String csv = ENTETE + "GEN-1,Objet,STANDARD,Corps\n";
        ImportRapport r = service.importer(csv, "admin");
        assertFalse(r.essai());
        assertEquals(1, r.importes());
        verify(clauses).creer(any(ClauseRequest.class), any());
        verify(audit).log(eq("admin"), eq("IMPORT_CLAUSES"), eq("Clause"), isNull(), contains("1 importées"));
    }

    @Test
    void validation_signaleLesErreurs_parLigne() {
        String csv = ENTETE
                + "GEN-1,Titre,STANDARD,Corps\n"       // valide (ligne 2)
                + ",SansCode,STANDARD,Corps\n"          // code manquant (ligne 3)
                + "GEN-3,Titre,INCONNUE,Corps\n"        // catégorie invalide (ligne 4)
                + "GEN-4,Titre\n";                       // colonnes insuffisantes (ligne 5)

        ImportRapport r = service.analyser(csv);

        assertEquals(4, r.lignesLues());
        assertEquals(1, r.valides());
        assertEquals(3, r.erreurs().size());
        assertEquals(3, r.erreurs().get(0).ligne());
        assertTrue(r.erreurs().get(1).message().contains("Catégorie invalide"));
        assertTrue(r.erreurs().get(2).message().contains("Colonnes insuffisantes"));
    }

    @Test
    void categorieVide_utiliseStandard_etColonnesOptionnelles() {
        String csv = "code,titre,categorie,corps,numero,section\n"
                + "GEN-1,Titre,,Corps,1.1,Généralités\n";
        ImportRapport r = service.analyser(csv);
        assertEquals(1, r.valides());
        assertTrue(r.erreurs().isEmpty());
    }

    @Test
    void csv_gereGuillemets_virgulesInternes_etDoubleGuillemets() {
        String csv = ENTETE
                + "\"GEN-1\",\"Titre, complet\",STANDARD,\"Corps avec \"\"citation\"\" et, virgule\"\n";
        ImportRapport r = service.analyser(csv);
        assertEquals(1, r.lignesLues());
        assertEquals(1, r.valides(), "les virgules et guillemets internes ne cassent pas l'analyse");
    }

    @Test
    void csv_gereCRLF_etSautDeLigneInterne() {
        String csv = "code,titre,categorie,corps\r\n"
                + "GEN-1,Titre,STANDARD,\"ligne1\nligne2\"\r\n";
        ImportRapport r = service.analyser(csv);
        assertEquals(1, r.valides());
    }

    @Test
    void csv_vide_ouEnteteSeule_neLitAucuneLigne() {
        assertEquals(0, service.analyser("").lignesLues());
        assertEquals(0, service.analyser("   ").lignesLues());
        assertEquals(0, service.analyser("code,titre,categorie,corps").lignesLues());
    }

    // --- Import de modèle Word (.docx) ---

    private qc.levis.contrat.redaction.domain.DocumentTemplate modele(Long id, String nom) {
        var t = new qc.levis.contrat.redaction.domain.DocumentTemplate();
        t.setNom(nom);
        try {
            var f = qc.levis.contrat.redaction.domain.DocumentTemplate.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(t, id);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        return t;
    }

    @Test
    void analyserWord_detecteLesSections_sansPersister() {
        when(wordParser.extraire(any())).thenReturn(java.util.List.of(
                new qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord("Objet", "corps"),
                new qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord("Garantie", "12 mois")));
        ImportWordRapport r = service.analyserWord(new byte[]{1});
        assertTrue(r.essai());
        assertEquals(2, r.sectionsDetectees());
        assertEquals(0, r.clausesCreees());
        assertTrue(r.erreurs().isEmpty());
        verify(clauses, never()).creer(any(), any());
        verify(templates, never()).creer(any());
    }

    @Test
    void analyserWord_documentSansSection_signaleUneErreur() {
        when(wordParser.extraire(any())).thenReturn(java.util.List.of());
        ImportWordRapport r = service.analyserWord(new byte[]{1});
        assertEquals(0, r.sectionsDetectees());
        assertEquals(1, r.erreurs().size());
    }

    @Test
    void importerWord_creeUneClauseParSection_puisUnModele() {
        when(wordParser.extraire(any())).thenReturn(java.util.List.of(
                new qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord("Objet", "corps"),
                new qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord(null, "")));  // titre/corps vides → défauts
        when(templates.creer(any())).thenReturn(modele(42L, "Modèle Ville"));

        ImportWordRapport r = service.importerWord(new byte[]{1}, "Modèle Ville",
                qc.levis.contrat.commun.domain.Enums.TypeContrat.APPEL_OFFRES_PUBLIC, "admin");

        assertFalse(r.essai());
        assertEquals(2, r.sectionsDetectees());
        assertEquals(2, r.clausesCreees());
        assertEquals(42L, r.templateId());
        verify(clauses, times(2)).creer(any(ClauseRequest.class), any());
        verify(templates).creer(any(TemplateRequest.class));
        verify(audit).log(eq("admin"), eq("IMPORT_WORD"), eq("DocumentTemplate"), eq(42L), any());
    }

    @Test
    void importerWord_documentVide_neCreeRien() {
        when(wordParser.extraire(any())).thenReturn(java.util.List.of());
        ImportWordRapport r = service.importerWord(new byte[]{1}, "X", null, "admin");
        assertEquals(0, r.clausesCreees());
        assertNull(r.templateId());
        verify(clauses, never()).creer(any(), any());
        verify(templates, never()).creer(any());
    }
}
