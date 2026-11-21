package qc.levis.contrat.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.tenant.domain.Tenant;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String motDePasseHash;

    private String nom;
    private String direction;

    @Column(nullable = false)
    private boolean actif = true;

    /** Tenant d'appartenance (null = superadmin global). */
    @Column(name = "tenant_id")
    private Long tenantId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_code")
    private Set<Enums.Role> roles = new HashSet<>();

    // --- Second facteur d'authentification ---
    @Column(name = "mfa_active", nullable = false)
    private boolean mfaActive = false;

    /** Compte local de secours désigné et approuvé (addenda 4, Q12) : seul admis en connexion locale si l'organisation la réserve. */
    @jakarta.persistence.Column(name = "compte_secours", nullable = false)
    private boolean compteSecours = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_methode")
    private Enums.MfaMethode mfaMethode = Enums.MfaMethode.NONE;

    @JsonIgnore
    @Column(name = "totp_secret")
    private String totpSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_security_question", joinColumns = @JoinColumn(name = "user_id"))
    private List<SecurityQuestion> questionsSecurite = new ArrayList<>();

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String h) { this.motDePasseHash = h; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Set<Enums.Role> getRoles() { return roles; }
    public void setRoles(Set<Enums.Role> roles) { this.roles = roles; }
    public boolean isMfaActive() { return mfaActive; }
    public boolean isCompteSecours() { return compteSecours; }
    public void setCompteSecours(boolean compteSecours) { this.compteSecours = compteSecours; }
    public void setMfaActive(boolean mfaActive) { this.mfaActive = mfaActive; }
    public Enums.MfaMethode getMfaMethode() { return mfaMethode; }
    public void setMfaMethode(Enums.MfaMethode m) { this.mfaMethode = m; }
    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }
    public List<SecurityQuestion> getQuestionsSecurite() { return questionsSecurite; }
    public void setQuestionsSecurite(List<SecurityQuestion> q) { this.questionsSecurite = q; }

    /**
     * Loi 25 : le compte ne désigne plus personne. Il reste en base, désactivé et sans moyen de connexion, pour que les
     * références vers lui (droits, journaux) restent cohérentes.
     */
    public void effacer(String courrielPseudonyme, String empreinteInutilisable) {
        email = courrielPseudonyme;
        nom = "Personne effacée";
        direction = null;
        motDePasseHash = empreinteInutilisable;
        actif = false;
        roles = new HashSet<>();
        compteSecours = false;
        mfaActive = false;
        mfaMethode = Enums.MfaMethode.NONE;
        totpSecret = null;
        questionsSecurite = new ArrayList<>();
    }
}
