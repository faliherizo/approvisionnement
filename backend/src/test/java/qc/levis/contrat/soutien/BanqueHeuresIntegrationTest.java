package qc.levis.contrat.soutien;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 9.05 : le fournisseur tient la banque d'heures ; l'organisation suit son solde et son relevé. */
@SpringBootTest
@AutoConfigureMockMvc
class BanqueHeuresIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie sa;
    Cookie admin;
    long tenant;

    @BeforeEach
    void preparer() throws Exception {
        api = new Api(mvc, json);
        admin = api.organisation("essai-banque");
        sa = api.connecter("superadmin@plateforme.ca", "SuperPassw0rd!");
        for (JsonNode t : api.get("/api/superadmin/tenants", sa).corps()) if ("essai-banque".equals(t.get("slug").asText())) tenant = t.get("id").asLong();
    }

    private String url() { return "/api/superadmin/tenants/" + tenant + "/banques-heures"; }

    @Test
    void la_banque_s_ouvre_se_consomme_par_categorie_et_se_consulte() throws Exception {
        Api.Reponse b = api.post(url(), sa, Map.of("nom", "Soutien 2026", "debut", "2026-01-01", "fin", "2026-12-31", "heures", 40));
        assertEquals(200, b.statut(), b.texte());
        long id = b.id();
        Api.Reponse c = api.post(url() + "/" + id + "/mouvements", sa, Map.of("type", "CONSOMMATION", "categorie", "FORMATION", "heures", 3.5,
                "date", "2026-11-20", "description", "Formation des agents", "intervenant", "Julie Côté"));
        assertEquals(200, c.statut(), c.texte());
        assertEquals(36.5, c.corps().get("solde").get("restantes").asDouble());

        JsonNode vue = api.get("/api/soutien/banque-heures/" + id, admin).corps();
        assertEquals(3.5, vue.get("solde").get("parCategorie").get("FORMATION").asDouble());
        assertEquals(2, vue.get("mouvements").size());
        String releve = api.get("/api/soutien/banque-heures/" + id + "/releve.csv", admin).texte();
        assertTrue(releve.contains("\"Formation des agents\""), releve);
    }

    @Test
    void une_consommation_au_dela_du_solde_ou_hors_periode_est_refusee() throws Exception {
        long id = api.post(url(), sa, Map.of("nom", "Petite banque", "debut", "2026-01-01", "fin", "2026-12-31", "heures", 2)).id();
        Api.Reponse trop = api.post(url() + "/" + id + "/mouvements", sa, Map.of("type", "CONSOMMATION", "categorie", "ANALYSE", "heures", 2.25,
                "date", "2026-06-01", "description", "Analyse", "intervenant", "X"));
        assertEquals(409, trop.statut());
        assertTrue(trop.corps().get("message").asText().contains("Solde insuffisant"));
        assertEquals(400, api.post(url() + "/" + id + "/mouvements", sa, Map.of("type", "CONSOMMATION", "categorie", "ANALYSE", "heures", 1,
                "date", "2027-01-05", "description", "Analyse", "intervenant", "X")).statut());
        assertEquals(400, api.post(url() + "/" + id + "/mouvements", sa, Map.of("type", "CONSOMMATION", "categorie", "ANALYSE", "heures", 0.1,
                "date", "2026-06-01", "description", "Analyse", "intervenant", "X")).statut());
        assertEquals(400, api.post(url() + "/" + id + "/mouvements", sa, Map.of("type", "CORRECTION", "categorie", "RAPPORTS", "heures", 1,
                "date", "2026-06-01", "description", "Erreur")).statut());
    }

    @Test
    void seul_le_fournisseur_tient_la_banque_et_chaque_organisation_ne_voit_que_la_sienne() throws Exception {
        long id = api.post(url(), sa, Map.of("nom", "Soutien", "debut", "2026-01-01", "fin", "2026-12-31", "heures", 10)).id();
        assertEquals(403, api.post(url(), admin, Map.of("nom", "Auto", "debut", "2026-01-01", "fin", "2026-12-31", "heures", 999)).statut());
        assertEquals(403, api.post(url() + "/" + id + "/mouvements", admin, Map.of("type", "ACHAT", "heures", 100, "date", "2026-06-01",
                "description", "gratuit")).statut());
        Cookie autre = api.organisation("essai-banque-b");
        assertEquals(404, api.get("/api/soutien/banque-heures/" + id, autre).statut());
        assertEquals(0, api.get("/api/soutien/banque-heures", autre).corps().size());
        assertEquals(403, api.get("/api/soutien/banque-heures", api.connecter("redacteur@levis.qc.ca")).statut());
        assertEquals(404, api.post("/api/superadmin/tenants/999999/banques-heures", sa, Map.of("nom", "X", "debut", "2026-01-01",
                "fin", "2026-12-31", "heures", 1)).statut());
    }
}
