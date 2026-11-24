package qc.levis.contrat.interop;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.support.Api;

/**
 * CHU-8.04 (ADR-021), sur la vraie pile : un système de la plateforme précédente écrit et lit des fiches sous ses anciens
 * noms de champs ; l'historique exporté se reprend après un essai qui n'écrit rien. IQ-D-14 (ADR-022) : un numéro de
 * carte est refusé à la frontière.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EchangesHeritesIntegrationTest {

    static final String STRUCTURE = """
            {"pages":[{"titre":"Inscription","champs":[
              {"cle":"nom","libelle":"Nom","type":"TEXTE"},
              {"cle":"courriel","libelle":"Courriel","type":"TEXTE"},
              {"code":"service","libelle":"Service","type":"TEXTE"}]}],
             "regles":[{"type":"VALIDATION","conditions":[{"champ":"nom","operateur":"VIDE"}],"valeur":"Le nom est requis."}]}""";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;
    String formulaireCode;
    long formulaireId;

    @BeforeEach
    void organisation() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-echanges", "formulaire", "saisie", "regle", "interop");
        formulaireCode = "INS_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        formulaireId = api.post("/api/formulaires", admin, Map.of("code", formulaireCode, "nom", "Inscriptions", "domaine", "RH")).id();
        api.put("/api/formulaires/" + formulaireId + "/structure", admin, Map.of("structureJson", STRUCTURE));
        api.post("/api/formulaires/" + formulaireId + "/soumettre", admin, null);
        assertEquals(200, api.post("/api/formulaires/" + formulaireId + "/publier", admin, null).statut());
    }

    private JsonNode declarer(String code) throws Exception {
        Api.Reponse r = api.post("/api/interop/echanges", admin, Map.of("code", code, "formulaireCode", formulaireCode,
                "correspondances", Map.of("NOM_EMPLOYE", "nom", "COURRIEL_PRO", "courriel", "DIRECTION", "service")));
        assertEquals(201, r.statut(), r.texte());
        return r.corps();
    }

    private String jeton() throws Exception {
        JsonNode client = api.post("/api/interop/clients?nom=Ancien%20portail%20RH", admin, null).corps();
        MockHttpServletResponse r = mvc.perform(MockMvcRequestBuilders.post("/api/public/oauth/token")
                .param("client_id", client.get("clientId").asText()).param("client_secret", client.get("secret").asText()))
                .andReturn().getResponse();
        return json.readTree(r.getContentAsString()).get("accessToken").asText();
    }

    private Api.Reponse publique(String methode, String url, String jeton, Object corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(methode), url);
        if (jeton != null) r = r.header("Authorization", "Bearer " + jeton);
        if (corps != null) r = r.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(corps));
        MockHttpServletResponse rep = mvc.perform(r).andReturn().getResponse();
        String texte = rep.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return new Api.Reponse(rep.getStatus(), texte.getBytes(), texte, texte.isBlank() ? json.createObjectNode() : json.readTree(texte));
    }

    @Test
    void un_systeme_herite_ecrit_et_lit_des_fiches_sous_ses_anciens_noms() throws Exception {
        String code = "portail-rh-" + UUID.randomUUID().toString().substring(0, 4);
        JsonNode echange = declarer(code);
        assertEquals(formulaireCode, echange.get("formulaireCode").asText());
        assertEquals("Inscriptions", echange.get("formulaireNom").asText());
        String jeton = jeton();
        String url = "/api/public/v1/echanges/" + code + "/fiches";

        Api.Reponse depot = publique("POST", url, jeton, Map.of("NOM_EMPLOYE", "Alex Tremblay", "COURRIEL_PRO", "alex@chu.example"));
        assertEquals(201, depot.statut(), depot.texte());
        assertEquals("SOUMIS", depot.corps().get("statut").asText());
        String numero = depot.corps().get("numero").asText();

        JsonNode fiche = api.get("/api/formulaires/" + formulaireId + "/saisies/" + depot.corps().get("id").asLong(), admin).corps();
        assertEquals("Alex Tremblay", json.readTree(fiche.get("donneesJson").asText()).get("nom").asText(), "traduit vers nos clés");

        Api.Reponse lues = publique("GET", url + "?depuis=2020-01-01T00:00:00Z", jeton, null);
        assertEquals(200, lues.statut());
        JsonNode lue = lues.corps().get(0);
        assertEquals(numero, lue.get("numero").asText());
        assertEquals("Alex Tremblay", lue.get("donnees").get("NOM_EMPLOYE").asText(), "rendu sous l'ancien nom");
        assertEquals(0, publique("GET", url + "?depuis=2999-01-01T00:00:00Z", jeton, null).corps().size());

        Api.Reponse inconnu = publique("POST", url, jeton, Map.of("NOM_EMPLOYE", "B", "MATRICULE", "42"));
        assertEquals(400, inconnu.statut());
        assertTrue(inconnu.texte().contains("MATRICULE"), "le nom refusé est cité");
        assertEquals(400, publique("POST", url, jeton, Map.of("COURRIEL_PRO", "sans.nom@chu.example")).statut(), "règles du formulaire");

        assertEquals(401, publique("POST", url, null, Map.of("NOM_EMPLOYE", "X")).statut());
        assertEquals(401, publique("GET", url, "faux", null).statut());
        assertEquals(404, publique("GET", "/api/public/v1/echanges/inconnu/fiches", jeton, null).statut());

        assertEquals(200, api.post("/api/interop/echanges/" + echange.get("id").asLong() + "/desactivation", admin, null).statut());
        assertEquals(404, publique("POST", url, jeton, Map.of("NOM_EMPLOYE", "Y")).statut(), "désactivé = introuvable");
        assertEquals(409, api.post("/api/interop/echanges/" + echange.get("id").asLong() + "/desactivation", admin, null).statut());
    }

    @Test
    void la_reprise_s_essaie_sans_rien_ecrire_puis_reprend_les_enregistrements_valides() throws Exception {
        long id = declarer("reprise-" + UUID.randomUUID().toString().substring(0, 4)).get("id").asLong();
        List<Map<String, Object>> export = List.of(
                Map.of("NOM_EMPLOYE", "Alice", "DIRECTION", "Soins"),
                Map.of("NOM_EMPLOYE", "Bruno", "INCONNU", "x"),
                Map.of("COURRIEL_PRO", "c@chu.example"));

        JsonNode essai = api.post("/api/interop/echanges/" + id + "/reprise", admin, export).corps();
        assertTrue(essai.get("essai").asBoolean());
        assertEquals(1, essai.get("acceptes").asInt());
        assertEquals(2, essai.get("rejetes").asInt());
        assertEquals("VALIDE", essai.get("lignes").get(0).get("statut").asText());
        assertTrue(essai.get("lignes").get(1).get("motif").asText().contains("INCONNU"));
        assertTrue(essai.get("lignes").get(2).get("motif").asText().contains("Le nom est requis"));
        assertEquals(0, api.get("/api/formulaires/" + formulaireId + "/saisies", admin).corps().size(), "l'essai n'écrit rien");

        JsonNode reprise = api.post("/api/interop/echanges/" + id + "/reprise?essai=false", admin, export).corps();
        assertFalse(reprise.get("essai").asBoolean());
        assertEquals("REPRISE", reprise.get("lignes").get(0).get("statut").asText());
        assertTrue(reprise.get("lignes").get(0).get("numero").asText().startsWith("RH-"));
        assertEquals(1, api.get("/api/formulaires/" + formulaireId + "/saisies", admin).corps().size(), "aucun brouillon laissé par un rejet");

        assertEquals(400, api.post("/api/interop/echanges/" + id + "/reprise", admin, List.of()).statut());
        assertEquals(404, api.post("/api/interop/echanges/999999/reprise", admin, export).statut());
    }

    @Test
    void une_declaration_invalide_ou_en_double_est_refusee_et_la_liste_est_bornee_a_l_organisation() throws Exception {
        String code = "double-" + UUID.randomUUID().toString().substring(0, 4);
        declarer(code);
        assertEquals(409, api.post("/api/interop/echanges", admin, Map.of("code", code, "formulaireCode", formulaireCode,
                "correspondances", Map.of("A", "nom"))).statut());
        assertEquals(400, api.post("/api/interop/echanges", admin, Map.of("code", "autre-code", "formulaireCode", formulaireCode,
                "correspondances", Map.of("A", "absent"))).statut());
        assertEquals(400, api.post("/api/interop/echanges", admin, Map.of("code", "autre-code", "formulaireCode", "INCONNU",
                "correspondances", Map.of("A", "nom"))).statut());
        assertTrue(api.get("/api/interop/echanges", admin).corps().size() >= 1);

        Cookie autre = api.organisation("essai-echanges-b", "formulaire", "saisie", "interop");
        assertEquals(0, api.get("/api/interop/echanges", autre).corps().size(), "les échanges de A sont invisibles de B");
        Cookie sansModule = api.organisation("essai-echanges-c", "formulaire", "saisie");
        assertEquals(403, api.get("/api/interop/echanges", sansModule).statut());
    }

    @Test
    void un_numero_de_carte_de_paiement_est_refuse_a_la_frontiere_et_rien_n_est_ecrit() throws Exception {
        long fiche = api.post("/api/formulaires/" + formulaireId + "/saisies", admin, null).id();
        String donnees = json.writeValueAsString(Map.of("nom", "Paiement 4111 1111 1111 1111"));
        Api.Reponse refus = api.appel(org.springframework.http.HttpMethod.PATCH, "/api/formulaires/" + formulaireId + "/saisies/" + fiche, admin, donnees);
        assertEquals(422, refus.statut());
        assertEquals("carte_paiement_refusee", refus.corps().get("error").asText());
        assertFalse(refus.texte().contains("4111"));
        String sain = json.writeValueAsString(Map.of("nom", "Commande 2026-4471"));
        Api.Reponse accepte = api.appel(org.springframework.http.HttpMethod.PATCH, "/api/formulaires/" + formulaireId + "/saisies/" + fiche, admin, sain);
        assertEquals(200, accepte.statut(), accepte.texte());
    }
}
