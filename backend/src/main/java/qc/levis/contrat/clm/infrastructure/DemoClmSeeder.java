package qc.levis.contrat.clm.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

/**
 * Environnement de démonstration : au démarrage, chaque organisation qui a un jeu de démonstration CLM reçoit ce qui lui
 * en manque (DemonstrationClm). L'administration de l'organisation peut aussi le charger elle-même depuis ses paramètres.
 */
@Component
@Order(8)
@ConditionalOnProperty(prefix = "app.amorcage", name = "tenants-demo", havingValue = "true")
public class DemoClmSeeder implements CommandLineRunner {

    private final DemonstrationClm demonstration;
    private final ContexteClm ctx;
    private final TenantRepository tenants;

    public DemoClmSeeder(DemonstrationClm demonstration, ContexteClm ctx, TenantRepository tenants) {
        this.demonstration = demonstration;
        this.ctx = ctx;
        this.tenants = tenants;
    }

    @Override
    public void run(String... args) {
        for (String slug : demonstration.organisations()) {
            tenants.findBySlug(slug).ifPresent(t -> ctx.dansTenant(t.getId(), () -> demonstration.charger(slug)));
        }
    }
}
