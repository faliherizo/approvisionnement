package qc.levis.contrat.temps.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.temps.domain.TacheProjet;

public interface TacheProjetRepository extends JpaRepository<TacheProjet, Long> {
    List<TacheProjet> findByTenantIdOrderByCreeLeDesc(Long tenantId);
    List<TacheProjet> findByTenantIdAndProjetIdOrderByCreeLeDesc(Long tenantId, Long projetId);
    List<TacheProjet> findByTenantIdAndAssigneIdOrderByCreeLeDesc(Long tenantId, Long assigneId);
    Optional<TacheProjet> findByIdAndTenantId(Long id, Long tenantId);
}
