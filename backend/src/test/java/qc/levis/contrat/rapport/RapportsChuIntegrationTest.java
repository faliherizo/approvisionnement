package qc.levis.contrat.rapport;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 6.03 (rapport PDF personnalisé) et 6.04 (tableaux de bord configurables). */
@SpringBootTest
@AutoConfigureMockMvc
class RapportsChuIntegrationTest {

    static final String[] MODULES = {"formulaire", "saisie", "rapport"};
    static final String STRUCTURE = """
            {"pages":[{"titre":"Inscription","champs":[{"cle":"nom","libelle":"Nom complet","type":"TEXTE"},
              {"cle":"activite","libelle":"Activité choisie","type":"LISTE","options":["Atelier","Formation"]}]}]}""";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;
    long formulaire;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-rapports", MODULES);
        String code = "RAP_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        formulaire = api.post("/api/formulaires", admin, Map.of("code", code, "nom", "Inscriptions automne", "domaine", "RH")).id();
        api.put("/api/formulaires/" + formulaire + "/structure", admin, Map.of("structureJson", STRUCTURE));
        api.post("/api/formulaires/" + formulaire + "/soumettre", admin, null);
        api.post("/api/formulaires/" + formulaire + "/publier", admin, null);
        for (String[] r : new String[][] {{"Alice Roy", "Atelier"}, {"Bruno Gagnon", "Formation"}, {"Chloé Roy", "Atelier"}}) {
            long f = api.post("/api/formulaires/" + formulaire + "/saisies", admin, null).id();
            api.post("/api/formulaires/" + formulaire + "/saisies/" + f + "/soumettre", admin,
                    Map.of("donneesJson", json.writeValueAsString(Map.of("nom", r[0], "activite", r[1]))));
        }
        api.post("/api/formulaires/" + formulaire + "/saisies", admin, null);
    }

    @Test
    void le_rapport_pdf_montre_les_questions_et_statuts_choisis() throws Exception {
        Api.Reponse r = api.get("/api/rapport/formulaires/" + formulaire + "/rapport.pdf?titre=Bilan-automne&champs=nom,activite&statuts=SOUMIS", admin);
        assertEquals(200, r.statut(), r.texte());
        try (PDDocument pdf = PDDocument.load(r.octets())) {
            String texte = new PDFTextStripper().getText(pdf);
            assertTrue(texte.contains("Bilan-automne"), texte);
            assertTrue(texte.contains("Nom complet") && texte.contains("Activité choisie"), texte);
            assertTrue(texte.contains("Alice Roy") && texte.contains("Chloé Roy"), texte);
            assertFalse(texte.contains("BROUILLON"), "le brouillon est filtré");
        }
        assertEquals(400, api.get("/api/rapport/formulaires/" + formulaire + "/rapport.pdf?champs=inexistant", admin).statut());
        assertEquals(400, api.get("/api/rapport/formulaires/" + formulaire + "/rapport.pdf?statuts=PERDU", admin).statut());
        assertEquals(404, api.get("/api/rapport/formulaires/" + (formulaire + 999) + "/rapport.pdf", admin).statut());
    }

    @Test
    void le_tableau_de_bord_calcule_ses_indicateurs_et_reste_au_proprietaire() throws Exception {
        Map<String, Object> config = Map.of("nom", "Pilotage automne", "partage", false, "indicateurs", List.of(
                Map.of("titre", "Fiches soumises", "type", "COMPTEUR", "formulaireId", formulaire, "statuts", List.of("SOUMIS")),
                Map.of("titre", "Par activité", "type", "BARRES", "formulaireId", formulaire, "regroupement", "CHAMP", "champ", "activite"),
                Map.of("titre", "Par mois", "type", "EVOLUTION", "formulaireId", formulaire, "regroupement", "MOIS")));
        Api.Reponse cree = api.post("/api/rapport/tableaux-de-bord", admin, config);
        assertEquals(200, cree.statut(), cree.texte());
        JsonNode ind = cree.corps().get("indicateurs");
        assertEquals(3, ind.get(0).get("points").get(0).get("valeur").asInt());
        assertEquals("Atelier", ind.get(1).get("points").get(1).get("etiquette").asText());
        assertEquals(2, ind.get(1).get("points").get(1).get("valeur").asInt());
        long id = cree.id();

        api.post("/api/admin/users", admin, Map.of("email", "appro.tdb@essai.example", "nom", "Appro", "motDePasse", Api.PW + "x1",
                "roles", List.of("APPRO"), "actif", true));
        Cookie appro = api.connecter("appro.tdb@essai.example", Api.PW + "x1");
        assertEquals(404, api.get("/api/rapport/tableaux-de-bord/" + id, appro).statut(), "non partagé : invisible");
        api.put("/api/rapport/tableaux-de-bord/" + id, admin, Map.of("nom", "Pilotage automne", "partage", true, "indicateurs", List.of()));
        assertEquals(200, api.get("/api/rapport/tableaux-de-bord/" + id, appro).statut());
        assertFalse(api.get("/api/rapport/tableaux-de-bord/" + id, appro).corps().get("modifiable").asBoolean());
        assertEquals(403, api.put("/api/rapport/tableaux-de-bord/" + id, appro, Map.of("nom", "X", "partage", true, "indicateurs", List.of())).statut());
        assertEquals(403, api.delete("/api/rapport/tableaux-de-bord/" + id, appro).statut());
        assertEquals(200, api.delete("/api/rapport/tableaux-de-bord/" + id, admin).statut());
    }

    @Test
    void un_indicateur_ne_porte_que_sur_un_formulaire_et_une_question_de_l_organisation() throws Exception {
        Cookie autre = api.organisation("essai-rapports-b", MODULES);
        Api.Reponse r = api.post("/api/rapport/tableaux-de-bord", autre, Map.of("nom", "Espion", "partage", false, "indicateurs",
                List.of(Map.of("titre", "Leurs fiches", "type", "COMPTEUR", "formulaireId", formulaire))));
        assertEquals(400, r.statut());
        assertEquals(400, api.post("/api/rapport/tableaux-de-bord", admin, Map.of("nom", "Q", "partage", false, "indicateurs",
                List.of(Map.of("titre", "X", "type", "BARRES", "formulaireId", formulaire, "regroupement", "CHAMP", "champ", "inconnu")))).statut());
        assertEquals(400, api.post("/api/rapport/tableaux-de-bord", admin, Map.of("nom", "Q", "partage", false, "indicateurs",
                List.of(Map.of("titre", "X", "type", "COMPTEUR", "formulaireId", formulaire, "regroupement", "MOIS")))).statut());
        assertEquals(404, api.get("/api/rapport/formulaires/" + formulaire + "/rapport.pdf", autre).statut());
    }

    @Test
    void le_lecteur_n_obtient_ni_rapport_ni_tableau_de_bord() throws Exception {
        api.post("/api/admin/users", admin, Map.of("email", "lecteur.rap@essai.example", "nom", "L", "motDePasse", Api.PW + "x1",
                "roles", List.of("LECTEUR"), "actif", true));
        Cookie lecteur = api.connecter("lecteur.rap@essai.example", Api.PW + "x1");
        assertEquals(403, api.get("/api/rapport/formulaires/" + formulaire + "/rapport.pdf", lecteur).statut());
        assertEquals(403, api.get("/api/rapport/tableaux-de-bord", lecteur).statut());
    }
}
