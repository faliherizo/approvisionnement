package qc.levis.contrat.ia.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.ia.application.AiTraceabiliteService;
import qc.levis.contrat.ia.application.KnowledgeService;
import qc.levis.contrat.ia.application.dto.AiRedactionRequest;
import qc.levis.contrat.ia.application.dto.ChatRequest;
import qc.levis.contrat.ia.application.dto.ChatResponse;
import qc.levis.contrat.ia.application.dto.IaParametre;
import qc.levis.contrat.ia.application.dto.IaParametreRequest;
import qc.levis.contrat.ia.application.dto.KnowledgeRequest;
import qc.levis.contrat.ia.application.port.AssistantRedactionPort;
import qc.levis.contrat.ia.domain.AiSuggestion;
import qc.levis.contrat.ia.domain.KnowledgeSnippet;

/**
 * Assistant IA spécialisé en rédaction contractuelle. Dépend du port (DIP).
 * Le chat s'appuie sur une base de connaissances (RAG) alimentée par le tenant.
 */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/ia")
public class AiController {

    private final AssistantRedactionPort assistant;
    private final KnowledgeService knowledge;
    private final AiTraceabiliteService traceabilite;

    public AiController(AssistantRedactionPort assistant, KnowledgeService knowledge,
                        AiTraceabiliteService traceabilite) {
        this.assistant = assistant;
        this.knowledge = knowledge;
        this.traceabilite = traceabilite;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @PostMapping("/rediger")
    @PreAuthorize("hasAnyRole('REDACTEUR','JURIDIQUE','ADMIN')")
    public ResponseEntity<?> rediger(@Valid @RequestBody AiRedactionRequest req, Authentication auth) {
        traceabilite.verifierActivee();
        try {
            var r = assistant.rediger(req);
            traceabilite.journaliser("REDIGER", req.instruction(), r.proposition(), r.modele(), utilisateur(auth));
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.status(503).body(java.util.Map.of(
                    "erreur", "Service IA (Ollama) indisponible", "detail", e.getMessage()));
        }
    }

    // --- Encadrement IA : activation/désactivation + traçabilité (art. 21) ---

    @GetMapping("/parametres")
    public IaParametre parametres() {
        return new IaParametre(traceabilite.estActivee());
    }

    @PutMapping("/parametres")
    @PreAuthorize("hasRole('ADMIN')")
    public IaParametre definirParametres(@Valid @RequestBody IaParametreRequest req, Authentication auth) {
        return new IaParametre(traceabilite.activer(req.activee(), utilisateur(auth)));
    }

    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyRole('JURIDIQUE','ADMIN')")
    public List<AiSuggestion> suggestions() {
        return traceabilite.lister();
    }

    /** Chat IA spécialisé : augmente la question avec la base de connaissances (RAG). */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('REDACTEUR','JURIDIQUE','ADMIN')")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest req, Authentication auth) {
        traceabilite.verifierActivee();
        String rag = knowledge.contextePour(req.question(), 4);
        StringBuilder contexte = new StringBuilder();
        if (req.contexteDocument() != null && !req.contexteDocument().isBlank()) {
            contexte.append("Contexte du document : ").append(req.contexteDocument()).append("\n\n");
        }
        if (!rag.isBlank()) {
            contexte.append("Connaissances internes pertinentes :\n").append(rag);
        }
        try {
            var r = assistant.rediger(new AiRedactionRequest(req.question(), contexte.toString(), null));
            List<String> sources = rag.isBlank() ? List.of() : List.of("base de connaissances du tenant");
            traceabilite.journaliser("CHAT", req.question(), r.proposition(), r.modele(), utilisateur(auth));
            return ResponseEntity.ok(new ChatResponse(r.proposition(), r.modele(), sources));
        } catch (Exception e) {
            // Repli : le modèle local (Ollama) est hors ligne → on répond quand même à
            // partir de la base de connaissances (RAG), sans échouer (chat toujours utile).
            String repli = rag.isBlank()
                    ? "Le moteur IA local (Ollama) est momentanément hors ligne. "
                      + "Ajoutez des documents à la base de connaissances et démarrez Ollama "
                      + "pour obtenir une rédaction complète."
                    : "Réponse hors ligne (VINA) — éléments pertinents de votre base de connaissances :\n\n"
                      + rag + "\n\n(Démarrez Ollama pour une rédaction complète.)";
            List<String> sources = rag.isBlank() ? List.of() : List.of("base de connaissances (mode hors ligne)");
            traceabilite.journaliser("CHAT", req.question(), repli, "hors-ligne", utilisateur(auth));
            return ResponseEntity.ok(new ChatResponse(repli, "hors-ligne", sources));
        }
    }

    // --- Base de connaissances (ingestion de documents / données) ---

    @GetMapping("/knowledge")
    public List<KnowledgeSnippet> lister() {
        return knowledge.lister();
    }

    @PostMapping("/knowledge")
    @PreAuthorize("hasAnyRole('REDACTEUR','JURIDIQUE','ADMIN')")
    public KnowledgeSnippet ajouter(@Valid @RequestBody KnowledgeRequest req, Authentication auth) {
        return knowledge.ajouter(req.titre(), req.source(), req.contenu(),
                auth != null ? auth.getName() : "anonyme");
    }

    @DeleteMapping("/knowledge/{id}")
    @PreAuthorize("hasAnyRole('REDACTEUR','JURIDIQUE','ADMIN')")
    public void supprimer(@PathVariable Long id) {
        knowledge.supprimer(id);
    }
}
