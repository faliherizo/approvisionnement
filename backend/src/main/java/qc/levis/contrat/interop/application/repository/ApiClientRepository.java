package qc.levis.contrat.interop.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.interop.domain.ApiClient;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {
    Optional<ApiClient> findByClientId(String clientId);
    List<ApiClient> findByTenantId(Long tenantId);
}
