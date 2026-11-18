package qc.levis.contrat.clm.infrastructure.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.CertificatDestructionService;
import qc.levis.contrat.clm.application.ConservationService;
import qc.levis.contrat.clm.application.ExportationReferentielService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Calendrier de conservation, gel légal, versement et destruction (Cahier § 1.4.11, § 1.4.12) : administrateur seulement. */
@RestController
@RequestMapping("/api/clm/admin/conservation")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN','SUPERADMIN')")
public class ConservationController {

    private final ConservationService conservation;
    private final ExportationReferentielService exportation;
    private final CertificatDestructionService certificats;
    private final WebClm.Acteurs acteurs;

    public ConservationController(ConservationService conservation, ExportationReferentielService exportation,
                                  CertificatDestructionService certificats, WebClm.Acteurs acteurs) {
        this.conservation = conservation;
        this.exportation = exportation;
        this.certificats = certificats;
        this.acteurs = acteurs;
    }

    @GetMapping("/detruites")
    public List<ClmDto.DestructionVue> detruites() {
        return conservation.detruites();
    }

    /** Certificat de destruction scellé (addenda 3, Q48). */
    @GetMapping("/trousses/{id}/certificat-destruction")
    public ResponseEntity<byte[]> certificatDestruction(@PathVariable Long id) {
        return TrousseController.fichier(certificats.certificat(id), "application/pdf", "certificat-destruction-" + id + ".pdf");
    }

    /** Lot de trousses closes pour le référentiel désigné par IQ (addenda 2, Q13). */
    @GetMapping("/exportation")
    public ResponseEntity<byte[]> exporter(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au, Authentication auth) {
        return TrousseController.fichier(exportation.exporter(acteurs.de(auth), du, au), "application/zip",
                "exportation-clm-" + du + "-" + au + ".zip");
    }

    @GetMapping("/regles")
    public List<ClmDto.RegleConservationVue> regles() {
        return conservation.regles();
    }

    @PutMapping("/regles")
    public ClmDto.RegleConservationVue definir(@RequestBody ClmDto.RegleConservationRequest req, Authentication auth) {
        return conservation.definir(req, auth.getName());
    }

    @DeleteMapping("/regles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        conservation.supprimer(id, auth.getName());
    }

    @GetMapping("/trousses")
    public List<ClmDto.ConservationVue> suivi(@RequestParam(defaultValue = "false") boolean echues) {
        return conservation.suivi(echues);
    }

    @PostMapping("/trousses/{id}/gel")
    public ClmDto.ConservationVue geler(@PathVariable Long id, @RequestBody ClmDto.GelRequest req, Authentication auth) {
        return conservation.geler(id, req.motif(), auth.getName());
    }

    @DeleteMapping("/trousses/{id}/gel")
    public ClmDto.ConservationVue leverGel(@PathVariable Long id, Authentication auth) {
        return conservation.leverGel(id, auth.getName());
    }

    @PostMapping("/trousses/{id}/versee")
    public ClmDto.ConservationVue versee(@PathVariable Long id, Authentication auth) {
        return conservation.marquerVersee(id, auth.getName());
    }

    @PostMapping("/trousses/{id}/detruire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detruire(@PathVariable Long id, Authentication auth) {
        conservation.detruire(id, auth.getName());
    }
}
