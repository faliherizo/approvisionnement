package qc.levis.contrat.commun.module.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * État d'activation d'un module pour un tenant donné (feature flag). L'absence de ligne signifie
 * « valeur par défaut du catalogue ». Géré par le superadmin.
 */
@Entity
@Table(name = "tenant_module",
        uniqueConstraints = @UniqueConstraint(name = "uq_tenant_module", columnNames = {"tenant_id", "module_code"}))
public class TenantModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "module_code", nullable = false, length = 50)
    private String moduleCode;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "active_le")
    private Instant activeLe = Instant.now();

    @Column(name = "active_par", length = 150)
    private String activePar;

    public TenantModule() {}

    public TenantModule(Long tenantId, String moduleCode, boolean actif, String activePar) {
        this.tenantId = tenantId;
        this.moduleCode = moduleCode;
        this.actif = actif;
        this.activePar = activePar;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Instant getActiveLe() { return activeLe; }
    public void setActiveLe(Instant activeLe) { this.activeLe = activeLe; }
    public String getActivePar() { return activePar; }
    public void setActivePar(String activePar) { this.activePar = activePar; }
}
