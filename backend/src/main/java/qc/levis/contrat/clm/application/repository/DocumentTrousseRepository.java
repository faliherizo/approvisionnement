package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.DocumentTrousse;

public interface DocumentTrousseRepository extends JpaRepository<DocumentTrousse, Long> {
    Optional<DocumentTrousse> findByIdAndTenantId(Long id, Long tenantId);

    List<DocumentTrousse> findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(Long tenantId, Long trousseId);

    List<DocumentTrousse> findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(Long tenantId, Long trousseId, String cleDocument);
}
