package qc.levis.contrat.clm.application.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import qc.levis.contrat.clm.application.DemandesSignatureService;
import qc.levis.contrat.clm.application.ModeleService;
import qc.levis.contrat.clm.domain.ClientIntegration;

/** Outils en lecture seule du connecteur MCP (ADR-023), adossés aux lectures de l'API d'intégration (ADR-011). */
@Configuration
public class OutilsClm {

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    @Bean
    OutilMcp listerModeles(DemandesSignatureService demandes, ModeleService modeles) {
        return outil("lister_modeles", "Modèles de contrat publiés",
                "Liste les modèles de contrat publiés de l'organisation et leurs champs : identifiant, nom, catégorie.",
                Map.of("type", "object", "additionalProperties", false, "properties", Map.of()),
                (client, args) -> Map.of("modeles", demandes.dansOrganisation(client,
                        () -> modeles.lister(demandes.acteurCatalogue(client), "PUBLIE"))));
    }

    @Bean
    OutilMcp lireTrousse(DemandesSignatureService demandes) {
        return outil("lire_trousse", "Statut d'une trousse de signature",
                "Donne le statut d'une trousse de signature créée par ce client d'intégration : référence, signataires, documents.",
                OutilMcp.schemaIdentifiant("trousseId", "Identifiant de la trousse, rendu à sa création."),
                (client, args) -> objet(demandes.trousseDuClient(client, OutilMcp.identifiant(args, "trousseId"))));
    }

    @Bean
    OutilMcp lireLot(DemandesSignatureService demandes) {
        return outil("lire_lot", "Statut d'un lot de signatures",
                "Donne l'avancement d'un lot de signatures créé par ce client d'intégration : total, réussis, échecs.",
                OutilMcp.schemaIdentifiant("lotId", "Identifiant du lot, rendu à sa création."),
                (client, args) -> objet(demandes.lotDuClient(client, OutilMcp.identifiant(args, "lotId"))));
    }

    private static Map<String, Object> objet(Object vue) {
        return JSON.convertValue(vue, new TypeReference<Map<String, Object>>() { });
    }

    private interface Execution {
        Map<String, Object> executer(ClientIntegration client, Map<String, Object> arguments);
    }

    private static OutilMcp outil(String nom, String titre, String description, Map<String, Object> schema, Execution execution) {
        return new OutilMcp() {
            @Override public String nom() { return nom; }
            @Override public String titre() { return titre; }
            @Override public String description() { return description; }
            @Override public Map<String, Object> schemaEntree() { return schema; }
            @Override public Map<String, Object> executer(ClientIntegration client, Map<String, Object> arguments) {
                return execution.executer(client, arguments);
            }
        };
    }
}
