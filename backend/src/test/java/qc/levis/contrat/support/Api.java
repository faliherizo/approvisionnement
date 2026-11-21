package qc.levis.contrat.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Client d'API pour les tests d'intégration : connexion par cookie, appel, statut et corps JSON. */
public final class Api {

    public static final String PW = "Passw0rd!";

    private final MockMvc mvc;
    private final ObjectMapper json;

    public Api(MockMvc mvc, ObjectMapper json) {
        this.mvc = mvc;
        this.json = json;
    }

    /** Réponse d'un appel : statut, corps brut et corps JSON (nœud vide si le corps n'est pas du JSON). */
    public record Reponse(int statut, byte[] octets, String texte, JsonNode corps) {
        public long id() { return corps.get("id").asLong(); }
    }

    public Cookie connecter(String courriel, String motDePasse) throws Exception {
        String corps = json.writeValueAsString(java.util.Map.of("email", courriel, "motDePasse", motDePasse));
        Cookie c = mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
        if (c == null) throw new IllegalStateException("Connexion refusée pour " + courriel);
        return c;
    }

    public Cookie connecter(String courriel) throws Exception {
        return connecter(courriel, PW);
    }

    /**
     * Organisation d'essai (créée au besoin par le superadministrateur, modules activés) et connexion de son administrateur.
     * Sert à éprouver l'isolation entre organisations : rien de ce qu'une organisation crée ne doit être visible d'une autre.
     */
    public Cookie organisation(String slug, String... modules) throws Exception {
        Cookie sa = connecter("superadmin@plateforme.ca", "SuperPassw0rd!");
        JsonNode existante = null;
        for (JsonNode t : get("/api/superadmin/tenants", sa).corps()) if (slug.equals(t.get("slug").asText())) existante = t;
        String admin = "admin@" + slug + ".example";
        long id;
        if (existante == null) {
            id = post("/api/superadmin/tenants", sa, java.util.Map.of("slug", slug, "titre", "Essai " + slug, "sousTitre", "Tests")).id();
            post("/api/superadmin/tenants/" + id + "/admin", sa, java.util.Map.of("email", admin, "motDePasse", PW + "x1", "nom", "Admin " + slug));
        } else {
            id = existante.get("id").asLong();
        }
        for (String m : modules) put("/api/superadmin/tenants/" + id + "/modules/" + m + "/activer", sa, null);
        return connecter(admin, PW + "x1");
    }

    public Reponse get(String url, Cookie c) throws Exception { return appel(HttpMethod.GET, url, c, null); }
    public Reponse post(String url, Cookie c, Object corps) throws Exception { return appel(HttpMethod.POST, url, c, corps); }
    public Reponse put(String url, Cookie c, Object corps) throws Exception { return appel(HttpMethod.PUT, url, c, corps); }
    public Reponse delete(String url, Cookie c) throws Exception { return appel(HttpMethod.DELETE, url, c, null); }

    /** Le corps est sérialisé en JSON, sauf une chaîne, transmise telle quelle. */
    public Reponse appel(HttpMethod methode, String url, Cookie c, Object corps) throws Exception {
        MockHttpServletRequestBuilder r = MockMvcRequestBuilders.request(methode, url);
        if (c != null) r = r.cookie(c);
        if (corps != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corps instanceof String s ? s : json.writeValueAsString(corps));
        }
        MockHttpServletResponse rep = mvc.perform(r).andReturn().getResponse();
        byte[] octets = rep.getContentAsByteArray();
        String texte = new String(octets, java.nio.charset.StandardCharsets.UTF_8);
        JsonNode noeud;
        try {
            noeud = texte.isBlank() ? json.createObjectNode() : json.readTree(texte);
        } catch (Exception pasDuJson) {
            noeud = json.createObjectNode();
        }
        return new Reponse(rep.getStatus(), octets, texte, noeud);
    }
}
