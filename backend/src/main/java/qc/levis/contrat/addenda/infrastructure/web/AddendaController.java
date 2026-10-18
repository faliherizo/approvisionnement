package qc.levis.contrat.addenda.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.addenda.application.AddendaService;
import qc.levis.contrat.addenda.application.dto.AddendaChangeRequest;
import qc.levis.contrat.addenda.application.dto.AddendaRequest;
import qc.levis.contrat.addenda.domain.Addenda;

@RestController
@RequestMapping("/api")
public class AddendaController {

    private final AddendaService service;

    public AddendaController(AddendaService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping("/documents/{documentId}/addendas")
    public List<Addenda> lister(@PathVariable Long documentId) {
        return service.lister(documentId);
    }

    @PostMapping("/documents/{documentId}/addendas")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Addenda creer(@PathVariable Long documentId,
                         @Valid @RequestBody AddendaRequest req,
                         Authentication auth) {
        return service.creer(documentId, req.objet(), utilisateur(auth));
    }

    @PostMapping("/addendas/{addendaId}/changements")
    @PreAuthorize("hasAnyRole('REDACTEUR','ADMIN')")
    public Addenda ajouterChangement(@PathVariable Long addendaId,
                                     @Valid @RequestBody AddendaChangeRequest req,
                                     Authentication auth) {
        return service.ajouterChangement(addendaId, req.clauseId(), req.nouveauTexte(), utilisateur(auth));
    }

    @PostMapping("/addendas/{addendaId}/reintegrer")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Addenda reintegrer(@PathVariable Long addendaId, Authentication auth) {
        return service.reintegrer(addendaId, utilisateur(auth));
    }
}
