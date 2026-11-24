package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
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

/**
 * Écarts de conformité comblés (matrice 02-Analyse/conformite_exigences_fonctionnelles.md), organisation IQ, vraie pile :
 * groupes du module (§ 1.4.7, § 1.4.12), signataire ajouté et remplacé en cours de parcours (§ 1.4.7, scénario 5),
 * document modifié en cours de parcours → nouveau cycle (scénario 5), retour à l'étape appropriée (scénario 9),
 * calendrier de conservation et gel légal (§ 1.4.11, § 1.4.12).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class ConformiteExigencesIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final byte[] PDF = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
            + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1);

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

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
    void les_groupes_du_module_ouvrent_les_modeles_partages_et_les_trousses_de_l_unite() throws Exception {
        long modele = corps(appel("POST", "/api/clm/modeles", initiateur, "{\"nom\":\"Modèle réservé\",\"groupes\":\"JURIDIQUE_IQ\","
                + "\"corps\":\"Texte\",\"champs\":[],\"clauses\":[],\"parcours\":[]}"), 200).get("id").asLong();
        corps(appel("POST", "/api/clm/modeles/" + modele + "/soumettre", initiateur, "{}"), 200);
        corps(appel("POST", "/api/clm/modeles/" + modele + "/publier", admin, "{}"), 200);
        long trousse = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"Unité\",\"unite\":\"FINANCEMENT_ENTREPRISES\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(404, appel("GET", "/api/clm/modeles/" + modele, validateur, null).getStatus(), "modèle partagé à un autre groupe");
        assertTrue(appel("GET", "/api/clm/trousses/" + trousse, validateur, null).getStatus() >= 403, "trousse d'une autre unité");

        JsonNode juridique = corps(appel("POST", "/api/clm/admin/groupes", admin, "{\"code\":\"juridique_iq\",\"libelle\":\"Juristes IQ\"}"), 201);
        assertEquals("JURIDIQUE_IQ", juridique.get("code").asText());
        assertFalse(juridique.get("unite").asBoolean());
        JsonNode unite = corps(appel("POST", "/api/clm/admin/groupes", admin,
                "{\"code\":\"FINANCEMENT_ENTREPRISES\",\"libelle\":\"Financement des entreprises\"}"), 201);
        assertTrue(unite.get("unite").asBoolean(), "le code est celui d'une unité de l'organisation");
        assertEquals(409, appel("POST", "/api/clm/admin/groupes", admin, "{\"code\":\"JURIDIQUE_IQ\",\"libelle\":\"Doublon\"}").getStatus());
        assertEquals(400, appel("POST", "/api/clm/admin/groupes/" + juridique.get("id").asLong() + "/membres", admin,
                "{\"courriel\":\"inconnu@ailleurs.example\"}").getStatus(), "seul un compte de l'organisation devient membre");
        corps(appel("POST", "/api/clm/admin/groupes/" + juridique.get("id").asLong() + "/membres", admin, "{\"courriel\":\"validateur@iq.example\"}"), 200);
        JsonNode membres = corps(appel("POST", "/api/clm/admin/groupes/" + unite.get("id").asLong() + "/membres", admin,
                "{\"courriel\":\"Validateur@IQ.example\"}"), 200);
        assertEquals("validateur@iq.example", membres.get("membres").get(0).asText());
        assertEquals(409, appel("POST", "/api/clm/admin/groupes/" + unite.get("id").asLong() + "/membres", admin,
                "{\"courriel\":\"validateur@iq.example\"}").getStatus());

        assertEquals(200, appel("GET", "/api/clm/modeles/" + modele, validateur, null).getStatus(), "membre du groupe de partage");
        assertEquals(200, appel("GET", "/api/clm/trousses/" + trousse, validateur, null).getStatus(), "membre du groupe de l'unité");
        assertTrue(corps(appel("GET", "/api/clm/groupes", initiateur, null), 200).size() >= 2, "liste lisible des éditeurs");
        assertEquals(403, appel("POST", "/api/clm/admin/groupes", initiateur, "{\"code\":\"X\",\"libelle\":\"X\"}").getStatus());

        corps(appel("DELETE", "/api/clm/admin/groupes/" + unite.get("id").asLong() + "/membres/validateur@iq.example", admin, null), 200);
        assertTrue(appel("GET", "/api/clm/trousses/" + trousse, validateur, null).getStatus() >= 403, "accès retiré avec l'appartenance");
        assertEquals(404, appel("DELETE", "/api/clm/admin/groupes/" + unite.get("id").asLong() + "/membres/validateur@iq.example", admin, null).getStatus());
        assertEquals(204, appel("DELETE", "/api/clm/admin/groupes/" + juridique.get("id").asLong(), admin, null).getStatus());
        assertEquals(404, appel("GET", "/api/clm/modeles/" + modele, validateur, null).getStatus(), "groupe supprimé : partage retiré");
        assertEquals(204, appel("DELETE", "/api/clm/admin/groupes/" + unite.get("id").asLong(), admin, null).getStatus());
    }

    @Test
    void un_signataire_s_ajoute_et_se_remplace_en_cours_de_parcours_sans_reprise() throws Exception {
        long id = trousseAvecDocument("Signataire remplacé");
        long premier = participant(id, "Alice Externe", "alice@exemple.ca", "SIGNATAIRE_1");
        definirParcours(id, "[{\"cle\":\"s1\",\"libelle\":\"Signature\",\"phase\":1,\"type\":\"SIGNATURE\","
                + "\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"" + premier + "\"}]}]");
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);

        long second = participant(id, "Bruno Remplaçant", "bruno@exemple.ca", "SIGNATAIRE_2");
        long etape = detail(id).get("etapes").get(0).get("id").asLong();
        corps(appel("PUT", "/api/clm/etapes/" + etape + "/affectations", initiateur, "{\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\""
                + second + "\"}],\"motif\":\"Signataire absent\"}"), 200);

        JsonNode d = detail(id);
        assertEquals("EN_TRAITEMENT", d.get("resume").get("statut").asText(), "aucune reprise du processus");
        String invitations = d.get("invitations").toString();
        assertTrue(invitations.contains("ANNULEE") && invitations.contains("ENVOYEE"), invitations);
        String journal = appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(journal.contains("ajouté en cours de parcours"), journal);
        assertTrue(journal.contains("REASSIGNATION"), journal);
        assertEquals(409, appel("DELETE", "/api/clm/trousses/" + id + "/participants/" + second, initiateur, null).getStatus(),
                "un signataire affecté ne se retire pas");
    }

    @Test
    void un_document_modifie_en_cours_ouvre_un_nouveau_cycle_et_un_retour_vise_l_etape_appropriee() throws Exception {
        long id = trousseAvecDocument("Cycle et retour");
        definirParcours(id, "[{\"cle\":\"v1\",\"libelle\":\"Validation\",\"phase\":1,\"type\":\"VALIDATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"validateur@iq.example\"}]},"
                + "{\"cle\":\"a1\",\"libelle\":\"Approbation\",\"phase\":2,\"type\":\"APPROBATION\","
                + "\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"approbateur@iq.example\"}]}]");
        corps(appel("POST", "/api/clm/trousses/" + id + "/lancer", initiateur, "{}"), 200);
        long v1 = detail(id).get("etapes").get(0).get("id").asLong();
        long a1 = detail(id).get("etapes").get(1).get("id").asLong();
        decider(v1, validateur, "{\"decision\":\"APPROUVE\"}", 200);

        assertEquals(400, decider(a1, approbateur, "{\"decision\":\"RETOUR\",\"commentaire\":\"x\",\"retourVers\":\"a1\"}", 400).getStatus(),
                "le retour vise une étape antérieure");
        assertEquals(400, decider(a1, approbateur, "{\"decision\":\"RETOUR\",\"commentaire\":\"x\",\"retourVers\":\"inconnue\"}", 400).getStatus());
        decider(a1, approbateur, "{\"decision\":\"RETOUR\",\"commentaire\":\"Clause 4 à revalider\",\"retourVers\":\"v1\"}", 200);
        JsonNode apresRetour = detail(id);
        assertEquals("EN_TRAITEMENT", apresRetour.get("resume").get("statut").asText(), "pas de retour au brouillon");
        assertEquals("ACTIVE", apresRetour.get("etapes").get(0).get("statut").asText(), "l'étape visée reprend");
        assertEquals("EN_ATTENTE", apresRetour.get("etapes").get(1).get("statut").asText());
        decider(v1, validateur, "{\"decision\":\"APPROUVE\"}", 200);
        assertEquals("ACTIVE", detail(id).get("etapes").get(1).get("statut").asText());

        String cle = detail(id).get("documents").get(0).get("cleDocument").asText();
        MockMultipartFile v2 = new MockMultipartFile("fichier", "contrat-v2.pdf", "application/pdf", PDF);
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents/" + cle + "/versions")
                .file(v2).param("motif", "Montant corrigé").cookie(initiateur)).andReturn().getResponse().getStatus());
        JsonNode apresVersion = detail(id);
        assertEquals("ACTIVE", apresVersion.get("etapes").get(0).get("statut").asText(), "nouveau cycle depuis la première phase");
        assertEquals("EN_ATTENTE", apresVersion.get("etapes").get(1).get("statut").asText());
        decider(v1, validateur, "{\"decision\":\"APPROUVE\"}", 200);
        String journal = appel("GET", "/api/clm/trousses/" + id + "/historique", initiateur, null).getContentAsString(StandardCharsets.UTF_8);
        assertTrue(journal.contains("RETOUR_ETAPE") && journal.contains("NOUVEAU_CYCLE") && journal.contains("Montant corrigé"), journal);
    }

    @Test
    void le_calendrier_de_conservation_s_applique_aux_trousses_closes_et_le_gel_legal_suspend_la_destruction() throws Exception {
        // Les règles de démonstration (data/clm-demo.json) peuvent exister : le décompte se fait par rapport à l'existant.
        java.util.Set<String> avant = new java.util.HashSet<>();
        corps(appel("GET", "/api/clm/admin/conservation/regles", admin, null), 200)
                .forEach(r -> avant.add(r.path("typeContrat").isNull() ? "" : r.path("typeContrat").asText()));
        corps(appel("PUT", "/api/clm/admin/conservation/regles", admin, "{\"dureeAns\":7,\"sortFinal\":\"DETRUIRE\"}"), 200);
        JsonNode garantie = corps(appel("PUT", "/api/clm/admin/conservation/regles", admin,
                "{\"typeContrat\":\"GARANTIE\",\"dureeAns\":10,\"sortFinal\":\"VERSER\"}"), 200);
        assertEquals(10, corps(appel("PUT", "/api/clm/admin/conservation/regles", admin,
                "{\"typeContrat\":\"GARANTIE\",\"dureeAns\":10,\"sortFinal\":\"verser\"}"), 200).get("dureeAns").asInt(), "remplace, ne duplique pas");
        assertEquals(400, appel("PUT", "/api/clm/admin/conservation/regles", admin, "{\"dureeAns\":0,\"sortFinal\":\"DETRUIRE\"}").getStatus());
        assertEquals(400, appel("PUT", "/api/clm/admin/conservation/regles", admin, "{\"dureeAns\":5,\"sortFinal\":\"BRULER\"}").getStatus());
        avant.add("");
        avant.add("GARANTIE");
        assertEquals(avant.size(), corps(appel("GET", "/api/clm/admin/conservation/regles", admin, null), 200).size(),
                "règle par défaut et règle GARANTIE remplacées ou ajoutées, sans doublon");

        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"À conserver\"}"), 200).get("resume").get("id").asLong();
        corps(appel("POST", "/api/clm/trousses/" + id + "/annuler", initiateur, "{\"motif\":\"Abandon\"}"), 200);
        JsonNode suivi = ligne(corps(appel("GET", "/api/clm/admin/conservation/trousses", admin, null), 200), id);
        assertEquals("DETRUIRE", suivi.get("sortFinal").asText(), "règle par défaut");
        assertFalse(suivi.get("echue").asBoolean());
        assertTrue(suivi.get("echeance").asText().startsWith(String.valueOf(java.time.LocalDate.now().getYear() + 7)));
        assertEquals(409, appel("POST", "/api/clm/admin/conservation/trousses/" + id + "/detruire", admin, "{}").getStatus(), "échéance non atteinte");
        assertEquals(409, appel("POST", "/api/clm/admin/conservation/trousses/" + id + "/versee", admin, "{}").getStatus());
        assertEquals(0, corps(appel("GET", "/api/clm/admin/conservation/trousses?echues=true", admin, null), 200).size());

        assertEquals(400, appel("POST", "/api/clm/admin/conservation/trousses/" + id + "/gel", admin, "{\"motif\":\" \"}").getStatus());
        assertTrue(corps(appel("POST", "/api/clm/admin/conservation/trousses/" + id + "/gel", admin, "{\"motif\":\"Litige 2026-17\"}"), 200)
                .get("gelLegal").asBoolean());
        assertFalse(corps(appel("DELETE", "/api/clm/admin/conservation/trousses/" + id + "/gel", admin, null), 200).get("gelLegal").asBoolean());
        assertEquals(409, appel("DELETE", "/api/clm/admin/conservation/trousses/" + id + "/gel", admin, null).getStatus(), "aucun gel à lever");
        assertEquals(403, appel("GET", "/api/clm/admin/conservation/regles", initiateur, null).getStatus());
        assertEquals(204, appel("DELETE", "/api/clm/admin/conservation/regles/" + garantie.get("id").asLong(), admin, null).getStatus());
    }

    private static JsonNode ligne(JsonNode liste, long trousseId) {
        for (JsonNode n : liste) {
            if (n.get("trousseId").asLong() == trousseId) {
                return n;
            }
        }
        throw new AssertionError("Trousse absente du suivi de conservation : " + trousseId);
    }

    private long trousseAvecDocument(String titre) throws Exception {
        long id = corps(appel("POST", "/api/clm/trousses", initiateur, "{\"titre\":\"" + titre + "\",\"risque\":\"FAIBLE\"}"), 200)
                .get("resume").get("id").asLong();
        assertEquals(200, mvc.perform(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", "contrat.pdf", "application/pdf", PDF)).cookie(initiateur)).andReturn().getResponse().getStatus());
        return id;
    }

    private long participant(long trousse, String nom, String courriel, String role) throws Exception {
        return corps(appel("POST", "/api/clm/trousses/" + trousse + "/participants", initiateur, "{\"nom\":\"" + nom + "\",\"courriel\":\""
                + courriel + "\",\"roleContrat\":\"" + role + "\",\"externe\":true}"), 200).get("id").asLong();
    }

    private void definirParcours(long trousse, String etapes) throws Exception {
        corps(appel("PUT", "/api/clm/trousses/" + trousse + "/parcours", initiateur, etapes), 200);
    }

    private JsonNode detail(long id) throws Exception {
        return corps(appel("GET", "/api/clm/trousses/" + id, initiateur, null), 200);
    }

    private MockHttpServletResponse decider(long etape, Cookie qui, String corps, int attendu) throws Exception {
        MockHttpServletResponse r = appel("POST", "/api/clm/etapes/" + etape + "/decider", qui, corps);
        assertEquals(attendu, r.getStatus(), r.getContentAsString(StandardCharsets.UTF_8));
        return r;
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
