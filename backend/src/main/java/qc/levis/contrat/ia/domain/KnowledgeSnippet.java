package qc.levis.contrat.ia.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Fragment de connaissance alimentant l'assistant IA (RAG). Permet de spécialiser
 * l'IA en rédaction contractuelle à partir de documents / données ajoutés. Scopé par tenant.
 */
@Entity
public class KnowledgeSnippet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(nullable = false)
    private String titre;

    /** Provenance (URL, référence légale, document interne…). */
    private String source;

    @Column(nullable = false, length = 8000)
    private String contenu;

    private Instant creeLe = Instant.now();

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
}
