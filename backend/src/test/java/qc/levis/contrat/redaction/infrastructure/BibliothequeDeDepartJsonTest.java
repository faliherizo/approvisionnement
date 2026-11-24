package qc.levis.contrat.redaction.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/** Bibliothèque de départ : recopiée chez une organisation vide, jamais doublée chez une organisation qui en a une. */
class BibliothequeDeDepartJsonTest {

    private final ClauseRepository clauses = mock(ClauseRepository.class);
    private final DocumentTemplateRepository templates = mock(DocumentTemplateRepository.class);
    private final BibliothequeDeDepartJson bibliotheque = new BibliothequeDeDepartJson(clauses, templates);

    private static JsonNode ressource(String chemin) throws Exception {
        try (var in = new ClassPathResource(chemin).getInputStream()) { return new ObjectMapper().readTree(in); }
    }

    @Test
    void une_organisation_vide_recoit_toutes_les_clauses_et_tous_les_templates_a_son_nom() throws Exception {
        when(clauses.findByActifTrueAndTenantId(5L)).thenReturn(List.of());
        when(templates.findByTenantId(5L)).thenReturn(List.of());

        bibliotheque.amorcer(5L);

        ArgumentCaptor<Clause> c = ArgumentCaptor.forClass(Clause.class);
        verify(clauses, org.mockito.Mockito.times(ressource("data/clauses.json").size())).save(c.capture());
        assertTrue(c.getAllValues().stream().allMatch(x -> Long.valueOf(5L).equals(x.getTenantId())));
        ArgumentCaptor<DocumentTemplate> t = ArgumentCaptor.forClass(DocumentTemplate.class);
        verify(templates, org.mockito.Mockito.times(ressource("data/templates.json").size())).save(t.capture());
        assertTrue(t.getAllValues().stream().allMatch(x -> Long.valueOf(5L).equals(x.getTenantId())));
    }

    @Test
    void la_conversion_garde_conditions_dependances_et_caractere_normatif() throws Exception {
        when(clauses.findByActifTrueAndTenantId(5L)).thenReturn(List.of());
        when(templates.findByTenantId(5L)).thenReturn(List.of(new DocumentTemplate()));

        bibliotheque.amorcer(5L);

        ArgumentCaptor<Clause> c = ArgumentCaptor.forClass(Clause.class);
        verify(clauses, org.mockito.Mockito.atLeastOnce()).save(c.capture());
        JsonNode source = ressource("data/clauses.json");
        for (int i = 0; i < source.size(); i++) {
            JsonNode s = source.get(i);
            Clause x = c.getAllValues().get(i);
            assertEquals(s.get("code").asText(), x.getCode());
            assertEquals(s.path("normative").asBoolean(false), x.isNormative());
            assertEquals(s.path("conditions").size(), x.getConditions().size());
            assertEquals(s.path("dependances").size(), x.getDependances() == null ? 0 : x.getDependances().size());
        }
        assertTrue(c.getAllValues().stream().anyMatch(x -> x.isNormative()), "des clauses légales sont normatives");
        verify(templates, never()).save(any());
    }

    @Test
    void une_organisation_qui_a_sa_bibliotheque_n_en_recoit_pas_une_seconde() {
        when(clauses.findByActifTrueAndTenantId(5L)).thenReturn(List.of(new Clause()));
        when(templates.findByTenantId(5L)).thenReturn(List.of(new DocumentTemplate()));

        bibliotheque.amorcer(5L);

        verify(clauses, never()).save(any());
        verify(templates, never()).save(any());
    }

    @Test
    void sans_organisation_la_regle_d_origine_vaut_base_vide_seulement() {
        when(clauses.count()).thenReturn(3L);
        when(templates.count()).thenReturn(0L);

        bibliotheque.amorcer(null);

        verify(clauses, never()).save(any());
        ArgumentCaptor<DocumentTemplate> t = ArgumentCaptor.forClass(DocumentTemplate.class);
        verify(templates, org.mockito.Mockito.atLeastOnce()).save(t.capture());
        assertFalse(t.getAllValues().isEmpty());
    }
}
