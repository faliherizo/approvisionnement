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
import java.util.Locale;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Langue langue = Langue.FR;

    /** Qualité du signataire (ex. « Président ») et entreprise qu'il représente (addenda 4, Q6). */
    @Column(length = 150)
    private String qualite;

    @Column(name = "entreprise_representee", length = 200)
    private String entreprise;

    /** IQ exige du représentant une attestation de son autorité avant la signature. */
    @Column(name = "autorite_requise", nullable = false)
    private boolean autoriteRequise;

    /** Ce que le participant fait au portail (AF-48, RST-50) ; SIGNER pour les participants antérieurs. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature", nullable = false, length = 10)
    private NatureParticipation nature = NatureParticipation.SIGNER;

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

    /**
     * Signature pour une entreprise (addenda 4, Q6) : qualité et entreprise représentée. Exiger une attestation d'autorité
     * suppose de savoir pour qui et à quel titre le signataire s'engage.
     */
    public void representer(String qualite, String entreprise, boolean autoriteRequise) {
        String q = vide(qualite);
        String e = vide(entreprise);
        if (autoriteRequise && (q == null || e == null)) {
            throw new IllegalArgumentException("Attestation d'autorité : qualité du signataire et entreprise représentée requises.");
        }
        if (q != null && q.length() > 150 || e != null && e.length() > 200) {
            throw new IllegalArgumentException("Qualité : 150 caractères au plus ; entreprise : 200 au plus.");
        }
        this.qualite = q;
        this.entreprise = e;
        this.autoriteRequise = autoriteRequise;
    }

    /** Avant de signer : l'attestation d'autorité est donnée si IQ l'exige. */
    public void exigerAutorite(boolean attestee) {
        if (autoriteRequise && !attestee) {
            throw new IllegalArgumentException("Attestez être dûment autorisé à signer pour " + entreprise + " avant de signer.");
        }
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    /** Langue des messages et du portail du participant ; nulle : français (addenda 2, Q19). */
    public void choisirLangue(Langue langue) {
        this.langue = langue == null ? Langue.FR : langue;
    }

    /** Nature de la participation ; nulle : SIGNER, la seule nature connue avant RST-50. */
    public void choisirNature(NatureParticipation nature) {
        this.nature = nature == null ? NatureParticipation.SIGNER : nature;
    }

    /** Refuse au portail toute action qui n'est pas celle de la nature du participant. */
    public void exigerAction(NatureParticipation action) {
        nature.exiger(action);
    }

    public void definirQuestion(String question, String reponseHash) {
        this.questionIdentite = question;
        this.reponseIdentiteHash = reponseHash;
    }

    /** Une zone de signature d'un modèle désigne un rôle : attribuer la zone, c'est attribuer ce rôle au participant (UI-15). */
    public void attribuerRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Rôle dans le contrat requis.");
        }
        this.roleContrat = role.trim().toUpperCase(Locale.ROOT);
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
    public Langue getLangue() { return langue; }
    public String getQualite() { return qualite; }
    public String getEntreprise() { return entreprise; }
    public boolean isAutoriteRequise() { return autoriteRequise; }
    public NatureParticipation getNature() { return nature; }

    /** Destruction à l'échéance de conservation : les coordonnées du participant sont effacées. */
    public void anonymiser() {
        this.nom = "Participant anonymisé";
        this.courriel = "anonyme-" + id + "@supprime.invalid";
        this.telephone = null;
        this.questionIdentite = null;
        this.reponseIdentiteHash = null;
    }
}
