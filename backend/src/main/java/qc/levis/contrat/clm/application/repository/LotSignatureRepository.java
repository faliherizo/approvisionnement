package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.LotSignature;

public interface LotSignatureRepository extends JpaRepository<LotSignature, Long> {
    Optional<LotSignature> findByIdAndTenantId(Long id, Long tenantId);

    List<LotSignature> findTop50ByTenantIdOrderByCreeLeDesc(Long tenantId);
}
