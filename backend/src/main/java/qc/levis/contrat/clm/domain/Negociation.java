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
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Cycle de négociation d'une trousse avec une contrepartie externe (RST-55, IA-05). La contrepartie y accède par un lien
 * dont seule l'empreinte du jeton est conservée ; chaque tour clos par l'organisation produit une version numérotée.
 */
@Entity
@Table(name = "clm_negociation", indexes = @Index(name = "ix_clm_nego_trousse", columnList = "tenant_id,trousse_id"))
public class Negociation {

    /** Tiers avec qui l'organisation négocie : il n'a pas de compte, son courriel reçoit le lien. */
    public record Contrepartie(String nom, String courriel, String entreprise, Langue langue) {

        private static final Pattern COURRIEL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

        public Contrepartie {
            nom = nom == null ? "" : nom.strip();
            courriel = courriel == null ? "" : courriel.strip();
            entreprise = entreprise == null || entreprise.isBlank() ? null : entreprise.strip();
            langue = langue == null ? Langue.FR : langue;
            if (nom.length() < 2) {
                throw new IllegalArgumentException("Nom de la contrepartie requis.");
            }
            if (!COURRIEL.matcher(courriel).matches()) {
                throw new IllegalArgumentException("Courriel de la contrepartie invalide.");
            }
        }
    }

    /** Données d'ouverture : la trousse, la contrepartie, le lien (empreinte et échéance) et qui l'ouvre. */
    public record Ouverture(Long tenantId, Long trousseId, Contrepartie contrepartie, String jetonHash, Instant expireLe, String par,
                            Instant le) {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id", nullable = false)
    private Long trousseId;

    @Column(name = "contrepartie_nom", nullable = false, length = 150)
    private String contrepartieNom;

    @Column(name = "contrepartie_courriel", nullable = false, length = 254)
    private String contrepartieCourriel;

    @Column(name = "contrepartie_entreprise", length = 200)
    private String contrepartieEntreprise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Langue langue;

    @Column(name = "jeton_hash", nullable = false, length = 64, unique = true)
    private String jetonHash;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 22)
    private EtatNegociation etat = EtatNegociation.ATTENTE_CONTREPARTIE;

    @Column(name = "version_courante", nullable = false)
    private int versionCourante = 1;

    @Column(nullable = false)
    private int tour = 1;

    @Column(name = "ouverte_par", nullable = false, length = 150)
    private String ouvertePar;

    @Column(name = "ouverte_le", nullable = false)
    private Instant ouverteLe;

    @Column(name = "terminee_par", length = 150)
    private String termineePar;

    @Column(name = "terminee_le")
    private Instant termineeLe;

    @Column(name = "motif_fin", length = 1000)
    private String motifFin;

    @Version
    private long verrou;

    protected Negociation() {
    }

    public Negociation(Ouverture o) {
        this.tenantId = o.tenantId();
        this.trousseId = o.trousseId();
        this.contrepartieNom = o.contrepartie().nom();
        this.contrepartieCourriel = o.contrepartie().courriel();
        this.contrepartieEntreprise = o.contrepartie().entreprise();
        this.langue = o.contrepartie().langue();
        this.jetonHash = o.jetonHash();
        this.expireLe = o.expireLe();
        this.ouvertePar = o.par();
        this.ouverteLe = o.le();
    }

    /** Le lien de la contrepartie n'ouvre plus rien une fois la négociation terminée ou le délai échu. */
    public void exigerAccessible(Instant maintenant) {
        if (etat.estFinal() || maintenant.isAfter(expireLe)) {
            throw new NegociationFermee();
        }
    }

    /** La contrepartie soumet ses propositions du tour : la main passe à l'organisation. */
    public void recevoirPropositions(int nombre) {
        if (nombre <= 0) {
            throw new IllegalArgumentException("Proposez au moins une modification, ou acceptez la version courante.");
        }
        exigerEtat(EtatNegociation.ATTENTE_CONTREPARTIE);
        etat = etat.vers(EtatNegociation.ATTENTE_INTERNE);
    }

    /** La contrepartie accepte la version courante telle quelle : l'entente est atteinte. */
    public void accepterVersion() {
        exigerEtat(EtatNegociation.ATTENTE_CONTREPARTIE);
        etat = etat.vers(EtatNegociation.ENTENTE);
    }

    /** L'organisation a décidé chaque proposition du tour : nouvelle version ; entente si tout est accepté. */
    public int conclureTour(boolean toutAccepte) {
        exigerEtat(EtatNegociation.ATTENTE_INTERNE);
        etat = etat.vers(toutAccepte ? EtatNegociation.ENTENTE : EtatNegociation.ATTENTE_CONTREPARTIE);
        versionCourante++;
        tour++;
        return versionCourante;
    }

    /** Clôture sur l'entente : la version courante devient le contrat de la trousse. */
    public void clore(String par, Instant le) {
        etat = etat.vers(EtatNegociation.CLOSE);
        terminer(par, null, le);
    }

    public void annuler(String par, String motif, Instant le) {
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Motif de l'annulation requis.");
        }
        etat = etat.vers(EtatNegociation.ANNULEE);
        terminer(par, motif.strip(), le);
    }

    public boolean estOuverte() {
        return !etat.estFinal();
    }

    private void terminer(String par, String motif, Instant le) {
        this.termineePar = par;
        this.motifFin = motif;
        this.termineeLe = le;
    }

    private void exigerEtat(EtatNegociation attendu) {
        if (etat != attendu) {
            throw new EtatNegociation.TransitionInterdite(etat, attendu);
        }
    }

    /** Une trousse ne négocie qu'avec une contrepartie à la fois : un nouveau cycle attend la fin du précédent. */
    public static void exigerAucuneOuverte(java.util.List<Negociation> existantes) {
        if (existantes.stream().anyMatch(Negociation::estOuverte)) {
            throw new DejaEnCours();
        }
    }

    /** Refus typé : une négociation est déjà ouverte pour la trousse. */
    public static final class DejaEnCours extends IllegalStateException {
        DejaEnCours() {
            super("Une négociation est déjà en cours pour cette trousse : clôturez-la ou annulez-la d'abord.");
        }
    }

    /** Refus typé : le lien de négociation ne permet plus aucun échange. */
    public static final class NegociationFermee extends IllegalStateException {
        NegociationFermee() {
            super("Cette négociation est terminée ou son lien a expiré.");
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getTrousseId() { return trousseId; }
    public String getContrepartieNom() { return contrepartieNom; }
    public String getContrepartieCourriel() { return contrepartieCourriel; }
    public String getContrepartieEntreprise() { return contrepartieEntreprise; }
    public Langue getLangue() { return langue; }
    public String getJetonHash() { return jetonHash; }
    public Instant getExpireLe() { return expireLe; }
    public EtatNegociation getEtat() { return etat; }
    public int getVersionCourante() { return versionCourante; }
    public int getTour() { return tour; }
    public String getOuvertePar() { return ouvertePar; }
    public Instant getOuverteLe() { return ouverteLe; }
    public String getTermineePar() { return termineePar; }
    public Instant getTermineeLe() { return termineeLe; }
    public String getMotifFin() { return motifFin; }
}
