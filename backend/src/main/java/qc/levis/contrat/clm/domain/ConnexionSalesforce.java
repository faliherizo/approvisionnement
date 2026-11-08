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
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Connexion Salesforce propre à une organisation (UI-21). Le secret n'y figure que chiffré et ne ressort jamais vers
 * l'interface. Les noms d'objet et de champs entrent dans les requêtes SOQL et les URL : ils sont restreints à la forme
 * d'un nom d'API Salesforce.
 */
@Entity
@Table(name = "clm_connexion_salesforce")
public class ConnexionSalesforce {

    public enum Mode { SIMULATION, REST }

    public static final int REPRISES_PLAFOND = 20;
    private static final Pattern NOM_API = Pattern.compile("[A-Za-z]\\w{0,79}");
    private static final Pattern VERSION_API = Pattern.compile("v\\d{1,3}\\.\\d");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mode mode;

    @Column(name = "instance_url", length = 500)
    private String instanceUrl;

    @Column(name = "token_url", length = 500)
    private String tokenUrl;

    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "secret_chiffre", length = 1000)
    private String secretChiffre;

    @Column(nullable = false, length = 80)
    private String objet;

    @Column(name = "version_api", nullable = false, length = 10)
    private String versionApi;

    @Column(name = "champ_statut", nullable = false, length = 80)
    private String champStatut;

    @Column(name = "champ_reference", nullable = false, length = 80)
    private String champReference;

    @Column(name = "champ_correlation", nullable = false, length = 80)
    private String champCorrelation;

    @Column(name = "reprises_max", nullable = false)
    private int reprisesMax;

    @Column(name = "modifie_par", nullable = false, length = 150)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    @Version
    private long version;

    protected ConnexionSalesforce() {
    }

    public ConnexionSalesforce(Long tenantId) {
        this.tenantId = tenantId;
    }

    /** Réglages préalablement validés ; un secret chiffré nul conserve le secret en place (rotation facultative). */
    public void appliquer(Reglages r, String nouveauSecretChiffre, String par, Instant le) {
        mode = r.mode();
        instanceUrl = r.instanceUrl();
        tokenUrl = r.tokenUrl();
        clientId = r.clientId();
        if (nouveauSecretChiffre != null) {
            secretChiffre = nouveauSecretChiffre;
        }
        objet = r.objet();
        versionApi = r.versionApi();
        champStatut = r.champs().statut();
        champReference = r.champs().reference();
        champCorrelation = r.champs().correlation();
        reprisesMax = r.reprisesMax();
        modifiePar = par;
        modifieLe = le;
    }

    public Reglages reglages() {
        return new Reglages(mode, instanceUrl, tokenUrl, clientId, objet, versionApi,
                new ChampsRetour(champStatut, champReference, champCorrelation), reprisesMax);
    }

    public boolean secretConfigure() {
        return secretChiffre != null && !secretChiffre.isBlank();
    }

    public Long getTenantId() { return tenantId; }
    public String getSecretChiffre() { return secretChiffre; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }

    /** Champs de la demande Salesforce qui reçoivent le retour de statut d'une trousse (ST-12). */
    public record ChampsRetour(String statut, String reference, String correlation) {
    }

    public record Reglages(Mode mode, String instanceUrl, String tokenUrl, String clientId, String objet, String versionApi,
                           ChampsRetour champs, int reprisesMax) {

        /**
         * @param hotesAutorises domaines que la plateforme permet de joindre (protection contre la falsification de requêtes
         *                       côté serveur) ; une URL doit être en https et viser l'un d'eux ou l'un de leurs sous-domaines
         * @param secretDisponible un secret est fourni ou déjà conservé
         */
        public List<String> erreurs(List<String> hotesAutorises, boolean secretDisponible) {
            List<String> erreurs = new ArrayList<>();
            if (mode == null) {
                erreurs.add("Mode d'intégration requis : SIMULATION ou REST.");
                return erreurs;
            }
            nomApi(objet, "Objet Salesforce", erreurs);
            if (versionApi == null || !VERSION_API.matcher(versionApi).matches()) {
                erreurs.add("Version de l'API : forme vNN.N (ex. v60.0).");
            }
            if (champs == null) {
                erreurs.add("Champs de retour de statut requis.");
            } else {
                nomApi(champs.statut(), "Champ du statut", erreurs);
                nomApi(champs.reference(), "Champ de la référence", erreurs);
                nomApi(champs.correlation(), "Champ de la corrélation", erreurs);
            }
            if (reprisesMax < 1 || reprisesMax > REPRISES_PLAFOND) {
                erreurs.add("Reprises avant abandon : entre 1 et " + REPRISES_PLAFOND + ".");
            }
            if (mode == Mode.REST) {
                List<String> domaines = domaines(hotesAutorises);
                if (!hoteAutorise(instanceUrl, domaines)) {
                    erreurs.add("Instance Salesforce : URL https d'un domaine autorisé (" + String.join(", ", domaines) + ").");
                }
                if (!hoteAutorise(tokenUrl, domaines)) {
                    erreurs.add("Serveur de jetons : URL https d'un domaine autorisé (" + String.join(", ", domaines) + ").");
                }
                if (clientId == null || clientId.isBlank() || clientId.length() > 255) {
                    erreurs.add("Identifiant client requis (255 caractères au plus).");
                }
                if (!secretDisponible) {
                    erreurs.add("Secret client requis en mode REST.");
                }
            }
            return erreurs;
        }

        private static void nomApi(String valeur, String champ, List<String> erreurs) {
            if (valeur == null || !NOM_API.matcher(valeur).matches()) {
                erreurs.add(champ + " : nom d'API Salesforce (lettres, chiffres, _).");
            }
        }

        /** Domaines tels que configurés, sans casse, point initial ni entrée vide. */
        static List<String> domaines(List<String> hotesAutorises) {
            return hotesAutorises.stream().map(a -> a.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\.", ""))
                    .filter(a -> !a.isEmpty()).toList();
        }

        static boolean hoteAutorise(String url, List<String> hotesAutorises) {
            if (url == null || url.isBlank()) {
                return false;
            }
            try {
                URI u = new URI(url.trim());
                String hote = u.getHost();
                if (!"https".equalsIgnoreCase(u.getScheme()) || hote == null || u.getUserInfo() != null) {
                    return false;
                }
                String h = hote.toLowerCase(Locale.ROOT);
                return domaines(hotesAutorises).stream().anyMatch(a -> h.equals(a) || h.endsWith("." + a));
            } catch (URISyntaxException e) {
                return false;
            }
        }
    }
}
