package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.RegleConservation;

public interface RegleConservationRepository extends JpaRepository<RegleConservation, Long> {
    List<RegleConservation> findByTenantIdOrderByTypeContratAsc(Long tenantId);

    Optional<RegleConservation> findByIdAndTenantId(Long id, Long tenantId);
}
