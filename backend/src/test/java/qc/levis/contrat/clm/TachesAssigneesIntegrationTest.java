package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.port.NotificateurClm;

/**
 * Tâches assignées sur la vraie pile (H2 + Liquibase + sécurité), dans l'organisation IQ : annuaire, priorité tirée de la
 * liste que l'organisation administre, message à l'assigné, cycle de vie, refus et trace au journal de la trousse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class TachesAssigneesIntegrationTest {

    private static final String PW = "Passw0rd!";
    static final List<String[]> ENVOIS = Collections.synchronizedList(new ArrayList<>());

    @TestConfiguration
    static class Capture {
        @Bean
        @Primary
        NotificateurClm notificateurCapture() {
            return (canal, destinataire, sujet, corps) -> {
                ENVOIS.add(new String[]{canal, destinataire, sujet, corps});
                return "SIMULE";
            };
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie initiateur;
    private Cookie validateur;
    private Cookie approbateur;
    private Cookie lecteur;
    private Cookie admin;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        validateur = login("validateur@iq.example");
        approbateur = login("approbateur@iq.example");
        lecteur = login("signataire@iq.example");
        admin = login("admin.ti@iq.example");
        ENVOIS.clear();
    }

    @Test
    void confier_suivre_et_clore_une_tache_rattachee_a_une_trousse() throws Exception {
        JsonNode membres = lire("/api/clm/membres", initiateur);
        assertTrue(membres.findValuesAsText("courriel").contains("validateur@iq.example"), membres.toString());
        assertFalse(membres.findValuesAsText("courriel").contains("admin@levis.qc.ca"), "l'annuaire ne sort pas de l'organisation");

        JsonNode creee = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Prêt Essor — tâches\",\"risque\":\"FAIBLE\"}"), 200);
        long trousse = creee.at("/resume/id").asLong();
        JsonNode t = corps(appel("POST", "/api/clm/taches-assignees", initiateur, """
                {"trousseId":%d,"titre":"Relire la clause de garantie","assigneA":"Validateur@IQ.example","priorite":"haute","echeance":"2030-01-15"}
                """.formatted(trousse)), 200);
        long id = t.get("id").asLong();
        assertEquals("HAUTE", t.get("priorite").asText());
        assertEquals("validateur@iq.example", t.get("assigneA").asText());
        assertEquals(creee.at("/resume/reference").asText(), t.get("reference").asText(), "la tâche porte la référence de sa trousse");
        assertTrue(ENVOIS.stream().anyMatch(e -> "validateur@iq.example".equals(e[1]) && e[2].contains("Relire la clause de garantie")),
                "l'assigné reçoit le message « Action requise » de l'organisation");

        assertEquals(400, appel("POST", "/api/clm/taches-assignees", initiateur,
                "{\"titre\":\"X\",\"assigneA\":\"validateur@iq.example\",\"priorite\":\"CRITIQUE\"}").getStatus(), "priorité hors liste");
        assertEquals(400, appel("POST", "/api/clm/taches-assignees", initiateur,
                "{\"titre\":\"X\",\"assigneA\":\"admin@levis.qc.ca\"}").getStatus(), "personne d'une autre organisation");
        assertEquals(403, appel("POST", "/api/clm/taches-assignees", lecteur,
                "{\"titre\":\"X\",\"assigneA\":\"validateur@iq.example\"}").getStatus(), "le lecteur ne confie pas de tâche");

        assertEquals(1, lire("/api/clm/taches-assignees", validateur).size());
        assertEquals(403, appel("POST", "/api/clm/taches-assignees/" + id + "/action", initiateur, "{\"action\":\"DEMARRER\"}").getStatus());
        assertEquals(403, appel("POST", "/api/clm/taches-assignees/" + id + "/action", approbateur, "{\"action\":\"TERMINER\"}").getStatus(),
                "un tiers n'agit pas sur la tâche");
        assertEquals("EN_COURS", action(id, validateur, "DEMARRER").get("statut").asText());
        assertEquals("TERMINEE", action(id, validateur, "TERMINER").get("statut").asText());
        assertEquals(409, appel("POST", "/api/clm/taches-assignees/" + id + "/action", validateur, "{\"action\":\"TERMINER\"}").getStatus());
        assertEquals(1, lire("/api/clm/taches-assignees?filtre=terminees", validateur).size());
        assertEquals(1, lire("/api/clm/taches-assignees?filtre=creees", initiateur).size());
        assertEquals(400, appel("GET", "/api/clm/taches-assignees?filtre=toutes", initiateur, null).getStatus());

        assertEquals(1, lire("/api/clm/trousses/" + trousse + "/taches-assignees", initiateur).size());
        JsonNode historique = lire("/api/clm/trousses/" + trousse + "/historique", initiateur);
        List<String> types = historique.findValuesAsText("type");
        assertTrue(types.containsAll(List.of("TACHE_CREEE", "TACHE_EN_COURS", "TACHE_TERMINEE")), types.toString());
    }

    @Test
    void reassigner_puis_annuler_une_tache_libre() throws Exception {
        long id = corps(appel("POST", "/api/clm/taches-assignees", approbateur,
                "{\"titre\":\"Valider le budget annuel\",\"assigneA\":\"validateur@iq.example\"}"), 200).get("id").asLong();
        ENVOIS.clear();
        assertEquals("initiateur@iq.example", corps(appel("POST", "/api/clm/taches-assignees/" + id + "/reassigner", validateur,
                "{\"vers\":\"initiateur@iq.example\",\"motif\":\"Congé\"}"), 200).get("assigneA").asText());
        assertTrue(ENVOIS.stream().anyMatch(e -> "initiateur@iq.example".equals(e[1])), "le nouvel assigné est prévenu");
        assertEquals(403, appel("POST", "/api/clm/taches-assignees/" + id + "/reassigner", validateur,
                "{\"vers\":\"validateur@iq.example\"}").getStatus(), "l'ancien assigné n'intervient plus");
        assertEquals(403, appel("POST", "/api/clm/taches-assignees/" + id + "/action", initiateur, "{\"action\":\"ANNULER\"}").getStatus(),
                "seul le créateur annule");
        assertEquals("ANNULEE", action(id, approbateur, "ANNULER").get("statut").asText());
        assertEquals(404, appel("POST", "/api/clm/taches-assignees/999999/action", approbateur, "{\"action\":\"ANNULER\"}").getStatus());
    }

    @Test
    void la_liste_des_priorites_se_configure_dans_l_administration_de_l_organisation() throws Exception {
        long critique = corps(appel("POST", "/api/admin/reference", admin,
                "{\"categorie\":\"CLM_PRIORITE_TACHE\",\"code\":\"CRITIQUE\",\"libelle\":\"Critique\",\"ordre\":5}"), 200).get("id").asLong();
        try {
            assertEquals("CRITIQUE", corps(appel("POST", "/api/clm/taches-assignees", initiateur,
                    "{\"titre\":\"Signature urgente\",\"assigneA\":\"validateur@iq.example\",\"priorite\":\"critique\"}"), 200)
                    .get("priorite").asText(), "la valeur ajoutée par l'organisation est aussitôt acceptée");
        } finally {
            assertEquals(200, appel("DELETE", "/api/admin/reference/" + critique, admin, null).getStatus());
        }
        assertEquals(400, appel("POST", "/api/clm/taches-assignees", initiateur,
                "{\"titre\":\"X\",\"assigneA\":\"validateur@iq.example\",\"priorite\":\"CRITIQUE\"}").getStatus(), "retirée, elle est refusée");
    }

    private JsonNode action(long id, Cookie qui, String action) throws Exception {
        return corps(appel("POST", "/api/clm/taches-assignees/" + id + "/action", qui, "{\"action\":\"" + action + "\"}"), 200);
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

    private JsonNode lire(String url, Cookie qui) throws Exception {
        return corps(appel("GET", url, qui, null), 200);
    }

    private JsonNode corps(MockHttpServletResponse r, int attendu) throws Exception {
        String s = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(attendu, r.getStatus(), s);
        return json.readTree(s);
    }
}
