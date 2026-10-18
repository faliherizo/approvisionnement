package qc.levis.contrat.dossier.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.dossier.application.dto.DossierRapport;
import qc.levis.contrat.dossier.application.dto.DossierRequest;
import qc.levis.contrat.dossier.application.dto.DossierStatutRequest;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.domain.Document;

/** API REST des dossiers d'approvisionnement (regroupement de documents - art. 15). */
@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    private final DossierService service;

    public DossierController(DossierService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping
    public List<Dossier> lister() { return service.lister(); }

    @GetMapping("/{id}")
    public Dossier get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Dossier creer(@Valid @RequestBody DossierRequest req, Authentication auth) {
        return service.creer(req, utilisateur(auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Dossier modifier(@PathVariable Long id, @Valid @RequestBody DossierRequest req, Authentication auth) {
        return service.modifier(id, req, utilisateur(auth));
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('APPRO','ADMIN')")
    public Dossier changerStatut(@PathVariable Long id, @Valid @RequestBody DossierStatutRequest req, Authentication auth) {
        return service.changerStatut(id, req.statut(), utilisateur(auth));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('APPRO','ADMIN')")
    public void supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimer(id, utilisateur(auth));
    }

    @GetMapping("/{id}/documents")
    public List<Document> documents(@PathVariable Long id) {
        return service.documentsDu(id);
    }

    @PostMapping("/{id}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Document rattacher(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
        return service.rattacher(id, documentId, utilisateur(auth));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Document detacher(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
        return service.detacher(id, documentId, utilisateur(auth));
    }

    @GetMapping("/{id}/rapport")
    public DossierRapport rapport(@PathVariable Long id) {
        return service.rapport(id);
    }
}
