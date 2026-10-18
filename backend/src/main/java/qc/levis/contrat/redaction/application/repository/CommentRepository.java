package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentIdOrderByCreeLeAsc(Long documentId);
}
