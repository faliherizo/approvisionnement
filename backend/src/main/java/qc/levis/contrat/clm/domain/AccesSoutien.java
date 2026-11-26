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
import java.time.Duration;
import java.time.Instant;

/**
 * Accès de soutien de l'exploitant aux données d'une organisation (Annexe 10, SEC-17 ; ADR-031). L'exploitant le demande
 * avec un motif ; l'administration d'affaires de l'organisation l'accorde ou le refuse ; l'accès accordé expire de lui-même
 * et se révoque à tout moment. Il n'ouvre qu'une consultation de diagnostic, chacune journalisée et visible de
 * l'organisation.
 */
@Entity
@Table(name = "clm_acces_soutien", indexes = @Index(name = "ix_clm_acces_soutien_tenant", columnList = "tenant_id,demande_le"))
public class AccesSoutien {

    public enum Statut { DEMANDE, ACCORDE, REFUSE, REVOQUE, EXPIRE }

    public static final int DUREE_MAX_HEURES = 8;
    public static final int MOTIF_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 150)
    private String demandeur;

    @Column(nullable = false, length = MOTIF_MAX)
    private String motif;

    @Column(name = "duree_heures", nullable = false)
    private int dureeHeures;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Statut statut = Statut.DEMANDE;

    @Column(name = "demande_le", nullable = false)
    private Instant demandeLe;

    @Column(name = "decide_par", length = 150)
    private String decidePar;

    @Column(name = "decide_le")
    private Instant decideLe;

    @Column(name = "expire_le")
    private Instant expireLe;

    @Column(nullable = false)
    private int consultations;

    protected AccesSoutien() {
    }

    public static AccesSoutien demander(Long tenantId, String demandeur, String motif, int dureeHeures, Instant le) {
        String m = motif == null ? "" : motif.strip();
        if (m.isEmpty() || m.length() > MOTIF_MAX) {
            throw new IllegalArgumentException("Motif de l'accès de soutien : 1 à " + MOTIF_MAX + " caractères (billet, symptôme).");
        }
        if (dureeHeures < 1 || dureeHeures > DUREE_MAX_HEURES) {
            throw new IllegalArgumentException("Durée de l'accès de soutien : de 1 à " + DUREE_MAX_HEURES + " heures.");
        }
        AccesSoutien a = new AccesSoutien();
        a.tenantId = tenantId;
        a.demandeur = demandeur;
        a.motif = m;
        a.dureeHeures = dureeHeures;
        a.demandeLe = le;
        return a;
    }

    /** L'accord fixe l'échéance : la durée demandée court à partir de la décision, pas de la demande. */
    public void accorder(String par, Instant le) {
        exigerStatut(Statut.DEMANDE, "Seule une demande en attente s'accorde.");
        decider(Statut.ACCORDE, par, le);
        expireLe = le.plus(Duration.ofHours(dureeHeures));
    }

    public void refuser(String par, Instant le) {
        exigerStatut(Statut.DEMANDE, "Seule une demande en attente se refuse.");
        decider(Statut.REFUSE, par, le);
    }

    /** Fin anticipée par l'organisation ; un accès expiré ou jamais accordé n'a rien à révoquer. */
    public void revoquer(String par, Instant le) {
        if (!actif(le)) {
            throw new IllegalStateException("Aucun accès de soutien en cours à révoquer.");
        }
        decider(Statut.REVOQUE, par, le);
        expireLe = le;
    }

    public boolean actif(Instant maintenant) {
        return statut == Statut.ACCORDE && maintenant.isBefore(expireLe);
    }

    /** Statut lu par l'organisation : un accès accordé dont l'échéance est passée est expiré. */
    public Statut statut(Instant maintenant) {
        return statut == Statut.ACCORDE && !actif(maintenant) ? Statut.EXPIRE : statut;
    }

    public void consulter() {
        consultations++;
    }

    private void exigerStatut(Statut attendu, String message) {
        if (statut != attendu) {
            throw new IllegalStateException(message);
        }
    }

    private void decider(Statut cible, String par, Instant le) {
        statut = cible;
        decidePar = par;
        decideLe = le;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getDemandeur() { return demandeur; }
    public String getMotif() { return motif; }
    public int getDureeHeures() { return dureeHeures; }
    public Instant getDemandeLe() { return demandeLe; }
    public String getDecidePar() { return decidePar; }
    public Instant getDecideLe() { return decideLe; }
    public Instant getExpireLe() { return expireLe; }
    public int getConsultations() { return consultations; }
}
