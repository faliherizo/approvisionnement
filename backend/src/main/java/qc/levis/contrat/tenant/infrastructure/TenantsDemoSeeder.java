package qc.levis.contrat.tenant.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import qc.levis.contrat.redaction.application.port.BibliothequeDeDepart;
import org.springframework.stereotype.Component;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Organisations de démonstration ({@code data/tenants-demo.json}) : tenant, modules souscrits, valeurs propres de ses listes
 * de référence et un compte par profil demandé par le client. Idempotent : modules et listes ne s'appliquent qu'à la création
 * du tenant (le superadmin et l'administration ont pu les changer depuis) et un compte existant n'est jamais modifié.
 * S'exécute après le catalogue des modules et la déclaration des listes ; inactif en production.
 */
@Component
@Order(7)
@ConditionalOnProperty(prefix = "app.amorcage", name = "tenants-demo", havingValue = "true")
public class TenantsDemoSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantsDemoSeeder.class);
    private static final String PAR = "amorçage";

    /** Compte de démonstration ; {@code profil} nomme l'acteur du client que le compte incarne. */
    public record CompteDemo(String email, String nom, String direction, String profil, List<String> roles) {
    }

    /** Valeur propre à l'organisation dans une liste de référence ; un code de la plateforme la personnalise. */
    public record ValeurDemo(String code, String libelle, String detail) {
    }

    /** Modules dans l'ordre d'application : activer une option tire ses prérequis, désactiver retire ses dépendants. */
    /** {@code bibliotheque} : l'organisation reçoit à sa création la bibliothèque de départ (clauses et templates). */
    public record TenantDemo(String slug, String titre, String sousTitre, String motDePasse, Map<String, Boolean> modules,
                             Map<String, List<ValeurDemo>> listes, List<CompteDemo> comptes, Boolean bibliotheque) {
    }

    private final TenantRepository tenants;
    private final UserRepository users;
    private final ModuleService modules;
    private final ReferenceService references;
    private final PasswordEncoder encoder;
    private final BibliothequeDeDepart bibliotheque;
    private final ObjectMapper json = new ObjectMapper();

    public TenantsDemoSeeder(TenantRepository tenants, UserRepository users, ModuleService modules, ReferenceService references,
                             PasswordEncoder encoder, BibliothequeDeDepart bibliotheque) {
        this.tenants = tenants;
        this.users = users;
        this.modules = modules;
        this.references = references;
        this.encoder = encoder;
        this.bibliotheque = bibliotheque;
    }

    @Override
    public void run(String... args) throws IOException {
        for (TenantDemo demo : lire()) {
            Tenant t = tenants.findBySlug(demo.slug()).orElseGet(() -> creer(demo));
            if (demo.listes() != null) {
                listes(t.getId(), demo.listes());
            }
            if (Boolean.TRUE.equals(demo.bibliotheque())) {
                bibliotheque.amorcer(t.getId());
            }
            for (CompteDemo c : demo.comptes()) {
                if (!users.existsByEmail(c.email())) {
                    users.save(compte(c, demo.motDePasse(), t.getId()));
                    log.info("Compte de démonstration créé : profil « {} », rôles {}", c.profil(), c.roles());
                }
            }
        }
    }

    private List<TenantDemo> lire() throws IOException {
        try (InputStream in = new ClassPathResource("data/tenants-demo.json").getInputStream()) {
            return json.readValue(in, json.getTypeFactory().constructCollectionType(List.class, TenantDemo.class));
        }
    }

    private Tenant creer(TenantDemo demo) {
        Tenant t = tenants.save(new Tenant(demo.slug(), demo.titre(), demo.sousTitre()));
        demo.modules().forEach((code, actif) -> {
            if (Boolean.TRUE.equals(actif)) {
                modules.activer(t.getId(), code, PAR);
            } else {
                modules.desactiver(t.getId(), code, PAR);
            }
        });
        log.info("Organisation de démonstration créée : {} (modules {})", demo.slug(), demo.modules());
        return t;
    }

    /**
     * Valeurs créées comme le ferait l'administration de l'organisation : chaque liste exige son module souscrit. Seules
     * les valeurs absentes (par code) sont ajoutées, y compris dans une organisation déjà en place : ce que
     * l'administration a renommé, retiré ou ajouté n'est jamais touché.
     */
    private void listes(Long tenantId, Map<String, List<ValeurDemo>> listes) {
        TenantContext.set(tenantId);
        try {
            for (Map.Entry<String, List<ValeurDemo>> liste : listes.entrySet()) {
                java.util.Set<String> presents = references.lister(liste.getKey(), false).stream()
                        .map(r -> r.code()).collect(Collectors.toSet());
                int ordre = 0;
                for (ValeurDemo v : liste.getValue()) {
                    ordre += 10;
                    if (!presents.contains(v.code())) {
                        references.creer(new ReferenceRequest(liste.getKey(), v.code(), v.libelle(), v.detail(), ordre, true), false);
                    }
                }
            }
        } finally {
            TenantContext.clear();
        }
    }

    private User compte(CompteDemo c, String motDePasse, Long tenantId) {
        User u = new User();
        u.setEmail(c.email());
        u.setNom(c.nom());
        u.setDirection(c.direction());
        u.setMotDePasseHash(encoder.encode(motDePasse));
        u.setRoles(c.roles().stream().map(Enums.Role::valueOf).collect(Collectors.toSet()));
        u.setTenantId(tenantId);
        return u;
    }
}
