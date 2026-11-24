package qc.levis.contrat.clm.application.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.clm.domain.ClientIntegration;

/**
 * Protocole MCP (Model Context Protocol) du module CLM, JSON-RPC 2.0 (ADR-023) : {@code initialize}, {@code ping},
 * {@code tools/list}, {@code tools/call}. Une erreur d'outil devient un résultat {@code isError} que l'assistant peut
 * lire et corriger ; une erreur de protocole devient une erreur JSON-RPC.
 */
@Service
public class ServeurMcp {

    /** Versions prises en charge, la plus récente en tête. */
    public static final List<String> VERSIONS = List.of("2025-11-25", "2025-06-18", "2025-03-26");

    static final int REQUETE_INVALIDE = -32600;
    static final int METHODE_INCONNUE = -32601;
    static final int PARAMETRES_INVALIDES = -32602;

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    private final Map<String, OutilMcp> outils;
    private final AuditService audit;

    public ServeurMcp(List<OutilMcp> outils, AuditService audit) {
        this.outils = outils.stream().collect(Collectors.toMap(OutilMcp::nom, Function.identity(), (a, b) -> {
            throw new IllegalStateException("Outil MCP en double : " + a.nom());
        }, LinkedHashMap::new));
        this.audit = audit;
    }

    /** Réponse JSON-RPC au message ; vide pour une notification ou une réponse, qui n'appellent pas de réponse. */
    public Optional<Map<String, Object>> traiter(ClientIntegration client, JsonNode message) {
        if (message == null || !message.isObject() || !"2.0".equals(message.path("jsonrpc").asText())) {
            return Optional.of(erreur(null, REQUETE_INVALIDE, "Message JSON-RPC 2.0 attendu."));
        }
        JsonNode id = message.get("id");
        if (!message.hasNonNull("method")) {
            return id == null ? Optional.empty() : Optional.of(erreur(id, REQUETE_INVALIDE, "Méthode manquante."));
        }
        if (id == null || id.isNull()) {
            return Optional.empty();
        }
        JsonNode params = message.path("params");
        switch (message.get("method").asText()) {
            case "initialize": return Optional.of(succes(id, initialiser(params)));
            case "ping": return Optional.of(succes(id, Map.of()));
            case "tools/list": return Optional.of(succes(id, Map.of("tools", outils.values().stream().map(ServeurMcp::description).toList())));
            case "tools/call": return Optional.of(appeler(client, id, params));
            default: return Optional.of(erreur(id, METHODE_INCONNUE, "Méthode inconnue : " + message.get("method").asText()));
        }
    }

    private static Map<String, Object> initialiser(JsonNode params) {
        String demandee = params.path("protocolVersion").asText("");
        return Map.of(
                "protocolVersion", VERSIONS.contains(demandee) ? demandee : VERSIONS.get(0),
                "capabilities", Map.of("tools", Map.of("listChanged", false)),
                "serverInfo", Map.of("name", "vina-clm", "title", "Contrats et signature", "version", "1.0.0"),
                "instructions", "Outils en lecture seule sur les modèles et les signatures de l'organisation du client d'intégration.");
    }

    private static Map<String, Object> description(OutilMcp o) {
        return Map.of("name", o.nom(), "title", o.titre(), "description", o.description(), "inputSchema", o.schemaEntree(),
                "annotations", Map.of("readOnlyHint", true, "destructiveHint", false, "idempotentHint", true, "openWorldHint", false));
    }

    private Map<String, Object> appeler(ClientIntegration client, JsonNode id, JsonNode params) {
        OutilMcp outil = outils.get(params.path("name").asText(""));
        if (outil == null) {
            return erreur(id, PARAMETRES_INVALIDES, "Outil inconnu : " + params.path("name").asText(""));
        }
        JsonNode brut = params.path("arguments");
        if (!brut.isMissingNode() && !brut.isNull() && !brut.isObject()) {
            return erreur(id, PARAMETRES_INVALIDES, "Les arguments forment un objet JSON.");
        }
        Map<String, Object> arguments = brut.isObject() ? JSON.convertValue(brut, new TypeReference<Map<String, Object>>() { }) : Map.of();
        audit.log(client.getTenantId(), "integration:" + client.getClientId(), "CLM_MCP_OUTIL", "OutilMcp", null, outil.nom());
        try {
            Map<String, Object> resultat = outil.executer(client, arguments);
            return succes(id, Map.of("content", List.of(texte(resultat)), "structuredContent", resultat, "isError", false));
        } catch (OutilMcp.ArgumentInvalide | NoSuchElementException refus) {
            return succes(id, Map.of("content", List.of(Map.of("type", "text", "text", refus.getMessage())), "isError", true));
        }
    }

    private static Map<String, Object> texte(Map<String, Object> resultat) {
        try {
            return Map.of("type", "text", "text", JSON.writeValueAsString(resultat));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Résultat d'outil non sérialisable.", e);
        }
    }

    private static Map<String, Object> succes(JsonNode id, Map<String, Object> resultat) {
        return Map.of("jsonrpc", "2.0", "id", id, "result", resultat);
    }

    static Map<String, Object> erreur(JsonNode id, int code, String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("jsonrpc", "2.0");
        r.put("id", id);
        r.put("error", Map.of("code", code, "message", message));
        return r;
    }

    /** Erreur d'analyse JSON (code -32700), rendue par le contrôleur quand le corps n'est pas du JSON. */
    public static Map<String, Object> erreurAnalyse() {
        return erreur(null, -32700, "Corps JSON illisible.");
    }

    /** Lot JSON-RPC : retiré du protocole depuis la version 2025-06-18. */
    public static Map<String, Object> lotRefuse() {
        return erreur(null, REQUETE_INVALIDE, "Les lots JSON-RPC ne sont pas pris en charge.");
    }
}
