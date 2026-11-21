package qc.levis.contrat.saisie.infrastructure.web;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.saisie.application.DocumentFicheService;
import qc.levis.contrat.saisie.application.AccesFiches.Acteur;
import qc.levis.contrat.saisie.application.dto.TransmissionRequest;

/** Document d'une fiche (CHU 5.04) : production PDF/A et transmission par courriel. */
@RestController
@RequestMapping("/api/formulaires/{fid}/saisies/{id}")
@PreAuthorize("@modules.actif('saisie')")
public class DocumentFicheController {

    private final DocumentFicheService service;

    public DocumentFicheController(DocumentFicheService service) {
        this.service = service;
    }

    private static Acteur acteur(Authentication auth) {
        return new Acteur(auth.getName(), auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @GetMapping("/document.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long fid, @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("fiche-" + id + ".pdf").build().toString())
                .body(service.pdf(fid, id, acteur(auth)));
    }

    @PostMapping("/document/transmettre")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','SUPERADMIN')")
    public Map<String, String> transmettre(@PathVariable Long fid, @PathVariable Long id, @Valid @RequestBody TransmissionRequest req,
                                           Authentication auth) {
        return Map.of("statut", service.transmettre(fid, id, req.destinataire(), req.message(), acteur(auth)));
    }
}
