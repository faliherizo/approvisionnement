package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.AccesSoutien;

public interface AccesSoutienRepository extends JpaRepository<AccesSoutien, Long> {
    List<AccesSoutien> findTop100ByTenantIdOrderByDemandeLeDesc(Long tenantId);

    Optional<AccesSoutien> findByIdAndTenantId(Long id, Long tenantId);

    List<AccesSoutien> findByTenantIdAndDemandeurIgnoreCaseOrderByDemandeLeDesc(Long tenantId, String demandeur);
}
