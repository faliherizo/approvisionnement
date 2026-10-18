package qc.levis.contrat.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
 * Validation bout en bout du flux d'authentification SAML 2.0 SP-initiated contre le {@link FakeSamlIdp} :
 * génération de l'AuthnRequest (redirection), validation d'une assertion réellement signée (XML-DSig
 * contre le certificat de l'IdP), restriction d'audience, auto-provisionnement du compte et ouverture
 * de session — sans IdP réel. Le flux complet est ainsi vérifié en test, y compris le rejet d'une
 * signature altérée. Seule une intégration avec un IdP de production sort du périmètre testé.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SamlFlowTest {

    private static final String PW = "Passw0rd!";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private final FakeSamlIdp idp = new FakeSamlIdp();

    @BeforeEach
    void setUp() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        String body = "{\"actif\":true,\"idpEntityId\":\"" + idp.idpEntityId + "\","
                + "\"idpSsoUrl\":\"https://idp.test/sso\","
                + "\"idpCertificat\":\"" + FakeSamlIdp.CERT_PEM.replace("\n", "\\n") + "\","
                + "\"spEntityId\":\"" + idp.spEntityId + "\",\"boutonLibelle\":\"Connexion SAML\","
                + "\"autoProvision\":true,\"roleParDefaut\":\"REDACTEUR\"}";
        mvc.perform(put("/api/parametres/integration-saml").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
    }

    @Test
    void test_configuration_reussie() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        JsonNode r = json.readTree(mvc.perform(post("/api/parametres/integration-saml/test").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn()
                .getResponse().getContentAsString());
        assertTrue(r.get("succes").asBoolean(), "la config complète avec certificat X.509 valide passe le test");
    }

    @Test
    void config_publique_exposeEtatSaml_selonTenant() throws Exception {
        JsonNode cfg = json.readTree(mvc.perform(get("/api/auth/config").param("slug", "levis"))
                .andReturn().getResponse().getContentAsString());
        assertTrue(cfg.get("samlActif").asBoolean());
        assertEquals("Connexion SAML", cfg.get("samlLibelle").asText());
    }

    @Test
    void login_redirigeVersIdp_avecAuthnRequest() throws Exception {
        MvcResult r = mvc.perform(get("/api/auth/saml/login").param("slug", "levis"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String location = r.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.startsWith("https://idp.test/sso"));
        assertTrue(location.contains("SAMLRequest="));
        assertTrue(location.contains("RelayState="));
    }

    @Test
    void fluxComplet_callback_valideProvisionneEtOuvreSession() throws Exception {
        String relayState = relayStateDepuisLogin();
        MvcResult cb = mvc.perform(post("/api/auth/saml/callback")
                        .param("SAMLResponse", idp.reponseBase64(false))
                        .param("RelayState", relayState))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andReturn();
        Cookie session = cb.getResponse().getCookie("ACCESS_TOKEN");
        assertNotNull(session, "une session doit être ouverte");
        assertTrue(session.isHttpOnly());

        JsonNode me = json.readTree(mvc.perform(get("/api/auth/me").cookie(session))
                .andReturn().getResponse().getContentAsString());
        assertEquals(idp.courriel, me.get("email").asText());
    }

    @Test
    void callback_signatureAlteree_pasDeSession() throws Exception {
        String relayState = relayStateDepuisLogin();
        MvcResult cb = mvc.perform(post("/api/auth/saml/callback")
                        .param("SAMLResponse", idp.reponseBase64(true)) // corrompue après signature
                        .param("RelayState", relayState))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?saml=erreur"))
                .andReturn();
        assertNull(cb.getResponse().getCookie("ACCESS_TOKEN"));
    }

    @Test
    void callback_reponseNonSignee_pasDeSession() throws Exception {
        String relayState = relayStateDepuisLogin();
        mvc.perform(post("/api/auth/saml/callback")
                        .param("SAMLResponse", idp.reponseNonSigneeBase64())
                        .param("RelayState", relayState))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?saml=erreur"));
    }

    @Test
    void login_tenantSansConfig_redirigeIndisponible() throws Exception {
        mvc.perform(get("/api/auth/saml/login")) // pas de slug → aucun tenant résolu
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?saml=indisponible"));
    }

    /** Démarre le flux et renvoie le RelayState signé (porteur du tenant) émis par /login. */
    private String relayStateDepuisLogin() throws Exception {
        MvcResult login = mvc.perform(get("/api/auth/saml/login").param("slug", "levis")).andReturn();
        String location = login.getResponse().getHeader("Location");
        return params(URI.create(location)).get("RelayState");
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
