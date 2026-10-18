package qc.levis.contrat.soumission.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.soumission.application.dto.BordereauRequest;
import qc.levis.contrat.soumission.application.dto.LigneRequest;
import qc.levis.contrat.soumission.application.repository.BordereauRepository;
import qc.levis.contrat.soumission.domain.Bordereau;
import qc.levis.contrat.soumission.domain.LignePrix;

/** Bordereau de prix d'un document (tableau tarifaire). Accès cadré par le tenant. */
@Service
public class BordereauService {

    private final BordereauRepository bordereaux;
    private final DocumentService documents;
    private final AuditService audit;

    public BordereauService(BordereauRepository bordereaux, DocumentService documents, AuditService audit) {
        this.bordereaux = bordereaux;
        this.documents = documents;
        this.audit = audit;
    }

    public Bordereau get(Long documentId) {
        documents.get(documentId); // vérifie l'appartenance au tenant
        return bordereaux.findByDocumentId(documentId).orElse(null);
    }

    /** Crée ou remplace le bordereau (et ses lignes) d'un document. */
    @Transactional
    public Bordereau enregistrer(Long documentId, BordereauRequest req, String utilisateur) {
        documents.get(documentId);
        Bordereau b = bordereaux.findByDocumentId(documentId).orElseGet(() -> {
            Bordereau nb = new Bordereau();
            nb.setDocumentId(documentId);
            return nb;
        });
        b.setTitre(req.titre());
        b.getLignes().clear();
        if (req.lignes() != null) {
            for (LigneRequest l : req.lignes()) {
                LignePrix lp = new LignePrix();
                lp.setDescription(l.description());
                lp.setQuantite(l.quantite());
                lp.setTaux(l.taux());
                lp.setOrdre(b.getLignes().size());
                b.addLigne(lp);
            }
        }
        Bordereau saved = bordereaux.save(b);
        audit.log(utilisateur, "ENREGISTRER_BORDEREAU", "Document", documentId,
                b.getLignes().size() + " ligne(s)");
        return saved;
    }
}
