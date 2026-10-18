package qc.levis.contrat.commun.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Métadonnées OpenAPI 3.0 de l'API. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI apiContractuelle() {
        return new OpenAPI().info(new Info()
                .title("API — Logiciel de rédaction contractuelle")
                .description("Ville de Lévis · Avis # 2026-50-92. Rédaction de clauses, "
                        + "assemblage, approbation, addendas, signature électronique, IA.")
                .version("0.1.0")
                .contact(new Contact().name("Ville de Lévis"))
                .license(new License().name("Usage municipal")));
    }
}
