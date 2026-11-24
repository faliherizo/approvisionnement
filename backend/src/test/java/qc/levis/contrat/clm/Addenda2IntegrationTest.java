package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.infrastructure.SalesforceSimulation;

/**
 * Addenda 2 (réponses d'IQ du 14 novembre 2026), organisation IQ, vraie pile : statut « retournée » transmis à
 * Salesforce quelle que soit l'étape visée, valeur de statut selon la correspondance de l'organisation (Q14), lecture
 * limitée aux champs déclarés (Q15), authentification JWT Bearer (Q11) et exportation vers un référentiel désigné (Q13).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class Addenda2IntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String DOSSIER = "a0B5e00000GRT001";
    private static final byte[] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
            + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired SalesforceSimulation simulation;

    private Cookie initiateur;
    private Cookie validateur;
    private Cookie approbateur;
    private Cookie admin;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        validateur = login("validateur@iq.example");
        approbateur = login("approbateur@iq.example");
        admin = login("admin.ti@iq.example");
    }

    @Test
    void le_retour_a_une_etape_anterieure_est_transmis_a_salesforce_avec_la_valeur_de_l_organisation() throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Convention Agri-Nord\",\"source\":\"SALESFORCE\","
                + "\"sfDossierId\":\"DEM-2026-0001\",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", "convention.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]},"
                + "{\"cle\":\"a1\",\"libelle\":\"Approbation\",\"phase\":2,\"type\":\"APPROBATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}]}]"), 200);
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        JsonNode d = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200);
        String reference = d.get("resume").get("reference").asText();
        decider(d.get("etapes").get(0).get("id").asLong(), validateur, "{\"decision\":\"APPROUVE\"}");
        decider(d.get("etapes").get(1).get("id").asLong(), approbateur, "{\"decision\":\"RETOUR\",\"commentaire\":\"Annexe B\",\"retourVers\":\"v1\"}");

        corps(appel("POST", "/api/clm/admin/integrations/salesforce/relayer", admin, "{}"), 200);
        Map<String, PortSalesforce.Statut> recus = new LinkedHashMap<>();
        simulation.statutsRecus().stream().filter(s -> reference.equals(s.donnees().get("reference")))
                .forEach(s -> recus.put(s.evenement(), s));
        assertEquals("Lancée", recus.get("LANCEE").donnees().get("valeurStatut"), recus.toString());
        PortSalesforce.Statut retour = recus.get("RETOURNEE");
        assertNotNull(retour, "le retour à l'étape v1 est un statut « retournée » pour Salesforce : " + recus.keySet());
        assertEquals("Retournée", retour.donnees().get("valeurStatut"));
        assertEquals("v1", retour.donnees().get("etape"));
        assertEquals(DOSSIER, retour.sfDossierId());
    }

    @Test
    void un_signataire_externe_peut_communiquer_en_anglais_et_toute_autre_langue_est_refusee() throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Signataire anglophone\",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        JsonNode p = corps(appel("POST", "/api/clm/trousses/" + id + "/participants", initiateur, "{\"nom\":\"Jane Smith\","
                + "\"courriel\":\"jane.smith@example.com\",\"roleContrat\":\"SIGNATAIRE_1\",\"telephone\":\"+15145550177\",\"externe\":true,\"langue\":\"en\"}"), 200);
        assertEquals("EN", p.get("langue").asText());
        assertEquals(400, appel("POST", "/api/clm/trousses/" + id + "/participants", initiateur, "{\"nom\":\"Juan\","
                + "\"courriel\":\"juan@example.com\",\"roleContrat\":\"SIGNATAIRE_2\",\"telephone\":\"+15145550178\",\"externe\":true,\"langue\":\"es\"}").getStatus());
        JsonNode defaut = corps(appel("POST", "/api/clm/trousses/" + id + "/participants", initiateur, "{\"nom\":\"Luc\","
                + "\"courriel\":\"luc@example.com\",\"roleContrat\":\"SIGNATAIRE_2\",\"telephone\":\"+15145550179\",\"externe\":true}"), 200);
        assertEquals("FR", defaut.get("langue").asText(), "le français est la langue par défaut");
    }

    @Test
    void la_lecture_salesforce_se_limite_aux_champs_declares_par_l_organisation() throws Exception {
        JsonNode dossier = corps(appel("GET", "/api/clm/salesforce/dossiers/DEM-2026-0002", initiateur, null), 200);
        JsonNode champs = dossier.get("champs");
        assertTrue(champs.has("Montant") && champs.has("Name") && champs.has("Id"), champs.toString());
        assertFalse(champs.has("NotesInternes__c"), "champ de la demande que l'organisation n'a pas déclaré : jamais lu");
    }

    @Test
    void la_connexion_jwt_bearer_exige_un_utilisateur_d_execution_et_une_cle_privee() throws Exception {
        MockHttpServletResponse sansUtilisateur = appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", admin,
                "{\"mode\":\"REST\",\"authentification\":\"JWT_BEARER\",\"instanceUrl\":\"https://iq.my.salesforce.com\","
                        + "\"tokenUrl\":\"https://login.salesforce.com/services/oauth2/token\",\"clientId\":\"3MVG9-client\","
                        + "\"objet\":\"FundingRequest\",\"versionApi\":\"v60.0\",\"champStatut\":\"CLM_Statut__c\","
                        + "\"champReference\":\"CLM_Reference__c\",\"champCorrelation\":\"CLM_Correlation__c\",\"reprisesMax\":8}");
        assertEquals(400, sansUtilisateur.getStatus());
        String erreur = sansUtilisateur.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(erreur.contains("Utilisateur d'exécution requis") && erreur.contains("Clé privée requise"), erreur);
        JsonNode config = corps(appel("GET", "/api/clm/admin/integrations/salesforce", admin, null), 200).get("config");
        assertEquals("CLIENT_CREDENTIALS", config.get("authentification").asText(), "méthode par défaut");
    }

    @Test
    void un_lot_de_trousses_closes_s_exporte_vers_le_referentiel_designe() throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"À exporter\"}"), 200).get("resume").get("id").asLong();
        corps(appel("POST", "/api/clm/trousses/" + id + "/annuler", initiateur, "{\"motif\":\"Abandon\"}"), 200);
        String reference = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).get("resume").get("reference").asText();
        LocalDate jour = LocalDate.now();
        String url = "/api/clm/admin/conservation/exportation?du=" + jour.minusDays(1) + "&au=" + jour.plusDays(1);

        MockHttpServletResponse r = appel("GET", url, admin, null);
        assertEquals(200, r.getStatus());
        assertEquals("application/zip", r.getContentType());
        Map<String, byte[]> zip = lire(r.getContentAsByteArray());
        JsonNode index = json.readTree(zip.get("index.json"));
        JsonNode entree = null;
        for (JsonNode e : index.get("trousses")) {
            if (reference.equals(e.get("reference").asText())) {
                entree = e;
            }
        }
        assertNotNull(entree, index.toString());
        assertEquals("ANNULEE", entree.get("statut").asText());
        assertTrue(zip.containsKey(entree.get("fichier").asText()), "dossier de preuve autoportant de la trousse");
        assertTrue(new String(zip.get("index.csv"), StandardCharsets.UTF_8).contains(reference));

        assertEquals(400, appel("GET", "/api/clm/admin/conservation/exportation?du=" + jour + "&au=" + jour.minusDays(2), admin, null).getStatus());
        assertEquals(403, appel("GET", url, initiateur, null).getStatus());
        String journal = appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(journal.contains("PREUVE_EXPORTEE"), "chaque trousse exportée l'est à son journal scellé : " + journal);
    }

    private static Map<String, byte[]> lire(byte[] octets) throws Exception {
        Map<String, byte[]> fichiers = new LinkedHashMap<>();
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(octets))) {
            for (ZipEntry e = z.getNextEntry(); e != null; e = z.getNextEntry()) {
                fichiers.put(e.getName(), z.readAllBytes());
            }
        }
        return fichiers;
    }

    private void decider(long etape, Cookie qui, String corps) throws Exception {
        MockHttpServletResponse r = appel("POST", "/api/clm/etapes/" + etape + "/decider", qui, corps);
        assertEquals(200, r.getStatus(), r.getContentAsString(StandardCharsets.UTF_8));
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
