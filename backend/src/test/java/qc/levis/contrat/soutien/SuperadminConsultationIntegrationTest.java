package qc.levis.contrat.soutien;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 9.05 : le fournisseur consulte les banques d'une organisation, avec leur relevé. */
@SpringBootTest
@AutoConfigureMockMvc
class SuperadminConsultationIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void le_fournisseur_liste_et_consulte_les_banques_d_une_organisation() throws Exception {
        Api api = new Api(mvc, json);
        api.organisation("essai-banque-sa");
        Cookie sa = api.connecter("superadmin@plateforme.ca", "SuperPassw0rd!");
        long tenant = 0;
        for (JsonNode t : api.get("/api/superadmin/tenants", sa).corps()) if ("essai-banque-sa".equals(t.get("slug").asText())) tenant = t.get("id").asLong();
        String url = "/api/superadmin/tenants/" + tenant + "/banques-heures";
        long id = api.post(url, sa, Map.of("nom", "Soutien", "debut", "2026-01-01", "fin", "2026-12-31", "heures", 8)).id();
        assertEquals(1, api.get(url, sa).corps().size());
        assertEquals(0, api.get(url, sa).corps().get(0).get("mouvements").size(), "la liste ne charge pas les relevés");
        assertEquals(1, api.get(url + "/" + id, sa).corps().get("mouvements").size());
        assertEquals(404, api.get("/api/superadmin/tenants/999999/banques-heures/" + id, sa).statut());
    }
}
