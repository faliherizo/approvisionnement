package qc.levis.contrat.clm.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Invitation à signer (ST-09, RST-17, UI-16). Seule l'empreinte du jeton est conservée : une fuite de la base ne
 * donne pas accès au portail. La signature exige, dans l'ordre, l'identité vérifiée puis le consentement explicite.
 */
@Entity
@Table(name = "clm_invitation", indexes = {
        @Index(name = "ix_clm_inv_jeton", columnList = "jeton_hash", unique = true),
        @Index(name = "ix_clm_inv_trousse", columnList = "tenant_id,trousse_id")})
public class Invitation {

    public enum Statut { ENVOYEE, VERIFIEE, SIGNEE, REFUSEE, ANNULEE, BLOQUEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "etape_id", nullable = false)
    private Long etapeId;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "jeton_hash", nullable = false, length = 64)
    private String jetonHash;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Statut statut = Statut.ENVOYEE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Identite.Niveau niveau;

    @Column(name = "code_hash", length = 64)
    private String codeHash;

    @Column(name = "code_expire_le")
    private Instant codeExpireLe;

    @Column(nullable = false)
    private int tentatives;

    @Column(name = "verifiee_le")
    private Instant verifieeLe;

    @Column(name = "consentement_le")
    private Instant consentementLe;

    @Column(name = "signee_le")
    private Instant signeeLe;

    @Column(name = "nom_signe", length = 150)
    private String nomSigne;

    @Column(length = 64)
    private String ip;

    @Column(name = "agent", length = 300)
    private String agent;

    @Column(name = "attestation_json", length = 10_000)
    private String attestationJson;

    @Column(name = "champs_json", length = 20_000)
    private String champsJson;

    @Column(length = 1000)
    private String motif;

    protected Invitation() {
    }

    public Invitation(Long tenantId, Long trousseId, Long etapeId, Long participantId, String jetonHash, Instant expireLe,
                      Identite.Niveau niveau) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.etapeId = etapeId;
        this.participantId = participantId;
        this.jetonHash = jetonHash;
        this.expireLe = expireLe;
        this.niveau = niveau;
    }

    public boolean estExpiree(Instant maintenant) {
        return maintenant.isAfter(expireLe);
    }

    /** Refuse toute action sur une invitation close ou expirée, avec un motif lisible par le signataire. */
    public void exigerOuverte(Instant maintenant) {
        switch (statut) {
            case SIGNEE, REFUSEE -> throw new IllegalStateException("Lien déjà utilisé.");
            case ANNULEE -> throw new IllegalStateException("Document retiré : cette invitation a été annulée.");
            case BLOQUEE -> throw new IllegalStateException("Vérification échouée : trop de tentatives.");
            default -> {
                if (estExpiree(maintenant)) {
                    throw new IllegalStateException("Invitation expirée.");
                }
            }
        }
    }

    public void preparerCode(String codeHash, Instant expireLe) {
        this.codeHash = codeHash;
        this.codeExpireLe = expireLe;
    }

    /** Une réponse fausse consomme une tentative ; à la limite, l'invitation est bloquée. */
    public void echecVerification() {
        this.tentatives++;
        if (tentatives >= Identite.TENTATIVES_MAX) {
            this.statut = Statut.BLOQUEE;
        }
    }

    public void verifier(Instant maintenant) {
        this.statut = Statut.VERIFIEE;
        this.verifieeLe = maintenant;
        this.codeHash = null;
    }

    public void consentir(Instant maintenant) {
        if (statut != Statut.VERIFIEE) {
            throw new IllegalStateException("Identité à vérifier avant le consentement.");
        }
        this.consentementLe = maintenant;
    }

    public void signer(String nom, String ip, String agent, String champsJson, String attestationJson, Instant maintenant) {
        if (statut != Statut.VERIFIEE) {
            throw new IllegalStateException("Identité à vérifier avant de signer.");
        }
        if (consentementLe == null) {
            throw new IllegalStateException("Consentement explicite requis avant de signer.");
        }
        this.statut = Statut.SIGNEE;
        this.nomSigne = nom;
        this.ip = ip;
        this.agent = agent;
        this.champsJson = champsJson;
        this.attestationJson = attestationJson;
        this.signeeLe = maintenant;
    }

    public void refuser(String motif, String ip, Instant maintenant) {
        this.statut = Statut.REFUSEE;
        this.motif = motif;
        this.ip = ip;
        this.signeeLe = maintenant;
    }

    public void annuler() {
        if (statut != Statut.SIGNEE && statut != Statut.REFUSEE) {
            this.statut = Statut.ANNULEE;
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public Long getEtapeId() { return etapeId; }
    public Long getParticipantId() { return participantId; }
    public Instant getExpireLe() { return expireLe; }
    public Statut getStatut() { return statut; }
    public Identite.Niveau getNiveau() { return niveau; }
    public String getCodeHash() { return codeHash; }
    public Instant getCodeExpireLe() { return codeExpireLe; }
    public int getTentatives() { return tentatives; }
    public Instant getVerifieeLe() { return verifieeLe; }
    public Instant getConsentementLe() { return consentementLe; }
    public Instant getSigneeLe() { return signeeLe; }
    public String getNomSigne() { return nomSigne; }
    public String getIp() { return ip; }
    public String getAgent() { return agent; }
    public String getAttestationJson() { return attestationJson; }
    public String getChampsJson() { return champsJson; }
    public String getMotif() { return motif; }
}
