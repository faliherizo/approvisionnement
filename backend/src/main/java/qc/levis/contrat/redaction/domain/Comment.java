package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Commentaire en ligne rattaché à un document (et optionnellement à une clause). */
@Entity
@Table(name = "commentaire")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    private Long clauseId;

    @Column(nullable = false)
    private String auteur;

    @Column(nullable = false, length = 4000)
    private String corps;

    private boolean resolu = false;
    private String resoluPar;

    /** Commentaire parent pour un fil de discussion (art. 7.5) ; nul = commentaire racine. */
    @Column(name = "parent_id")
    private Long parentId;

    private Instant creeLe = Instant.now();

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long d) { this.documentId = d; }
    public Long getClauseId() { return clauseId; }
    public void setClauseId(Long c) { this.clauseId = c; }
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }
    public boolean isResolu() { return resolu; }
    public void setResolu(boolean resolu) { this.resolu = resolu; }
    public String getResoluPar() { return resoluPar; }
    public void setResoluPar(String resoluPar) { this.resoluPar = resoluPar; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }

    /** Loi 25 : le commentaire garde son texte (il porte sur le contrat), mais plus le nom de la personne. */
    public void pseudonymiser(String courriel, String pseudonyme) {
        if (courriel.equalsIgnoreCase(auteur)) auteur = pseudonyme;
        if (courriel.equalsIgnoreCase(resoluPar)) resoluPar = pseudonyme;
    }
}
