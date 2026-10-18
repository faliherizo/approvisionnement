package qc.levis.contrat.ia.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import qc.levis.contrat.ia.application.repository.KnowledgeRepository;
import qc.levis.contrat.ia.domain.KnowledgeSnippet;

/**
 * Amorce le socle de connaissances global (tenant_id nul) spécialisant l'assistant IA
 * en rédaction contractuelle de marchés publics québécois (LCOP, CCAG, SCT). Ce socle
 * est hérité par tous les tenants via le RAG. Idempotent.
 */
@Component
@Order(6)
public class KnowledgeSeeder implements CommandLineRunner {

    public record SnippetSeed(String titre, String source, String contenu) {}

    private final KnowledgeRepository knowledge;
    private final ObjectMapper mapper = new ObjectMapper();

    public KnowledgeSeeder(KnowledgeRepository knowledge) {
        this.knowledge = knowledge;
    }

    @Override
    public void run(String... args) throws Exception {
        if (knowledge.countByTenantIdIsNull() > 0) return;
        try (InputStream in = new ClassPathResource("data/knowledge.json").getInputStream()) {
            List<SnippetSeed> graines = mapper.readValue(in,
                    mapper.getTypeFactory().constructCollectionType(List.class, SnippetSeed.class));
            for (SnippetSeed g : graines) {
                KnowledgeSnippet s = new KnowledgeSnippet();
                s.setTenantId(null);
                s.setTitre(g.titre());
                s.setSource(g.source());
                s.setContenu(g.contenu());
                knowledge.save(s);
            }
        }
    }
}
