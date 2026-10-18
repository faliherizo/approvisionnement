package qc.levis.contrat.integration.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.integration.domain.IntegrationSap;

public interface IntegrationSapRepository extends JpaRepository<IntegrationSap, Long> {
    Optional<IntegrationSap> findByTenantId(Long tenantId);
}
