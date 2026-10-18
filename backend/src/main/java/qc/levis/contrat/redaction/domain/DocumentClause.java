package qc.levis.contrat.redaction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/** Clause effectivement retenue dans un document, avec son ordre et son texte. */
@Entity
public class DocumentClause {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    private Long clauseId;
    private String code;
    private String numero;
    private String section;
    private String titre;

    @Column(length = 8000)
    private String texteEffectif;

    private int ordre;

    /** Vrai si le texte a été adapté localement dans ce document (diverge du catalogue). */
    @Column(name = "modifie_localement")
    private boolean modifieLocalement = false;

    /** Recopie de la clause : normative (verrouillée) car issue d'un référentiel obligatoire. */
    private boolean normative = false;

    /** Section rédigée librement (hors catalogue de clauses) : aucune clause d'origine. */
    public boolean isLibre() { return clauseId == null; }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public Long getClauseId() { return clauseId; }
    public void setClauseId(Long clauseId) { this.clauseId = clauseId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getTexteEffectif() { return texteEffectif; }
    public void setTexteEffectif(String t) { this.texteEffectif = t; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public boolean isModifieLocalement() { return modifieLocalement; }
    public void setModifieLocalement(boolean v) { this.modifieLocalement = v; }
    public boolean isNormative() { return normative; }
    public void setNormative(boolean normative) { this.normative = normative; }
}
