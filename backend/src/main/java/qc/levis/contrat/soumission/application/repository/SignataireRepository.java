package qc.levis.contrat.soumission.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.soumission.domain.Signataire;

public interface SignataireRepository extends JpaRepository<Signataire, Long> {
    List<Signataire> findByDocumentId(Long documentId);
    Optional<Signataire> findByJeton(String jeton);
}
