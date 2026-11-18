package qc.levis.contrat.clm.infrastructure.web;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.UtilisationService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Relevé d'utilisation par enveloppe envoyée (addenda 3, Q39) : administration et audit, pour valider les factures. */
@RestController
@RequestMapping("/api/clm/admin/utilisation")
@PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN','SUPERADMIN','AUDITEUR')")
public class UtilisationController {

    private final UtilisationService utilisation;

    public UtilisationController(UtilisationService utilisation) {
        this.utilisation = utilisation;
    }

    @GetMapping
    public ClmDto.UtilisationVue releve(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return utilisation.releve(du, au);
    }
}
