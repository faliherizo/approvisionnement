package qc.levis.contrat.soumission.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.soumission.application.SignatureService;
import qc.levis.contrat.soumission.application.dto.SignataireRequest;
import qc.levis.contrat.soumission.application.dto.SignerRequest;
import qc.levis.contrat.soumission.domain.Signataire;

/** Signature électronique. Les endpoints /api/signature/{jeton} sont publics
 *  (le cocontractant externe signe via un lien, sans compte). */
@RestController
@RequestMapping("/api")
public class SignatureController {

    private final SignatureService service;

    public SignatureController(SignatureService service) {
        this.service = service;
    }

    private String utilisateur(Authentication auth) {
        return auth != null ? auth.getName() : "anonyme";
    }

    @GetMapping("/documents/{documentId}/signataires")
    public List<Signataire> lister(@PathVariable Long documentId) {
        return service.lister(documentId);
    }

    @PostMapping("/documents/{documentId}/signataires")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public Signataire ajouter(@PathVariable Long documentId,
                              @Valid @RequestBody SignataireRequest req,
                              Authentication auth) {
        return service.ajouter(documentId, req.nom(), req.courriel(), req.role(), utilisateur(auth));
    }

    @PostMapping("/documents/{documentId}/signature/demarrer")
    @PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
    public List<Signataire> demarrer(@PathVariable Long documentId, Authentication auth) {
        return service.demarrer(documentId, utilisateur(auth));
    }

    // --- Endpoints publics pour le signataire externe (par jeton) ---

    @GetMapping("/signature/{jeton}")
    public Map<String, Object> consulter(@PathVariable String jeton) {
        Signataire s = service.parJeton(jeton);
        return Map.of(
                "nom", s.getNom(),
                "role", s.getRole() != null ? s.getRole() : "",
                "statut", s.getStatut().name(),
                "documentId", s.getDocumentId());
    }

    @PostMapping("/signature/{jeton}/signer")
    public Signataire signer(@PathVariable String jeton, @Valid @RequestBody SignerRequest req) {
        return service.signer(jeton, req.nomComplet(), req.signatureImage());
    }
}
