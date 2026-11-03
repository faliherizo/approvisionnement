package qc.levis.contrat.clm.infrastructure.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.ParcoursService;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.SuiviService;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Boîte de tâches, décisions, réassignation (UI-12 à UI-14) et pilotage (UI-01, UI-18). */
@RestController
@RequestMapping("/api/clm")
@PreAuthorize("@modules.actif('clm')")
public class ParcoursController {

    private final ParcoursService parcours;
    private final SuiviService suivi;
    private final SalesforceService salesforce;
    private final WebClm.Acteurs acteurs;

    public ParcoursController(ParcoursService parcours, SuiviService suivi, SalesforceService salesforce, WebClm.Acteurs acteurs) {
        this.parcours = parcours;
        this.suivi = suivi;
        this.salesforce = salesforce;
        this.acteurs = acteurs;
    }

    @GetMapping("/salesforce/dossiers/{identifiant}")
    public PortSalesforce.Dossier dossierSalesforce(@PathVariable String identifiant) {
        return salesforce.lireDossier(identifiant);
    }

    @GetMapping("/taches")
    public List<ClmDto.TacheVue> taches(@RequestParam(defaultValue = "a_traiter") String filtre, Authentication auth) {
        return parcours.mesTaches(acteurs.de(auth), filtre);
    }

    @PostMapping("/etapes/{id}/decider")
    public ClmDto.EtapeVue decider(@PathVariable Long id, @RequestBody ClmDto.DecisionRequest req, Authentication auth) {
        return parcours.decider(id, acteurs.de(auth), req);
    }

    @PutMapping("/etapes/{id}/affectations")
    public ClmDto.EtapeVue affectations(@PathVariable Long id, @RequestBody ClmDto.AffectationsRequest req, Authentication auth) {
        return parcours.modifierAffectations(id, acteurs.de(auth), req);
    }

    @PostMapping("/etapes/{id}/lien-signature")
    public Map<String, String> lienSignature(@PathVariable Long id, Authentication auth) {
        return Map.of("lien", parcours.lienSignature(id, acteurs.de(auth)));
    }

    @PostMapping("/etapes/{id}/relancer")
    public ClmDto.EtapeVue relancer(@PathVariable Long id, Authentication auth) {
        return parcours.relancerEtape(id, acteurs.de(auth));
    }

    @PostMapping("/etapes/{id}/commentaires")
    public ResponseEntity<Void> commenter(@PathVariable Long id, @RequestBody ClmDto.CommentaireRequest req, Authentication auth) {
        parcours.commenter(id, acteurs.de(auth), req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/etapes/{id}/deleguer")
    public ClmDto.EtapeVue deleguer(@PathVariable Long id, @RequestBody ClmDto.DelegationRequest req, Authentication auth) {
        return parcours.deleguer(id, acteurs.de(auth), req);
    }

    @GetMapping("/tableau-de-bord")
    public ClmDto.TableauBord tableauBord(@RequestParam(required = false) String statut, @RequestParam(required = false) String unite,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                          Authentication auth) {
        return suivi.tableauBord(acteurs.de(auth), new SuiviService.Filtre(null, statut, unite, null, null, du, au, false));
    }

    @GetMapping("/suivi")
    public List<ClmDto.SuiviLigne> suivi(@RequestParam(required = false) String q, @RequestParam(required = false) String statut,
                                         @RequestParam(required = false) String unite, @RequestParam(required = false) String responsable,
                                         @RequestParam(required = false) String source,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                         @RequestParam(defaultValue = "false") boolean exceptions, Authentication auth) {
        return suivi.suivi(acteurs.de(auth), new SuiviService.Filtre(q, statut, unite, responsable, source, du, au, exceptions));
    }

    @GetMapping("/suivi/export")
    public ResponseEntity<byte[]> exporter(@RequestParam(required = false) String q, @RequestParam(required = false) String statut,
                                           @RequestParam(defaultValue = "false") boolean exceptions, Authentication auth) {
        String csv = suivi.exporterCsv(acteurs.de(auth), new SuiviService.Filtre(q, statut, null, null, null, null, null, exceptions));
        return TrousseController.fichier(("\uFEFF" + csv).getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/csv;charset=UTF-8", "suivi-clm.csv");
    }
}
