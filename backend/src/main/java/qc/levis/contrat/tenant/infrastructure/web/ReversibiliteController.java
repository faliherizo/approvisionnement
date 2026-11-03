package qc.levis.contrat.tenant.infrastructure.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.tenant.application.ExportTenantService;

/** Réversibilité : export complet des données du tenant en formats ouverts (art. 20). Admin. Exporte les données du socle. */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/reversibilite")
@PreAuthorize("hasRole('ADMIN')")
public class ReversibiliteController {

    private final ExportTenantService service;

    public ReversibiliteController(ExportTenantService service) {
        this.service = service;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exporter(Authentication auth) {
        byte[] archive = service.exporterZip(auth != null ? auth.getName() : "anonyme");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export-tenant.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(archive);
    }
}
