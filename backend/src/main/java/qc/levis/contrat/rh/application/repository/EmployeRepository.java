package qc.levis.contrat.rh.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.rh.domain.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    List<Employe> findByTenantIdOrderByNomAscPrenomAsc(Long tenantId);
    Optional<Employe> findByIdAndTenantId(Long id, Long tenantId);
    long countByTenantId(Long tenantId);
}
