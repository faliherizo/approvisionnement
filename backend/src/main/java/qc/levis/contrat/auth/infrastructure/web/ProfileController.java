package qc.levis.contrat.auth.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.application.dto.MeResponse;
import qc.levis.contrat.auth.application.dto.ProfileUpdateRequest;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;

/** Gestion du profil personnel de l'utilisateur connecté (self-service). */
@RestController
@RequestMapping("/api/profil")
public class ProfileController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public ProfileController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping
    public MeResponse profil(Authentication auth) {
        User u = courant(auth);
        return new MeResponse(u.getEmail(), u.getNom(), u.getDirection(),
                u.getRoles().stream().map(Enum::name).toList(),
                u.isMfaActive(), u.getMfaMethode() != null ? u.getMfaMethode().name() : "NONE");
    }

    @PutMapping
    public ResponseEntity<?> mettreAJour(@Valid @RequestBody ProfileUpdateRequest req,
                                         Authentication auth) {
        User u = courant(auth);
        if (req.nom() != null) u.setNom(req.nom());
        if (req.direction() != null) u.setDirection(req.direction());

        if (req.nouveauMotDePasse() != null && !req.nouveauMotDePasse().isBlank()) {
            if (req.motDePasseActuel() == null
                    || !encoder.matches(req.motDePasseActuel(), u.getMotDePasseHash())) {
                return ResponseEntity.status(400)
                        .body(java.util.Map.of("erreur", "Mot de passe actuel invalide"));
            }
            u.setMotDePasseHash(encoder.encode(req.nouveauMotDePasse()));
        }
        users.save(u);
        return ResponseEntity.ok(java.util.Map.of("statut", "ok"));
    }

    private User courant(Authentication auth) {
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));
    }
}
