package qc.levis.contrat.addenda.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/** Modification apportée à une clause par un addenda. */
@Entity
public class AddendaChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "addenda_id")
    private Addenda addenda;

    private Long clauseId;
    private String code;

    @Column(length = 8000)
    private String ancienTexte;

    @Column(length = 8000)
    private String nouveauTexte;

    public Long getId() { return id; }
    public Addenda getAddenda() { return addenda; }
    public void setAddenda(Addenda addenda) { this.addenda = addenda; }
    public Long getClauseId() { return clauseId; }
    public void setClauseId(Long clauseId) { this.clauseId = clauseId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getAncienTexte() { return ancienTexte; }
    public void setAncienTexte(String t) { this.ancienTexte = t; }
    public String getNouveauTexte() { return nouveauTexte; }
    public void setNouveauTexte(String t) { this.nouveauTexte = t; }
}
