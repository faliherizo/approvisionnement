package qc.levis.contrat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.addenda.domain.Addenda;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.domain.Gabarit;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration couvrant les principaux parcours de l'API (authentification,
 * clauses, documents, addendas, versions, obligations, signature, templates, IA,
 * multi-tenant, gestion des utilisateurs). Boot complet sur H2 + Liquibase.
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties =
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-test")
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static final String PW = "Passw0rd!";

    // --- Helpers ---
    private Cookie login(String email, String motDePasse, String... extra) throws Exception {
        String body = extra.length == 2
                ? "{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"," + extra[0] + ":" + extra[1] + "}"
                : "{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}";
        MvcResult r = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
        return r.getResponse().getCookie("ACCESS_TOKEN");
    }

    private JsonNode post(String url, Cookie c, String body) throws Exception {
        var req = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (c != null) req = req.cookie(c);
        String s = mvc.perform(req).andReturn().getResponse().getContentAsString();
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    private JsonNode put(String url, Cookie c, String body) throws Exception {
        var req = MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (c != null) req = req.cookie(c);
        String s = mvc.perform(req).andReturn().getResponse().getContentAsString();
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    private JsonNode get(String url, Cookie c) throws Exception {
        var req = MockMvcRequestBuilders.get(url);
        if (c != null) req = req.cookie(c);
        String s = mvc.perform(req).andReturn().getResponse().getContentAsString();
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    /** Fait passer un document en statut APPROUVE : chaque niveau du circuit est décidé par une personne de son rôle. */
    private void validerDocument(Cookie c, long id) throws Exception {
        post("/api/documents/" + id + "/approbation", c, "{}");
        for (JsonNode etape : get("/api/documents/" + id + "/approbation", c)) {
            post("/api/documents/approbation/" + etape.get("id").asLong() + "/decision", decideur(etape.get("roleRequis").asText()),
                    "{\"approuve\":true,\"note\":\"ok\"}");
        }
    }

    private Cookie decideur(String role) throws Exception {
        return switch (role) {
            case "JURIDIQUE" -> login("juriste@levis.qc.ca", PW);
            case "DIRECTION" -> login("direction@levis.qc.ca", PW);
            default -> login("redacteur@levis.qc.ca", PW);
        };
    }

    /** La spécification OpenAPI décrit chaque DTO, même quand deux modules emploient le même nom simple. */
    @Test
    void openapi_distingue_les_dto_de_meme_nom_simple() throws Exception {
        JsonNode schemas = json.readTree(mvc.perform(MockMvcRequestBuilders.get("/v3/api-docs")).andReturn().getResponse().getContentAsString())
                .get("components").get("schemas");
        java.util.List<String> commentaires = new java.util.ArrayList<>();
        schemas.fieldNames().forEachRemaining(n -> { if (n.endsWith("CommentaireRequest")) commentaires.add(n); });
        assertEquals(2, commentaires.size(), commentaires.toString());
        boolean texte = false, corps = false;
        for (String n : commentaires) {
            texte |= schemas.get(n).get("properties").has("texte");
            corps |= schemas.get(n).get("properties").has("corps");
        }
        assertTrue(texte && corps, "les deux schémas sont publiés : " + commentaires);
    }

    /** Séparation des responsabilités : rôle requis, ordre du circuit, décision unique (avis 2026-50-92, art. 2.1 n° 6b). */
    @Test
    void approbation_reservee_au_role_du_niveau_dans_l_ordre_et_une_seule_fois() throws Exception {
        Cookie redacteur = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", redacteur, "{\"titre\":\"Circuit\",\"type\":\"GRE_A_GRE\",\"modeSollicitation\":\"GRE_A_GRE\"}").get("id").asLong();
        JsonNode etapes = post("/api/documents/" + id + "/approbation", redacteur, "{}");
        long n1 = etapes.get(0).get("id").asLong();
        long n2 = etapes.get(1).get("id").asLong();
        String ok = "{\"approuve\":true,\"note\":\"ok\"}";
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/approbation/" + n2 + "/decision").cookie(redacteur)
                .contentType(MediaType.APPLICATION_JSON).content(ok)).andExpect(status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/approbation/" + n2 + "/decision").cookie(login("juriste@levis.qc.ca", PW))
                .contentType(MediaType.APPLICATION_JSON).content(ok)).andExpect(status().isConflict());
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/approbation/" + n1 + "/decision").cookie(redacteur)
                .contentType(MediaType.APPLICATION_JSON).content(ok)).andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/approbation/" + n1 + "/decision").cookie(redacteur)
                .contentType(MediaType.APPLICATION_JSON).content(ok)).andExpect(status().isConflict());
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/approbation/" + n2 + "/decision").cookie(login("juriste@levis.qc.ca", PW))
                .contentType(MediaType.APPLICATION_JSON).content(ok)).andExpect(status().isOk());
    }

    /** La clause qui déclare l'exclusion prévaut, même si la clause exclue a été créée avant elle (cahier EF2). */
    @Test
    void ef2_exclusion_independante_de_l_ordre_de_creation() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long exclue = post("/api/clauses", c, "{\"code\":\"ORD-X\",\"titre\":\"X\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"x\"}").get("id").asLong();
        long requise = post("/api/clauses", c, "{\"code\":\"ORD-R\",\"titre\":\"R\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"r\"}").get("id").asLong();
        long principale = post("/api/clauses", c, "{\"code\":\"ORD-P\",\"titre\":\"P\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"p\","
                + "\"dependances\":[\"ORD-R\"],\"exclusions\":[\"ORD-X\"]}").get("id").asLong();
        long doc = post("/api/documents", c, "{\"titre\":\"Ordre\",\"type\":\"GRE_A_GRE\",\"modeSollicitation\":\"GRE_A_GRE\"}").get("id").asLong();
        JsonNode ass = post("/api/documents/" + doc + "/assembler", c, "{\"clauseIds\":[" + exclue + "," + principale + "]}");
        java.util.List<String> codes = new java.util.ArrayList<>();
        ass.get("clauses").forEach(dc -> codes.add(dc.get("code").asText()));
        assertTrue(codes.contains("ORD-P") && codes.contains("ORD-R"), codes.toString());
        assertFalse(codes.contains("ORD-X"), "clause exclue retirée : " + codes);
        assertTrue(requise > 0);
    }

    /** Clauses exigées par la loi (Charte de la langue française, AMP) : normatives, donc verrouillées dans les documents. */
    @Test
    void les_clauses_legales_de_demonstration_sont_normatives() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        java.util.Map<String, Boolean> normatives = new java.util.HashMap<>();
        get("/api/clauses", c).forEach(cl -> normatives.put(cl.get("code").asText(), cl.get("normative").asBoolean()));
        assertTrue(normatives.get("GEN-002"), "Charte de la langue française");
        assertTrue(normatives.get("AOP-020"), "autorisation de l'AMP");
        assertFalse(normatives.get("GEN-001"));
    }

    // --- Santé & authentification ---
    @Test
    void health_estPublic() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void login_mauvaisMotDePasse_401() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_cookie_puisMe_etLogout() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        assertNotNull(c);
        assertTrue(c.isHttpOnly());
        JsonNode me = get("/api/auth/me", c);
        assertEquals("redacteur@levis.qc.ca", me.get("email").asText());
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/logout").cookie(c)).andExpect(status().isNoContent());
    }

    @Test
    void mfaQuestion_defiPuisReponse() throws Exception {
        JsonNode defi = post("/api/auth/login", new Cookie("x", ""),
                "{\"email\":\"question@levis.qc.ca\",\"motDePasse\":\"" + PW + "\"}");
        assertTrue(defi.get("mfaRequired").asBoolean());
        assertEquals("question", defi.get("securityMethod").asText());
        JsonNode ok = post("/api/auth/login", new Cookie("x", ""),
                "{\"email\":\"question@levis.qc.ca\",\"motDePasse\":\"" + PW + "\",\"securityAnswers\":[\"Montreal\"]}");
        assertTrue(ok.get("ok").asBoolean());
    }

    @Test
    void mfaTotp_avecCodeCalcule() throws Exception {
        String code = totp("JBSWY3DPEHPK3PXP");
        JsonNode ok = post("/api/auth/login", new Cookie("x", ""),
                "{\"email\":\"totp@levis.qc.ca\",\"motDePasse\":\"" + PW + "\",\"totpCode\":\"" + code + "\"}");
        assertTrue(ok.get("ok").asBoolean());
    }

    @Test
    void connexion_parSousDomaine_verifieLeTenant() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        post("/api/superadmin/tenants", sa, "{\"slug\":\"ville-x\",\"titre\":\"Ville X\",\"sousTitre\":\"x\"}");

        Cookie vide = new Cookie("x", "");
        // Bon sous-domaine (levis) pour un compte du tenant Lévis → succès.
        JsonNode ok = post("/api/auth/login", vide,
                "{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"" + PW + "\",\"slug\":\"levis\"}");
        assertTrue(ok.get("ok").asBoolean());

        // Mauvais sous-domaine (ville-x) → refus, le compte n'appartient pas à cet établissement.
        JsonNode ko = post("/api/auth/login", vide,
                "{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"" + PW + "\",\"slug\":\"ville-x\"}");
        assertFalse(ko.get("ok").asBoolean());

        // Sans slug (domaine nu / dev) → rétrocompatible, succès.
        JsonNode neutre = post("/api/auth/login", vide,
                "{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"" + PW + "\"}");
        assertTrue(neutre.get("ok").asBoolean());
    }

    @Test
    void superadmin_gereLeTarifParTenant() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long id = post("/api/superadmin/tenants", sa,
                "{\"slug\":\"ville-tarif\",\"titre\":\"Ville Tarif\",\"sousTitre\":\"t\"}").get("id").asLong();

        // Tarif initial vierge.
        assertEquals(0, get("/api/superadmin/tenants/" + id + "/tarif", sa).get("forfaitAnnuel").decimalValue().signum());

        // Enregistrement d'un forfait annuel + indexation.
        JsonNode maj = put("/api/superadmin/tenants/" + id + "/tarif", sa,
                "{\"forfaitAnnuel\":58000,\"devise\":\"cad\",\"indexationPct\":3,\"nbUtilisateursInclus\":50,\"notes\":\"tout inclus\",\"actif\":true}");
        assertEquals(0, new java.math.BigDecimal("58000").compareTo(maj.get("forfaitAnnuel").decimalValue()));
        assertEquals("CAD", maj.get("devise").asText());

        // Persistance : relecture.
        assertEquals("CAD", get("/api/superadmin/tenants/" + id + "/tarif", sa).get("devise").asText());

        // RBAC : un administrateur de tenant n'accède pas à la gestion des tarifs.
        Cookie admin = login("admin@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/superadmin/tenants/" + id + "/tarif").cookie(admin))
                .andExpect(status().isForbidden());
    }

    @Test
    void superadmin_editeUnTenant() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long id = post("/api/superadmin/tenants", sa,
                "{\"slug\":\"ville-edit\",\"titre\":\"Ville Edit\",\"sousTitre\":\"e\"}").get("id").asLong();

        JsonNode maj = put("/api/superadmin/tenants/" + id, sa,
                "{\"titre\":\"Ville Editee\",\"sousTitre\":\"maj\",\"actif\":false}");
        assertEquals("Ville Editee", maj.get("titre").asText());
        assertFalse(maj.get("actif").asBoolean());
    }

    @Test
    void admin_configureIntegrationM365_secretMasque_etRbac() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);

        // Enregistrement de la configuration M365 du tenant (actif=false : le tenant Lévis reste en
        // simulation pour les autres tests ; la bascule réelle est couverte en test unitaire).
        JsonNode vue = put("/api/parametres/integration-m365", admin,
                "{\"actif\":false,\"msTenantId\":\"ms-1\",\"clientId\":\"cli-1\",\"clientSecret\":\"tres-secret\","
                + "\"sharepointSiteId\":\"site-1\",\"driveId\":\"drive-1\",\"dossierRacine\":\"Contrats\"}");
        assertEquals("cli-1", vue.get("clientId").asText());
        assertTrue(vue.get("secretDefini").asBoolean());
        assertNull(vue.get("clientSecret"));                 // le secret n'est jamais renvoyé

        // Relecture : la présence du secret persiste, toujours sans l'exposer.
        JsonNode relu = get("/api/parametres/integration-m365", admin);
        assertTrue(relu.get("secretDefini").asBoolean());
        assertNull(relu.get("clientSecret"));

        // Test de connexion sans config active → résultat structuré, sans appel réseau.
        JsonNode test = post("/api/parametres/integration-m365/test", admin, "{}");
        assertFalse(test.get("succes").asBoolean());

        // RBAC : un rédacteur n'accède pas à la configuration M365 du tenant.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/parametres/integration-m365").cookie(red))
                .andExpect(status().isForbidden());
    }

    @Test
    void publication_refuseeAvantValidation_puisApercuImage() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Doc gate\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");

        // Avant validation : la publication est refusée (409 Conflit).
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/" + id + "/publications").cookie(c)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cible\":\"SHAREPOINT\"}"))
                .andExpect(status().isConflict());

        // L'aperçu image (PNG) de la première page est disponible et fidèle.
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf/apercu-image").cookie(c))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("png")));

        // Après validation : la publication passe.
        validerDocument(c, id);
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/" + id + "/publications").cookie(c)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cible\":\"SHAREPOINT\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void simulateur_depotsReels_visualisablesEtTelechargeables() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Doc simulateur\",\"type\":\"GRE_A_GRE\"}")
                .get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        validerDocument(c, id); // publication autorisée seulement après validation
        post("/api/documents/" + id + "/publications", c,
                "{\"cible\":\"SHAREPOINT\",\"format\":\"PDF\",\"destination\":\"Contrats/demo\"}");

        // Le dépôt simulé apparaît dans la visionneuse, rangé sous l'environnement SHAREPOINT.
        // On repère précisément NOTRE dépôt par son chemin (le magasin est partagé entre les tests).
        JsonNode depots = get("/api/simulateur/depots", c);
        assertTrue(depots.isArray());
        String chemin = null;
        for (JsonNode d : depots) {
            String ch = d.get("chemin").asText();
            if (ch.startsWith("SHAREPOINT/Contrats/demo/")) chemin = ch;
        }
        assertNotNull(chemin, "le dépôt SHAREPOINT/Contrats/demo doit être présent");

        // Le fichier réellement déposé est téléchargeable (PDF).
        mvc.perform(MockMvcRequestBuilders.get("/api/simulateur/depots/contenu")
                        .param("chemin", chemin).cookie(c))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("pdf")));

        // RBAC : un juriste (sans droit de publication) ne consulte pas la visionneuse.
        Cookie jur = login("juriste@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/simulateur/depots").cookie(jur))
                .andExpect(status().isForbidden());
    }

    @Test
    void repertoiresDepot_personnels_parUtilisateur() throws Exception {
        Cookie red = login("redacteur@levis.qc.ca", PW);

        // Ajout d'un répertoire personnel (chemin normalisé).
        JsonNode ajoute = post("/api/profil/repertoires", red, "{\"chemin\":\"/Contrats/2027/\"}");
        assertEquals("Contrats/2027", ajoute.get("chemin").asText());
        long repId = ajoute.get("id").asLong();

        // Il apparaît dans SA liste.
        assertTrue(get("/api/profil/repertoires", red).toString().contains("Contrats/2027"));

        // Isolation : un autre utilisateur ne voit pas ce répertoire.
        Cookie admin = login("admin@levis.qc.ca", PW);
        assertFalse(get("/api/profil/repertoires", admin).toString().contains("Contrats/2027"));

        // Suppression par le propriétaire.
        mvc.perform(MockMvcRequestBuilders.delete("/api/profil/repertoires/" + repId).cookie(red))
                .andExpect(status().isNoContent());
        assertFalse(get("/api/profil/repertoires", red).toString().contains("Contrats/2027"));
    }

    @Test
    void admin_configureIntegrationSap_secretsMasques_etRbac() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);

        // actif=false : le tenant Lévis reste en simulation pour les autres tests.
        JsonNode vue = put("/api/parametres/integration-sap", admin,
                "{\"actif\":false,\"baseUrl\":\"https://sap.ville.ca/odata\",\"servicePath\":\"/ZREF/References\","
                + "\"auth\":\"basic\",\"username\":\"sapuser\",\"password\":\"tres-secret\",\"apiKey\":\"cle-123\"}");
        assertEquals("sapuser", vue.get("username").asText());
        assertTrue(vue.get("passwordDefini").asBoolean());
        assertTrue(vue.get("apiKeyDefini").asBoolean());
        assertNull(vue.get("password"));       // secrets jamais renvoyés
        assertNull(vue.get("apiKey"));

        JsonNode relu = get("/api/parametres/integration-sap", admin);
        assertEquals("https://sap.ville.ca/odata", relu.get("baseUrl").asText());
        assertTrue(relu.get("passwordDefini").asBoolean());

        // Test de connexion sans config active → résultat structuré, sans appel réseau.
        JsonNode test = post("/api/parametres/integration-sap/test", admin, "{}");
        assertFalse(test.get("succes").asBoolean());

        // RBAC : un rédacteur n'accède pas à la configuration SAP du tenant.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/parametres/integration-sap").cookie(red))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_configureIntegrationOidc_secretMasque_etRbac() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);

        // actif=false : le tenant Lévis n'active pas l'OIDC pour les autres tests.
        JsonNode vue = put("/api/parametres/integration-oidc", admin,
                "{\"actif\":false,\"issuer\":\"https://login.microsoftonline.com/abc/v2.0\","
                + "\"clientId\":\"client-abc\",\"clientSecret\":\"tres-secret\","
                + "\"boutonLibelle\":\"Connexion gouvernementale\",\"roleParDefaut\":\"REDACTEUR\"}");
        assertEquals("client-abc", vue.get("clientId").asText());
        assertTrue(vue.get("secretDefini").asBoolean());
        assertNull(vue.get("clientSecret"));                 // le secret n'est jamais renvoyé
        assertEquals("openid profile email", vue.get("scopes").asText()); // défaut appliqué

        // Relecture : la présence du secret persiste, toujours sans l'exposer.
        JsonNode relu = get("/api/parametres/integration-oidc", admin);
        assertTrue(relu.get("secretDefini").asBoolean());
        assertEquals("https://login.microsoftonline.com/abc/v2.0", relu.get("issuer").asText());
        assertNull(relu.get("clientSecret"));

        // Test de découverte sur un émetteur injoignable → échec structuré, sans appel externe.
        JsonNode test = post("/api/parametres/integration-oidc/test", admin,
                "{\"issuer\":\"http://127.0.0.1:1\"}");
        assertFalse(test.get("succes").asBoolean());

        // RBAC : un rédacteur n'accède pas à la configuration OIDC du tenant.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/parametres/integration-oidc").cookie(red))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_configureIntegrationSaml_etRbac() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);

        // actif=false : le tenant Lévis n'active pas le SAML pour les autres tests.
        JsonNode vue = put("/api/parametres/integration-saml", admin,
                "{\"actif\":false,\"idpEntityId\":\"https://idp.ville.ca/entity\","
                + "\"idpSsoUrl\":\"https://idp.ville.ca/sso\","
                + "\"idpCertificat\":\"cert-invalide-non-parsable\","
                + "\"spEntityId\":\"https://contrat.levis.qc.ca/sp\",\"roleParDefaut\":\"REDACTEUR\"}");
        assertEquals("https://idp.ville.ca/entity", vue.get("idpEntityId").asText());
        assertTrue(vue.get("certificatDefini").asBoolean());
        assertEquals("email", vue.get("claimCourriel").asText());       // défaut appliqué
        assertEquals("Connexion SAML", vue.get("boutonLibelle").asText()); // défaut appliqué

        // Relecture : la configuration persiste (le certificat de l'IdP étant public, il est renvoyé).
        JsonNode relu = get("/api/parametres/integration-saml", admin);
        assertEquals("https://idp.ville.ca/sso", relu.get("idpSsoUrl").asText());
        assertTrue(relu.get("certificatDefini").asBoolean());

        // Test avec un certificat X.509 illisible → échec structuré, sans appel réseau.
        JsonNode test = post("/api/parametres/integration-saml/test", admin, "{}");
        assertFalse(test.get("succes").asBoolean());

        // RBAC : un rédacteur n'accède pas à la configuration SAML du tenant.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/parametres/integration-saml").cookie(red))
                .andExpect(status().isForbidden());
    }

    @Test
    void profil_miseAJour() throws Exception {
        Cookie c = login("juriste@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.put("/api/profil").cookie(c)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nom\":\"Juriste MAJ\"}"))
                .andExpect(status().isOk());
    }

    // --- Clauses, documents, moteur de règles (dont EF2) ---
    @Test
    void cycleDocument_assemblage_approbation_export() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        assertTrue(get("/api/clauses", c).size() >= 5);

        JsonNode doc = post("/api/documents", c,
                "{\"titre\":\"IT-Doc\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\",\"valeurEstimee\":1500000}");
        long id = doc.get("id").asLong();

        assertTrue(get("/api/documents/" + id + "/clauses-eligibles", c).size() >= 1);
        JsonNode assemble = post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        assertTrue(assemble.get("clauses").size() >= 2);

        post("/api/documents/" + id + "/commentaires", c, "{\"corps\":\"Un commentaire\"}");
        assertTrue(post("/api/documents/" + id + "/approbation", c, "{}").size() >= 1);

        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(c))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(c))
                .andExpect(status().isOk());
    }

    // --- Dossier d'approvisionnement (P1 - art. 15) ---
    @Test
    void dossier_regroupementDocuments_etRapport() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);

        JsonNode dossier = post("/api/dossiers", c,
                "{\"titre\":\"Acquisition solution redaction\",\"numeroAO\":\"7000017905\","
                + "\"typeAcquisition\":\"TECHNOLOGIES\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}");
        long dossierId = dossier.get("id").asLong();
        assertEquals("OUVERT", dossier.get("statut").asText());

        // Document créé directement dans le dossier.
        long docA = post("/api/documents", c,
                "{\"titre\":\"Cahier des charges\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"dossierId\":" + dossierId + "}")
                .get("id").asLong();
        // Document créé isolé puis rattaché.
        long docB = post("/api/documents", c, "{\"titre\":\"Addenda 1\",\"type\":\"ADDENDA\"}").get("id").asLong();
        post("/api/dossiers/" + dossierId + "/documents/" + docB, c, "{}");

        assertEquals(2, get("/api/dossiers/" + dossierId + "/documents", c).size());

        JsonNode rapport = get("/api/dossiers/" + dossierId + "/rapport", c);
        assertEquals(2, rapport.get("nombreDocuments").asInt());

        // Changement de statut + détachement.
        JsonNode maj = post("/api/dossiers/" + dossierId + "/statut", c, "{\"statut\":\"EN_COURS\"}");
        assertEquals("EN_COURS", maj.get("statut").asText());
        mvc.perform(MockMvcRequestBuilders.delete("/api/dossiers/" + dossierId + "/documents/" + docA).cookie(c))
                .andExpect(status().isOk());
        assertEquals(1, get("/api/dossiers/" + dossierId + "/documents", c).size());

        assertTrue(get("/api/dossiers", c).size() >= 1);

        // Lecture unitaire + modification (PUT).
        assertEquals("Acquisition solution redaction", get("/api/dossiers/" + dossierId, c).get("titre").asText());
        JsonNode modifie = put("/api/dossiers/" + dossierId, c,
                "{\"titre\":\"Acquisition revisee\",\"responsable\":\"Sarra Boulahmi\"}");
        assertEquals("Acquisition revisee", modifie.get("titre").asText());
        assertEquals("Sarra Boulahmi", modifie.get("responsable").asText());

        // Suppression logique : le dossier disparaît des listes, ses documents sont détachés.
        mvc.perform(MockMvcRequestBuilders.delete("/api/dossiers/" + dossierId).cookie(c))
                .andExpect(status().isOk());
        for (JsonNode d : get("/api/dossiers", c)) {
            assertNotEquals(dossierId, d.get("id").asLong());
        }
    }

    // --- Assistant guidé + recherche enrichie (P2 - art. 7.1, 7.2, 7.4) ---
    @Test
    void assistant_analyse_puisGenere_viaTemplateEtManuel() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);

        // Template classé par type d'acquisition (art. 7.1).
        String codeTpl = "TR-TI-" + System.currentTimeMillis();
        post("/api/templates", c, "{\"code\":\"" + codeTpl + "\",\"nom\":\"Appel offres TI\","
                + "\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\","
                + "\"categorieAcquisition\":\"TECHNOLOGIES\",\"clauseCodes\":[\"GEN-001\"]}");
        long tplId = 0;
        for (JsonNode t : get("/api/templates", c)) {
            if (codeTpl.equals(t.get("code").asText())) tplId = t.get("id").asLong();
        }
        assertTrue(tplId > 0);

        // Analyse : le mode est suggéré et le template TI est recommandé (correspondance exacte).
        JsonNode suggestion = post("/api/assistant/analyser", c,
                "{\"type\":\"APPEL_OFFRES_PUBLIC\",\"categorieAcquisition\":\"TECHNOLOGIES\",\"valeurEstimee\":300000}");
        assertEquals("APPEL_OFFRES_PUBLIC", suggestion.get("modeSuggere").asText());
        boolean tplRecommande = false;
        for (JsonNode r : suggestion.get("templatesRecommandes")) {
            if (codeTpl.equals(r.get("code").asText())) tplRecommande = true;
        }
        assertTrue(tplRecommande);
        assertTrue(suggestion.get("clausesEligibles").size() >= 1);

        // Génération guidée via template, rattachée à un dossier.
        long dossierId = post("/api/dossiers", c, "{\"titre\":\"Dossier assistant\"}").get("id").asLong();
        JsonNode viaTemplate = post("/api/assistant/generer", c,
                "{\"titre\":\"Doc guide template\",\"templateId\":" + tplId + ",\"dossierId\":" + dossierId + "}");
        assertEquals(dossierId, viaTemplate.get("dossierId").asLong());
        assertTrue(viaTemplate.get("clauses").size() >= 1);

        // Génération guidée manuelle (mode déduit de la valeur).
        JsonNode manuel = post("/api/assistant/generer", c,
                "{\"titre\":\"Doc guide manuel\",\"type\":\"GRE_A_GRE\",\"valeurEstimee\":5000,\"clauseIds\":[]}");
        assertEquals("GRE_A_GRE", manuel.get("modeSollicitation").asText());
    }

    @Test
    void recherche_enrichie_avecFacettesEtFiltres() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        String terme = "recherche" + System.currentTimeMillis();
        post("/api/clauses", c, "{\"code\":\"RX-1\",\"titre\":\"" + terme + " standard\","
                + "\"categorie\":\"STANDARD\",\"corps\":\"corps\"}");

        JsonNode res = get("/api/search?q=" + terme, c);
        assertTrue(res.get("total").asInt() >= 1);
        assertTrue(res.has("templates"));
        assertTrue(res.get("facettesCategorieClause").has("STANDARD"));

        // Filtre par catégorie qui exclut la clause standard → aucune clause.
        JsonNode filtre = get("/api/search?q=" + terme + "&categorie=OPTIONNELLE", c);
        assertEquals(0, filtre.get("clauses").size());

        // Filtre par type de document (facette documents/templates).
        assertNotNull(get("/api/search?q=" + terme + "&type=APPEL_OFFRES_PUBLIC", c).get("total"));

        // LEV-C-02 : thématique (section), clauses normatives, applicabilité au type de contrat.
        post("/api/clauses", c, "{\"code\":\"RX-2\",\"titre\":\"" + terme + " assurance\",\"categorie\":\"STANDARD\","
                + "\"corps\":\"corps\",\"section\":\"Assurances" + terme + "\",\"normative\":true,"
                + "\"conditions\":[{\"attribut\":\"TYPE_CONTRAT\",\"valeur\":\"APPEL_OFFRES_PUBLIC\"}]}");
        JsonNode parTheme = get("/api/search?q=" + terme + "&thematique=ASSURANCES" + terme, c);
        assertEquals(1, parTheme.get("clauses").size());
        assertEquals(1, parTheme.get("facettesThematique").get("Assurances" + terme).asInt());
        assertEquals(1, get("/api/search?q=" + terme + "&normative=true", c).get("clauses").size());
        assertEquals(2, get("/api/search?q=" + terme + "&type=APPEL_OFFRES_PUBLIC", c).get("clauses").size());
        assertEquals(1, get("/api/search?q=" + terme + "&type=GRE_A_GRE", c).get("clauses").size());
        assertTrue(get("/api/search/thematiques", c).toString().contains("Assurances" + terme));
    }

    // --- Workflow configurable + résolution commentaires + journal d'audit (P3) ---
    @Test
    void workflowConfigurable_piloteLeCircuitDApprobation() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);

        // Gabarit de circuit pour les contrats de gré à gré : APPRO -> DIRECTION (2 étapes).
        post("/api/workflows", c, "{\"nom\":\"Circuit gre a gre\",\"type\":\"GRE_A_GRE\","
                + "\"etapes\":[\"APPRO\",\"DIRECTION\"]}");

        long wfId = 0;
        for (JsonNode w : get("/api/workflows", c)) {
            if ("Circuit gre a gre".equals(w.get("nom").asText())) wfId = w.get("id").asLong();
        }
        assertTrue(wfId > 0);
        assertEquals("GRE_A_GRE", get("/api/workflows/" + wfId, c).get("type").asText());
        // Mise à jour du circuit (3 étapes) puis relecture.
        JsonNode maj = put("/api/workflows/" + wfId, c, "{\"nom\":\"Circuit gre a gre\",\"type\":\"GRE_A_GRE\","
                + "\"etapes\":[\"APPRO\",\"JURIDIQUE\",\"DIRECTION\"]}");
        assertEquals(3, maj.get("etapes").size());

        long id = post("/api/documents", c, "{\"titre\":\"GAG\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        JsonNode etapes = post("/api/documents/" + id + "/approbation", c, "{}");
        assertEquals(3, etapes.size(), "le circuit configuré prime sur le circuit par défaut");
        assertEquals("APPRO", etapes.get(0).get("roleRequis").asText());

        // Suppression du gabarit : on revient au circuit par défaut (3 étapes APPRO/JURIDIQUE/DIRECTION).
        mvc.perform(MockMvcRequestBuilders.delete("/api/workflows/" + wfId).cookie(c)).andExpect(status().isNoContent());
        long id2 = post("/api/documents", c, "{\"titre\":\"GAG2\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        assertEquals(3, post("/api/documents/" + id2 + "/approbation", c, "{}").size());
    }

    @Test
    void commentaires_filEtResolution() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Doc commente\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();

        long racine = post("/api/documents/" + id + "/commentaires", c, "{\"corps\":\"À revoir\"}").get("id").asLong();
        // Réponse dans le fil (parentId).
        post("/api/documents/" + id + "/commentaires", c, "{\"corps\":\"Corrigé\",\"parentId\":" + racine + "}");
        assertEquals(2, get("/api/documents/" + id + "/commentaires", c).size());

        // Résolution puis réouverture.
        JsonNode resolu = post("/api/documents/" + id + "/commentaires/" + racine + "/resolution", c, "{\"resolu\":true}");
        assertTrue(resolu.get("resolu").asBoolean());
        assertEquals("redacteur@levis.qc.ca", resolu.get("resoluPar").asText());
        JsonNode rouvert = post("/api/documents/" + id + "/commentaires/" + racine + "/resolution", c, "{\"resolu\":false}");
        assertFalse(rouvert.get("resolu").asBoolean());

        // Résolution d'un commentaire inexistant → 404.
        mvc.perform(MockMvcRequestBuilders.post("/api/documents/" + id + "/commentaires/999999/resolution")
                .cookie(c).contentType(MediaType.APPLICATION_JSON).content("{\"resolu\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void journalAudit_consultable_etExportable_adminSeulement() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        // Génère un événement d'audit.
        post("/api/documents", admin, "{\"titre\":\"Doc audit\",\"type\":\"GRE_A_GRE\"}");

        assertTrue(get("/api/audit?action=CREER", admin).size() >= 1);

        MvcResult export = mvc.perform(MockMvcRequestBuilders.get("/api/audit/export").cookie(admin)).andReturn();
        assertEquals(200, export.getResponse().getStatus());
        String csv = export.getResponse().getContentAsString();
        assertTrue(csv.startsWith("horodatage,utilisateur,action,entite,entiteId,details"));

        // Un rédacteur n'a pas accès au journal (refus par défaut).
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/audit").cookie(red)).andExpect(status().isForbidden());
    }

    // --- Publication externe M365/SAP (P4 - art. 9, ADR-002) ---
    @Test
    void publication_versCibleExterne_etHistorique() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c,
                "{\"titre\":\"Doc publie\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}")
                .get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        validerDocument(c, id); // la publication n'est permise qu'après validation

        // Publication PDF vers SharePoint (adaptateur de simulation) — dossier par défaut « Contrats ».
        JsonNode pub = post("/api/documents/" + id + "/publications", c,
                "{\"cible\":\"SHAREPOINT\",\"format\":\"PDF\"}");
        assertTrue(pub.get("succes").asBoolean());
        assertEquals("SHAREPOINT", pub.get("cible").asText());
        assertTrue(pub.get("urlCible").asText().startsWith("sim://sharepoint/Contrats/"));

        // Publication DOCX vers OneDrive avec un gabarit explicite et un répertoire de destination saisi.
        JsonNode pubDest = post("/api/documents/" + id + "/publications", c,
                "{\"cible\":\"ONEDRIVE\",\"format\":\"DOCX\",\"gabarit\":\"BORDEREAU\",\"destination\":\"/Contrats/2027/\"}");
        assertTrue(pubDest.get("urlCible").asText().startsWith("sim://onedrive/Contrats/2027/"));

        // Export avec paramètre gabarit vide (branche gabarit blanc) — même pipeline de rendu.
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf?gabarit=").cookie(c))
                .andExpect(status().isOk());

        // Historique : deux dépôts tracés.
        assertEquals(2, get("/api/documents/" + id + "/publications", c).size());
    }

    // --- Connecteur SAP + notification Teams (P5 - art. 9) ---
    @Test
    void referenceSap_dossier_etNotificationTeams() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);

        long dossierId = post("/api/dossiers", c,
                "{\"titre\":\"Dossier SAP\",\"numeroAO\":\"7000018100\"}").get("id").asLong();
        // Deux documents rattachés avec des valeurs estimées → montant agrégé.
        post("/api/documents", c, "{\"titre\":\"D1\",\"type\":\"GRE_A_GRE\",\"valeurEstimee\":100000,\"dossierId\":" + dossierId + "}");
        post("/api/documents", c, "{\"titre\":\"D2\",\"type\":\"GRE_A_GRE\",\"valeurEstimee\":50000,\"dossierId\":" + dossierId + "}");

        // Transmission de la référence financière vers SAP.
        JsonNode ref = post("/api/dossiers/" + dossierId + "/reference-sap", c, "{}");
        assertTrue(ref.get("succes").asBoolean());
        assertTrue(ref.get("url").asText().startsWith("sap://dossier/7000018100"));

        // Notification Teams : cible du même port de publication (art. 9).
        long docId = post("/api/documents", c, "{\"titre\":\"Doc Teams\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        validerDocument(c, docId); // validation préalable à la publication
        JsonNode pub = post("/api/documents/" + docId + "/publications", c, "{\"cible\":\"TEAMS\"}");
        assertTrue(pub.get("succes").asBoolean());
        assertEquals("TEAMS", pub.get("cible").asText());
    }

    // --- Import/migration + réversibilité + encadrement IA (P6 - art. 20, 21, 22) ---
    @Test
    void import_migrationEssai_puisReelle() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        String csv = "code,titre,categorie,corps\n"
                + "IMP-1,Objet du contrat,STANDARD,Le présent contrat...\n"
                + ",SansCode,STANDARD,invalide\n";

        // Migration d'essai : valide sans persister.
        JsonNode essai = json.readTree(mvc.perform(MockMvcRequestBuilders.post("/api/import/clauses?essai=true")
                .cookie(c).contentType("text/csv").content(csv))
                .andReturn().getResponse().getContentAsString());
        assertTrue(essai.get("essai").asBoolean());
        assertEquals(1, essai.get("valides").asInt());
        assertEquals(0, essai.get("importes").asInt());
        assertEquals(1, essai.get("erreurs").size());

        // Import réel : persiste la ligne valide.
        JsonNode reel = json.readTree(mvc.perform(MockMvcRequestBuilders.post("/api/import/clauses?essai=false")
                .cookie(c).contentType("text/csv").content(csv))
                .andReturn().getResponse().getContentAsString());
        assertEquals(1, reel.get("importes").asInt());

        // Un rédacteur n'a pas accès à l'import.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.post("/api/import/clauses?essai=true")
                .cookie(red).contentType("text/csv").content(csv)).andExpect(status().isForbidden());
    }

    @Test
    void reversibilite_exportZip_adminSeulement() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        var reponse = mvc.perform(MockMvcRequestBuilders.get("/api/reversibilite/export").cookie(admin))
                .andExpect(status().isOk()).andReturn().getResponse();
        assertTrue(reponse.getContentType().contains("application/zip"));
        assertTrue(reponse.getContentAsByteArray().length > 0);

        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/reversibilite/export").cookie(red))
                .andExpect(status().isForbidden());
    }

    @Test
    void ia_tracabilite_etDesactivation() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        try {
            // IA activée par défaut : le chat répond (mode hors ligne) et journalise la suggestion.
            assertNotNull(post("/api/ia/chat", c, "{\"question\":\"Quelle clause de garantie ?\"}").get("reponse"));
            // Rédaction : Ollama hors ligne en CI → 503 (chemin d'erreur couvert).
            mvc.perform(MockMvcRequestBuilders.post("/api/ia/rediger").cookie(c)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"instruction\":\"Rédige une clause\"}"))
                    .andExpect(status().isServiceUnavailable());
            assertTrue(get("/api/ia/suggestions", c).size() >= 1);
            assertTrue(get("/api/ia/parametres", c).get("activee").asBoolean());

            // Désactivation : le chat est refusé (art. 21).
            assertFalse(put("/api/ia/parametres", c, "{\"activee\":false}").get("activee").asBoolean());
            mvc.perform(MockMvcRequestBuilders.post("/api/ia/chat").cookie(c)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"test\"}"))
                    .andExpect(status().isConflict());
        } finally {
            put("/api/ia/parametres", c, "{\"activee\":true}"); // réactive pour les autres tests
        }
    }

    // --- Addenda 1 : import Word, rapports de suivi, journal d'accès ---
    private byte[] docxDemo() throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var titre = doc.createParagraph(); titre.setStyle("Heading1");
            titre.createRun().setText("Objet du contrat");
            doc.createParagraph().createRun().setText("Le présent contrat a pour objet la fourniture de services.");
            var garantie = doc.createParagraph(); garantie.setStyle("Heading1");
            garantie.createRun().setText("Garantie");
            doc.createParagraph().createRun().setText("Garantie de douze (12) mois.");
            doc.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void importWord_essai_puisReel_creeModeleEtClauses() throws Exception {
        Cookie c = login("admin@levis.qc.ca", PW);
        var fichier = new MockMultipartFile("fichier", "modele.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxDemo());

        // Migration d'essai : sections détectées, rien de persisté.
        JsonNode essai = json.readTree(mvc.perform(MockMvcRequestBuilders.multipart("/api/import/word")
                .file(fichier).param("essai", "true").cookie(c))
                .andReturn().getResponse().getContentAsString());
        assertTrue(essai.get("essai").asBoolean());
        assertEquals(2, essai.get("sectionsDetectees").asInt());

        // Import réel : un modèle + 2 clauses.
        JsonNode reel = json.readTree(mvc.perform(MockMvcRequestBuilders.multipart("/api/import/word")
                .file(fichier).param("essai", "false").param("nom", "Modele Ville").cookie(c))
                .andReturn().getResponse().getContentAsString());
        assertEquals(2, reel.get("clausesCreees").asInt());
        assertTrue(reel.get("templateId").asLong() > 0);

        // Un rédacteur n'a pas accès à l'import.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.multipart("/api/import/word").file(fichier)
                .param("essai", "true").cookie(red)).andExpect(status().isForbidden());
    }

    @Test
    void rapportSuivi_agrege_etRbac() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        post("/api/documents", admin, "{\"titre\":\"Doc rapport\",\"type\":\"GRE_A_GRE\"}");

        JsonNode r = get("/api/rapports/suivi", admin);
        assertTrue(r.get("totalDocuments").asInt() >= 1);
        assertTrue(r.has("documentsParStatut"));
        assertTrue(r.has("activiteParUtilisateur"));

        // Un profil juridique (sans APPRO/DIRECTION/ADMIN) n'accède pas aux rapports de pilotage.
        Cookie juriste = login("juriste@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.get("/api/rapports/suivi").cookie(juriste))
                .andExpect(status().isForbidden());
    }

    @Test
    void journalAcces_echecConnexion_etDeconnexion_sontTraces() throws Exception {
        // Échec de connexion (mauvais mot de passe).
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized());
        // Connexion puis déconnexion.
        Cookie c = login("admin@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/logout").cookie(c)).andExpect(status().isNoContent());

        // Le jeton de la session fermée est révoqué ; une nouvelle session lit le journal (échec et déconnexion).
        Cookie lecture = login("admin@levis.qc.ca", PW);
        assertTrue(get("/api/audit?action=CONNEXION_ECHEC", lecture).size() >= 1);
        assertTrue(get("/api/audit?action=DECONNEXION", lecture).size() >= 1);
    }

    @Test
    void ef2_dependancesEtExclusions() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        post("/api/clauses", c, "{\"code\":\"IT-A\",\"titre\":\"A\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"a\",\"exclusions\":[\"IT-B\"]}");
        post("/api/clauses", c, "{\"code\":\"IT-B\",\"titre\":\"B\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"b\",\"exclusions\":[\"IT-A\"]}");
        // Récupère les ids réels de IT-A / IT-B
        long aid = 0, bid = 0;
        for (JsonNode cl : get("/api/clauses", c)) {
            if (cl.get("code").asText().equals("IT-A")) aid = cl.get("id").asLong();
            if (cl.get("code").asText().equals("IT-B")) bid = cl.get("id").asLong();
        }
        JsonNode doc = post("/api/documents", c, "{\"titre\":\"EF2\",\"type\":\"GRE_A_GRE\",\"modeSollicitation\":\"GRE_A_GRE\"}");
        JsonNode ass = post("/api/documents/" + doc.get("id").asLong() + "/assembler", c,
                "{\"clauseIds\":[" + aid + "," + bid + "]}");
        long nbAB = 0;
        for (JsonNode dc : ass.get("clauses")) {
            if (dc.get("code").asText().startsWith("IT-")) nbAB++;
        }
        assertEquals(1, nbAB, "Deux clauses mutuellement exclusives ne doivent pas coexister");
    }

    // --- Addendas, versions, obligations, signature ---
    @Test
    void addenda_version_obligation_signature() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Full\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");

        long v1 = post("/api/documents/" + id + "/versions", c, "{\"note\":\"v1\"}").get("id").asLong();
        long v2 = post("/api/documents/" + id + "/versions", c, "{\"note\":\"v2\"}").get("id").asLong();
        assertTrue(get("/api/versions/comparer?a=" + v1 + "&b=" + v2, c).size() >= 1);

        long aid = post("/api/documents/" + id + "/addendas", c, "{\"objet\":\"Report\"}").get("id").asLong();
        post("/api/addendas/" + aid + "/reintegrer", c, "{}");

        post("/api/documents/" + id + "/obligations", c, "{\"titre\":\"Garantie\",\"type\":\"GARANTIE\",\"dateEcheance\":\"2026-08-01\"}");
        assertTrue(get("/api/documents/" + id + "/obligations", c).size() >= 1);
        assertTrue(get("/api/obligations/a-venir?jours=3650", c).size() >= 1);

        String jeton = post("/api/documents/" + id + "/signataires", c, "{\"nom\":\"J\",\"courriel\":\"j@x.ca\",\"role\":\"C\"}").get("jeton").asText();
        post("/api/documents/" + id + "/signature/demarrer", c, "{}");
        // Signature externe par jeton (endpoint public) avec image.
        JsonNode signe = post("/api/signature/" + jeton + "/signer", new Cookie("x", ""),
                "{\"nomComplet\":\"Jean\",\"signatureImage\":\"data:image/png;base64,AAAA\"}");
        assertEquals("SIGNE", signe.get("statut").asText());
    }

    // --- Templates, recherche, stats, IA ---
    @Test
    void templates_search_stats_ia() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        assertTrue(get("/api/templates", c).size() >= 1);
        long tid = get("/api/templates", c).get(0).get("id").asLong();
        JsonNode inst = post("/api/templates/" + tid + "/instancier", c, "{\"titre\":\"Depuis template\"}");
        assertTrue(inst.get("id").asLong() > 0);

        assertTrue(get("/api/search?q=garantie", c).get("total").asInt() >= 0);
        assertTrue(get("/api/stats", c).has("nbDocuments"));

        post("/api/ia/knowledge", c, "{\"titre\":\"Note\",\"source\":\"LCV\",\"contenu\":\"contenu de reference\"}");
        assertTrue(get("/api/ia/knowledge", c).size() >= 1);
        // Chat résilient : répond même sans Ollama (200 + repli RAG).
        mvc.perform(MockMvcRequestBuilders.post("/api/ia/chat").cookie(c)
                .contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"reference ?\"}"))
                .andExpect(status().isOk());
    }

    // --- Multi-tenant, superadmin, gestion des utilisateurs, isolation ---
    @Test
    void superadmin_tenant_admin_isolation() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        String slug = "it" + System.nanoTime();
        long tid = post("/api/superadmin/tenants", sa,
                "{\"slug\":\"" + slug + "\",\"titre\":\"IT Ville\"}").get("id").asLong();
        String adminEmail = "admin-" + slug + "@x.ca";
        post("/api/superadmin/tenants/" + tid + "/admin", sa,
                "{\"email\":\"" + adminEmail + "\",\"nom\":\"Admin IT\",\"motDePasse\":\"" + PW + "\"}");

        // Public : titre de connexion accessible sans session.
        assertEquals("IT Ville", get("/api/tenants/" + slug + "/public", null).get("titre").asText());

        // L'admin du nouveau tenant ne voit aucun document de Lévis (isolation).
        Cookie adminIt = login(adminEmail, PW);
        long docIt = post("/api/documents", adminIt, "{\"titre\":\"DOC-IT\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        assertEquals(1, get("/api/documents", adminIt).size());

        Cookie redacteurLevis = login("redacteur@levis.qc.ca", PW);
        boolean voitDocIt = false;
        for (JsonNode d : get("/api/documents", redacteurLevis)) {
            if (d.get("id").asLong() == docIt) voitDocIt = true;
        }
        assertFalse(voitDocIt, "Un tenant ne doit pas voir les documents d'un autre");
    }

    @Test
    void admin_gestionUtilisateurs_superadminMasque() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        JsonNode page = get("/api/admin/users?q=&page=0&taille=20", admin);
        assertTrue(page.get("total").asInt() >= 1);
        for (JsonNode u : page.get("contenu")) {
            assertNotEquals("superadmin@plateforme.ca", u.get("email").asText());
        }
        assertTrue(get("/api/admin/users/roles", admin).size() >= 1);

        // CRUD utilisateur.
        long uid = post("/api/admin/users", admin,
                "{\"email\":\"nouveau@levis.qc.ca\",\"nom\":\"Nouveau\",\"roles\":[\"LECTEUR\"],\"actif\":true}").get("id").asLong();
        mvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + uid).cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nouveau@levis.qc.ca\",\"nom\":\"Modifié\",\"roles\":[\"REDACTEUR\"],\"actif\":false}"))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/" + uid).cookie(admin))
                .andExpect(status().isOk());
    }

    // --- Calcul TOTP de référence ---
    private String totp(String base32) throws Exception {
        byte[] key = decodeBase32(base32);
        long compteur = System.currentTimeMillis() / 1000L / 30L;
        byte[] data = java.nio.ByteBuffer.allocate(8).putLong(compteur).array();
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
        byte[] h = mac.doFinal(data);
        int o = h[h.length - 1] & 0xF;
        int bin = ((h[o] & 0x7F) << 24) | ((h[o + 1] & 0xFF) << 16)
                | ((h[o + 2] & 0xFF) << 8) | (h[o + 3] & 0xFF);
        return String.format("%06d", bin % 1_000_000);
    }

    private byte[] decodeBase32(String s) {
        String base32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(s.length() * 5 / 8);
        int bits = 0, value = 0;
        for (char ch : s.toCharArray()) {
            value = (value << 5) | base32.indexOf(ch);
            bits += 5;
            if (bits >= 8) { buf.put((byte) ((value >>> (bits - 8)) & 0xFF)); bits -= 8; }
        }
        byte[] out = new byte[buf.position()];
        buf.rewind(); buf.get(out);
        return out;
    }
}
