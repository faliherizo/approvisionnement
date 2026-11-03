package qc.levis.contrat.document.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Commentaire sur une demande (fil d'échange / demande de complément). {@code visibilite} :
 * INTERNE (agents seulement) ou DEMANDEUR (visible du demandeur). Append-only.
 */
@Entity
@Table(name = "commentaire_demande", indexes = @Index(name = "ix_comdem_saisie", columnList = "tenant_id,saisie_id"))
public class CommentaireDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "saisie_id", nullable = false)
    private Long saisieId;

    @Column(nullable = false, length = 20)
    private String visibilite = "INTERNE";

    @Column(nullable = false, length = 4000)
    private String corps;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public CommentaireDemande() {}

    public CommentaireDemande(Long tenantId, Long saisieId, String visibilite, String corps, String auteur) {
        this.tenantId = tenantId;
        this.saisieId = saisieId;
        this.visibilite = visibilite;
        this.corps = corps;
        this.auteur = auteur;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getSaisieId() { return saisieId; }
    public String getVisibilite() { return visibilite; }
    public String getCorps() { return corps; }
    public String getAuteur() { return auteur; }
    public Instant getCreeLe() { return creeLe; }
}
