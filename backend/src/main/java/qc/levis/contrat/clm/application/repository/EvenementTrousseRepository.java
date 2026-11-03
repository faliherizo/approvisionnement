package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.EvenementTrousse;

public interface EvenementTrousseRepository extends JpaRepository<EvenementTrousse, Long> {
    List<EvenementTrousse> findByTenantIdAndTrousseIdOrderBySequenceAsc(Long tenantId, Long trousseId);

    Optional<EvenementTrousse> findTopByTenantIdAndTrousseIdOrderBySequenceDesc(Long tenantId, Long trousseId);

    List<EvenementTrousse> findTop30ByTenantIdOrderByHorodatageDesc(Long tenantId);

    List<EvenementTrousse> findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(Long tenantId, Instant du, Instant au);
}
