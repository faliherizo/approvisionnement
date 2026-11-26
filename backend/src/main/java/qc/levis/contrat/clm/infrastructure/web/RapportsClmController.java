package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.RapportsClmService;
import qc.levis.contrat.clm.application.TableauExport;
import qc.levis.contrat.clm.application.dto.ClmDto;

/**
 * Rapports personnalisables (Annexe 10, AF-37) : l'administration d'affaires les crée et les modifie ; avec l'auditeur, elle
 * les consulte et les exporte en CSV ou en Excel.
 */
@RestController
@RequestMapping("/api/clm/admin/rapports")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES','AUDITEUR')")
public class RapportsClmController {

    private static final String GESTION = "@modules.actif('clm') and hasRole('ADMIN_AFFAIRES')";

    private final RapportsClmService rapports;

    public RapportsClmController(RapportsClmService rapports) {
        this.rapports = rapports;
    }

    @GetMapping
    public List<ClmDto.RapportVue> lister() {
        return rapports.lister();
    }

    @GetMapping("/types")
    public List<ClmDto.TypeRapportVue> types() {
        return rapports.types();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(GESTION)
    public ClmDto.RapportVue creer(@RequestBody ClmDto.RapportRequest req, Authentication auth) {
        return rapports.creer(req, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize(GESTION)
    public ClmDto.RapportVue modifier(@PathVariable Long id, @RequestBody ClmDto.RapportRequest req, Authentication auth) {
        return rapports.modifier(id, req, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(GESTION)
    public void supprimer(@PathVariable Long id, Authentication auth) {
        rapports.supprimer(id, auth.getName());
    }

    @GetMapping("/{id}/resultat")
    public TableauExport resultat(@PathVariable Long id) {
        return rapports.executer(id);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id, @RequestParam(defaultValue = "csv") String format, Authentication auth) {
        return fichier(rapports.exporter(id, format, auth.getName()), format, "rapport-clm-" + id);
    }

    static ResponseEntity<byte[]> fichier(byte[] contenu, String format, String nom) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        return TrousseController.fichier(contenu, xlsx ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "text/csv;charset=UTF-8",
                nom + (xlsx ? ".xlsx" : ".csv"));
    }
}
