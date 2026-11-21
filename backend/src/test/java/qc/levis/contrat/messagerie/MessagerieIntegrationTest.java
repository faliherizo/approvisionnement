package qc.levis.contrat.messagerie;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 5.05 : messagerie interne entre membres actifs ; messages chiffrés au repos ; invisible hors des participants. */
@SpringBootTest
@AutoConfigureMockMvc
class MessagerieIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate base;
    Api api;
    Cookie admin;
    String bob;
    String eve;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-messagerie", "saisie");
        String u = UUID.randomUUID().toString().substring(0, 6);
        bob = "bob." + u + "@essai.example";
        eve = "eve." + u + "@essai.example";
        for (String c : List.of(bob, eve)) {
            api.post("/api/admin/users", admin, Map.of("email", c, "nom", c, "motDePasse", Api.PW + "x1", "roles", List.of("LECTEUR"), "actif", true));
        }
    }

    @Test
    void une_conversation_s_echange_entre_participants_et_le_contenu_est_chiffre_en_base() throws Exception {
        String secret = "Diagnostic confidentiel " + UUID.randomUUID();
        Api.Reponse r = api.post("/api/messagerie/conversations", admin, Map.of("sujet", "Fiche RH-12", "reference", "RH-12",
                "participants", List.of(bob), "message", secret));
        assertEquals(200, r.statut(), r.texte());
        long id = r.id();
        assertEquals(secret, r.corps().get("messages").get(0).get("texte").asText());
        String enBase = base.queryForObject("select contenu_chiffre from messagerie_message where conversation_id = ?", String.class, id);
        assertFalse(enBase.contains("confidentiel"), "le message est chiffré au repos");

        Cookie b = api.connecter(bob, Api.PW + "x1");
        JsonNode liste = api.get("/api/messagerie/conversations", b).corps();
        assertEquals(1, liste.get(0).get("nonLus").asInt());
        api.get("/api/messagerie/conversations/" + id, b);
        assertEquals(0, api.get("/api/messagerie/conversations", b).corps().get(0).get("nonLus").asInt());
        JsonNode reponse = api.post("/api/messagerie/conversations/" + id + "/messages", b, Map.of("texte", "<script>x</script> Reçu")).corps();
        assertEquals("<script>x</script> Reçu", reponse.get("messages").get(1).get("texte").asText(), "texte brut, rendu échappé par l'interface");
    }

    @Test
    void qui_ne_participe_pas_ne_voit_rien_jusqu_a_son_ajout() throws Exception {
        long id = api.post("/api/messagerie/conversations", admin, Map.of("sujet", "Privé", "participants", List.of(bob), "message", "m")).id();
        Cookie e = api.connecter(eve, Api.PW + "x1");
        assertEquals(404, api.get("/api/messagerie/conversations/" + id, e).statut());
        assertEquals(404, api.post("/api/messagerie/conversations/" + id + "/messages", e, Map.of("texte", "intrusion")).statut());
        assertEquals(0, api.get("/api/messagerie/conversations", e).corps().size());
        api.post("/api/messagerie/conversations/" + id + "/participants", admin, Map.of("courriel", eve));
        assertEquals(200, api.get("/api/messagerie/conversations/" + id, e).statut());
    }

    @Test
    void seuls_des_membres_actifs_de_l_organisation_participent() throws Exception {
        Api.Reponse r = api.post("/api/messagerie/conversations", admin, Map.of("sujet", "S", "participants", List.of("redacteur@levis.qc.ca"), "message", "m"));
        assertEquals(400, r.statut());
        assertTrue(r.corps().get("message").asText().contains("membre actif"));
        assertEquals(400, api.post("/api/messagerie/conversations", admin, Map.of("sujet", "S", "participants", List.of(), "message", "m")).statut());
        assertEquals(400, api.post("/api/messagerie/conversations", admin, Map.of("sujet", "S", "participants", List.of(bob), "message", " ")).statut());
    }

    @Test
    void une_autre_organisation_ne_voit_pas_la_conversation() throws Exception {
        long id = api.post("/api/messagerie/conversations", admin, Map.of("sujet", "S", "participants", List.of(bob), "message", "m")).id();
        Cookie autre = api.organisation("essai-messagerie-b", "saisie");
        assertEquals(404, api.get("/api/messagerie/conversations/" + id, autre).statut());
    }

    @Test
    void l_effacement_loi25_retire_l_auteur_des_conversations() throws Exception {
        long id = api.post("/api/messagerie/conversations", admin, Map.of("sujet", "S", "participants", List.of(bob), "message", "m")).id();
        api.post("/api/messagerie/conversations/" + id + "/messages", api.connecter(bob, Api.PW + "x1"), Map.of("texte", "écrit par Bob"));
        JsonNode dossier = api.post("/api/loi25/acces", admin, Map.of("courriel", bob)).corps();
        assertTrue(dossier.toString().contains("écrit par Bob"));
        api.post("/api/loi25/effacement", admin, Map.of("courriel", bob, "motif", "demande"));
        String fil = api.get("/api/messagerie/conversations/" + id, admin).texte();
        assertFalse(fil.contains(bob));
        assertTrue(fil.contains("écrit par Bob"), "le message reste pour les autres participants");
    }
}
