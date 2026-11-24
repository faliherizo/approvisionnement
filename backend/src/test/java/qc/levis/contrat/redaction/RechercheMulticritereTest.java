package qc.levis.contrat.redaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.RechercheMulticritere;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.CriteresRechercheClause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

class RechercheMulticritereTest {

    private final ClauseRepository clauses = mock(ClauseRepository.class);
    private final DocumentRepository documents = mock(DocumentRepository.class);
    private final DocumentTemplateRepository templates = mock(DocumentTemplateRepository.class);
    private final RechercheMulticritere recherche = new RechercheMulticritere(clauses, documents, templates);

    private static Clause clause(String titre, String section, Enums.ClauseCategorie categorie) {
        Clause c = new Clause();
        c.setCode(titre);
        c.setTitre(titre);
        c.setSection(section);
        c.setCategorie(categorie);
        return c;
    }

    @BeforeEach
    void depot() {
        when(clauses.findByActifTrueAndTenantId(7L)).thenReturn(List.of(
                clause("Assurance A", "Assurances", Enums.ClauseCategorie.STANDARD),
                clause("Assurance B", " Assurances ", Enums.ClauseCategorie.OPTIONNELLE),
                clause("Garantie", "Garanties", Enums.ClauseCategorie.STANDARD),
                clause("Libre", null, Enums.ClauseCategorie.STANDARD),
                clause("Vide", "  ", Enums.ClauseCategorie.STANDARD)));
        Document d1 = new Document();
        d1.setTitre("Contrat d'assurance");
        d1.setType(Enums.TypeContrat.APPEL_OFFRES_PUBLIC);
        Document d2 = new Document();
        d2.setTitre("Autre");
        d2.setContexte("assurance en contexte");
        d2.setType(Enums.TypeContrat.GRE_A_GRE);
        Document d3 = new Document();
        d3.setTitre("Rien");
        when(documents.findByTenantIdAndActifTrue(7L)).thenReturn(List.of(d1, d2, d3));
        DocumentTemplate t1 = new DocumentTemplate();
        t1.setCode("T-ASS");
        t1.setType(Enums.TypeContrat.APPEL_OFFRES_PUBLIC);
        DocumentTemplate t2 = new DocumentTemplate();
        t2.setCode("T-2");
        t2.setNom("Modèle");
        t2.setDescription("assurance");
        t2.setType(Enums.TypeContrat.GRE_A_GRE);
        when(templates.findByTenantId(7L)).thenReturn(List.of(t1, t2));
    }

    @Test
    void terme_filtreTout_etCompteLesFacettes() {
        var r = recherche.rechercher(7L, "Assurance", new CriteresRechercheClause("Assurance", null, null, null, false));
        assertThat(r.terme()).isEqualTo("Assurance");
        assertThat(r.clauses()).extracting(Clause::getTitre).containsExactly("Assurance A", "Assurance B");
        assertThat(r.documents()).hasSize(2);
        assertThat(r.templates()).extracting(DocumentTemplate::getCode).containsExactly("T-2");
        assertThat(r.total()).isEqualTo(5);
        assertThat(r.facettesCategorieClause()).containsEntry("STANDARD", 1L).containsEntry("OPTIONNELLE", 1L);
        assertThat(r.facettesThematique()).containsOnly(java.util.Map.entry("Assurances", 2L));
    }

    @Test
    void type_filtreDocumentsEtModeles() {
        var r = recherche.rechercher(7L, "assurance",
                new CriteresRechercheClause("assurance", null, Enums.TypeContrat.GRE_A_GRE, null, false));
        assertThat(r.documents()).extracting(Document::getTitre).containsExactly("Autre");
        assertThat(r.templates()).extracting(DocumentTemplate::getCode).containsExactly("T-2");
    }

    @Test
    void sansTerme_retientTout_etIgnoreLesSectionsAbsentesDansLesFacettes() {
        var r = recherche.rechercher(7L, "", new CriteresRechercheClause("", null, null, null, false));
        assertThat(r.clauses()).hasSize(5);
        assertThat(r.documents()).hasSize(3);
        assertThat(r.facettesThematique()).containsOnlyKeys("Assurances", "Garanties");
    }

    @Test
    void thematiques_distinctes_triees_sansVides() {
        assertThat(recherche.thematiques(7L)).containsExactly("Assurances", "Garanties");
    }
}
