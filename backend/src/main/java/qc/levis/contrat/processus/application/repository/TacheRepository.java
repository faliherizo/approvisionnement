package qc.levis.contrat.processus.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.processus.domain.StatutTache;
import qc.levis.contrat.processus.domain.Tache;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    Optional<Tache> findByIdAndTenantId(Long id, Long tenantId);
    List<Tache> findByTenantIdAndSaisieIdOrderByOrdreAsc(Long tenantId, Long saisieId);
    List<Tache> findByTenantIdAndAssigneRoleInAndStatutInOrderByEcheanceLeAsc(
            Long tenantId, List<String> roles, List<StatutTache> statuts);
}
