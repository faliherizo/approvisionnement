package qc.levis.contrat.soumission.application;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.soumission.application.repository.SignataireRepository;
import qc.levis.contrat.soumission.domain.Signataire;

/** Signature électronique (écart CLM E-1 : étape d'exécution du cycle). */
@Service
public class SignatureService {

    private final SignataireRepository signataires;
    private final DocumentRepository documents;
    /** Devis bornés à l'organisation courante (OWASP A01). */
    private final qc.levis.contrat.redaction.application.DocumentsDuTenant portee;
    private final AuditService audit;

    public SignatureService(SignataireRepository signataires, DocumentRepository documents,
                            AuditService audit) {
        this.signataires = signataires;
        this.documents = documents;
        this.portee = new qc.levis.contrat.redaction.application.DocumentsDuTenant(documents);
        this.audit = audit;
    }

    public List<Signataire> lister(Long documentId) {
        portee.exiger(documentId);
        return signataires.findByDocumentId(documentId);
    }

    @Transactional
    public Signataire ajouter(Long documentId, String nom, String courriel, String role, String utilisateur) {
        Document doc = portee.exiger(documentId);
        Signataire s = new Signataire();
        s.setDocumentId(doc.getId());
        s.setNom(nom);
        s.setCourriel(courriel);
        s.setRole(role);
        s.setJeton(UUID.randomUUID().toString());
        Signataire saved = signataires.save(s);
        audit.log(utilisateur, "AJOUT_SIGNATAIRE", "Document", documentId, courriel);
        return saved;
    }

    /** Démarre la cérémonie : le document passe EN_SIGNATURE. */
    @Transactional
    public List<Signataire> demarrer(Long documentId, String utilisateur) {
        Document doc = portee.exiger(documentId);
        List<Signataire> liste = signataires.findByDocumentId(documentId);
        if (liste.isEmpty()) {
            throw new IllegalStateException("Aucun signataire défini pour ce document.");
        }
        doc.setStatut(Enums.StatutDocument.EN_SIGNATURE);
        documents.save(doc);
        audit.log(utilisateur, "DEMARRER_SIGNATURE", "Document", documentId, liste.size() + " signataire(s)");
        return liste;
    }

    public Signataire parJeton(String jeton) {
        return signataires.findByJeton(jeton)
                .orElseThrow(() -> new NoSuchElementException("Lien de signature invalide."));
    }

    /** Signe par jeton. Quand tous ont signé, le document passe SIGNE. */
    @Transactional
    public Signataire signer(String jeton, String nomComplet, String signatureImage) {
        Signataire s = parJeton(jeton);
        if (s.getStatut() == Enums.StatutSignature.SIGNE) {
            return s; // idempotent
        }
        s.setStatut(Enums.StatutSignature.SIGNE);
        s.setNom(nomComplet != null && !nomComplet.isBlank() ? nomComplet : s.getNom());
        s.setSigneLe(Instant.now());
        if (signatureImage != null && !signatureImage.isBlank()) {
            s.setSignatureImage(signatureImage);
        }
        signataires.save(s);

        List<Signataire> tous = signataires.findByDocumentId(s.getDocumentId());
        boolean toutSigne = tous.stream().allMatch(x -> x.getStatut() == Enums.StatutSignature.SIGNE);
        if (toutSigne) {
            Document doc = documents.findById(s.getDocumentId()).orElseThrow();
            doc.setStatut(Enums.StatutDocument.SIGNE);
            documents.save(doc);
        }
        audit.log(s.getCourriel(), "SIGNER", "Signataire", s.getId(), null);
        return s;
    }
}
