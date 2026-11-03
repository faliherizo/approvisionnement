package qc.levis.contrat.securite.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.securite.domain.Groupe;

public interface GroupeRepository extends JpaRepository<Groupe, Long> {
    List<Groupe> findByTenantId(Long tenantId);
}
