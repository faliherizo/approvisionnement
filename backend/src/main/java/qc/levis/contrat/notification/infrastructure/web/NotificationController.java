package qc.levis.contrat.notification.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.notification.application.NotificationService;
import qc.levis.contrat.notification.application.dto.ModeleRequest;
import qc.levis.contrat.notification.application.dto.RegleNotifRequest;
import qc.levis.contrat.notification.domain.NotificationEnvoi;
import qc.levis.contrat.notification.domain.NotificationModele;
import qc.levis.contrat.notification.domain.NotificationRegle;

/** Configuration des notifications (gabarits, règles) et journal. Gardé par le module « notification ». */
@RestController
@RequestMapping("/api/notification")
@PreAuthorize("@modules.actif('notification') and hasAnyRole('ADMIN','SUPERADMIN')")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/modeles")
    public List<NotificationModele> modeles() {
        return service.listerModeles(TenantContext.get());
    }

    @PostMapping("/modeles")
    public NotificationModele creerModele(@Valid @RequestBody ModeleRequest req) {
        return service.enregistrerModele(new NotificationModele(
                TenantContext.get(), req.code(), req.canal(), req.sujet(), req.corps()));
    }

    @GetMapping("/regles")
    public List<NotificationRegle> regles() {
        return service.listerRegles(TenantContext.get());
    }

    @PostMapping("/regles")
    public NotificationRegle creerRegle(@Valid @RequestBody RegleNotifRequest req) {
        return service.enregistrerRegle(new NotificationRegle(
                TenantContext.get(), req.formulaireId(), req.declencheur(), req.modeleCode(), req.destinataires()));
    }

    @GetMapping("/journal")
    public List<NotificationEnvoi> journal() {
        return service.journal(TenantContext.get());
    }
}
