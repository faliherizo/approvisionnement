package qc.levis.contrat.auth.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.auth.domain.JetonScim;

public interface JetonScimRepository extends JpaRepository<JetonScim, Long> {
    Optional<JetonScim> findByEmpreinte(String empreinte);
}
