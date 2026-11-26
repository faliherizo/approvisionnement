package qc.levis.contrat.clm.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.DiffusionM365;

public interface DiffusionM365Repository extends JpaRepository<DiffusionM365, Long> {
    Optional<DiffusionM365> findByTenantId(Long tenantId);
}
