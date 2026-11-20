package qc.levis.contrat.clm.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Gabarit Word d'une version de modèle (addenda 4, Q22) : le {@code .docx} d'origine de l'organisation, dont la mise en
 * page est conservée ; seules ses variables {@code {{cle}}} et son marqueur {@code {{clauses}}} sont remplacés à la
 * génération. Il suit le cycle de vie du modèle : déposé ou retiré seulement sur un brouillon.
 */
@Entity
@Table(name = "clm_modele_gabarit")
public class GabaritWord {

    /** Un gabarit est un document de mise en page, sans pièces jointes volumineuses : 20 Mo suffisent largement. */
    public static final long TAILLE_MAX = 20L * 1_048_576;

    @Id
    @Column(name = "modele_id")
    private Long modeleId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String nom;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenu", nullable = false)
    private byte[] contenu;

    /** Variables relevées au dépôt, pour valider le modèle sans relire le document. */
    @Column(nullable = false, length = 4000)
    private String variables;

    @Column(name = "depose_par", nullable = false, length = 150)
    private String deposePar;

    @Column(name = "depose_le", nullable = false)
    private Instant deposeLe;

    protected GabaritWord() {
    }

    public GabaritWord(ModeleContrat modele, Depot depot, String par, Instant le) {
        modele.exigerBrouillon();
        String nom = depot.nom();
        byte[] contenu = depot.contenu();
        if (FormatDocument.verifier(nom, contenu, TAILLE_MAX) != FormatDocument.Type.DOCX) {
            throw new IllegalArgumentException("Le gabarit doit être un document Word (.docx).");
        }
        this.modeleId = modele.getId();
        this.tenantId = modele.getTenantId();
        this.nom = FormatDocument.nomSur(nom);
        this.contenu = contenu.clone();
        this.variables = String.join(",", depot.variables());
        if (variables.length() > 4000) {
            throw new IllegalArgumentException("Gabarit Word : trop de variables distinctes.");
        }
        this.deposePar = par;
        this.deposeLe = le;
    }

    /** Copie pour une nouvelle version du modèle : le gabarit suit le modèle d'une version à l'autre. */
    public GabaritWord pour(ModeleContrat version, Instant le) {
        return new GabaritWord(version, new Depot(nom, contenu, variables()), deposePar, le);
    }

    /** Fichier déposé et variables qu'il contient (lues par l'adaptateur de rendu). */
    public record Depot(String nom, byte[] contenu, java.util.Set<String> variables) {
    }

    /** Variables du gabarit que le modèle ne déclare pas : elles resteraient entre crochets dans le contrat. */
    public java.util.List<String> inconnues(java.util.Set<String> cles) {
        return variables().stream().filter(v -> !cles.contains(v) && !Modele.MARQUEUR_CLAUSES.equals(v)).toList();
    }

    public java.util.Set<String> variables() {
        return variables.isEmpty() ? java.util.Set.of() : new java.util.LinkedHashSet<>(java.util.List.of(variables.split(",")));
    }

    public Long getModeleId() { return modeleId; }
    public Long getTenantId() { return tenantId; }
    public String getNom() { return nom; }
    public byte[] getContenu() { return contenu.clone(); }
    public String getDeposePar() { return deposePar; }
    public Instant getDeposeLe() { return deposeLe; }
}
