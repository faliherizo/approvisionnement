package qc.levis.contrat.clm.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Résultat d'une analyse CLM/IA (ST-15) : sans effet tant qu'un humain ne l'a pas validé avec justification. */
@Entity
@Table(name = "clm_analyse", indexes = @Index(name = "ix_clm_analyse_trousse", columnList = "tenant_id,trousse_id"))
public class AnalyseIa {

    public enum Statut { A_REVISER, VALIDEE, REJETEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "resultat_json", nullable = false, length = 20_000)
    private String resultatJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Statut statut = Statut.A_REVISER;

    @Column(name = "lancee_par", nullable = false, length = 150)
    private String lanceePar;

    @Column(name = "revisee_par", length = 150)
    private String reviseePar;

    @Column(length = 2000)
    private String justification;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "revisee_le")
    private Instant reviseeLe;

    protected AnalyseIa() {
    }

    public AnalyseIa(Long tenantId, Long trousseId, Long documentId, String resultatJson, String lanceePar, Instant maintenant) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.documentId = documentId;
        this.resultatJson = resultatJson;
        this.lanceePar = lanceePar;
        this.creeLe = maintenant;
    }

    public void reviser(boolean valide, String par, String justification, Instant maintenant) {
        if (statut != Statut.A_REVISER) {
            throw new IllegalStateException("Analyse déjà révisée.");
        }
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Justification requise.");
        }
        this.statut = valide ? Statut.VALIDEE : Statut.REJETEE;
        this.reviseePar = par;
        this.justification = justification.trim();
        this.reviseeLe = maintenant;
    }

    public Long getId() { return id; }
    public Long getTrousseId() { return trousseId; }
    public Long getDocumentId() { return documentId; }
    public String getResultatJson() { return resultatJson; }
    public Statut getStatut() { return statut; }
    public String getLanceePar() { return lanceePar; }
    public String getReviseePar() { return reviseePar; }
    public String getJustification() { return justification; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getReviseeLe() { return reviseeLe; }
}
