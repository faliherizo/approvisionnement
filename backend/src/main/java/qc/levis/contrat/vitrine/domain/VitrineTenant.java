package qc.levis.contrat.vitrine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Site vitrine d'une organisation (https://<organisation>.contrat.vinaitsolution.com/accueil) : titre, accroche,
 * fonctionnalités présentées et vidéo de présentation, administrés par le superadministrateur de la plateforme.
 * Les fonctionnalités sont tenues en texte (une par ligne, « Titre :: Texte ») pour rester lisibles en base.
 */
@Entity
@Table(name = "vitrine_tenant")
public class VitrineTenant {

    public static final int TITRE_MAX = 160;
    public static final int ACCROCHE_MAX = 600;
    public static final int FONCTIONNALITES_MAX = 24;
    public static final int FONCTIONNALITE_MAX = 400;
    /** Chemin dans la médiathèque : segments en minuscules, chiffres, tirets, soulignés et points, sans remontée. */
    private static final Pattern CHEMIN = Pattern.compile("[a-z0-9_-]+(/[a-z0-9_.-]+)*");
    private static final String SEPARATEUR = " :: ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(nullable = false, length = TITRE_MAX)
    private String titre;

    @Column(length = ACCROCHE_MAX)
    private String accroche;

    @Column(name = "fonctionnalites", length = 20_000)
    private String fonctionnalites = "";

    @Column(length = 255)
    private String video;

    @Column(name = "sous_titres", length = 255)
    private String sousTitres;

    @Column(length = 255)
    private String affiche;

    private boolean publiee;

    @Column(name = "maj_par", length = 200)
    private String majPar;

    @Column(name = "maj_le")
    private Instant majLe;

    protected VitrineTenant() {
    }

    public static VitrineTenant nouvelle(Long tenantId, String titre) {
        VitrineTenant v = new VitrineTenant();
        v.tenantId = tenantId;
        v.titre = texte(titre, TITRE_MAX, "Titre");
        return v;
    }

    /** Une fonctionnalité présentée : un titre court et sa description. */
    public record Fonctionnalite(String titre, String texte) {
    }

    /** Contenu complet remplacé d'un coup, comme l'éditeur du superadministrateur l'envoie. */
    public record Contenu(String titre, String accroche, List<Fonctionnalite> fonctionnalites, String video, String sousTitres,
                          String affiche, boolean publiee) {
    }

    public void modifier(Contenu c, String par, Instant le) {
        List<Fonctionnalite> f = c.fonctionnalites() == null ? List.of() : c.fonctionnalites();
        if (f.size() > FONCTIONNALITES_MAX) {
            throw new IllegalArgumentException("Au plus " + FONCTIONNALITES_MAX + " fonctionnalités.");
        }
        this.titre = texte(c.titre(), TITRE_MAX, "Titre");
        this.accroche = facultatif(c.accroche(), ACCROCHE_MAX, "Accroche");
        this.fonctionnalites = String.join("\n", f.stream().map(VitrineTenant::ligne).toList());
        this.video = chemin(c.video());
        this.sousTitres = chemin(c.sousTitres());
        this.affiche = chemin(c.affiche());
        this.publiee = c.publiee();
        this.majPar = par;
        this.majLe = le;
    }

    public List<Fonctionnalite> getFonctionnalites() {
        if (fonctionnalites == null || fonctionnalites.isBlank()) {
            return List.of();
        }
        return fonctionnalites.lines().map(l -> {
            int i = l.indexOf(SEPARATEUR);
            return i < 0 ? new Fonctionnalite(l, "") : new Fonctionnalite(l.substring(0, i), l.substring(i + SEPARATEUR.length()));
        }).toList();
    }

    private static String ligne(Fonctionnalite f) {
        String t = texte(f.titre(), 120, "Titre de fonctionnalité").replace(SEPARATEUR, " : ");
        String d = facultatif(f.texte(), FONCTIONNALITE_MAX, "Description de fonctionnalité");
        return (t + (d == null ? "" : SEPARATEUR + d.replace(SEPARATEUR, " : "))).replace('\n', ' ');
    }

    private static String texte(String v, int max, String quoi) {
        String t = facultatif(v, max, quoi);
        if (t == null) {
            throw new IllegalArgumentException(quoi + " obligatoire.");
        }
        return t;
    }

    private static String facultatif(String v, int max, String quoi) {
        String t = v == null ? "" : v.strip();
        if (t.length() > max) {
            throw new IllegalArgumentException(quoi + " : " + max + " caractères au plus.");
        }
        return t.isEmpty() ? null : t;
    }

    /** Chemin de média relatif à la médiathèque ; vide = aucun. Toute remontée (« .. ») ou chemin absolu est refusé. */
    public static String chemin(String v) {
        String c = v == null ? "" : v.strip();
        if (c.isEmpty()) {
            return null;
        }
        if (c.length() > 255 || c.contains("..") || !CHEMIN.matcher(c).matches()) {
            throw new IllegalArgumentException("Chemin de média invalide : " + c);
        }
        return c;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getTitre() { return titre; }
    public String getAccroche() { return accroche; }
    public String getVideo() { return video; }
    public String getSousTitres() { return sousTitres; }
    public String getAffiche() { return affiche; }
    public boolean isPubliee() { return publiee; }
    public String getMajPar() { return majPar; }
    public Instant getMajLe() { return majLe; }
}
