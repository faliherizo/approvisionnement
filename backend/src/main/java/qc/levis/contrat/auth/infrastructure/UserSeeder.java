package qc.levis.contrat.auth.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import qc.levis.contrat.auth.application.AuthService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.SecurityQuestion;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import static qc.levis.contrat.tenant.infrastructure.TenantSeeder.DEFAULT_SLUG;

/**
 * Amorce les comptes utilisateurs à partir de {@code data/users.json}.
 * Fait partie du module d'authentification (réutilisable, autonome).
 */
@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {

    /** Représentation JSON d'un utilisateur de démonstration. */
    public record UserSeed(String email, String nom, String direction, String motDePasse,
                           List<String> roles, String mfaMethode, String totpSecret,
                           List<QuestionSeed> questions, Boolean global) {}
    public record QuestionSeed(String question, String reponse) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthService auth;
    private final TenantRepository tenants;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserSeeder(UserRepository users, PasswordEncoder encoder, AuthService auth,
                      TenantRepository tenants) {
        this.users = users;
        this.encoder = encoder;
        this.auth = auth;
        this.tenants = tenants;
    }

    @Override
    public void run(String... args) throws Exception {
        if (users.count() > 0) return;
        Long tenantParDefaut = tenants.findBySlug(DEFAULT_SLUG).map(t -> t.getId()).orElse(null);
        for (UserSeed s : lire()) {
            users.save(versEntite(s, tenantParDefaut));
        }
    }

    private List<UserSeed> lire() throws Exception {
        try (InputStream in = new ClassPathResource("data/users.json").getInputStream()) {
            return mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, UserSeed.class));
        }
    }

    private User versEntite(UserSeed s, Long tenantParDefaut) {
        User u = new User();
        u.setEmail(s.email());
        u.setNom(s.nom());
        u.setDirection(s.direction());
        u.setMotDePasseHash(encoder.encode(s.motDePasse()));
        u.setRoles(s.roles().stream().map(Enums.Role::valueOf).collect(Collectors.toSet()));
        // Superadmin = global (tenantId null) ; les autres appartiennent au tenant par défaut.
        u.setTenantId(Boolean.TRUE.equals(s.global()) ? null : tenantParDefaut);

        Enums.MfaMethode methode = s.mfaMethode() == null
                ? Enums.MfaMethode.NONE : Enums.MfaMethode.valueOf(s.mfaMethode());
        u.setMfaMethode(methode);
        u.setMfaActive(methode != Enums.MfaMethode.NONE);
        u.setTotpSecret(s.totpSecret());
        if (s.questions() != null) {
            for (QuestionSeed q : s.questions()) {
                // Réutilise la normalisation/hachage du service d'auth (DRY).
                u.getQuestionsSecurite().add(new SecurityQuestion(q.question(), auth.hacher(q.reponse())));
            }
        }
        return u;
    }
}
