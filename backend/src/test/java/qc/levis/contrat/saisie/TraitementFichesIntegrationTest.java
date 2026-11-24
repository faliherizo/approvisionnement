package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** Traitement des fiches sous le contrôle d'accès SEC-04 : cycle complet par le personnel, périmètre du demandeur. */
@SpringBootTest
@AutoConfigureMockMvc
class TraitementFichesIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;
    Cookie demandeur;
    long formulaire;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-traitement", "formulaire", "saisie", "rapport");
        String u = UUID.randomUUID().toString().substring(0, 6);
        api.post("/api/admin/users", admin, Map.of("email", "d." + u + "@essai.example", "nom", "D", "motDePasse", Api.PW + "x1", "roles", List.of("LECTEUR"), "actif", true));
        demandeur = api.connecter("d." + u + "@essai.example", Api.PW + "x1");
        formulaire = api.post("/api/formulaires", admin, Map.of("code", "TR" + u.toUpperCase(), "nom", "Demande", "domaine", "RH")).id();
        api.put("/api/formulaires/" + formulaire + "/structure", admin, Map.of("structureJson", "{\"pages\":[{\"champs\":[{\"cle\":\"nom\",\"libelle\":\"Nom\"}]}]}"));
        api.post("/api/formulaires/" + formulaire + "/soumettre", admin, null);
        api.post("/api/formulaires/" + formulaire + "/publier", admin, null);
    }

    private String fiche(Cookie qui, String nom) throws Exception {
        long id = api.post("/api/formulaires/" + formulaire + "/saisies", qui, null).id();
        String url = "/api/formulaires/" + formulaire + "/saisies/" + id;
        assertEquals(200, api.appel(org.springframework.http.HttpMethod.PATCH, url, qui, json.writeValueAsString(Map.of("nom", nom))).statut());
        api.post(url + "/soumettre", qui, Map.of("donneesJson", json.writeValueAsString(Map.of("nom", nom))));
        return url;
    }

    @Test
    void le_personnel_mene_une_fiche_de_bout_en_bout() throws Exception {
        String url = fiche(demandeur, "Alice");
        assertEquals("EN_TRAITEMENT", api.post(url + "/prendre-en-charge", admin, null).corps().get("statut").asText());
        assertEquals("A_COMPLETER", api.post(url + "/complement", admin, null).corps().get("statut").asText());
        api.post(url + "/soumettre", demandeur, Map.of("donneesJson", "{\"nom\":\"Alice complétée\"}"));
        assertEquals("VALIDE", api.post(url + "/valider", admin, null).corps().get("statut").asText());
        assertEquals("CLOTURE", api.post(url + "/cloturer", admin, null).corps().get("statut").asText());
        String autre = fiche(demandeur, "Bruno");
        assertEquals("REJETE", api.post(autre + "/rejeter", admin, null).corps().get("statut").asText());
        assertEquals(403, api.post(autre + "/cloturer", demandeur, null).statut(), "le demandeur ne traite pas");
    }

    @Test
    void le_demandeur_ne_voit_et_n_annule_que_ses_fiches() throws Exception {
        fiche(admin, "Fiche du personnel");
        long brouillon = api.post("/api/formulaires/" + formulaire + "/saisies", demandeur, null).id();
        assertEquals(1, api.get("/api/formulaires/" + formulaire + "/saisies", demandeur).corps().size());
        assertTrue(api.get("/api/formulaires/" + formulaire + "/saisies", admin).corps().size() >= 2);
        assertEquals("ANNULE", api.post("/api/formulaires/" + formulaire + "/saisies/" + brouillon + "/annuler", demandeur, null).corps().get("statut").asText());
        assertEquals(200, api.get("/api/formulaires/" + formulaire + "/saisies/structure", demandeur).statut());
        assertEquals(404, api.get("/api/formulaires/999999/saisies/structure", demandeur).statut());
        assertEquals(404, api.post("/api/formulaires/999999/saisies", demandeur, null).statut());
    }

    @Test
    void criteres_resume_et_export_suivent_leur_proprietaire_et_le_personnel() throws Exception {
        fiche(demandeur, "Alice");
        long critere = api.post("/api/rapport/criteres?nom=Mes%20fiches&formulaireId=" + formulaire, demandeur, "{\"statut\":\"SOUMIS\"}").id();
        assertEquals(1, api.get("/api/rapport/criteres", demandeur).corps().size());
        assertEquals(404, api.delete("/api/rapport/criteres/" + critere, admin).statut(), "le critère d'autrui est introuvable");
        assertEquals(200, api.delete("/api/rapport/criteres/" + critere, demandeur).statut());
        assertEquals(1, api.get("/api/rapport/formulaires/" + formulaire + "/resume", admin).corps().get("SOUMIS").asInt());
        assertEquals(200, api.get("/api/rapport/formulaires/" + formulaire + "/export.xlsx", admin).statut());
        assertEquals(403, api.get("/api/rapport/formulaires/" + formulaire + "/resume", demandeur).statut());
        assertEquals(403, api.get("/api/rapport/formulaires/" + formulaire + "/export.xlsx", demandeur).statut());
    }

    @Test
    void un_tableau_de_bord_prive_reste_a_son_proprietaire_et_ses_indicateurs_se_relisent() throws Exception {
        long t = api.post("/api/rapport/tableaux-de-bord", admin, Map.of("nom", "Privé", "partage", false, "indicateurs",
                List.of(Map.of("titre", "Total", "type", "COMPTEUR", "formulaireId", formulaire, "statuts", List.of("SOUMIS"))))).id();
        api.post("/api/admin/users", admin, Map.of("email", "dir.tdb@essai-traitement.example", "nom", "Dir", "motDePasse", Api.PW + "x1", "roles", List.of("DIRECTION"), "actif", true));
        Cookie direction = api.connecter("dir.tdb@essai-traitement.example", Api.PW + "x1");
        assertTrue(api.get("/api/rapport/tableaux-de-bord", direction).corps().findValuesAsText("nom").stream().noneMatch("Privé"::equals));
        assertEquals("Total", api.get("/api/rapport/tableaux-de-bord", admin).corps().findValue("indicateur").get("titre").asText());
        assertEquals(400, api.post("/api/rapport/tableaux-de-bord", admin, Map.of("nom", "S", "partage", false, "indicateurs",
                List.of(Map.of("titre", "X", "type", "COMPTEUR", "formulaireId", formulaire, "statuts", List.of("INCONNU"))))).statut());
        assertEquals(404, api.delete("/api/rapport/tableaux-de-bord/" + t, direction).statut());
    }
}
