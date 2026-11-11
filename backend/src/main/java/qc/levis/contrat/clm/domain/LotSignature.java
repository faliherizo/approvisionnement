package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Signature en lot (addenda 1, AF-47) : un même modèle et un même parcours appliqués à plusieurs dossiers, chacun dans
 * sa propre trousse. Initié depuis Salesforce (client d'intégration) ou depuis la plateforme (utilisateur). Un élément
 * en échec n'empêche pas les autres : le lot en garde le motif.
 */
@Entity
@Table(name = "clm_lot")
public class LotSignature {

    public enum Statut { EN_COURS, TERMINE, TERMINE_AVEC_ECHECS }

    /** Plafond par lot : au-delà, l'appelant découpe ; borne la durée d'une requête synchrone. */
    public static final int TAILLE_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(name = "modele_id")
    private Long modeleId;

    @Column(name = "client_integration_id")
    private Long clientIntegrationId;

    @Column(name = "cree_par", nullable = false, length = 200)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int reussis;

    @Column(nullable = false)
    private int echecs;

    @Column(nullable = false)
    private boolean lancer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Statut statut = Statut.EN_COURS;

    protected LotSignature() {
    }

    public static LotSignature ouvrir(Long tenantId, String libelle, Long modeleId, Long clientIntegrationId, int total,
                                      boolean lancer, String par, Instant le) {
        if (total < 1 || total > TAILLE_MAX) {
            throw new IllegalArgumentException("Un lot compte de 1 à " + TAILLE_MAX + " éléments.");
        }
        String l = libelle == null ? "" : libelle.strip();
        LotSignature lot = new LotSignature();
        lot.tenantId = tenantId;
        lot.libelle = l.isEmpty() ? "Lot du " + le.toString().substring(0, 10) : l.substring(0, Math.min(l.length(), 200));
        lot.modeleId = modeleId;
        lot.clientIntegrationId = clientIntegrationId;
        lot.total = total;
        lot.lancer = lancer;
        lot.creePar = par;
        lot.creeLe = le;
        return lot;
    }

    public void noterReussite() {
        reussis++;
        cloreSiComplet();
    }

    public void noterEchec() {
        echecs++;
        cloreSiComplet();
    }

    private void cloreSiComplet() {
        if (reussis + echecs > total) {
            throw new IllegalStateException("Le lot a déjà traité tous ses éléments.");
        }
        if (reussis + echecs == total) {
            statut = echecs == 0 ? Statut.TERMINE : Statut.TERMINE_AVEC_ECHECS;
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getLibelle() { return libelle; }
    public Long getModeleId() { return modeleId; }
    public Long getClientIntegrationId() { return clientIntegrationId; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
    public int getTotal() { return total; }
    public int getReussis() { return reussis; }
    public int getEchecs() { return echecs; }
    public boolean isLancer() { return lancer; }
    public Statut getStatut() { return statut; }
}
