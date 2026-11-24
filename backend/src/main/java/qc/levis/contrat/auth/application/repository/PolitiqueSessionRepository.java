package qc.levis.contrat.auth.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.PolitiqueSession;

public interface PolitiqueSessionRepository extends JpaRepository<PolitiqueSession, Long> {
}
