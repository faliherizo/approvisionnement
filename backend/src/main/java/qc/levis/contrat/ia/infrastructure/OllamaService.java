package qc.levis.contrat.ia.infrastructure;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import qc.levis.contrat.ia.application.dto.AiRedactionRequest;
import qc.levis.contrat.ia.application.dto.AiRedactionResponse;
import qc.levis.contrat.ia.application.port.AssistantRedactionPort;

/**
 * Adaptateur Ollama du port {@link AssistantRedactionPort} : assistance IA via un
 * modèle local. Aucun renseignement ne quitte l'infrastructure (conteneur Ollama),
 * ce qui soutient la conformité Loi 25.
 */
@Service
public class OllamaService implements AssistantRedactionPort {

    private final RestClient client;
    private final String modele;

    public OllamaService(@Value("${app.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
                         @Value("${app.ai.ollama.model:llama3.1}") String modele) {
        // Timeouts explicites : en l'absence d'Ollama, l'appel échoue vite (503)
        // plutôt que de bloquer la requête HTTP.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(120)); // génération LLM
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.modele = modele;
    }

    @Override
    public AiRedactionResponse rediger(AiRedactionRequest req) {
        String prompt = construirePrompt(req);
        Map<String, Object> body = Map.of(
                "model", modele,
                "prompt", prompt,
                "stream", false,
                "options", Map.of("temperature", 0.2)
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = client.post()
                .uri("/api/generate")
                .body(body)
                .retrieve()
                .body(Map.class);

        String texte = resp != null && resp.get("response") != null
                ? resp.get("response").toString().trim()
                : "";
        return new AiRedactionResponse(texte, modele);
    }

    private String construirePrompt(AiRedactionRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEME).append("\n\n");
        if (req.contexte() != null && !req.contexte().isBlank()) {
            sb.append("Contexte et connaissances de référence :\n").append(req.contexte()).append("\n\n");
        }
        if (req.texteExistant() != null && !req.texteExistant().isBlank()) {
            sb.append("Texte existant à améliorer :\n").append(req.texteExistant()).append("\n\n");
        }
        sb.append("Instruction : ").append(req.instruction());
        return sb.toString();
    }

    /** Persona et cadre de l'assistant : expert en rédaction contractuelle publique québécoise. */
    private static final String SYSTEME = """
            Tu es VINA, assistant expert en rédaction contractuelle de marchés publics au Québec.
            Domaine : Loi sur les contrats des organismes publics (LCOP, C-65.1), cadre du Secrétariat
            du Conseil du trésor (SCT), CCAG, Autorité des marchés publics (AMP) ; pour le municipal,
            Loi sur les cités et villes (LCV) et Code municipal (CM).
            Règles de rédaction :
            - Réponds en français, de façon claire, précise et juridiquement rigoureuse ; pas de bavardage.
            - Emploie le présent de l'indicatif à valeur obligatoire (« le prestataire fournit… »),
              une obligation par phrase, une numérotation hiérarchique (0.00, 1.00, 1.01).
            - Aligne le vocabulaire sur le CCAG et la LCOP ; désigne les parties « l'ORGANISME PUBLIC »
              et « le PRESTATAIRE DE SERVICES ».
            - Une clause normative (CCAG) ne se récrit pas : signale-le plutôt que de l'inventer.
            - Fonde-toi sur les connaissances de référence fournies ; n'invente pas de disposition légale
              et n'affirme pas de seuil ou de règle dont tu n'es pas certain.
            - Fournis uniquement le texte demandé, prêt à insérer dans le document.""";
}
