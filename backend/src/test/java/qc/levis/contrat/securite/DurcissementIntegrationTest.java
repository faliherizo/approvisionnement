package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.support.Api;

/**
 * Corrections issues des tests d'intrusion, verrouillées ici : 401 sans session, en-têtes de sécurité, administration de
 * la plateforme fermée au niveau de l'URL, force brute freinée, jeton révoqué à la déconnexion, anti-SSRF.
 */
@SpringBootTest(properties = "app.securite.sortant.autorises=localhost:8180")
@AutoConfigureMockMvc
class DurcissementIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;

    @BeforeEach
    void client() { api = new Api(mvc, json); }

    @Test
    void sans_session_l_api_repond_401_et_pose_les_en_tetes_de_securite() throws Exception {
        MvcResult r = mvc.perform(MockMvcRequestBuilders.get("/api/clauses")).andReturn();
        assertEquals(401, r.getResponse().getStatus());
        MvcResult ok = mvc.perform(MockMvcRequestBuilders.get("/api/auth/me").cookie(api.connecter("redacteur@levis.qc.ca"))).andReturn();
        assertEquals("DENY", ok.getResponse().getHeader("X-Frame-Options"));
        assertEquals("nosniff", ok.getResponse().getHeader("X-Content-Type-Options"));
        assertTrue(ok.getResponse().getHeader("Content-Security-Policy").contains("frame-ancestors 'none'"));
        assertEquals("no-referrer", ok.getResponse().getHeader("Referrer-Policy"));
        assertTrue(ok.getResponse().getHeader("Cache-Control").contains("no-store"));
    }

    @Test
    void l_administration_de_la_plateforme_est_fermee_avant_meme_la_lecture_des_parametres() throws Exception {
        Cookie admin = api.connecter("admin@levis.qc.ca");
        assertEquals(403, api.get("/api/superadmin/reference", admin).statut(), "403 et non 400 malgré le paramètre manquant");
        assertEquals(403, api.get("/api/superadmin/tenants", admin).statut());
    }

    @Test
    void la_force_brute_est_suspendue_sans_reveler_si_le_compte_existe() throws Exception {
        Cookie admin = api.connecter("admin@levis.qc.ca");
        String cible = "brute." + UUID.randomUUID().toString().substring(0, 6) + "@levis.qc.ca";
        api.post("/api/admin/users", admin, Map.of("email", cible, "nom", "Cible", "motDePasse", Api.PW, "roles", List.of("LECTEUR"), "actif", true));
        for (int i = 0; i < 10; i++) assertEquals(401, api.post("/api/auth/login", null, Map.of("email", cible, "motDePasse", "Mauvais!" + i)).statut());
        Api.Reponse bloque = api.post("/api/auth/login", null, Map.of("email", cible, "motDePasse", Api.PW));
        assertEquals(429, bloque.statut(), "même le bon mot de passe attend la fin de la fenêtre");
        assertTrue(bloque.corps().get("error").asText().contains("Trop de tentatives"));
        String inconnu = "inconnu." + UUID.randomUUID() + "@levis.qc.ca";
        for (int i = 0; i < 10; i++) api.post("/api/auth/login", null, Map.of("email", inconnu, "motDePasse", "Mauvais!" + i));
        assertEquals(429, api.post("/api/auth/login", null, Map.of("email", inconnu, "motDePasse", "x")).statut(), "un compte inconnu est freiné pareil");
    }

    @Test
    void un_jeton_rejoue_apres_la_deconnexion_est_refuse() throws Exception {
        Cookie session = api.connecter("redacteur@levis.qc.ca");
        assertEquals(200, api.get("/api/auth/me", session).statut());
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/logout").cookie(session));
        assertEquals(401, api.get("/api/auth/me", session).statut());
    }

    @Test
    void le_test_de_decouverte_oidc_refuse_les_adresses_internes() throws Exception {
        Cookie admin = api.connecter("admin@levis.qc.ca");
        for (String cible : List.of("http://169.254.169.254/latest/meta-data/", "http://127.0.0.1:8100/", "file:///etc/passwd")) {
            Api.Reponse r = api.post("/api/parametres/integration-oidc/test", admin, Map.of("issuer", cible));
            assertEquals(400, r.statut(), cible);
            assertTrue(r.corps().get("message").asText().startsWith("Adresse refusée"), cible);
        }
    }
}
