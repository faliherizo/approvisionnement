package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;

/**
 * Service de clés Azure Key Vault (profil azure-kms, ADR-037) contre un coffre simulé qui applique réellement RSA-OAEP-256 :
 * contrat du port, jeton Entra ID mis en cache, refus d'un identifiant de clé hors du coffre sans aucun appel.
 */
class GestionnaireClesAzureKeyVaultTest extends GestionnaireClesContrat {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private HttpServer serveur;
    private String coffre;
    private KeyPair rsa;
    private final AtomicInteger jetons = new AtomicInteger();
    private final AtomicInteger appels = new AtomicInteger();
    private GestionnaireClesAzureKeyVault sujet;

    @BeforeEach
    void demarrer() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        rsa = g.generateKeyPair();
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        coffre = "http://127.0.0.1:" + serveur.getAddress().getPort();
        serveur.createContext("/", this::repondre);
        serveur.start();
        sujet = new GestionnaireClesAzureKeyVault(proprietes(coffre + "/"), JSON, Clock.systemUTC());
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    @Override
    GestionnaireCles sujet() {
        return sujet;
    }

    @Override
    String cleMaitresseInconnue() {
        return coffre + "/keys/cle/v9";
    }

    @Override
    String depuisUrl(String base64Url) {
        return base64Url;
    }

    @Test
    void la_version_active_est_celle_du_coffre_et_le_jeton_se_reutilise() {
        assertEquals(coffre + "/keys/cle/v1", sujet.cleMaitresseActive());
        sujet.desenvelopper(sujet.envelopper(CLE));
        assertEquals(1, jetons.get(), "un seul jeton Entra ID pour plusieurs appels");
    }

    @Test
    void un_identifiant_hors_du_coffre_est_refuse_sans_appel() {
        int avant = appels.get();
        assertThrows(GestionnaireCles.CleIllisible.class,
                () -> sujet.desenvelopper(new CleEnveloppee("https://attaquant.example/keys/cle/v1", "AAAA")));
        assertEquals(avant, appels.get(), "aucune requête vers un hôte désigné par la base");
    }

    @Test
    void une_configuration_incomplete_empeche_le_demarrage() {
        assertThrows(IllegalStateException.class, () -> new GestionnaireClesAzureKeyVault(proprietes(null), JSON, Clock.systemUTC()));
        assertThrows(IllegalStateException.class, () -> new GestionnaireClesAzureKeyVault(proprietes(" "), JSON, Clock.systemUTC()));
        ChiffrementProprietes sansCle = new ChiffrementProprietes(null, null, null,
                new ChiffrementProprietes.Azure(coffre, " ", "t1", "c", "s", coffre));
        assertThrows(IllegalStateException.class, () -> new GestionnaireClesAzureKeyVault(sansCle, JSON, Clock.systemUTC()));
        ChiffrementProprietes cleNulle = new ChiffrementProprietes(null, null, null,
                new ChiffrementProprietes.Azure(coffre, null, "t1", "c", "s", coffre));
        assertThrows(IllegalStateException.class, () -> new GestionnaireClesAzureKeyVault(cleNulle, JSON, Clock.systemUTC()));
    }

    @Test
    void un_jeton_refuse_rend_la_cle_illisible() {
        GestionnaireClesAzureKeyVault refuse = new GestionnaireClesAzureKeyVault(new ChiffrementProprietes(null, null, null,
                new ChiffrementProprietes.Azure(coffre, "cle", "refuse", "c", "s", coffre)), JSON, Clock.systemUTC());
        CleEnveloppee e = sujet.envelopper(CLE);
        assertThrows(GestionnaireCles.CleIllisible.class, () -> refuse.desenvelopper(e));
        assertThrows(ClientEntra.Refus.class, () -> refuse.envelopper(CLE));
    }

    private ChiffrementProprietes proprietes(String url) {
        return new ChiffrementProprietes(null, null, null, new ChiffrementProprietes.Azure(url, "cle", "t1", "client", "secret", coffre));
    }

    private void repondre(HttpExchange x) throws IOException {
        appels.incrementAndGet();
        String chemin = x.getRequestURI().getPath();
        if (chemin.endsWith("/oauth2/v2.0/token")) {
            jetons.incrementAndGet();
            boolean refuse = chemin.startsWith("/refuse/");
            ecrire(x, refuse ? 401 : 200, refuse ? Map.of() : Map.of("access_token", "jeton-1", "expires_in", 3600));
            return;
        }
        if (!"Bearer jeton-1".equals(x.getRequestHeaders().getFirst("Authorization"))) {
            ecrire(x, 401, Map.of());
            return;
        }
        JsonNode corps = "POST".equals(x.getRequestMethod()) ? JSON.readTree(x.getRequestBody()) : null;
        switch (chemin) {
            case "/keys/cle" -> ecrire(x, 200, Map.of("key", Map.of("kid", coffre + "/keys/cle/v1")));
            case "/keys/cle/wrapkey" -> ecrire(x, 200, Map.of("kid", coffre + "/keys/cle/v1", "value", rsa(Cipher.ENCRYPT_MODE, corps)));
            case "/keys/cle/v1/unwrapkey" -> ouvrir(x, corps);
            default -> ecrire(x, 404, Map.of("error", Map.of("code", "KeyNotFound")));
        }
    }

    private void ouvrir(HttpExchange x, JsonNode corps) throws IOException {
        try {
            ecrire(x, 200, Map.of("kid", coffre + "/keys/cle/v1", "value", rsa(Cipher.DECRYPT_MODE, corps)));
        } catch (RuntimeException e) {
            ecrire(x, 400, Map.of("error", Map.of("code", "BadParameter")));
        }
    }

    private String rsa(int mode, JsonNode corps) {
        try {
            assertEquals("RSA-OAEP-256", corps.get("alg").asText());
            Cipher c = Cipher.getInstance("RSA/ECB/OAEPPadding");
            c.init(mode, mode == Cipher.ENCRYPT_MODE ? rsa.getPublic() : rsa.getPrivate(), OAEP);
            byte[] sortie = c.doFinal(Base64.getUrlDecoder().decode(corps.get("value").asText()));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sortie);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void ecrire(HttpExchange x, int statut, Object corps) throws IOException {
        byte[] octets = JSON.writeValueAsString(corps).getBytes(StandardCharsets.UTF_8);
        x.sendResponseHeaders(statut, octets.length);
        x.getResponseBody().write(octets);
        x.close();
    }
}
