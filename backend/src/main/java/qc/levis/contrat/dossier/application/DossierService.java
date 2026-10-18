package qc.levis.contrat.dossier.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.dto.DossierRapport;
import qc.levis.contrat.dossier.application.dto.DossierRequest;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Orchestration des dossiers d'approvisionnement (art. 15) : regroupement de documents,
 * cycle de vie et rapports de suivi. Réutilise l'agrégat {@link Document} sans le dupliquer.
 */
@Service
public class DossierService {

    private final DossierRepository dossiers;
    private final DocumentRepository documents;
    private final AuditService audit;

    public DossierService(DossierRepository dossiers, DocumentRepository documents, AuditService audit) {
        this.dossiers = dossiers;
        this.documents = documents;
        this.audit = audit;
    }

    /** Dossiers actifs du tenant courant (isolation multi-tenant). */
    public List<Dossier> lister() {
        return dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(TenantContext.get());
    }

    /** Charge un dossier en vérifiant qu'il appartient au tenant courant. */
    public Dossier get(Long id) {
        Dossier d = dossiers.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable: " + id));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(d.getTenantId())) {
            throw new NoSuchElementException("Dossier introuvable: " + id);
        }
        return d;
    }

    @Transactional
    public Dossier creer(DossierRequest req, String utilisateur) {
        Dossier d = new Dossier();
        appliquer(d, req);
        d.setCreePar(utilisateur);
        d.setTenantId(TenantContext.get());
        d.setStatut(Enums.StatutDossier.OUVERT);
        Dossier saved = dossiers.save(d);
        audit.log(utilisateur, "CREER", "Dossier", saved.getId(), req.titre());
        return saved;
    }

    @Transactional
    public Dossier modifier(Long id, DossierRequest req, String utilisateur) {
        Dossier d = get(id);
        appliquer(d, req);
        Dossier saved = dossiers.save(d);
        audit.log(utilisateur, "MODIFIER", "Dossier", id, req.titre());
        return saved;
    }

    private void appliquer(Dossier d, DossierRequest req) {
        d.setTitre(req.titre());
        d.setNumeroAO(req.numeroAO());
        d.setObjet(req.objet());
        d.setTypeAcquisition(req.typeAcquisition());
        d.setModeSollicitation(req.modeSollicitation());
        d.setResponsable(req.responsable());
    }

    @Transactional
    public Dossier changerStatut(Long id, Enums.StatutDossier statut, String utilisateur) {
        Dossier d = get(id);
        d.changerStatut(statut);
        Dossier saved = dossiers.save(d);
        audit.log(utilisateur, "STATUT_DOSSIER", "Dossier", id, statut.name());
        return saved;
    }

    /** Suppression logique : le dossier est masqué mais ses documents sont détachés, pas supprimés. */
    @Transactional
    public void supprimer(Long id, String utilisateur) {
        Dossier d = get(id);
        for (Document doc : documents.findByDossierIdAndActifTrue(id)) {
            doc.setDossierId(null);
            documents.save(doc);
        }
        d.setActif(false);
        dossiers.save(d);
        audit.log(utilisateur, "SUPPRIMER", "Dossier", id, d.getTitre());
    }

    /** Documents (actifs) rattachés au dossier. */
    public List<Document> documentsDu(Long dossierId) {
        get(dossierId);
        return documents.findByDossierIdAndActifTrue(dossierId);
    }

    /** Rattache un document existant du même tenant au dossier. */
    @Transactional
    public Document rattacher(Long dossierId, Long documentId, String utilisateur) {
        get(dossierId);
        Document doc = documentDuTenant(documentId);
        doc.setDossierId(dossierId);
        Document saved = documents.save(doc);
        audit.log(utilisateur, "RATTACHER", "Document", documentId, "Dossier=" + dossierId);
        return saved;
    }

    /** Détache un document de son dossier (le document reste inchangé par ailleurs). */
    @Transactional
    public Document detacher(Long dossierId, Long documentId, String utilisateur) {
        get(dossierId);
        Document doc = documentDuTenant(documentId);
        if (!dossierId.equals(doc.getDossierId())) {
            throw new NoSuchElementException("Document non rattaché au dossier: " + documentId);
        }
        doc.setDossierId(null);
        Document saved = documents.save(doc);
        audit.log(utilisateur, "DETACHER", "Document", documentId, "Dossier=" + dossierId);
        return saved;
    }

    private Document documentDuTenant(Long documentId) {
        Document doc = documents.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document introuvable: " + documentId));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(doc.getTenantId())) {
            throw new NoSuchElementException("Document introuvable: " + documentId);
        }
        return doc;
    }

    /** Rapport de suivi agrégé du dossier (art. 15). */
    public DossierRapport rapport(Long dossierId) {
        Dossier d = get(dossierId);
        List<Document> docs = documents.findByDossierIdAndActifTrue(dossierId);
        Map<String, Long> parStatut = new HashMap<>();
        long approuves = 0;
        long signes = 0;
        for (Document doc : docs) {
            Enums.StatutDocument s = doc.getStatut();
            parStatut.merge(s.name(), 1L, Long::sum);
            if (s == Enums.StatutDocument.APPROUVE) approuves++;
            if (s == Enums.StatutDocument.SIGNE) signes++;
        }
        return new DossierRapport(d.getId(), d.getTitre(), d.getStatut(),
                docs.size(), approuves, signes, parStatut);
    }
}
