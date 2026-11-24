package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * IQ-A-09 (ADR-023), sur la vraie pile : un assistant d'IA se connecte au connecteur MCP avec le jeton d'un client
 * d'intégration, liste les outils, lit les modèles et le statut d'une trousse qu'il a créée — rien d'autre.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-mcp"})
class McpIntegrationTest {

    private static final String MCP = "/api/clm/integration/mcp";
    private static final String PDF = Base64.getEncoder().encodeToString(("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
            + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
            + "trailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie admin;

    @BeforeEach
    void connecter() throws Exception {
        admin = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin.ti@iq.example\",\"motDePasse\":\"Passw0rd!\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private JsonNode client(String nom) throws Exception {
        MockHttpServletResponse r = mvc.perform(MockMvcRequestBuilders.post("/api/clm/admin/clients-integration").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nom\":\"" + nom + "\",\"systeme\":\"AUTRE\"}")).andReturn().getResponse();
        assertEquals(201, r.getStatus(), r.getContentAsString());
        return json.readTree(r.getContentAsString(StandardCharsets.UTF_8));
    }

    private String jeton(JsonNode client) throws Exception {
        MockHttpServletResponse r = mvc.perform(MockMvcRequestBuilders.post("/api/clm/integration/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "client_credentials")
                .param("client_id", client.get("client").get("clientId").asText()).param("client_secret", client.get("secret").asText()))
                .andReturn().getResponse();
        return json.readTree(r.getContentAsString()).get("access_token").asText();
    }

    private MockHttpServletResponse mcp(String jeton, String corps, String... entetes) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.post(MCP).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM).content(corps);
        if (jeton != null) r = r.header("Authorization", "Bearer " + jeton);
        for (int i = 0; i < entetes.length; i += 2) r = r.header(entetes[i], entetes[i + 1]);
        return mvc.perform(r).andReturn().getResponse();
    }

    private JsonNode resultat(String jeton, String methode, String params) throws Exception {
        MockHttpServletResponse r = mcp(jeton, "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"" + methode + "\",\"params\":" + params + "}");
        assertEquals(200, r.getStatus(), r.getContentAsString());
        JsonNode n = json.readTree(r.getContentAsString(StandardCharsets.UTF_8));
        assertEquals(7, n.get("id").asInt());
        return n;
    }

    @Test
    void un_assistant_s_initialise_liste_les_outils_et_lit_ce_que_son_client_a_cree() throws Exception {
        JsonNode client = client("Assistant IA");
        String jeton = jeton(client);

        JsonNode init = resultat(jeton, "initialize",
                "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"essai\",\"version\":\"1\"}}").get("result");
        assertEquals("2025-06-18", init.get("protocolVersion").asText());
        assertEquals("vina-clm", init.get("serverInfo").get("name").asText());
        assertFalse(init.get("capabilities").get("tools").get("listChanged").asBoolean());
        assertEquals("2025-11-25", resultat(jeton, "initialize", "{\"protocolVersion\":\"1999-01-01\"}").get("result")
                .get("protocolVersion").asText(), "version inconnue ramenée à la plus récente");
        assertEquals(202, mcp(jeton, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}",
                "MCP-Protocol-Version", "2025-06-18").getStatus());
        assertTrue(resultat(jeton, "ping", "{}").get("result").isObject());

        JsonNode outils = resultat(jeton, "tools/list", "{}").get("result").get("tools");
        assertEquals(3, outils.size());
        for (JsonNode o : outils) {
            assertTrue(o.get("annotations").get("readOnlyHint").asBoolean(), o.get("name").asText() + " en lecture seule");
            assertEquals("object", o.get("inputSchema").get("type").asText());
        }

        JsonNode modeles = resultat(jeton, "tools/call", "{\"name\":\"lister_modeles\",\"arguments\":{}}").get("result");
        assertFalse(modeles.get("isError").asBoolean());
        assertTrue(modeles.get("structuredContent").get("modeles").isArray());
        assertEquals("text", modeles.get("content").get(0).get("type").asText());

        String demande = "{\"titre\":\"Entente MCP\",\"referenceExterne\":\"MCP-1\",\"initiateur\":\"initiateur@iq.example\","
                + "\"documents\":[{\"nom\":\"entente.pdf\",\"contenuBase64\":\"" + PDF + "\"}],"
                + "\"signataires\":[{\"nom\":\"Alex Candidat\",\"courriel\":\"alex@exemple.ca\",\"roleContrat\":\"SIGNATAIRE_1\",\"telephone\":\"+15145550101\",\"externe\":true}],"
                + "\"lancer\":false}";
        MockHttpServletResponse cree = mvc.perform(MockMvcRequestBuilders.post("/api/clm/integration/v1/trousses")
                .header("Authorization", "Bearer " + jeton).contentType(MediaType.APPLICATION_JSON).content(demande)).andReturn().getResponse();
        assertEquals(201, cree.getStatus(), cree.getContentAsString());
        long trousse = json.readTree(cree.getContentAsString()).get("id").asLong();

        JsonNode lue = resultat(jeton, "tools/call", "{\"name\":\"lire_trousse\",\"arguments\":{\"trousseId\":" + trousse + "}}").get("result");
        assertFalse(lue.get("isError").asBoolean(), lue.toString());
        assertEquals("MCP-1", lue.get("structuredContent").get("referenceExterne").asText());

        String autre = jeton(client("Autre assistant"));
        JsonNode etrangere = resultat(autre, "tools/call", "{\"name\":\"lire_trousse\",\"arguments\":{\"trousseId\":" + trousse + "}}").get("result");
        assertTrue(etrangere.get("isError").asBoolean(), "un client ne lit que ce qu'il a créé");

        JsonNode lot = resultat(jeton, "tools/call", "{\"name\":\"lire_lot\",\"arguments\":{\"lotId\":999999}}").get("result");
        assertTrue(lot.get("isError").asBoolean());
        JsonNode mauvais = resultat(jeton, "tools/call", "{\"name\":\"lire_trousse\",\"arguments\":{\"trousseId\":\"abc\"}}").get("result");
        assertTrue(mauvais.get("isError").asBoolean());
        assertTrue(mauvais.get("content").get(0).get("text").asText().contains("trousseId"));
    }

    @Test
    void le_protocole_refuse_ce_qui_n_est_pas_prevu() throws Exception {
        String jeton = jeton(client("Assistant protocole"));
        assertEquals(-32601, resultat(jeton, "resources/list", "{}").get("error").get("code").asInt());
        assertEquals(-32602, resultat(jeton, "tools/call", "{\"name\":\"supprimer_tout\"}").get("error").get("code").asInt());
        assertEquals(-32602, resultat(jeton, "tools/call", "{\"name\":\"lister_modeles\",\"arguments\":[1]}").get("error").get("code").asInt());
        assertEquals(-32600, json.readTree(mcp(jeton, "{\"id\":1,\"method\":\"ping\"}").getContentAsString()).get("error").get("code").asInt());
        assertEquals(-32600, json.readTree(mcp(jeton, "{\"jsonrpc\":\"2.0\",\"id\":1}").getContentAsString()).get("error").get("code").asInt());
        assertEquals(202, mcp(jeton, "{\"jsonrpc\":\"2.0\",\"result\":{}}").getStatus(), "une réponse du client n'appelle rien");

        MockHttpServletResponse lot = mcp(jeton, "[{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}]");
        assertEquals(400, lot.getStatus());
        MockHttpServletResponse illisible = mcp(jeton, "{pas du json");
        assertEquals(400, illisible.getStatus());
        assertEquals(-32700, json.readTree(illisible.getContentAsString()).get("error").get("code").asInt());

        assertEquals(400, mcp(jeton, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}", "MCP-Protocol-Version", "2024-01-01").getStatus());
        assertEquals(403, mcp(jeton, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}", "Origin", "https://attaquant.example").getStatus(),
                "DNS rebinding");
        assertEquals(200, mcp(jeton, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}", "Origin", "http://localhost:5173").getStatus());

        MockHttpServletResponse anonyme = mcp(null, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}");
        assertEquals(401, anonyme.getStatus());
        assertEquals("Bearer", anonyme.getHeader("WWW-Authenticate"));
        assertEquals(401, mcp("faux.jeton", "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}").getStatus());
        assertEquals(405, mvc.perform(MockMvcRequestBuilders.get(MCP)).andReturn().getResponse().getStatus());
    }
}
