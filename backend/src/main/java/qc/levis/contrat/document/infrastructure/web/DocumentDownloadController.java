package qc.levis.contrat.document.infrastructure.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.document.application.DocumentService;
import qc.levis.contrat.document.domain.DocumentDemande;

/**
 * Téléchargement d'une pièce jointe après contrôle d'accès (tenant-scopé dans le service). Gardé par le
 * module « document ». Le binaire est servi ici uniquement, jamais dans les listes.
 */
@RestController
@RequestMapping("/api/documents")
@PreAuthorize("@modules.actif('document')")
public class DocumentDownloadController {

    private final DocumentService service;

    public DocumentDownloadController(DocumentService service) {
        this.service = service;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        DocumentDemande d = service.telecharger(id);
        MediaType type = d.getMediaType() != null ? MediaType.parseMediaType(d.getMediaType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + d.getNomOriginal() + "\"")
                .contentType(type)
                .body(d.getContenu());
    }
}
