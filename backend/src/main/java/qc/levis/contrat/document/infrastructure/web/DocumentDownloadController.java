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

    /** Types servis tels quels ; tout autre contenu (HTML, script, SVG…) part en flux binaire, jamais interprété. */
    static final java.util.Set<String> TYPES_SURS = java.util.Set.of("application/pdf", "image/png", "image/jpeg", "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final DocumentService service;
    private final qc.levis.contrat.saisie.application.AccesFiches acces;

    public DocumentDownloadController(DocumentService service, qc.levis.contrat.saisie.application.AccesFiches acces) {
        this.service = service;
        this.acces = acces;
    }

    /** La pièce jointe n'est servie qu'au demandeur de la fiche et au personnel, toujours en pièce jointe. */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        DocumentDemande d = service.telecharger(id);
        acces.exigerFiche(d.getFormulaireId(), d.getSaisieId(), new qc.levis.contrat.saisie.application.AccesFiches.Acteur(auth.getName(),
                auth.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).toList()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, org.springframework.http.ContentDisposition.attachment()
                        .filename(d.getNomOriginal() == null ? "piece-jointe" : d.getNomOriginal(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .contentType(typeServi(d.getMediaType()))
                .body(d.getContenu());
    }

    static MediaType typeServi(String declare) {
        String t = declare == null ? "" : declare.toLowerCase(java.util.Locale.ROOT).split(";")[0].strip();
        return TYPES_SURS.contains(t) ? MediaType.parseMediaType(t) : MediaType.APPLICATION_OCTET_STREAM;
    }
}
