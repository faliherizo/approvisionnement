package qc.levis.contrat.redaction;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** Lévis EF1 (C-03) : chaque version d'une clause est consultable avec son auteur et sa date, et peut être restaurée. */
@SpringBootTest
@AutoConfigureMockMvc
class ClauseVersionIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;

    @BeforeEach
    void client() { api = new Api(mvc, json); }

    private Map<String, Object> clause(String code, String corps) {
        return Map.of("code", code, "titre", "Assurance " + code, "categorie", "STANDARD", "corps", corps);
    }

    @Test
    void chaque_modification_cree_une_version_consultable_puis_restaurable() throws Exception {
        Cookie redacteur = api.connecter("redacteur@levis.qc.ca");
        Cookie juriste = api.connecter("juriste@levis.qc.ca");
        String code = "HIS-" + UUID.randomUUID().toString().substring(0, 8);
        long id = api.post("/api/clauses", redacteur, clause(code, "Texte initial")).id();
        api.put("/api/clauses/" + id, juriste, clause(code, "Texte révisé"));

        JsonNode versions = api.get("/api/clauses/" + id + "/versions", redacteur).corps();
        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).get("numero").asInt());
        assertEquals("juriste@levis.qc.ca", versions.get(0).get("auteur").asText());
        assertEquals("MODIFICATION", versions.get(0).get("motif").asText());
        assertEquals("Texte initial", versions.get(1).get("corps").asText());
        assertEquals("redacteur@levis.qc.ca", versions.get(1).get("auteur").asText());
        assertNotNull(versions.get(1).get("creeLe").asText());

        Api.Reponse restauree = api.post("/api/clauses/" + id + "/versions/1/restaurer", redacteur, null);
        assertEquals(200, restauree.statut());
        assertEquals("Texte initial", restauree.corps().get("corps").asText());
        assertEquals(3, restauree.corps().get("version").asInt());
        JsonNode apres = api.get("/api/clauses/" + id + "/versions", redacteur).corps();
        assertEquals("RESTAURATION", apres.get(0).get("motif").asText());
        assertEquals(1, apres.get(0).get("versionRestauree").asInt());
    }

    @Test
    void une_version_inexistante_ne_se_restaure_pas() throws Exception {
        Cookie redacteur = api.connecter("redacteur@levis.qc.ca");
        long id = api.post("/api/clauses", redacteur, clause("HIS-" + UUID.randomUUID().toString().substring(0, 8), "v1")).id();
        assertEquals(404, api.post("/api/clauses/" + id + "/versions/9/restaurer", redacteur, null).statut());
    }

    @Test
    void une_clause_amorcee_presente_son_etat_initial_attribue_au_systeme() throws Exception {
        Cookie redacteur = api.connecter("redacteur@levis.qc.ca");
        JsonNode premiere = api.get("/api/clauses", redacteur).corps().get(0);
        JsonNode versions = api.get("/api/clauses/" + premiere.get("id").asLong() + "/versions", redacteur).corps();
        assertTrue(versions.size() >= 1);
        assertEquals(premiere.get("corps").asText(), versions.get(versions.size() - 1).get("corps").asText());
    }

    @Test
    void la_modification_d_une_clause_amorcee_conserve_son_etat_d_origine() throws Exception {
        Cookie juriste = api.connecter("juriste@levis.qc.ca");
        JsonNode amorcee = api.get("/api/clauses", juriste).corps().get(1);
        long id = amorcee.get("id").asLong();
        Map<String, Object> req = Map.of("code", amorcee.get("code").asText(), "titre", amorcee.get("titre").asText(),
                "categorie", amorcee.get("categorie").asText(), "corps", amorcee.get("corps").asText() + " (révisé)");
        assertEquals(200, api.put("/api/clauses/" + id, juriste, req).statut());
        JsonNode versions = api.get("/api/clauses/" + id + "/versions", juriste).corps();
        JsonNode origine = versions.get(versions.size() - 1);
        assertEquals(amorcee.get("corps").asText(), origine.get("corps").asText());
        assertTrue(origine.get("auteur").asText().startsWith("système"));
    }

    @Test
    void une_clause_d_une_autre_organisation_est_introuvable() throws Exception {
        Cookie levis = api.connecter("redacteur@levis.qc.ca");
        long id = api.post("/api/clauses", levis, clause("ISO-" + UUID.randomUUID().toString().substring(0, 8), "Lévis seulement")).id();
        Cookie autre = api.organisation("essai-clauses", "redaction");
        assertEquals(404, api.get("/api/clauses/" + id, autre).statut());
        assertEquals(404, api.get("/api/clauses/" + id + "/versions", autre).statut());
        assertEquals(404, api.put("/api/clauses/" + id, autre, clause("X", "piraté")).statut());
        assertEquals(404, api.post("/api/clauses/" + id + "/versions/1/restaurer", autre, null).statut());
        assertEquals(404, api.delete("/api/clauses/" + id, autre).statut());
        assertEquals("Lévis seulement", api.get("/api/clauses/" + id, levis).corps().get("corps").asText());
    }
}
