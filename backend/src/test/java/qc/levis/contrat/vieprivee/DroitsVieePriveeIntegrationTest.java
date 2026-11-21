package qc.levis.contrat.vieprivee;

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
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** Loi 25 (Lévis ENF1, C-15) : accès aux renseignements d'une personne, puis effacement, inscrits au registre. */
@SpringBootTest
@AutoConfigureMockMvc
class DroitsVieePriveeIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;

    @BeforeEach
    void client() throws Exception {
        api = new Api(mvc, json);
        admin = api.connecter("admin@levis.qc.ca");
    }

    /** Une personne qui a un compte, s'est connectée (journal) et a écrit une clause (version attribuée). */
    private String personneActive() throws Exception {
        String courriel = "personne." + UUID.randomUUID().toString().substring(0, 8) + "@levis.qc.ca";
        assertEquals(200, api.post("/api/admin/users", admin, Map.of("email", courriel, "nom", "Marie Tremblay",
                "motDePasse", Api.PW, "roles", List.of("REDACTEUR"), "actif", true)).statut());
        Cookie elle = api.connecter(courriel);
        api.post("/api/clauses", elle, Map.of("code", "L25-" + UUID.randomUUID().toString().substring(0, 6),
                "titre", "Clause de Marie", "categorie", "STANDARD", "corps", "Texte"));
        return courriel;
    }

    private JsonNode source(JsonNode dossier, String nom) {
        for (JsonNode s : dossier.get("sources")) if (nom.equals(s.get("source").asText())) return s;
        throw new AssertionError("source absente : " + nom);
    }

    @Test
    void l_acces_communique_tout_ce_qui_est_detenu_sur_la_personne() throws Exception {
        String courriel = personneActive();
        Api.Reponse r = api.post("/api/loi25/acces", admin, Map.of("courriel", courriel.toUpperCase()));
        assertEquals(200, r.statut());
        assertEquals(courriel, r.corps().get("courriel").asText());
        JsonNode compte = source(r.corps(), "Compte").get("elements");
        assertEquals(1, compte.size());
        assertEquals("Marie Tremblay", compte.get(0).get("valeurs").get("nom").asText());
        assertFalse(compte.get(0).get("valeurs").toString().contains("$2a$"), "aucune empreinte de mot de passe communiquée");
        assertTrue(source(r.corps(), "Journal d'audit").get("elements").size() >= 1);
        assertTrue(source(r.corps(), "Rédaction").get("elements").size() >= 1);
        JsonNode registre = api.get("/api/loi25/demandes", admin).corps();
        assertEquals("ACCES", registre.get(0).get("type").asText());
        assertFalse(registre.toString().contains(courriel), "le registre ne garde que l'empreinte du courriel");
    }

    @Test
    void l_effacement_retire_l_identite_partout_et_ferme_le_compte() throws Exception {
        String courriel = personneActive();
        Api.Reponse r = api.post("/api/loi25/effacement", admin, Map.of("courriel", courriel, "motif", "Demande écrite n° 2026-07"));
        assertEquals(200, r.statut(), r.texte());
        assertTrue(r.texte().contains("\"source\":\"Compte\",\"effaces\":1"), r.texte());

        Api.Reponse connexion = api.post("/api/auth/login", null, Map.of("email", courriel, "motDePasse", Api.PW));
        assertNotEquals(200, connexion.statut());
        JsonNode dossier = api.post("/api/loi25/acces", admin, Map.of("courriel", courriel)).corps();
        for (JsonNode s : dossier.get("sources")) assertEquals(0, s.get("elements").size(), s.get("source").asText());
        String journal = api.get("/api/audit", admin).texte();
        assertFalse(journal.contains(courriel), "le journal ne nomme plus la personne");
        JsonNode registre = api.get("/api/loi25/demandes", admin).corps();
        assertEquals("ACCES", registre.get(0).get("type").asText(), "la vérification qui suit est elle-même une demande d'accès");
        assertEquals("EFFACEMENT", registre.get(1).get("type").asText());
        assertEquals("Demande écrite n° 2026-07", registre.get(1).get("motif").asText());
    }

    @Test
    void une_personne_ne_s_efface_pas_elle_meme() throws Exception {
        Api.Reponse r = api.post("/api/loi25/effacement", admin, Map.of("courriel", "admin@levis.qc.ca", "motif", "essai"));
        assertEquals(409, r.statut());
        assertEquals(200, api.get("/api/auth/me", admin).statut());
    }

    @Test
    void seuls_les_administrateurs_exercent_ces_droits_et_les_entrees_sont_validees() throws Exception {
        Cookie redacteur = api.connecter("redacteur@levis.qc.ca");
        assertEquals(403, api.post("/api/loi25/acces", redacteur, Map.of("courriel", "admin@levis.qc.ca")).statut());
        assertEquals(403, api.post("/api/loi25/effacement", redacteur, Map.of("courriel", "admin@levis.qc.ca", "motif", "x")).statut());
        assertEquals(403, api.get("/api/loi25/demandes", redacteur).statut());
        assertEquals(400, api.post("/api/loi25/acces", admin, Map.of("courriel", "pas-un-courriel")).statut());
        assertEquals(400, api.post("/api/loi25/effacement", admin, Map.of("courriel", "a@b.ca")).statut());
    }

    @Test
    void une_organisation_n_obtient_rien_sur_les_personnes_d_une_autre() throws Exception {
        Cookie autre = api.organisation("essai-loi25");
        JsonNode dossier = api.post("/api/loi25/acces", autre, Map.of("courriel", "redacteur@levis.qc.ca")).corps();
        for (JsonNode s : dossier.get("sources")) assertEquals(0, s.get("elements").size(), s.get("source").asText());
        api.post("/api/loi25/effacement", autre, Map.of("courriel", "redacteur@levis.qc.ca", "motif", "tentative"));
        assertEquals(200, api.get("/api/auth/me", api.connecter("redacteur@levis.qc.ca")).statut());
    }
}
