package qc.levis.contrat.dossier.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.dossier.domain.Dossier;

public interface DossierRepository extends JpaRepository<Dossier, Long> {
    List<Dossier> findByTenantIdAndActifTrueOrderByCreeLeDesc(Long tenantId);
}
