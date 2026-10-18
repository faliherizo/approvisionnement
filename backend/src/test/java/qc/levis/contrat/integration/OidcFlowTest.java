package qc.levis.contrat.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Validation bout en bout du flux d'authentification OIDC (authorization code + PKCE) contre le
 * {@link FakeOidcServer} : découverte, redirection vers l'IdP, échange du code, validation du jeton
 * (signature JWKS, émetteur, audience, nonce) et auto-provisionnement du compte, sans IdP réel.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OidcFlowTest {

    private static final String PW = "Passw0rd!";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private FakeOidcServer idp;

    @BeforeEach
    void setUp() throws Exception {
        idp = new FakeOidcServer();
        idp.demarrer();
        Cookie admin = login("admin@levis.qc.ca", PW);
        enregistrerConfig(admin, true);
    }

    @AfterEach
    void tearDown() {
        idp.arreter();
    }

    private void enregistrerConfig(Cookie admin, boolean actif) throws Exception {
        String body = "{\"actif\":" + actif + ",\"issuer\":\"" + idp.issuer() + "\","
                + "\"clientId\":\"" + idp.clientId + "\",\"clientSecret\":\"secret-oidc\","
                + "\"boutonLibelle\":\"Connexion gouvernementale\",\"autoProvision\":true,"
                + "\"roleParDefaut\":\"REDACTEUR\"}";
        mvc.perform(put("/api/parametres/integration-oidc").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
    }

    @Test
    void test_decouverte_reussie() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        JsonNode r = json.readTree(mvc.perform(post("/api/parametres/integration-oidc/test").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn()
                .getResponse().getContentAsString());
        assertTrue(r.get("succes").asBoolean());
    }

    @Test
    void config_publique_exposeEtatOidc_selonTenant() throws Exception {
        JsonNode cfg = json.readTree(mvc.perform(get("/api/auth/config").param("slug", "levis"))
                .andReturn().getResponse().getContentAsString());
        assertTrue(cfg.get("oidcActif").asBoolean());
        assertEquals("Connexion gouvernementale", cfg.get("oidcLibelle").asText());
    }

    @Test
    void login_redirigeVersIdp_avecParametresPkce() throws Exception {
        MvcResult r = mvc.perform(get("/api/auth/oidc/login").param("slug", "levis"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String location = r.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.startsWith(idp.issuer() + "/authorize"));
        assertTrue(location.contains("code_challenge_method=S256"));
        assertTrue(location.contains("response_type=code"));
        assertTrue(location.contains("state="));
        assertNotNull(r.getResponse().getCookie("OIDC_FLOW"));
    }

    @Test
    void fluxComplet_callback_valideProvisionneEtOuvreSession() throws Exception {
        // 1. Démarrage : redirection vers l'IdP + cookie de flux.
        MvcResult login = mvc.perform(get("/api/auth/oidc/login").param("slug", "levis")).andReturn();
        Cookie flow = login.getResponse().getCookie("OIDC_FLOW");
        String autorisation = login.getResponse().getHeader("Location");

        // 2. Autorisation chez l'IdP (le simulateur capture le nonce et redirige avec un code).
        Map<String, String> retour = suivreAutorisation(autorisation);

        // 3. Callback : échange du code, validation du jeton, provisionnement, session.
        MvcResult cb = mvc.perform(get("/api/auth/oidc/callback")
                        .param("code", retour.get("code")).param("state", retour.get("state"))
                        .cookie(flow))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andReturn();
        Cookie session = cb.getResponse().getCookie("ACCESS_TOKEN");
        assertNotNull(session, "une session doit être ouverte");
        assertTrue(session.isHttpOnly());

        // 4. La session porte bien le compte auto-provisionné.
        JsonNode me = json.readTree(mvc.perform(get("/api/auth/me").cookie(session))
                .andReturn().getResponse().getContentAsString());
        assertEquals(idp.courriel, me.get("email").asText());
    }

    @Test
    void callback_stateInvalide_pasDeSession() throws Exception {
        MvcResult login = mvc.perform(get("/api/auth/oidc/login").param("slug", "levis")).andReturn();
        Cookie flow = login.getResponse().getCookie("OIDC_FLOW");
        MvcResult cb = mvc.perform(get("/api/auth/oidc/callback")
                        .param("code", "FAKECODE").param("state", "state-falsifie").cookie(flow))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?oidc=erreur"))
                .andReturn();
        assertNull(cb.getResponse().getCookie("ACCESS_TOKEN"));
    }

    @Test
    void login_tenantSansConfig_redirigeIndisponible() throws Exception {
        mvc.perform(get("/api/auth/oidc/login")) // pas de slug → aucun tenant résolu
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?oidc=indisponible"));
    }

    /** Suit la redirection d'autorisation vers le simulateur (HTTP réel) et renvoie code + state. */
    private Map<String, String> suivreAutorisation(String autorisation) throws Exception {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(URI.create(autorisation)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, resp.statusCode());
        String location = resp.headers().firstValue("Location").orElseThrow();
        return params(URI.create(location));
    }

    private Map<String, String> params(URI uri) {
        Map<String, String> m = new HashMap<>();
        String q = uri.getRawQuery();
        if (q != null) {
            for (String p : q.split("&")) {
                String[] kv = p.split("=", 2);
                m.put(kv[0], kv.length > 1 ? java.net.URLDecoder.decode(kv[1],
                        java.nio.charset.StandardCharsets.UTF_8) : "");
            }
        }
        return m;
    }

    private Cookie login(String email, String motDePasse) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}";
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }
}
