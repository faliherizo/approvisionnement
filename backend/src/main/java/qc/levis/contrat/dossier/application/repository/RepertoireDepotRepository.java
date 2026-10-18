package qc.levis.contrat.dossier.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.dossier.domain.RepertoireDepot;

public interface RepertoireDepotRepository extends JpaRepository<RepertoireDepot, Long> {
    List<RepertoireDepot> findByUserIdOrderByCheminAsc(Long userId);
    boolean existsByUserIdAndChemin(Long userId, String chemin);
    Optional<RepertoireDepot> findByIdAndUserId(Long id, Long userId);
}
