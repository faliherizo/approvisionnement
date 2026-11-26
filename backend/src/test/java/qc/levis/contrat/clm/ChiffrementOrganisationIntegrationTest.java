package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.CoffreDocuments;
import qc.levis.contrat.clm.domain.ChaineScellee;

/**
 * RST-54 (SEC-28) : clé de chiffrement au repos dédiée à l'organisation, vraie pile. Le contenu stocké est une enveloppe
 * AES-GCM de la clé d'IQ ; l'API rend le clair et l'empreinte de preuve reste celle du clair ; une autre organisation ne
 * l'ouvre pas ; la rotation et le réenveloppement se font sans interruption, anciens contenus en clair compris.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-chiffrement"})
class ChiffrementOrganisationIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String URL = "/api/clm/admin/chiffrement";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired CoffreDocuments coffre;

    private Cookie initiateur;
    private Cookie technique;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        technique = login("admin.technique@iq.example");
    }

    @Test
    void le_contenu_est_chiffre_par_la_cle_d_iq_et_illisible_pour_une_autre_organisation() throws Exception {
        byte[] clair = pdf("CONTENU-CONFIDENTIEL-IQ-1");
        long trousse = trousse("Chiffrement IQ");
        JsonNode doc = deposer(trousse, clair);
        byte[] stocke = stockage(doc.get("id").asLong());
        assertEquals(0, stocke[0], "enveloppe : la marque commence par un octet nul");
        assertFalse(new String(stocke, StandardCharsets.ISO_8859_1).contains("CONTENU-CONFIDENTIEL-IQ-1"), "aucun clair en base");
        assertNotNull(jdbc.queryForObject("select cle_version from clm_document where id = ?", Integer.class, doc.get("id").asLong()));

        MockHttpServletResponse lu = appel("GET", "/api/clm/trousses/" + trousse + "/documents/" + doc.get("id").asLong() + "/contenu", initiateur, null);
        assertArrayEquals(clair, lu.getContentAsByteArray(), "l'API rend le clair");
        assertEquals(ChaineScellee.sha256(clair), doc.get("empreinte").asText(), "l'empreinte de preuve est celle du clair");

        long iq = jdbc.queryForObject("select tenant_id from clm_document where id = ?", Long.class, doc.get("id").asLong());
        long autre = jdbc.queryForObject("select min(id) from tenant where id <> ?", Long.class, iq);
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(autre, stocke), "refusé sous une autre organisation");
        assertThrows(CoffreDocuments.DocumentIllisible.class, () -> coffre.dechiffrer(autre, avecOrganisation(stocke, autre)),
                "la clé d'une autre organisation n'ouvre pas le document");
        assertArrayEquals(clair, coffre.dechiffrer(iq, stocke));
    }

    @Test
    void la_rotation_garde_les_anciens_documents_lisibles_puis_le_reenveloppement_migre_tout() throws Exception {
        assertEquals(403, appel("GET", URL, initiateur, null).getStatus(), "administration technique seulement");
        long trousse = trousse("Rotation IQ");
        byte[] avant = pdf("AVANT-ROTATION");
        long ancien = deposer(trousse, avant).get("id").asLong();
        byte[] enClair = pdf("ANCIEN-EN-CLAIR");
        long migre = deposer(trousse, enClair).get("id").asLong();
        jdbc.update("update clm_document set contenu = ?, cle_version = null where id = ?", enClair, migre);

        JsonNode etat = corps(appel("POST", URL + "/pivoter", technique, null), 200);
        int active = etat.at("/versions/0/version").asInt();
        assertEquals("ACTIVE", etat.at("/versions/0/statut").asText());
        assertEquals("RETIREE", etat.at("/versions/1/statut").asText());
        assertTrue(etat.get("nonChiffres").asLong() >= 1, "l'ancien contenu en clair est compté");
        assertEquals("developpement", etat.get("cleMaitresseActive").asText());

        assertArrayEquals(avant, contenu(trousse, ancien), "la version retirée déchiffre encore");
        assertArrayEquals(enClair, contenu(trousse, migre), "l'ancien clair se lit tel quel");
        long nouveau = deposer(trousse, pdf("APRES-ROTATION")).get("id").asLong();
        assertEquals(active, jdbc.queryForObject("select cle_version from clm_document where id = ?", Integer.class, nouveau));

        JsonNode lot;
        do {
            lot = corps(appel("POST", URL + "/reenvelopper", technique, null), 200);
        } while (lot.get("restants").asLong() > 0);
        assertEquals(active, jdbc.queryForObject("select cle_version from clm_document where id = ?", Integer.class, ancien));
        assertEquals(active, jdbc.queryForObject("select cle_version from clm_document where id = ?", Integer.class, migre));
        assertEquals(0, stockage(migre)[0], "l'ancien clair est désormais chiffré");
        assertArrayEquals(enClair, contenu(trousse, migre));
        assertEquals(0, corps(appel("GET", URL, technique, null), 200).get("nonChiffres").asLong());
    }

    private static byte[] avecOrganisation(byte[] enveloppe, long tenant) {
        byte[] copie = Arrays.copyOf(enveloppe, enveloppe.length);
        ByteBuffer.wrap(copie).putLong(5, tenant);
        return copie;
    }

    private static byte[] pdf(String marque) {
        return ("%PDF-1.4\n% " + marque + "\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
                + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);
    }

    private byte[] stockage(long id) {
        return jdbc.queryForObject("select contenu from clm_document where id = ?", byte[].class, id);
    }

    private byte[] contenu(long trousse, long doc) throws Exception {
        MockHttpServletResponse r = appel("GET", "/api/clm/trousses/" + trousse + "/documents/" + doc + "/contenu", initiateur, null);
        assertEquals(200, r.getStatus());
        return r.getContentAsByteArray();
    }

    private long trousse(String titre) throws Exception {
        return corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"" + titre + "\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
    }

    private JsonNode deposer(long trousse, byte[] octets) throws Exception {
        return corps(mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + trousse + "/documents")
                .file(new MockMultipartFile("fichier", "c" + System.nanoTime() + ".pdf", "application/pdf", octets)).cookie(initiateur))
                .andReturn().getResponse(), 200);
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
