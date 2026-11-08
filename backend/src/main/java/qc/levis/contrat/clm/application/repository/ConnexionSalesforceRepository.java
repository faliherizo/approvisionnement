package qc.levis.contrat.clm.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;

public interface ConnexionSalesforceRepository extends JpaRepository<ConnexionSalesforce, Long> {
    Optional<ConnexionSalesforce> findByTenantId(Long tenantId);
}
