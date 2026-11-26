package qc.levis.contrat.securite.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** SEC-33 : limitation de débit par adresse du client et par famille de routes publiques. */
class PareFeuFilterTest {

    private final AtomicLong maintenant = new AtomicLong(1_000_000L);
    private final PareFeuFilter filtre = new PareFeuFilter(2, List.of(" /api/clm/portail/", "/api/public/", " "), maintenant::get);

    private MockHttpServletResponse appeler(String uri, String distante, String transmise) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRemoteAddr(distante);
        if (transmise != null) req.addHeader("X-Forwarded-For", transmise);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtre.doFilter(req, res, new MockFilterChain());
        return res;
    }

    @Test
    void au_dela_du_seuil_la_famille_repond_429_avec_un_delai_de_reprise() throws Exception {
        assertEquals(200, appeler("/api/clm/portail/abc", "203.0.113.7", null).getStatus());
        MockHttpServletResponse deuxieme = appeler("/api/clm/portail/def", "203.0.113.7", null);
        assertEquals("2", deuxieme.getHeader("RateLimit-Limit"));
        MockHttpServletResponse refus = appeler("/api/clm/portail/ghi", "203.0.113.7", null);
        assertEquals(429, refus.getStatus());
        assertEquals("60", refus.getHeader("Retry-After"));
        assertEquals("{\"erreur\":\"Trop de requêtes. Réessayez plus tard.\"}", refus.getContentAsString());
    }

    @Test
    void chaque_famille_et_chaque_client_ont_leur_propre_compteur_et_la_fenetre_se_renouvelle() throws Exception {
        appeler("/api/clm/portail/a", "203.0.113.8", null);
        appeler("/api/clm/portail/a", "203.0.113.8", null);
        assertEquals(200, appeler("/api/public/v1/x", "203.0.113.8", null).getStatus(), "autre famille");
        assertEquals(200, appeler("/api/clm/portail/a", "203.0.113.9", null).getStatus(), "autre client");
        maintenant.addAndGet(PareFeuFilter.FENETRE_MS + 1);
        assertEquals(200, appeler("/api/clm/portail/a", "203.0.113.8", null).getStatus(), "fenêtre renouvelée");
    }

    @Test
    void un_x_forwarded_for_forge_ne_contourne_pas_la_limite() throws Exception {
        for (int i = 0; i < 2; i++) appeler("/api/public/v1/x", "172.18.0.3", "1.1.1." + i + ", 203.0.113.50");
        assertEquals(429, appeler("/api/public/v1/x", "172.18.0.3", "8.8.8.8, 203.0.113.50").getStatus());
    }

    @Test
    void une_route_hors_des_familles_n_est_pas_limitee() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse r = appeler("/api/clauses", "203.0.113.10", null);
            assertEquals(200, r.getStatus());
            assertNull(r.getHeader("RateLimit-Limit"));
        }
    }

    @Test
    void une_requete_sans_adresse_n_est_pas_limitee() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest() {
            @Override public String getRequestURI() { return null; }
        };
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtre.doFilter(req, res, new MockFilterChain());
        assertEquals(200, res.getStatus());
    }
}
