package qc.levis.contrat.redaction.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.redaction.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentIdOrderByCreeLeAsc(Long documentId);

    /** Commentaires écrits ou résolus par une personne, dans les documents de son organisation. */
    @org.springframework.data.jpa.repository.Query("""
            select c from Comment c where (lower(c.auteur) = lower(:courriel) or lower(c.resoluPar) = lower(:courriel))
              and c.documentId in (select d.id from Document d where d.tenantId = :tenantId)
            """)
    List<Comment> deLaPersonne(@org.springframework.data.repository.query.Param("tenantId") Long tenantId,
                               @org.springframework.data.repository.query.Param("courriel") String courriel);
}
