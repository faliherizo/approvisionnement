package qc.levis.contrat.redaction.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.domain.Enums.*;
import qc.levis.contrat.redaction.application.port.BibliothequeDeDepart;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.DocumentTemplate;

/** Bibliothèque de départ lue dans {@code data/clauses.json} et {@code data/templates.json} (ressources de la plateforme). */
@Component
public class BibliothequeDeDepartJson implements BibliothequeDeDepart {

    public record ConditionSeed(String attribut, String valeur) {}
    public record ClauseSeed(String code, String titre, String categorie, String corps,
                             String numero, String section,
                             List<ConditionSeed> conditions,
                             List<String> dependances, List<String> exclusions, Boolean normative) {}
    public record TemplateSeed(String code, String nom, String type, String modeSollicitation,
                               String description, List<String> clauseCodes) {}

    private final ClauseRepository clauses;
    private final DocumentTemplateRepository templates;
    private final ObjectMapper mapper = new ObjectMapper();

    public BibliothequeDeDepartJson(ClauseRepository clauses, DocumentTemplateRepository templates) {
        this.clauses = clauses;
        this.templates = templates;
    }

    @Override
    @Transactional
    public void amorcer(Long tenantId) {
        // Sans organisation (premier démarrage sans organisation par défaut), on garde la règle d'origine : base vide.
        boolean sansClause = tenantId == null ? clauses.count() == 0 : clauses.findByActifTrueAndTenantId(tenantId).isEmpty();
        boolean sansTemplate = tenantId == null ? templates.count() == 0 : templates.findByTenantId(tenantId).isEmpty();
        if (sansClause) {
            for (ClauseSeed s : lire("data/clauses.json", ClauseSeed.class)) {
                Clause c = versClause(s);
                c.setTenantId(tenantId);
                clauses.save(c);
            }
        }
        if (sansTemplate) {
            for (TemplateSeed s : lire("data/templates.json", TemplateSeed.class)) {
                DocumentTemplate t = versTemplate(s);
                t.setTenantId(tenantId);
                templates.save(t);
            }
        }
    }

    private <T> List<T> lire(String chemin, Class<T> type) {
        try (InputStream in = new ClassPathResource(chemin).getInputStream()) {
            return mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new UncheckedIOException("Ressource de la plateforme illisible : " + chemin, e);
        }
    }

    private Clause versClause(ClauseSeed s) {
        Clause c = new Clause();
        c.setCode(s.code());
        c.setTitre(s.titre());
        // Clause exigée par la loi (Charte de la langue française, LCOP) : verrouillée dans les documents.
        c.setNormative(Boolean.TRUE.equals(s.normative()));
        c.setCategorie(ClauseCategorie.valueOf(s.categorie()));
        c.setCorps(s.corps());
        c.setNumero(s.numero());
        c.setSection(s.section());
        if (s.conditions() != null) {
            for (ConditionSeed cond : s.conditions()) {
                ClauseCondition cc = new ClauseCondition();
                cc.setAttribut(AttributCondition.valueOf(cond.attribut()));
                cc.setValeur(cond.valeur());
                c.addCondition(cc);
            }
        }
        if (s.dependances() != null) c.setDependances(new ArrayList<>(s.dependances()));
        if (s.exclusions() != null) c.setExclusions(new ArrayList<>(s.exclusions()));
        return c;
    }

    private DocumentTemplate versTemplate(TemplateSeed s) {
        DocumentTemplate t = new DocumentTemplate();
        t.setCode(s.code());
        t.setNom(s.nom());
        t.setType(TypeContrat.valueOf(s.type()));
        if (s.modeSollicitation() != null) {
            t.setModeSollicitation(ModeSollicitation.valueOf(s.modeSollicitation()));
        }
        t.setDescription(s.description());
        t.setClauseCodes(new ArrayList<>(s.clauseCodes() != null ? s.clauseCodes() : List.of()));
        return t;
    }
}
