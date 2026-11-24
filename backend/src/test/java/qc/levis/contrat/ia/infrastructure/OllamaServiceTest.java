package qc.levis.contrat.ia.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import qc.levis.contrat.ia.application.dto.AiRedactionRequest;

/** Adaptateur Ollama contre un faux serveur qui parle le protocole /api/generate comme le vrai. */
class OllamaServiceTest {

    private final ObjectMapper json = new ObjectMapper();
    private final AtomicReference<JsonNode> recue = new AtomicReference<>();
    private HttpServer ollama;
    private int statut = 200;
    private String reponse = "{\"response\":\"  Clause proposée.  \"}";

    @BeforeEach
    void demarrer() throws IOException {
        ollama = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ollama.createContext("/api/generate", echange -> {
            recue.set(json.readTree(echange.getRequestBody()));
            byte[] corps = reponse.getBytes(StandardCharsets.UTF_8);
            echange.getResponseHeaders().add("Content-Type", "application/json");
            echange.sendResponseHeaders(statut, corps.length);
            echange.getResponseBody().write(corps);
            echange.close();
        });
        ollama.start();
    }

    @AfterEach
    void arreter() { ollama.stop(0); }

    private OllamaService service() {
        return new OllamaService("http://127.0.0.1:" + ollama.getAddress().getPort(), "qwen2.5:0.5b", 200);
    }

    @Test
    void la_generation_est_bornee_et_le_texte_rendu_sans_espaces() {
        var r = service().rediger(new AiRedactionRequest("Rédige une clause de garantie", "Garantie de 10 %", "Texte actuel"));
        assertEquals("Clause proposée.", r.proposition());
        assertEquals("qwen2.5:0.5b", r.modele());
        JsonNode corps = recue.get();
        assertEquals("qwen2.5:0.5b", corps.get("model").asText());
        assertEquals(false, corps.get("stream").asBoolean());
        assertEquals(200, corps.at("/options/num_predict").asInt());
        String invite = corps.get("prompt").asText();
        assertTrue(invite.contains("Contexte et connaissances de référence :\nGarantie de 10 %"), invite);
        assertTrue(invite.contains("Texte existant à améliorer :\nTexte actuel"), invite);
        assertTrue(invite.endsWith("Instruction : Rédige une clause de garantie"), invite);
    }

    @Test
    void sans_contexte_ni_texte_l_invite_ne_garde_que_l_instruction() {
        service().rediger(new AiRedactionRequest("Objet du contrat", " ", null));
        String invite = recue.get().get("prompt").asText();
        assertTrue(!invite.contains("Contexte et connaissances") && !invite.contains("Texte existant"), invite);
    }

    @Test
    void une_reponse_sans_texte_donne_un_texte_vide() {
        reponse = "{}";
        assertEquals("", service().rediger(new AiRedactionRequest("Objet", null, null)).proposition());
    }

    @Test
    void une_panne_d_ollama_remonte_en_erreur() {
        statut = 500;
        assertThrows(RestClientException.class, () -> service().rediger(new AiRedactionRequest("Objet", null, null)));
    }
}
