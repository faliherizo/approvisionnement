package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import qc.levis.contrat.securite.CartographieDesRoutes.Motif;
import qc.levis.contrat.securite.CartographieDesRoutes.Route;

/**
 * SEC-33 : CHAQUE opération accessible sans session (surface-publique.json, confrontée aux routes réelles) est soumise à
 * la limitation de débit par client ; un X-Forwarded-For forgé derrière le frontal ne la contourne pas.
 */
@SpringBootTest(properties = "app.securite.pare-feu.max-par-minute=3")
@AutoConfigureMockMvc
class PareFeuSurfacePubliqueIntegrationTest {

    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping correspondances;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private int appeler(Route r, String distante, String transmise) throws Exception {
        var req = MockMvcRequestBuilders.request(HttpMethod.valueOf(r.methode()), r.url("1")).with(q -> {
            q.setRemoteAddr(distante);
            return q;
        });
        if (transmise != null) req = req.header("X-Forwarded-For", transmise);
        return mvc.perform(req).andReturn().getResponse().getStatus();
    }

    @Test
    void chaque_operation_publique_repond_429_au_dela_du_seuil() throws Exception {
        List<Motif> publiques = CartographieDesRoutes.motifs(CartographieDesRoutes.registre(json, "surface-publique.json"), "operations");
        List<Route> routes = CartographieDesRoutes.routes(correspondances).stream()
                .filter(r -> CartographieDesRoutes.premier(publiques, r).isPresent()).toList();
        List<String> ecarts = new ArrayList<>();
        int n = 0;
        for (Route r : routes) {
            // Une adresse publique distincte par opération : les familles partagent un compteur par client.
            String client = "198.18." + (n / 250) + "." + (n % 250 + 1);
            n++;
            for (int i = 0; i < 3; i++) appeler(r, client, null);
            int statut = appeler(r, client, null);
            if (statut != 429) ecarts.add(r.cle() + " → " + statut);
        }
        assertTrue(routes.size() > 40, "surface publique anormalement courte : " + routes.size());
        assertEquals(List.of(), ecarts, "opérations publiques sans limitation de débit");
    }

    @Test
    void un_x_forwarded_for_forge_derriere_le_frontal_ne_contourne_pas_la_limite() throws Exception {
        Route health = CartographieDesRoutes.routes(correspondances).stream().filter(r -> r.cle().equals("GET /api/health")).findFirst().orElseThrow();
        for (int i = 0; i < 3; i++) appeler(health, "172.18.0.3", "203.0.113." + i + ", 198.19.0.1");
        assertEquals(429, appeler(health, "172.18.0.3", "8.8.8.8, 198.19.0.1"));
        assertEquals(200, appeler(health, "172.18.0.3", "198.19.0.2"), "un autre client n'est pas pénalisé");
    }
}
