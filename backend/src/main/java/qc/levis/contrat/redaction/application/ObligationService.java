package qc.levis.contrat.redaction.application;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.ObligationRequest;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.ObligationRepository;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.Obligation;

/** Obligations & échéances contractuelles (post-exécution) — écart CLM E-5. */
@Service
public class ObligationService {

    private final ObligationRepository obligations;
    private final DocumentRepository documents;
    /** Devis bornés à l'organisation courante (OWASP A01). */
    private final qc.levis.contrat.redaction.application.DocumentsDuTenant portee;
    private final AuditService audit;

    public ObligationService(ObligationRepository obligations, DocumentRepository documents,
                             AuditService audit) {
        this.obligations = obligations;
        this.documents = documents;
        this.portee = new qc.levis.contrat.redaction.application.DocumentsDuTenant(documents);
        this.audit = audit;
    }

    public List<Obligation> lister(Long documentId) {
        portee.exiger(documentId);
        return obligations.findByDocumentIdOrderByDateEcheanceAsc(documentId);
    }

    /** Obligations du tenant courant dont l'échéance tombe dans les {@code jours} prochains. */
    public List<Obligation> aVenir(int jours) {
        return obligations.findByTenantIdAndDateEcheanceLessThanEqualOrderByDateEcheanceAsc(
                TenantContext.get(), LocalDate.now().plusDays(jours)).stream()
                .filter(o -> o.getStatut() != Enums.ObligationStatut.COMPLETE)
                .toList();
    }

    @Transactional
    public Obligation ajouter(Long documentId, ObligationRequest req, String utilisateur) {
        Document doc = portee.exiger(documentId);
        Obligation o = new Obligation();
        o.setDocumentId(documentId);
        o.setTitre(req.titre());
        o.setDescription(req.description());
        o.setType(req.type());
        o.setDateEcheance(req.dateEcheance());
        o.setResponsable(req.responsable());
        o.setTenantId(doc.getTenantId());
        o.setStatut(calculerStatut(req.dateEcheance()));
        Obligation saved = obligations.save(o);
        audit.log(utilisateur, "AJOUT_OBLIGATION", "Document", documentId, req.titre());
        return saved;
    }

    @Transactional
    public Obligation completer(Long id, String utilisateur) {
        Obligation o = obligations.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Obligation introuvable: " + id));
        portee.exiger(o.getDocumentId());
        o.setStatut(Enums.ObligationStatut.COMPLETE);
        Obligation saved = obligations.save(o);
        audit.log(utilisateur, "COMPLETER_OBLIGATION", "Obligation", id, null);
        return saved;
    }

    private Enums.ObligationStatut calculerStatut(LocalDate echeance) {
        if (echeance != null && echeance.isBefore(LocalDate.now())) {
            return Enums.ObligationStatut.ECHU;
        }
        return Enums.ObligationStatut.A_VENIR;
    }
}
