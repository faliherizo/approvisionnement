package qc.levis.contrat.rapport.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.rapport.domain.CritereRechercheSauve;

public interface CritereRechercheSauveRepository extends JpaRepository<CritereRechercheSauve, Long> {
    List<CritereRechercheSauve> findByTenantIdAndUtilisateur(Long tenantId, String utilisateur);
}
