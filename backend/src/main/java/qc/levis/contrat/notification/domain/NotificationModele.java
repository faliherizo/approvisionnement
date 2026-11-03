package qc.levis.contrat.notification.domain;

import jakarta.persistence.*;

/**
 * Gabarit de notification configurable. {@code canal} : COURRIEL | SMS | INTERNE (chaîne, non figée).
 * {@code sujet}/{@code corps} peuvent contenir des marqueurs {@code {{code_champ}}} fusionnés au moment
 * de l'envoi. Tenant-scopé.
 */
@Entity
@Table(name = "notification_modele",
        uniqueConstraints = @UniqueConstraint(name = "uq_notif_modele", columnNames = {"tenant_id", "code"}))
public class NotificationModele {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(length = 300)
    private String sujet;

    @Column(length = 4000)
    private String corps;

    public NotificationModele() {}

    public NotificationModele(Long tenantId, String code, String canal, String sujet, String corps) {
        this.tenantId = tenantId;
        this.code = code;
        this.canal = canal;
        this.sujet = sujet;
        this.corps = corps;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }
}
