package qc.levis.contrat.rapport.domain;

import jakarta.persistence.*;

/** Critère de recherche sauvegardé par un utilisateur (réutilisable). Tenant-scopé. */
@Entity
@Table(name = "critere_recherche_sauve")
public class CritereRechercheSauve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 150)
    private String utilisateur;

    @Column(name = "formulaire_id")
    private Long formulaireId;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(name = "criteres_json", length = 4000)
    private String criteresJson;

    public CritereRechercheSauve() {}

    public CritereRechercheSauve(Long tenantId, String utilisateur, Long formulaireId, String nom, String criteresJson) {
        this.tenantId = tenantId;
        this.utilisateur = utilisateur;
        this.formulaireId = formulaireId;
        this.nom = nom;
        this.criteresJson = criteresJson;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
    public Long getFormulaireId() { return formulaireId; }
    public void setFormulaireId(Long formulaireId) { this.formulaireId = formulaireId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCriteresJson() { return criteresJson; }
    public void setCriteresJson(String criteresJson) { this.criteresJson = criteresJson; }
}
