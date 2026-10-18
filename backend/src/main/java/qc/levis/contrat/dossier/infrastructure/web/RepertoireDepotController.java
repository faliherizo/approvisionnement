package qc.levis.contrat.dossier.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.dossier.application.RepertoireDepotService;
import qc.levis.contrat.dossier.domain.RepertoireDepot;

/**
 * Répertoires de dépôt personnels de l'utilisateur connecté (son espace). Alimentent les
 * suggestions du champ « Répertoire de destination » à la publication.
 */
@RestController
@RequestMapping("/api/profil/repertoires")
public class RepertoireDepotController {

    private final RepertoireDepotService service;
    private final UserRepository users;

    public RepertoireDepotController(RepertoireDepotService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping
    public List<RepertoireDepot> lister(Authentication auth) {
        return service.lister(courant(auth).getId());
    }

    @PostMapping
    public RepertoireDepot ajouter(@RequestBody RepertoireRequest req, Authentication auth) {
        return service.ajouter(courant(auth).getId(), req.chemin());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimer(courant(auth).getId(), id);
        return ResponseEntity.noContent().build();
    }

    private User courant(Authentication auth) {
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));
    }

    public record RepertoireRequest(@NotBlank String chemin) {}
}
