package qc.levis.contrat.integration.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.IntegrationSapService.ConfigSap;
import qc.levis.contrat.integration.application.IntegrationSapService;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort;

/**
 * Transmet la référence financière d'un dossier vers <b>SAP</b> (service OData, POST JSON), en tenant
 * compte de la configuration <b>propre au tenant courant</b> (Paramètres → Intégration SAP). Si le
 * tenant a une configuration <em>active et complète</em>, le dépôt est réel ; sinon on retombe sur la
 * <b>simulation</b> ({@link SimulationSapAdapter}). Bean {@code @Primary} : c'est ce routeur qui est
 * injecté (le code appelant ne change pas, ADR-002). Aucun secret n'est journalisé.
 */
@Component
@Primary
public class SapODataAdapter implements ReferenceFinancierePort {

    private static final Logger log = LoggerFactory.getLogger(SapODataAdapter.class);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final ObjectMapper json;
    private final IntegrationSapService config;
    private final SimulationSapAdapter simulation;

    private final qc.levis.contrat.commun.infrastructure.reseau.GardeSortante garde;

    public SapODataAdapter(ObjectMapper json, IntegrationSapService config, SimulationSapAdapter simulation) {
        this(json, config, simulation, qc.levis.contrat.commun.infrastructure.reseau.GardeSortante.aucune());
    }

    /** L'URL SAP est saisie par l'organisation : elle passe la garde anti-SSRF avant tout appel. */
    @org.springframework.beans.factory.annotation.Autowired
    public SapODataAdapter(ObjectMapper json, IntegrationSapService config, SimulationSapAdapter simulation, qc.levis.contrat.commun.infrastructure.reseau.GardeSortante garde) {
        this.json = json;
        this.config = config;
        this.simulation = simulation;
        this.garde = garde;
    }

    @Override
    public ResultatPublication deposer(ReferenceFinanciere r) {
        ConfigSap cfg = config.configClient(TenantContext.get()).orElse(null);
        if (cfg == null) {
            return simulation.deposer(r); // pas de config SAP active pour ce tenant → simulation
        }
        String corps = "{\"NumeroAO\":" + s(r.numeroAO()) + ",\"Objet\":" + s(r.objet())
                + ",\"NombreDocuments\":" + r.nombreDocuments()
                + ",\"MontantTotal\":" + s(r.montantTotal() == null ? "0" : r.montantTotal().toPlainString()) + "}";
        HttpRequest.Builder req = HttpRequest.newBuilder(garde.exiger(cfg.baseUrl() + cfg.servicePath()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corps, StandardCharsets.UTF_8));
        appliquerAuth(req, cfg);
        try {
            HttpResponse<String> resp = http.send(req.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                log.error("Dépôt SAP refusé ({}) pour le dossier {} : {}",
                        resp.statusCode(), r.dossierId(), resp.body());
                return new ResultatPublication(false, null, "SAP " + resp.statusCode());
            }
            String ref = reference(resp.body(), r.numeroAO(), r.dossierId());
            log.info("Référence transmise à SAP : dossier {} → {}", r.dossierId(), ref);
            return new ResultatPublication(true, "sap://" + ref, "Référence transmise à SAP (OData).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultatPublication(false, null, "Échec SAP : requête interrompue.");
        } catch (Exception e) {
            log.error("Échec du dépôt SAP pour le dossier {} : {}", r.dossierId(), e.getMessage());
            return new ResultatPublication(false, null, "Échec SAP : " + e.getMessage());
        }
    }

    /**
     * Test de connexion : lecture ({@code GET …?$top=1}) du service avec les identifiants configurés.
     * Fonctionne même contre un service en lecture seule (bac à sable SAP) : un 2xx confirme
     * l'authentification et l'URL.
     */
    public ResultatPublication tester(ConfigSap cfg) {
        String sep = cfg.servicePath() != null && cfg.servicePath().contains("?") ? "&" : "?";
        HttpRequest.Builder req = HttpRequest.newBuilder(
                        garde.exiger(cfg.baseUrl() + cfg.servicePath() + sep + "$top=1"))
                .header("Accept", "application/json").GET();
        appliquerAuth(req, cfg);
        try {
            HttpResponse<String> resp = http.send(req.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 == 2) {
                return new ResultatPublication(true, null, "Connexion SAP réussie (" + resp.statusCode() + ").");
            }
            return new ResultatPublication(false, null,
                    "SAP a répondu " + resp.statusCode() + " (authentification ou URL à vérifier).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultatPublication(false, null, "Test SAP interrompu.");
        } catch (Exception e) {
            return new ResultatPublication(false, null, "Échec de connexion SAP : " + e.getMessage());
        }
    }

    private void appliquerAuth(HttpRequest.Builder req, ConfigSap cfg) {
        String mode = cfg.auth() == null ? "basic" : cfg.auth().toLowerCase(Locale.ROOT);
        if ("apikey".equals(mode) && cfg.apiKey() != null && !cfg.apiKey().isBlank()) {
            req.header("APIKey", cfg.apiKey());
        } else if ("basic".equals(mode) && cfg.username() != null && !cfg.username().isBlank()) {
            String creds = Base64.getEncoder().encodeToString(
                    (cfg.username() + ":" + cfg.password()).getBytes(StandardCharsets.UTF_8));
            req.header("Authorization", "Basic " + creds);
        }
    }

    /** Extrait l'identifiant de référence de la réponse OData ; repli sur le n° AO / l'id de dossier. */
    private String reference(String corps, String numeroAO, Long dossierId) {
        try {
            JsonNode racine = json.readTree(corps);
            JsonNode d = racine.has("d") ? racine.get("d") : racine;
            for (String champ : new String[]{"Reference", "ReferenceId", "id"}) {
                JsonNode n = d.get(champ);
                if (n != null && !n.isNull() && !n.asText().isBlank()) return n.asText();
            }
        } catch (Exception ignore) {
            // réponse non JSON : on retombe sur le repli
        }
        return (numeroAO != null && !numeroAO.isBlank()) ? numeroAO : ("dossier-" + dossierId);
    }

    private String s(String v) {
        return "\"" + (v == null ? "" : v.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"";
    }
}
