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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Annexe 10 IQ, AF-46 (RST-49), vraie pile : la priorité d'une trousse, choisie ou déduite du risque, trie et filtre le
 * suivi, son export et « Mes tâches ».
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.clm.planificateur.actif=false")
class PrioriteTrousseIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final byte[] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
            + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
            + "trailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie initiateur;
    private Cookie validateur;
    private Cookie affaires;
    private String marque;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        validateur = login("validateur@iq.example");
        affaires = login("admin.affaires@iq.example");
        marque = "Prio" + System.nanoTime();
    }

    @Test
    void la_priorite_choisie_ou_deduite_du_risque_est_visible_au_detail_et_modifiable() throws Exception {
        JsonNode deduite = creer("Déduite", "ELEVE", null);
        assertEquals("HAUTE", deduite.get("priorite").asText(), "risque élevé → priorité haute");
        assertFalse(deduite.get("prioriteChoisie").asBoolean());

        JsonNode choisie = creer("Choisie", "FAIBLE", "critique");
        assertEquals("CRITIQUE", choisie.get("priorite").asText());
        assertTrue(choisie.get("prioriteChoisie").asBoolean());

        long id = choisie.at("/resume/id").asLong();
        JsonNode inchangee = corps(appel("PUT", "/api/clm/trousses/" + id, initiateur, "{\"echeance\":\"2027-01-31\"}"), 200);
        assertEquals("CRITIQUE", inchangee.get("priorite").asText(), "priorité absente : inchangée");
        JsonNode revenue = corps(appel("PUT", "/api/clm/trousses/" + id, initiateur, "{\"priorite\":\"\"}"), 200);
        assertEquals("BASSE", revenue.get("priorite").asText(), "priorité vide : déduite du risque faible");
        assertFalse(revenue.get("prioriteChoisie").asBoolean());

        JsonNode erreur = corps(appel("PUT", "/api/clm/trousses/" + id, initiateur, "{\"priorite\":\"URGENTE\"}"), 400);
        assertTrue(erreur.toString().contains("Priorité inconnue"), erreur.toString());
    }

    @Test
    void le_suivi_se_trie_et_se_filtre_par_priorite_et_l_export_la_porte() throws Exception {
        creer("Basse", "FAIBLE", null);
        creer("Critique", "MOYEN", "CRITIQUE");
        creer("Haute", "ELEVE", null);

        JsonNode trie = corps(appel("GET", "/api/clm/suivi?q=" + marque + "&tri=priorite", affaires, null), 200);
        assertEquals(List.of("CRITIQUE", "HAUTE", "BASSE"), valeurs(trie.get("lignes"), "priorite"));
        JsonNode recent = corps(appel("GET", "/api/clm/suivi?q=" + marque, affaires, null), 200);
        assertEquals(List.of("HAUTE", "CRITIQUE", "BASSE"), valeurs(recent.get("lignes"), "priorite"), "par défaut, les plus récentes d'abord");

        JsonNode hautes = corps(appel("GET", "/api/clm/suivi?q=" + marque + "&priorite=HAUTE", affaires, null), 200);
        assertEquals(List.of(marque + " Haute"), valeurs(hautes.get("lignes"), "titre"));
        assertEquals(0, corps(appel("GET", "/api/clm/suivi?q=" + marque + "&priorite=AUCUNE", affaires, null), 200).get("lignes").size(),
                "un code inconnu ne retient rien");

        String csv = appel("GET", "/api/clm/suivi/export?q=" + marque + "&tri=priorite", affaires, null).getContentAsString(StandardCharsets.UTF_8);
        String[] lignes = csv.split("\n");
        assertTrue(lignes[0].contains("reference;titre;priorite;statut"), lignes[0]);
        assertTrue(lignes[1].contains(";CRITIQUE;") && lignes[3].contains(";BASSE;"), csv);
    }

    @Test
    void mes_taches_presentent_d_abord_les_trousses_les_plus_prioritaires_et_se_filtrent() throws Exception {
        lancer(creer("Normale", "MOYEN", null));
        lancer(creer("Critique", "FAIBLE", "CRITIQUE"));
        lancer(creer("Basse", "FAIBLE", null));

        List<String> ordre = new ArrayList<>();
        corps(appel("GET", "/api/clm/taches", validateur, null), 200).forEach(t -> {
            if (t.get("titre").asText().startsWith(marque)) {
                ordre.add(t.get("priorite").asText());
            }
        });
        assertEquals(List.of("CRITIQUE", "NORMALE", "BASSE"), ordre);

        JsonNode critiques = corps(appel("GET", "/api/clm/taches?priorite=critique", validateur, null), 200);
        assertTrue(critiques.size() >= 1);
        critiques.forEach(t -> assertEquals("CRITIQUE", t.get("priorite").asText()));
        assertEquals(400, appel("GET", "/api/clm/taches?priorite=URGENTE", validateur, null).getStatus());
        corps(appel("GET", "/api/clm/taches?filtre=terminees&priorite=BASSE", validateur, null), 200)
                .forEach(t -> assertEquals("BASSE", t.get("priorite").asText()));
    }

    private JsonNode creer(String titre, String risque, String priorite) throws Exception {
        String p = priorite == null ? "" : ",\"priorite\":\"" + priorite + "\"";
        return corps(appel("POST", "/api/clm/trousses", initiateur,
                "{\"titre\":\"" + marque + " " + titre + "\",\"risque\":\"" + risque + "\"" + p + "}"), 200);
    }

    private void lancer(JsonNode trousse) throws Exception {
        long id = trousse.at("/resume/id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", "c.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]}]"), 200);
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
    }

    private static List<String> valeurs(JsonNode lignes, String champ) {
        List<String> res = new ArrayList<>();
        lignes.forEach(l -> res.add(l.get(champ).asText()));
        return res;
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
        return json.readTree(s.isEmpty() ? "{}" : s);
    }
}
