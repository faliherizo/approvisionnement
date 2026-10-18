package qc.levis.contrat.ia.application.port;

import qc.levis.contrat.ia.application.dto.AiRedactionRequest;
import qc.levis.contrat.ia.application.dto.AiRedactionResponse;

/**
 * Port (architecture hexagonale) de l'assistance IA à la rédaction contractuelle.
 * L'implémentation par défaut est un modèle local Ollama ; on peut la substituer
 * (Azure OpenAI, Claude…) sans modifier les appelants (DIP / OCP).
 */
public interface AssistantRedactionPort {
    AiRedactionResponse rediger(AiRedactionRequest requete);
}
