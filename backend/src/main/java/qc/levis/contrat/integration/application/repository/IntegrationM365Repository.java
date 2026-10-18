package qc.levis.contrat.integration.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.integration.domain.IntegrationM365;

public interface IntegrationM365Repository extends JpaRepository<IntegrationM365, Long> {
    Optional<IntegrationM365> findByTenantId(Long tenantId);
}
