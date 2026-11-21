package qc.levis.contrat.messagerie.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.messagerie.application.MessagerieService;
import qc.levis.contrat.messagerie.application.dto.*;

/** Messagerie interne sécurisée entre intervenants (CHU 5.05), offerte avec le module de traitement des demandes. */
@RestController
@RequestMapping("/api/messagerie/conversations")
@PreAuthorize("@modules.actif('saisie')")
public class MessagerieController {

    private final MessagerieService service;

    public MessagerieController(MessagerieService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConversationResume> lister(Authentication auth) {
        return service.mesConversations(auth.getName());
    }

    @PostMapping
    public ConversationDetail creer(@Valid @RequestBody NouvelleConversationRequest req, Authentication auth) {
        return service.creer(req, auth.getName());
    }

    @GetMapping("/{id}")
    public ConversationDetail ouvrir(@PathVariable Long id, Authentication auth) {
        return service.ouvrir(id, auth.getName());
    }

    @PostMapping("/{id}/messages")
    public ConversationDetail repondre(@PathVariable Long id, @Valid @RequestBody MessageRequest req, Authentication auth) {
        return service.repondre(id, req.texte(), auth.getName());
    }

    @PostMapping("/{id}/participants")
    public ConversationDetail ajouter(@PathVariable Long id, @Valid @RequestBody ParticipantRequest req, Authentication auth) {
        return service.ajouterParticipant(id, req.courriel(), auth.getName());
    }
}
