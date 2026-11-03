package qc.levis.contrat.processus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Demande d'approbation d'un document (ex. une fiche, un achat). Statut global dérivé de ses étapes :
 * PENDING tant qu'une étape est en attente, APPROVED si toutes approuvées, REJECTED dès un refus.
 */
@Entity
@Table(name = "demande_approbation", indexes = @Index(name = "ix_demappro", columnList = "tenant_id,statut"))
public class DemandeApprobation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "document_type", nullable = false, length = 80)
    private String documentType;

    @Column(name = "document_id", length = 120)
    private String documentId;

    @Column(name = "contexte_json", length = 100_000)
    private String contexteJson;

    @Column(nullable = false, length = 150)
    private String demandeur;

    @Column(nullable = false, length = 20)
    private String statut = "PENDING";

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe = Instant.now();

    public DemandeApprobation() {}

    public DemandeApprobation(Long tenantId, String documentType, String documentId, String contexteJson, String demandeur) {
        this.tenantId = tenantId;
        this.documentType = documentType;
        this.documentId = documentId;
        this.contexteJson = contexteJson;
        this.demandeur = demandeur;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getDocumentType() { return documentType; }
    public String getDocumentId() { return documentId; }
    public String getContexteJson() { return contexteJson; }
    public String getDemandeur() { return demandeur; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Instant getCreeLe() { return creeLe; }
}
