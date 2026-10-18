package qc.levis.contrat.integration.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.integration.domain.IntegrationSaml;

public interface IntegrationSamlRepository extends JpaRepository<IntegrationSaml, Long> {
    Optional<IntegrationSaml> findByTenantId(Long tenantId);
}
