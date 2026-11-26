package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
 * Annexe 10 de l'appel d'offres IQ (2026-11-25), organisation IQ, vraie pile : écarts des exigences obligatoires et
 * essentielles (RST-42 à RST-46).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-annexe10",
        // Connexion Microsoft 365 réelle injoignable : la panne de Graph se simule sans appel externe.
        "app.m365.login-base-url=http://127.0.0.1:9", "app.m365.graph-base-url=http://127.0.0.1:9"})
class AnnexeDixIntegrationTest {

    private static final String PW = "Passw0rd!";
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

    // --- RST-45 (TI-01) : SharePoint/OneDrive et Teams pour le module de signature ---------------------------------------

    @Test
    void une_trousse_terminee_est_deposee_dans_sharepoint_avec_sa_preuve_puis_annoncee_dans_teams() throws Exception {
        String url = "/api/clm/admin/integrations/m365";
        assertEquals(403, appel("GET", url, affaires, null).getStatus(), "administration technique seulement");
        assertTrue(corps(appel("PUT", url, technique, reglage(true, "SHAREPOINT", "../hors")), 400).get("message").asText().contains(".."));
        assertEquals(400, appel("PUT", url, technique, reglage(true, "DROPBOX", "Contrats")).getStatus(), "bibliothèque inconnue");
        try {
            assertEquals("Contrats signés", corps(appel("GET", url, technique, null), 200).get("dossier").asText(), "réglage par défaut ou précédent");
            JsonNode r = corps(appel("PUT", url, technique, reglage(true, "SHAREPOINT", "Contrats signés/IQ")), 200);
            assertEquals("admin.technique@iq.example", r.get("modifiePar").asText());
            assertEquals(200, appel("GET", "/api/parametres/integration-m365", technique, null).getStatus(),
                    "la connexion Microsoft 365 se règle aussi pour une organisation sans module de rédaction");

            long id = trousseTerminee("Dépôt SharePoint");
            String reference = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).at("/resume/reference").asText();
            List<JsonNode> depots = depotsDe(id);
            assertEquals(List.of("DOCUMENT_SIGNE", "PREUVE", "AVIS_TEAMS"), depots.stream().map(d -> d.get("nature").asText()).toList());
            assertTrue(depots.stream().allMatch(d -> "EN_ATTENTE".equals(d.get("statut").asText())));

            assertTrue(corps(appel("POST", url + "/relayer", technique, null), 200).get("deposes").asInt() >= 3);
            depots = depotsDe(id);
            assertTrue(depots.stream().allMatch(d -> "DEPOSE".equals(d.get("statut").asText())), depots.toString());
            String dossier = "Contrats signés/IQ/" + reference.replaceAll("[^A-Za-z0-9._-]", "_");
            assertEquals("sim://sharepoint/" + dossier + "/c.pdf", depots.get(0).get("url").asText());
            assertTrue(depots.get(1).get("url").asText().endsWith(".zip"), "dossier de preuve déposé à côté du document");
            assertTrue(depots.get(2).get("url").asText().startsWith("sim://teams/Contrats signés/IQ/avis-"), "avis dans le canal Teams");
            Path racine = Path.of(System.getProperty("java.io.tmpdir"), "vina-sim-annexe10");
            try (var fichiers = Files.walk(racine)) {
                assertTrue(fichiers.anyMatch(p -> p.endsWith("c.pdf") && p.toString().contains("SHAREPOINT")), "fichier réellement déposé");
            }
            String historique = corps(appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null), 200).toString();
            assertTrue(historique.contains("DEPOT_M365") && historique.contains("document signé → sim://sharepoint/"), historique);
            assertEquals(0, corps(appel("POST", url + "/relayer", technique, null), 200).get("deposes").asInt(), "jamais déposé deux fois");
        } finally {
            appel("PUT", url, technique, reglage(false, "SHAREPOINT", "Contrats signés"));
        }
    }

    @Test
    void un_depot_en_panne_reprend_et_se_relance_apres_reparation() throws Exception {
        String url = "/api/clm/admin/integrations/m365";
        String m365 = "/api/parametres/integration-m365";
        try {
            corps(appel("PUT", url, technique, reglage(true, "ONEDRIVE", "Signés")), 200);
            corps(appel("PUT", m365, technique, "{\"actif\":true,\"msTenantId\":\"iq\",\"clientId\":\"app\",\"clientSecret\":\"secret\","
                    + "\"sharepointSiteId\":\"site\",\"driveId\":\"drive\",\"teamsTeamId\":\"equipe\",\"teamsChannelId\":\"canal\"}"), 200);
            long id = trousseTerminee("Dépôt en panne");
            assertEquals(0, corps(appel("POST", url + "/relayer", technique, null), 200).get("deposes").asInt(), "Graph injoignable");
            JsonNode echec = depotsDe(id).get(0);
            assertEquals("EN_ATTENTE", echec.get("statut").asText(), "reprise planifiée");
            assertEquals(1, echec.get("tentatives").asInt());
            assertTrue(echec.get("derniereErreur").asText().startsWith("Échec Microsoft 365"), echec.toString());

            corps(appel("PUT", m365, technique, "{\"actif\":false}"), 200);
            for (JsonNode d : depotsDe(id)) {
                corps(appel("POST", url + "/depots/" + d.get("id").asLong() + "/relancer", technique, null), 200);
            }
            assertEquals(404, appel("POST", url + "/depots/999999/relancer", technique, null).getStatus());
            corps(appel("POST", url + "/relayer", technique, null), 200);
            JsonNode repris = depotsDe(id).get(0);
            assertEquals("DEPOSE", repris.get("statut").asText(), "repris par la simulation une fois Graph désactivé");
            assertTrue(repris.get("url").asText().startsWith("sim://onedrive/Signés/"));
            assertEquals(409, appel("POST", url + "/depots/" + repris.get("id").asLong() + "/relancer", technique, null).getStatus(),
                    "un dépôt effectué ne se relance pas");
        } finally {
            appel("PUT", m365, technique, "{\"actif\":false}");
            appel("PUT", url, technique, reglage(false, "SHAREPOINT", "Contrats signés"));
        }
    }

    // --- RST-43 (AF-34, SEC-15) : modification contrôlée des champs en cours et journal avant/après ---------------------

    @Test
    void en_traitement_seuls_les_champs_prevus_se_modifient_et_chaque_modification_est_journalisee_avant_apres() throws Exception {
        String champs = "[" + champ("montant", "Montant") + "," + champ("beneficiaire", "Bénéficiaire") + "," + champ("programme", "Programme")
                + "," + champ("conditions", "Conditions") + "," + champ("echeancier", "Échéancier") + "]";
        String parcours = "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,\"type\":\"VALIDATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]},"
                + "{\"cle\":\"a1\",\"libelle\":\"Approbation\",\"phase\":2,\"type\":\"APPROBATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}]}]";
        String regles = "[{\"champ\":\"montant\",\"effet\":\"NOUVEAU_CYCLE\"},"
                + "{\"champ\":\"beneficiaire\",\"etapes\":[\"v1\"],\"roleRequis\":\"REDACTEUR\",\"effet\":\"AUCUN\"},"
                + "{\"champ\":\"conditions\",\"effet\":\"REPRISE_ETAPE\"},{\"champ\":\"echeancier\",\"roleRequis\":\"DIRECTION\",\"effet\":\"AUCUN\"}]";
        String modele = "{\"nom\":\"Convention AF-34\",\"corps\":\"{{beneficiaire}} {{montant}}\",\"champs\":" + champs
                + ",\"clauses\":[],\"parcours\":" + parcours + ",\"modificationsEnCours\":";
        long id = corps(appel("POST", "/api/clm/modeles", initiateur, modele + "[{\"champ\":\"inconnu\"},{\"champ\":\"montant\",\"etapes\":[\"zz\"]}]}"), 200)
                .get("id").asLong();
        String erreurs = corps(appel("GET", "/api/clm/modeles/" + id + "/validation", initiateur, null), 200).get("erreurs").toString();
        assertTrue(erreurs.contains("champ inconnu « inconnu »") && erreurs.contains("étape inconnue « zz »"), erreurs);
        JsonNode vue = corps(appel("PUT", "/api/clm/modeles/" + id, initiateur, modele + regles + "}"), 200);
        assertEquals("NOUVEAU_CYCLE", vue.at("/modificationsEnCours/0/effet").asText());
        assertEquals("[]", corps(appel("GET", "/api/clm/modeles/" + id + "/validation", initiateur, null), 200).get("erreurs").toString());
        corps(appel("POST", "/api/clm/modeles/" + id + "/soumettre", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + id + "/publier", affaires, "{}"), 200);

        long t = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Convention modifiée\",\"modeleId\":" + id
                + ",\"risque\":\"FAIBLE\"}"), 200).get("resume").get("id").asLong();
        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"montant\":\"50000\",\"beneficiaire\":\"Agri-Nord\","
                + "\"programme\":\"Essor\",\"conditions\":\"Aucune\",\"echeancier\":\"Annuel\"}}"), 200);
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + t + "/documents")
                .file(new MockMultipartFile("fichier", "c.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
        corps(appel("PUT", "/api/clm/trousses/" + t + "/parcours", initiateur, parcours), 200);
        JsonNode lance = corps(appel("POST", "/api/clm/trousses/" + t + "/lancer", initiateur, "{}"), 200);
        long v1 = lance.at("/etapes/0/id").asLong();

        assertEquals("« Programme » n'est pas modifiable en cours de traitement.",
                corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"programme\":\"Autre\"}}"), 409).get("message").asText());
        assertEquals("« Échéancier » exige le rôle DIRECTION.",
                corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"echeancier\":\"Mensuel\"}}"), 409).get("message").asText());
        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"programme\":\"Essor\"}}"), 200);
        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"beneficiaire\":\"Agri-Nord inc.\"}}"), 200);
        assertEquals("ACTIVE", etape(t, 0), "effet « aucun » : la validation reste en cours");

        corps(appel("POST", "/api/clm/etapes/" + v1 + "/decider", validateur, "{\"decision\":\"APPROUVE\"}"), 200);
        assertEquals("ACTIVE", etape(t, 1));
        assertEquals("« Bénéficiaire » n'est modifiable qu'aux étapes v1.",
                corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"beneficiaire\":\"X\"}}"), 409).get("message").asText());
        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"conditions\":\"Garantie\"}}"), 200);
        assertEquals("APPROUVEE", etape(t, 0), "reprise de l'étape : la validation franchie reste acquise");
        assertEquals("ACTIVE", etape(t, 1));

        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"montant\":\"60000\"}}"), 200);
        assertEquals("ACTIVE", etape(t, 0), "nouveau cycle : la validation est à refaire");
        assertEquals("EN_ATTENTE", etape(t, 1));

        String historique = corps(appel("GET", "/api/clm/trousses/" + t + "/historique", initiateur, null), 200).toString();
        assertTrue(historique.contains("« Montant » (montant) : « 50000 » → « 60000 » · en traitement, étape(s) a1 · effet NOUVEAU_CYCLE"), historique);
        assertTrue(historique.contains("« Bénéficiaire » (beneficiaire) : « Agri-Nord » → « Agri-Nord inc. » · en traitement, étape(s) v1 · effet AUCUN"));
        assertTrue(historique.contains("« Montant » (montant) : «  » → « 50000 »"), "saisie en brouillon, sans contexte de parcours");
        assertTrue(historique.contains("REPRISE_ETAPE") && historique.contains("NOUVEAU_CYCLE"));
        assertFalse(historique.contains("« Programme » (programme) : « Essor » → « Essor »"), "une valeur inchangée n'est pas une modification");
        String journal = corps(appel("GET", "/api/clm/admin/journal?texte=60000&action=VALEUR_MODIFIEE", affaires, null), 200).toString();
        assertTrue(journal.contains("60000"), "le journal d'audit de l'organisation porte la preuve : " + journal);
    }

    // --- RST-44 (SEC-17) : accès de l'exploitant verrouillé, soutien accordé, limité et journalisé ------------------------

    @Test
    void le_superadministrateur_ne_lit_aucune_donnee_clm_d_iq() throws Exception {
        long id = trousseTerminee("Contenu protégé SEC-17");
        long document = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).at("/documents/0/id").asLong();
        Cookie sa = superadmin();
        String[] lectures = {"/api/clm/trousses", "/api/clm/trousses/" + id, "/api/clm/trousses/" + id + "/historique",
                "/api/clm/trousses/" + id + "/preuve", "/api/clm/trousses/" + id + "/preuve/export", "/api/clm/trousses/" + id + "/certificat",
                "/api/clm/trousses/" + id + "/documents/" + document + "/contenu", "/api/clm/suivi", "/api/clm/suivi/export",
                "/api/clm/tableau-de-bord", "/api/clm/taches", "/api/clm/modeles", "/api/clm/lots", "/api/clm/admin/journal",
                "/api/clm/admin/journal/export", "/api/clm/admin/utilisation?du=2026-01-01&au=2026-12-31", "/api/clm/admin/parametres", "/api/clm/admin/messages",
                "/api/clm/admin/integrations/salesforce", "/api/clm/admin/integrations/m365", "/api/clm/admin/conservation/regles",
                "/api/clm/admin/clients-integration", "/api/clm/admin/soutien", "/api/clm/groupes", "/api/clm/admin/rapports"};
        for (String url : lectures) {
            MockHttpServletResponse r = appel("GET", url, sa, null);
            assertTrue(r.getStatus() == 403 || r.getStatus() == 404, url + " → " + r.getStatus());
            assertFalse(r.getContentAsString(StandardCharsets.UTF_8).contains("Contenu protégé SEC-17"), url);
        }
        assertEquals(403, appel("POST", "/api/clm/trousses", sa, "{\"titre\":\"Intrusion\"}").getStatus());
        assertEquals(403, appel("GET", "/api/audit", sa, null).getStatus(), "ni le journal d'audit de l'organisation");
        assertEquals(403, appel("POST", "/api/clm/etapes/1/decider", sa, "{\"decision\":\"APPROUVE\"}").getStatus());
    }

    @Test
    void l_acces_de_soutien_est_refuse_sans_accord_puis_limite_journalise_et_visible_d_iq() throws Exception {
        long id = trousseTerminee("Incident de soutien");
        String reference = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).at("/resume/reference").asText();
        Cookie sa = superadmin();
        long iq = 0;
        for (JsonNode t : corps(appel("GET", "/api/superadmin/tenants", sa, null), 200)) {
            if ("iq".equals(t.get("slug").asText())) {
                iq = t.get("id").asLong();
            }
        }
        String base = "/api/superadmin/tenants/" + iq + "/soutien-clm";
        assertEquals(403, appel("GET", base + "/trousses/" + reference, sa, null).getStatus(), "aucun accès sans accord");
        assertEquals(404, appel("GET", "/api/superadmin/tenants/999999/soutien-clm/demandes", sa, null).getStatus());
        assertEquals(400, appel("POST", base + "/demandes", sa, "{\"motif\":\"\",\"dureeHeures\":2}").getStatus(), "motif requis");
        assertEquals(400, appel("POST", base + "/demandes", sa, "{\"motif\":\"Billet 42\",\"dureeHeures\":9}").getStatus(), "8 h au plus");
        long acces = corps(appel("POST", base + "/demandes", sa, "{\"motif\":\"Billet 42 : signature bloquée\",\"dureeHeures\":2}"), 201)
                .get("id").asLong();
        assertEquals(403, appel("GET", base + "/trousses/" + reference, sa, null).getStatus(), "demandé n'est pas accordé");
        assertEquals(403, appel("POST", "/api/clm/admin/soutien/" + acces + "/accorder", technique, null).getStatus(),
                "l'administration d'affaires décide");

        JsonNode accorde = corps(appel("POST", "/api/clm/admin/soutien/" + acces + "/accorder", affaires, null), 200);
        assertEquals("ACCORDE", accorde.get("statut").asText());
        assertEquals(409, appel("POST", "/api/clm/admin/soutien/" + acces + "/refuser", affaires, null).getStatus());
        String diagnostic = corps(appel("GET", base + "/trousses/" + reference, sa, null), 200).toString();
        assertTrue(diagnostic.contains(reference) && diagnostic.contains("\"journalIntegre\":true") && diagnostic.contains("TROUSSE_TERMINEE"),
                diagnostic);
        assertFalse(diagnostic.contains("Incident de soutien") || diagnostic.contains("validateur@iq.example") || diagnostic.contains("c.pdf"),
                "diagnostic sans contenu, ni titre, ni personne, ni document : " + diagnostic);
        assertEquals(404, appel("GET", base + "/trousses/TR-INCONNUE", sa, null).getStatus());

        assertEquals(1, corps(appel("GET", "/api/clm/admin/soutien", affaires, null), 200).findValues("consultations").stream()
                .filter(c -> c.asInt() == 1).count(), "consultation comptée et visible d'IQ");
        String historique = corps(appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null), 200).toString();
        assertTrue(historique.contains("CONSULTATION_SOUTIEN") && historique.contains("superadmin@plateforme.ca"), historique);
        String audit = corps(appel("GET", "/api/audit?action=CLM_ACCES_SOUTIEN", login("admin.ti@iq.example"), null), 200).toString();
        assertTrue(audit.contains("demande · 2 h") && audit.contains("accordé à superadmin@plateforme.ca") && audit.contains("consultation · " + reference)
                && audit.contains("consultation refusée"), audit);

        assertEquals("REVOQUE", corps(appel("POST", "/api/clm/admin/soutien/" + acces + "/revoquer", affaires, null), 200).get("statut").asText());
        assertEquals(403, appel("GET", base + "/trousses/" + reference, sa, null).getStatus(), "révoqué : plus d'accès");
        assertEquals("REVOQUE", corps(appel("GET", base + "/demandes", sa, null), 200).get(0).get("statut").asText());
        long refuse = corps(appel("POST", base + "/demandes", sa, "{\"motif\":\"Billet 43\"}"), 201).get("id").asLong();
        assertEquals("REFUSE", corps(appel("POST", "/api/clm/admin/soutien/" + refuse + "/refuser", affaires, null), 200).get("statut").asText());
        assertEquals(404, appel("POST", "/api/clm/admin/soutien/999999/accorder", affaires, null).getStatus());
    }

    // --- RST-42 (AF-37) : rapports personnalisables et exportables ------------------------------------------------------

    @Test
    void l_administration_cree_modifie_execute_et_exporte_un_rapport_de_delais_en_csv_et_en_excel() throws Exception {
        String base = "/api/clm/admin/rapports";
        long id = trousseTerminee("Délai AF-37");
        String reference = corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200).at("/resume/reference").asText();
        assertTrue(corps(appel("GET", base + "/types", affaires, null), 200).toString().contains("delaiMoyenHeures"));
        assertTrue(corps(appel("POST", base, affaires, "{\"nom\":\"\",\"type\":\"DELAIS\"}"), 400).get("message").asText().contains("Nom du rapport"));
        assertEquals(400, appel("POST", base, affaires, "{\"nom\":\"X\",\"type\":\"DELAIS\",\"colonnes\":[\"inconnue\"]}").getStatus());
        Cookie auditeur = login("auditeur@iq.example");
        assertEquals(403, appel("POST", base, auditeur, "{\"nom\":\"X\",\"type\":\"DELAIS\"}").getStatus(), "l'auditeur consulte, ne crée pas");

        long rapport = corps(appel("POST", base, affaires, "{\"nom\":\"Délais IQ\",\"type\":\"DELAIS\",\"periodeJours\":30,"
                + "\"colonnes\":[\"reference\",\"lanceeLe\",\"termineeLe\",\"delaiHeures\"]}"), 201).get("id").asLong();
        JsonNode resultat = corps(appel("GET", base + "/" + rapport + "/resultat", auditeur, null), 200);
        assertEquals("[\"Référence\",\"Lancée le\",\"Terminée le\",\"Délai (h)\"]", resultat.get("colonnes").toString());
        assertTrue(resultat.get("lignes").toString().contains(reference), resultat.toString());
        assertTrue(resultat.get("lignes").toString().contains("Moyenne ("), "délai moyen en synthèse : " + resultat);

        MockHttpServletResponse csv = appel("GET", base + "/" + rapport + "/export?format=csv", affaires, null);
        assertEquals(200, csv.getStatus());
        assertTrue(csv.getContentType().startsWith("text/csv"));
        String texte = csv.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(texte.startsWith("﻿Référence;Lancée le;Terminée le;Délai (h)") && texte.contains(reference), texte);
        MockHttpServletResponse xlsx = appel("GET", base + "/" + rapport + "/export?format=xlsx", affaires, null);
        assertTrue(xlsx.getHeader("Content-Disposition").contains("rapport-clm-" + rapport + ".xlsx"));
        try (var classeur = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsx.getContentAsByteArray()))) {
            assertEquals("Référence", classeur.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertTrue(classeur.getSheetAt(0).getLastRowNum() >= 2, "au moins une trousse et la moyenne");
        }
        assertEquals(400, appel("GET", base + "/" + rapport + "/export?format=pdf", affaires, null).getStatus());

        JsonNode modifie = corps(appel("PUT", base + "/" + rapport, affaires, "{\"nom\":\"Achevées IQ\",\"type\":\"COMPLETES\",\"periodeJours\":7}"), 200);
        assertEquals(5, modifie.get("colonnes").size(), "toutes les colonnes du type");
        assertTrue(corps(appel("GET", base + "/" + rapport + "/resultat", affaires, null), 200).toString().contains(reference));
        String journal = corps(appel("GET", "/api/audit?action=CLM_CONFIGURATION", login("admin.ti@iq.example"), null), 200).toString();
        assertTrue(journal.contains("créé : Délais IQ") && journal.contains("modifié : Achevées IQ") && journal.contains("exporté (xlsx)"), journal);

        for (String type : List.of("SIGNATURES_EN_ATTENTE", "DELAIS_ETAPES", "REJETS", "EXPIRATIONS", "UTILISATION", "VOLUMETRIE", "INCIDENTS")) {
            long r = corps(appel("POST", base, affaires, "{\"nom\":\"" + type + "\",\"type\":\"" + type + "\",\"periodeJours\":30,"
                    + "\"unite\":null,\"statut\":null}"), 201).get("id").asLong();
            JsonNode t = corps(appel("GET", base + "/" + r + "/resultat", affaires, null), 200);
            if (type.equals("DELAIS_ETAPES")) {
                assertTrue(t.get("lignes").toString().contains("Validation"), t.toString());
            }
            if (type.equals("VOLUMETRIE")) {
                assertTrue(t.get("lignes").size() >= 1, t.toString());
            }
            corps(appel("DELETE", base + "/" + r, affaires, null), 204);
        }
        long filtre = corps(appel("POST", base, affaires, "{\"nom\":\"Filtre\",\"type\":\"COMPLETES\",\"unite\":\"AUCUNE_UNITE\"}"), 201).get("id").asLong();
        assertEquals(0, corps(appel("GET", base + "/" + filtre + "/resultat", affaires, null), 200).get("lignes").size(), "filtre par unité");
        assertTrue(corps(appel("GET", base, auditeur, null), 200).toString().contains("Achevées IQ"));
        corps(appel("DELETE", base + "/" + rapport, affaires, null), 204);
        assertEquals(404, appel("GET", base + "/" + rapport + "/resultat", affaires, null).getStatus());
    }

    @Test
    void le_releve_d_utilisation_s_exporte_en_csv_et_en_excel() throws Exception {
        trousseTerminee("Relevé AF-37");
        String url = "/api/clm/admin/utilisation/export?du=2026-01-01&au=2099-12-31&format=";
        String csv = appel("GET", url + "csv", affaires, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("Indicateur;Valeur") && csv.contains("Enveloppes envoyées;") && csv.contains("Terminées;"), csv);
        MockHttpServletResponse xlsx = appel("GET", url + "xlsx", login("auditeur@iq.example"), null);
        assertEquals(200, xlsx.getStatus());
        try (var classeur = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(xlsx.getContentAsByteArray()))) {
            assertEquals("Enveloppes envoyées", classeur.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }
    }

    // --- RST-46 (TI-04) : journal de sécurité lu par le SIEM d'IQ, sans session -----------------------------------------

    @Test
    void un_client_siem_lit_le_journal_par_curseur_au_format_asim_et_cef_puis_est_revoque() throws Exception {
        Cookie ti = login("admin.ti@iq.example");
        assertEquals(400, appel("POST", "/api/clm/admin/clients-integration", ti, "{\"nom\":\"X\",\"systeme\":\"SPLUNK\"}").getStatus());
        JsonNode cree = corps(appel("POST", "/api/clm/admin/clients-integration", ti, "{\"nom\":\"Microsoft Sentinel\",\"systeme\":\"SIEM\"}"), 201);
        String clientId = cree.get("client").get("clientId").asText();
        String jeton = jeton(clientId, cree.get("secret").asText());
        assertEquals(401, api("GET", "/api/clm/integration/v1/journal", "faux.jeton").getStatus(), "jeton requis, aucune session");

        // Une modification de valeur : son détail (avant/après) ne doit jamais sortir vers le SIEM.
        String champs = "[" + champ("montant", "Montant") + "]";
        long modele = corps(appel("POST", "/api/clm/modeles", initiateur, "{\"nom\":\"Modèle SIEM\",\"corps\":\"{{montant}}\",\"champs\":" + champs
                + ",\"clauses\":[],\"parcours\":[]}"), 200).get("id").asLong();
        corps(appel("POST", "/api/clm/modeles/" + modele + "/soumettre", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + modele + "/publier", affaires, "{}"), 200);
        long t = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Trousse SIEM\",\"modeleId\":" + modele + "}"), 200)
                .get("resume").get("id").asLong();
        corps(appel("PUT", "/api/clm/trousses/" + t + "/valeurs", initiateur, "{\"valeurs\":{\"montant\":\"777123\"}}"), 200);
        appel("GET", "/api/clm/admin/journal", technique, null);

        List<JsonNode> tous = new ArrayList<>();
        long curseur = 0;
        JsonNode page;
        do {
            page = corps(api("GET", "/api/clm/integration/v1/journal?apres=" + curseur + "&limite=200", jeton), 200);
            page.get("evenements").forEach(tous::add);
            assertEquals(page.get("evenements").size(), page.get("cef").size());
            curseur = page.get("curseurSuivant").asLong();
        } while (page.get("suite").asBoolean());
        assertFalse(tous.isEmpty());
        for (int i = 1; i < tous.size(); i++) {
            assertTrue(tous.get(i).get("EventUid").asLong() > tous.get(i - 1).get("EventUid").asLong(), "ordre d'écriture, sans doublon");
        }
        JsonNode valeur = tous.stream().filter(e -> "CLM_VALEUR_MODIFIEE".equals(e.get("EventOriginalType").asText())).reduce((a, b) -> b).orElseThrow();
        assertEquals("Vina", valeur.get("EventVendor").asText());
        assertEquals("AuditEvent", valeur.get("EventSchema").asText());
        assertEquals("initiateur@iq.example", valeur.get("ActorUsername").asText());
        assertFalse(tous.toString().contains("777123"), "aucune valeur d'affaires vers le SIEM");
        assertTrue(tous.stream().anyMatch(e -> "CLM_ACCES_REFUSE".equals(e.get("EventOriginalType").asText())
                && "Failure".equals(e.get("EventResult").asText()) && "Medium".equals(e.get("EventSeverity").asText())), "refus d'accès signalés");
        JsonNode vide = corps(api("GET", "/api/clm/integration/v1/journal?apres=" + curseur + "&limite=0", jeton), 200);
        assertEquals(curseur, vide.get("curseurSuivant").asLong(), "rien de neuf : le curseur ne bouge pas");
        assertTrue(api("GET", "/api/clm/integration/v1/journal?apres=0&limite=1", jeton).getContentAsString(StandardCharsets.UTF_8)
                .contains("CEF:0|Vina|CLM|1.0|"));

        assertEquals(403, api("POST", "/api/clm/integration/v1/trousses", jeton).getStatus(), "un SIEM ne déclenche aucune signature");
        assertEquals(403, api("GET", "/api/clm/integration/v1/modeles", jeton).getStatus());
        JsonNode workday = corps(appel("POST", "/api/clm/admin/clients-integration", ti, "{\"nom\":\"Workday SIEM\",\"systeme\":\"WORKDAY\"}"), 201);
        assertEquals(403, api("GET", "/api/clm/integration/v1/journal", jeton(workday.get("client").get("clientId").asText(),
                workday.get("secret").asText())).getStatus(), "un système d'affaires ne lit pas le journal");

        corps(appel("POST", "/api/clm/admin/clients-integration/" + cree.get("client").get("id").asLong() + "/revoquer", ti, null), 200);
        assertEquals(401, api("GET", "/api/clm/integration/v1/journal", jeton).getStatus(), "révoqué : collecte coupée");
    }

    // --- Outils -------------------------------------------------------------------------------------------------------

    private String jeton(String clientId, String secret) throws Exception {
        return corps(mvc.perform(MockMvcRequestBuilders.post("/api/clm/integration/oauth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials").param("client_id", clientId).param("client_secret", secret)).andReturn().getResponse(), 200)
                .get("access_token").asText();
    }

    private MockHttpServletResponse api(String methode, String url, String jeton) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url).header("Authorization", "Bearer " + jeton)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn().getResponse();
    }

    private Cookie superadmin() throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"superadmin@plateforme.ca\",\"motDePasse\":\"SuperPassw0rd!\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private static String champ(String cle, String libelle) {
        return "{\"cle\":\"" + cle + "\",\"libelle\":\"" + libelle + "\",\"type\":\"TEXTE\",\"obligatoire\":false}";
    }

    private String etape(long trousse, int rang) throws Exception {
        return corps(appel("GET", "/api/clm/trousses/" + trousse, initiateur, null), 200).get("etapes").get(rang).get("statut").asText();
    }

    private static String reglage(boolean actif, String bibliotheque, String dossier) {
        return "{\"actif\":" + actif + ",\"bibliotheque\":\"" + bibliotheque + "\",\"dossier\":\"" + dossier
                + "\",\"deposerPreuve\":true,\"avisTeams\":true}";
    }

    private List<JsonNode> depotsDe(long trousse) throws Exception {
        List<JsonNode> res = new ArrayList<>();
        corps(appel("GET", "/api/clm/admin/integrations/m365/depots", technique, null), 200)
                .forEach(d -> { if (d.get("trousseId").asLong() == trousse) res.add(d); });
        res.sort(java.util.Comparator.comparingLong(d -> d.get("id").asLong()));
        return res;
    }

    /** Trousse d'un document, validée par le validateur : terminée, son document est verrouillé en version signée. */
    long trousseTerminee(String titre) throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"" + titre + "\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", "c.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
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
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url);
        if (qui != null) {
            r.cookie(qui);
        }
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

    static String b64(byte[] o) {
        return Base64.getEncoder().encodeToString(o);
    }
}
