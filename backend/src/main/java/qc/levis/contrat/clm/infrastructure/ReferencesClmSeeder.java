package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.DeclarationCategorie;

/**
 * Déclare les listes de référence du module ({@code data/clm-references.json}) et leurs valeurs de plateforme. Chaque
 * organisation les adapte ensuite depuis son administration ; une valeur modifiée n'est jamais réécrite au démarrage.
 */
@Component
@Order(6)
public class ReferencesClmSeeder implements CommandLineRunner {

    private final ReferenceService references;
    private final ObjectMapper json = new ObjectMapper();

    public ReferencesClmSeeder(ReferenceService references) {
        this.references = references;
    }

    @Override
    public void run(String... args) throws IOException {
        try (InputStream in = new ClassPathResource("data/clm-references.json").getInputStream()) {
            List<DeclarationCategorie> listes = json.readValue(in,
                    json.getTypeFactory().constructCollectionType(List.class, DeclarationCategorie.class));
            listes.forEach(references::declarer);
        }
    }
}
