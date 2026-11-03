package qc.levis.contrat.temps.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Feuille de temps d'une période : lignes (date / catégorie / heures) en JSON — rien de figé — + total.
 * Cycle : BROUILLON → SOUMISE → APPROUVEE | REFUSEE. Inspirée de `paie.FeuillesDeTemps` de Nexu VINA.
 */
@Entity
@Table(name = "feuille_temps", indexes = @Index(name = "ix_feuilletemps", columnList = "tenant_id,proprietaire"))
public class FeuilleTemps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 150)
    private String proprietaire;

    @Column(name = "employe_id")
    private Long employeId;

    @Column(name = "periode_debut", length = 10)
    private String periodeDebut;

    @Column(name = "periode_fin", length = 10)
    private String periodeFin;

    @Column(nullable = false, length = 20)
    private String statut = "BROUILLON";

    @Column(name = "lignes_json", length = 100_000)
    private String lignesJson = "[]";

    @Column(name = "total_heures")
    private double totalHeures;

    @Column(length = 150)
    private String approbateur;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe = Instant.now();

    public FeuilleTemps() {}

    public FeuilleTemps(Long tenantId, String proprietaire, Long employeId, String periodeDebut, String periodeFin) {
        this.tenantId = tenantId;
        this.proprietaire = proprietaire;
        this.employeId = employeId;
        this.periodeDebut = periodeDebut;
        this.periodeFin = periodeFin;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getProprietaire() { return proprietaire; }
    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long employeId) { this.employeId = employeId; }
    public String getPeriodeDebut() { return periodeDebut; }
    public void setPeriodeDebut(String periodeDebut) { this.periodeDebut = periodeDebut; }
    public String getPeriodeFin() { return periodeFin; }
    public void setPeriodeFin(String periodeFin) { this.periodeFin = periodeFin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getLignesJson() { return lignesJson; }
    public void setLignesJson(String lignesJson) { this.lignesJson = lignesJson; }
    public double getTotalHeures() { return totalHeures; }
    public void setTotalHeures(double totalHeures) { this.totalHeures = totalHeures; }
    public String getApprobateur() { return approbateur; }
    public void setApprobateur(String approbateur) { this.approbateur = approbateur; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Instant getMajLe() { return majLe; }
    public void setMajLe(Instant majLe) { this.majLe = majLe; }
}
