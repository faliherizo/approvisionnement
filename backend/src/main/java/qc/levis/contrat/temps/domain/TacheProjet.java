package qc.levis.contrat.temps.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tâche d'un projet, assignable à une personne (porté du modèle `paie.Projets.Tache` de Nexu VINA :
 * name, description, taskType, status, assigneeId, estimatedHours, dueDate). C'est l'« ajout de tâche /
 * assignation à une personne ». Le temps saisi ({@link SaisieTemps}) s'y rattache.
 */
@Entity
@Table(name = "tache_projet", indexes = {
        @Index(name = "ix_tacheprojet_projet", columnList = "tenant_id,projet_id"),
        @Index(name = "ix_tacheprojet_assigne", columnList = "tenant_id,assigne_id")
})
public class TacheProjet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "projet_id", nullable = false)
    private Long projetId;

    @Column(nullable = false, length = 300)
    private String nom;

    @Column(length = 2000)
    private String description;

    @Column(name = "type_tache", length = 40)
    private String typeTache; // feature | bug | admin | ...

    @Column(nullable = false, length = 20)
    private String statut = "A_FAIRE"; // A_FAIRE | EN_COURS | TERMINEE | ANNULEE

    /** Personne assignée (id d'employé du contexte RH), ou null si non assignée. */
    @Column(name = "assigne_id")
    private Long assigneId;

    @Column(name = "heures_estimees")
    private Double heuresEstimees;

    @Column(name = "echeance", length = 10)
    private String echeance;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public TacheProjet() {}

    public TacheProjet(Long tenantId, Long projetId, String nom, String description, String typeTache,
                 String statut, Long assigneId, Double heuresEstimees, String echeance) {
        this.tenantId = tenantId;
        this.projetId = projetId;
        this.nom = nom;
        this.description = description;
        this.typeTache = typeTache;
        if (statut != null) this.statut = statut;
        this.assigneId = assigneId;
        this.heuresEstimees = heuresEstimees;
        this.echeance = echeance;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTypeTache() { return typeTache; }
    public void setTypeTache(String typeTache) { this.typeTache = typeTache; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Long getAssigneId() { return assigneId; }
    public void setAssigneId(Long assigneId) { this.assigneId = assigneId; }
    public Double getHeuresEstimees() { return heuresEstimees; }
    public void setHeuresEstimees(Double heuresEstimees) { this.heuresEstimees = heuresEstimees; }
    public String getEcheance() { return echeance; }
    public void setEcheance(String echeance) { this.echeance = echeance; }
    public Instant getCreeLe() { return creeLe; }
}
