package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.VersionNegociation;

public interface VersionNegociationRepository extends JpaRepository<VersionNegociation, Long> {
    List<VersionNegociation> findByTenantIdAndNegociationIdOrderByNumeroAsc(Long tenantId, Long negociationId);

    Optional<VersionNegociation> findByTenantIdAndNegociationIdAndNumero(Long tenantId, Long negociationId, int numero);
}
