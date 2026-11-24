package qc.levis.contrat.securite;

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
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/**
 * Constats SEC-17 à SEC-21 des tests d'intrusion, verrouillés : sous-ressources d'un devis et circuit d'un formulaire
 * bornés à l'organisation, feuilles de temps à leur propriétaire, demandes d'approbation et commentaires de révision
 * réservés au personnel, requête de fichier mal formée en 400.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SousRessourcesIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie redacteurLevis;
    Cookie autre;

    @BeforeEach
    void client() throws Exception {
        api = new Api(mvc, json);
        redacteurLevis = api.connecter("redacteur@levis.qc.ca");
        autre = api.organisation("essai-sous-ressources", "redaction", "formulaire", "processus", "rh", "saisie");
    }

    @Test
    void les_sous_ressources_d_un_devis_restent_dans_son_organisation() throws Exception {
        long devis = api.post("/api/documents", redacteurLevis, Map.of("titre", "Devis " + UUID.randomUUID(), "type", "CONTRAT_SERVICES")).id();
        for (String s : List.of("versions", "signataires", "obligations", "addendas")) {
            assertEquals(404, api.get("/api/documents/" + devis + "/" + s, autre).statut(), "lecture " + s);
        }
        for (Api.Reponse r : List.of(
                api.post("/api/documents/" + devis + "/signataires", autre, Map.of("nom", "X", "courriel", "x@x.ca", "role", "SIGNATAIRE")),
                api.post("/api/documents/" + devis + "/obligations", autre, Map.of("titre", "X", "dateEcheance", "2027-01-01")),
                api.post("/api/documents/" + devis + "/addendas", autre, Map.of("objet", "X")))) {
            assertTrue(r.statut() >= 400 && r.statut() < 500, "refusé sans erreur interne : " + r.statut());
        }
        for (String s : List.of("signataires", "obligations", "addendas")) {
            assertEquals(0, api.get("/api/documents/" + devis + "/" + s, redacteurLevis).corps().size(), "rien n'a été rattaché au devis : " + s);
        }
    }

    @Test
    void le_circuit_et_le_resume_d_un_formulaire_restent_dans_son_organisation() throws Exception {
        Cookie a = api.organisation("essai-sous-ressources-a", "formulaire", "processus", "rapport", "saisie");
        long f = api.post("/api/formulaires", a, Map.of("code", "W" + UUID.randomUUID().toString().substring(0, 6), "nom", "F", "domaine", "RH")).id();
        assertEquals(200, api.get("/api/formulaires/" + f + "/workflow", a).statut());
        assertEquals(404, api.get("/api/formulaires/" + f + "/workflow", autre).statut());
        assertEquals(404, api.put("/api/formulaires/" + f + "/workflow", autre, Map.of("definitionJson", "{}")).statut());
        Cookie autreRapport = api.organisation("essai-sous-ressources", "rapport");
        assertEquals(404, api.get("/api/rapport/formulaires/" + f + "/resume", autreRapport).statut());
        assertEquals(404, api.get("/api/rapport/formulaires/" + f + "/export.xlsx", autreRapport).statut());
    }

    @Test
    void une_feuille_de_temps_n_est_modifiee_et_soumise_que_par_son_proprietaire() throws Exception {
        String u = UUID.randomUUID().toString().substring(0, 6);
        for (String c : List.of("emp1." + u, "emp2." + u)) {
            api.post("/api/admin/users", autre, Map.of("email", c + "@essai.example", "nom", c, "motDePasse", Api.PW + "x1", "roles", List.of("LECTEUR"), "actif", true));
        }
        Cookie emp1 = api.connecter("emp1." + u + "@essai.example", Api.PW + "x1");
        Cookie emp2 = api.connecter("emp2." + u + "@essai.example", Api.PW + "x1");
        long feuille = api.post("/api/rh/feuilles-temps", emp1, Map.of("periodeDebut", "2026-11-16", "periodeFin", "2026-11-22", "lignes", List.of())).id();
        assertEquals(404, api.get("/api/rh/feuilles-temps/" + feuille, emp2).statut());
        assertEquals(404, api.put("/api/rh/feuilles-temps/" + feuille, emp2, Map.of("lignes", List.of())).statut());
        assertEquals(404, api.post("/api/rh/feuilles-temps/" + feuille + "/soumettre", emp2, null).statut());
        assertEquals(200, api.get("/api/rh/feuilles-temps/" + feuille, autre).statut(), "l'approbateur lit la feuille");
        assertEquals(200, api.post("/api/rh/feuilles-temps/" + feuille + "/soumettre", emp1, null).statut());
    }

    @Test
    void un_role_de_consultation_ne_cree_ni_demande_d_approbation_ni_commentaire_de_revision() throws Exception {
        api.post("/api/admin/users", autre, Map.of("email", "lect.sr@essai.example", "nom", "L", "motDePasse", Api.PW + "x1", "roles", List.of("LECTEUR"), "actif", true));
        Cookie lecteur = api.connecter("lect.sr@essai.example", Api.PW + "x1");
        assertEquals(403, api.post("/api/approbations/demandes", lecteur, Map.of("documentType", "ACHAT", "documentId", "1", "contexte", Map.of())).statut());
        assertEquals(403, api.get("/api/approbations/demandes", lecteur).statut());
        long devis = api.post("/api/documents", autre, Map.of("titre", "Devis interne", "type", "CONTRAT_SERVICES")).id();
        assertEquals(403, api.post("/api/documents/" + devis + "/commentaires", lecteur, Map.of("corps", "écrit par un lecteur")).statut());
    }

    @Test
    void une_requete_de_fichier_mal_formee_est_une_erreur_du_client() throws Exception {
        Cookie doc = api.organisation("essai-sous-ressources", "document");
        Api.Reponse r = api.appel(HttpMethod.POST, "/api/formulaires/1/saisies/1/documents", doc, Map.of("fichiers", "pas un fichier"));
        assertEquals(400, r.statut(), r.texte());
    }
}
