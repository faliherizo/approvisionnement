package qc.levis.contrat.vitrine.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vitrine.application.VitrineService;
import qc.levis.contrat.vitrine.application.dto.VitrineDto;

/**
 * Vitrine de départ de chaque organisation de démonstration (data/vitrines.json), créée une seule fois : le
 * superadministrateur la modifie ensuite à sa guise, et rien ne l'écrase au redémarrage.
 */
@Component
@Order(9)
@ConditionalOnProperty(prefix = "app.amorcage", name = "tenants-demo", havingValue = "true")
public class VitrinesSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VitrinesSeeder.class);

    private final VitrineService vitrines;
    private final ObjectMapper json = new ObjectMapper();

    public VitrinesSeeder(VitrineService vitrines) {
        this.vitrines = vitrines;
    }

    @Override
    public void run(String... args) throws IOException {
        try (InputStream in = new ClassPathResource("data/vitrines.json").getInputStream()) {
            Map<String, VitrineDto.Requete> parOrganisation = json.readValue(in, new TypeReference<>() {});
            parOrganisation.forEach((organisation, req) -> {
                if (vitrines.creerSiAbsente(organisation, req, "amorçage")) {
                    log.info("Vitrine de départ créée : {}", organisation);
                }
            });
        }
    }
}
