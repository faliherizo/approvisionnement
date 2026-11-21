package qc.levis.contrat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.soumission.domain.Bordereau;
import qc.levis.contrat.tenant.domain.Tenant;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Couverture complémentaire : MFA setup, profil, clauses (PUT/GET), addenda,
 *  obligations, signature, templates, tenants — parcours non couverts ailleurs. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiCoverageTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    private static final String PW = "Passw0rd!";

    private Cookie login(String email, String pw) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + pw + "\"}"))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private JsonNode post(String url, Cookie c, String body) throws Exception {
        var req = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(body);
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

    /** Crée un utilisateur dédié (via admin) pour ne pas perturber les autres tests. */
    private Cookie utilisateurDedie(String email) throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        post("/api/admin/users", admin,
                "{\"email\":\"" + email + "\",\"nom\":\"T\",\"motDePasse\":\"" + PW + "\",\"roles\":[\"REDACTEUR\",\"ADMIN\"],\"actif\":true}");
        return login(email, PW);
    }

    @Test
    void mfa_totpSetupEnableDisable_etQuestionSetup() throws Exception {
        Cookie u = utilisateurDedie("mfa1@levis.qc.ca");
        JsonNode setup = post("/api/auth/mfa/totp/setup", u, "{}");
        assertTrue(setup.has("secret"));
        String code = totp(setup.get("secret").asText());
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/mfa/totp/enable").cookie(u)
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/mfa/disable").cookie(u)).andExpect(status().isNoContent());

        Cookie u2 = utilisateurDedie("mfa2@levis.qc.ca");
        mvc.perform(MockMvcRequestBuilders.post("/api/auth/mfa/question/setup").cookie(u2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questions\":[{\"question\":\"Q\",\"reponse\":\"r\"}]}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void profil_getEtChangementMotDePasse() throws Exception {
        Cookie u = utilisateurDedie("pw@levis.qc.ca");
        assertEquals("pw@levis.qc.ca", get("/api/profil", u).get("email").asText());
        // Mauvais mot de passe actuel → 400.
        mvc.perform(MockMvcRequestBuilders.put("/api/profil").cookie(u)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motDePasseActuel\":\"faux\",\"nouveauMotDePasse\":\"NewPass1!\"}"))
                .andExpect(status().isBadRequest());
        // Bon mot de passe → 200.
        mvc.perform(MockMvcRequestBuilders.put("/api/profil").cookie(u)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"N\",\"motDePasseActuel\":\"" + PW + "\",\"nouveauMotDePasse\":\"NewPass1!\"}"))
                .andExpect(status().isOk());
        assertNotNull(login("pw@levis.qc.ca", "NewPass1!"));
    }

    @Test
    void clauses_getParId_etModification() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/clauses", c, "{\"code\":\"COV-1\",\"titre\":\"T\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"x\"}").get("id").asLong();
        assertEquals("COV-1", get("/api/clauses/" + id, c).get("code").asText());
        mvc.perform(MockMvcRequestBuilders.put("/api/clauses/" + id).cookie(c)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"COV-1\",\"titre\":\"T2\",\"categorie\":\"STANDARD\",\"corps\":\"y\",\"dependances\":[\"GEN-001\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void addenda_changement_reintegration_export_et_diffModifiee() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Add\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}").get("id").asLong();
        JsonNode ass = post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        long clauseId = ass.get("clauses").get(0).get("clauseId").asLong();

        long vAvant = post("/api/documents/" + id + "/versions", c, "{\"note\":\"avant\"}").get("id").asLong();

        long aid = post("/api/documents/" + id + "/addendas", c, "{\"objet\":\"O\"}").get("id").asLong();
        JsonNode ch = post("/api/addendas/" + aid + "/changements", c,
                "{\"clauseId\":" + clauseId + ",\"nouveauTexte\":\"Texte modifié par addenda\"}");
        assertTrue(ch.get("changements").size() >= 1);
        post("/api/addendas/" + aid + "/reintegrer", c, "{}"); // met à jour le texte + emisLe

        // Diff : une clause MODIFIEE entre avant/après réintégration.
        long vApres = post("/api/documents/" + id + "/versions", c, "{\"note\":\"apres\"}").get("id").asLong();
        boolean modifiee = false;
        for (JsonNode d : get("/api/versions/comparer?a=" + vAvant + "&b=" + vApres, c)) {
            if (d.get("changement").asText().equals("MODIFIEE")) modifiee = true;
        }
        assertTrue(modifiee, "Le texte modifié par l'addenda doit produire un diff MODIFIEE");

        // Export PDF/DOCX intégrant l'addenda émis (couvre le rendu des addendas).
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(c)).andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(c)).andExpect(status().isOk());
    }

    @Test
    void obligation_completer() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Obl\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        long oid = post("/api/documents/" + id + "/obligations", c,
                "{\"titre\":\"O\",\"type\":\"LIVRABLE\",\"dateEcheance\":\"2027-01-01\"}").get("id").asLong();
        JsonNode fait = post("/api/obligations/" + oid + "/completer", c, "{}");
        assertEquals("COMPLETE", fait.get("statut").asText());
    }

    @Test
    void signature_consultationPublique() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"Sig\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        String jeton = post("/api/documents/" + id + "/signataires", c, "{\"nom\":\"N\",\"courriel\":\"n@x.ca\",\"role\":\"R\"}").get("jeton").asText();
        assertEquals("EN_ATTENTE", get("/api/signature/" + jeton, null).get("statut").asText());
    }

    @Test
    void approbation_decisionsApprouvees() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c, "{\"titre\":\"App\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        JsonNode steps = post("/api/documents/" + id + "/approbation", c, "{}");
        for (JsonNode s : steps) {
            Cookie decideur = switch (s.get("roleRequis").asText()) {
                case "JURIDIQUE" -> login("juriste@levis.qc.ca", "Passw0rd!");
                case "DIRECTION" -> login("direction@levis.qc.ca", "Passw0rd!");
                default -> c;
            };
            post("/api/documents/approbation/" + s.get("id").asLong() + "/decision", decideur, "{\"approuve\":true,\"note\":\"ok\"}");
        }
        assertEquals("APPROUVE", get("/api/documents/" + id, c).get("statut").asText());
    }

    @Test
    void ia_rediger_etKnowledgeSupprimer() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        // /rediger : Ollama absent → 503 (le code du contrôleur est exercé).
        mvc.perform(MockMvcRequestBuilders.post("/api/ia/rediger").cookie(c)
                .contentType(MediaType.APPLICATION_JSON).content("{\"instruction\":\"Rédige une clause\"}"))
                .andExpect(status().isServiceUnavailable());
        long kid = post("/api/ia/knowledge", c, "{\"titre\":\"K\",\"contenu\":\"c\"}").get("id").asLong();
        mvc.perform(MockMvcRequestBuilders.delete("/api/ia/knowledge/" + kid).cookie(c)).andExpect(status().isOk());
    }

    @Test
    void template_creation_et_tenant_miseAJour() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        JsonNode t = post("/api/templates", c,
                "{\"code\":\"TPL-COV\",\"nom\":\"Cov\",\"type\":\"GRE_A_GRE\",\"modeSollicitation\":\"GRE_A_GRE\",\"clauseCodes\":[\"GEN-001\"]}");
        assertEquals("TPL-COV", t.get("code").asText());

        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long tid = post("/api/superadmin/tenants", sa, "{\"slug\":\"cov" + System.nanoTime() + "\",\"titre\":\"C\"}").get("id").asLong();
        mvc.perform(MockMvcRequestBuilders.put("/api/superadmin/tenants/" + tid).cookie(sa)
                .contentType(MediaType.APPLICATION_JSON).content("{\"titre\":\"C2\",\"actif\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void reference_lecture_gestionGlobaleEtTenant() throws Exception {
        Cookie red = login("redacteur@levis.qc.ca", PW);
        // Lecture des options (seeds globaux).
        assertTrue(get("/api/reference/TYPE_CONTRAT", red).size() >= 5);

        // Superadmin : CRUD global.
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long gid = post("/api/superadmin/reference", sa,
                "{\"categorie\":\"TYPE_CONTRAT\",\"code\":\"COV_G\",\"libelle\":\"Global cov\",\"ordre\":50}").get("id").asLong();
        assertTrue(get("/api/superadmin/reference?categorie=TYPE_CONTRAT", sa).size() >= 1);
        mvc.perform(MockMvcRequestBuilders.put("/api/superadmin/reference/" + gid).cookie(sa)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categorie\":\"TYPE_CONTRAT\",\"code\":\"COV_G\",\"libelle\":\"MAJ\",\"actif\":true}"))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.delete("/api/superadmin/reference/" + gid).cookie(sa))
                .andExpect(status().isOk());

        // Admin : CRUD tenant.
        Cookie admin = login("admin@levis.qc.ca", PW);
        long tid = post("/api/admin/reference", admin,
                "{\"categorie\":\"TYPE_OBLIGATION\",\"code\":\"COV_T\",\"libelle\":\"Tenant cov\",\"ordre\":9}").get("id").asLong();
        assertTrue(get("/api/admin/reference?categorie=TYPE_OBLIGATION", admin).size() >= 1);
        mvc.perform(MockMvcRequestBuilders.put("/api/admin/reference/" + tid).cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categorie\":\"TYPE_OBLIGATION\",\"code\":\"COV_T\",\"libelle\":\"MAJ\",\"ordre\":3}"))
                .andExpect(status().isOk());
        // La valeur tenant apparaît dans les options fusionnées.
        boolean present = false;
        for (JsonNode o : get("/api/reference/TYPE_OBLIGATION", admin)) {
            if (o.get("code").asText().equals("COV_T")) present = true;
        }
        assertTrue(present);
        mvc.perform(MockMvcRequestBuilders.delete("/api/admin/reference/" + tid).cookie(admin))
                .andExpect(status().isOk());
    }

    @Test
    void export_contratFormel_avecPreambuleEtNumeros() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", c,
                "{\"titre\":\"Contrat formel\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\","
                + "\"valeurEstimee\":2000000,\"contexte\":\"travaux TI\",\"numeroAO\":\"CO2026-999\","
                + "\"organismePublic\":\"Ville de Lévis, 2535 ch. …\",\"prestataire\":\"Fournisseur Inc.\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[]}");
        // Rendu PDF + DOCX : page couverture, préambule (ENTRE/ET), TOC, articles numérotés.
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(c))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(c))
                .andExpect(status().isOk());
    }

    @Test
    void annexes_bordereau_normative_etExportStructure() throws Exception {
        Cookie c = login("redacteur@levis.qc.ca", PW);
        // Clause normative avec numéro + section (couvre le regroupement en sections).
        JsonNode clause = post("/api/clauses", c,
                "{\"code\":\"COV-NORM\",\"titre\":\"Attestation\",\"categorie\":\"OPTIONNELLE\",\"corps\":\"<p>Texte</p>\","
                + "\"numero\":\"10.06.01\",\"section\":\"Attestations\",\"normative\":true}");
        assertTrue(clause.get("normative").asBoolean());
        long clauseId = clause.get("id").asLong();

        long id = post("/api/documents", c,
                "{\"titre\":\"Structure\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\","
                + "\"contexte\":\"travaux\",\"numeroAO\":\"CO2026-STR\",\"organismePublic\":\"Ville de Lévis\",\"prestataire\":\"Fournisseur\"}")
                .get("id").asLong();
        post("/api/documents/" + id + "/assembler", c, "{\"clauseIds\":[" + clauseId + "]}");

        // Annexes : ajout, lecture, suppression.
        long annexeId = post("/api/documents/" + id + "/annexes", c,
                "{\"numero\":\"A\",\"titre\":\"Plan des travaux\",\"referenceClause\":\"10.06.01\",\"contenu\":\"<p>Détails</p>\"}")
                .get("id").asLong();
        long annexe2 = post("/api/documents/" + id + "/annexes", c,
                "{\"titre\":\"Sans numéro\"}").get("id").asLong();
        assertEquals(2, get("/api/documents/" + id + "/annexes", c).size());

        // Bordereau : enregistrement (PUT) puis lecture (GET) avec calcul du total.
        JsonNode bord = mvcPut("/api/documents/" + id + "/bordereau", c,
                "{\"titre\":\"Bordereau\",\"lignes\":["
                + "{\"description\":\"Analyse\",\"quantite\":10,\"taux\":150.50},"
                + "{\"description\":\"Développement\",\"quantite\":20,\"taux\":175}]}");
        assertEquals(2, bord.get("lignes").size());
        assertEquals(2, get("/api/documents/" + id + "/bordereau", c).get("lignes").size());
        // Remplacement du bordereau (couvre la branche de suppression des anciennes lignes).
        mvcPut("/api/documents/" + id + "/bordereau", c,
                "{\"titre\":\"Bordereau v2\",\"lignes\":[{\"description\":\"Forfait\",\"quantite\":1,\"taux\":50000}]}");

        // Export avec sections + annexes + bordereau (couvre les renderers PDF/DOCX complets).
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(c)).andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(c)).andExpect(status().isOk());

        // Suppression d'une annexe.
        mvc.perform(MockMvcRequestBuilders.delete("/api/documents/" + id + "/annexes/" + annexeId).cookie(c))
                .andExpect(status().isOk());
        assertEquals(1, get("/api/documents/" + id + "/annexes", c).size());
        assertNotEquals(annexeId, annexe2);
    }

    private JsonNode mvcPut(String url, Cookie c, String body) throws Exception {
        String s = mvc.perform(MockMvcRequestBuilders.put(url).cookie(c)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }

    @Test
    void clauses_conditionsDefinissablesParAdmin_rechercheEtSuppression() throws Exception {
        Cookie red = login("redacteur@levis.qc.ca", PW);
        // L'admin définit une clause STANDARD éligible uniquement en gré à gré.
        String code = "COND-" + System.nanoTime();
        long clauseId = post("/api/clauses", red,
                "{\"code\":\"" + code + "\",\"titre\":\"Clause gré à gré\",\"categorie\":\"STANDARD\",\"corps\":\"<p>x</p>\","
                + "\"conditions\":[{\"attribut\":\"TYPE_CONTRAT\",\"valeur\":\"GRE_A_GRE\"}]}").get("id").asLong();

        // Document GRE_A_GRE → la clause est éligible.
        long dGre = post("/api/documents", red, "{\"titre\":\"D1\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        assertTrue(contient(get("/api/documents/" + dGre + "/clauses-eligibles", red), clauseId),
                "La clause conditionnée doit être éligible pour un contrat gré à gré");

        // Document APPEL_OFFRES_PUBLIC → non éligible (condition non satisfaite).
        long dAo = post("/api/documents", red,
                "{\"titre\":\"D2\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}").get("id").asLong();
        assertFalse(contient(get("/api/documents/" + dAo + "/clauses-eligibles", red), clauseId),
                "La clause gré à gré ne doit pas être éligible pour un appel d'offres");

        // Recherche paginée par code.
        JsonNode recherche = get("/api/clauses/recherche?q=" + code + "&page=0&taille=5", red);
        assertEquals(1, recherche.get("contenu").size());
        assertEquals(code, recherche.get("contenu").get(0).get("code").asText());

        // Modification : la condition reste éditable (remplacement).
        mvc.perform(MockMvcRequestBuilders.put("/api/clauses/" + clauseId).cookie(red)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"titre\":\"MAJ\",\"categorie\":\"STANDARD\",\"corps\":\"<p>y</p>\","
                        + "\"conditions\":[{\"attribut\":\"MODE_SOLLICITATION\",\"valeur\":\"DEMANDE_PRIX\"}]}"))
                .andExpect(status().isOk());

        // Suppression (logique) réservée à l'admin.
        mvc.perform(MockMvcRequestBuilders.delete("/api/clauses/" + clauseId).cookie(red))
                .andExpect(status().isForbidden());
        Cookie admin = login("admin@levis.qc.ca", PW);
        mvc.perform(MockMvcRequestBuilders.delete("/api/clauses/" + clauseId).cookie(admin))
                .andExpect(status().isOk());
        assertEquals(0, get("/api/clauses/recherche?q=" + code, admin).get("contenu").size());
    }

    private boolean contient(JsonNode liste, long id) {
        for (JsonNode c : liste) {
            if (c.get("id").asLong() == id) return true;
        }
        return false;
    }

    @Test
    void clauses_disponibles_montrentToutEtSelectionForceInclusion() throws Exception {
        Cookie red = login("redacteur@levis.qc.ca", PW);
        // Clause OPTIONNELLE éligible uniquement en gré à gré.
        long clauseId = post("/api/clauses", red,
                "{\"code\":\"DISPO-" + System.nanoTime() + "\",\"titre\":\"Hors règle AO\",\"categorie\":\"OPTIONNELLE\","
                + "\"corps\":\"<p>x</p>\",\"conditions\":[{\"attribut\":\"TYPE_CONTRAT\",\"valeur\":\"GRE_A_GRE\"}]}")
                .get("id").asLong();

        // Document APPEL_OFFRES : la clause n'est PAS éligible…
        long id = post("/api/documents", red,
                "{\"titre\":\"Dispo\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\"}").get("id").asLong();
        assertFalse(contient(get("/api/documents/" + id + "/clauses-eligibles", red), clauseId));

        // …mais elle apparaît bien dans les clauses DISPONIBLES, marquée non éligible.
        boolean vue = false;
        for (JsonNode c : get("/api/documents/" + id + "/clauses-disponibles", red)) {
            if (c.get("id").asLong() == clauseId) {
                vue = true;
                assertFalse(c.get("eligible").asBoolean());
                assertFalse(c.get("autoInclus").asBoolean());
            }
        }
        assertTrue(vue, "Toutes les clauses actives doivent être proposées, même hors règle");

        // Sélectionnée explicitement → l'assemblage l'inclut malgré le moteur de règles.
        JsonNode assemble = post("/api/documents/" + id + "/assembler", red,
                "{\"clauseIds\":[" + clauseId + "]}");
        boolean incluse = false;
        for (JsonNode dc : assemble.get("clauses")) {
            if (dc.get("clauseId").asLong() == clauseId) incluse = true;
        }
        assertTrue(incluse, "La sélection manuelle prime : la clause doit être assemblée");
    }

    @Test
    void export_appliqueLeBrandingDuTenant_logoCouleurEtApercuInline() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long tid = -1;
        for (JsonNode t : get("/api/superadmin/tenants", sa)) {
            if (t.get("slug").asText().equals("levis")) tid = t.get("id").asLong();
        }
        assertTrue(tid > 0, "Le tenant par défaut « levis » doit exister");

        // PNG 1x1 (data URL) : exerce le rendu du logo image dans PDF et DOCX.
        String logo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        mvc.perform(MockMvcRequestBuilders.put("/api/superadmin/tenants/" + tid + "/theme").cookie(sa)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"couleurPrimaire\":\"#8A0B0B\",\"couleurAccent\":\"#333333\",\"couleurFond\":\"#ffffff\","
                        + "\"couleurTexte\":\"#111111\",\"logoTexte\":\"Ville de Lévis\",\"logoImage\":\"" + logo + "\"}"))
                .andExpect(status().isOk());

        // Un rédacteur du tenant exporte : le branding (couleur + logo) est appliqué.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", red,
                "{\"titre\":\"Contrat brandé\",\"type\":\"APPEL_OFFRES_PUBLIC\",\"modeSollicitation\":\"APPEL_OFFRES_PUBLIC\","
                + "\"numeroAO\":\"CO-BRAND\",\"organismePublic\":\"Ville de Lévis\",\"prestataire\":\"Fournisseur\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", red, "{\"clauseIds\":[]}");

        // Aperçu PDF : affiché en ligne (Content-Disposition: inline) pour validation.
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf/apercu").cookie(red))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
        // Téléchargements PDF/DOCX brandés.
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(red))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(red))
                .andExpect(status().isOk());
    }

    @Test
    void modeleDocument_definiParEntreprise_appliqueAuxDocumentsGeneres() throws Exception {
        Cookie admin = login("admin@levis.qc.ca", PW);
        String logo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

        // L'admin de l'entreprise définit SON modèle de document (police, couleurs, logo, pied, sans couverture).
        mvc.perform(MockMvcRequestBuilders.put("/api/modele-document").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"police\":\"TIMES\",\"couleurTitre\":\"#204060\",\"couleurAccent\":\"#8A0B0B\","
                        + "\"marque\":\"Ville de Lévis\",\"piedDePage\":\"Document confidentiel\","
                        + "\"afficherCouverture\":false,\"logoImage\":\"" + logo + "\"}"))
                .andExpect(status().isOk());

        JsonNode m = get("/api/modele-document", admin);
        assertEquals("TIMES", m.get("police").asText());
        assertFalse(m.get("afficherCouverture").asBoolean());
        assertEquals("Document confidentiel", m.get("piedDePage").asText());

        // Un rédacteur du même tenant exporte : le modèle de l'entreprise s'applique.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", red,
                "{\"titre\":\"Modèle entreprise\",\"type\":\"GRE_A_GRE\",\"organismePublic\":\"Ville de Lévis\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", red, "{\"clauseIds\":[]}");
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf/apercu").cookie(red))
                .andExpect(status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/docx").cookie(red))
                .andExpect(status().isOk());

        // Police invalide → repli sur HELVETICA (validation du service).
        mvc.perform(MockMvcRequestBuilders.put("/api/modele-document").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"police\":\"COMIC\"}"))
                .andExpect(status().isOk());
        assertEquals("HELVETICA", get("/api/modele-document", admin).get("police").asText());

        // Le superadmin lit le modèle global par défaut (contexte tenant nul).
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        assertTrue(get("/api/modele-document", sa).has("police"));
    }

    @Test
    void modeleDocument_superadminLeGereParTenant() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        long tid = -1;
        for (JsonNode t : get("/api/superadmin/tenants", sa)) {
            if (t.get("slug").asText().equals("levis")) tid = t.get("id").asLong();
        }
        assertTrue(tid > 0);

        // Le superadmin définit le modèle de document d'UN tenant précis.
        mvc.perform(MockMvcRequestBuilders.put("/api/superadmin/tenants/" + tid + "/modele-document").cookie(sa)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"police\":\"COURIER\",\"couleurTitre\":\"#123456\",\"marque\":\"Lévis\"}"))
                .andExpect(status().isOk());
        assertEquals("COURIER",
                get("/api/superadmin/tenants/" + tid + "/modele-document", sa).get("police").asText());

        // Ce modèle s'applique aux documents des utilisateurs de ce tenant.
        Cookie red = login("redacteur@levis.qc.ca", PW);
        long id = post("/api/documents", red, "{\"titre\":\"Par tenant\",\"type\":\"GRE_A_GRE\"}").get("id").asLong();
        post("/api/documents/" + id + "/assembler", red, "{\"clauseIds\":[]}");
        mvc.perform(MockMvcRequestBuilders.get("/api/documents/" + id + "/export/pdf").cookie(red))
                .andExpect(status().isOk());
    }

    @Test
    void document_suppressionLogique_masqueDesListesEtRecherche() throws Exception {
        Cookie red = login("redacteur@levis.qc.ca", PW);
        String terme = "ctx" + System.nanoTime();
        long id = post("/api/documents", red,
                "{\"titre\":\"À supprimer\",\"type\":\"GRE_A_GRE\",\"contexte\":\"" + terme + "\"}").get("id").asLong();

        // Présent dans la liste et la recherche.
        assertTrue(contient(get("/api/documents", red), id));
        assertTrue(contient(get("/api/search?q=" + terme, red).get("documents"), id));

        // Suppression logique.
        mvc.perform(MockMvcRequestBuilders.delete("/api/documents/" + id).cookie(red))
                .andExpect(status().isOk());

        // Absent de la liste et de la recherche.
        assertFalse(contient(get("/api/documents", red), id));
        assertFalse(contient(get("/api/search?q=" + terme, red).get("documents"), id));
    }

    // --- TOTP de référence ---
    private String totp(String base32) throws Exception {
        byte[] key = decodeBase32(base32);
        long ctr = System.currentTimeMillis() / 1000L / 30L;
        byte[] data = java.nio.ByteBuffer.allocate(8).putLong(ctr).array();
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
        byte[] h = mac.doFinal(data);
        int o = h[h.length - 1] & 0xF;
        int bin = ((h[o] & 0x7F) << 24) | ((h[o + 1] & 0xFF) << 16) | ((h[o + 2] & 0xFF) << 8) | (h[o + 3] & 0xFF);
        return String.format("%06d", bin % 1_000_000);
    }

    private byte[] decodeBase32(String s) {
        String b32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(s.length() * 5 / 8);
        int bits = 0, val = 0;
        for (char ch : s.toCharArray()) {
            val = (val << 5) | b32.indexOf(ch);
            bits += 5;
            if (bits >= 8) { buf.put((byte) ((val >>> (bits - 8)) & 0xFF)); bits -= 8; }
        }
        byte[] out = new byte[buf.position()];
        buf.rewind(); buf.get(out);
        return out;
    }
}
