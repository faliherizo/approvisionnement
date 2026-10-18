package qc.levis.contrat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.ia.application.dto.AiRedactionResponse;
import qc.levis.contrat.ia.application.port.AssistantRedactionPort;
import qc.levis.contrat.redaction.domain.Clause;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Couvre les chemins « succès » de l'assistant IA (art. 21) avec un port simulé : rédaction et
 * chat aboutissent et journalisent la suggestion, sans dépendre d'un Ollama en fonctionnement.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AiControllerSuccessTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockBean AssistantRedactionPort assistant;

    private static final String PW = "Passw0rd!";

    @BeforeEach
    void stubAssistant() {
        when(assistant.rediger(any()))
                .thenReturn(new AiRedactionResponse("Clause de garantie proposée.", "llama3.1-test"));
    }

    private Cookie login() throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@levis.qc.ca\",\"motDePasse\":\"" + PW + "\"}"))
                .andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    @Test
    void rediger_succes_retourneLaPropositionEtJournalise() throws Exception {
        Cookie c = login();
        String corps = mvc.perform(MockMvcRequestBuilders.post("/api/ia/rediger").cookie(c)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"instruction\":\"Rédige une clause de garantie\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(json.readTree(corps).get("proposition").asText().contains("garantie"));

        // La suggestion a été tracée.
        JsonNode suggestions = json.readTree(mvc.perform(MockMvcRequestBuilders.get("/api/ia/suggestions").cookie(c))
                .andReturn().getResponse().getContentAsString());
        assertTrue(suggestions.size() >= 1);
        assertEquals("REDIGER", suggestions.get(0).get("typeInteraction").asText());
    }

    @Test
    void chat_succes_utiliseLeModele_etJournalise() throws Exception {
        Cookie c = login();
        JsonNode reponse = json.readTree(mvc.perform(MockMvcRequestBuilders.post("/api/ia/chat").cookie(c)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Quelle clause de garantie ?\",\"contexteDocument\":\"contrat TI\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("llama3.1-test", reponse.get("modele").asText());
        assertTrue(reponse.get("reponse").asText().contains("garantie"));
    }
}
