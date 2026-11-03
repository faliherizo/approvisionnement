package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;

/**
 * Workflow d'un formulaire (un par formulaire et par tenant). La définition (étapes, conditions de routage)
 * vit en JSON ({@code definition_json}) — aucune colonne figée, cohérent avec le moteur de formulaires.
 */
@Entity
@Table(name = "workflow",
        uniqueConstraints = @UniqueConstraint(name = "uq_workflow_tenant_form", columnNames = {"tenant_id", "formulaire_id"}))
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @Column(name = "definition_json", length = 1_000_000)
    private String definitionJson;

    @Column(nullable = false)
    private boolean actif = true;

    public Workflow() {}

    public Workflow(Long tenantId, Long formulaireId, String definitionJson) {
        this.tenantId = tenantId;
        this.formulaireId = formulaireId;
        this.definitionJson = definitionJson;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getFormulaireId() { return formulaireId; }
    public void setFormulaireId(Long formulaireId) { this.formulaireId = formulaireId; }
    public String getDefinitionJson() { return definitionJson; }
    public void setDefinitionJson(String definitionJson) { this.definitionJson = definitionJson; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
