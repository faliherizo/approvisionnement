package qc.levis.contrat.auth.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.PolitiqueConnexion;

public interface PolitiqueConnexionRepository extends JpaRepository<PolitiqueConnexion, Long> {
}
