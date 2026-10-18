package qc.levis.contrat.dossier.application;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.addenda.domain.Addenda;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.dto.ActiviteUtilisateur;
import qc.levis.contrat.dossier.application.dto.RapportSuivi;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.redaction.application.ObligationService;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Rapports de suivi consolidés (Addenda 1, exig. 22) : <em>read-model</em> en lecture seule
 * (ADR-010) agrégeant, pour le tenant courant, le portefeuille de documents, les éléments d'action
 * (approbations en attente, obligations à venir), les dossiers actifs et l'activité par utilisateur.
 * Réutilise les dépôts existants — aucune table ni dénormalisation.
 */
@Service
public class RapportService {

    private static final int JOURS_OBLIGATIONS = 30;
    private static final int TOP_UTILISATEURS = 5;

    private final DocumentRepository documents;
    private final DossierRepository dossiers;
    private final ObligationService obligations;
    private final AuditLogRepository audit;

    public RapportService(DocumentRepository documents, DossierRepository dossiers,
                          ObligationService obligations, AuditLogRepository audit) {
        this.documents = documents;
        this.dossiers = dossiers;
        this.obligations = obligations;
        this.audit = audit;
    }

    public RapportSuivi suivi() {
        Long tenant = TenantContext.get();
        List<Document> docs = documents.findByTenantIdAndActifTrue(tenant);

        Map<String, Long> parStatut = new LinkedHashMap<>();
        Map<String, Long> parType = new LinkedHashMap<>();
        long approbationsEnAttente = 0;
        for (Document d : docs) {
            parStatut.merge(d.getStatut().name(), 1L, Long::sum);
            if (d.getType() != null) {
                parType.merge(d.getType().name(), 1L, Long::sum);
            }
            if (d.getStatut() == Enums.StatutDocument.EN_APPROBATION) {
                approbationsEnAttente++;
            }
        }

        long dossiersActifs = dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(tenant).size();
        long obligationsAVenir = obligations.aVenir(JOURS_OBLIGATIONS).size();

        Map<String, Long> parUtilisateur = new LinkedHashMap<>();
        for (AuditLog a : audit.rechercher(tenant, null, null, null, null, null)) {
            if (a.getUtilisateur() != null) {
                parUtilisateur.merge(a.getUtilisateur(), 1L, Long::sum);
            }
        }
        List<ActiviteUtilisateur> activite = parUtilisateur.entrySet().stream()
                .map(e -> new ActiviteUtilisateur(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(ActiviteUtilisateur::actions).reversed())
                .limit(TOP_UTILISATEURS)
                .toList();

        return new RapportSuivi(docs.size(), parStatut, parType,
                approbationsEnAttente, dossiersActifs, obligationsAVenir, activite);
    }
}
