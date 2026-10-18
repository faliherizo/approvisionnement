package qc.levis.contrat.auth.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.auth.infrastructure.OidcClient.OidcException;
import qc.levis.contrat.commun.application.LogSafe;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.IntegrationOidcService.ConfigOidc;

/**
 * Établit une session applicative à partir d'une identité OIDC validée : rattache le compte au
 * tenant, l'auto-provisionne si autorisé, puis émet le jeton de session (JWT). Refuse en défaut
 * un courriel appartenant à un autre tenant ou inconnu si l'auto-provisionnement est désactivé.
 */
@Service
public class OidcSessionService {

    private static final Logger log = LoggerFactory.getLogger(OidcSessionService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditService audit;

    public OidcSessionService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                              AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.audit = audit;
    }

    /** Résout (ou crée) l'utilisateur du tenant et renvoie le jeton de session. */
    @Transactional
    public String etablirSession(ConfigOidc cfg, String courriel, String nom) {
        String email = courriel.trim().toLowerCase();
        User user = users.findByEmail(email).map(u -> verifierTenant(u, cfg)).orElse(null);
        if (user == null) {
            user = provisionner(cfg, email, nom);
        }
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String token = jwt.genererToken(user.getEmail(), roles, user.getTenantId());
        audit.log(user.getTenantId(), user.getEmail(), "LOGIN_OIDC", "User", user.getId(), null);
        log.info("Connexion OIDC réussie : utilisateur {} (tenant {}, rôles {})",
                user.getId(), user.getTenantId(), roles);
        return token;
    }

    private User verifierTenant(User u, ConfigOidc cfg) {
        if (u.getTenantId() != null && !u.getTenantId().equals(cfg.tenantId())) {
            throw new OidcException("Ce compte n'appartient pas à cet établissement.");
        }
        return u;
    }

    private User provisionner(ConfigOidc cfg, String email, String nom) {
        if (!cfg.autoProvision()) {
            log.warn("Connexion OIDC refusée : courriel inconnu {} (auto-provisionnement désactivé)",
                    LogSafe.courriel(email));
            throw new OidcException("Compte inconnu et auto-provisionnement désactivé.");
        }
        User u = new User();
        u.setEmail(email);
        u.setNom(nom == null || nom.isBlank() ? email : nom);
        u.setTenantId(cfg.tenantId());
        u.setActif(true);
        u.setRoles(Set.of(role(cfg.roleParDefaut())));
        // Compte fédéré : aucun mot de passe local exploitable (empreinte aléatoire non devinable).
        u.setMotDePasseHash(encoder.encode(UUID.randomUUID().toString()));
        User sauve = users.save(u);
        audit.log(sauve.getTenantId(), sauve.getEmail(), "PROVISION_OIDC", "User", sauve.getId(), null);
        log.info("Compte OIDC auto-provisionné : {} (tenant {})", sauve.getId(), sauve.getTenantId());
        return sauve;
    }

    private Enums.Role role(String code) {
        try {
            return Enums.Role.valueOf(code.trim().toUpperCase());
        } catch (RuntimeException e) {
            return Enums.Role.REDACTEUR;
        }
    }
}
