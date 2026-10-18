package qc.levis.contrat.integration.application;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Transmet la référence financière d'un dossier d'approvisionnement vers SAP (art. 9). Réutilise
 * le {@link DossierService} pour le contexte et agrège la valeur estimée des documents rattachés.
 */
@Service
public class SapReferenceService {

    private static final Logger log = LoggerFactory.getLogger(SapReferenceService.class);

    private final DossierService dossiers;
    private final ReferenceFinancierePort sap;
    private final AuditService audit;

    public SapReferenceService(DossierService dossiers, ReferenceFinancierePort sap, AuditService audit) {
        this.dossiers = dossiers;
        this.sap = sap;
        this.audit = audit;
    }

    public ResultatPublication transmettre(Long dossierId, String utilisateur) {
        Dossier dossier = dossiers.get(dossierId);
        List<Document> docs = dossiers.documentsDu(dossierId);
        BigDecimal total = docs.stream()
                .map(Document::getValeurEstimee)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ResultatPublication resultat = sap.deposer(new ReferenceFinancierePort.ReferenceFinanciere(
                dossier.getId(), dossier.getNumeroAO(), dossier.getObjet(), docs.size(), total));

        audit.log(utilisateur, "REFERENCE_SAP", "Dossier", dossierId, resultat.url());
        log.info("Référence financière transmise à SAP : dossier {} (n° AO {}, {} document(s), succès={})",
                dossierId, dossier.getNumeroAO(), docs.size(), resultat.succes());
        return resultat;
    }
}
