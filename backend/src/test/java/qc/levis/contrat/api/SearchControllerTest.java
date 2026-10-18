package qc.levis.contrat.api;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.RechercheResultat;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;
import qc.levis.contrat.redaction.infrastructure.web.SearchController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link SearchController} : couverture des filtres, facettes et branches. */
class SearchControllerTest {

    private static final Long TENANT = 7L;

    private ClauseRepository clauses;
    private DocumentRepository documents;
    private DocumentTemplateRepository templates;
    private SearchController controller;

    @BeforeEach
    void setUp() {
        clauses = mock(ClauseRepository.class);
        documents = mock(DocumentRepository.class);
        templates = mock(DocumentTemplateRepository.class);
        controller = new SearchController(clauses, documents, templates);
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Clause clause(String code, String titre, String corps, Enums.ClauseCategorie cat) {
        Clause c = new Clause();
        c.setCode(code);
        c.setTitre(titre);
        c.setCorps(corps);
        c.setCategorie(cat);
        return c;
    }

    private Document document(String titre, String contexte, Enums.TypeContrat type) {
        Document d = new Document();
        d.setTitre(titre);
        d.setContexte(contexte);
        d.setType(type);
        return d;
    }

    private DocumentTemplate template(String code, String nom, String description, Enums.TypeContrat type) {
        DocumentTemplate t = new DocumentTemplate();
        t.setCode(code);
        t.setNom(nom);
        t.setDescription(description);
        t.setType(type);
        return t;
    }

    @Test
    void recherche_matchParCode_titre_corps_etFacettes() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(
                clause("ALPHA-1", "Titre", "corps", Enums.ClauseCategorie.STANDARD),      // match par code
                clause("X", "porte ALPHA ouverte", "z", Enums.ClauseCategorie.OPTIONNELLE), // match par titre
                clause("Y", "titre", "texte ALPHA final", Enums.ClauseCategorie.STANDARD),  // match par corps
                clause("Z", "rien", "rien", Enums.ClauseCategorie.PERSONNALISEE)));         // aucun match
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of());
        when(templates.findByTenantId(TENANT)).thenReturn(List.of());

        RechercheResultat r = controller.rechercher("alpha", null, null);

        assertEquals(3, r.clauses().size());
        assertEquals(2L, r.facettesCategorieClause().get("STANDARD"));
        assertEquals(1L, r.facettesCategorieClause().get("OPTIONNELLE"));
        assertEquals(3, r.total());
    }

    @Test
    void recherche_filtreCategorieClause() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(
                clause("A", "alpha", "c", Enums.ClauseCategorie.STANDARD),
                clause("B", "alpha", "c", Enums.ClauseCategorie.OPTIONNELLE)));
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of());
        when(templates.findByTenantId(TENANT)).thenReturn(List.of());

        RechercheResultat r = controller.rechercher("alpha", Enums.ClauseCategorie.OPTIONNELLE, null);

        assertEquals(1, r.clauses().size());
        assertEquals(Enums.ClauseCategorie.OPTIONNELLE, r.clauses().get(0).getCategorie());
    }

    @Test
    void recherche_documents_parTitre_contexte_etFiltreType() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of());
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of(
                document("Beta un", null, Enums.TypeContrat.APPEL_OFFRES_PUBLIC),      // match titre
                document("autre", "contient beta", Enums.TypeContrat.GRE_A_GRE),        // match contexte, filtré par type
                document("sans", "sans", Enums.TypeContrat.APPEL_OFFRES_PUBLIC)));      // aucun match
        when(templates.findByTenantId(TENANT)).thenReturn(List.of());

        RechercheResultat sansFiltre = controller.rechercher("beta", null, null);
        assertEquals(2, sansFiltre.documents().size());

        RechercheResultat filtre = controller.rechercher("beta", null, Enums.TypeContrat.APPEL_OFFRES_PUBLIC);
        assertEquals(1, filtre.documents().size());
    }

    @Test
    void recherche_templates_parCode_nom_description_etFiltreType() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of());
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of());
        when(templates.findByTenantId(TENANT)).thenReturn(List.of(
                template("GAMMA-1", "nom", "desc", Enums.TypeContrat.APPEL_OFFRES_PUBLIC),  // code
                template("X", "gamma modele", "desc", Enums.TypeContrat.APPEL_OFFRES_PUBLIC), // nom
                template("Y", "nom", "gamma inclus", Enums.TypeContrat.GRE_A_GRE),            // description, filtré
                template("Z", "nom", "desc", Enums.TypeContrat.GRE_A_GRE)));                  // aucun match

        RechercheResultat sansFiltre = controller.rechercher("gamma", null, null);
        assertEquals(3, sansFiltre.templates().size());

        RechercheResultat filtre = controller.rechercher("gamma", null, Enums.TypeContrat.APPEL_OFFRES_PUBLIC);
        assertEquals(2, filtre.templates().size());
    }

    @Test
    void recherche_termeVide_retourneTout() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(
                clause("A", "t", "c", Enums.ClauseCategorie.STANDARD)));
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of(
                document("d", "x", Enums.TypeContrat.GRE_A_GRE)));
        when(templates.findByTenantId(TENANT)).thenReturn(List.of(
                template("t", "n", "d", Enums.TypeContrat.GRE_A_GRE)));

        RechercheResultat r = controller.rechercher(null, null, null);
        assertEquals(3, r.total());
        assertNull(r.terme(), "terme reflète la valeur brute reçue (null ici)");
    }

    @Test
    void recherche_champsNuls_neCassentPas() {
        when(clauses.findByActifTrueAndTenantId(TENANT)).thenReturn(List.of(
                clause(null, null, null, Enums.ClauseCategorie.STANDARD)));
        when(documents.findByTenantIdAndActifTrue(TENANT)).thenReturn(List.of(
                document(null, null, Enums.TypeContrat.GRE_A_GRE)));
        when(templates.findByTenantId(TENANT)).thenReturn(List.of(
                template(null, null, null, Enums.TypeContrat.GRE_A_GRE)));

        RechercheResultat r = controller.rechercher("introuvable", null, null);
        assertEquals(0, r.total());
    }
}
