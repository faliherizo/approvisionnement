package qc.levis.contrat.integration.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.integration.domain.IntegrationOidc;

public interface IntegrationOidcRepository extends JpaRepository<IntegrationOidc, Long> {
    Optional<IntegrationOidc> findByTenantId(Long tenantId);
}
