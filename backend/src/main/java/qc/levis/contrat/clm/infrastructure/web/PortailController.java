package qc.levis.contrat.clm.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.PortailService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.DocumentTrousse;

/**
 * Portail public du signataire (UI-16) : authentifié par le seul jeton d'invitation, puis par la vérification
 * d'identité exigée par le risque. Aucun en-tête de rôle ni de tenant n'est lu du navigateur.
 */
@RestController
@RequestMapping("/api/clm/portail/{jeton}")
public class PortailController {

    private final PortailService portail;

    public PortailController(PortailService portail) {
        this.portail = portail;
    }

    @GetMapping
    public ClmDto.PortailVue consulter(@PathVariable String jeton) {
        return portail.consulter(jeton);
    }

    @PostMapping("/verification")
    public ClmDto.PortailVue demarrer(@PathVariable String jeton) {
        return portail.demarrerVerification(jeton);
    }

    @PostMapping("/verification/reponse")
    public ClmDto.PortailVue verifier(@PathVariable String jeton, @RequestBody ClmDto.VerificationRequest req) {
        return portail.verifier(jeton, req);
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<byte[]> document(@PathVariable String jeton, @PathVariable Long documentId) {
        DocumentTrousse d = portail.document(jeton, documentId);
        return TrousseController.fichier(d.getContenu(), d.getType().mime(), d.getNom());
    }

    @PostMapping("/signer")
    public ClmDto.Recu signer(@PathVariable String jeton, @RequestBody ClmDto.SignatureRequest req, HttpServletRequest http) {
        return portail.signer(jeton, req, http.getRemoteAddr(), http.getHeader("User-Agent"));
    }

    @PostMapping("/refuser")
    public ClmDto.PortailVue refuser(@PathVariable String jeton, @RequestBody ClmDto.RefusRequest req, HttpServletRequest http) {
        return portail.refuser(jeton, req, http.getRemoteAddr());
    }
}
