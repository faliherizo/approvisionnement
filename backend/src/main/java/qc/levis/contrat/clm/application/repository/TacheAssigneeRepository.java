package qc.levis.contrat.clm.application.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.TacheAssignee;

public interface TacheAssigneeRepository extends JpaRepository<TacheAssignee, Long> {
    Optional<TacheAssignee> findByIdAndTenantId(Long id, Long tenantId);

    List<TacheAssignee> findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(
            Long tenantId, String assigneA, Collection<TacheAssignee.Statut> statuts);

    List<TacheAssignee> findByTenantIdAndCreeParIgnoreCaseOrderByIdDesc(Long tenantId, String creePar);

    List<TacheAssignee> findByTenantIdAndTrousseIdOrderByIdAsc(Long tenantId, Long trousseId);
}
