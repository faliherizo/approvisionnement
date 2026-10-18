package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;

/** Annexe d'un document contractuel (numérotée, éventuellement rattachée à une clause). */
@Entity
@Table(name = "document_annexe")
public class DocumentAnnexe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    private String numero;

    @Column(nullable = false)
    private String titre;

    /** Référence de la clause à laquelle l'annexe se rapporte (ex. « 0.01.07 »). */
    private String referenceClause;

    @Column(length = 8000)
    private String contenu;

    private int ordre = 0;

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getReferenceClause() { return referenceClause; }
    public void setReferenceClause(String referenceClause) { this.referenceClause = referenceClause; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
}
