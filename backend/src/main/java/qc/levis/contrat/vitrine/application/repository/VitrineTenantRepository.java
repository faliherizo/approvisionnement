package qc.levis.contrat.vitrine.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.vitrine.domain.VitrineTenant;

public interface VitrineTenantRepository extends JpaRepository<VitrineTenant, Long> {
    Optional<VitrineTenant> findByTenantId(Long tenantId);
}
