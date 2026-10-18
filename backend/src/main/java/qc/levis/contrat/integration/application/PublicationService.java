package qc.levis.contrat.integration.application;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.dto.PublicationRequest;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.integration.application.repository.PublicationLogRepository;
import qc.levis.contrat.integration.domain.PublicationLog;
import qc.levis.contrat.redaction.application.DocumentRenderingService;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.infrastructure.DocumentRenderer;

/**
 * Publication d'un document vers une cible externe M365/SAP (art. 9). Réutilise le rendu
 * ({@link DocumentRenderingService}) puis délègue le dépôt à l'adaptateur de la cible via le
 * port {@link PublicationCiblePort} (ADR-002). Trace chaque dépôt dans {@link PublicationLog}.
 */
@Service
public class PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    private final DocumentService documents;
    private final DocumentRenderingService rendu;
    private final List<PublicationCiblePort> adaptateurs;
    private final PublicationLogRepository journal;
    private final AuditService audit;

    /** Répertoire de dépôt par défaut si aucun n'est fourni à la publication (configurable). */
    private final String dossierDefaut;

    public PublicationService(DocumentService documents, DocumentRenderingService rendu,
                              List<PublicationCiblePort> adaptateurs,
                              PublicationLogRepository journal, AuditService audit,
                              @Value("${app.publication.dossier-defaut:Contrats}") String dossierDefaut) {
        this.documents = documents;
        this.rendu = rendu;
        this.adaptateurs = adaptateurs;
        this.journal = journal;
        this.audit = audit;
        this.dossierDefaut = dossierDefaut;
    }

    /** Statuts à partir desquels un document est « validé » et peut donc être publié. */
    private static final Set<Enums.StatutDocument> STATUTS_PUBLIABLES = EnumSet.of(
            Enums.StatutDocument.APPROUVE, Enums.StatutDocument.EN_SIGNATURE,
            Enums.StatutDocument.SIGNE, Enums.StatutDocument.PUBLIE);

    @Transactional
    public PublicationLog publier(Long documentId, PublicationRequest req, String utilisateur) {
        Document doc = documents.get(documentId); // valide l'appartenance au tenant
        if (!STATUTS_PUBLIABLES.contains(doc.getStatut())) {
            log.warn("Publication refusée pour le document {} : statut {} non validé", documentId, doc.getStatut());
            throw new IllegalStateException(
                    "Publication impossible : le document doit d'abord être validé (approuvé).");
        }
        Enums.CiblePublication cible = req.cible();
        PublicationCiblePort adaptateur = adaptateurs.stream()
                .filter(a -> a.supporte(cible))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucun adaptateur pour la cible " + cible));
        log.debug("Publication du document {} vers {} via l'adaptateur {}",
                documentId, cible, adaptateur.getClass().getSimpleName());

        Enums.FormatPublication format = req.format() != null ? req.format() : Enums.FormatPublication.PDF;
        DocumentRenderer.Format formatRendu = format == Enums.FormatPublication.DOCX
                ? DocumentRenderer.Format.DOCX : DocumentRenderer.Format.PDF;
        String extension = format == Enums.FormatPublication.DOCX ? "docx" : "pdf";
        String mediaType = format == Enums.FormatPublication.DOCX
                ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                : "application/pdf";

        byte[] octets = rendu.rendre(documentId, formatRendu, req.gabarit());
        String destination = cheminValide(req.destination());
        var contenu = new PublicationCiblePort.ContenuPublie(documentId, doc.getTitre(),
                "contrat-" + documentId + "." + extension, mediaType, octets, destination);
        PublicationCiblePort.ResultatPublication resultat = adaptateur.deposer(cible, contenu);

        PublicationLog entree = new PublicationLog();
        entree.setDocumentId(documentId);
        entree.setCible(cible);
        entree.setSucces(resultat.succes());
        entree.setUrlCible(resultat.url());
        entree.setMessage(resultat.message());
        entree.setPubliePar(utilisateur);
        entree.setTenantId(TenantContext.get());
        PublicationLog saved = journal.save(entree);
        audit.log(utilisateur, "PUBLIER", "Document", documentId, cible.name() + " -> " + resultat.url());
        log.info("Document {} publié vers {} (dossier '{}', succès={})",
                documentId, cible, destination, resultat.succes());
        return saved;
    }

    /**
     * Répertoire de dépôt normalisé : le chemin demandé, ou le dossier par défaut si vide.
     * Retire les séparateurs superflus et refuse la traversée de répertoire (« .. »), quel que
     * soit l'adaptateur (SharePoint, OneDrive, Teams…), pour éviter tout dépôt hors du dossier visé.
     */
    String cheminValide(String demande) {
        String base = (demande == null || demande.isBlank()) ? dossierDefaut : demande.trim();
        String barres = base.replace('\\', '/');
        int debut = 0;
        int fin = barres.length();
        while (debut < fin && barres.charAt(debut) == '/') debut++;
        while (fin > debut && barres.charAt(fin - 1) == '/') fin--;
        String norme = barres.substring(debut, fin);
        for (String segment : norme.split("/")) {
            if (segment.equals("..")) {
                throw new IllegalArgumentException("Chemin de destination invalide: " + demande);
            }
        }
        return norme.isBlank() ? dossierDefaut : norme;
    }

    /** Historique de publication d'un document (traçabilité). */
    public List<PublicationLog> historique(Long documentId) {
        documents.get(documentId); // vérifie l'appartenance au tenant
        return journal.findByDocumentIdOrderByPublieLeDesc(documentId);
    }
}
