package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Étiquettes Microsoft Purview que l'administration choisit (Annexe 10, SEC-27 ; ADR-036) : par défaut pour l'organisation
 * ({@code typeContrat} nul), ou pour un type de contrat qui la remplace. Elles sont apposées aux documents déposés et
 * produits, conservées avec eux, puis propagées au dépôt SharePoint.
 */
@Entity
@Table(name = "clm_politique_purview", uniqueConstraints = @UniqueConstraint(name = "uk_clm_purview_type",
        columnNames = {"tenant_id", "type_contrat"}))
public class PolitiquePurview {

    public static final int TYPE_MAX = 80;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "type_contrat", length = TYPE_MAX)
    private String typeContrat;

    @Column(name = "sensibilite_id", length = EtiquettePurview.ID_MAX)
    private String sensibiliteId;

    @Column(name = "sensibilite_nom", length = EtiquettePurview.NOM_MAX)
    private String sensibiliteNom;

    @Column(name = "retention_nom", length = EtiquettePurview.NOM_MAX)
    private String retentionNom;

    @Column(name = "retention_jours")
    private Integer retentionJours;

    @Column(name = "modifie_par", nullable = false, length = 150)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected PolitiquePurview() {
    }

    public PolitiquePurview(Long tenantId, String typeContrat) {
        if (typeContrat != null && (typeContrat.isBlank() || typeContrat.length() > TYPE_MAX)) {
            throw new IllegalArgumentException("Type de contrat invalide (80 caractères au plus).");
        }
        this.tenantId = tenantId;
        this.typeContrat = typeContrat;
    }

    /** Au moins une étiquette ; chacune de son type. */
    public void definir(EtiquettePurview sensibilite, EtiquettePurview retention, String par, Instant le) {
        if (sensibilite == null && retention == null) {
            throw new IllegalArgumentException("Choisir une étiquette de sensibilité, de rétention, ou les deux.");
        }
        if (sensibilite != null && sensibilite.type() != EtiquettePurview.Type.SENSIBILITE
                || retention != null && retention.type() != EtiquettePurview.Type.RETENTION) {
            throw new IllegalArgumentException("Étiquette de sensibilité ou de rétention attendue.");
        }
        sensibiliteId = sensibilite == null ? null : sensibilite.id();
        sensibiliteNom = sensibilite == null ? null : sensibilite.nom();
        retentionNom = retention == null ? null : retention.nom();
        retentionJours = retention == null ? null : retention.retentionJours();
        modifiePar = par;
        modifieLe = le;
    }

    public EtiquettePurview sensibilite() {
        return sensibiliteId == null ? null : EtiquettePurview.sensibilite(sensibiliteId, sensibiliteNom);
    }

    public EtiquettePurview retention() {
        return retentionNom == null ? null : EtiquettePurview.retention(retentionNom, retentionJours);
    }

    public boolean estDefaut() {
        return typeContrat == null;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getTypeContrat() { return typeContrat; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}
