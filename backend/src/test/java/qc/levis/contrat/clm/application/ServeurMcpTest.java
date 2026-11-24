package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.mcp.OutilMcp;
import qc.levis.contrat.clm.application.mcp.ServeurMcp;
import qc.levis.contrat.clm.domain.ClientIntegration;

/** ADR-023 : protocole MCP, hors pile web. */
class ServeurMcpTest {

    private static OutilMcp outil(String nom) {
        return new OutilMcp() {
            @Override public String nom() { return nom; }
            @Override public String titre() { return nom; }
            @Override public String description() { return nom; }
            @Override public Map<String, Object> schemaEntree() { return Map.of("type", "object"); }
            @Override public Map<String, Object> executer(ClientIntegration c, Map<String, Object> a) {
                return Map.of("id", OutilMcp.identifiant(a, "id"));
            }
        };
    }

    @Test
    void deux_outils_du_meme_nom_sont_refuses_au_demarrage() {
        assertThrows(IllegalStateException.class, () -> new ServeurMcp(List.of(outil("a"), outil("a")), null));
    }

    @Test
    void un_message_absent_ou_non_objet_est_une_requete_invalide() throws Exception {
        ServeurMcp s = new ServeurMcp(List.of(outil("a")), null);
        assertEquals(-32600, ((Map<?, ?>) s.traiter(null, null).orElseThrow().get("error")).get("code"));
        assertEquals(-32600, ((Map<?, ?>) s.traiter(null, new ObjectMapper().readTree("3")).orElseThrow().get("error")).get("code"));
        assertTrue(s.traiter(null, new ObjectMapper().readTree("{\"jsonrpc\":\"2.0\",\"id\":null,\"method\":\"ping\"}")).isEmpty(),
                "un identifiant nul est traité comme une notification");
    }

    @Test
    void un_identifiant_doit_etre_un_entier_positif() {
        assertEquals(4L, OutilMcp.identifiant(Map.of("id", 4), "id"));
        for (Object v : new Object[]{0, -1, 2.5, "4", null}) {
            Map<String, Object> args = new java.util.HashMap<>();
            args.put("id", v);
            assertThrows(OutilMcp.ArgumentInvalide.class, () -> OutilMcp.identifiant(args, "id"), String.valueOf(v));
        }
    }
}
