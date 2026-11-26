package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.EtiquetagePurview;
import qc.levis.contrat.clm.domain.EtiquettePurview;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.port.Microsoft365Client;

/**
 * Microsoft Purview par Microsoft Graph (profil {@code purview}, ADR-036), avec la connexion Microsoft 365 de
 * l'organisation (ADR-002) :
 * <ul>
 *   <li>catalogue : {@code GET /beta/security/informationProtection/sensitivityLabels} (permission
 *       {@code InformationProtectionPolicy.Read.All}) et {@code GET /v1.0/security/labels/retentionLabels}
 *       ({@code RecordsManagement.Read.All}) ;</li>
 *   <li>apposition : l'élément déposé se résout par son adresse ({@code /shares/u!…/driveItem}), puis
 *       {@code POST …/assignSensitivityLabel} (asynchrone, 202) et {@code PATCH …/retentionLabel}
 *       ({@code Files.ReadWrite.All}, {@code Sites.ReadWrite.All}).</li>
 * </ul>
 * Toute erreur se lit dans le résultat ; ni secret ni contenu n'est journalisé.
 */
@Component
@Profile("purview")
public class EtiquetagePurviewGraph implements EtiquetagePurview {

    private static final Logger log = LoggerFactory.getLogger(EtiquetagePurviewGraph.class);
    static final String JUSTIFICATION = "Étiquette de la politique de l'organisation (module Contrats et signature)";

    private final IntegrationM365Service connexions;
    private final ClientEntra client;
    private final String graph;
    private final String login;

    public EtiquetagePurviewGraph(IntegrationM365Service connexions, ObjectMapper json, Clock horloge,
                                  @Value("${app.m365.graph-base-url:https://graph.microsoft.com/v1.0}") String graph,
                                  @Value("${app.m365.login-base-url:https://login.microsoftonline.com}") String login) {
        this.connexions = connexions;
        this.client = new ClientEntra(json, horloge);
        this.graph = graph.replaceAll("/+$", "");
        this.login = login;
    }

    @Override
    public List<EtiquettePurview> catalogue(Long tenantId) {
        Optional<ClientEntra.Identite> qui = identite(tenantId);
        if (qui.isEmpty()) {
            return List.of();
        }
        try {
            List<EtiquettePurview> etiquettes = new ArrayList<>();
            client.get(qui.get(), beta() + "/security/informationProtection/sensitivityLabels").path("value")
                    .forEach(n -> etiquettes.add(EtiquettePurview.sensibilite(n.path("id").asText(), n.path("name").asText())));
            client.get(qui.get(), graph + "/security/labels/retentionLabels").path("value")
                    .forEach(n -> etiquettes.add(EtiquettePurview.retention(n.path("displayName").asText(), jours(n))));
            return etiquettes;
        } catch (RuntimeException e) {
            log.warn("Catalogue Purview indisponible pour l'organisation {} : {}", tenantId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Resultat apposer(Long tenantId, String urlDepot, EtiquettePurview sensibilite, EtiquettePurview retention) {
        Optional<ClientEntra.Identite> qui = identite(tenantId);
        if (qui.isEmpty() || urlDepot == null || urlDepot.isBlank()) {
            return new Resultat(false, "Connexion Microsoft 365 inactive ou fichier non déposé.");
        }
        try {
            String element = element(qui.get(), urlDepot);
            if (sensibilite != null) {
                client.post(qui.get(), element + "/assignSensitivityLabel", Map.of("sensitivityLabelId", sensibilite.id(),
                        "assignmentMethod", "standard", "justificationText", JUSTIFICATION));
            }
            if (retention != null) {
                client.patch(qui.get(), element + "/retentionLabel", Map.of("name", retention.nom()));
            }
            return new Resultat(true, "Étiquettes apposées par Microsoft Purview.");
        } catch (RuntimeException e) {
            return new Resultat(false, "Microsoft Purview : " + e.getMessage());
        }
    }

    /** Élément du lecteur désigné par l'adresse du fichier déposé (identifiant de partage « u! » de Graph). */
    private String element(ClientEntra.Identite qui, String url) {
        String partage = "u!" + Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
        JsonNode item = client.get(qui, graph + "/shares/" + partage + "/driveItem");
        return graph + "/drives/" + item.path("parentReference").path("driveId").asText() + "/items/" + item.path("id").asText();
    }

    private Optional<ClientEntra.Identite> identite(Long tenantId) {
        return connexions.configClient(tenantId).map((Microsoft365Client.ConfigM365 c) ->
                new ClientEntra.Identite(login, c.msTenantId(), c.clientId(), c.clientSecretClair(), "https://graph.microsoft.com/.default"));
    }

    private String beta() {
        return graph.endsWith("/v1.0") ? graph.substring(0, graph.length() - "/v1.0".length()) + "/beta" : graph;
    }

    private static Integer jours(JsonNode etiquette) {
        JsonNode jours = etiquette.path("retentionDuration").path("days");
        return jours.isNumber() ? jours.asInt() : null;
    }
}
