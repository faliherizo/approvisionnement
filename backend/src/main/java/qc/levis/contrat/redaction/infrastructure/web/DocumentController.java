package qc.levis.contrat.redaction.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.approbation.application.dto.ApprovalDecisionRequest;
import qc.levis.contrat.approbation.domain.ApprovalStep;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.application.dto.ClauseDisponible;
import qc.levis.contrat.redaction.application.dto.ClauseSelectionRequest;
import qc.levis.contrat.redaction.application.dto.CommentRequest;
import qc.levis.contrat.redaction.application.dto.CommentResolutionRequest;
import qc.levis.contrat.redaction.application.dto.DocumentClauseRequest;
import qc.levis.contrat.redaction.application.dto.DocumentRequest;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Comment;
import qc.levis.contrat.redaction.domain.Document;

@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping
    public List<Document> lister() { return service.lister(); }

    @GetMapping("/{id}")
    public Document get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document creer(@Valid @RequestBody DocumentRequest req, Authentication auth) {
        return service.creer(req, utilisateur(auth));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public void supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimer(id, utilisateur(auth));
    }

    @GetMapping("/{id}/clauses-eligibles")
    public List<Clause> eligibles(@PathVariable Long id) {
        return service.clausesEligibles(id);
    }

    /** Toutes les clauses actives proposées à l'assemblage (avec verdict d'éligibilité). */
    @GetMapping("/{id}/clauses-disponibles")
    public List<ClauseDisponible> disponibles(@PathVariable Long id) {
        return service.clausesDisponibles(id);
    }

    @PostMapping("/{id}/assembler")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document assembler(@PathVariable Long id,
                              @RequestBody ClauseSelectionRequest req,
                              Authentication auth) {
        return service.assembler(id, req.clauseIds(), utilisateur(auth));
    }

    // --- Rédaction locale des sections (texte propre au document + sections libres) ---

    @PutMapping("/{id}/clauses/{clauseId}")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document modifierClause(@PathVariable Long id, @PathVariable Long clauseId,
                                   @RequestBody DocumentClauseRequest req, Authentication auth) {
        return service.modifierClause(id, clauseId, req, utilisateur(auth));
    }

    @PostMapping("/{id}/clauses")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document ajouterClauseLibre(@PathVariable Long id,
                                       @RequestBody DocumentClauseRequest req, Authentication auth) {
        return service.ajouterClauseLibre(id, req, utilisateur(auth));
    }

    @DeleteMapping("/{id}/clauses/{clauseId}")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Document supprimerClause(@PathVariable Long id, @PathVariable Long clauseId, Authentication auth) {
        return service.supprimerClause(id, clauseId, utilisateur(auth));
    }

    // --- Commentaires ---

    @GetMapping("/{id}/commentaires")
    public List<Comment> commentaires(@PathVariable Long id) {
        return service.commentaires(id);
    }

    @PostMapping("/{id}/commentaires")
    public Comment commenter(@PathVariable Long id,
                             @Valid @RequestBody CommentRequest req,
                             Authentication auth) {
        return service.commenter(id, req, utilisateur(auth));
    }

    @PostMapping("/{id}/commentaires/{commentaireId}/resolution")
    public Comment resoudreCommentaire(@PathVariable Long id, @PathVariable Long commentaireId,
                                       @RequestBody CommentResolutionRequest req, Authentication auth) {
        return service.resoudreCommentaire(id, commentaireId, req.resolu(), utilisateur(auth));
    }

    // --- Approbation hiérarchisée ---

    @PostMapping("/{id}/approbation")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public List<ApprovalStep> demarrer(@PathVariable Long id, Authentication auth) {
        return service.demarrerApprobation(id, utilisateur(auth));
    }

    @GetMapping("/{id}/approbation")
    public List<ApprovalStep> etapes(@PathVariable Long id) {
        return service.etapesApprobation(id);
    }

    @PostMapping("/approbation/{stepId}/decision")
    @PreAuthorize("hasAnyRole('APPRO','JURIDIQUE','DIRECTION','ADMIN')")
    public ApprovalStep decider(@PathVariable Long stepId,
                                @Valid @RequestBody ApprovalDecisionRequest req,
                                Authentication auth) {
        return service.decider(stepId, req.approuve(), req.note(), utilisateur(auth));
    }
}
