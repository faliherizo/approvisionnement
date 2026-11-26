package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * RST-52 (TI-21) : découverte des droits par SCIM 2.0. Un compte provisionné expose, en lecture, ses rôles effectifs de la
 * plateforme (« roles ») et ses groupes du module CLM (« entitlements »), au format RFC 7643 ; le schéma publié les
 * déclare en lecture seule, et un PATCH ne peut pas les modifier.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class DecouverteDroitsScimIntegrationTest {

    private static final String PW = "Passw0rd!";
    private static final String USER = "urn:ietf:params:scim:schemas:core:2.0:User";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie admin;
    private String jeton;

    @BeforeEach
    void preparer() throws Exception {
        admin = login("admin.ti@iq.example");
        jeton = corps(appel("POST", "/api/admin/annuaire/scim/jeton", admin, "{}"), 200).get("jeton").asText();
    }

    @Test
    void un_compte_provisionne_expose_ses_roles_effectifs_et_ses_groupes_clm() throws Exception {
        corps(appel("POST", "/api/admin/annuaire/correspondances", admin,
                "{\"source\":\"GROUPE\",\"valeur\":\"aad-droits-rst52\",\"role\":\"JURIDIQUE\",\"groupe\":\"conformite_rst52\"}"), 201);
        String uid = corps(scim("POST", "/scim/v2/Users", "{\"userName\":\"droits.rst52@iq.example\",\"displayName\":\"Droits\"}"), 201)
                .get("id").asText();
        JsonNode cree = corps(scim("GET", "/scim/v2/Users/" + uid, null), 200);
        assertEquals(List.of("LECTEUR"), valeurs(cree.get("roles")), "moindre privilège avant tout groupe");
        assertEquals(0, cree.get("entitlements").size());

        corps(scim("POST", "/scim/v2/Groups", "{\"displayName\":\"Droits RST-52\",\"externalId\":\"aad-droits-rst52\","
                + "\"members\":[{\"value\":\"" + uid + "\"}]}"), 201);
        long groupe = corps(appel("POST", "/api/clm/admin/groupes", admin, "{\"code\":\"tresorerie_rst52\",\"libelle\":\"Trésorerie\"}"), 201)
                .get("id").asLong();
        corps(appel("POST", "/api/clm/admin/groupes/" + groupe + "/membres", admin, "{\"courriel\":\"droits.rst52@iq.example\"}"), 200);

        JsonNode u = corps(scim("GET", "/scim/v2/Users/" + uid, null), 200);
        assertEquals(List.of("JURIDIQUE"), valeurs(u.get("roles")), "rôle effectif donné par le groupe d'annuaire");
        assertEquals("plateforme", u.at("/roles/0/type").asText());
        assertEquals(List.of("clm:CONFORMITE_RST52", "clm:TRESORERIE_RST52"), valeurs(u.get("entitlements")),
                "groupes du module CLM : piloté par l'annuaire et manuel");
        assertEquals("Trésorerie", u.at("/entitlements/1/display").asText());
        assertEquals("clm", u.at("/entitlements/1/type").asText());

        JsonNode liste = corps(scim("GET", "/scim/v2/Users?filter=userName eq \"droits.rst52@iq.example\"", null), 200);
        assertEquals(u.get("entitlements"), liste.at("/Resources/0/entitlements"), "la liste expose les mêmes droits");

        JsonNode patche = corps(scim("PATCH", "/scim/v2/Users/" + uid,
                "{\"Operations\":[{\"op\":\"replace\",\"path\":\"roles\",\"value\":[{\"value\":\"ADMIN\"}]}]}"), 200);
        assertEquals(List.of("JURIDIQUE"), valeurs(patche.get("roles")), "lecture seule : SCIM ne donne aucun rôle");
    }

    @Test
    void le_schema_user_declare_roles_et_entitlements_en_lecture_seule() throws Exception {
        assertEquals(401, mvc.perform(MockMvcRequestBuilders.get("/scim/v2/Schemas").header("Authorization", "Bearer inconnu"))
                .andReturn().getResponse().getStatus());
        JsonNode schemas = corps(scim("GET", "/scim/v2/Schemas", null), 200);
        assertEquals(1, schemas.get("totalResults").asInt());
        JsonNode user = corps(scim("GET", "/scim/v2/Schemas/" + USER, null), 200);
        assertEquals(USER, user.get("id").asText());
        assertEquals("readOnly", attribut(user, "roles").get("mutability").asText());
        assertEquals("readOnly", attribut(user, "entitlements").get("mutability").asText());
        assertTrue(attribut(user, "userName").get("required").asBoolean());
        assertEquals("boolean", attribut(user, "active").get("type").asText());
        assertEquals(404, scim("GET", "/scim/v2/Schemas/urn:inconnu", null).getStatus());
    }

    private static JsonNode attribut(JsonNode schema, String nom) {
        for (JsonNode a : schema.get("attributes")) {
            if (nom.equals(a.get("name").asText())) {
                return a;
            }
        }
        throw new AssertionError("attribut absent : " + nom);
    }

    private static List<String> valeurs(JsonNode multiple) {
        List<String> v = new ArrayList<>();
        multiple.forEach(n -> v.add(n.get("value").asText()));
        return v;
    }

    private MockHttpServletResponse scim(String methode, String url, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url)
                .header("Authorization", "Bearer " + jeton);
        if (corps != null) {
            r.contentType("application/scim+json").content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
    }

    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private MockHttpServletResponse appel(String methode, String url, Cookie qui, String corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(HttpMethod.valueOf(methode), url).cookie(qui);
        if (corps != null) {
            r.contentType(MediaType.APPLICATION_JSON).content(corps);
        }
        return mvc.perform(r).andReturn().getResponse();
    }

    private JsonNode corps(MockHttpServletResponse r, int attendu) throws Exception {
        String s = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(attendu, r.getStatus(), s);
        return json.readTree(s);
    }
}
