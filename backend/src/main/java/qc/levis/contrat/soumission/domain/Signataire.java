package qc.levis.contrat.soumission.domain;

import jakarta.persistence.*;
import java.time.Instant;
import qc.levis.contrat.commun.domain.Enums;

/** Signataire d'un document dans la cérémonie de signature électronique. */
@Entity
public class Signataire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String courriel;

    /** Rôle du signataire (ex. « Ville de Lévis », « Cocontractant »). */
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.StatutSignature statut = Enums.StatutSignature.EN_ATTENTE;

    /** Jeton du lien de signature transmis au signataire. */
    @Column(nullable = false, unique = true)
    private String jeton;

    private Instant signeLe;

    /** Signature manuscrite (canvas) ou image téléversée, encodée en data URL base64. */
    @Column(name = "signature_image", length = 1000000)
    private String signatureImage;

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCourriel() { return courriel; }
    public void setCourriel(String courriel) { this.courriel = courriel; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Enums.StatutSignature getStatut() { return statut; }
    public void setStatut(Enums.StatutSignature s) { this.statut = s; }
    public String getJeton() { return jeton; }
    public void setJeton(String jeton) { this.jeton = jeton; }
    public Instant getSigneLe() { return signeLe; }
    public void setSigneLe(Instant signeLe) { this.signeLe = signeLe; }
    public String getSignatureImage() { return signatureImage; }
    public void setSignatureImage(String signatureImage) { this.signatureImage = signatureImage; }
}
