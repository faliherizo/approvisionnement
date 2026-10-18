package qc.levis.contrat.approbation.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Gabarit;

/**
 * Gabarit de circuit d'approbation configurable par type de document (art. 7.5, ADR-005).
 * Les étapes sont une liste ordonnée de rôles requis ; l'exécution reste portée par
 * {@link ApprovalStep}, non dupliqué. Un {@code switch} de rôles en dur est ainsi remplacé
 * par une donnée de configuration (OCP).
 */
@Entity
@Table(name = "approval_workflow")
public class ApprovalWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.TypeContrat type;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "tenant_id")
    private Long tenantId;

    /** Rôles requis, dans l'ordre du circuit (niveau = position + 1). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "approval_workflow_step", joinColumns = @JoinColumn(name = "workflow_id"))
    @OrderColumn(name = "niveau")
    @Enumerated(EnumType.STRING)
    @Column(name = "role_requis", nullable = false)
    private List<Enums.Role> etapes = new ArrayList<>();

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Enums.TypeContrat getType() { return type; }
    public void setType(Enums.TypeContrat type) { this.type = type; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public List<Enums.Role> getEtapes() { return etapes; }
    public void setEtapes(List<Enums.Role> etapes) { this.etapes = etapes; }
}
