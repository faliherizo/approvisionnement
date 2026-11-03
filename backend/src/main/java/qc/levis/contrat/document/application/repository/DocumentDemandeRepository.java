package qc.levis.contrat.document.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.document.domain.DocumentDemande;

public interface DocumentDemandeRepository extends JpaRepository<DocumentDemande, Long> {
    List<DocumentDemande> findByTenantIdAndSaisieIdOrderByUploadeLeDesc(Long tenantId, Long saisieId);
    Optional<DocumentDemande> findByIdAndTenantId(Long id, Long tenantId);
}
