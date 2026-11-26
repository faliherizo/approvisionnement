package qc.levis.contrat.clm.infrastructure.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.clm.application.AccesSoutienService;
import qc.levis.contrat.clm.application.dto.ClmDto;

/** Accès de soutien de l'exploitant aux données d'une organisation (Annexe 10, SEC-17 ; ADR-031). */
public final class SoutienClmController {

    private SoutienClmController() {
    }

    /** Côté exploitant : demander un accès motivé, suivre ses demandes, consulter un diagnostic pendant un accès accordé. */
    @RestController
    @RequestMapping("/api/superadmin/tenants/{tenantId}/soutien-clm")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public static class ConsoleSoutienClm {

        private final AccesSoutienService soutien;

        public ConsoleSoutienClm(AccesSoutienService soutien) {
            this.soutien = soutien;
        }

        @PostMapping("/demandes")
        @ResponseStatus(HttpStatus.CREATED)
        public ClmDto.AccesSoutienVue demander(@PathVariable Long tenantId, @RequestBody ClmDto.DemandeSoutien req, Authentication auth) {
            return soutien.demander(tenantId, req, auth.getName());
        }

        @GetMapping("/demandes")
        public List<ClmDto.AccesSoutienVue> demandes(@PathVariable Long tenantId, Authentication auth) {
            return soutien.mesAcces(tenantId, auth.getName());
        }

        @GetMapping("/trousses/{reference}")
        public ClmDto.DiagnosticSoutien diagnostic(@PathVariable Long tenantId, @PathVariable String reference, Authentication auth) {
            return soutien.diagnostic(tenantId, reference, auth.getName());
        }
    }

    /** Côté organisation : l'administration d'affaires voit, accorde, refuse et révoque les accès de soutien. */
    @RestController
    @RequestMapping("/api/clm/admin/soutien")
    @PreAuthorize("@modules.actif('clm') and hasRole('ADMIN_AFFAIRES')")
    public static class Organisation {

        private final AccesSoutienService soutien;

        public Organisation(AccesSoutienService soutien) {
            this.soutien = soutien;
        }

        @GetMapping
        public List<ClmDto.AccesSoutienVue> lister() {
            return soutien.lister();
        }

        @PostMapping("/{id}/accorder")
        public ClmDto.AccesSoutienVue accorder(@PathVariable Long id, Authentication auth) {
            return soutien.accorder(id, auth.getName());
        }

        @PostMapping("/{id}/refuser")
        public ClmDto.AccesSoutienVue refuser(@PathVariable Long id, Authentication auth) {
            return soutien.refuser(id, auth.getName());
        }

        @PostMapping("/{id}/revoquer")
        public ClmDto.AccesSoutienVue revoquer(@PathVariable Long id, Authentication auth) {
            return soutien.revoquer(id, auth.getName());
        }
    }
}
