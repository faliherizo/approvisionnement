package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.RapportClm;

public interface RapportClmRepository extends JpaRepository<RapportClm, Long> {
    List<RapportClm> findByTenantIdOrderByNomAsc(Long tenantId);

    Optional<RapportClm> findByIdAndTenantId(Long id, Long tenantId);
}
