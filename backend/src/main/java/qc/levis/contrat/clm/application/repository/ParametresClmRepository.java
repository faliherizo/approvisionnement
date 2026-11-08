package qc.levis.contrat.clm.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.ParametresClm;

public interface ParametresClmRepository extends JpaRepository<ParametresClm, Long> {
    Optional<ParametresClm> findByTenantId(Long tenantId);
}
