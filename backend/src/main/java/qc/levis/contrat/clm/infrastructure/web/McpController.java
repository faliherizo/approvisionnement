package qc.levis.contrat.clm.infrastructure.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.ClientsIntegrationService;
import qc.levis.contrat.clm.application.mcp.ServeurMcp;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.commun.module.application.ModuleService;

/**
 * Connecteur MCP du module CLM (IQ-A-09, ADR-023), transport « Streamable HTTP » en réponse JSON. Un assistant d'IA s'y
 * connecte avec le jeton d'un client d'intégration ; il ne lit que ce que l'API d'intégration permet à ce client. Une
 * origine étrangère (attaque par « DNS rebinding ») est refusée par la politique CORS de la plateforme, avant ce contrôleur.
 */
@RestController
@RequestMapping("/api/clm/integration/mcp")
public class McpController {

    private final ServeurMcp serveur;
    private final ClientsIntegrationService clients;
    private final ModuleService modules;
    private final ObjectMapper json;

    public McpController(ServeurMcp serveur, ClientsIntegrationService clients, ModuleService modules, ObjectMapper json) {
        this.serveur = serveur;
        this.clients = clients;
        this.modules = modules;
        this.json = json;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> recevoir(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authz,
                                           @RequestHeader(value = "MCP-Protocol-Version", required = false) String version,
                                           @RequestBody(required = false) String corps) {
        if (version != null && !ServeurMcp.VERSIONS.contains(version)) {
            return ResponseEntity.badRequest().body(Map.of("error", "version_non_prise_en_charge", "versions", ServeurMcp.VERSIONS));
        }
        ClientIntegration client = client(authz);
        JsonNode message;
        try {
            message = json.readTree(corps == null ? "" : corps);
        } catch (JsonProcessingException illisible) {
            return ResponseEntity.badRequest().body(ServeurMcp.erreurAnalyse());
        }
        if (message != null && message.isArray()) {
            return ResponseEntity.badRequest().body(ServeurMcp.lotRefuse());
        }
        Optional<Map<String, Object>> reponse = serveur.traiter(client, message);
        return reponse.<ResponseEntity<Object>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.accepted().build());
    }

    /** Le serveur n'ouvre aucun flux SSE : la spécification prévoit alors 405. */
    @GetMapping
    public ResponseEntity<Void> flux() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).header(HttpHeaders.ALLOW, "POST").build();
    }

    @ExceptionHandler(ClientsIntegrationService.AccesRefuse.class)
    public ResponseEntity<Map<String, String>> refuse(ClientsIntegrationService.AccesRefuse e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(Map.of("error", "invalid_token", "message", e.getMessage()));
    }

    private ClientIntegration client(String authz) {
        String jeton = authz != null && authz.startsWith("Bearer ") ? authz.substring(7) : null;
        ClientIntegration c = clients.authentifier(jeton);
        if (!modules.estActif(c.getTenantId(), "clm")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Module Contrats et signature non souscrit.");
        }
        return c;
    }
}
