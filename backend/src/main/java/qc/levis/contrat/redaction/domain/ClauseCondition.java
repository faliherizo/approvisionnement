package qc.levis.contrat.redaction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Condition d'inclusion d'une clause. Encode, comme donnée, la règle métier
 * québécoise permettant l'adaptation automatique (type, mode, valeur, contexte).
 */
@Entity
public class ClauseCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "clause_id")
    private Clause clause;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.AttributCondition attribut;

    /** Valeur attendue (ex. code d'énumération, ou seuil numérique en chaîne). */
    @Column(nullable = false)
    private String valeur;

    public Long getId() { return id; }
    public Clause getClause() { return clause; }
    public void setClause(Clause clause) { this.clause = clause; }
    public Enums.AttributCondition getAttribut() { return attribut; }
    public void setAttribut(Enums.AttributCondition a) { this.attribut = a; }
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
}
