package qc.levis.contrat.clm.application.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.EtatNegociation;
import qc.levis.contrat.clm.domain.Negociation;

public interface NegociationRepository extends JpaRepository<Negociation, Long> {
    Optional<Negociation> findByJetonHash(String jetonHash);

    Optional<Negociation> findByIdAndTenantId(Long id, Long tenantId);

    List<Negociation> findByTenantIdAndTrousseIdOrderByIdDesc(Long tenantId, Long trousseId);

    boolean existsByTenantIdAndTrousseIdAndEtatIn(Long tenantId, Long trousseId, Collection<EtatNegociation> etats);
}
