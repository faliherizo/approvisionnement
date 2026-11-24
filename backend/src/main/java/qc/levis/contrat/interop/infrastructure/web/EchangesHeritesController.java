package qc.levis.contrat.interop.infrastructure.web;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.interop.application.EchangesHeritesService;
import qc.levis.contrat.interop.application.dto.EchangeDto;
import qc.levis.contrat.interop.domain.BilanReprise;

/** Administration des échanges hérités et reprise de l'historique (CHU-8.04, ADR-021). Module « interop ». */
@RestController
@RequestMapping("/api/interop/echanges")
@PreAuthorize("@modules.actif('interop') and hasAnyRole('ADMIN','SUPERADMIN')")
public class EchangesHeritesController {

    private final EchangesHeritesService service;

    public EchangesHeritesController(EchangesHeritesService service) {
        this.service = service;
    }

    @GetMapping
    public List<EchangeDto.Vue> lister() {
        return service.lister(TenantContext.get());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EchangeDto.Vue declarer(@RequestBody EchangeDto.Declaration d, Principal p) {
        return service.declarer(TenantContext.get(), d, p.getName());
    }

    @PostMapping("/{id}/desactivation")
    public EchangeDto.Vue desactiver(@PathVariable Long id, Principal p) {
        return service.desactiver(TenantContext.get(), id, p.getName());
    }

    /** Reprise de l'export JSON de la plateforme précédente ; {@code essai=true} (par défaut) n'écrit rien. */
    @PostMapping("/{id}/reprise")
    public BilanReprise reprendre(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean essai,
                                  @RequestBody List<Map<String, Object>> export, Principal p) {
        return service.reprendre(TenantContext.get(), id, export, essai, p.getName());
    }
}
