package qc.levis.contrat.soumission.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.soumission.domain.Bordereau;

public interface BordereauRepository extends JpaRepository<Bordereau, Long> {
    Optional<Bordereau> findByDocumentId(Long documentId);
}
