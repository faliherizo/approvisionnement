package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.DepotM365;

public interface DepotM365Repository extends JpaRepository<DepotM365, Long> {
    boolean existsByCleIdempotence(String cle);

    Optional<DepotM365> findByIdAndTenantId(Long id, Long tenantId);

    List<DepotM365> findTop100ByTenantIdOrderByCreeLeDesc(Long tenantId);

    List<DepotM365> findByTenantIdAndTrousseIdOrderByIdAsc(Long tenantId, Long trousseId);

    List<DepotM365> findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(DepotM365.Statut statut, Instant maintenant);
}
