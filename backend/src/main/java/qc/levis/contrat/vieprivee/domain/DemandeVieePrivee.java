package qc.levis.contrat.vieprivee.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Registre des demandes d'accès et d'effacement (Loi 25, art. 27 à 28.1 ; preuve du délai de réponse de 30 jours).
 * La personne n'y est désignée que par l'empreinte de son courriel : après un effacement, le registre prouve que la demande
 * a été traitée sans conserver l'adresse effacée.
 */
@Entity
@Table(name = "vie_privee_demande")
public class DemandeVieePrivee {

    public enum Type { ACCES, EFFACEMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long tenantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, updatable = false, length = 20) private Type type;
    @Column(nullable = false, updatable = false, length = 64) private String empreinteCourriel;
    @Column(nullable = false, updatable = false, length = 150) private String traitePar;
    @Column(updatable = false, length = 500) private String motif;
    @Column(nullable = false, updatable = false, length = 2000) private String resultat;
    @Column(nullable = false, updatable = false) private Instant traiteeLe;

    protected DemandeVieePrivee() { }

    public DemandeVieePrivee(Long tenantId, Type type, String empreinteCourriel, String traitePar, String motif,
                             String resultat, Instant traiteeLe) {
        this.tenantId = tenantId;
        this.type = type;
        this.empreinteCourriel = empreinteCourriel;
        this.traitePar = traitePar;
        this.motif = motif;
        this.resultat = resultat;
        this.traiteeLe = traiteeLe;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Type getType() { return type; }
    public String getEmpreinteCourriel() { return empreinteCourriel; }
    public String getTraitePar() { return traitePar; }
    public String getMotif() { return motif; }
    public String getResultat() { return resultat; }
    public Instant getTraiteeLe() { return traiteeLe; }
}
