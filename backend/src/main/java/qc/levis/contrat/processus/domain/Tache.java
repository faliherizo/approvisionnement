package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tâche de workflow générée pour une demande (saisie) à une étape donnée. Relationnelle (audit, requêtes,
 * contraintes §5.3). Affectée à un rôle et/ou un groupe ; l'ordre reflète la séquence des étapes applicables.
 */
@Entity
@Table(name = "tache",
        indexes = {
                @Index(name = "ix_tache_assignation", columnList = "tenant_id,assigne_role,statut"),
                @Index(name = "ix_tache_saisie", columnList = "saisie_id,statut")
        })
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "saisie_id", nullable = false)
    private Long saisieId;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @Column(name = "etape_code", nullable = false, length = 80)
    private String etapeCode;

    @Column(name = "etape_nom", length = 200)
    private String etapeNom;

    @Column(name = "assigne_role", length = 60)
    private String assigneRole;

    @Column(name = "assigne_groupe", length = 80)
    private String assigneGroupe;

    @Column(name = "commentaire_refus_obligatoire", nullable = false)
    private boolean commentaireRefusObligatoire = false;

    @Column(nullable = false)
    private int ordre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTache statut = StatutTache.OUVERTE;

    @Column(name = "echeance_le")
    private Instant echeanceLe;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    @Column(name = "termine_le")
    private Instant termineLe;

    public Tache() {}

    public Tache(Long tenantId, Long saisieId, Long formulaireId, String etapeCode, String etapeNom,
                 String assigneRole, String assigneGroupe, boolean commentaireRefusObligatoire,
                 int ordre, Instant echeanceLe) {
        this.tenantId = tenantId;
        this.saisieId = saisieId;
        this.formulaireId = formulaireId;
        this.etapeCode = etapeCode;
        this.etapeNom = etapeNom;
        this.assigneRole = assigneRole;
        this.assigneGroupe = assigneGroupe;
        this.commentaireRefusObligatoire = commentaireRefusObligatoire;
        this.ordre = ordre;
        this.echeanceLe = echeanceLe;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getSaisieId() { return saisieId; }
    public Long getFormulaireId() { return formulaireId; }
    public String getEtapeCode() { return etapeCode; }
    public String getEtapeNom() { return etapeNom; }
    public String getAssigneRole() { return assigneRole; }
    public String getAssigneGroupe() { return assigneGroupe; }
    public boolean isCommentaireRefusObligatoire() { return commentaireRefusObligatoire; }
    public int getOrdre() { return ordre; }
    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }
    public Instant getEcheanceLe() { return echeanceLe; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getTermineLe() { return termineLe; }
    public void setTermineLe(Instant termineLe) { this.termineLe = termineLe; }
}
