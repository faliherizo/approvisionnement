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
import qc.levis.contrat.auth.infrastructure.SamlClient.SamlException;
import qc.levis.contrat.commun.application.LogSafe;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.IntegrationSamlService.ConfigSaml;

/**
 * Établit une session applicative à partir d'une identité SAML validée : rattache le compte au
 * tenant, l'auto-provisionne si autorisé, puis émet le jeton de session (JWT). Refuse par défaut
 * un courriel appartenant à un autre tenant ou inconnu si l'auto-provisionnement est désactivé.
 */
@Service
public class SamlSessionService {

    private static final Logger log = LoggerFactory.getLogger(SamlSessionService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditService audit;
    private final AnnuaireService annuaire;

    public SamlSessionService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                              AuditService audit,
                              AnnuaireService annuaire) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.audit = audit;
        this.annuaire = annuaire;
    }

    /** Résout (ou crée) l'utilisateur du tenant et renvoie le jeton de session. */
    @Transactional
    public String etablirSession(ConfigSaml cfg, String courriel, String nom) {
        return etablirSession(cfg, courriel, nom, java.util.List.of());
    }

    /**
     * Avec les faits d'annuaire de l'assertion : rôles et domaines d'affaires suivent les correspondances de l'organisation
     * (addenda 3, Q43-Q44). Un compte désactivé (départ, déprovisionnement SCIM) ne se connecte plus.
     */
    @Transactional
    public String etablirSession(ConfigSaml cfg, String courriel, String nom, java.util.List<qc.levis.contrat.auth.domain.FaitAnnuaire> faits) {
        String email = courriel.trim().toLowerCase();
        User user = users.findByEmail(email).map(u -> verifierTenant(u, cfg)).orElse(null);
        if (user == null) {
            user = provisionner(cfg, email, nom);
        }
        if (!user.isActif()) {
            throw new SamlException("Compte désactivé.");
        }
        annuaire.synchroniser(user, qc.levis.contrat.auth.domain.AppartenanceAnnuaire.Origine.SSO, faits, cfg.roleParDefaut());
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String token = jwt.genererToken(user.getEmail(), roles, user.getTenantId());
        audit.log(user.getTenantId(), user.getEmail(), "LOGIN_SAML", "User", user.getId(), null);
        log.info("Connexion SAML réussie : utilisateur {} (tenant {}, rôles {})",
                user.getId(), user.getTenantId(), roles);
        return token;
    }

    private User verifierTenant(User u, ConfigSaml cfg) {
        if (u.getTenantId() != null && !u.getTenantId().equals(cfg.tenantId())) {
            throw new SamlException("Ce compte n'appartient pas à cet établissement.");
        }
        return u;
    }

    private User provisionner(ConfigSaml cfg, String email, String nom) {
        if (!cfg.autoProvision()) {
            log.warn("Connexion SAML refusée : courriel inconnu {} (auto-provisionnement désactivé)",
                    LogSafe.courriel(email));
            throw new SamlException("Compte inconnu et auto-provisionnement désactivé.");
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
        audit.log(sauve.getTenantId(), sauve.getEmail(), "PROVISION_SAML", "User", sauve.getId(), null);
        log.info("Compte SAML auto-provisionné : {} (tenant {})", sauve.getId(), sauve.getTenantId());
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
