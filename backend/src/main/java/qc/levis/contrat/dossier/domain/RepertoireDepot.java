package qc.levis.contrat.dossier.domain;

import jakarta.persistence.*;

/**
 * Répertoire de dépôt personnel d'un utilisateur : dossier de destination réutilisable pour la
 * publication (SharePoint/OneDrive/Teams). Chaque utilisateur gère ses propres répertoires dans son
 * espace ; ils alimentent les suggestions du champ « Répertoire de destination ».
 */
@Entity
@Table(name = "repertoire_depot",
        uniqueConstraints = @UniqueConstraint(name = "uq_repertoire_user_chemin",
                columnNames = {"user_id", "chemin"}))
public class RepertoireDepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 300)
    private String chemin;

    public RepertoireDepot() {}

    public RepertoireDepot(Long userId, String chemin) {
        this.userId = userId;
        this.chemin = chemin;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }
}
