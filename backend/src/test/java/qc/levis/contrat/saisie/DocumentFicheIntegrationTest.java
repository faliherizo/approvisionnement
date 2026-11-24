package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 5.04 : un document PDF/A se produit depuis les données d'une fiche, puis se transmet par courriel. */
@SpringBootTest
@AutoConfigureMockMvc
class DocumentFicheIntegrationTest {

    static final String STRUCTURE = """
            {"pages":[{"titre":"Identité","champs":[{"cle":"nom","libelle":"Nom complet","type":"TEXTE"},
              {"cle":"jours","libelle":"Jours","type":"CASES","options":["Lundi","Mardi"]},{"cle":"accepte","libelle":"J'accepte","type":"CASE"}]},
              {"titre":"Activité","champs":[{"cle":"note","libelle":"Commentaire","type":"ZONE_TEXTE"}]}]}""";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie admin;
    long formulaire;
    long fiche;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-document-fiche", "formulaire", "saisie", "notification");
        String code = "DOC_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        formulaire = api.post("/api/formulaires", admin, Map.of("code", code, "nom", "Demande de soutien", "domaine", "RH")).id();
        api.put("/api/formulaires/" + formulaire + "/structure", admin, Map.of("structureJson", STRUCTURE));
        api.post("/api/formulaires/" + formulaire + "/soumettre", admin, null);
        api.post("/api/formulaires/" + formulaire + "/publier", admin, null);
        fiche = api.post("/api/formulaires/" + formulaire + "/saisies", admin, null).id();
        String donnees = json.writeValueAsString(Map.of("nom", "Alice Roy", "jours", List.of("Lundi", "Mardi"), "accepte", true));
        api.post("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/soumettre", admin, Map.of("donneesJson", donnees));
    }

    @Test
    void le_document_reprend_les_reponses_dans_l_ordre_du_formulaire() throws Exception {
        Api.Reponse r = api.get("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document.pdf", admin);
        assertEquals(200, r.statut());
        try (PDDocument pdf = PDDocument.load(r.octets())) {
            String texte = new PDFTextStripper().getText(pdf);
            assertTrue(texte.contains("Demande de soutien"), texte);
            assertTrue(texte.contains("Nom complet") && texte.contains("Alice Roy"), texte);
            assertTrue(texte.contains("Lundi, Mardi"), texte);
            assertTrue(texte.contains("Oui"), texte);
            assertTrue(texte.indexOf("Identité") < texte.indexOf("Activité"), texte);
            assertEquals("Demande de soutien", pdf.getDocumentInformation().getTitle());
        }
    }

    @Test
    void le_document_se_transmet_et_la_transmission_est_journalisee() throws Exception {
        Api.Reponse r = api.post("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document/transmettre", admin,
                Map.of("destinataire", "partenaire@chu.example", "message", "Voici la fiche."));
        assertEquals(200, r.statut(), r.texte());
        assertEquals("SIMULE", r.corps().get("statut").asText());
        assertTrue(api.get("/api/notification/journal", admin).texte().contains("pièce jointe"));
        assertEquals(400, api.post("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document/transmettre", admin,
                Map.of("destinataire", "pas-un-courriel")).statut());
    }

    @Test
    void ni_un_autre_demandeur_ni_une_autre_organisation_n_obtient_le_document() throws Exception {
        api.post("/api/admin/users", admin, Map.of("email", "lecteur.doc@essai.example", "nom", "Lecteur", "motDePasse", Api.PW + "x1",
                "roles", List.of("LECTEUR"), "actif", true));
        Cookie lecteur = api.connecter("lecteur.doc@essai.example", Api.PW + "x1");
        assertEquals(404, api.get("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document.pdf", lecteur).statut(),
                "la fiche d'un autre demandeur est introuvable");
        assertEquals(403, api.post("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document/transmettre", lecteur,
                Map.of("destinataire", "x@y.ca")).statut());
        assertEquals(404, api.get("/api/formulaires/" + (formulaire + 999) + "/saisies/" + fiche + "/document.pdf", admin).statut());
        Cookie autre = api.organisation("essai-document-fiche-b", "formulaire", "saisie");
        assertEquals(404, api.get("/api/formulaires/" + formulaire + "/saisies/" + fiche + "/document.pdf", autre).statut());
    }
}
