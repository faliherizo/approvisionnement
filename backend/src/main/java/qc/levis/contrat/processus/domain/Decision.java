package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Décision prise sur une tâche (append-only, architecture §5.3 : jamais d'effacement silencieux).
 * {@code decision} : APPROUVE | REFUSE | RETOUR (retour pour correction). Enregistre l'acteur et la date.
 */
@Entity
@Table(name = "decision", indexes = @Index(name = "ix_decision_tache", columnList = "tache_id"))
public class Decision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tache_id", nullable = false)
    private Long tacheId;

    @Column(name = "saisie_id", nullable = false)
    private Long saisieId;

    @Column(nullable = false, length = 150)
    private String acteur;

    @Column(nullable = false, length = 20)
    private String decision;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "decide_le", nullable = false)
    private Instant decideLe = Instant.now();

    public Decision() {}

    public Decision(Long tenantId, Long tacheId, Long saisieId, String acteur, String decision, String commentaire) {
        this.tenantId = tenantId;
        this.tacheId = tacheId;
        this.saisieId = saisieId;
        this.acteur = acteur;
        this.decision = decision;
        this.commentaire = commentaire;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTacheId() { return tacheId; }
    public Long getSaisieId() { return saisieId; }
    public String getActeur() { return acteur; }
    public String getDecision() { return decision; }
    public String getCommentaire() { return commentaire; }
    public Instant getDecideLe() { return decideLe; }
}
