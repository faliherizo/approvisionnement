package qc.levis.contrat.rapport.infrastructure.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.rapport.application.RapportService;
import qc.levis.contrat.rapport.domain.CritereRechercheSauve;

/** Rapports, exports et tableaux de bord. Gardé par le module « rapport ». */
@RestController("rapportFormulaireController")
@RequestMapping("/api/rapport")
@PreAuthorize("@modules.actif('rapport')")
public class RapportController {

    private final RapportService service;

    public RapportController(RapportService service) {
        this.service = service;
    }

    @GetMapping("/criteres")
    public List<CritereRechercheSauve> criteres(Authentication auth) {
        return service.mesCriteres(auth != null ? auth.getName() : null);
    }

    @PostMapping("/criteres")
    public CritereRechercheSauve sauvegarder(@RequestParam(required = false) Long formulaireId,
                                             @RequestParam String nom,
                                             @RequestBody(required = false) String criteresJson,
                                             Authentication auth) {
        return service.sauvegarder(auth != null ? auth.getName() : null, formulaireId, nom, criteresJson);
    }

    @DeleteMapping("/criteres/{id}")
    public void supprimer(@PathVariable Long id) {
        service.supprimerCritere(id);
    }

    @GetMapping("/formulaires/{id}/resume")
    public Map<String, Long> resume(@PathVariable Long id) {
        return service.resume(id);
    }

    @GetMapping("/formulaires/{id}/export.xlsx")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id) {
        byte[] xlsx = service.exporterExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fiches-" + id + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
