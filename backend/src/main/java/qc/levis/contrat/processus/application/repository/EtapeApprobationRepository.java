package qc.levis.contrat.processus.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.EtapeApprobation;

public interface EtapeApprobationRepository extends JpaRepository<EtapeApprobation, Long> {
    Optional<EtapeApprobation> findByIdAndTenantId(Long id, Long tenantId);
    List<EtapeApprobation> findByTenantIdAndDemandeIdOrderByNiveauAsc(Long tenantId, Long demandeId);
    List<EtapeApprobation> findByTenantIdAndStatutAndApprobateurRoleIn(Long tenantId, String statut, List<String> roles);
}
