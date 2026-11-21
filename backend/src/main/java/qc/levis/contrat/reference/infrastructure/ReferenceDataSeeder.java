package qc.levis.contrat.reference.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.domain.Enums.*;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.DocumentTemplate;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;
import static qc.levis.contrat.tenant.infrastructure.TenantSeeder.DEFAULT_SLUG;

/** Amorce les données de référence (clauses, templates) à partir de fichiers JSON. */
@Component
@Order(3)
public class ReferenceDataSeeder implements CommandLineRunner {

    public record ConditionSeed(String attribut, String valeur) {}
    public record ClauseSeed(String code, String titre, String categorie, String corps,
                             String numero, String section,
                             List<ConditionSeed> conditions,
                             List<String> dependances, List<String> exclusions, Boolean normative) {}
    public record TemplateSeed(String code, String nom, String type, String modeSollicitation,
                               String description, List<String> clauseCodes) {}

    private final ClauseRepository clauses;
    private final DocumentTemplateRepository templates;
    private final TenantRepository tenants;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReferenceDataSeeder(ClauseRepository clauses, DocumentTemplateRepository templates,
                               TenantRepository tenants) {
        this.clauses = clauses;
        this.templates = templates;
        this.tenants = tenants;
    }

    @Override
    public void run(String... args) throws Exception {
        Long tenant = tenants.findBySlug(DEFAULT_SLUG).map(Tenant::getId).orElse(null);
        if (clauses.count() == 0) {
            for (ClauseSeed s : lire("data/clauses.json", ClauseSeed.class)) {
                Clause c = versClause(s);
                c.setTenantId(tenant);
                clauses.save(c);
            }
        }
        if (templates.count() == 0) {
            for (TemplateSeed s : lire("data/templates.json", TemplateSeed.class)) {
                DocumentTemplate t = versTemplate(s);
                t.setTenantId(tenant);
                templates.save(t);
            }
        }
    }

    private <T> List<T> lire(String chemin, Class<T> type) throws Exception {
        try (InputStream in = new ClassPathResource(chemin).getInputStream()) {
            return mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, type));
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
