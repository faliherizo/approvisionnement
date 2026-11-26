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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Diffusion Microsoft 365 des trousses terminées (Annexe 10, TI-01) : bibliothèque SharePoint ou OneDrive où déposer le
 * document signé et son dossier de preuve, et avis dans le canal Teams de l'organisation. Réglée par l'administration
 * technique ; la connexion Microsoft 365 elle-même reste celle de la plateforme (ADR-002), simulée tant qu'elle n'est pas
 * configurée et activée.
 */
@Entity
@Table(name = "clm_diffusion_m365")
public class DiffusionM365 {

    /** Bibliothèque de dépôt : site SharePoint de l'organisation ou OneDrive désigné. */
    public enum Bibliotheque { SHAREPOINT, ONEDRIVE }

    public static final int DOSSIER_MAX = 300;
    public static final String DOSSIER_PAR_DEFAUT = "Contrats signés";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(nullable = false)
    private boolean actif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Bibliotheque bibliotheque;

    @Column(nullable = false, length = DOSSIER_MAX)
    private String dossier;

    @Column(name = "deposer_preuve", nullable = false)
    private boolean deposerPreuve;

    @Column(name = "avis_teams", nullable = false)
    private boolean avisTeams;

    @Column(name = "modifie_par", nullable = false, length = 150)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    @Version
    private long version;

    protected DiffusionM365() {
    }

    public DiffusionM365(Long tenantId) {
        this.tenantId = tenantId;
    }

    /** Réglage préalablement validé par {@link Reglage#erreurs()}. */
    public void appliquer(Reglage r, String par, Instant le) {
        actif = r.actif();
        bibliotheque = r.bibliotheque();
        dossier = r.dossier();
        deposerPreuve = r.deposerPreuve();
        avisTeams = r.avisTeams();
        modifiePar = par;
        modifieLe = le;
    }

    public Reglage reglage() {
        return new Reglage(actif, bibliotheque, dossier, deposerPreuve, avisTeams);
    }

    public Long getTenantId() { return tenantId; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }

    /**
     * Réglage de diffusion. Le dossier est relatif à la bibliothèque, séparé par « / » ; la traversée (« .. ») est refusée
     * quel que soit l'adaptateur qui déposera, comme pour la publication du module de rédaction.
     */
    public record Reglage(boolean actif, Bibliotheque bibliotheque, String dossier, boolean deposerPreuve, boolean avisTeams) {

        public static final Reglage INACTIF = new Reglage(false, Bibliotheque.SHAREPOINT, DOSSIER_PAR_DEFAUT, true, true);

        public List<String> erreurs() {
            List<String> erreurs = new ArrayList<>();
            if (bibliotheque == null) {
                erreurs.add("Bibliothèque de dépôt requise : SHAREPOINT ou ONEDRIVE.");
            }
            if (dossier == null || dossier.isBlank() || dossier.length() > DOSSIER_MAX) {
                erreurs.add("Dossier de dépôt : 1 à " + DOSSIER_MAX + " caractères.");
            } else if (traverse(dossier)) {
                erreurs.add("Dossier de dépôt : « .. » et « \\ » sont interdits.");
            }
            return erreurs;
        }

        private static boolean traverse(String dossier) {
            if (dossier.contains("\\")) {
                return true;
            }
            for (String segment : dossier.split("/")) {
                if (segment.strip().equals("..")) {
                    return true;
                }
            }
            return false;
        }
    }
}
