package qc.levis.contrat.soutien.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.soutien.domain.Mouvement;

public interface MouvementRepository extends JpaRepository<Mouvement, Long> {
    List<Mouvement> findByBanqueIdOrderByDateAscIdAsc(Long banqueId);
}
