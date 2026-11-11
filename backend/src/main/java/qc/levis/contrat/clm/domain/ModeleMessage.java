package qc.levis.contrat.clm.domain;

import jakarta.persistence.*;

/** Message personnalisable par événement et canal (ST-11, UI-17) ; variables {@code {{nom}}} fusionnées à l'envoi. */
@Entity
@Table(name = "clm_modele_message", uniqueConstraints = @UniqueConstraint(name = "uk_clm_msg", columnNames = {"tenant_id", "evenement", "canal"}))
public class ModeleMessage {

    public enum Evenement { INVITATION, TACHE, RAPPEL, ECHEANCE, RETARD, REJET, TERMINEE, CODE }

    public static final java.util.Set<String> VARIABLES = java.util.Set.of(
            "reference", "titre", "etape", "destinataire", "lien", "echeance", "code", "motif");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Evenement evenement;

    @Column(nullable = false, length = 12)
    private String canal;

    @Column(nullable = false, length = 200)
    private String sujet;

    @Column(nullable = false, length = 4000)
    private String corps;

    @Column(nullable = false)
    private boolean actif = true;

    protected ModeleMessage() {
    }

    public ModeleMessage(Long tenantId, Evenement evenement, String canal, String sujet, String corps) {
        this.tenantId = tenantId;
        this.evenement = evenement;
        this.canal = canal;
        this.sujet = sujet;
        this.corps = corps;
    }

    public Long getId() { return id; }
    public String getCanal() { return canal; }
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public String getCorps() { return corps; }
    public void setCorps(String corps) { this.corps = corps; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
