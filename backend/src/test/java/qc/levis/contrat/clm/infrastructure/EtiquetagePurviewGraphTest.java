package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.EtiquetagePurview;
import qc.levis.contrat.clm.domain.EtiquettePurview;
import qc.levis.contrat.integration.application.IntegrationM365Service;
import qc.levis.contrat.integration.application.port.Microsoft365Client;

/**
 * Purview par Microsoft Graph (profil purview, ADR-036) contre un Graph simulé : contrat du port, requêtes conformes à
 * l'API (catalogues, résolution du fichier par son adresse, assignSensitivityLabel, retentionLabel), pannes lues dans le
 * résultat.
 */
class EtiquetagePurviewGraphTest extends EtiquetagePurviewContrat {

    private static final String FICHIER = "https://iq.sharepoint.com/sites/contrats/Shared%20Documents/Purview/c.pdf";

    private HttpServer serveur;
    private String base;
    private final List<String> requetes = new CopyOnWriteArrayList<>();
    private final IntegrationM365Service connexions = mock(IntegrationM365Service.class);
    private EtiquetagePurviewGraph sujet;

    @BeforeEach
    void demarrer() throws Exception {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        serveur.createContext("/", this::repondre);
        serveur.start();
        when(connexions.configClient(TENANT)).thenReturn(Optional.of(new Microsoft365Client.ConfigM365("ms", "app", "secret",
                "site", "drive", null, null, null)));
        sujet = new EtiquetagePurviewGraph(connexions, new ObjectMapper(), Clock.systemUTC(), base + "/v1.0/", base);
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    @Override
    EtiquetagePurview sujet() {
        return sujet;
    }

    @Override
    String fichierDepose() {
        return FICHIER;
    }

    @Test
    void le_catalogue_lit_les_etiquettes_mip_et_de_retention_avec_leur_duree() {
        List<EtiquettePurview> c = sujet.catalogue(TENANT);
        assertEquals(List.of(EtiquettePurview.sensibilite("s-conf", "Confidentiel"), EtiquettePurview.retention("Contrats - 7 ans", 2557),
                EtiquettePurview.retention("Permanent", null)), c);
        assertTrue(requetes.contains("GET /beta/security/informationProtection/sensitivityLabels"));
        assertTrue(requetes.contains("GET /v1.0/security/labels/retentionLabels"));
    }

    @Test
    void l_apposition_resout_le_fichier_puis_assigne_la_sensibilite_et_la_retention() {
        assertTrue(sujet.apposer(TENANT, FICHIER, EtiquettePurview.sensibilite("s-conf", "Confidentiel"),
                EtiquettePurview.retention("Contrats - 7 ans", 2557)).succes());
        String partage = "u!" + Base64.getUrlEncoder().withoutPadding().encodeToString(FICHIER.getBytes(StandardCharsets.UTF_8));
        assertTrue(requetes.contains("GET /v1.0/shares/" + partage + "/driveItem"));
        assertTrue(requetes.stream().anyMatch(r -> r.startsWith("POST /v1.0/drives/d1/items/i1/assignSensitivityLabel")
                && r.contains("\"sensitivityLabelId\":\"s-conf\"") && r.contains("\"assignmentMethod\":\"standard\"")), requetes.toString());
        assertTrue(requetes.stream().anyMatch(r -> r.startsWith("PATCH /v1.0/drives/d1/items/i1/retentionLabel")
                && r.contains("\"name\":\"Contrats - 7 ans\"")), requetes.toString());
    }

    @Test
    void sans_connexion_ou_en_panne_le_catalogue_est_vide_et_l_apposition_echoue_sans_exception() {
        assertTrue(sujet.catalogue(99L).isEmpty(), "connexion Microsoft 365 absente");
        assertFalse(sujet.apposer(99L, FICHIER, null, null).succes());
        assertFalse(sujet.apposer(TENANT, "https://iq.sharepoint.com/inconnu.pdf", EtiquettePurview.sensibilite("s", "S"), null).succes(),
                "fichier introuvable : 404 lu dans le résultat");
        EtiquetagePurviewGraph injoignable = new EtiquetagePurviewGraph(connexions, new ObjectMapper(), Clock.systemUTC(),
                "http://127.0.0.1:9/v1.0", "http://127.0.0.1:9");
        assertTrue(injoignable.catalogue(TENANT).isEmpty());
    }

    private void repondre(HttpExchange x) throws IOException {
        String chemin = x.getRequestURI().getRawPath();
        String corps = new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requetes.add(x.getRequestMethod() + " " + chemin + (corps.isEmpty() ? "" : " " + corps));
        if (chemin.equals("/ms/oauth2/v2.0/token")) {
            ecrire(x, 200, "{\"access_token\":\"jeton\",\"expires_in\":3600}");
        } else if (chemin.equals("/beta/security/informationProtection/sensitivityLabels")) {
            ecrire(x, 200, "{\"value\":[{\"id\":\"s-conf\",\"name\":\"Confidentiel\"}]}");
        } else if (chemin.equals("/v1.0/security/labels/retentionLabels")) {
            ecrire(x, 200, "{\"value\":[{\"displayName\":\"Contrats - 7 ans\",\"retentionDuration\":{\"days\":2557}},"
                    + "{\"displayName\":\"Permanent\",\"retentionDuration\":{\"@odata.type\":\"#microsoft.graph.security.retentionDurationForever\"}}]}");
        } else if (chemin.startsWith("/v1.0/shares/") && decoder(chemin).equals(FICHIER)) {
            ecrire(x, 200, "{\"id\":\"i1\",\"parentReference\":{\"driveId\":\"d1\"}}");
        } else if (chemin.equals("/v1.0/drives/d1/items/i1/assignSensitivityLabel")) {
            ecrire(x, 202, "");
        } else if (chemin.equals("/v1.0/drives/d1/items/i1/retentionLabel")) {
            ecrire(x, 200, "{\"name\":\"Contrats - 7 ans\"}");
        } else {
            ecrire(x, 404, "{\"error\":{\"code\":\"itemNotFound\"}}");
        }
    }

    private static String decoder(String chemin) {
        String id = chemin.substring("/v1.0/shares/u!".length(), chemin.length() - "/driveItem".length());
        return new String(Base64.getUrlDecoder().decode(id), StandardCharsets.UTF_8);
    }

    private static void ecrire(HttpExchange x, int statut, String corps) throws IOException {
        byte[] o = corps.getBytes(StandardCharsets.UTF_8);
        x.sendResponseHeaders(statut, o.length == 0 ? -1 : o.length);
        if (o.length > 0) {
            x.getResponseBody().write(o);
        }
        x.close();
    }
}
