package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Étape d'approbation d'une demande (un niveau), générée par le moteur {@link Approbations}. */
@Entity
@Table(name = "etape_approbation", indexes = @Index(name = "ix_etappro", columnList = "tenant_id,approbateur_role,statut"))
public class EtapeApprobation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "demande_id", nullable = false)
    private Long demandeId;

    @Column(nullable = false)
    private int niveau;

    @Column(name = "approbateur_role", length = 60)
    private String approbateurRole;

    @Column(name = "approbateur_id", length = 150)
    private String approbateurId;

    @Column(name = "rule_id", length = 80)
    private String ruleId;

    @Column(nullable = false, length = 20)
    private String statut = "PENDING";

    @Column(name = "decide_par", length = 150)
    private String decidePar;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "decide_le")
    private Instant decideLe;

    public EtapeApprobation() {}

    public EtapeApprobation(Long tenantId, Long demandeId, int niveau, String approbateurRole, String approbateurId, String ruleId) {
        this.tenantId = tenantId;
        this.demandeId = demandeId;
        this.niveau = niveau;
        this.approbateurRole = approbateurRole;
        this.approbateurId = approbateurId;
        this.ruleId = ruleId;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getDemandeId() { return demandeId; }
    public int getNiveau() { return niveau; }
    public String getApprobateurRole() { return approbateurRole; }
    public String getApprobateurId() { return approbateurId; }
    public String getRuleId() { return ruleId; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getDecidePar() { return decidePar; }
    public void setDecidePar(String decidePar) { this.decidePar = decidePar; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Instant getDecideLe() { return decideLe; }
    public void setDecideLe(Instant decideLe) { this.decideLe = decideLe; }
}
