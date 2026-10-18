package qc.levis.contrat.ia.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Trace d'une suggestion de l'assistant IA (art. 21 : traçabilité des suggestions). */
@Entity
@Table(name = "ai_suggestion")
public class AiSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeInteraction;

    @Column(length = 4000)
    private String invite;

    @Column(length = 8000)
    private String reponse;

    private String modele;
    private String utilisateur;
    private Instant creeLe = Instant.now();

    @Column(name = "tenant_id")
    private Long tenantId;

    public AiSuggestion() {}

    public AiSuggestion(String typeInteraction, String invite, String reponse, String modele,
                        String utilisateur, Long tenantId) {
        this.typeInteraction = typeInteraction;
        this.invite = invite;
        this.reponse = reponse;
        this.modele = modele;
        this.utilisateur = utilisateur;
        this.tenantId = tenantId;
    }

    public Long getId() { return id; }
    public String getTypeInteraction() { return typeInteraction; }
    public void setTypeInteraction(String t) { this.typeInteraction = t; }
    public String getInvite() { return invite; }
    public void setInvite(String invite) { this.invite = invite; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
    public Instant getCreeLe() { return creeLe; }
    public void setCreeLe(Instant creeLe) { this.creeLe = creeLe; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
