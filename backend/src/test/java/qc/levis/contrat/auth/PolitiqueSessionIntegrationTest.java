package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.auth.application.JwtService;

/**
 * ST-01, sur la vraie pile : l'administration technique d'IQ fixe sa politique de session ; une session inactive ou
 * au-delà de sa durée maximale est refusée ; une session active est renouvelée sans dépasser la durée maximale.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PolitiqueSessionIntegrationTest {

    private static final String PW = "Passw0rd!";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Value("${app.jwt.secret}") String secret;

    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + PW + "\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private MockHttpServletResponse appel(String methode, String url, Cookie c) throws Exception {
        var r = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(methode), url);
        return mvc.perform(c == null ? r : r.cookie(c)).andReturn().getResponse();
    }

    /** Jeton forgé avec la clé du serveur : émis et ouvert aux instants voulus, comme le ferait une session réelle. */
    private Cookie jeton(Long tenant, Instant emis, Instant debut) {
        var b = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject("admin.technique@iq.example")
                .addClaims(Map.of("roles", List.of("ADMIN_TECHNIQUE"), "tenant", tenant))
                .setIssuedAt(Date.from(emis)).setExpiration(Date.from(emis.plusSeconds(86_400)));
        if (debut != null) b.claim(JwtService.DEBUT_SESSION, debut.getEpochSecond());
        return new Cookie("ACCESS_TOKEN", b.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256).compact());
    }

    @Autowired qc.levis.contrat.auth.application.repository.UserRepository utilisateurs;

    private long tenantIq() {
        return utilisateurs.findByEmail("admin.technique@iq.example").orElseThrow().getTenantId();
    }

    @Test
    void l_administration_technique_fixe_la_politique_et_les_autres_ne_peuvent_pas() throws Exception {
        Cookie tech = login("admin.technique@iq.example");
        JsonNode avant = json.readTree(appel("GET", "/api/admin/session", tech).getContentAsString());
        assertTrue(avant.get("inactiviteMinutes").asLong() > 0);
        MockHttpServletResponse ok = appel("PUT", "/api/admin/session?inactiviteMinutes=20&dureeMaxMinutes=600", tech);
        assertEquals(200, ok.getStatus(), ok.getContentAsString());
        JsonNode apres = json.readTree(ok.getContentAsString());
        assertTrue(apres.get("propre").asBoolean());
        assertEquals(20, apres.get("inactiviteMinutes").asLong());
        assertEquals("admin.technique@iq.example", apres.get("modifiePar").asText());
        assertEquals(400, appel("PUT", "/api/admin/session?inactiviteMinutes=1&dureeMaxMinutes=600", tech).getStatus());
        assertEquals(400, appel("PUT", "/api/admin/session?inactiviteMinutes=60&dureeMaxMinutes=30", tech).getStatus());
        assertEquals(403, appel("GET", "/api/admin/session", login("admin.affaires@iq.example")).getStatus(),
                "l'administration d'affaires n'y a pas accès (séparation, ADR-017)");
        assertEquals(200, appel("GET", "/api/admin/session", login("admin.ti@iq.example")).getStatus(),
                "l'administration principale l'englobe (hiérarchie de rôles, ADR-017)");
        assertEquals(401, appel("GET", "/api/admin/session", null).getStatus());
    }

    @Test
    void une_session_inactive_ou_trop_longue_est_refusee_une_session_active_est_renouvelee() throws Exception {
        long iq = tenantIq();
        Cookie tech = login("admin.technique@iq.example");
        assertEquals(200, appel("PUT", "/api/admin/session?inactiviteMinutes=30&dureeMaxMinutes=120", tech).getStatus());
        Instant maintenant = Instant.now();

        assertEquals(401, appel("GET", "/api/admin/session", jeton(iq, maintenant.minusSeconds(31 * 60), null)).getStatus(), "inactive depuis 31 min");
        assertEquals(401, appel("GET", "/api/admin/session", jeton(iq, maintenant.minusSeconds(120), maintenant.minusSeconds(121 * 60))).getStatus(),
                "au-delà de 2 h depuis la connexion, même active");

        Instant debut = maintenant.minusSeconds(20 * 60);
        MockHttpServletResponse active = appel("GET", "/api/admin/session", jeton(iq, maintenant.minusSeconds(5 * 60), debut));
        assertEquals(200, active.getStatus());
        Cookie renouvele = active.getCookie("ACCESS_TOKEN");
        assertNotNull(renouvele, "session active : cookie renouvelé");
        assertTrue(renouvele.isHttpOnly());
        assertTrue(renouvele.getMaxAge() <= 30 * 60 && renouvele.getMaxAge() > 25 * 60, "validité = délai d'inactivité");
        var revendications = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(renouvele.getValue()).getBody();
        assertEquals(debut.getEpochSecond(), revendications.get(JwtService.DEBUT_SESSION, Number.class).longValue(), "le début de session est conservé");

        MockHttpServletResponse recent = appel("GET", "/api/admin/session", jeton(iq, maintenant.minusSeconds(10), debut));
        assertNull(recent.getCookie("ACCESS_TOKEN"), "un jeton de moins d'une minute n'est pas réémis");

        MockHttpServletResponse finProche = appel("GET", "/api/admin/session", jeton(iq, maintenant.minusSeconds(5 * 60), maintenant.minusSeconds(110 * 60)));
        assertTrue(finProche.getCookie("ACCESS_TOKEN").getMaxAge() <= 10 * 60, "jamais au-delà de la durée maximale");

        MockHttpServletResponse bearer = mvc.perform(MockMvcRequestBuilders.get("/api/admin/session")
                .header("Authorization", "Bearer " + jeton(iq, maintenant.minusSeconds(5 * 60), debut).getValue())).andReturn().getResponse();
        assertEquals(200, bearer.getStatus());
        assertNull(bearer.getCookie("ACCESS_TOKEN"), "un client par en-tête n'a pas de cookie à renouveler");
    }
}
