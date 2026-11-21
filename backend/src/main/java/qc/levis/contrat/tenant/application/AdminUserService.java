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
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.tenant.application.dto.AdminUserRequest;
import qc.levis.contrat.tenant.application.dto.MembreOrganisation;
import qc.levis.contrat.tenant.application.dto.PageResponse;

/**
 * Gestion des utilisateurs et rôles (RBAC) au sein d'un tenant.
 * Le superadmin (tenantId null) n'est jamais visible ni modifiable ici.
 */
@Service
public class AdminUserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AdminUserService(UserRepository users, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.audit = audit;
    }

    /** Rôles assignables par un admin (SUPERADMIN exclu). */
    public List<String> rolesAssignables() {
        return List.of(Enums.Role.ADMIN, Enums.Role.ADMIN_AFFAIRES, Enums.Role.ADMIN_TECHNIQUE, Enums.Role.REDACTEUR, Enums.Role.JURIDIQUE,
                        Enums.Role.APPRO, Enums.Role.DIRECTION, Enums.Role.LECTEUR, Enums.Role.AUDITEUR)
                .stream().map(Enum::name).toList();
    }

    /** Annuaire des comptes actifs d'une organisation, servi aux modules qui confient une tâche à quelqu'un. */
    public List<MembreOrganisation> membresActifs(Long tenantId) {
        return users.findByTenantIdAndActifTrueOrderByNomAsc(tenantId).stream()
                .map(u -> new MembreOrganisation(u.getEmail(), u.getNom())).toList();
    }

    public PageResponse<User> lister(int page, int taille, String q) {
        Page<User> resultat = users.rechercher(TenantContext.get(), q == null ? "" : q,
                PageRequest.of(page, taille, Sort.by("nom")));
        return new PageResponse<>(resultat.getContent(),
                resultat.getTotalElements(), page, taille);
    }

    @Transactional
    public User creer(AdminUserRequest req, String par) {
        if (users.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Courriel déjà utilisé: " + req.email());
        }
        User u = new User();
        u.setEmail(req.email());
        u.setTenantId(TenantContext.get());
        if (req.motDePasse() != null) {
            qc.levis.contrat.auth.domain.PolitiqueMotDePasse.exiger(req.motDePasse());
        }
        u.setMotDePasseHash(encoder.encode(
                req.motDePasse() != null ? req.motDePasse() : "ChangezMoi!1"));
        appliquer(u, req);
        User cree = users.save(u);
        audit.log(par, "COMPTE_CREE", "Utilisateur", cree.getId(), cree.getEmail() + " · rôles " + roles(cree));
        return cree;
    }

    /**
     * Les changements de rôles, d'activation et de mot de passe sont consignés au journal d'audit (UI-20, zone 05) :
     * l'auditeur doit pouvoir reconstituer qui a accordé quel droit. Le mot de passe lui-même n'y figure jamais.
     */
    @Transactional
    public User modifier(Long id, AdminUserRequest req, String par) {
        User u = exigerDansTenant(id);
        String rolesAvant = roles(u);
        boolean actifAvant = u.isActif();
        boolean motDePasseChange = req.motDePasse() != null && !req.motDePasse().isBlank();
        if (motDePasseChange) {
            qc.levis.contrat.auth.domain.PolitiqueMotDePasse.exiger(req.motDePasse());
            u.setMotDePasseHash(encoder.encode(req.motDePasse()));
        }
        appliquer(u, req);
        User modifie = users.save(u);
        journaliserModification(modifie, par, rolesAvant, actifAvant, motDePasseChange);
        return modifie;
    }

    @Transactional
    public void supprimer(Long id, String par) {
        User u = exigerDansTenant(id);
        users.delete(u);
        audit.log(par, "COMPTE_SUPPRIME", "Utilisateur", id, u.getEmail() + " · rôles " + roles(u));
    }

    private void journaliserModification(User u, String par, String rolesAvant, boolean actifAvant, boolean motDePasseChange) {
        String rolesApres = roles(u);
        if (!rolesAvant.equals(rolesApres)) {
            audit.log(par, "COMPTE_ROLES_MODIFIES", "Utilisateur", u.getId(), u.getEmail() + " · " + rolesAvant + " → " + rolesApres);
        }
        if (actifAvant != u.isActif()) {
            audit.log(par, u.isActif() ? "COMPTE_REACTIVE" : "COMPTE_DESACTIVE", "Utilisateur", u.getId(), u.getEmail());
        }
        if (motDePasseChange) {
            audit.log(par, "COMPTE_MOT_DE_PASSE_REINITIALISE", "Utilisateur", u.getId(), u.getEmail());
        }
        audit.log(par, "COMPTE_MODIFIE", "Utilisateur", u.getId(), u.getEmail());
    }

    private static String roles(User u) {
        return u.getRoles() == null ? "[]" : u.getRoles().stream().map(Enum::name).sorted().toList().toString();
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
