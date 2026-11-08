package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

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

/**
 * Tâche ad hoc confiée à une personne de l'organisation (« préparer l'annexe financière », « relire la clause 7 »),
 * rattachée ou non à une trousse. Elle complète la boîte de tâches : les étapes de parcours naissent du flux, celles-ci
 * sont créées à la main. Seuls le créateur et l'assigné agissent dessus ; le créateur seul l'annule.
 */
@Entity
@Table(name = "clm_tache_assignee", indexes = {
        @Index(name = "ix_clm_tache_assigne", columnList = "tenant_id,assigne_a,statut"),
        @Index(name = "ix_clm_tache_createur", columnList = "tenant_id,cree_par"),
        @Index(name = "ix_clm_tache_trousse", columnList = "tenant_id,trousse_id")})
public class TacheAssignee {

    public enum Statut { A_FAIRE, EN_COURS, TERMINEE, ANNULEE }

    public static final int TITRE_MAX = 200;
    public static final int DESCRIPTION_MAX = 2000;
    private static final String COURRIEL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "trousse_id")
    private Long trousseId;

    @Column(nullable = false, length = TITRE_MAX)
    private String titre;

    @Column(length = DESCRIPTION_MAX)
    private String description;

    @Column(name = "assigne_a", nullable = false, length = 150)
    private String assigneA;

    @Column(length = 60)
    private String priorite;

    private LocalDate echeance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    @Column(name = "cree_par", nullable = false, length = 150)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    @Column(name = "terminee_le")
    private Instant termineeLe;

    @Version
    private long version;

    protected TacheAssignee() {
    }

    /** Fiche d'une tâche à créer : la priorité est un code déjà validé contre la liste de l'organisation. */
    public record Saisie(Long trousseId, String titre, String description, String assigneA, String priorite, LocalDate echeance) {
    }

    public static TacheAssignee creer(Long tenantId, Saisie s, String createur, Instant maintenant) {
        TacheAssignee t = new TacheAssignee();
        t.tenantId = tenantId;
        t.trousseId = s.trousseId();
        t.titre = titre(s.titre());
        t.description = description(s.description());
        t.assigneA = courriel(s.assigneA());
        t.priorite = s.priorite();
        t.echeance = s.echeance();
        t.statut = Statut.A_FAIRE;
        t.creePar = createur;
        t.creeLe = maintenant;
        t.modifieLe = maintenant;
        return t;
    }

    public boolean estAssigneeA(String acteur) {
        return assigneA.equalsIgnoreCase(acteur);
    }

    public boolean estCreeePar(String acteur) {
        return creePar.equalsIgnoreCase(acteur);
    }

    /** Le créateur et l'assigné sont les deux seuls intervenants d'une tâche. */
    public boolean concerne(String acteur) {
        return estAssigneeA(acteur) || estCreeePar(acteur);
    }

    public boolean estOuverte() {
        return statut == Statut.A_FAIRE || statut == Statut.EN_COURS;
    }

    public boolean enRetard(LocalDate aujourdhui) {
        return estOuverte() && echeance != null && echeance.isBefore(aujourdhui);
    }

    /** L'assigné signale qu'il a commencé : le créateur sait que la tâche est prise en charge. */
    public boolean peutDemarrer(String acteur) {
        return estAssigneeA(acteur);
    }

    /** Annuler retire une demande : seul celui qui l'a formulée en décide. */
    public boolean peutAnnuler(String acteur) {
        return estCreeePar(acteur);
    }

    public void demarrer(Instant maintenant) {
        if (statut != Statut.A_FAIRE) {
            throw new IllegalStateException("Tâche déjà démarrée ou close.");
        }
        changer(Statut.EN_COURS, maintenant);
    }

    public void terminer(Instant maintenant) {
        exigerOuverte();
        changer(Statut.TERMINEE, maintenant);
        termineeLe = maintenant;
    }

    public void annuler(Instant maintenant) {
        exigerOuverte();
        changer(Statut.ANNULEE, maintenant);
    }

    /** Une tâche terminée à tort reprend son cours ; une tâche annulée reste close pour garder la décision lisible. */
    public void rouvrir(Instant maintenant) {
        if (statut != Statut.TERMINEE) {
            throw new IllegalStateException("Seule une tâche terminée peut être rouverte.");
        }
        changer(Statut.A_FAIRE, maintenant);
        termineeLe = null;
    }

    public void reassigner(String vers, Instant maintenant) {
        exigerOuverte();
        String cible = courriel(vers);
        if (estAssigneeA(cible)) {
            throw new IllegalArgumentException("La tâche est déjà assignée à " + cible + ".");
        }
        assigneA = cible;
        changer(Statut.A_FAIRE, maintenant);
    }

    private void exigerOuverte() {
        if (!estOuverte()) {
            throw new IllegalStateException("Tâche close : " + titre);
        }
    }

    private void changer(Statut nouveau, Instant maintenant) {
        statut = nouveau;
        modifieLe = maintenant;
    }

    private static String titre(String saisie) {
        String t = saisie == null ? "" : saisie.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Titre de la tâche requis.");
        }
        if (t.length() > TITRE_MAX) {
            throw new IllegalArgumentException("Titre trop long (" + TITRE_MAX + " caractères au plus).");
        }
        return t;
    }

    private static String description(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return null;
        }
        String d = saisie.trim();
        if (d.length() > DESCRIPTION_MAX) {
            throw new IllegalArgumentException("Description trop longue (" + DESCRIPTION_MAX + " caractères au plus).");
        }
        return d;
    }

    private static String courriel(String saisie) {
        String c = saisie == null ? "" : saisie.trim().toLowerCase(Locale.ROOT);
        if (!c.matches(COURRIEL)) {
            throw new IllegalArgumentException("Personne assignée invalide : courriel attendu.");
        }
        return c;
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getTrousseId() {
        return trousseId;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public String getAssigneA() {
        return assigneA;
    }

    public String getPriorite() {
        return priorite;
    }

    public LocalDate getEcheance() {
        return echeance;
    }

    public Statut getStatut() {
        return statut;
    }

    public String getCreePar() {
        return creePar;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public Instant getTermineeLe() {
        return termineeLe;
    }
}
