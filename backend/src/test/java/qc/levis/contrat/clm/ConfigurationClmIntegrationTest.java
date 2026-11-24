package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.clm.application.repository.ConnexionSalesforceRepository;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

/**
 * « Rien n'est figé » : listes de référence, paramètres du module et connexion Salesforce se configurent depuis
 * l'administration de chaque organisation, s'appliquent à elle seule et laissent une trace d'audit. Mêmes propriétés que
 * {@link ClmIntegrationTest}, pour partager son contexte.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false", "app.clm.documents.taille-max-octets=200000",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class ConfigurationClmIntegrationTest {

    private static final String PW = "Passw0rd!";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ModuleService modules;
    @Autowired UserRepository users;
    @Autowired TenantRepository tenants;
    @Autowired ConnexionSalesforceRepository connexions;

    private Cookie adminIq;
    private Cookie initiateurIq;
    private Cookie adminLevis;

    @BeforeEach
    void preparer() throws Exception {
        modules.activer(users.findByEmail("admin@levis.qc.ca").orElseThrow().getTenantId(), "clm_ia", "test");
        adminIq = login("admin.ti@iq.example");
        initiateurIq = login("initiateur@iq.example");
        adminLevis = login("admin@levis.qc.ca");
    }

    // --- Listes de référence ---

    @Test
    void l_organisation_ne_gere_que_les_listes_de_ses_modules() throws Exception {
        List<String> iq = codes(lire("/api/admin/reference/categories", adminIq));
        assertTrue(iq.containsAll(List.of("CLM_TYPE_CONTRAT", "CLM_UNITE", "CLM_ROLE_CONTRAT", "CLM_CHAMP_SALESFORCE", "CLM_CLAUSE_ATTENDUE")), iq.toString());
        assertFalse(iq.contains("TYPE_CONTRAT") || iq.contains("TYPE_CHAMP"), "aucune liste d'un module non souscrit : " + iq);
        assertTrue(codes(lire("/api/admin/reference/categories", adminLevis)).contains("TYPE_CONTRAT"));
        JsonNode plateforme = lire("/api/superadmin/reference/categories", login("superadmin@plateforme.ca", "SuperPassw0rd!"));
        assertTrue(codes(plateforme).containsAll(List.of("TYPE_CONTRAT", "TYPE_CHAMP", "CLM_TYPE_CONTRAT")), "la plateforme les voit toutes");
        for (JsonNode c : plateforme) {
            if ("CLM_CLAUSE_ATTENDUE".equals(c.get("code").asText())) {
                assertEquals("clm_ia", c.get("module").asText());
                assertFalse(c.get("libelleDetail").asText().isBlank(), "une liste à mots-clés annonce son détail");
            }
        }
        assertEquals(404, appel("GET", "/api/admin/reference?categorie=INCONNUE", adminIq, null).getStatus());
        assertEquals(403, appel("POST", "/api/admin/reference", initiateurIq,
                "{\"categorie\":\"CLM_UNITE\",\"code\":\"X\",\"libelle\":\"X\"}").getStatus(), "réservé à l'administration");
    }

    @Test
    void l_organisation_ajoute_personnalise_retire_puis_retablit_les_valeurs_de_la_plateforme() throws Exception {
        Map<String, String> unites = origines("CLM_UNITE");
        assertEquals("ORGANISATION", unites.get("FINANCEMENT_ENTREPRISES"), "unités propres à IQ : " + unites);
        Map<String, String> types = origines("CLM_TYPE_CONTRAT");
        assertEquals("PLATEFORME", types.get("SUBVENTION"));
        assertEquals("ORGANISATION", types.get("GARANTIE"));
        assertFalse(origines("CLM_UNITE", adminLevis).containsKey("FINANCEMENT_ENTREPRISES"), "les valeurs d'IQ ne sortent pas d'IQ");

        long services = creer("{\"categorie\":\"CLM_TYPE_CONTRAT\",\"code\":\"SERVICES\",\"libelle\":\"Services professionnels\","
                + "\"detail\":\"honoraires, mandat\",\"ordre\":25}").get("id").asLong();
        long pret = creer("{\"categorie\":\"CLM_TYPE_CONTRAT\",\"code\":\"PRET\",\"libelle\":\"Prêt\",\"actif\":false}").get("id").asLong();
        try {
            assertEquals("PERSONNALISEE", origines("CLM_TYPE_CONTRAT").get("SERVICES"));
            Map<String, String> options = options("CLM_TYPE_CONTRAT", initiateurIq);
            assertEquals("Services professionnels", options.get("SERVICES"), "la personnalisation remplace la valeur de la plateforme");
            assertFalse(options.containsKey("PRET"), "une valeur désactivée n'est plus proposée");
            assertEquals("Contrat de prêt", options("CLM_TYPE_CONTRAT", adminLevis).get("PRET"), "Lévis garde la valeur de la plateforme");
            assertEquals(409, appel("POST", "/api/admin/reference", adminIq,
                    "{\"categorie\":\"CLM_TYPE_CONTRAT\",\"code\":\"SERVICES\",\"libelle\":\"Doublon\"}").getStatus());
            assertEquals(400, appel("PUT", "/api/admin/reference/" + services, adminIq,
                    "{\"categorie\":\"CLM_TYPE_CONTRAT\",\"code\":\"SERVICES\",\"libelle\":\"" + "x".repeat(300) + "\"}").getStatus());
            JsonNode unite = creer("{\"categorie\":\"CLM_UNITE\",\"code\":\"AUDIT\",\"libelle\":\"Audit interne\",\"detail\":\"ignoré\"}");
            assertTrue(unite.get("detail").isNull(), "une liste sans détail n'en conserve pas");
            assertEquals(200, appel("DELETE", "/api/admin/reference/" + unite.get("id").asLong(), adminIq, null).getStatus());
        } finally {
            assertEquals(200, appel("DELETE", "/api/admin/reference/" + pret, adminIq, null).getStatus());
            assertEquals(200, appel("DELETE", "/api/admin/reference/" + services, adminIq, null).getStatus());
        }
        assertEquals("Contrat de prêt", options("CLM_TYPE_CONTRAT", initiateurIq).get("PRET"), "supprimer la personnalisation rétablit la plateforme");
        assertEquals("PLATEFORME", origines("CLM_TYPE_CONTRAT").get("SERVICES"));
        assertEquals(404, appel("DELETE", "/api/admin/reference/" + services, adminLevis, null).getStatus());
    }

    @Test
    void trousses_participants_et_modeles_puisent_dans_les_listes_de_l_organisation() throws Exception {
        JsonNode t = lireCorps(appel("POST", "/api/clm/trousses", initiateurIq,
                "{\"titre\":\"Garantie Essor\",\"typeContrat\":\"garantie\",\"unite\":\"FINANCEMENT_ENTREPRISES\",\"risque\":\"FAIBLE\"}"), 200);
        assertEquals("GARANTIE", t.at("/resume/typeContrat").asText(), "le code de la liste est retenu");
        long id = t.at("/resume/id").asLong();
        MockHttpServletResponse unite = appel("POST", "/api/clm/trousses", initiateurIq, "{\"titre\":\"X\",\"unite\":\"Ventes\"}");
        assertEquals(400, unite.getStatus());
        assertTrue(unite.getContentAsString(StandardCharsets.UTF_8).contains("Unité administrative"));
        assertEquals(400, appel("POST", "/api/clm/trousses", initiateurIq, "{\"titre\":\"X\",\"typeContrat\":\"INCONNU\"}").getStatus());
        assertEquals(200, appel("PUT", "/api/clm/trousses/" + id, initiateurIq,
                "{\"titre\":\"Garantie Essor\",\"typeContrat\":\"GARANTIE\",\"unite\":\"FINANCEMENT_ENTREPRISES\"}").getStatus());
        assertEquals(200, appel("POST", "/api/clm/trousses", adminLevis, "{\"titre\":\"Libre\",\"unite\":\"Ventes\"}").getStatus(),
                "sans liste d'unités, la saisie reste libre");

        assertEquals(400, appel("POST", "/api/clm/trousses/" + id + "/participants", initiateurIq,
                "{\"nom\":\"P\",\"courriel\":\"p@coop.example\",\"roleContrat\":\"PARRAIN\",\"externe\":true}").getStatus());
        JsonNode temoin = lireCorps(appel("POST", "/api/clm/trousses/" + id + "/participants", initiateurIq,
                "{\"nom\":\"T\",\"courriel\":\"t@coop.example\",\"roleContrat\":\"temoin\",\"externe\":true}"), 200);
        assertEquals("TEMOIN", temoin.get("roleContrat").asText());
        assertEquals(400, appel("PUT", "/api/clm/trousses/" + id + "/participants/" + temoin.get("id").asLong() + "/role", initiateurIq,
                "{\"roleContrat\":\"PARRAIN\"}").getStatus());

        assertEquals(400, appel("POST", "/api/clm/modeles", adminIq,
                "{\"nom\":\"M\",\"categorie\":\"INCONNUE\",\"corps\":\"x\",\"champs\":[],\"clauses\":[],\"parcours\":[]}").getStatus());
        long modele = lireCorps(appel("POST", "/api/clm/modeles", adminIq, """
                {"nom":"Garantie","categorie":"GARANTIE","corps":"Signé : {{sig}}","clauses":[],"parcours":[],
                 "champs":[{"cle":"sig","libelle":"Signature","type":"SIGNATURE","obligatoire":true,"roleDestinataire":"PARRAIN",
                            "position":{"page":1,"x":10,"y":10,"largeur":20,"hauteur":5}}]}
                """), 200).get("id").asLong();
        assertTrue(lire("/api/clm/modeles/" + modele + "/validation", adminIq).get("erreurs").toString().contains("PARRAIN"),
                "une zone adressée à un rôle hors liste ne serait remise à personne");
    }

    // --- Paramètres du module (UI-22) ---

    @Test
    void les_parametres_du_module_s_appliquent_a_l_organisation_seule() throws Exception {
        assertFalse(lire("/api/clm/admin/parametres", adminIq).at("/module/personnalise").asBoolean());
        MockHttpServletResponse refus = appel("PUT", "/api/clm/admin/parametres", adminIq,
                "{\"invitationValiditeJours\":0,\"prefixeReference\":\"1A\",\"identiteExterne\":{\"faible\":\"FORTE\",\"moyen\":\"COURRIEL\"}}");
        assertEquals(400, refus.getStatus());
        String motifs = refus.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(motifs.contains("Validité d'une invitation") && motifs.contains("Préfixe") && motifs.contains("externes"), motifs);
        assertEquals(400, appel("PUT", "/api/clm/admin/parametres", adminIq, "{\"identiteInterne\":{\"faible\":\"RETINE\"}}").getStatus());
        assertEquals(403, appel("PUT", "/api/clm/admin/parametres", initiateurIq, "{\"rappelsMax\":1}").getStatus());
        try {
            JsonNode p = lireCorps(appel("PUT", "/api/clm/admin/parametres", adminIq, """
                    {"prefixeReference":"iq","rappelFrequenceJours":5,"rappelsMax":2,"invitationValiditeJours":7,"tailleMaxOctets":150000,
                     "identiteInterne":{"faible":"CODE_SMS","moyen":"CODE_SMS","eleve":"FORTE"},"tentativesIdentite":3,"champMontant":"montantAide"}
                    """), 200);
            assertTrue(p.get("personnalise").asBoolean());
            assertEquals("IQ", p.get("prefixeReference").asText());
            assertEquals("admin.ti@iq.example", p.get("modifiePar").asText());

            JsonNode t = lireCorps(appel("POST", "/api/clm/trousses", initiateurIq, "{\"titre\":\"Selon IQ\",\"risque\":\"FAIBLE\"}"), 200);
            assertTrue(t.at("/resume/reference").asText().startsWith("IQ-"), t.at("/resume/reference").asText());
            assertEquals(5, t.get("rappelFrequenceJours").asInt());
            assertEquals(2, t.get("rappelsMax").asInt());
            long id = t.at("/resume/id").asLong();
            JsonNode interne = lireCorps(appel("POST", "/api/clm/trousses/" + id + "/participants", initiateurIq,
                    "{\"nom\":\"Étienne Côté\",\"courriel\":\"signataire@iq.example\",\"telephone\":\"+15145550123\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":false}"), 200);
            assertEquals("CODE_SMS", interne.get("niveauIdentite").asText(), "la politique d'IQ exige le SMS dès le risque faible");
            MockHttpServletResponse lourd = mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                    .file(new MockMultipartFile("fichier", "lourd.pdf", "application/pdf", pdf(160_000))).cookie(initiateurIq)).andReturn().getResponse();
            assertEquals(400, lourd.getStatus(), "au-delà de la taille fixée par IQ");

            JsonNode publics = lire("/api/clm/parametres", initiateurIq);
            assertEquals("CODE_SMS", publics.at("/identiteInterne/faible").asText());
            assertEquals(5, publics.get("rappelFrequenceJours").asInt());
            assertEquals("TR", lire("/api/clm/admin/parametres", adminLevis).at("/module/prefixeReference").asText(), "Lévis est inchangée");
            assertTrue(lire("/api/audit?action=CLM_CONFIGURATION&entite=PARAMETRES", adminIq).toString().contains("préfixe IQ"));
        } finally {
            JsonNode retabli = lireCorps(appel("DELETE", "/api/clm/admin/parametres", adminIq, null), 200);
            assertFalse(retabli.get("personnalise").asBoolean());
            assertEquals("TR", retabli.get("prefixeReference").asText());
        }
    }

    // --- Connexion Salesforce (UI-21) ---

    @Test
    void la_connexion_salesforce_est_propre_a_chaque_organisation_et_son_secret_ne_ressort_jamais() throws Exception {
        String rest = """
                {"mode":"REST","instanceUrl":"%s","tokenUrl":"https://login.salesforce.com/services/oauth2/token","clientId":"3MVG9-iq-client",
                 "clientSecret":%s,"objet":"%s","versionApi":"v61.0","champStatut":"Etat_CLM__c","champReference":"No_Trousse__c",
                 "champCorrelation":"Trace__c","reprisesMax":4}""";
        assertFalse(lire("/api/clm/admin/integrations/salesforce", adminIq).at("/config/personnalisee").asBoolean());
        MockHttpServletResponse hote = appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", adminIq,
                rest.formatted("https://intranet.iq.example", "\"s\"", "FundingRequest"));
        assertEquals(400, hote.getStatus());
        assertTrue(hote.getContentAsString(StandardCharsets.UTF_8).contains("domaine autorisé"), "aucune requête vers un hôte arbitraire");
        assertEquals(400, appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", adminIq,
                rest.formatted("https://iq.my.salesforce.com", "null", "FundingRequest")).getStatus(), "secret requis");
        assertEquals(400, appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", adminIq,
                rest.formatted("https://iq.my.salesforce.com", "\"s\"", "Funding' OR Name != '")).getStatus(), "nom d'objet injecté dans le SOQL");

        Long tenantIq = tenants.findBySlug("iq").orElseThrow().getId();
        try {
            MockHttpServletResponse ok = appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", adminIq,
                    rest.formatted("https://iq.my.salesforce.com", "\"s3cr3t-iq\"", "FundingRequest"));
            assertEquals(200, ok.getStatus(), ok.getContentAsString(StandardCharsets.UTF_8));
            String corps = ok.getContentAsString(StandardCharsets.UTF_8);
            assertFalse(corps.contains("s3cr3t-iq") || corps.contains("3MVG9-iq-client"), "ni secret ni identifiant en clair");
            JsonNode c = json.readTree(corps);
            assertEquals("REST", c.get("mode").asText());
            assertTrue(c.get("secretConfigure").asBoolean() && c.get("personnalisee").asBoolean());
            assertEquals(4, c.get("reprisesMax").asInt());
            String chiffre = connexions.findByTenantId(tenantIq).orElseThrow().getSecretChiffre();
            assertNotEquals("s3cr3t-iq", chiffre, "le secret est chiffré au repos");

            JsonNode sansSecret = lireCorps(appel("PUT", "/api/clm/admin/integrations/salesforce/configuration", adminIq,
                    rest.formatted("https://iq.my.salesforce.com", "\"\"", "FundingRequest").replace("\"reprisesMax\":4", "\"reprisesMax\":6")), 200);
            assertTrue(sansSecret.get("secretConfigure").asBoolean(), "un secret laissé vide conserve celui en place");
            assertEquals(chiffre, connexions.findByTenantId(tenantIq).orElseThrow().getSecretChiffre());
            assertEquals("SIMULATION", lire("/api/clm/admin/integrations/salesforce", adminLevis).at("/config/mode").asText(), "Lévis est inchangée");
            assertEquals(404, appel("PUT", "/api/clm/admin/integrations/salesforce/simulation/panne", adminIq, "{\"panne\":true}").getStatus(),
                    "IQ ne relève plus de la simulation");
            assertTrue(lire("/api/audit?action=CLM_CONFIGURATION&entite=SALESFORCE", adminIq).toString().contains("secret remplacé"));
        } finally {
            JsonNode retablie = lireCorps(appel("DELETE", "/api/clm/admin/integrations/salesforce/configuration", adminIq, null), 200);
            assertFalse(retablie.get("personnalisee").asBoolean());
            assertTrue(connexions.findByTenantId(tenantIq).isEmpty(), "le secret disparaît avec la configuration");
        }
    }

    @Test
    void le_dernier_test_de_connexion_d_une_organisation_n_apparait_pas_chez_une_autre() throws Exception {
        JsonNode levis = lireCorps(appel("POST", "/api/clm/admin/integrations/salesforce/tester", adminLevis, "{}"), 200);
        lireCorps(appel("POST", "/api/clm/admin/integrations/salesforce/tester", adminIq, "{}"), 200);
        JsonNode vu = lire("/api/clm/admin/integrations/salesforce", adminLevis).get("connexion");
        assertEquals(levis.get("testeLe").asText(), vu.get("testeLe").asText());
    }

    // --- Outils ---

    private JsonNode creer(String corps) throws Exception {
        return lireCorps(appel("POST", "/api/admin/reference", adminIq, corps), 200);
    }

    private Map<String, String> origines(String categorie) throws Exception {
        return origines(categorie, adminIq);
    }

    private Map<String, String> origines(String categorie, Cookie qui) throws Exception {
        Map<String, String> m = new HashMap<>();
        lire("/api/admin/reference?categorie=" + categorie, qui).forEach(v -> m.put(v.get("code").asText(), v.get("origine").asText()));
        return m;
    }

    private Map<String, String> options(String categorie, Cookie qui) throws Exception {
        Map<String, String> m = new HashMap<>();
        lire("/api/reference/" + categorie, qui).forEach(v -> m.put(v.get("code").asText(), v.get("libelle").asText()));
        return m;
    }

    private static List<String> codes(JsonNode categories) {
        List<String> codes = new ArrayList<>();
        categories.forEach(c -> codes.add(c.get("code").asText()));
        return codes;
    }

    /** PDF valide d'au moins {@code taille} octets (flux de remplissage hors des objets). */
    private static byte[] pdf(int taille) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(out);
            byte[] base = out.toByteArray();
            byte[] lourd = new byte[Math.max(taille, base.length)];
            System.arraycopy(base, 0, lourd, 0, base.length);
            java.util.Arrays.fill(lourd, base.length, lourd.length, (byte) '\n');
            return lourd;
        }
    }

    private Cookie login(String email) throws Exception {
        return login(email, PW);
    }

    private Cookie login(String email, String motDePasse) throws Exception {
        Cookie c = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "motDePasse", motDePasse))))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
        assertTrue(c != null, "connexion de " + email);
        return c;
    }

    private MockHttpServletResponse appel(String methode, String url, Cookie qui, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(methode), url).cookie(qui);
        if (corps != null) {
            r.contentType(MediaType.APPLICATION_JSON).content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
    }

    private JsonNode lire(String url, Cookie qui) throws Exception {
        return lireCorps(appel("GET", url, qui, null), 200);
    }

    private JsonNode lireCorps(MockHttpServletResponse r, int attendu) throws Exception {
        String corps = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(attendu, r.getStatus(), corps);
        return json.readTree(corps);
    }
}
