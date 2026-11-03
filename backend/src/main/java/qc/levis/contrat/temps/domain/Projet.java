package qc.levis.contrat.temps.domain;

import jakarta.persistence.*;

/**
 * Projet auquel des tâches et des saisies de temps se rattachent (porté du modèle `paie.SaisiesDeTemps.Projet`
 * de Nexu VINA : code, nom, gestionnaire, budget d'heures).
 */
@Entity
@Table(name = "projet_temps",
        uniqueConstraints = @UniqueConstraint(name = "uq_projet_tenant_code", columnNames = {"tenant_id", "code"}))
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(name = "gestionnaire", length = 150)
    private String gestionnaire;

    @Column(name = "budget_heures")
    private Double budgetHeures;

    @Column(nullable = false)
    private boolean actif = true;

    public Projet() {}

    public Projet(Long tenantId, String code, String nom, String gestionnaire, Double budgetHeures) {
        this.tenantId = tenantId;
        this.code = code;
        this.nom = nom;
        this.gestionnaire = gestionnaire;
        this.budgetHeures = budgetHeures;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getGestionnaire() { return gestionnaire; }
    public void setGestionnaire(String gestionnaire) { this.gestionnaire = gestionnaire; }
    public Double getBudgetHeures() { return budgetHeures; }
    public void setBudgetHeures(Double budgetHeures) { this.budgetHeures = budgetHeures; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
