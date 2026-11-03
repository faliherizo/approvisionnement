package qc.levis.contrat.document.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.document.application.dto.CommentaireRequest;
import qc.levis.contrat.document.application.dto.CommentaireVue;
import qc.levis.contrat.document.application.dto.DocumentVue;
import qc.levis.contrat.document.application.repository.CommentaireDemandeRepository;
import qc.levis.contrat.document.application.repository.DocumentDemandeRepository;
import qc.levis.contrat.document.domain.CommentaireDemande;
import qc.levis.contrat.document.domain.DocumentDemande;

/**
 * Pièces jointes et commentaires d'une demande. Tenant-scopé. Le binaire n'est jamais renvoyé dans une
 * liste ; seul le téléchargement unitaire l'expose, après contrôle d'accès (architecture §5.3).
 */
@Service("documentDemandeService")
public class DocumentService {

    /** Taille maximale par pièce jointe (10 Mo) — borne de sécurité côté serveur. */
    private static final long TAILLE_MAX = 10L * 1024 * 1024;

    private final DocumentDemandeRepository documents;
    private final CommentaireDemandeRepository commentaires;

    public DocumentService(DocumentDemandeRepository documents, CommentaireDemandeRepository commentaires) {
        this.documents = documents;
        this.commentaires = commentaires;
    }

    @Transactional
    public DocumentVue ajouter(Long formulaireId, Long saisieId, String nom, String mediaType,
                               byte[] contenu, String classification, String par) {
        if (contenu == null || contenu.length == 0)
            throw new IllegalArgumentException("Fichier vide.");
        if (contenu.length > TAILLE_MAX)
            throw new IllegalArgumentException("Fichier trop volumineux (max 10 Mo).");
        DocumentDemande d = documents.save(new DocumentDemande(tenant(), saisieId, formulaireId, nom, mediaType,
                contenu.length, classification != null ? classification : "interne", contenu, par));
        return vue(d);
    }

    public List<DocumentVue> lister(Long saisieId) {
        List<DocumentVue> res = new ArrayList<>();
        for (DocumentDemande d : documents.findByTenantIdAndSaisieIdOrderByUploadeLeDesc(tenant(), saisieId)) res.add(vue(d));
        return res;
    }

    /** Document complet (binaire inclus) pour téléchargement contrôlé. */
    public DocumentDemande telecharger(Long id) {
        return documents.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Document introuvable : " + id));
    }

    @Transactional
    public CommentaireVue ajouterCommentaire(Long saisieId, CommentaireRequest req, String auteur) {
        if (req.corps() == null || req.corps().isBlank())
            throw new IllegalArgumentException("Commentaire vide.");
        String visibilite = "DEMANDEUR".equalsIgnoreCase(req.visibilite()) ? "DEMANDEUR" : "INTERNE";
        CommentaireDemande c = commentaires.save(new CommentaireDemande(tenant(), saisieId, visibilite, req.corps(), auteur));
        return vueCommentaire(c);
    }

    public List<CommentaireVue> listerCommentaires(Long saisieId) {
        List<CommentaireVue> res = new ArrayList<>();
        for (CommentaireDemande c : commentaires.findByTenantIdAndSaisieIdOrderByCreeLeAsc(tenant(), saisieId)) res.add(vueCommentaire(c));
        return res;
    }

    private DocumentVue vue(DocumentDemande d) {
        return new DocumentVue(d.getId(), d.getSaisieId(), d.getNomOriginal(), d.getMediaType(),
                d.getTailleOctets(), d.getClassification(), d.getUploadePar(), d.getUploadeLe());
    }

    private CommentaireVue vueCommentaire(CommentaireDemande c) {
        return new CommentaireVue(c.getId(), c.getSaisieId(), c.getVisibilite(), c.getCorps(), c.getAuteur(), c.getCreeLe());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
