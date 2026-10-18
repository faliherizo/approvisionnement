package qc.levis.contrat.tenant.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Tarification d'abonnement propre à chaque entreprise (tenant), gérée par le superadmin.
 * Un tarif par tenant. Le forfait annuel est « tout inclus » (licences, services, hébergement,
 * support) ; l'indexation s'applique aux années de renouvellement optionnelles.
 */
@Entity
@Table(name = "tarif_tenant")
public class TarifTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true)
    private Long tenantId;

    /** Forfait annuel avant taxes, dans la devise du tenant. */
    @Column(name = "forfait_annuel", precision = 12, scale = 2)
    private BigDecimal forfaitAnnuel = BigDecimal.ZERO;

    /** Devise ISO 4217 (CAD par défaut). */
    private String devise = "CAD";

    /** Taux d'indexation annuel (%) appliqué aux années de renouvellement. */
    @Column(name = "indexation_pct", precision = 5, scale = 2)
    private BigDecimal indexationPct = BigDecimal.ZERO;

    /** Nombre d'utilisateurs inclus dans le forfait (facultatif, illimité si nul). */
    @Column(name = "nb_utilisateurs_inclus")
    private Integer nbUtilisateursInclus;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean actif = true;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public BigDecimal getForfaitAnnuel() { return forfaitAnnuel; }
    public void setForfaitAnnuel(BigDecimal forfaitAnnuel) { this.forfaitAnnuel = forfaitAnnuel; }
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    public BigDecimal getIndexationPct() { return indexationPct; }
    public void setIndexationPct(BigDecimal indexationPct) { this.indexationPct = indexationPct; }
    public Integer getNbUtilisateursInclus() { return nbUtilisateursInclus; }
    public void setNbUtilisateursInclus(Integer nbUtilisateursInclus) { this.nbUtilisateursInclus = nbUtilisateursInclus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
