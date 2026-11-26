package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.PolitiquePurview;

public interface PolitiquePurviewRepository extends JpaRepository<PolitiquePurview, Long> {
    List<PolitiquePurview> findByTenantIdOrderByTypeContratAsc(Long tenantId);

    Optional<PolitiquePurview> findByIdAndTenantId(Long id, Long tenantId);
}
