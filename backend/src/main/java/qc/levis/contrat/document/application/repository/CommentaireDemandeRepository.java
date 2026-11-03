package qc.levis.contrat.document.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.document.domain.CommentaireDemande;

public interface CommentaireDemandeRepository extends JpaRepository<CommentaireDemande, Long> {
    List<CommentaireDemande> findByTenantIdAndSaisieIdOrderByCreeLeAsc(Long tenantId, Long saisieId);
}
