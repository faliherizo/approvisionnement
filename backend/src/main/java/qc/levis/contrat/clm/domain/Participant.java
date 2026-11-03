package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Participant d'une trousse (ST-03/ST-09) : interne ou externe, avec le niveau d'identité exigé pour signer. */
@Entity
@Table(name = "clm_participant", indexes = @Index(name = "ix_clm_part_trousse", columnList = "tenant_id,trousse_id"))
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 150)
    private String courriel;

    @Column(length = 30)
    private String telephone;

    /** Rôle dans le contrat (ex. SIGNATAIRE_1), référencé par les zones et champs du modèle. */
    @Column(name = "role_contrat", nullable = false, length = 60)
    private String roleContrat;

    @Column(nullable = false)
    private boolean externe;

    @Enumerated(EnumType.STRING)
    @Column(name = "niveau_identite", nullable = false, length = 12)
    private Identite.Niveau niveauIdentite;

    @Column(name = "question_identite", length = 250)
    private String questionIdentite;

    @Column(name = "reponse_identite_hash", length = 64)
    private String reponseIdentiteHash;

    protected Participant() {
    }

    public Participant(Long tenantId, Long trousseId, String nom, String courriel, String telephone, String roleContrat,
                       boolean externe, Identite.Niveau niveauIdentite) {
        this.tenantId = tenantId;
        this.trousseId = trousseId;
        this.nom = nom;
        this.courriel = courriel;
        this.telephone = telephone;
        this.roleContrat = roleContrat;
        this.externe = externe;
        this.niveauIdentite = niveauIdentite;
    }

    public void definirQuestion(String question, String reponseHash) {
        this.questionIdentite = question;
        this.reponseIdentiteHash = reponseHash;
    }

    public Long getId() { return id; }
    public Long getTrousseId() { return trousseId; }
    public String getNom() { return nom; }
    public String getCourriel() { return courriel; }
    public String getTelephone() { return telephone; }
    public String getRoleContrat() { return roleContrat; }
    public boolean isExterne() { return externe; }
    public Identite.Niveau getNiveauIdentite() { return niveauIdentite; }
    public String getQuestionIdentite() { return questionIdentite; }
    public String getReponseIdentiteHash() { return reponseIdentiteHash; }
}
