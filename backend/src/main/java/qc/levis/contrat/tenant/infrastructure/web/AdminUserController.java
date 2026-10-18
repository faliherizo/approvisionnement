package qc.levis.contrat.tenant.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.tenant.application.AdminUserService;
import qc.levis.contrat.tenant.application.dto.AdminUserRequest;
import qc.levis.contrat.tenant.application.dto.PageResponse;

/** Gestion des utilisateurs et des rôles (RBAC) d'un tenant — réservé aux admins. */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<User> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(defaultValue = "") String q) {
        return service.lister(page, taille, q);
    }

    @GetMapping("/roles")
    public List<String> roles() { return service.rolesAssignables(); }

    @PostMapping
    public User creer(@Valid @RequestBody AdminUserRequest req) {
        return service.creer(req);
    }

    @PutMapping("/{id}")
    public User modifier(@PathVariable Long id, @Valid @RequestBody AdminUserRequest req) {
        return service.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
