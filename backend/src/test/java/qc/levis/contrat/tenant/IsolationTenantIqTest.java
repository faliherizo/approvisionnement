package qc.levis.contrat.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

/**
 * Isolation du tenant Investissement Québec (AO 2026-6370-0005) sur la vraie pile : organisation et comptes amorcés, modules
 * souscrits (CLM et son option IA seulement), refus 403 de toute fonctionnalité d'un autre module — socle de rédaction compris —,
 * fonctions transverses conservées et Ville de Lévis inchangée.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.clm.planificateur.actif=false")
class IsolationTenantIqTest {

    private static final String MOT_DE_PASSE = "Passw0rd!";

    /** Profil du client → courriel du compte amorcé et rôle attendu. */
    private static final Map<String, String[]> PROFILS = Map.of(
            "Initiateur", new String[]{"initiateur@iq.example", "REDACTEUR"},
            "Validateur", new String[]{"validateur@iq.example", "JURIDIQUE"},
            "Approbateur", new String[]{"approbateur@iq.example", "DIRECTION"},
            "Signataire interne", new String[]{"signataire@iq.example", "LECTEUR"},
            "Administrateur d'affaires", new String[]{"admin.affaires@iq.example", "ADMIN_AFFAIRES"},
            "Administrateur technique", new String[]{"admin.technique@iq.example", "ADMIN_TECHNIQUE"},
            "Administrateur principal", new String[]{"admin.ti@iq.example", "ADMIN"},
            "Auditeur / conformité", new String[]{"auditeur@iq.example", "AUDITEUR"});

    /** Une lecture représentative par fonctionnalité du socle de rédaction contractuelle (module « redaction »). */
    private static final List<String> SOCLE = List.of("/api/dossiers", "/api/documents", "/api/clauses", "/api/templates",
            "/api/gabarits", "/api/modele-document", "/api/workflows", "/api/stats", "/api/rapports/suivi", "/api/search?q=contrat",
            "/api/ia/knowledge", "/api/ia/parametres", "/api/simulateur/depots", "/api/profil/repertoires",
            "/api/reference/TYPE_CONTRAT", "/api/admin/reference?categorie=TYPE_CONTRAT", "/api/parametres/integration-m365",
            "/api/parametres/integration-sap", "/api/reversibilite/export");

    /** Une lecture par autre module de la plateforme ; chacun est gardé par sa propre activation. */
    private static final List<String> AUTRES_MODULES = List.of("/api/formulaires", "/api/catalogue", "/api/mes-demandes",
            "/api/taches/mine", "/api/approbations/demandes", "/api/notification/modeles", "/api/rapport/criteres",
            "/api/interop/clients", "/api/applications", "/api/securite/groupes", "/api/rh/employes", "/api/rh/feuilles-temps/mine",
            "/api/rh/projets", "/api/paie/taux");

    /** Fonctions transverses dont tout tenant a besoin : session, profil, modules, comptes, audit, authentification unique. */
    private static final List<String> TRANSVERSES = List.of("/api/auth/me", "/api/profil", "/api/modules", "/api/admin/users",
            "/api/admin/users/roles", "/api/audit", "/api/parametres/integration-oidc", "/api/parametres/integration-saml");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TenantRepository tenants;
    @Autowired UserRepository users;

    @Test
    void l_organisation_iq_est_amorcee_avec_un_compte_par_profil_du_client() throws Exception {
        assertEquals("Investissement Québec", tenants.findBySlug("iq").orElseThrow().getTitre());
        Cookie admin = login("admin.ti@iq.example");
        Map<String, Set<String>> roles = new HashMap<>();
        for (JsonNode u : lire("/api/admin/users?q=&page=0&taille=50", admin).get("contenu")) {
            Set<String> r = new HashSet<>();
            u.get("roles").forEach(x -> r.add(x.asText()));
            roles.put(u.get("email").asText(), r);
        }
        PROFILS.forEach((profil, compte) -> assertEquals(Set.of(compte[1]), roles.get(compte[0]), profil));
        assertTrue(roles.keySet().stream().allMatch(e -> e.endsWith("@iq.example")), "aucun compte d'une autre organisation : " + roles.keySet());
        assertTrue(lire("/api/admin/users/roles", admin).toString().contains("\"AUDITEUR\""), "le rôle Auditeur est assignable");
    }

    @Test
    void chaque_profil_iq_n_a_que_le_module_clm_et_son_option() throws Exception {
        for (String[] compte : PROFILS.values()) {
            Set<String> actifs = new HashSet<>();
            for (JsonNode m : lire("/api/modules", login(compte[0]))) {
                if (m.get("actif").asBoolean()) {
                    actifs.add(m.get("code").asText());
                }
            }
            assertEquals(Set.of("clm", "clm_ia"), actifs, compte[0]);
        }
    }

    @Test
    void le_socle_et_les_autres_modules_sont_refuses_meme_a_l_administrateur_iq() throws Exception {
        Cookie admin = login("admin.ti@iq.example");
        for (String url : SOCLE) {
            MockHttpServletResponse r = appel(MockMvcRequestBuilders.get(url), admin);
            assertEquals(403, r.getStatus(), url);
            assertTrue(r.getContentAsString(StandardCharsets.UTF_8).contains("Fonctionnalité non souscrite"), url);
        }
        for (String url : AUTRES_MODULES) {
            assertEquals(403, appel(MockMvcRequestBuilders.get(url), admin).getStatus(), url);
        }
        assertEquals(403, appel(MockMvcRequestBuilders.post("/api/assistant/analyser").contentType(MediaType.APPLICATION_JSON)
                .content("{}"), admin).getStatus(), "assistant guidé du socle");
        for (String url : TRANSVERSES) {
            assertEquals(200, appel(MockMvcRequestBuilders.get(url), admin).getStatus(), url);
        }
        assertEquals(200, appel(MockMvcRequestBuilders.get("/api/clm/trousses"), admin).getStatus(), "le module souscrit répond");
    }

    @Test
    void la_ville_de_levis_conserve_son_socle() throws Exception {
        Cookie admin = login("admin@levis.qc.ca");
        for (String url : SOCLE) {
            assertEquals(200, appel(MockMvcRequestBuilders.get(url), admin).getStatus(), url);
        }
    }

    @Test
    void le_superadmin_gere_le_socle_par_organisation_comme_tout_module() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        Cookie admin = login("admin.ti@iq.example");
        String modules = "/api/superadmin/tenants/" + tenants.findBySlug("iq").orElseThrow().getId() + "/modules/redaction/";
        try {
            assertEquals(200, appel(MockMvcRequestBuilders.put(modules + "activer"), sa).getStatus());
            assertEquals(200, appel(MockMvcRequestBuilders.get("/api/dossiers"), admin).getStatus(), "socle souscrit");
        } finally {
            assertEquals(200, appel(MockMvcRequestBuilders.put(modules + "desactiver"), sa).getStatus());
        }
        assertEquals(403, appel(MockMvcRequestBuilders.get("/api/dossiers"), admin).getStatus(), "socle retiré");
        assertEquals(403, appel(MockMvcRequestBuilders.put(modules + "activer"), admin).getStatus(), "réservé au superadmin");
    }

    @Test
    void l_administrateur_iq_ne_gere_que_les_comptes_de_son_organisation() throws Exception {
        Cookie admin = login("admin.ti@iq.example");
        long compteLevis = users.findByEmail("redacteur@levis.qc.ca").orElseThrow().getId();
        Set<String> avant = roles("redacteur@levis.qc.ca");
        String prise = "{\"email\":\"redacteur@levis.qc.ca\",\"nom\":\"Prise de contrôle\",\"roles\":[\"ADMIN\"]}";
        assertEquals(404, appel(MockMvcRequestBuilders.put("/api/admin/users/" + compteLevis).contentType(MediaType.APPLICATION_JSON)
                .content(prise), admin).getStatus(), "un compte d'une autre organisation n'existe pas pour IQ");
        assertEquals(404, appel(MockMvcRequestBuilders.delete("/api/admin/users/" + compteLevis), admin).getStatus());
        assertEquals(404, appel(MockMvcRequestBuilders.delete("/api/admin/users/999999"), admin).getStatus());
        assertEquals(400, appel(MockMvcRequestBuilders.post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                .content(prise), admin).getStatus(), "un courriel déjà pris n'est pas réattribué");
        assertEquals(avant, roles("redacteur@levis.qc.ca"), "le compte de Lévis est intact");
        assertFalse(avant.contains("ADMIN"));
    }

    private Set<String> roles(String email) {
        Set<String> r = new HashSet<>();
        users.findByEmail(email).orElseThrow().getRoles().forEach(x -> r.add(x.name()));
        return r;
    }

    @Test
    void la_liste_publique_des_organisations_presente_iq() throws Exception {
        List<String> slugs = new ArrayList<>();
        lire("/api/tenants", null).forEach(t -> slugs.add(t.get("slug").asText()));
        assertTrue(slugs.contains("iq"), slugs.toString());
        assertFalse(lire("/api/tenants/iq/public", null).get("titre").asText().isBlank());
    }

    private Cookie login(String email) throws Exception {
        return login(email, MOT_DE_PASSE);
    }

    private Cookie login(String email, String motDePasse) throws Exception {
        Cookie c = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "motDePasse", motDePasse))))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
        assertTrue(c != null, "connexion de " + email);
        return c;
    }

    private MockHttpServletResponse appel(MockHttpServletRequestBuilder requete, Cookie qui) throws Exception {
        return mvc.perform(qui == null ? requete : requete.cookie(qui)).andReturn().getResponse();
    }

    private JsonNode lire(String url, Cookie qui) throws Exception {
        MockHttpServletResponse r = appel(MockMvcRequestBuilders.get(url), qui);
        assertEquals(200, r.getStatus(), url);
        return json.readTree(r.getContentAsString(StandardCharsets.UTF_8));
    }
}
