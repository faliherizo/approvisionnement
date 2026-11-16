package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;

/** Adaptateur Salesforce REST contre un serveur HTTP local qui imite l'API (OAuth, sobjects, SOQL, PATCH). */
class SalesforceRestTest {

    private HttpServer serveur;
    private final List<String> appels = new CopyOnWriteArrayList<>();
    private volatile int statutPatch = 204;
    private volatile int statutJeton = 200;
    private volatile int statutLimites = 503;
    private final AtomicInteger jetons = new AtomicInteger();
    private final List<String> requetes = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> demandeJeton = new AtomicReference<>();
    private String base;
    private SalesforceRest sf;

    @BeforeEach
    void demarrer() throws IOException {
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serveur.createContext("/token", ex -> {
            jetons.incrementAndGet();
            demandeJeton.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            repondre(ex, statutJeton, "{\"access_token\":\"jeton-test\"}");
        });
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/a01", ex -> {
            appels.add(ex.getRequestMethod() + " " + ex.getRequestHeaders().getFirst("Authorization") + " " + ex.getRequestHeaders().getFirst("X-Correlation-Id"));
            requetes.add(String.valueOf(ex.getRequestURI().getQuery()));
            if ("PATCH".equals(ex.getRequestMethod())) {
                appels.add(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                repondre(ex, statutPatch, "");
            } else {
                repondre(ex, 200, "{\"attributes\":{\"type\":\"FundingRequest\"},\"Id\":\"a01\",\"Name\":\"DEM-1\",\"Montant\":100,"
                        + "\"NotesInternes__c\":\"interne\"}");
            }
        });
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/DEM-2", ex -> repondre(ex, 404, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/CHAMP", ex -> repondre(ex, 400,
                "[{\"errorCode\":\"INVALID_FIELD\",\"message\":\"No such column 'Demandeur' on entity 'FundingRequest'\"}]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/BRUT", ex -> repondre(ex, 400, "<html/>"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/INCONNU", ex -> repondre(ex, 404, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/EXPIRE", ex -> repondre(ex, 401, "[]"));
        serveur.createContext("/services/data/v60.0/sobjects/FundingRequest/ILLISIBLE", ex -> repondre(ex, 200, "<html>maintenance</html>"));
        serveur.createContext("/services/data/v60.0/query", ex -> {
            String q = ex.getRequestURI().getQuery();
            appels.add(q);
            repondre(ex, 200, q.contains("DEM-2") ? "{\"records\":[{\"attributes\":{},\"Id\":\"a01\"}]}"
                    : q.contains("SUPPRIME") ? "{\"records\":[{\"attributes\":{},\"Id\":\"INCONNU\"}]}" : "{\"records\":[]}");
        });
        serveur.createContext("/services/data/v60.0/limits", ex -> repondre(ex, statutLimites, "{}"));
        serveur.start();
        base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        sf = client(base, base + "/token");
    }

    private static SalesforceRest client(String instance, String jeton) {
        return new SalesforceRest(() -> connexion(null, instance, jeton, "secret", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT),
                new ObjectMapper(), Clock.systemUTC());
    }

    private static ConnexionsSalesforce.Connexion connexion(Long proprietaire, String instance, String jeton, String secret,
                                                           ConnexionSalesforce.ChampsRetour champs) {
        return new ConnexionsSalesforce.Connexion(proprietaire, ConnexionSalesforce.Mode.REST, instance, jeton, "id", secret,
                "FundingRequest", "v60.0", champs, 8);
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    private static void repondre(HttpExchange ex, int statut, String corps) throws IOException {
        byte[] b = corps.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(statut, b.length == 0 ? -1 : b.length);
        if (b.length > 0) {
            ex.getResponseBody().write(b);
        }
        ex.close();
    }

    @Test
    void lecture_par_id_puis_par_numero_soql() {
        PortSalesforce.Dossier d = sf.lireDossier("a01", Set.of("Montant")).orElseThrow();
        assertEquals("DEM-1", d.numero());
        assertFalse(d.champs().containsKey("attributes"));
        assertTrue(appels.get(0).contains("Bearer jeton-test"));
        PortSalesforce.Dossier parNumero = sf.lireDossier("DEM-2", Set.of("Montant")).orElseThrow();
        assertEquals("a01", parNumero.id());
        assertEquals(100, parNumero.champs().get("Montant"), "relu par identifiant : tous les champs, personnalisés compris");
        assertTrue(sf.lireDossier("INCONNU", Set.of("Montant")).isEmpty());
        assertTrue(sf.lireDossier("SUPPRIME", Set.of("Montant")).isEmpty(), "trouvé par numéro puis disparu avant la relecture");
        assertTrue(appels.stream().anyMatch(a -> a.replace('+', ' ').contains("SELECT Id FROM FundingRequest WHERE Name = 'DEM-2'")), appels.toString());
    }

    @Test
    void retour_de_statut_porte_la_correlation_et_signale_les_refus() {
        sf.publierStatut(new PortSalesforce.Statut("a01", "LANCEE", "corr-1", "k", Map.of("reference", "TR-1")));
        assertTrue(appels.stream().anyMatch(a -> a.startsWith("PATCH") && a.endsWith("corr-1")));
        assertTrue(appels.stream().anyMatch(a -> a.contains("\"CLM_Statut__c\":\"LANCEE\"") && a.contains("TR-1")));
        statutPatch = 400;
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of())));
        statutPatch = 500;
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of())));
    }

    @Test
    void test_de_connexion_et_authentification_refusee() {
        assertFalse(sf.tester().disponible());
        statutJeton = 401;
        assertThrows(PortSalesforce.Indisponible.class, () -> client(base, base + "/token").lireDossier("a01", Set.of("Montant")));
        assertFalse(client("http://127.0.0.1:1", "http://127.0.0.1:1/token").tester().disponible());
    }

    @Test
    void connexion_etablie_quand_l_api_repond() {
        statutLimites = 200;
        PortSalesforce.EtatConnexion etat = sf.tester();
        assertTrue(etat.disponible());
        assertTrue(etat.detail().contains("v60.0"), etat.detail());
    }

    @Test
    void un_jeton_refuse_par_l_api_est_redemande_au_prochain_appel() {
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("EXPIRE", Set.of("Montant")));
        assertEquals(1, jetons.get());
        sf.lireDossier("a01", Set.of("Montant"));
        assertEquals(2, jetons.get(), "le 401 invalide le jeton : une nouvelle authentification précède l'appel suivant");
    }

    @Test
    void une_reponse_illisible_ou_une_instance_injoignable_sont_des_indisponibilites() {
        assertEquals("Réponse Salesforce illisible.",
                assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("ILLISIBLE", Set.of("Montant"))).getMessage());
        SalesforceRest coupee = client("http://127.0.0.1:1", base + "/token");
        assertEquals("Salesforce injoignable.", assertThrows(PortSalesforce.Indisponible.class, () -> coupee.lireDossier("a01", Set.of("Montant"))).getMessage());
    }

    @Test
    void un_appel_interrompu_rend_l_interruption_a_l_appelant() {
        SalesforceRest neuf = client(base, base + "/token");
        Thread.currentThread().interrupt();
        PortSalesforce.Indisponible authentification = assertThrows(PortSalesforce.Indisponible.class, () -> neuf.lireDossier("a01", Set.of("Montant")));
        assertTrue(Thread.interrupted(), "l'interruption est rétablie pour l'appelant");
        assertEquals("Authentification Salesforce interrompue.", authentification.getMessage());

        sf.lireDossier("a01", Set.of("Montant"));
        Thread.currentThread().interrupt();
        PortSalesforce.Indisponible appel = assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("a01", Set.of("Montant")));
        assertTrue(Thread.interrupted(), "l'interruption est rétablie pour l'appelant");
        assertEquals("Appel Salesforce interrompu.", appel.getMessage());
    }

    @Test
    void un_statut_non_serialisable_est_refuse_avant_tout_envoi() {
        assertThrows(IllegalStateException.class,
                () -> sf.publierStatut(new PortSalesforce.Statut("a01", "X", "c", "k", Map.of("reference", new Object()))));
        assertTrue(appels.isEmpty(), appels.toString());
        assertEquals(0, jetons.get(), "aucune authentification n'a été tentée");
    }

    @Test
    void le_retour_de_statut_alimente_les_champs_choisis_par_l_organisation() {
        ConnexionSalesforce.ChampsRetour champs = new ConnexionSalesforce.ChampsRetour("Etat_CLM__c", "No_Trousse__c", "Trace__c");
        SalesforceRest propre = new SalesforceRest(() -> connexion(42L, base, base + "/token", "secret", champs), new ObjectMapper(),
                Clock.systemUTC());
        propre.publierStatut(new PortSalesforce.Statut("a01", "SIGNEE", "corr-9", "k", Map.of("reference", "IQ-1")));
        assertTrue(appels.stream().anyMatch(a -> a.contains("\"Etat_CLM__c\":\"SIGNEE\"") && a.contains("\"No_Trousse__c\":\"IQ-1\"")
                && a.contains("\"Trace__c\":\"corr-9\"")), appels.toString());
        assertTrue(appels.stream().noneMatch(a -> a.contains("CLM_Statut__c")), "aucun champ de la plateforme n'est écrit");
    }

    @Test
    void chaque_connexion_obtient_son_jeton_et_un_secret_change_en_exige_un_nouveau() {
        AtomicReference<ConnexionsSalesforce.Connexion> courante = new AtomicReference<>(
                connexion(1L, base, base + "/token", "secret-1", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        SalesforceRest partage = new SalesforceRest(courante::get, new ObjectMapper(), Clock.systemUTC());
        partage.lireDossier("a01", Set.of("Montant"));
        partage.lireDossier("a01", Set.of("Montant"));
        assertEquals(1, jetons.get(), "le jeton de l'organisation 1 est réutilisé");
        courante.set(connexion(2L, base, base + "/token", "secret-2", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        partage.lireDossier("a01", Set.of("Montant"));
        assertEquals(2, jetons.get(), "l'organisation 2 ne réutilise jamais le jeton de l'organisation 1");
        courante.set(connexion(2L, base, base + "/token", "secret-2-rotation", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT));
        partage.lireDossier("a01", Set.of("Montant"));
        assertEquals(3, jetons.get(), "un secret remplacé exige une nouvelle authentification");
    }

    @Test
    void la_lecture_demande_nommement_les_champs_requis_et_ne_garde_rien_d_autre() {
        PortSalesforce.Dossier d = sf.lireDossier("a01", new java.util.HashSet<>(java.util.Arrays.asList("Montant", "Autre__c", "nom invalide", null))).orElseThrow();
        assertEquals("fields=Id,Name,Autre__c,Montant", requetes.get(0), "identifiant et numéro d'abord, noms invalides jamais transmis");
        assertEquals(java.util.Set.of("Id", "Name", "Montant"), d.champs().keySet(),
                "un champ rendu sans avoir été demandé (NotesInternes__c) n'entre pas dans la solution");
    }

    @Test
    void la_valeur_de_statut_suit_la_correspondance_de_l_organisation() {
        sf.publierStatut(new PortSalesforce.Statut("a01", "SIGNATURE_TERMINEE", "corr-2", "k", Map.of("reference", "TR-2", "valeurStatut", "Terminée")));
        assertTrue(appels.stream().anyMatch(a -> a.contains("\"CLM_Statut__c\":\"Terminée\"")), appels.toString());
    }

    @Test
    void le_jwt_bearer_presente_une_assertion_signee_par_la_cle_privee() throws Exception {
        java.security.KeyPairGenerator g = java.security.KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        java.security.KeyPair paire = g.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\n" + java.util.Base64.getMimeEncoder().encodeToString(paire.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        SalesforceRest jwt = new SalesforceRest(() -> new ConnexionsSalesforce.Connexion(5L, ConnexionSalesforce.Mode.REST, base, base + "/token",
                "3MVG9-client", pem, "FundingRequest", "v60.0", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT, 8,
                ConnexionSalesforce.Authentification.JWT_BEARER, "integration@iq.example"), new ObjectMapper(), Clock.systemUTC());
        jwt.lireDossier("a01", Set.of());

        Map<String, String> formulaire = new java.util.HashMap<>();
        for (String p : demandeJeton.get().split("&")) {
            String[] kv = p.split("=", 2);
            formulaire.put(kv[0], java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", formulaire.get("grant_type"));
        assertFalse(demandeJeton.get().contains("client_secret"), "aucun secret partagé ne circule");
        String[] parties = formulaire.get("assertion").split("\\.");
        java.security.Signature v = java.security.Signature.getInstance("SHA256withRSA");
        v.initVerify(paire.getPublic());
        v.update((parties[0] + "." + parties[1]).getBytes(StandardCharsets.US_ASCII));
        assertTrue(v.verify(java.util.Base64.getUrlDecoder().decode(parties[2])), "signature RS256 vérifiable par le certificat public");
        Map<?, ?> charge = new ObjectMapper().readValue(java.util.Base64.getUrlDecoder().decode(parties[1]), Map.class);
        assertEquals("3MVG9-client", charge.get("iss"));
        assertEquals("integration@iq.example", charge.get("sub"));
        assertEquals(base, charge.get("aud"), "audience : origine du serveur de jetons");

        SalesforceRest cleInvalide = new SalesforceRest(() -> new ConnexionsSalesforce.Connexion(6L, ConnexionSalesforce.Mode.REST, base,
                base + "/token", "c", "pas une clé", "FundingRequest", "v60.0", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT, 8,
                ConnexionSalesforce.Authentification.JWT_BEARER, "u"), new ObjectMapper(), Clock.systemUTC());
        PortSalesforce.Indisponible e = assertThrows(PortSalesforce.Indisponible.class, () -> cleInvalide.lireDossier("a01", Set.of()));
        assertFalse(e.getMessage().contains("pas une clé"), "la clé n'apparaît jamais dans un message");
    }

    @Test
    void un_champ_declare_inconnu_de_salesforce_est_signale_pour_correction() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> sf.lireDossier("CHAMP", Set.of("Demandeur")));
        assertTrue(e.getMessage().contains("No such column 'Demandeur'") && e.getMessage().contains("Champs Salesforce"), e.getMessage());
        assertTrue(assertThrows(IllegalArgumentException.class, () -> sf.lireDossier("BRUT", Set.of())).getMessage().contains("HTTP 400"));
    }
}
