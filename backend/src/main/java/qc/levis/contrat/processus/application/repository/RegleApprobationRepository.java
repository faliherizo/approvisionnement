package qc.levis.contrat.processus.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.RegleApprobation;

public interface RegleApprobationRepository extends JpaRepository<RegleApprobation, Long> {
    List<RegleApprobation> findByTenantIdAndDocumentType(Long tenantId, String documentType);
    List<RegleApprobation> findByTenantIdOrderByDocumentTypeAscNiveauAsc(Long tenantId);
}
