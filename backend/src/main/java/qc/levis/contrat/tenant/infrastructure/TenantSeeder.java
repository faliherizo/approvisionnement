package qc.levis.contrat.tenant.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/** Amorce le tenant par défaut (Ville de Lévis). Doit s'exécuter avant les autres seeders. */
@Component
@Order(1)
public class TenantSeeder implements CommandLineRunner {

    /** Slug du tenant par défaut sous lequel sont rattachées les données existantes. */
    public static final String DEFAULT_SLUG = "levis";

    private final TenantRepository tenants;

    public TenantSeeder(TenantRepository tenants) {
        this.tenants = tenants;
    }

    @Override
    public void run(String... args) {
        if (tenants.findBySlug(DEFAULT_SLUG).isEmpty()) {
            tenants.save(new Tenant(DEFAULT_SLUG, "Ville de Lévis",
                    "Rédaction contractuelle · Approvisionnement"));
        }
    }
}
