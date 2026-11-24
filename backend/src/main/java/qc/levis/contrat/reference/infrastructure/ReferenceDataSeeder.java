package qc.levis.contrat.reference.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.redaction.application.port.BibliothequeDeDepart;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;
import static qc.levis.contrat.tenant.infrastructure.TenantSeeder.DEFAULT_SLUG;

/** Amorce la bibliothèque de départ (clauses, templates) de l'organisation par défaut. */
@Component
@Order(3)
public class ReferenceDataSeeder implements CommandLineRunner {

    private final BibliothequeDeDepart bibliotheque;
    private final TenantRepository tenants;

    public ReferenceDataSeeder(BibliothequeDeDepart bibliotheque, TenantRepository tenants) {
        this.bibliotheque = bibliotheque;
        this.tenants = tenants;
    }

    @Override
    public void run(String... args) {
        bibliotheque.amorcer(tenants.findBySlug(DEFAULT_SLUG).map(Tenant::getId).orElse(null));
    }
}
