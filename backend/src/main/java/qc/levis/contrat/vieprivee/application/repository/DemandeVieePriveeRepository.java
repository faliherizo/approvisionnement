package qc.levis.contrat.vieprivee.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.vieprivee.domain.DemandeVieePrivee;

public interface DemandeVieePriveeRepository extends JpaRepository<DemandeVieePrivee, Long> {
    List<DemandeVieePrivee> findByTenantIdOrderByTraiteeLeDesc(Long tenantId);
}
