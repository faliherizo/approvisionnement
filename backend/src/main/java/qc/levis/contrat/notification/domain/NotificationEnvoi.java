package qc.levis.contrat.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Journal des notifications envoyées (traçabilité). */
@Entity
@Table(name = "notification_envoi")
public class NotificationEnvoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(length = 300)
    private String destinataire;

    @Column(length = 300)
    private String sujet;

    @Column(length = 4000)
    private String corps;

    @Column(nullable = false, length = 20)
    private String statut;

    @Column(name = "envoye_le", nullable = false)
    private Instant envoyeLe = Instant.now();

    public NotificationEnvoi() {}

    public NotificationEnvoi(Long tenantId, String canal, String destinataire, String sujet, String corps, String statut) {
        this.tenantId = tenantId;
        this.canal = canal;
        this.destinataire = destinataire;
        this.sujet = sujet;
        this.corps = corps;
        this.statut = statut;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getCanal() { return canal; }
    public String getDestinataire() { return destinataire; }
    public String getSujet() { return sujet; }
    public String getCorps() { return corps; }
    public String getStatut() { return statut; }

    /** Loi 25 : l'envoi reste au journal des notifications, sans destinataire ni contenu qui identifie la personne. */
    public void pseudonymiser(String courrielPseudonyme) {
        destinataire = courrielPseudonyme;
        sujet = CONTENU_EFFACE;
        corps = CONTENU_EFFACE;
    }

    public static final String CONTENU_EFFACE = "[contenu effacé — Loi 25]";
    public Instant getEnvoyeLe() { return envoyeLe; }
}
