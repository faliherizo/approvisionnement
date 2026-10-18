package qc.levis.contrat.integration;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.integration.infrastructure.HttpMicrosoft365Client;

/**
 * Simulateur Microsoft 365 en mémoire (serveur HTTP du JDK, aucune dépendance externe). Reproduit la
 * surface utile pour valider {@link HttpMicrosoft365Client} de bout en bout :
 *
 * <ul>
 *   <li>émetteur de jetons Entra ID : {@code POST /{tenant}/oauth2/v2.0/token} ;</li>
 *   <li>dépôt SharePoint/OneDrive/Teams : {@code PUT …/root:/{chemin}:/content} ;</li>
 *   <li>envoi Outlook : {@code POST /users/{exp}/sendMail}.</li>
 * </ul>
 *
 * Il capture la dernière requête reçue (méthode, chemin, en-têtes, corps) pour les assertions et
 * permet de simuler des échecs (jeton refusé, dépôt interdit). Sert uniquement aux tests.
 */
final class FakeMicrosoft365Server {

    private final HttpServer server;

    // Comportements simulés (modifiables par test).
    boolean echecJeton = false;
    boolean jetonSansAccessToken = false;
    boolean jetonCorpsInvalide = false;
    boolean echecDepot = false;

    // Dernière requête capturée.
    volatile String derniereMethode;
    volatile String dernierChemin;
    volatile String dernierCorps;
    volatile String derniereAutorisation;
    volatile String dernierContentType;
    volatile int nbDepots = 0;

    FakeMicrosoft365Server() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de démarrer le simulateur M365", e);
        }
        server.createContext("/", this::traiter);
    }

    void demarrer() { server.start(); }

    void arreter() { server.stop(0); }

    int port() { return server.getAddress().getPort(); }

    String graphBaseUrl() { return "http://127.0.0.1:" + port() + "/v1.0"; }

    String loginBaseUrl() { return "http://127.0.0.1:" + port(); }

    private void traiter(com.sun.net.httpserver.HttpExchange ex) throws IOException {
        derniereMethode = ex.getRequestMethod();
        dernierChemin = ex.getRequestURI().getPath();
        dernierCorps = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        derniereAutorisation = ex.getRequestHeaders().getFirst("Authorization");
        dernierContentType = ex.getRequestHeaders().getFirst("Content-Type");

        int code;
        String corps;
        if (dernierChemin.endsWith("/oauth2/v2.0/token")) {
            code = echecJeton ? 401 : 200;
            corps = echecJeton ? "{\"error\":\"invalid_client\"}"
                    : jetonCorpsInvalide ? "ceci-n-est-pas-du-json"
                    : jetonSansAccessToken ? "{\"token_type\":\"Bearer\"}"
                    : "{\"access_token\":\"FAKE_TOKEN\",\"token_type\":\"Bearer\",\"expires_in\":3599}";
        } else if (dernierChemin.contains("/sendMail")) {
            code = 202;
            corps = "";
        } else if (dernierChemin.endsWith("/filesFolder")) {
            // Dossier « Fichiers » du canal Teams : renvoie l'id du dossier + le drive parent.
            code = 200;
            corps = "{\"id\":\"CHANFOLDER\",\"name\":\"General\","
                    + "\"parentReference\":{\"driveId\":\"CHANDRIVE\"}}";
        } else if ("PUT".equals(derniereMethode) && dernierChemin.endsWith(":/content")) {
            if (echecDepot) {
                code = 403;
                corps = "{\"error\":{\"code\":\"accessDenied\",\"message\":\"refuse\"}}";
            } else {
                nbDepots++;
                code = 201;
                corps = "{\"id\":\"01FAKE\",\"name\":\"fichier\",\"webUrl\":\"https://fake.m365"
                        + dernierChemin + "\"}";
            }
        } else {
            code = 404;
            corps = "";
        }

        byte[] octets = corps.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(code, octets.length == 0 ? -1 : octets.length);
        if (octets.length > 0) {
            try (OutputStream os = ex.getResponseBody()) {
                os.write(octets);
            }
        }
        ex.close();
    }
}
