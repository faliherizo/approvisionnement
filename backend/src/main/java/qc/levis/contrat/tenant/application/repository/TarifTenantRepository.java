package qc.levis.contrat.tenant.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.tenant.domain.TarifTenant;

public interface TarifTenantRepository extends JpaRepository<TarifTenant, Long> {
    Optional<TarifTenant> findByTenantId(Long tenantId);
}
