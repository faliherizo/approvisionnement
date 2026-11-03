package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByIdAndTenantId(Long id, Long tenantId);

    List<Participant> findByTenantIdAndTrousseIdOrderByIdAsc(Long tenantId, Long trousseId);

    List<Participant> findByTenantIdAndCourrielIgnoreCase(Long tenantId, String courriel);
}
