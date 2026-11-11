package qc.levis.contrat.clm.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.ElementLot;

public interface ElementLotRepository extends JpaRepository<ElementLot, Long> {
    List<ElementLot> findByTenantIdAndLotIdOrderByRangAsc(Long tenantId, Long lotId);
}
