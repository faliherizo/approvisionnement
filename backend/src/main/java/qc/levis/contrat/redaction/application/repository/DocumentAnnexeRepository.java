package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.DocumentAnnexe;

public interface DocumentAnnexeRepository extends JpaRepository<DocumentAnnexe, Long> {
    List<DocumentAnnexe> findByDocumentIdOrderByOrdreAsc(Long documentId);
}
