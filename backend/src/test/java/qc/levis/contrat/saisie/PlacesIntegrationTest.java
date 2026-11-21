package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 3.06 et 4.08 : un choix complet n'est plus proposé ni accepté ; l'inventaire montre places et inscriptions. */
@SpringBootTest
@AutoConfigureMockMvc
class PlacesIntegrationTest {

    static final String[] MODULES = {"formulaire", "saisie", "regle", "rapport"};
    static final String STRUCTURE = """
            {"pages":[{"titre":"Inscription","champs":[
              {"cle":"nom","libelle":"Nom","type":"TEXTE"},
              {"cle":"activite","libelle":"Activité","type":"LISTE","options":["Atelier","Formation"],"capacites":{"Atelier":1}},
              {"code":"mode","libelle":"Mode","type":"radio","options":[{"valeur":"Présentiel","capacite":5},{"valeur":"Virtuel"}]}]}]}""";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;

    @BeforeEach
    void client() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-places", MODULES);
    }

    private long formulairePublie() throws Exception {
        String code = "INS_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        long id = api.post("/api/formulaires", admin, Map.of("code", code, "nom", code, "domaine", "RH")).id();
        api.put("/api/formulaires/" + id + "/structure", admin, Map.of("structureJson", STRUCTURE));
        api.post("/api/formulaires/" + id + "/soumettre", admin, null);
        assertEquals(200, api.post("/api/formulaires/" + id + "/publier", admin, null).statut());
        return id;
    }

    private Api.Reponse inscrire(long formulaire, String nom, String activite) throws Exception {
        long fiche = api.post("/api/formulaires/" + formulaire + "/saisies", admin, null).id();
        String donnees = json.writeValueAsString(Map.of("nom", nom, "activite", activite, "mode", "Virtuel"));
        return api.post("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/soumettre", admin, Map.of("donneesJson", donnees));
    }

    @Test
    void la_derniere_place_prise_le_choix_est_complet_et_refuse() throws Exception {
        long f = formulairePublie();
        assertEquals(200, inscrire(f, "Alice", "Atelier").statut());
        JsonNode places = api.get("/api/formulaires/" + f + "/places", admin).corps();
        JsonNode atelier = places.get(0);
        assertEquals("Atelier", atelier.get("choix").asText());
        assertTrue(atelier.get("complet").asBoolean());
        assertEquals(0, atelier.get("restantes").asInt());
        assertFalse(places.get(1).get("complet").asBoolean());
        assertEquals(5, places.get(2).get("capacite").asInt(), "capacité déclarée sur l'option");

        Api.Reponse refus = inscrire(f, "Bruno", "Atelier");
        assertEquals(409, refus.statut());
        assertTrue(refus.corps().get("message").asText().contains("complet"));
        assertEquals(200, inscrire(f, "Bruno", "Formation").statut());
    }

    @Test
    void l_inventaire_liste_les_inscriptions_par_choix_et_s_exporte_en_csv_sans_formule() throws Exception {
        long f = formulairePublie();
        inscrire(f, "Alice", "Formation");
        api.post("/api/formulaires/" + f + "/saisies", admin, null);
        JsonNode inventaire = api.get("/api/formulaires/" + f + "/inventaire", admin).corps();
        JsonNode formation = inventaire.get(1);
        assertEquals("Formation", formation.get("place").get("choix").asText());
        assertEquals(1, formation.get("inscriptions").size(), "le brouillon n'occupe pas de place");
        assertEquals("SOUMIS", formation.get("inscriptions").get(0).get("statut").asText());
        Api.Reponse csv = api.get("/api/formulaires/" + f + "/inventaire.csv", admin);
        assertEquals(200, csv.statut());
        assertTrue(csv.texte().contains("\"Formation\""));
        assertTrue(csv.texte().startsWith("﻿champ;choix;capacite"));
    }

    @Test
    void les_places_d_un_formulaire_d_une_autre_organisation_sont_introuvables() throws Exception {
        long f = formulairePublie();
        Cookie autre = api.organisation("essai-places-b", MODULES);
        assertEquals(404, api.get("/api/formulaires/" + f + "/places", autre).statut());
        assertEquals(404, api.get("/api/formulaires/" + f + "/inventaire", autre).statut());
    }
}
