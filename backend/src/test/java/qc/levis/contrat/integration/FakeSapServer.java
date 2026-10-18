package qc.levis.contrat.integration;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import qc.levis.contrat.integration.infrastructure.SapODataAdapter;

/**
 * Simulateur SAP en mémoire (serveur HTTP du JDK, sans dépendance) : reproduit un service <b>OData</b>
 * qui accepte un <b>POST</b> de référence financière et renvoie une réponse OData. Capture la dernière
 * requête (méthode, chemin, en-têtes d'authentification, corps) pour les assertions et permet de
 * simuler un échec. Sert uniquement aux tests pour valider {@link SapODataAdapter} de bout en bout.
 */
final class FakeSapServer {

    private final HttpServer server;

    boolean echecDepot = false;
    boolean reponseSansReference = false;
    boolean reponseNonJson = false;

    volatile String derniereMethode;
    volatile String dernierChemin;
    volatile String dernierCorps;
    volatile String derniereAutorisation;
    volatile String dernierApiKey;
    volatile int nbDepots = 0;

    FakeSapServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de démarrer le simulateur SAP", e);
        }
        server.createContext("/", this::traiter);
    }

    void demarrer() { server.start(); }

    void arreter() { server.stop(0); }

    int port() { return server.getAddress().getPort(); }

    String baseUrl() { return "http://127.0.0.1:" + port(); }

    private void traiter(com.sun.net.httpserver.HttpExchange ex) throws IOException {
        derniereMethode = ex.getRequestMethod();
        dernierChemin = ex.getRequestURI().getPath();
        dernierCorps = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        derniereAutorisation = ex.getRequestHeaders().getFirst("Authorization");
        dernierApiKey = ex.getRequestHeaders().getFirst("APIKey");

        int code;
        String corps;
        if ("POST".equals(derniereMethode)) {
            if (echecDepot) {
                code = 500;
                corps = "{\"error\":{\"code\":\"SY/530\",\"message\":\"echec\"}}";
            } else {
                nbDepots++;
                code = 201;
                corps = reponseNonJson ? "reference cree: SAP-REF-1"
                        : reponseSansReference ? "{\"d\":{}}"
                        : "{\"d\":{\"Reference\":\"SAP-REF-1\"}}";
            }
        } else if ("GET".equals(derniereMethode)) {
            // Lecture (test de connexion) : un service OData en lecture seule répond 200.
            code = echecDepot ? 401 : 200;
            corps = echecDepot ? "{\"error\":\"unauthorized\"}" : "{\"d\":{\"results\":[]}}";
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
