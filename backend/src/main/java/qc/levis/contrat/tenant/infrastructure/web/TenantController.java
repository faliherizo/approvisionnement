package qc.levis.contrat.tenant.infrastructure.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.redaction.application.ModeleDocumentService;
import qc.levis.contrat.redaction.application.dto.ModeleDocumentRequest;
import qc.levis.contrat.redaction.domain.ModeleDocument;
import qc.levis.contrat.tenant.application.TarifTenantService;
import qc.levis.contrat.tenant.application.TenantAdminService;
import qc.levis.contrat.tenant.application.dto.CreateTenantAdminRequest;
import qc.levis.contrat.tenant.application.dto.TarifTenantRequest;
import qc.levis.contrat.tenant.application.dto.TenantRequest;
import qc.levis.contrat.tenant.application.dto.TenantUpdateRequest;
import qc.levis.contrat.tenant.application.dto.ThemeRequest;
import qc.levis.contrat.tenant.domain.TarifTenant;
import qc.levis.contrat.tenant.domain.Tenant;

/** Espace superadmin : gestion des tenants et de leurs administrateurs. */
@RestController
@RequestMapping("/api/superadmin/tenants")
@PreAuthorize("hasRole('SUPERADMIN')")
public class TenantController {

    private final TenantAdminService service;
    private final ModeleDocumentService modeles;
    private final TarifTenantService tarifs;

    public TenantController(TenantAdminService service, ModeleDocumentService modeles,
                            TarifTenantService tarifs) {
        this.service = service;
        this.modeles = modeles;
        this.tarifs = tarifs;
    }

    @GetMapping
    public List<Tenant> lister() { return service.lister(); }

    @PostMapping
    public Tenant creer(@Valid @RequestBody TenantRequest req) {
        return service.creer(req.slug(), req.titre(), req.sousTitre());
    }

    @PutMapping("/{id}")
    public Tenant mettreAJour(@PathVariable Long id,
                              @RequestBody TenantUpdateRequest req) {
        return service.mettreAJour(id, req.titre(), req.sousTitre(), req.actif());
    }

    @PostMapping("/{id}/admin")
    public User creerAdmin(@PathVariable Long id,
                           @Valid @RequestBody CreateTenantAdminRequest req) {
        return service.creerAdminTenant(id, req.email(), req.nom(), req.motDePasse());
    }

    @PutMapping("/{id}/theme")
    public Tenant theme(@PathVariable Long id, @RequestBody ThemeRequest req) {
        return service.mettreAJourTheme(id, req);
    }

    /** Modèle de document (gabarit PDF/DOCX) d'un tenant précis. */
    @GetMapping("/{id}/modele-document")
    public ModeleDocument modeleDocument(@PathVariable Long id) {
        return modeles.pour(id);
    }

    @PutMapping("/{id}/modele-document")
    public ModeleDocument enregistrerModele(@PathVariable Long id,
                                            @RequestBody ModeleDocumentRequest req) {
        return modeles.enregistrer(id, req);
    }

    /** Tarif d'abonnement d'un tenant précis. */
    @GetMapping("/{id}/tarif")
    public TarifTenant tarif(@PathVariable Long id) {
        return tarifs.pour(id);
    }

    @PutMapping("/{id}/tarif")
    public TarifTenant enregistrerTarif(@PathVariable Long id,
                                        @RequestBody TarifTenantRequest req) {
        return tarifs.enregistrer(id, req);
    }
}
