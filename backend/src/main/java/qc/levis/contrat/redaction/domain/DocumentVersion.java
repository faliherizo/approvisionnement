package qc.levis.contrat.redaction.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Instantané versionné d'un document (support du versionnage / redlining). */
@Entity
public class DocumentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private int numeroVersion;

    /** Sérialisation JSON des clauses au moment de l'instantané. */
    @Column(length = 20000)
    private String contenuJson;

    @Column(length = 500)
    private String note;

    private String creePar;
    private Instant creeLe = Instant.now();

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public int getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(int n) { this.numeroVersion = n; }
    public String getContenuJson() { return contenuJson; }
    public void setContenuJson(String c) { this.contenuJson = c; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCreePar() { return creePar; }
    public void setCreePar(String creePar) { this.creePar = creePar; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
}
