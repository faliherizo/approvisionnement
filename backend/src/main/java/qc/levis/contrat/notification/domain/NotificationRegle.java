package qc.levis.contrat.notification.domain;

import jakarta.persistence.*;

/**
 * Règle de notification : « quand {@code declencheur} survient sur {@code formulaireId}, envoyer le
 * gabarit {@code modeleCode} aux {@code destinataires} ». {@code declencheur} : SOUMISSION | VALIDATION
 * | REJET. {@code destinataires} : liste séparée par des virgules (courriels/numéros) ou marqueurs.
 */
@Entity
@Table(name = "notification_regle")
public class NotificationRegle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "formulaire_id", nullable = false)
    private Long formulaireId;

    @Column(nullable = false, length = 30)
    private String declencheur;

    @Column(name = "modele_code", nullable = false, length = 80)
    private String modeleCode;

    @Column(length = 2000)
    private String destinataires;

    @Column(nullable = false)
    private boolean actif = true;

    public NotificationRegle() {}

    public NotificationRegle(Long tenantId, Long formulaireId, String declencheur, String modeleCode, String destinataires) {
        this.tenantId = tenantId;
        this.formulaireId = formulaireId;
        this.declencheur = declencheur;
        this.modeleCode = modeleCode;
        this.destinataires = destinataires;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getFormulaireId() { return formulaireId; }
    public void setFormulaireId(Long formulaireId) { this.formulaireId = formulaireId; }
    public String getDeclencheur() { return declencheur; }
    public void setDeclencheur(String declencheur) { this.declencheur = declencheur; }
    public String getModeleCode() { return modeleCode; }
    public void setModeleCode(String modeleCode) { this.modeleCode = modeleCode; }
    public String getDestinataires() { return destinataires; }
    public void setDestinataires(String destinataires) { this.destinataires = destinataires; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
