package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.RappelSortant;

public interface RappelSortantRepository extends JpaRepository<RappelSortant, Long> {
    boolean existsByCleIdempotence(String cle);

    Optional<RappelSortant> findByIdAndTenantId(Long id, Long tenantId);

    List<RappelSortant> findTop100ByTenantIdOrderByCreeLeDesc(Long tenantId);

    List<RappelSortant> findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(RappelSortant.Statut statut, Instant maintenant);
}
