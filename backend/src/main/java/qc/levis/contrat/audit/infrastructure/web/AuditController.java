package qc.levis.contrat.audit.infrastructure.web;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.audit.application.AuditQueryService;
import qc.levis.contrat.audit.domain.AuditLog;

/** Consultation et export du journal d'audit (art. 18). Réservé à l'administration. */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditQueryService service;

    public AuditController(AuditQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditLog> rechercher(
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant depuis,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant jusqua) {
        return service.rechercher(utilisateur, action, entite, depuis, jusqua);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exporter(
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant depuis,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant jusqua) {
        byte[] corps = service.exporterCsv(utilisateur, action, entite, depuis, jusqua)
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"journal-audit.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(corps);
    }
}
