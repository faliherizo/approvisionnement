package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Addenda 4 (19 novembre 2026), organisation IQ, vraie pile : administration d'affaires et administration technique
 * séparées (Q10), signataire représentant une entreprise (Q6), approbation globale d'un lot (Q36), attributs SCIM (Q31),
 * acheminement selon les attributs de la trousse (Q8, Q20).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class Addenda4IntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String PDF64 = Base64.getEncoder().encodeToString(("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
            + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\n"
            + "trailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired qc.levis.contrat.auth.application.repository.UserRepository users;

    private Cookie initiateur;
    private Cookie affaires;
    private Cookie technique;
    private Cookie principal;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        affaires = login("admin.affaires@iq.example");
        technique = login("admin.technique@iq.example");
        principal = login("admin.ti@iq.example");
    }

    @Test
    void l_administration_d_affaires_et_l_administration_technique_sont_separees() throws Exception {
        String[] affairesSeulement = {"/api/clm/admin/conservation/regles", "/api/clm/admin/messages", "/api/clm/admin/parametres",
                "/api/clm/admin/journal", "/api/admin/reference?categorie=CLM_UNITE"};
        String[] techniqueSeulement = {"/api/clm/admin/integrations/salesforce", "/api/admin/annuaire/correspondances",
                "/api/admin/annuaire/scim", "/api/clm/admin/clients-integration"};
        for (String url : affairesSeulement) {
            assertEquals(200, appel("GET", url, affaires, null).getStatus(), url);
            assertEquals(403, appel("GET", url, technique, null).getStatus(), "technique : " + url);
            assertEquals(200, appel("GET", url, principal, null).getStatus(), "ADMIN cumule : " + url);
        }
        for (String url : techniqueSeulement) {
            assertEquals(200, appel("GET", url, technique, null).getStatus(), url);
            assertEquals(403, appel("GET", url, affaires, null).getStatus(), "affaires : " + url);
            assertEquals(200, appel("GET", url, principal, null).getStatus(), "ADMIN cumule : " + url);
        }
        long trousse = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Contenu protégé\"}"), 200).get("resume").get("id").asLong();
        assertEquals(200, appel("GET", "/api/clm/trousses/" + trousse, affaires, null).getStatus(), "l'administration d'affaires gère les trousses");
        assertTrue(appel("GET", "/api/clm/trousses/" + trousse, technique, null).getStatus() >= 403, "l'administration technique ne lit aucun contenu");
        assertTrue(corps(appel("GET", "/api/admin/users/roles", principal, null), 200).toString().contains("ADMIN_TECHNIQUE"));
        assertEquals(403, appel("GET", "/api/admin/users/roles", technique, null).getStatus(), "seul ADMIN attribue les rôles");
    }

    @Test
    void la_connexion_locale_se_reserve_aux_comptes_de_secours_munis_d_un_second_facteur() throws Exception {
        String url = "/api/admin/connexion-locale";
        assertEquals(403, appel("GET", url, affaires, null).getStatus(), "administration technique seulement");
        JsonNode etat = corps(appel("GET", url, technique, null), 200);
        assertFalse(etat.get("reservee").asBoolean());
        assertEquals(409, appel("PUT", url + "?reservee=true", technique, null).getStatus(), "aucun compte de secours : pas de verrouillage");
        qc.levis.contrat.auth.domain.User secours = users.findByEmail("admin.ti@iq.example").orElseThrow();
        try {
            secours.setMfaActive(true);
            secours.setMfaMethode(qc.levis.contrat.commun.domain.Enums.MfaMethode.TOTP);
            users.save(secours);
            JsonNode designe = corps(appel("PUT", url + "/comptes/" + secours.getId() + "?secours=true", technique, null), 200);
            assertTrue(designe.toString().contains("\"secours\":true"));
            assertTrue(corps(appel("PUT", url + "?reservee=true", technique, null), 200).get("reservee").asBoolean());

            MockHttpServletResponse refuse = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"initiateur@iq.example\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse();
            assertTrue(refuse.getContentAsString(StandardCharsets.UTF_8).contains("réservée aux comptes de secours"), "connexion locale refusée");
            assertNull(refuse.getCookie("ACCESS_TOKEN"));
            String defi = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin.ti@iq.example\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse()
                    .getContentAsString(StandardCharsets.UTF_8);
            assertTrue(defi.contains("\"mfaRequired\":true"), "le compte de secours passe au second facteur : " + defi);
            assertEquals(409, appel("PUT", url + "/comptes/" + secours.getId() + "?secours=false", technique, null).getStatus(),
                    "dernier compte de secours valide");
            assertEquals(404, appel("PUT", url + "/comptes/999999?secours=true", technique, null).getStatus());
        } finally {
            appel("PUT", url + "?reservee=false", technique, null);
            appel("PUT", url + "/comptes/" + secours.getId() + "?secours=false", technique, null);
            qc.levis.contrat.auth.domain.User remis = users.findByEmail("admin.ti@iq.example").orElseThrow();
            remis.setMfaActive(false);
            remis.setMfaMethode(qc.levis.contrat.commun.domain.Enums.MfaMethode.NONE);
            users.save(remis);
        }
        assertFalse(corps(appel("GET", url, technique, null), 200).get("reservee").asBoolean());
    }

    @Test
    void un_representant_signe_pour_une_entreprise_avec_sa_qualite_et_l_attestation_exigee() throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Convention Agri-Nord\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(400, appel("POST", "/api/clm/trousses/" + id + "/participants", initiateur, "{\"nom\":\"Marc Lavoie\","
                + "\"courriel\":\"marc@agrinord.example\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":true,\"autoriteRequise\":true}").getStatus(),
                "l'attestation d'autorité suppose la qualité et l'entreprise");
        JsonNode p = corps(appel("POST", "/api/clm/trousses/" + id + "/participants", initiateur, "{\"nom\":\"Marc Lavoie\","
                + "\"courriel\":\"marc@agrinord.example\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":true,\"qualite\":\"Président\","
                + "\"entreprise\":\"Coopérative Agri-Nord inc.\",\"autoriteRequise\":true}"), 200);
        assertEquals("Président", p.get("qualite").asText());
        assertEquals("Coopérative Agri-Nord inc.", p.get("entreprise").asText());
        assertTrue(p.get("autoriteRequise").asBoolean());
    }

    @Test
    void l_approbateur_approuve_un_lot_en_une_action_et_chaque_dossier_garde_sa_trace() throws Exception {
        String parcours = "[{\"cle\":\"a1\",\"libelle\":\"Approbation\",\"phase\":1,\"type\":\"APPROBATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}]}]";
        long modele = corps(appel("POST", "/api/clm/modeles", initiateur, "{\"nom\":\"Modèle lot approuvé\",\"corps\":\"Texte\","
                + "\"champs\":[],\"clauses\":[],\"parcours\":" + parcours + "}"), 200).get("id").asLong();
        corps(appel("POST", "/api/clm/modeles/" + modele + "/soumettre", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + modele + "/publier", affaires, "{}"), 200);
        String element = "{\"titre\":\"%s\",\"risque\":\"FAIBLE\",\"documents\":[{\"nom\":\"c.pdf\",\"contenuBase64\":\"" + PDF64 + "\"}]}";
        JsonNode lot = corps(appel("POST", "/api/clm/lots", initiateur, "{\"libelle\":\"Lot approbation\",\"modeleId\":" + modele
                + ",\"lancer\":true,\"elements\":[" + element.formatted("Dossier A") + "," + element.formatted("Dossier B") + "]}"), 201);
        long lotId = lot.get("id").asLong();

        JsonNode refus = corps(appel("POST", "/api/clm/lots/" + lotId + "/approuver", login("validateur@iq.example"), "{\"texte\":\"ok\"}"), 200);
        assertEquals(0, refus.get("approuvees").asInt(), "étapes non affectées au validateur");
        assertEquals(2, refus.get("ecartees").asInt());
        JsonNode r = corps(appel("POST", "/api/clm/lots/" + lotId + "/approuver", login("approbateur@iq.example"), "{\"texte\":\"Lot conforme\"}"), 200);
        assertEquals(2, r.get("dossiers").asInt());
        assertEquals(2, r.get("approuvees").asInt(), r.toString());
        long trousse = lot.get("elements").get(0).get("trousseId").asLong();
        String journal = appel("GET", "/api/clm/trousses/" + trousse + "/historique", initiateur, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(journal.contains("Approbation globale du lot") && journal.contains("Lot conforme"), "trace propre à chaque dossier");
        assertEquals(404, appel("POST", "/api/clm/lots/999999/approuver", initiateur, null).getStatus());
    }

    @Test
    void les_attributs_scim_alimentent_les_domaines_d_affaires() throws Exception {
        corps(appel("POST", "/api/admin/annuaire/correspondances", technique,
                "{\"source\":\"ATTRIBUT\",\"attribut\":\"department\",\"valeur\":\"Affaires juridiques\",\"groupe\":\"JURIDIQUE_SCIM_ATTR\"}"), 201);
        String jeton = corps(appel("POST", "/api/admin/annuaire/scim/jeton", technique, "{}"), 200).get("jeton").asText();
        JsonNode u = corps(scim("POST", "/scim/v2/Users", jeton, "{\"userName\":\"juriste.attr@iq.example\",\"title\":\"Conseillère\","
                + "\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\":{\"department\":\"Affaires juridiques\"}}"), 201);
        assertTrue(groupes().contains("JURIDIQUE_SCIM_ATTR:juriste.attr@iq.example"), "domaine tiré du département");
        corps(scim("PATCH", "/scim/v2/Users/" + u.get("id").asText(), jeton, "{\"Operations\":[{\"op\":\"replace\","
                + "\"path\":\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department\",\"value\":\"Finances\"}]}"), 200);
        assertFalse(groupes().contains("JURIDIQUE_SCIM_ATTR:juriste.attr@iq.example"), "changement de département : domaine retiré");
        corps(scim("PATCH", "/scim/v2/Users/" + u.get("id").asText(), jeton, "{\"Operations\":[{\"op\":\"replace\","
                + "\"value\":{\"urn:ietf:params:scim:schemas:extension:enterprise:2.0:User\":{\"department\":\"Affaires juridiques\"}}}]}"), 200);
        assertTrue(groupes().contains("JURIDIQUE_SCIM_ATTR:juriste.attr@iq.example"));
        corps(scim("PATCH", "/scim/v2/Users/" + u.get("id").asText(), jeton, "{\"Operations\":[{\"op\":\"remove\",\"path\":\"department\"}]}"), 200);
        assertFalse(groupes().contains("JURIDIQUE_SCIM_ATTR:juriste.attr@iq.example"), "attribut retiré");
    }

    @Test
    void une_etape_s_achemine_selon_le_risque_de_la_trousse() throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Acheminement\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new org.springframework.mock.web.MockMultipartFile("fichier", "c.pdf", "application/pdf", Base64.getDecoder().decode(PDF64)))
                .cookie(initiateur)).andReturn().getResponse().getStatus());
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]},"
                + "{\"cle\":\"vp\",\"libelle\":\"Vice-présidence\",\"phase\":2,\"type\":\"APPROBATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}],"
                + "\"conditions\":[{\"champ\":\"trousse.risque\",\"operateur\":\"EGAL\",\"valeur\":\"ELEVE\"}]}]"), 200);
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        JsonNode etapes = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).get("etapes");
        assertEquals("NON_REQUISE", etapes.get(1).get("statut").asText(), "risque faible : la vice-présidence n'est pas requise");
    }

    private String groupes() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (JsonNode g : corps(appel("GET", "/api/clm/groupes", principal, null), 200)) {
            g.get("membres").forEach(m -> sb.append(g.get("code").asText()).append(':').append(m.asText()).append(' '));
        }
        return sb.toString();
    }

    @Test
    void au_depart_d_un_employe_ses_trousses_etapes_et_taches_passent_a_une_personne_active() throws Exception {
        long possedee = corps(appel("POST", "/api/clm/trousses", login("validateur@iq.example"),
                "{\"titre\":\"Brouillon du partant\",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Dossier où le partant valide\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new org.springframework.mock.web.MockMultipartFile("fichier", "c.pdf", "application/pdf", Base64.getDecoder().decode(PDF64)))
                .cookie(initiateur)).andReturn().getResponse().getStatus());
        corps(appel("PUT", "/api/clm/trousses/" + id + "/parcours", initiateur, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,"
                + "\"type\":\"VALIDATION\",\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"},"
                + "{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}],\"quorum\":2}]"), 200);
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/taches-assignees", affaires, "{\"trousseId\":" + id + ",\"titre\":\"Relire l'annexe\","
                + "\"assigneA\":\"validateur@iq.example\",\"priorite\":\"NORMALE\"}"), 200);

        JsonNode inventaire = corps(appel("GET", "/api/clm/admin/departs?courriel=Validateur@iq.example", affaires, null), 200);
        assertTrue(inventaire.get("trousses").toString().contains(corps(appel("GET", "/api/clm/trousses/" + possedee, affaires, null), 200)
                .get("resume").get("reference").asText()), inventaire.toString());
        assertTrue(inventaire.get("etapes").asInt() >= 1 && inventaire.get("taches").asInt() >= 1, inventaire.toString());
        assertEquals(403, appel("GET", "/api/clm/admin/departs?courriel=validateur@iq.example", technique, null).getStatus(), "technique : aucun contenu");
        assertEquals(403, appel("POST", "/api/clm/admin/departs", initiateur, "{\"de\":\"validateur@iq.example\",\"vers\":\"initiateur@iq.example\"}").getStatus());
        assertEquals(400, appel("POST", "/api/clm/admin/departs", affaires, "{\"de\":\"validateur@iq.example\",\"vers\":\"VALIDATEUR@iq.example\"}").getStatus());
        assertEquals(400, appel("POST", "/api/clm/admin/departs", affaires, "{\"de\":\"validateur@iq.example\",\"vers\":\"inconnu@iq.example\"}").getStatus());
        assertEquals(400, appel("POST", "/api/clm/admin/departs", affaires, "{\"de\":\" \",\"vers\":\"approbateur@iq.example\"}").getStatus());

        JsonNode bilan = corps(appel("POST", "/api/clm/admin/departs", affaires,
                "{\"de\":\"validateur@iq.example\",\"vers\":\"auditeur@iq.example\",\"motif\":\"Départ à la retraite\"}"), 200);
        assertTrue(bilan.get("trousses").asInt() >= 1 && bilan.get("etapes").asInt() >= 1 && bilan.get("taches").asInt() >= 1, bilan.toString());
        assertEquals("auditeur@iq.example", corps(appel("GET", "/api/clm/trousses/" + possedee, affaires, null), 200)
                .get("resume").get("proprietaire").asText(), "propriété transférée");
        String historique = appel("GET", "/api/clm/trousses/" + id + "/historique", affaires, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(historique.contains("auditeur@iq.example") && historique.contains("Départ à la retraite"), historique);
        String detail = appel("GET", "/api/clm/trousses/" + id, affaires, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(detail.contains("auditeur@iq.example") && !detail.contains("validateur@iq.example"), "étape active réassignée : " + detail);
        JsonNode taches = corps(appel("GET", "/api/clm/trousses/" + id + "/taches-assignees", affaires, null), 200);
        assertEquals("auditeur@iq.example", taches.get(0).get("assigneA").asText(), "tâche ouverte réassignée");
        JsonNode apres = corps(appel("GET", "/api/clm/admin/departs?courriel=validateur@iq.example", affaires, null), 200);
        assertEquals(0, apres.get("trousses").size());
        assertEquals(0, apres.get("etapes").asInt());
        assertEquals(0, apres.get("taches").asInt());
    }

    /** Gabarit Word d'IQ : titre en gras, variable répartie sur deux runs, tableau, marqueur de clauses. */
    private static byte[] gabaritWord(String variable) throws Exception {
        try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            var p = doc.createParagraph();
            var r = p.createRun();
            r.setBold(true);
            r.setText("Convention de financement — {{bene");
            p.createRun().setText("ficiaire}}");
            doc.createTable(1, 2).getRow(0).getCell(1).setText("{{" + variable + "}}");
            doc.createParagraph().createRun().setText("{{clauses}}");
            doc.write(out);
            return out.toByteArray();
        }
    }

    private MockHttpServletResponse deposer(long modele, Cookie qui, String nom, byte[] contenu) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/modeles/" + modele + "/gabarit-word")
                .file(new org.springframework.mock.web.MockMultipartFile("fichier", nom, "application/octet-stream", contenu)).cookie(qui))
                .andReturn().getResponse();
    }

    @Test
    void le_contrat_se_genere_dans_le_gabarit_word_d_iq_en_conservant_sa_mise_en_page() throws Exception {
        String champs = "[{\"cle\":\"beneficiaire\",\"libelle\":\"Bénéficiaire\",\"type\":\"TEXTE\",\"obligatoire\":false},"
                + "{\"cle\":\"montant\",\"libelle\":\"Montant\",\"type\":\"TEXTE\",\"obligatoire\":false}]";
        String clauses = "[{\"cle\":\"garantie\",\"titre\":\"Garantie\",\"texte\":\"Hypothèque de {{montant}}\",\"combinateur\":\"ET\",\"conditions\":[]}]";
        long modele = corps(appel("POST", "/api/clm/modeles", initiateur, "{\"nom\":\"Convention Word IQ\",\"corps\":\"{{beneficiaire}}\","
                + "\"champs\":" + champs + ",\"clauses\":" + clauses + ",\"parcours\":[]}"), 200).get("id").asLong();

        assertTrue(corps(deposer(modele, initiateur, "iq.docx", gabaritWord("inconnue")), 400).toString().contains("inconnue"),
                "variable absente du modèle refusée");
        assertEquals(400, deposer(modele, initiateur, "iq.pdf", PDF64.getBytes()).getStatus(), "Word seulement");
        assertEquals(403, deposer(modele, login("validateur@iq.example"), "iq.docx", gabaritWord("montant")).getStatus());
        JsonNode vue = corps(deposer(modele, initiateur, "Convention IQ.docx", gabaritWord("montant")), 200);
        assertEquals("Convention IQ.docx", vue.get("gabaritWord").asText());
        MockHttpServletResponse fichier = appel("GET", "/api/clm/modeles/" + modele + "/gabarit-word", initiateur, null);
        assertEquals(200, fichier.getStatus());
        assertTrue(fichier.getHeader("Content-Disposition").contains("IQ.docx"), fichier.getHeader("Content-Disposition"));
        assertTrue(corps(appel("GET", "/api/clm/modeles/" + modele + "/validation", initiateur, null), 200).get("valide").asBoolean());

        corps(appel("POST", "/api/clm/modeles/" + modele + "/soumettre", initiateur, "{}"), 200);
        assertEquals(409, appel("DELETE", "/api/clm/modeles/" + modele + "/gabarit-word", initiateur, null).getStatus(), "modèle hors brouillon");
        corps(appel("POST", "/api/clm/modeles/" + modele + "/publier", affaires, "{}"), 200);
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Convention Agri-Nord\",\"modeleId\":" + modele
                + ",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        corps(appel("PUT", "/api/clm/trousses/" + id + "/valeurs", initiateur,
                "{\"valeurs\":{\"beneficiaire\":\"Agri-Nord inc.\",\"montant\":\"250 000 $\"}}"), 200);

        JsonNode word = corps(appel("POST", "/api/clm/trousses/" + id + "/generer?format=DOCX", initiateur, null), 200);
        byte[] docx = appel("GET", "/api/clm/trousses/" + id + "/documents/" + word.get("id").asLong() + "/contenu", initiateur, null).getContentAsByteArray();
        try (var doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(docx))) {
            assertEquals("Convention de financement — Agri-Nord inc.", doc.getParagraphs().get(0).getText());
            assertTrue(doc.getParagraphs().get(0).getRuns().get(0).isBold(), "mise en forme d'origine");
            assertEquals("250 000 $", doc.getTables().get(0).getRow(0).getCell(1).getText(), "tableau d'origine conservé");
            assertTrue(doc.getParagraphs().stream().anyMatch(x -> x.getText().equals("Hypothèque de 250 000 $")), "clause insérée au marqueur");
        }
        JsonNode pdf = corps(appel("POST", "/api/clm/trousses/" + id + "/generer?format=PDF", initiateur, null), 200);
        assertEquals("PDF", pdf.get("type").asText());
        assertTrue(word.get("motif").asText().contains("gabarit Word « Convention IQ.docx »"), word.toString());
        assertTrue(pdf.get("motif").asText().contains("mise en page simplifiée"), "sans LibreOffice, le motif le dit : " + pdf);
        assertTrue(appel("GET", "/api/clm/trousses/" + id + "/documents/" + pdf.get("id").asLong() + "/texte", initiateur, null)
                .getContentAsString(StandardCharsets.UTF_8).contains("Agri-Nord inc."), "PDF issu du gabarit fusionné");

        long version2 = corps(appel("POST", "/api/clm/modeles/" + modele + "/nouvelle-version", initiateur, "{}"), 200).get("id").asLong();
        assertEquals("Convention IQ.docx", corps(appel("GET", "/api/clm/modeles/" + version2, initiateur, null), 200).get("gabaritWord").asText(),
                "le gabarit suit la nouvelle version");
        assertTrue(corps(appel("DELETE", "/api/clm/modeles/" + version2 + "/gabarit-word", initiateur, null), 200).get("gabaritWord").isNull());
        assertEquals(404, appel("GET", "/api/clm/modeles/" + version2 + "/gabarit-word", initiateur, null).getStatus());
    }

    private MockHttpServletResponse scim(String methode, String url, String jeton, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url).header("Authorization", "Bearer " + jeton);
        if (corps != null) {
            r.contentType("application/scim+json").content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
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
