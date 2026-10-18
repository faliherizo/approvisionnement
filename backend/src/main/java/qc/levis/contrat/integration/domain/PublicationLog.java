package qc.levis.contrat.integration.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;

/** Traçabilité d'un dépôt/envoi d'un document vers une cible externe (art. 9). */
@Entity
@Table(name = "publication_log")
public class PublicationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.CiblePublication cible;

    @Column(nullable = false)
    private boolean succes;

    @Column(name = "url_cible", length = 2000)
    private String urlCible;

    @Column(length = 2000)
    private String message;

    private String publiePar;
    private Instant publieLe = Instant.now();

    @Column(name = "tenant_id")
    private Long tenantId;

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public Enums.CiblePublication getCible() { return cible; }
    public void setCible(Enums.CiblePublication cible) { this.cible = cible; }
    public boolean isSucces() { return succes; }
    public void setSucces(boolean succes) { this.succes = succes; }
    public String getUrlCible() { return urlCible; }
    public void setUrlCible(String urlCible) { this.urlCible = urlCible; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPubliePar() { return publiePar; }
    public void setPubliePar(String publiePar) { this.publiePar = publiePar; }
    public Instant getPublieLe() { return publieLe; }
    public void setPublieLe(Instant publieLe) { this.publieLe = publieLe; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
