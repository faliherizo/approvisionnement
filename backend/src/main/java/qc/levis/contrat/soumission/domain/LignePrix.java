package qc.levis.contrat.soumission.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/** Ligne d'un bordereau de prix (description, quantité, taux → total calculé). */
@Entity
@Table(name = "ligne_prix")
public class LignePrix {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "bordereau_id")
    private Bordereau bordereau;

    @Column(nullable = false)
    private String description;

    private BigDecimal quantite;
    private BigDecimal taux;
    private int ordre = 0;

    public Long getId() { return id; }
    public Bordereau getBordereau() { return bordereau; }
    public void setBordereau(Bordereau bordereau) { this.bordereau = bordereau; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getQuantite() { return quantite; }
    public void setQuantite(BigDecimal quantite) { this.quantite = quantite; }
    public BigDecimal getTaux() { return taux; }
    public void setTaux(BigDecimal taux) { this.taux = taux; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    /** Total = quantité × taux (0 si l'un est absent). */
    public BigDecimal getTotal() {
        if (quantite == null || taux == null) return BigDecimal.ZERO;
        return quantite.multiply(taux);
    }
}
