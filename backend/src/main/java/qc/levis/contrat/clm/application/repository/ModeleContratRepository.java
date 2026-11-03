package qc.levis.contrat.clm.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleContrat;

public interface ModeleContratRepository extends JpaRepository<ModeleContrat, Long> {
    Optional<ModeleContrat> findByIdAndTenantId(Long id, Long tenantId);

    List<ModeleContrat> findByTenantIdOrderByNomAscNumeroDesc(Long tenantId);

    List<ModeleContrat> findByTenantIdAndCleModeleOrderByNumeroDesc(Long tenantId, String cleModele);

    List<ModeleContrat> findByTenantIdAndCleModeleAndStatut(Long tenantId, String cleModele, Modele.Statut statut);
}
