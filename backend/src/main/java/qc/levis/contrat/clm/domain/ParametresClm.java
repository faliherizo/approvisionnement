package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Paramètres du module propres à une organisation (UI-22) : ils remplacent ceux de la plateforme tant qu'elle les
 * conserve. Les bornes sont vérifiées ici, quel que soit l'écran ou l'appel qui porte la modification.
 */
@Entity
@Table(name = "clm_parametres")
public class ParametresClm {

    public static final long UN_MO = 1024L * 1024;
    public static final long TAILLE_MIN_OCTETS = 100L * 1024;
    public static final int VALIDITE_MAX_JOURS = 90;
    public static final String PREFIXE_PAR_DEFAUT = "TR";
    public static final String CHAMP_MONTANT_PAR_DEFAUT = "montant";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "taille_max_octets", nullable = false)
    private long tailleMaxOctets;

    @Column(name = "invitation_validite_jours", nullable = false)
    private int invitationValiditeJours;

    @Column(name = "portail_url", nullable = false, length = 500)
    private String portailUrl;

    @Column(name = "prefixe_reference", nullable = false, length = 10)
    private String prefixeReference;

    @Column(name = "rappel_frequence_jours", nullable = false)
    private int rappelFrequenceJours;

    @Column(name = "rappels_max", nullable = false)
    private int rappelsMax;

    @Column(name = "preavis_echeance_jours", nullable = false)
    private int preavisEcheanceJours = Relances.PREAVIS_PAR_DEFAUT_JOURS;

    @Enumerated(EnumType.STRING)
    @Column(name = "externe_faible", nullable = false, length = 20)
    private Identite.Niveau externeFaible;

    @Enumerated(EnumType.STRING)
    @Column(name = "externe_moyen", nullable = false, length = 20)
    private Identite.Niveau externeMoyen;

    @Enumerated(EnumType.STRING)
    @Column(name = "externe_eleve", nullable = false, length = 20)
    private Identite.Niveau externeEleve;

    @Enumerated(EnumType.STRING)
    @Column(name = "interne_faible", nullable = false, length = 20)
    private Identite.Niveau interneFaible;

    @Enumerated(EnumType.STRING)
    @Column(name = "interne_moyen", nullable = false, length = 20)
    private Identite.Niveau interneMoyen;

    @Enumerated(EnumType.STRING)
    @Column(name = "interne_eleve", nullable = false, length = 20)
    private Identite.Niveau interneEleve;

    @Column(name = "tentatives_identite", nullable = false)
    private int tentativesIdentite;

    @Column(name = "code_validite_minutes", nullable = false)
    private int codeValiditeMinutes;

    @Column(name = "champ_montant", nullable = false, length = 64)
    private String champMontant;

    @Column(name = "modifie_par", nullable = false, length = 150)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    @Version
    private long version;

    protected ParametresClm() {
    }

    public ParametresClm(Long tenantId) {
        this.tenantId = tenantId;
    }

    /** Réglages complets, préalablement validés par {@link Reglages#erreurs(long)}. */
    public void appliquer(Reglages r, String par, Instant le) {
        tailleMaxOctets = r.tailleMaxOctets();
        invitationValiditeJours = r.invitationValiditeJours();
        portailUrl = r.portailUrl();
        prefixeReference = r.prefixeReference();
        rappelFrequenceJours = r.rappelFrequenceJours();
        rappelsMax = r.rappelsMax();
        preavisEcheanceJours = r.preavisEcheanceJours();
        Identite.Politique p = r.identite();
        externeFaible = p.minimumRequis(Identite.Risque.FAIBLE, true);
        externeMoyen = p.minimumRequis(Identite.Risque.MOYEN, true);
        externeEleve = p.minimumRequis(Identite.Risque.ELEVE, true);
        interneFaible = p.minimumRequis(Identite.Risque.FAIBLE, false);
        interneMoyen = p.minimumRequis(Identite.Risque.MOYEN, false);
        interneEleve = p.minimumRequis(Identite.Risque.ELEVE, false);
        tentativesIdentite = p.tentativesMax();
        codeValiditeMinutes = p.codeValiditeMinutes();
        champMontant = r.champMontant();
        modifiePar = par;
        modifieLe = le;
    }

    public Reglages reglages() {
        Identite.Politique identite = new Identite.Politique(
                Map.of(Identite.Risque.FAIBLE, externeFaible, Identite.Risque.MOYEN, externeMoyen, Identite.Risque.ELEVE, externeEleve),
                Map.of(Identite.Risque.FAIBLE, interneFaible, Identite.Risque.MOYEN, interneMoyen, Identite.Risque.ELEVE, interneEleve),
                tentativesIdentite, codeValiditeMinutes);
        return new Reglages(tailleMaxOctets, invitationValiditeJours, portailUrl, prefixeReference, rappelFrequenceJours, rappelsMax,
                preavisEcheanceJours, identite, champMontant);
    }

    public Long getTenantId() { return tenantId; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }

    /** Valeurs d'un jeu de paramètres, qu'il vienne de la plateforme ou d'une organisation. */
    public record Reglages(long tailleMaxOctets, int invitationValiditeJours, String portailUrl, String prefixeReference,
                           int rappelFrequenceJours, int rappelsMax, int preavisEcheanceJours, Identite.Politique identite,
                           String champMontant) {

        /** Bornes de chaque paramètre ; {@code plafondOctets} : taille qu'accepte le transport des fichiers de la plateforme. */
        public List<String> erreurs(long plafondOctets) {
            List<String> erreurs = new ArrayList<>();
            if (tailleMaxOctets < TAILLE_MIN_OCTETS || tailleMaxOctets > plafondOctets) {
                erreurs.add("Taille maximale d'un document : entre 100 Ko et " + plafondOctets / UN_MO + " Mo.");
            }
            if (invitationValiditeJours < 1 || invitationValiditeJours > VALIDITE_MAX_JOURS) {
                erreurs.add("Validité d'une invitation : entre 1 et " + VALIDITE_MAX_JOURS + " jours.");
            }
            if (!adresseWeb(portailUrl)) {
                erreurs.add("Adresse du portail des signataires : URL http ou https complète requise.");
            }
            if (prefixeReference == null || !prefixeReference.matches("[A-Z][A-Z0-9]{0,9}")) {
                erreurs.add("Préfixe des références : 1 à 10 majuscules ou chiffres, en commençant par une lettre.");
            }
            if (rappelFrequenceJours < 1 || rappelFrequenceJours > Relances.FREQUENCE_MAX_JOURS) {
                erreurs.add("Fréquence de rappel par défaut : entre 1 et " + Relances.FREQUENCE_MAX_JOURS + " jours.");
            }
            if (rappelsMax < 0 || rappelsMax > Relances.RAPPELS_PLAFOND) {
                erreurs.add("Nombre de rappels par défaut : entre 0 et " + Relances.RAPPELS_PLAFOND + ".");
            }
            if (preavisEcheanceJours < 0 || preavisEcheanceJours > Relances.PREAVIS_MAX_JOURS) {
                erreurs.add("Préavis d'échéance : entre 0 (aucune alerte) et " + Relances.PREAVIS_MAX_JOURS + " jours.");
            }
            erreurs.addAll(identite == null ? List.of("Politique d'identité requise.") : identite.erreurs());
            if (champMontant == null || !champMontant.matches("[A-Za-z]\\w{0,63}")) {
                erreurs.add("Champ du montant (option IA) : clé d'un champ de modèle (lettres, chiffres, _).");
            }
            return erreurs;
        }

        static boolean adresseWeb(String url) {
            if (url == null || url.isBlank()) {
                return false;
            }
            try {
                URI u = new URI(url.trim());
                return ("https".equalsIgnoreCase(u.getScheme()) || "http".equalsIgnoreCase(u.getScheme())) && u.getHost() != null;
            } catch (URISyntaxException e) {
                return false;
            }
        }
    }
}
