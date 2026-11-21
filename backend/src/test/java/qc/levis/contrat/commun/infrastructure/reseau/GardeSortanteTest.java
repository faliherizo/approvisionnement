package qc.levis.contrat.commun.infrastructure.reseau;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.infrastructure.reseau.GardeSortante.DestinationRefusee;

/** OWASP A10 : aucune requête sortante vers une adresse interne, sauf hôte:port autorisé. */
class GardeSortanteTest {

    static InetAddress ip(String s) {
        try { return InetAddress.getByName(s); } catch (UnknownHostException e) { throw new IllegalStateException(e); }
    }

    static final Map<String, String> DNS = Map.of("idp.example.org", "203.0.113.10", "interne.example.org", "10.0.0.5",
            "localhost", "127.0.0.1", "metadonnees", "169.254.169.254", "ula", "fd00::1", "cgnat", "100.64.0.1", "multi", "224.0.0.1");

    final GardeSortante garde = new GardeSortante(Set.of("localhost:8180"), h -> {
        String a = DNS.get(h);
        if (a == null && !h.matches("[0-9a-f.:]+")) throw new UnknownHostException(h);
        return new InetAddress[] { ip(a == null ? h : a) };
    });

    @Test
    void une_adresse_publique_en_https_est_admise() {
        assertEquals(URI.create("https://idp.example.org/.well-known/openid-configuration"),
                garde.exiger("https://idp.example.org/.well-known/openid-configuration"));
        assertDoesNotThrow(() -> garde.exiger("http://idp.example.org:8443/x"));
    }

    @Test
    void les_adresses_internes_et_les_metadonnees_infonuagiques_sont_refusees() {
        for (String url : new String[] { "http://169.254.169.254/latest/meta-data/", "http://metadonnees/", "http://interne.example.org/",
                "http://localhost:8100/api/admin/users", "http://127.0.0.1/", "http://[::1]:22/", "http://ula/", "http://cgnat/",
                "http://multi/", "http://0.0.0.0/", "http://192.168.1.1/", "http://172.16.0.1/" }) {
            DestinationRefusee r = assertThrows(DestinationRefusee.class, () -> garde.exiger(url), url);
            assertTrue(r.getMessage().startsWith("Adresse refusée"), url);
        }
    }

    @Test
    void seuls_http_et_https_sont_admis_et_l_url_doit_etre_complete() {
        assertThrows(DestinationRefusee.class, () -> garde.exiger("file:///etc/passwd"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger("gopher://idp.example.org/"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger("https:///chemin"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger("http://inconnu.invalid/"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger("http://a b/"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger((String) null));
        assertThrows(DestinationRefusee.class, () -> garde.exiger(URI.create("relative/chemin")));
    }

    @Test
    void un_hote_port_autorise_reste_joignable_et_la_garde_inactive_laisse_tout_passer() {
        assertDoesNotThrow(() -> garde.exiger("http://localhost:8180/realms/iq"));
        assertThrows(DestinationRefusee.class, () -> garde.exiger("http://localhost:8181/"));
        assertDoesNotThrow(() -> GardeSortante.aucune().exiger("http://127.0.0.1:9/"));
        assertDoesNotThrow(() -> new GardeSortante("localhost:8180, ,127.0.0.1:8099").exiger("http://127.0.0.1:8099/x"));
    }
}
