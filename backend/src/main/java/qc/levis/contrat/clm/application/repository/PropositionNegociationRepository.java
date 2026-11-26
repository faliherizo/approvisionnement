package qc.levis.contrat.clm.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.PropositionNegociation;

public interface PropositionNegociationRepository extends JpaRepository<PropositionNegociation, Long> {
    List<PropositionNegociation> findByTenantIdAndNegociationIdOrderByIdAsc(Long tenantId, Long negociationId);

    List<PropositionNegociation> findByTenantIdAndNegociationIdAndTourOrderByIdAsc(Long tenantId, Long negociationId, int tour);
}
