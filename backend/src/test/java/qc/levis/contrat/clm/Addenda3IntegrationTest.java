package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.auth.application.OidcSessionService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.FaitAnnuaire;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.auth.infrastructure.OidcClient;
import qc.levis.contrat.integration.application.IntegrationOidcService;

/**
 * Addenda 3 (17 novembre 2026), organisation IQ, vraie pile : SCIM 2.0 (Q51), correspondances d'annuaire vers rôles et
 * domaines d'affaires (Q43, Q44), génération Word (Q26) et relevé d'utilisation par enveloppe (Q39).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class Addenda3IntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final byte[] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
            + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired OidcSessionService sessions;
    @Autowired qc.levis.contrat.audit.application.repository.AuditLogRepository journal;

    private Cookie initiateur;
    private Cookie admin;

    @BeforeEach
    void preparer() throws Exception {
        initiateur = login("initiateur@iq.example");
        admin = login("admin.ti@iq.example");
    }

    @Test
    void saviynt_provisionne_un_compte_et_ses_groupes_donnent_role_et_domaine_d_affaires() throws Exception {
        assertEquals(401, scim("GET", "/scim/v2/Users", "jeton-inconnu", null).getStatus());
        String jeton = corps(appel("POST", "/api/admin/annuaire/scim/jeton", admin, "{}"), 200).get("jeton").asText();
        assertTrue(jeton.startsWith("scim_"));
        assertTrue(corps(appel("GET", "/api/admin/annuaire/scim", admin, null), 200).get("configure").asBoolean());
        assertFalse(corps(appel("GET", "/api/admin/annuaire/scim", admin, null), 200).has("jeton")
                && !corps(appel("GET", "/api/admin/annuaire/scim", admin, null), 200).get("jeton").isNull(), "jamais réaffiché");
        assertTrue(corps(scim("GET", "/scim/v2/ServiceProviderConfig", jeton, null), 200).at("/patch/supported").asBoolean());
        assertEquals(2, corps(scim("GET", "/scim/v2/ResourceTypes", jeton, null), 200).get("totalResults").asInt());

        corps(appel("POST", "/api/admin/annuaire/correspondances", admin,
                "{\"source\":\"GROUPE\",\"valeur\":\"aad-juristes\",\"role\":\"AUDITEUR\",\"groupe\":\"juridique_scim\"}"), 201);
        assertEquals(400, appel("POST", "/api/admin/annuaire/correspondances", admin,
                "{\"source\":\"GROUPE\",\"valeur\":\"x\",\"role\":\"SUPERADMIN\"}").getStatus(), "jamais superadministrateur");
        assertEquals(400, appel("POST", "/api/admin/annuaire/correspondances", admin, "{\"source\":\"AUTRE\",\"valeur\":\"x\",\"role\":\"LECTEUR\"}").getStatus());
        assertTrue(corps(appel("GET", "/api/admin/annuaire/correspondances", admin, null), 200).toString().contains("JURIDIQUE_SCIM"),
                "la correspondance est listée avec son domaine normalisé");

        JsonNode u = corps(scim("POST", "/scim/v2/Users", jeton, "{\"schemas\":[\"urn:ietf:params:scim:schemas:core:2.0:User\"],"
                + "\"userName\":\"Nouvelle.Juriste@iq.example\",\"name\":{\"givenName\":\"Nadia\",\"familyName\":\"Juriste\"},\"active\":true}"), 201);
        String uid = u.get("id").asText();
        assertEquals("nouvelle.juriste@iq.example", u.get("userName").asText());
        assertEquals("Nadia Juriste", u.at("/name/formatted").asText());
        assertEquals(409, scim("POST", "/scim/v2/Users", jeton, "{\"userName\":\"nouvelle.juriste@iq.example\"}").getStatus());
        assertEquals(400, scim("POST", "/scim/v2/Users", jeton, "{\"userName\":\"pas-un-courriel\"}").getStatus());
        assertEquals(1, corps(scim("GET", "/scim/v2/Users?filter=userName eq \"nouvelle.juriste@iq.example\"", jeton, null), 200)
                .get("totalResults").asInt());
        assertEquals(400, scim("GET", "/scim/v2/Users?filter=emails co \"x\"", jeton, null).getStatus());
        assertTrue(corps(scim("GET", "/scim/v2/Users?startIndex=1&count=1", jeton, null), 200).get("itemsPerPage").asInt() <= 1);
        assertEquals("LECTEUR", roles("nouvelle.juriste@iq.example"), "moindre privilège à la création");

        JsonNode g = corps(scim("POST", "/scim/v2/Groups", jeton, "{\"displayName\":\"Juristes\",\"externalId\":\"aad-juristes\","
                + "\"members\":[{\"value\":\"" + uid + "\"}]}"), 201);
        String gid = g.get("id").asText();
        assertEquals("AUDITEUR", roles("nouvelle.juriste@iq.example"), "le groupe (identifiant externe) donne le rôle");
        assertTrue(groupesClm().contains("JURIDIQUE_SCIM:nouvelle.juriste@iq.example"), "et le domaine d'affaires");
        assertEquals(gid, corps(scim("GET", "/scim/v2/Users/" + uid, jeton, null), 200).at("/groups/0/value").asText());
        assertEquals(409, scim("POST", "/scim/v2/Groups", jeton, "{\"displayName\":\"juristes\"}").getStatus());
        assertEquals(1, corps(scim("GET", "/scim/v2/Groups?filter=displayName eq \"Juristes\"", jeton, null), 200).get("totalResults").asInt());

        corps(scim("PATCH", "/scim/v2/Groups/" + gid, jeton, "{\"Operations\":[{\"op\":\"remove\",\"path\":\"members[value eq \\\"" + uid + "\\\"]\"},"
                + "{\"op\":\"replace\",\"path\":\"displayName\",\"value\":\"Juristes IQ\"}]}"), 200);
        assertEquals("LECTEUR", roles("nouvelle.juriste@iq.example"), "retiré du groupe : retour au rôle par défaut");
        assertFalse(groupesClm().contains("JURIDIQUE_SCIM:nouvelle.juriste@iq.example"));
        corps(scim("PATCH", "/scim/v2/Groups/" + gid, jeton, "{\"Operations\":[{\"op\":\"add\",\"path\":\"members\",\"value\":[{\"value\":\"" + uid + "\"}]}]}"), 200);
        assertEquals("AUDITEUR", roles("nouvelle.juriste@iq.example"));
        corps(scim("PATCH", "/scim/v2/Groups/" + gid, jeton, "{\"Operations\":[{\"op\":\"remove\",\"path\":\"members\",\"value\":[{\"value\":\"" + uid + "\"}]},"
                + "{\"op\":\"replace\",\"value\":{\"displayName\":\"Juristes\"}}]}"), 200);
        JsonNode remplace = corps(scim("PUT", "/scim/v2/Groups/" + gid, jeton, "{\"displayName\":\"Juristes\",\"externalId\":\"aad-juristes\","
                + "\"members\":[{\"value\":\"" + uid + "\"}]}"), 200);
        assertEquals(1, remplace.get("members").size());
        corps(scim("PUT", "/scim/v2/Groups/" + gid, jeton, "{\"displayName\":\"Juristes\"}"), 200);
        assertEquals(gid, corps(scim("GET", "/scim/v2/Groups/" + gid, jeton, null), 200).get("id").asText());

        assertFalse(corps(scim("PATCH", "/scim/v2/Users/" + uid, jeton,
                "{\"Operations\":[{\"op\":\"Replace\",\"path\":\"active\",\"value\":\"False\"}]}"), 200).get("active").asBoolean(), "valeur texte d'Entra ID");
        assertTrue(corps(scim("PATCH", "/scim/v2/Users/" + uid, jeton,
                "{\"Operations\":[{\"op\":\"replace\",\"value\":{\"active\":true,\"displayName\":\"Nadia J.\"}}]}"), 200).get("active").asBoolean());
        assertEquals("Nadia Juriste", corps(scim("PATCH", "/scim/v2/Users/" + uid, jeton,
                "{\"Operations\":[{\"op\":\"replace\",\"path\":\"name.formatted\",\"value\":\"Nadia Juriste\"}]}"), 200).get("displayName").asText());
        assertEquals("Nadia", corps(scim("PUT", "/scim/v2/Users/" + uid, jeton, "{\"userName\":\"nouvelle.juriste@iq.example\",\"displayName\":\"Nadia\","
                + "\"active\":true}"), 200).get("displayName").asText());
        assertEquals(204, scim("DELETE", "/scim/v2/Groups/" + gid, jeton, null).getStatus());
        assertEquals(404, scim("GET", "/scim/v2/Groups/" + gid, jeton, null).getStatus());
        assertEquals(204, scim("DELETE", "/scim/v2/Users/" + uid, jeton, null).getStatus());
        assertFalse(users.findByEmail("nouvelle.juriste@iq.example").orElseThrow().isActif(), "déprovisionné : désactivé, jamais effacé");
        assertEquals(404, scim("GET", "/scim/v2/Users/abc", jeton, null).getStatus());

        String nouveau = corps(appel("POST", "/api/admin/annuaire/scim/jeton", admin, "{}"), 200).get("jeton").asText();
        assertEquals(401, scim("GET", "/scim/v2/Users", jeton, null).getStatus(), "l'ancien jeton est révoqué");
        assertEquals(200, scim("GET", "/scim/v2/Users", nouveau, null).getStatus());
        List<String> traces = journal.findAll().stream().map(l -> l.getAction() + " " + l.getDetails()).toList();
        assertTrue(traces.stream().anyMatch(t -> t.startsWith("ROLES_ANNUAIRE") && t.contains("LECTEUR → AUDITEUR")), "changement d'accès tracé");
        assertTrue(traces.stream().anyMatch(t -> t.startsWith("PROVISION_SCIM nouvelle.juriste@iq.example")));
        assertTrue(traces.stream().anyMatch(t -> t.startsWith("DEPROVISION_SCIM")));
    }

    @Test
    void la_connexion_unique_applique_les_attributs_et_refuse_un_compte_desactive() throws Exception {
        JsonNode c = corps(appel("POST", "/api/admin/annuaire/correspondances", admin,
                "{\"source\":\"ATTRIBUT\",\"attribut\":\"department\",\"valeur\":\"Financement\",\"groupe\":\"FIN_SSO\"}"), 201);
        Long tenant = users.findByEmail("admin.ti@iq.example").orElseThrow().getTenantId();
        IntegrationOidcService.ConfigOidc cfg = new IntegrationOidcService.ConfigOidc(tenant, "https://login.microsoftonline.com/x/v2.0",
                "client", null, "openid", "email", true, "LECTEUR", "Entra ID");
        assertNotNull(sessions.etablirSession(cfg, "Sso.Financement@iq.example", "Sam Sso",
                List.of(FaitAnnuaire.attribut("department", "financement"), FaitAnnuaire.attribut("jobTitle", "Analyste"))));
        assertTrue(groupesClm().contains("FIN_SSO:sso.financement@iq.example"), "domaine d'affaires tiré d'un attribut d'annuaire");
        sessions.etablirSession(cfg, "sso.financement@iq.example", "Sam Sso", List.of());
        assertFalse(groupesClm().contains("FIN_SSO:sso.financement@iq.example"), "attribut disparu : domaine retiré");

        User u = users.findByEmail("sso.financement@iq.example").orElseThrow();
        u.setActif(false);
        users.save(u);
        assertThrows(OidcClient.OidcException.class, () -> sessions.etablirSession(cfg, "sso.financement@iq.example", "Sam", List.of()));
        assertEquals(204, appel("DELETE", "/api/admin/annuaire/correspondances/" + c.get("id").asLong(), admin, null).getStatus());
        assertEquals(404, appel("DELETE", "/api/admin/annuaire/correspondances/" + c.get("id").asLong(), admin, null).getStatus());
        assertEquals(403, appel("GET", "/api/admin/annuaire/correspondances", initiateur, null).getStatus());
    }

    @Test
    void le_contrat_se_genere_en_word_modifiable_et_l_utilisation_se_mesure_par_enveloppe() throws Exception {
        long modele = corps(appel("POST", "/api/clm/modeles", initiateur, "{\"nom\":\"Convention Word\",\"corps\":\"Convention entre IQ et le demandeur.\","
                + "\"champs\":[],\"clauses\":[],\"parcours\":[]}"), 200).get("id").asLong();
        corps(appel("POST", "/api/clm/modeles/" + modele + "/soumettre", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + modele + "/publier", admin, "{}"), 200);
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Convention en Word\",\"modeleId\":" + modele + ",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        JsonNode word = corps(appel("POST", "/api/clm/trousses/" + id + "/generer?format=DOCX", initiateur, null), 200);
        assertEquals("DOCX", word.get("type").asText());
        assertTrue(word.get("nom").asText().endsWith(".docx"));
        assertTrue(appel("GET", "/api/clm/trousses/" + id + "/documents/" + word.get("id").asLong() + "/texte", initiateur, null)
                .getContentAsString(StandardCharsets.UTF_8).contains("Convention entre IQ"), "Word lisible et modifiable");

        long signee = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Enveloppe\",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + signee + "/documents")
                .file(new MockMultipartFile("fichier", "c.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
        long p = corps(appel("POST", "/api/clm/trousses/" + signee + "/participants", initiateur, "{\"nom\":\"Ext\",\"courriel\":\"ext@example.com\","
                + "\"telephone\":\"+15145550190\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":true}"), 200).get("id").asLong();
        corps(appel("PUT", "/api/clm/trousses/" + signee + "/parcours", initiateur, "[{\"cle\":\"s1\",\"libelle\":\"Signature\",\"phase\":1,"
                + "\"type\":\"SIGNATURE\",\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"" + p + "\"}]}]"), 200);
        corps(appel("POST", "/api/clm/trousses/" + signee + "/lancer", initiateur, "{}"), 200);
        LocalDate jour = LocalDate.now();
        JsonNode releve = corps(appel("GET", "/api/clm/admin/utilisation?du=" + jour.minusDays(1) + "&au=" + jour.plusDays(1), admin, null), 200);
        assertTrue(releve.get("envoyees").asInt() >= 1, releve.toString());
        assertTrue(releve.get("parMois").size() >= 1);
        assertEquals(400, appel("GET", "/api/clm/admin/utilisation?du=" + jour + "&au=" + jour.minusDays(1), admin, null).getStatus());
        assertEquals(403, appel("GET", "/api/clm/admin/utilisation?du=" + jour + "&au=" + jour, initiateur, null).getStatus());
    }

    private String roles(String courriel) {
        return String.join(",", users.findByEmail(courriel).orElseThrow().getRoles().stream().map(Enum::name).sorted().toList());
    }

    /** « CODE:courriel » de chaque appartenance aux groupes du module. */
    private String groupesClm() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (JsonNode g : corps(appel("GET", "/api/clm/groupes", admin, null), 200)) {
            g.get("membres").forEach(m -> sb.append(g.get("code").asText()).append(':').append(m.asText()).append(' '));
        }
        return sb.toString();
    }

    private MockHttpServletResponse scim(String methode, String url, String jeton, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url)
                .header("Authorization", "Bearer " + jeton);
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
