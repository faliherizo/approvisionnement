package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.ClientIntegration;

public interface ClientIntegrationRepository extends JpaRepository<ClientIntegration, Long> {
    Optional<ClientIntegration> findByClientId(String clientId);

    Optional<ClientIntegration> findByIdAndTenantId(Long id, Long tenantId);

    List<ClientIntegration> findByTenantIdOrderByCreeLeDesc(Long tenantId);
}
