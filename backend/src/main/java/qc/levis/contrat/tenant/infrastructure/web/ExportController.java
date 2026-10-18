package qc.levis.contrat.tenant.infrastructure.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.redaction.application.DocumentRenderingService;
import qc.levis.contrat.redaction.infrastructure.DocumentRenderer;

/** Génère les documents contractuels finaux (PDF/DOCX) via le pipeline composer + renderer. */
@RestController
@RequestMapping("/api/documents/{id}/export")
public class ExportController {

    private final DocumentRenderingService rendu;

    public ExportController(DocumentRenderingService rendu) {
        this.rendu = rendu;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id,
                                      @RequestParam(required = false) String gabarit) {
        return fichier(rendu.rendre(id, DocumentRenderer.Format.PDF, gabarit),
                MediaType.APPLICATION_PDF, nomFichier(id, "pdf"), true);
    }

    /** Aperçu PDF affiché en ligne (validation avant téléchargement). */
    @GetMapping("/pdf/apercu")
    public ResponseEntity<byte[]> apercuPdf(@PathVariable Long id,
                                            @RequestParam(required = false) String gabarit) {
        return fichier(rendu.rendre(id, DocumentRenderer.Format.PDF, gabarit),
                MediaType.APPLICATION_PDF, nomFichier(id, "pdf"), false);
    }

    /** Aperçu image (PNG) de la première page : rendu fidèle et visible dans tous les navigateurs. */
    @GetMapping("/pdf/apercu-image")
    public ResponseEntity<byte[]> apercuImage(@PathVariable Long id,
                                              @RequestParam(required = false) String gabarit) {
        return fichier(rendu.apercuPng(id, gabarit), MediaType.IMAGE_PNG, nomFichier(id, "png"), false);
    }

    @GetMapping("/docx")
    public ResponseEntity<byte[]> docx(@PathVariable Long id,
                                       @RequestParam(required = false) String gabarit) {
        MediaType type = MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return fichier(rendu.rendre(id, DocumentRenderer.Format.DOCX, gabarit), type, nomFichier(id, "docx"), true);
    }

    private String nomFichier(Long id, String extension) {
        return "contrat-" + id + "." + extension;
    }

    private ResponseEntity<byte[]> fichier(byte[] contenu, MediaType type, String nom, boolean telechargement) {
        String disposition = (telechargement ? "attachment" : "inline") + "; filename=\"" + nom + "\"";
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(contenu);
    }
}
