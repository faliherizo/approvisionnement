package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * RST-53 (SEC-27) : étiquettes Microsoft Purview, vraie pile (Purview et Microsoft 365 simulés). L'administration choisit
 * les étiquettes par défaut et par type de contrat dans le catalogue du locataire ; le document déposé puis sa version
 * signée les portent ; le dépôt SharePoint les reçoit ; la conservation ne détruit pas avant la fin de la rétention.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-purview"})
class PurviewIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String URL = "/api/clm/admin/purview";
    private static final String CONFIDENTIEL = "b1a5d0c2-0000-4000-8000-000000000003";
    private static final String INTERNE = "b1a5d0c2-0000-4000-8000-000000000002";
    private static final byte[] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
            + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
            + "trailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie initiateur;
    private Cookie validateur;
    private Cookie affaires;
    private Cookie technique;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        validateur = login("validateur@iq.example");
        affaires = login("admin.affaires@iq.example");
        technique = login("admin.technique@iq.example");
    }

    @Test
    void l_administration_choisit_les_etiquettes_dans_le_catalogue_du_locataire() throws Exception {
        assertEquals(403, appel("GET", URL + "/catalogue", initiateur, null).getStatus());
        JsonNode catalogue = corps(appel("GET", URL + "/catalogue", affaires, null), 200);
        assertTrue(catalogue.toString().contains("\"Confidentiel\""));
        assertTrue(catalogue.toString().contains("{\"type\":\"RETENTION\",\"id\":\"Contrats - 7 ans\",\"nom\":\"Contrats - 7 ans\",\"jours\":2557}"));
        assertEquals(400, appel("PUT", URL + "/politiques", affaires, "{\"sensibiliteId\":\"inconnue\"}").getStatus());
        assertEquals(400, appel("PUT", URL + "/politiques", affaires, "{\"typeContrat\":\"PRET\"}").getStatus(), "au moins une étiquette");

        JsonNode p = corps(appel("PUT", URL + "/politiques", technique, "{\"typeContrat\":\"PRET\",\"sensibiliteId\":\"" + INTERNE + "\"}"), 200);
        assertEquals("Interne", p.get("sensibilite").asText());
        assertEquals("admin.technique@iq.example", p.get("modifiePar").asText());
        JsonNode remplacee = corps(appel("PUT", URL + "/politiques", technique, "{\"typeContrat\":\"PRET\",\"retention\":\"Contrats - 7 ans\"}"), 200);
        assertEquals(p.get("id").asLong(), remplacee.get("id").asLong(), "remplace, ne duplique pas");
        assertTrue(remplacee.get("sensibilite").isNull());
        assertEquals(204, appel("DELETE", URL + "/politiques/" + p.get("id").asLong(), affaires, null).getStatus());
        assertFalse(corps(appel("GET", URL + "/politiques", affaires, null), 200).toString().contains("\"PRET\""));
        assertEquals(404, appel("DELETE", URL + "/politiques/" + p.get("id").asLong(), affaires, null).getStatus());
    }

    @Test
    void le_document_signe_garde_ses_etiquettes_les_propage_a_sharepoint_et_la_conservation_s_aligne() throws Exception {
        String m365 = "/api/clm/admin/integrations/m365";
        corps(appel("PUT", URL + "/politiques", affaires, "{\"sensibiliteId\":\"" + INTERNE + "\"}"), 200);
        corps(appel("PUT", URL + "/politiques", affaires, "{\"typeContrat\":\"SUBVENTION\",\"sensibiliteId\":\"" + CONFIDENTIEL
                + "\",\"retention\":\"Contrats - 7 ans\"}"), 200);
        corps(appel("PUT", "/api/clm/admin/conservation/regles", affaires, "{\"typeContrat\":\"SUBVENTION\",\"dureeAns\":1,\"sortFinal\":\"DETRUIRE\"}"), 200);
        try {
            corps(appel("PUT", m365, technique, "{\"actif\":true,\"bibliotheque\":\"SHAREPOINT\",\"dossier\":\"Purview\","
                    + "\"deposerPreuve\":false,\"avisTeams\":false}"), 200);
            long id = trousseTerminee("Purview", "SUBVENTION");
            JsonNode docs = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).get("documents");
            docs.forEach(d -> {
                assertEquals("Confidentiel", d.get("etiquetteSensibilite").asText(), "politique du type de contrat");
                assertEquals("Contrats - 7 ans", d.get("etiquetteRetention").asText());
            });
            assertTrue(docs.toString().contains("\"statut\":\"SIGNE\""), "la version signée garde les étiquettes");

            assertTrue(corps(appel("POST", m365 + "/relayer", technique, null), 200).get("deposes").asInt() >= 1);
            JsonNode depot = null;
            for (JsonNode d : corps(appel("GET", m365 + "/depots", technique, null), 200)) {
                depot = d.get("trousseId").asLong() == id ? d : depot;
            }
            assertNotNull(depot);
            assertEquals("Confidentiel · Contrats - 7 ans — apposées", depot.get("etiquette").asText());
            assertTrue(corps(appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null), 200).toString().contains("ETIQUETTE_PURVIEW"));

            JsonNode suivi = null;
            for (JsonNode s : corps(appel("GET", "/api/clm/admin/conservation/trousses?echues=false", affaires, null), 200)) {
                suivi = s.get("trousseId").asLong() == id ? s : suivi;
            }
            assertNotNull(suivi);
            LocalDate cloture = Instant.parse(suivi.get("clotureLe").asText()).atZone(ZoneId.of("America/Toronto")).toLocalDate();
            assertEquals(cloture.plusDays(2557).toString(), suivi.get("echeance").asText(), "rétention Purview (7 ans) plutôt que la règle (1 an)");

            long sansType = trousse("Purview défaut", null);
            JsonNode doc = deposer(sansType);
            assertEquals("Interne", doc.get("etiquetteSensibilite").asText(), "politique par défaut");
            assertTrue(doc.get("etiquetteRetention").isNull());
        } finally {
            appel("PUT", m365, technique, "{\"actif\":false,\"bibliotheque\":\"SHAREPOINT\",\"dossier\":\"Contrats signés\","
                    + "\"deposerPreuve\":true,\"avisTeams\":true}");
        }
    }

    private long trousse(String titre, String type) throws Exception {
        String corps = "{\"titre\":\"" + titre + "\",\"risque\":\"FAIBLE\"" + (type == null ? "" : ",\"typeContrat\":\"" + type + "\"") + "}";
        return corps(appel("POST", "/api/clm/trousses", initiateur, corps), 200).get("resume").get("id").asLong();
    }

    private JsonNode deposer(long trousse) throws Exception {
        return corps(mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + trousse + "/documents")
                .file(new MockMultipartFile("fichier", "c.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse(), 200);
    }

    private long trousseTerminee(String titre, String type) throws Exception {
        long id = trousse(titre, type);
        deposer(id);
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]}]"), 200);
        JsonNode lance = corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/etapes/" + lance.at("/etapes/0/id").asLong() + "/decider", validateur, "{\"decision\":\"APPROUVE\"}"), 200);
        assertEquals("TERMINEE", corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).at("/resume/statut").asText());
        return id;
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
