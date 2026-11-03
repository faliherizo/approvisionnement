package qc.levis.contrat.clm.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Décision d'un intervenant sur une étape : historique unifié des validations, approbations et signatures (ST-08). */
@Entity
@Table(name = "clm_decision", indexes = @Index(name = "ix_clm_dec_etape", columnList = "tenant_id,etape_id"))
public class DecisionEtape {

    public enum Nature { APPROUVE, REJETE, RETOUR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "etape_id", nullable = false)
    private Long etapeId;

    @Column(nullable = false, length = 150)
    private String acteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nature nature;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "decide_le", nullable = false)
    private Instant decideLe;

    protected DecisionEtape() {
    }

    public DecisionEtape(Long tenantId, Long trousseId, Long etapeId, String acteur, Nature nature, String commentaire, Instant decideLe) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.etapeId = etapeId;
        this.acteur = acteur;
        this.nature = nature;
        this.commentaire = commentaire;
        this.decideLe = decideLe;
    }

    public Long getEtapeId() { return etapeId; }
    public String getActeur() { return acteur; }
    public Nature getNature() { return nature; }
    public String getCommentaire() { return commentaire; }
    public Instant getDecideLe() { return decideLe; }
}
