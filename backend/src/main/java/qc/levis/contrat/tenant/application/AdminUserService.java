package qc.levis.contrat.tenant.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.tenant.application.dto.AdminUserRequest;
import qc.levis.contrat.tenant.application.dto.PageResponse;

/**
 * Gestion des utilisateurs et rôles (RBAC) au sein d'un tenant.
 * Le superadmin (tenantId null) n'est jamais visible ni modifiable ici.
 */
@Service
public class AdminUserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminUserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** Rôles assignables par un admin (SUPERADMIN exclu). */
    public List<String> rolesAssignables() {
        return List.of(Enums.Role.ADMIN, Enums.Role.REDACTEUR, Enums.Role.JURIDIQUE,
                        Enums.Role.APPRO, Enums.Role.DIRECTION, Enums.Role.LECTEUR, Enums.Role.AUDITEUR)
                .stream().map(Enum::name).toList();
    }

    public PageResponse<User> lister(int page, int taille, String q) {
        Page<User> resultat = users.rechercher(TenantContext.get(), q == null ? "" : q,
                PageRequest.of(page, taille, Sort.by("nom")));
        return new PageResponse<>(resultat.getContent(),
                resultat.getTotalElements(), page, taille);
    }

    @Transactional
    public User creer(AdminUserRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Courriel déjà utilisé: " + req.email());
        }
        User u = new User();
        u.setEmail(req.email());
        u.setTenantId(TenantContext.get());
        u.setMotDePasseHash(encoder.encode(
                req.motDePasse() != null ? req.motDePasse() : "ChangezMoi!1"));
        appliquer(u, req);
        return users.save(u);
    }

    @Transactional
    public User modifier(Long id, AdminUserRequest req) {
        User u = exigerDansTenant(id);
        if (req.motDePasse() != null && !req.motDePasse().isBlank()) {
            u.setMotDePasseHash(encoder.encode(req.motDePasse()));
        }
        appliquer(u, req);
        return users.save(u);
    }

    @Transactional
    public void supprimer(Long id) {
        users.delete(exigerDansTenant(id));
    }

    private void appliquer(User u, AdminUserRequest req) {
        u.setNom(req.nom());
        u.setDirection(req.direction());
        if (req.actif() != null) u.setActif(req.actif());
        if (req.roles() != null) {
            u.setRoles(req.roles().stream()
                    .map(Enums.Role::valueOf)
                    .filter(r -> r != Enums.Role.SUPERADMIN) // un admin ne peut pas accorder SUPERADMIN
                    .collect(Collectors.toSet()));
        }
    }

    /** Vérifie que l'utilisateur cible appartient au tenant courant (jamais le superadmin). */
    private User exigerDansTenant(Long id) {
        User u = users.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable: " + id));
        Long tenant = TenantContext.get();
        if (tenant == null || !tenant.equals(u.getTenantId())) {
            throw new NoSuchElementException("Utilisateur introuvable: " + id);
        }
        return u;
    }
}
