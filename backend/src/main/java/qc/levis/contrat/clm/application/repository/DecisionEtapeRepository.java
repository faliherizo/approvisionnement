package qc.levis.contrat.clm.application.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.clm.domain.DecisionEtape;
import qc.levis.contrat.clm.domain.EtapeTrousse;

public interface DecisionEtapeRepository extends JpaRepository<DecisionEtape, Long> {
    List<DecisionEtape> findByTenantIdAndEtapeIdOrderByDecideLeAsc(Long tenantId, Long etapeId);

    List<DecisionEtape> findByTenantIdAndTrousseIdOrderByDecideLeAsc(Long tenantId, Long trousseId);

    List<DecisionEtape> findTop50ByTenantIdAndActeurOrderByDecideLeDesc(Long tenantId, String acteur);

    /**
     * Décisions du cycle en cours : une reprise ou un retour pour correction réactive l'étape, dont le quorum, la
     * politique de rejet et le « déjà décidé » repartent alors de zéro. Les décisions antérieures restent à l'historique.
     */
    default List<DecisionEtape> duCycleEnCours(EtapeTrousse e) {
        Instant depuis = e.getActiveeLe();
        return findByTenantIdAndEtapeIdOrderByDecideLeAsc(e.getTenantId(), e.getId()).stream()
                .filter(d -> depuis == null || !d.getDecideLe().isBefore(depuis)).toList();
    }
}
