package qc.levis.contrat.rapport.infrastructure.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.rapport.application.RapportPersonnaliseService;
import qc.levis.contrat.rapport.application.RapportService;
import qc.levis.contrat.rapport.domain.CritereRechercheSauve;

/** Rapports, exports et tableaux de bord. Gardé par le module « rapport ». */
@RestController("rapportFormulaireController")
@RequestMapping("/api/rapport")
@PreAuthorize("@modules.actif('rapport')")
public class RapportController {

    private final RapportService service;

    private final RapportPersonnaliseService personnalise;

    /**
     * Rapport PDF personnalisé (CHU 6.03) : questions et statuts choisis. Il nomme des demandeurs et leurs réponses, il est
     * donc réservé au personnel qui traite les fiches.
     */
    @GetMapping("/formulaires/{id}/rapport.pdf")
    @PreAuthorize("@modules.actif('rapport') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
    public ResponseEntity<byte[]> rapportPdf(@PathVariable Long id, @RequestParam(required = false) String titre,
                                             @RequestParam(required = false) List<String> champs,
                                             @RequestParam(required = false) List<String> statuts) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"rapport-" + id + ".pdf\"")
                .body(personnalise.pdf(id, titre, champs, statuts));
    }

    private final qc.levis.contrat.rapport.application.port.LectureFormulaires formulaires;

    public RapportController(RapportService service, RapportPersonnaliseService personnalise,
                             qc.levis.contrat.rapport.application.port.LectureFormulaires formulaires) {
        this.formulaires = formulaires;
        this.service = service;
        this.personnalise = personnalise;
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
    public void supprimer(@PathVariable Long id, Authentication auth) {
        service.supprimerCritere(id, auth.getName());
    }

    /** Décompte et export des fiches : données des demandeurs, réservées au personnel qui traite. */
    @GetMapping("/formulaires/{id}/resume")
    @PreAuthorize("@modules.actif('rapport') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
    public Map<String, Long> resume(@PathVariable Long id) {
        exigerFormulaire(id);
        return service.resume(id);
    }

    @GetMapping("/formulaires/{id}/export.xlsx")
    @PreAuthorize("@modules.actif('rapport') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id) {
        exigerFormulaire(id);
        byte[] xlsx = service.exporterExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fiches-" + id + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    /** Le résumé et l'export d'un formulaire d'une autre organisation sont introuvables. */
    private void exigerFormulaire(Long id) {
        formulaires.decrire(qc.levis.contrat.commun.infrastructure.TenantContext.get(), id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Formulaire introuvable : " + id));
    }
}
