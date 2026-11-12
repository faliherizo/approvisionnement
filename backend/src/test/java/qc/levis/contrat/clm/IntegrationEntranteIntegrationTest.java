package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.Cookie;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.RappelsExternesService;

/**
 * API d'intégration entrante sur la vraie pile, organisation IQ (addenda 1) : Workday déclenche une signature et reçoit
 * des rappels signés (TI-22) ; Salesforce lance un lot (AF-47) ; un utilisateur lance un lot depuis la plateforme.
 * Un client ne lit que ses trousses, et un client révoqué n'obtient plus rien.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class IntegrationEntranteIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String PDF = Base64.getEncoder().encodeToString(("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
            + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
            + "trailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RappelsExternesService rappels;

    private HttpServer recepteur;
    private final List<String[]> recus = Collections.synchronizedList(new ArrayList<>());
    private Cookie admin;

    @BeforeEach
    void preparer() throws Exception {
        recepteur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        recepteur.createContext("/rappels", ex -> {
            recus.add(new String[]{ex.getRequestHeaders().getFirst("X-CLM-Signature"), ex.getRequestHeaders().getFirst("Idempotency-Key"),
                    new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)});
            ex.sendResponseHeaders(204, -1);
            ex.close();
        });
        recepteur.start();
        admin = login("admin.ti@iq.example");
    }

    @AfterEach
    void arreter() {
        recepteur.stop(0);
    }

    @Test
    void workday_declenche_une_signature_et_recoit_des_rappels_signes() throws Exception {
        JsonNode cree = corps(appel("POST", "/api/clm/admin/clients-integration", admin, "{\"nom\":\"Workday RH\",\"systeme\":\"WORKDAY\","
                + "\"urlRappel\":\"http://127.0.0.1:" + recepteur.getAddress().getPort() + "/rappels\"}"), 201);
        String clientId = cree.get("client").get("clientId").asText();
        String cleRappel = cree.get("cleRappel").asText();
        assertEquals(401, jetonReponse(clientId, "mauvais-secret").getStatus(), "secret refusé, sans détail");
        String jeton = jeton(clientId, cree.get("secret").asText());

        String demande = "{\"titre\":\"Lettre d'offre — poste 4471\",\"referenceExterne\":\"WD-BP-2026-4471\",\"initiateur\":\"initiateur@iq.example\","
                + "\"documents\":[{\"nom\":\"offre.pdf\",\"contenuBase64\":\"" + PDF + "\"}],"
                + "\"signataires\":[{\"nom\":\"Alex Candidat\",\"courriel\":\"alex@exemple.ca\",\"roleContrat\":\"SIGNATAIRE_1\",\"telephone\":\"+15145550101\",\"externe\":true}],"
                + "\"lancer\":true}";
        JsonNode t = corps(api("POST", "/api/clm/integration/v1/trousses", jeton, demande), 201);
        assertEquals("WORKDAY", t.get("source").asText());
        assertEquals("EN_TRAITEMENT", t.get("statut").asText());
        assertEquals("WD-BP-2026-4471", t.get("referenceExterne").asText());

        assertEquals(1, rappels.relayer(), "le lancement est rappelé à Workday");
        String[] recu = recus.get(0);
        assertEquals(hmac(cleRappel, recu[2]), recu[0], "signature vérifiable avec la clé remise à la création");
        JsonNode rappel = json.readTree(recu[2]);
        assertEquals("LANCEE", rappel.get("evenement").asText());
        assertEquals("WD-BP-2026-4471", rappel.get("referenceExterne").asText());
        assertEquals(rappel.get("cleIdempotence").asText(), recu[1]);
        assertEquals(0, rappels.relayer(), "un rappel livré ne repart pas");

        long id = t.get("id").asLong();
        long doc = t.get("documents").get(0).get("id").asLong();
        MockHttpServletResponse contenu = api("GET", "/api/clm/integration/v1/trousses/" + id + "/documents/" + doc + "/contenu", jeton, null);
        assertEquals(200, contenu.getStatus());
        assertEquals("%PDF", new String(contenu.getContentAsByteArray(), 0, 4, StandardCharsets.ISO_8859_1));

        assertEquals(403, api("POST", "/api/clm/integration/v1/trousses", jeton,
                demande.replace("initiateur@iq.example", "inconnu@ailleurs.example")).getStatus(), "initiateur hors organisation");
        assertEquals(400, api("POST", "/api/clm/integration/v1/trousses", jeton,
                demande.replace("\"initiateur\":\"initiateur@iq.example\",", "")).getStatus(), "initiateur requis");
        assertEquals(400, api("POST", "/api/clm/integration/v1/trousses", jeton, demande.replace(PDF, "%%%")).getStatus(), "base64 illisible");
        assertEquals(400, api("POST", "/api/clm/integration/v1/trousses", jeton,
                "{\"initiateur\":\"initiateur@iq.example\"}").getStatus(), "ni titre, ni dossier, ni référence externe");

        MockHttpServletResponse preuve = api("GET", "/api/clm/integration/v1/trousses/" + id + "/preuve", jeton, null);
        assertEquals(200, preuve.getStatus(), "dossier de preuve exportable par le système qui a créé la trousse");
        assertEquals("application/zip", preuve.getContentType());
        assertTrue(corps(appel("GET", "/api/clm/admin/clients-integration", admin, null), 200).toString().contains("Workday RH"));
        JsonNode suivis = corps(appel("GET", "/api/clm/admin/rappels", admin, null), 200);
        assertEquals("ENVOYE", suivis.get(0).get("statut").asText());
        assertEquals("Workday RH", suivis.get(0).get("client").asText());
        assertEquals(404, appel("POST", "/api/clm/admin/rappels/999999/relancer", admin, "{}").getStatus());

        JsonNode autre = corps(appel("POST", "/api/clm/admin/clients-integration", admin, "{\"nom\":\"Autre\",\"systeme\":\"AUTRE\"}"), 201);
        String jetonAutre = jeton(autre.get("client").get("clientId").asText(), autre.get("secret").asText());
        assertEquals(404, api("GET", "/api/clm/integration/v1/trousses/" + id, jetonAutre, null).getStatus(), "un client ne lit que ses trousses");

        corps(appel("POST", "/api/clm/admin/clients-integration/" + cree.get("client").get("id").asLong() + "/revoquer", admin, "{}"), 200);
        assertEquals(401, api("GET", "/api/clm/integration/v1/trousses/" + id, jeton, null).getStatus(), "jeton d'un client révoqué refusé");
        assertEquals(401, api("GET", "/api/clm/integration/v1/trousses/" + id, "falsifie.0000", null).getStatus());
        assertEquals(400, mvc.perform(MockMvcRequestBuilders.post("/api/clm/integration/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "password")
                .param("client_id", clientId).param("client_secret", "x")).andReturn().getResponse().getStatus());
    }

    @Test
    void salesforce_et_la_plateforme_lancent_des_lots_dont_chaque_echec_reste_isole() throws Exception {
        JsonNode cree = corps(appel("POST", "/api/clm/admin/clients-integration", admin,
                "{\"nom\":\"Salesforce Grantmaking\",\"systeme\":\"SALESFORCE\"}"), 201);
        String jeton = jeton(cree.get("client").get("clientId").asText(), cree.get("secret").asText());
        String signataire = "{\"nom\":\"Marc Bénéficiaire\",\"courriel\":\"marc@exemple.ca\",\"roleContrat\":\"SIGNATAIRE_1\",\"telephone\":\"+15145550101\",\"externe\":true}";
        String lot = "{\"libelle\":\"Conventions octobre\",\"initiateur\":\"initiateur@iq.example\",\"lancer\":true,\"elements\":["
                + "{\"sfDossierId\":\"DEM-2026-0001\",\"documents\":[{\"nom\":\"c1.pdf\",\"contenuBase64\":\"" + PDF + "\"}],\"signataires\":[" + signataire + "]},"
                + "{\"referenceExterne\":\"SANS-DOCUMENT\",\"signataires\":[" + signataire + "]}]}";

        assertTrue(corps(api("GET", "/api/clm/integration/v1/modeles", jeton, null), 200).isArray(), "catalogue des modèles publiés");
        JsonNode l = corps(api("POST", "/api/clm/integration/v1/lots", jeton, lot), 201);
        assertEquals("TERMINE_AVEC_ECHECS", l.get("statut").asText());
        assertEquals(1, l.get("reussis").asInt());
        assertEquals(1, l.get("echecs").asInt());
        assertEquals("LANCEE", l.get("elements").get(0).get("statut").asText());
        assertFalse(l.get("elements").get(1).get("erreur").asText().isBlank(), "le motif de l'échec est conservé");
        assertEquals(l.get("id"), corps(api("GET", "/api/clm/integration/v1/lots/" + l.get("id").asLong(), jeton, null), 200).get("id"));
        JsonNode autre = corps(appel("POST", "/api/clm/admin/clients-integration", admin, "{\"nom\":\"Tiers\",\"systeme\":\"AUTRE\"}"), 201);
        assertEquals(404, api("GET", "/api/clm/integration/v1/lots/" + l.get("id").asLong(),
                jeton(autre.get("client").get("clientId").asText(), autre.get("secret").asText()), null).getStatus(), "un client ne lit que ses lots");

        Cookie initiateur = login("initiateur@iq.example");
        JsonNode parPlateforme = corps(appel("POST", "/api/clm/lots", initiateur, "{\"libelle\":\"Lot écran\",\"lancer\":false,\"elements\":["
                + "{\"titre\":\"Entente A\",\"documents\":[{\"nom\":\"a.pdf\",\"contenuBase64\":\"" + PDF + "\"}],\"signataires\":[" + signataire + "]}]}"), 201);
        assertEquals("TERMINE", parPlateforme.get("statut").asText());
        assertEquals("CREEE", parPlateforme.get("elements").get(0).get("statut").asText());
        assertEquals("Lot écran", corps(appel("GET", "/api/clm/lots/" + parPlateforme.get("id").asLong(), initiateur, null), 200)
                .get("libelle").asText());
        assertTrue(corps(appel("GET", "/api/clm/lots", initiateur, null), 200).size() >= 2);
        assertEquals(403, appel("POST", "/api/clm/lots", login("auditeur@iq.example"), "{\"elements\":[]}").getStatus(), "l'auditeur ne lance rien");
        assertEquals(400, appel("POST", "/api/clm/lots", initiateur, "{\"elements\":[]}").getStatus(), "un lot vide est refusé");
    }

    private String jeton(String clientId, String secret) throws Exception {
        return corps(jetonReponse(clientId, secret), 200).get("access_token").asText();
    }

    private MockHttpServletResponse jetonReponse(String clientId, String secret) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/clm/integration/oauth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials").param("client_id", clientId).param("client_secret", secret)).andReturn().getResponse();
    }

    private MockHttpServletResponse api(String methode, String url, String jeton, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url).header("Authorization", "Bearer " + jeton);
        if (corps != null) {
            r.contentType(MediaType.APPLICATION_JSON).content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
    }

    private static String hmac(String cle, String corps) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(cle.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(corps.getBytes(StandardCharsets.UTF_8)));
    }

    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private MockHttpServletResponse appel(String methode, String url, Cookie qui, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url).cookie(qui);
        if (corps != null) {
            r.contentType(MediaType.APPLICATION_JSON).content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
    }

    private JsonNode corps(MockHttpServletResponse r, int attendu) throws Exception {
        String s = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(attendu, r.getStatus(), s);
        return json.readTree(s);
    }
}
