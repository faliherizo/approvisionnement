package qc.levis.contrat.rapport.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.rapport.domain.TableauBord;

public interface TableauBordRepository extends JpaRepository<TableauBord, Long> {
    List<TableauBord> findByTenantIdOrderByNomAsc(Long tenantId);
    Optional<TableauBord> findByIdAndTenantId(Long id, Long tenantId);
}
