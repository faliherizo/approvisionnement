package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
 * Annexe 10 IQ, AF-49 (RST-51), vraie pile : les pages des PDF sont comptées au dépôt et sur la version signée ; le relevé
 * d'utilisation et son export donnent les documents et les pages signés par période et par unité. Les PDF, de nombre de
 * pages connu, sont produits par PDFBox ; les comptes se lisent en écart avant/après, la base étant partagée.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.clm.planificateur.actif=false")
class ComptageSignesIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String UNITE = "FINANCEMENT_ENTREPRISES";
    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie initiateur;
    private Cookie validateur;
    private Cookie affaires;
    private String periode;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        validateur = login("validateur@iq.example");
        affaires = login("admin.affaires@iq.example");
        LocalDate aujourdhui = LocalDate.now();
        periode = "du=" + aujourdhui.minusDays(1) + "&au=" + aujourdhui.plusDays(1);
    }

    @Test
    void les_pages_d_un_pdf_sont_comptees_au_depot() throws Exception {
        long id = trousse("Pagination", UNITE);
        JsonNode d = corps(deposer(id, "cinq.pdf", "application/pdf", pdf(5)), 200);
        JsonNode lu = corps(appel("GET", "/api/clm/trousses/" + id, initiateur), 200).get("documents").get(0);
        assertEquals(d.get("id").asLong(), lu.get("id").asLong());
        assertEquals(5, lu.get("pages").asInt());
    }

    @Test
    void le_releve_compte_les_documents_et_les_pages_signes_par_unite_et_l_export_les_porte() throws Exception {
        JsonNode avant = signes();

        long financement = trousse("Pages A", UNITE);
        deposer(financement, "trois.pdf", "application/pdf", pdf(3));
        deposer(financement, "cinq.pdf", "application/pdf", pdf(5));
        deposer(financement, "annexe.docx", DOCX, docx());
        terminer(financement);
        long sansUnite = trousse("Pages B", null);
        deposer(sansUnite, "deux.pdf", "application/pdf", pdf(2));
        terminer(sansUnite);

        JsonNode apres = signes();
        assertEquals(4, ecart(avant, apres, "/total/documents"), "trois PDF et un document Word signés");
        assertEquals(10, ecart(avant, apres, "/total/pages"), "3 + 5 + 2 pages : la validation n'ajoute pas de page");
        assertEquals(1, ecart(avant, apres, "/total/sansPagination"), "le document Word n'est pas paginé");
        assertEquals(3, ecartUnite(avant, apres, UNITE, "documents"));
        assertEquals(8, ecartUnite(avant, apres, UNITE, "pages"));
        assertEquals(2, ecartUnite(avant, apres, "", "pages"), "trousse sans unité");

        JsonNode versions = corps(appel("GET", "/api/clm/trousses/" + financement, initiateur), 200).get("documents");
        versions.forEach(v -> {
            if ("SIGNE".equals(v.get("statut").asText()) && v.get("nom").asText().endsWith(".pdf")) {
                assertTrue(v.get("pages").asInt() == 3 || v.get("pages").asInt() == 5, v.toString());
            }
        });

        String csv = appel("GET", "/api/clm/admin/utilisation/export?" + periode + "&format=csv", affaires).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("Documents signés") && csv.contains("Pages signées") && csv.contains("unité " + UNITE), csv);
        assertEquals(403, appel("GET", "/api/clm/admin/utilisation?" + periode, initiateur).getStatus(), "administration et audit seulement");
    }

    private JsonNode signes() throws Exception {
        return corps(appel("GET", "/api/clm/admin/utilisation?" + periode, affaires), 200).get("signes");
    }

    private static long ecart(JsonNode avant, JsonNode apres, String chemin) {
        return apres.at(chemin).asLong() - avant.at(chemin).asLong();
    }

    private static long ecartUnite(JsonNode avant, JsonNode apres, String unite, String champ) {
        return valeur(apres, unite, champ) - valeur(avant, unite, champ);
    }

    private static long valeur(JsonNode signes, String unite, String champ) {
        for (JsonNode u : signes.get("parUnite")) {
            if (unite.equals(u.get("cle").asText())) {
                return u.get(champ).asLong();
            }
        }
        return 0;
    }

    private long trousse(String titre, String unite) throws Exception {
        String u = unite == null ? "" : ",\"unite\":\"" + unite + "\"";
        return corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"" + titre + "\",\"risque\":\"FAIBLE\"" + u + "}"), 200)
                .at("/resume/id").asLong();
    }

    private MockHttpServletResponse deposer(long trousse, String nom, String type, byte[] contenu) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + trousse + "/documents")
                .file(new MockMultipartFile("fichier", nom, type, contenu)).cookie(initiateur)).andReturn().getResponse();
    }

    private void terminer(long id) throws Exception {
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]}]"), 200);
        JsonNode lance = corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/etapes/" + lance.at("/etapes/0/id").asLong() + "/decider", validateur, "{\"decision\":\"APPROUVE\"}"), 200);
        assertEquals("TERMINEE", corps(appel("GET", "/api/clm/trousses/" + id, initiateur), 200).at("/resume/statut").asText());
    }

    private static byte[] pdf(int pages) throws IOException {
        try (PDDocument d = new PDDocument(); ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) {
                d.addPage(new PDPage());
            }
            d.save(o);
            return o.toByteArray();
        }
    }

    private static byte[] docx() throws IOException {
        try (XWPFDocument d = new XWPFDocument(); ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            d.createParagraph().createRun().setText("Annexe");
            d.write(o);
            return o.toByteArray();
        }
    }

    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private MockHttpServletResponse appel(String methode, String url, Cookie qui) throws Exception {
        return appel(methode, url, qui, null);
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
