package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.port.NotificateurClm;

/**
 * AF-48 (RST-50), vraie pile : trois participants de natures différentes dans une même trousse. Le participant
 * « consulter » atteste sa prise de connaissance, le « compléter » soumet ses champs, le « signer » signe ; toute action
 * hors nature est refusée ; journal et preuve distinguent chaque participation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false"})
class NatureParticipationIntegrationTest {

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

    private Cookie admin;
    private Cookie redacteur;

    @BeforeEach
    void preparer() throws Exception {
        admin = login("admin.affaires@iq.example");
        redacteur = login("initiateur@iq.example");
        ENVOIS.clear();
    }

    @Test
    void trois_natures_dans_une_trousse_et_actions_refusees_hors_nature() throws Exception {
        long id = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Natures de participation\",\"risque\":\"FAIBLE\",\"modeleId\":"
                + modelePublie() + "}"), status().isOk()).at("/resume/id").asLong();
        appel(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", "entente.pdf", "application/pdf", pdf())).cookie(redacteur), status().isOk());
        assertTrue(corps(post("/api/clm/trousses/" + id + "/participants", redacteur, participant("Hors", "hors", "SIGNATAIRE_3", "APPROUVER")),
                400).get("message").asText().contains("CONSULTER, COMPLETER ou SIGNER"));
        JsonNode marie = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, participant("Marie", "marie", "SIGNATAIRE_1", null)),
                status().isOk());
        assertEquals("SIGNER", marie.get("nature").asText(), "sans nature : signer, comme avant");
        long paul = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, participant("Paul", "paul", "SIGNATAIRE_2", "completer")),
                status().isOk()).get("id").asLong();
        long lise = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, participant("Lise", "lise", "TEMOIN", "CONSULTER")),
                status().isOk()).get("id").asLong();
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, "[{\"cle\":\"s1\",\"libelle\":\"Participation\",\"phase\":1,"
                + "\"type\":\"SIGNATURE\",\"affectations\":[" + affecte(marie.get("id").asLong()) + "," + affecte(paul) + "," + affecte(lise) + "]}]"),
                status().isOk());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());

        String jLise = verifier("lise");
        JsonNode vueLise = appel(get("/api/clm/portail/" + jLise), status().isOk());
        assertEquals("CONSULTER", vueLise.get("nature").asText());
        assertTrue(vueLise.get("champs").isEmpty(), "un participant « consulter » n'a aucun champ");
        assertEquals("Action refusée : votre participation vous permet de consulter les documents, pas de signer.",
                corps(post("/api/clm/portail/" + jLise + "/signer", null, "{\"nomComplet\":\"Lise\",\"consentement\":true}"), 409)
                        .get("message").asText());
        corps(post("/api/clm/portail/" + jLise + "/completion", null, "{\"champs\":{}}"), 409);
        corps(post("/api/clm/portail/" + jLise + "/consultation", null, "{\"attestation\":false}"), 400);
        JsonNode recuLise = corps(post("/api/clm/portail/" + jLise + "/consultation", null, "{\"attestation\":true}"), 200);
        assertEquals("CONSULTATION", recuLise.get("action").asText());
        assertTrue(recuLise.get("attestation").isNull(), "aucune attestation de signature");
        assertEquals("CONSULTEE", appel(get("/api/clm/portail/" + jLise), status().isOk()).get("statut").asText());
        corps(post("/api/clm/portail/" + jLise + "/consultation", null, "{\"attestation\":true}"), 409);

        String jPaul = verifier("paul");
        JsonNode vuePaul = appel(get("/api/clm/portail/" + jPaul), status().isOk());
        assertEquals("neq", vuePaul.at("/champs/0/cle").asText(), "seuls les champs de son rôle");
        assertEquals(1, vuePaul.get("champs").size());
        corps(post("/api/clm/portail/" + jPaul + "/signer", null, "{\"nomComplet\":\"Paul\",\"consentement\":true}"), 409);
        corps(post("/api/clm/portail/" + jPaul + "/consultation", null, "{\"attestation\":true}"), 409);
        corps(post("/api/clm/portail/" + jPaul + "/completion", null, "{}"), 400);
        assertEquals("COMPLETION", corps(post("/api/clm/portail/" + jPaul + "/completion", null,
                "{\"champs\":{\"neq\":\"1170000001\"}}"), 200).get("action").asText());

        String jMarie = verifier("marie");
        corps(post("/api/clm/portail/" + jMarie + "/completion", null, "{\"champs\":{\"titre\":\"Présidente\"}}"), 409);
        JsonNode recuMarie = corps(post("/api/clm/portail/" + jMarie + "/signer", null,
                "{\"nomComplet\":\"Marie Tremblay\",\"consentement\":true,\"champs\":{\"titre\":\"Présidente\"}}"), 200);
        assertEquals("SIGNATURE", recuMarie.get("action").asText());
        assertFalse(recuMarie.get("attestation").isNull());

        assertEquals("TERMINEE", appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/resume/statut").asText(),
                "les trois participations accomplies complètent l'étape");
        String journal = texte(get("/api/clm/trousses/" + id + "/historique", redacteur));
        assertTrue(journal.contains("CONSULTATION_ATTESTEE") && journal.contains("\"COMPLETION\"") && journal.contains("\"SIGNATURE\""), journal);
        assertTrue(journal.contains("Lise · TEMOIN · QUESTION · CONSULTER"), "nature consignée à l'ajout du participant");
        JsonNode signatures = appel(get("/api/clm/trousses/" + id + "/preuve", redacteur), status().isOk()).get("signatures");
        assertEquals("[SIGNER/SIGNATURE, COMPLETER/COMPLETION, CONSULTER/CONSULTATION]", natures(signatures));
        assertEquals("200", String.valueOf(mvc.perform(get("/api/clm/trousses/" + id + "/preuve/certificat", redacteur)).andReturn()
                .getResponse().getStatus()));
    }

    private static String natures(JsonNode signatures) {
        List<String> res = new ArrayList<>();
        signatures.forEach(s -> res.add(s.get("nature").asText() + "/" + s.get("action").asText()));
        return res.toString();
    }

    /** Jeton lu dans le courriel d'invitation, puis identité vérifiée par la question secrète. */
    private String verifier(String qui) throws Exception {
        String jeton = jeton(qui + "@participant.example");
        appel(post("/api/clm/portail/" + jeton + "/verification/reponse", null, "{\"reponse\":\"Lévis\"}"), status().isOk());
        return jeton;
    }

    private String jeton(String destinataire) {
        Pattern p = Pattern.compile("/portail/([A-Za-z0-9_-]+)");
        for (int i = ENVOIS.size() - 1; i >= 0; i--) {
            Matcher m = p.matcher(ENVOIS.get(i)[3]);
            if (ENVOIS.get(i)[1].equals(destinataire) && m.find()) {
                return m.group(1);
            }
        }
        throw new AssertionError("Aucune invitation pour " + destinataire);
    }

    private static String participant(String nom, String qui, String role, String nature) {
        return "{\"nom\":\"" + nom + "\",\"courriel\":\"" + qui + "@participant.example\",\"roleContrat\":\"" + role + "\",\"externe\":true,"
                + "\"niveauIdentite\":\"QUESTION\",\"question\":\"Ville ?\",\"reponse\":\"Lévis\""
                + (nature == null ? "" : ",\"nature\":\"" + nature + "\"") + "}";
    }

    private static String affecte(long participant) {
        return "{\"type\":\"PARTICIPANT\",\"valeur\":\"" + participant + "\"}";
    }

    private long modelePublie() throws Exception {
        long id = appel(post("/api/clm/modeles", redacteur, """
                {"nom":"Entente à natures multiples","corps":"Entente de participation.",
                 "champs":[
                   {"cle":"titre","libelle":"Titre du signataire","type":"TEXTE","obligatoire":true,"roleDestinataire":"SIGNATAIRE_1"},
                   {"cle":"neq","libelle":"Numéro d'entreprise","type":"TEXTE","obligatoire":true,"roleDestinataire":"SIGNATAIRE_2"},
                   {"cle":"signature1","libelle":"Signature","type":"SIGNATURE","roleDestinataire":"SIGNATAIRE_1",
                    "position":{"page":1,"x":10,"y":70,"largeur":40,"hauteur":10}}]}
                """), status().isOk()).get("id").asLong();
        appel(post("/api/clm/modeles/" + id + "/soumettre", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + id + "/publier", admin, "{}"), status().isOk());
        return id;
    }

    private static byte[] pdf() throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(out);
            return out.toByteArray();
        }
    }

    // --- HTTP ---
    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"Passw0rd!\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private static AbstractMockHttpServletRequestBuilder<?> post(String url, Cookie c, String corps) {
        AbstractMockHttpServletRequestBuilder<?> r = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(corps);
        return c == null ? r : r.cookie(c);
    }

    private static AbstractMockHttpServletRequestBuilder<?> put(String url, Cookie c, String corps) {
        return MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(corps).cookie(c);
    }

    private static AbstractMockHttpServletRequestBuilder<?> get(String url) {
        return MockMvcRequestBuilders.get(url);
    }

    private static AbstractMockHttpServletRequestBuilder<?> get(String url, Cookie c) {
        return MockMvcRequestBuilders.get(url).cookie(c);
    }

    private String texte(AbstractMockHttpServletRequestBuilder<?> req) throws Exception {
        return mvc.perform(req).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private JsonNode corps(AbstractMockHttpServletRequestBuilder<?> req, int statut) throws Exception {
        var r = mvc.perform(req).andReturn().getResponse();
        String s = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(statut, r.getStatus(), s);
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    private JsonNode appel(AbstractMockHttpServletRequestBuilder<?> req, ResultMatcher attendu) throws Exception {
        String s = mvc.perform(req).andExpect(attendu).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }
}
