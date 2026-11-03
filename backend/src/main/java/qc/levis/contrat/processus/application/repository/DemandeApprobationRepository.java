package qc.levis.contrat.processus.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.DemandeApprobation;

public interface DemandeApprobationRepository extends JpaRepository<DemandeApprobation, Long> {
    Optional<DemandeApprobation> findByIdAndTenantId(Long id, Long tenantId);
    List<DemandeApprobation> findByTenantIdOrderByCreeLeDesc(Long tenantId);
}
