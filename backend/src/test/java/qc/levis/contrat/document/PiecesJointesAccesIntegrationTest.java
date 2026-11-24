package qc.levis.contrat.document;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.support.Api;

/** SEC-05 : pièces jointes et commentaires d'une fiche réservés à son demandeur et au personnel ; l'interne reste interne. */
@SpringBootTest
@AutoConfigureMockMvc
class PiecesJointesAccesIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;
    Cookie demandeur;
    Cookie autre;
    String base;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-pieces", "formulaire", "saisie", "document");
        String u = UUID.randomUUID().toString().substring(0, 6);
        for (String c : List.of("demandeur." + u, "autre." + u)) {
            api.post("/api/admin/users", admin, Map.of("email", c + "@essai.example", "nom", c, "motDePasse", Api.PW + "x1", "roles", List.of("LECTEUR"), "actif", true));
        }
        demandeur = api.connecter("demandeur." + u + "@essai.example", Api.PW + "x1");
        autre = api.connecter("autre." + u + "@essai.example", Api.PW + "x1");
        long f = api.post("/api/formulaires", admin, Map.of("code", "PJ" + u.toUpperCase(), "nom", "Demande", "domaine", "RH")).id();
        api.put("/api/formulaires/" + f + "/structure", admin, Map.of("structureJson", "{\"pages\":[{\"champs\":[{\"cle\":\"nom\",\"libelle\":\"Nom\"}]}]}"));
        api.post("/api/formulaires/" + f + "/soumettre", admin, null);
        api.post("/api/formulaires/" + f + "/publier", admin, null);
        long fiche = api.post("/api/formulaires/" + f + "/saisies", demandeur, null).id();
        base = "/api/formulaires/" + f + "/saisies/" + fiche;
    }

    private int televerser(Cookie qui) throws Exception {
        MockMultipartFile fichier = new MockMultipartFile("fichiers", "piece.pdf", "application/pdf", "%PDF-1.7 piece".getBytes());
        return mvc.perform(MockMvcRequestBuilders.multipart(base + "/documents").file(fichier).cookie(qui)).andReturn().getResponse().getStatus();
    }

    @Test
    void le_demandeur_et_le_personnel_accedent_aux_pieces_mais_pas_un_autre_membre() throws Exception {
        assertEquals(200, televerser(demandeur));
        assertEquals(404, televerser(autre));
        JsonNode pieces = api.get(base + "/documents", admin).corps();
        assertEquals(1, pieces.size());
        long piece = pieces.get(0).get("id").asLong();
        assertEquals(404, api.get(base + "/documents", autre).statut());
        assertEquals(200, api.get("/api/documents/" + piece + "/download", demandeur).statut());
        assertEquals(404, api.get("/api/documents/" + piece + "/download", autre).statut());
    }

    @Test
    void le_demandeur_ne_lit_ni_n_ecrit_de_commentaire_interne() throws Exception {
        api.post(base + "/commentaires", admin, Map.of("visibilite", "INTERNE", "corps", "Dossier à surveiller"));
        api.post(base + "/commentaires", admin, Map.of("visibilite", "DEMANDEUR", "corps", "Merci de joindre la pièce"));
        JsonNode vus = api.get(base + "/commentaires", demandeur).corps();
        assertEquals(1, vus.size());
        assertEquals("Merci de joindre la pièce", vus.get(0).get("corps").asText());
        JsonNode ecrit = api.post(base + "/commentaires", demandeur, Map.of("visibilite", "INTERNE", "corps", "Je me déclare interne")).corps();
        assertEquals("DEMANDEUR", ecrit.get("visibilite").asText());
        assertEquals(3, api.get(base + "/commentaires", admin).corps().size());
        assertEquals(404, api.get(base + "/commentaires", autre).statut());
    }
}
