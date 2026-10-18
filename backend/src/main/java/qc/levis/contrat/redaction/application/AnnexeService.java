package qc.levis.contrat.redaction.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.redaction.application.dto.AnnexeRequest;
import qc.levis.contrat.redaction.application.repository.DocumentAnnexeRepository;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentAnnexe;

/** Annexes d'un document. L'accès est cadré par le tenant via {@link DocumentService}. */
@Service
public class AnnexeService {

    private final DocumentAnnexeRepository annexes;
    private final DocumentService documents;
    private final AuditService audit;

    public AnnexeService(DocumentAnnexeRepository annexes, DocumentService documents, AuditService audit) {
        this.annexes = annexes;
        this.documents = documents;
        this.audit = audit;
    }

    public List<DocumentAnnexe> lister(Long documentId) {
        documents.get(documentId); // vérifie l'appartenance au tenant
        return annexes.findByDocumentIdOrderByOrdreAsc(documentId);
    }

    @Transactional
    public DocumentAnnexe ajouter(Long documentId, AnnexeRequest req, String utilisateur) {
        documents.get(documentId);
        DocumentAnnexe a = new DocumentAnnexe();
        a.setDocumentId(documentId);
        a.setNumero(req.numero());
        a.setTitre(req.titre());
        a.setReferenceClause(req.referenceClause());
        a.setContenu(req.contenu());
        a.setOrdre(annexes.findByDocumentIdOrderByOrdreAsc(documentId).size());
        DocumentAnnexe saved = annexes.save(a);
        audit.log(utilisateur, "AJOUT_ANNEXE", "Document", documentId, req.titre());
        return saved;
    }

    @Transactional
    public void supprimer(Long documentId, Long annexeId) {
        documents.get(documentId);
        annexes.findById(annexeId)
                .filter(a -> a.getDocumentId().equals(documentId))
                .ifPresent(annexes::delete);
    }
}
