package qc.levis.contrat.paie.infrastructure.web;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.paie.application.PaieService;
import qc.levis.contrat.paie.application.dto.ApercuPaieRequest;
import qc.levis.contrat.paie.domain.Declarations;
import qc.levis.contrat.paie.domain.Remises;

/**
 * API de paie (RH) : aperçu des retenues, barèmes et fréquences. S'appuie sur le moteur pur porté de
 * Nexu VINA. Gardé par le module « paie » et par rôle d'administration.
 */
@RestController
@RequestMapping("/api/paie")
@PreAuthorize("@modules.actif('paie') and hasAnyRole('ADMIN','DIRECTION','SUPERADMIN')")
public class PaieController {

    private final PaieService service;

    public PaieController(PaieService service) {
        this.service = service;
    }

    @PostMapping("/apercu")
    public Map<String, Object> apercu(@RequestBody ApercuPaieRequest req) {
        return service.apercu(req);
    }

    @GetMapping("/taux")
    public Map<String, Object> taux() {
        return service.taux();
    }

    @GetMapping("/frequences")
    public List<Map<String, Object>> frequences() {
        return service.frequences();
    }

    @PostMapping("/impot/apercu")
    public Map<String, Object> impot(@RequestBody Map<String, Object> e) {
        return service.impot(e);
    }

    @PostMapping("/declarations/taxes")
    public Declarations.RapportTaxes taxes(@RequestBody Map<String, Object> body) {
        return service.taxes(body);
    }

    @PostMapping("/declarations/retenues")
    public Declarations.RapportRetenues retenues(@RequestBody Map<String, Object> body) {
        return service.retenues(body);
    }

    @PostMapping("/declarations/fin-annee")
    public Declarations.SyntheseAnnuelle finDAnnee(@RequestBody Map<String, Object> body) {
        return service.finDAnnee(body);
    }

    @GetMapping("/remises/echeancier")
    public List<Remises.Echeance> echeancierRemises(@RequestParam String authority,
                                                    @RequestParam(required = false) String frequency,
                                                    @RequestParam int year) {
        return service.echeancierRemises(authority, frequency, year);
    }
}
