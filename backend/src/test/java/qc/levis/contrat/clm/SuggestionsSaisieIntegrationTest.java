package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
 * Saisie assistée (F-04) sur la vraie pile, dans l'organisation IQ : un champ texte propose les valeurs que l'initiateur a
 * déjà retenues dans ses propres trousses, de la plus fréquente à la moins fréquente ; jamais celles d'un collègue.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class SuggestionsSaisieIntegrationTest {

    private static final String PW = "Passw0rd!";

    @TestConfiguration
    static class Silence {
        @Bean
        @Primary
        NotificateurClm notificateurMuet() {
            return (canal, destinataire, sujet, corps) -> "SIMULE";
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void un_champ_texte_propose_les_valeurs_deja_retenues_par_l_initiateur_seulement() throws Exception {
        Cookie initiateur = login("initiateur@iq.example");
        Cookie validateur = login("validateur@iq.example");
        long modele = modelePublie(initiateur, login("admin.affaires@iq.example"));

        for (String demandeur : List.of("Acme inc.", "Boréal ltée", "Acme inc.")) {
            long t = trousse(initiateur, modele);
            corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur,
                    "{\"valeurs\":{\"demandeur\":\"" + demandeur + "\",\"programme\":\"ESSOR\"}}"), 200);
        }
        long nouvelle = trousse(initiateur, modele);

        JsonNode proposees = lire("/api/clm/trousses/" + nouvelle + "/suggestions/demandeur", initiateur);
        assertEquals(List.of("Acme inc.", "Boréal ltée"), textes(proposees), "la plus fréquente d'abord");
        assertEquals(0, lire("/api/clm/trousses/" + nouvelle + "/suggestions/programme", initiateur).size(), "une liste fermée n'a pas de suggestion");
        assertEquals(404, appel("GET", "/api/clm/trousses/" + nouvelle + "/suggestions/inconnu", initiateur, null).getStatus());

        long autre = trousse(validateur, modele);
        assertEquals(0, lire("/api/clm/trousses/" + autre + "/suggestions/demandeur", validateur).size(),
                "les valeurs d'un collègue ne sont jamais proposées");
        assertTrue(appel("GET", "/api/clm/trousses/" + nouvelle + "/suggestions/demandeur", validateur, null).getStatus() >= 403,
                "la trousse d'autrui n'est pas accessible");
    }

    private long modelePublie(Cookie redacteur, Cookie admin) throws Exception {
        long id = corps(appel("POST", "/api/clm/modeles", redacteur, "{\"nom\":\"Convention d'aide\",\"corps\":\"Entre IQ et {{demandeur}} ({{programme}})\","
                + "\"champs\":[{\"cle\":\"demandeur\",\"libelle\":\"Demandeur\",\"type\":\"TEXTE\"},"
                + "{\"cle\":\"programme\",\"libelle\":\"Programme\",\"type\":\"LISTE\",\"options\":[\"ESSOR\",\"IMPULSION\"]}],"
                + "\"clauses\":[],\"parcours\":[]}"), 200).get("id").asLong();
        corps(appel("POST", "/api/clm/modeles/" + id + "/soumettre", redacteur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + id + "/publier", admin, "{}"), 200);
        return id;
    }

    private long trousse(Cookie qui, long modele) throws Exception {
        return corps(appel("POST", "/api/clm/trousses", qui, "{\"titre\":\"Aide e2e\",\"modeleId\":" + modele + "}"), 200)
                .get("resume").get("id").asLong();
    }

    private static List<String> textes(JsonNode n) {
        List<String> l = new ArrayList<>();
        n.forEach(x -> l.add(x.asText()));
        return l;
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
