package qc.levis.contrat.tenant.application;

import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.tenant.application.dto.TarifTenantRequest;
import qc.levis.contrat.tenant.application.repository.TarifTenantRepository;
import qc.levis.contrat.tenant.domain.TarifTenant;

/**
 * Gestion de la tarification d'abonnement par tenant (réservé au superadmin).
 * Responsabilité unique : lire/écrire le tarif d'un tenant, avec validation des bornes (SRP).
 */
@Service
public class TarifTenantService {

    private static final Set<String> DEVISES = Set.of("CAD", "USD", "EUR");
    private static final BigDecimal INDEXATION_MAX = new BigDecimal("100");

    private final TarifTenantRepository tarifs;

    public TarifTenantService(TarifTenantRepository tarifs) {
        this.tarifs = tarifs;
    }

    /** Tarif d'un tenant, ou un tarif vierge (valeurs par défaut) si non défini. */
    public TarifTenant pour(Long tenantId) {
        return tarifs.findByTenantId(tenantId).orElseGet(() -> {
            TarifTenant vide = new TarifTenant();
            vide.setTenantId(tenantId);
            return vide;
        });
    }

    @Transactional
    public TarifTenant enregistrer(Long tenantId, TarifTenantRequest req) {
        TarifTenant t = tarifs.findByTenantId(tenantId).orElseGet(TarifTenant::new);
        t.setTenantId(tenantId);
        t.setForfaitAnnuel(montantValide(req.forfaitAnnuel()));
        t.setDevise(deviseValide(req.devise()));
        t.setIndexationPct(indexationValide(req.indexationPct()));
        t.setNbUtilisateursInclus(req.nbUtilisateursInclus());
        t.setNotes(req.notes());
        t.setActif(req.actif() == null || req.actif());
        return tarifs.save(t);
    }

    private BigDecimal montantValide(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) return BigDecimal.ZERO;
        return montant;
    }

    private String deviseValide(String devise) {
        String d = devise == null ? "" : devise.toUpperCase();
        return DEVISES.contains(d) ? d : "CAD";
    }

    private BigDecimal indexationValide(BigDecimal pct) {
        if (pct == null || pct.signum() < 0) return BigDecimal.ZERO;
        return pct.min(INDEXATION_MAX);
    }
}
