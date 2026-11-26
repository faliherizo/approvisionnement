package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/**
 * Trousse (ST-03) : l'unité de travail qui regroupe documents, participants, paramètres de parcours et état, et
 * qui garde la corrélation avec le dossier source Salesforce.
 */
@Entity
@Table(name = "clm_trousse", uniqueConstraints = @UniqueConstraint(name = "uk_clm_trousse_ref", columnNames = {"tenant_id", "reference"}))
public class Trousse {

    public enum Source { MANUELLE, SALESFORCE, WORKDAY, INTEGRATION }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 60)
    private String reference;

    @Column(nullable = false, length = 250)
    private String titre;

    @Column(name = "type_contrat", length = 80)
    private String typeContrat;

    @Column(length = 120)
    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Source source = Source.MANUELLE;

    @Column(name = "sf_dossier_id", length = 80)
    private String sfDossierId;

    /** Système externe qui a créé la trousse (addenda 1) : il reçoit les rappels de statut ; null si créée à l'écran. */
    @Column(name = "client_integration_id")
    private Long clientIntegrationId;

    /** Identifiant de l'objet d'origine chez l'appelant (ex. événement Workday), renvoyé dans chaque rappel. */
    @Column(name = "reference_externe", length = 120)
    private String referenceExterne;

    @Column(name = "lot_id")
    private Long lotId;

    /** Conservation (Cahier § 1.4.11, § 1.4.12 ; Loi sur les archives) : la durée court à partir de la clôture. */
    @Column(name = "cloture_le")
    private Instant clotureLe;

    @Column(name = "gel_legal", nullable = false)
    private boolean gelLegal;

    @Column(name = "gel_motif", length = 500)
    private String gelMotif;

    @Column(name = "versee_le")
    private Instant verseeLe;

    @Column(name = "detruite_le")
    private Instant detruiteLe;

    /** Bordereau de destruction (JSON) : éléments, date, autorité, méthode, résultat (addenda 3, Q48). */
    @Column(name = "bordereau_destruction", length = 20_000)
    private String bordereauDestruction;

    @Column(name = "sf_recupere_le")
    private Instant sfRecupereLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTrousse statut = StatutTrousse.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Identite.Risque risque = Identite.Risque.MOYEN;

    /** Priorité du dossier (AF-46, migration 080) : choisie, ou déduite du risque tant qu'aucune ne l'est. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioriteTrousse priorite = PrioriteTrousse.NORMALE;

    /** Rang de la priorité, conservé pour trier le suivi en base (le nom ne suit pas l'ordre). */
    @Column(name = "priorite_rang", nullable = false)
    private int prioriteRang = PrioriteTrousse.NORMALE.rang();

    @Column(name = "priorite_choisie", nullable = false)
    private boolean prioriteChoisie;

    @Column(nullable = false, length = 150)
    private String proprietaire;

    private LocalDate echeance;

    @Column(name = "modele_id")
    private Long modeleId;

    @Column(name = "valeurs_json", length = 100_000)
    private String valeursJson = "{}";

    @Column(name = "rappel_frequence_jours", nullable = false)
    private int rappelFrequenceJours = Relances.FREQUENCE_PAR_DEFAUT_JOURS;

    @Column(name = "rappels_max", nullable = false)
    private int rappelsMax = Relances.RAPPELS_PAR_DEFAUT;

    @Column(length = 1000)
    private String copies;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "maj_le", nullable = false)
    private Instant majLe;

    @Version
    private long version;

    protected Trousse() {
    }

    public Trousse(Long tenantId, String reference, String titre, String proprietaire, Instant maintenant) {
        this.tenantId = tenantId;
        this.reference = reference;
        this.titre = titre;
        this.proprietaire = proprietaire;
        this.creeLe = maintenant;
        this.majLe = maintenant;
    }

    public void changerStatut(StatutTrousse cible, Instant maintenant) {
        this.statut = statut.vers(cible);
        this.majLe = maintenant;
        if (cible.estFinal()) {
            this.clotureLe = maintenant;
        }
    }

    /** Gel légal : aucune destruction tant qu'il n'est pas levé, quelle que soit l'échéance. */
    public void geler(String motif) {
        if (motif == null || motif.isBlank() || motif.strip().length() > 500) {
            throw new IllegalArgumentException("Motif du gel légal : 1 à 500 caractères.");
        }
        exigerNonDetruite();
        this.gelLegal = true;
        this.gelMotif = motif.strip();
    }

    public void leverGel() {
        if (!gelLegal) {
            throw new IllegalStateException("Aucun gel légal sur cette trousse.");
        }
        this.gelLegal = false;
        this.gelMotif = null;
    }

    public void marquerVersee(Instant le) {
        exigerDestructible();
        this.verseeLe = le;
    }

    /**
     * Destruction à l'échéance : le titre et les valeurs, qui peuvent porter des renseignements personnels, sont effacés ;
     * la référence et le journal scellé demeurent, comme bordereau de destruction.
     */
    public void detruire(Instant le) {
        exigerDestructible();
        this.titre = "Trousse détruite — " + reference;
        this.valeursJson = "{}";
        this.copies = null;
        this.detruiteLe = le;
    }

    /** Le bordereau accompagne la destruction : il ne se consigne qu'une fois, sur une trousse détruite. */
    public void consignerBordereau(String bordereau) {
        if (detruiteLe == null || bordereauDestruction != null) {
            throw new IllegalStateException("Bordereau de destruction : trousse non détruite ou bordereau déjà consigné.");
        }
        this.bordereauDestruction = bordereau;
    }

    private void exigerDestructible() {
        exigerNonDetruite();
        // Une trousse close (terminée ou annulée) porte toujours sa date de clôture : changerStatut la fixe.
        if (!statut.estFinal()) {
            throw new IllegalStateException("Seule une trousse close (terminée ou annulée) suit le calendrier de conservation.");
        }
        if (gelLegal) {
            throw new IllegalStateException("Gel légal en vigueur : " + gelMotif);
        }
    }

    private void exigerNonDetruite() {
        if (detruiteLe != null) {
            throw new IllegalStateException("Trousse déjà détruite.");
        }
    }

    public Instant getClotureLe() { return clotureLe; }
    public boolean isGelLegal() { return gelLegal; }
    public String getGelMotif() { return gelMotif; }
    public Instant getVerseeLe() { return verseeLe; }
    public Instant getDetruiteLe() { return detruiteLe; }
    public String getBordereauDestruction() { return bordereauDestruction; }

    /** Brouillon ou parcours en cours : on peut encore ajouter un signataire ou modifier un document (nouveau cycle). */
    public void exigerOuverte() {
        if (statut != StatutTrousse.BROUILLON && statut != StatutTrousse.EN_TRAITEMENT) {
            throw new IllegalStateException("La trousse est close (statut " + statut + ").");
        }
    }

    public boolean enTraitement() {
        return statut == StatutTrousse.EN_TRAITEMENT;
    }

    public void exigerModifiable() {
        if (!statut.estModifiable()) {
            throw new IllegalStateException("La trousse n'est plus modifiable (statut " + statut + ").");
        }
    }

    public void toucher(Instant maintenant) {
        this.majLe = maintenant;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getReference() { return reference; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    /** Rattache la trousse au système externe qui l'a demandée, seul destinataire de ses rappels. */
    public void rattacherExterne(Source origine, Long clientIntegrationId, String referenceExterne, Long lotId) {
        if (referenceExterne != null && referenceExterne.strip().length() > 120) {
            throw new IllegalArgumentException("Référence externe : 120 caractères au plus.");
        }
        if (source == Source.MANUELLE) {
            this.source = origine;
        }
        this.clientIntegrationId = clientIntegrationId;
        this.referenceExterne = referenceExterne == null || referenceExterne.isBlank() ? null : referenceExterne.strip();
        this.lotId = lotId;
    }

    public Long getClientIntegrationId() { return clientIntegrationId; }
    public String getReferenceExterne() { return referenceExterne; }
    public Long getLotId() { return lotId; }
    public String getSfDossierId() { return sfDossierId; }
    public void setSfDossierId(String sfDossierId) { this.sfDossierId = sfDossierId; }
    public Instant getSfRecupereLe() { return sfRecupereLe; }
    public void setSfRecupereLe(Instant sfRecupereLe) { this.sfRecupereLe = sfRecupereLe; }
    public StatutTrousse getStatut() { return statut; }
    public Identite.Risque getRisque() { return risque; }
    /** Le risque change : une priorité qui n'a pas été choisie le suit. */
    public void setRisque(Identite.Risque risque) {
        this.risque = risque;
        if (!prioriteChoisie) {
            fixerPriorite(PrioriteTrousse.deduite(risque));
        }
    }

    /** Priorité choisie par l'initiateur ; nulle, la trousse revient à la priorité déduite de son risque (AF-46). */
    public void prioriser(PrioriteTrousse choisie) {
        this.prioriteChoisie = choisie != null;
        fixerPriorite(choisie != null ? choisie : PrioriteTrousse.deduite(risque));
    }

    private void fixerPriorite(PrioriteTrousse p) {
        this.priorite = p;
        this.prioriteRang = p.rang();
    }

    public PrioriteTrousse getPriorite() { return priorite; }
    public boolean isPrioriteChoisie() { return prioriteChoisie; }

    /** Rappels de la trousse, rapprochés selon sa priorité. */
    public Relances.Cadence cadenceRappels() {
        return new Relances.Cadence(rappelFrequenceJours, rappelsMax, priorite);
    }
    /**
     * Départ ou changement de responsable (P3) : une trousse ouverte change de propriétaire ; une trousse close garde le
     * sien, qui fait partie de sa preuve. Retourne l'ancien propriétaire, pour la trace.
     */
    public String transfererA(String nouveau, Instant maintenant) {
        if (statut.estFinal()) {
            throw new IllegalStateException("Trousse close : son propriétaire ne change plus (" + reference + ").");
        }
        String cible = nouveau == null ? "" : nouveau.trim().toLowerCase(java.util.Locale.ROOT);
        if (cible.isEmpty() || cible.equalsIgnoreCase(proprietaire)) {
            throw new IllegalArgumentException("Nouveau propriétaire absent ou identique à l'actuel.");
        }
        String ancien = proprietaire;
        proprietaire = cible;
        majLe = maintenant;
        return ancien;
    }

    public String getProprietaire() { return proprietaire; }
    public LocalDate getEcheance() { return echeance; }
    public void setEcheance(LocalDate echeance) { this.echeance = echeance; }
    public Long getModeleId() { return modeleId; }
    public void setModeleId(Long modeleId) { this.modeleId = modeleId; }
    public String getValeursJson() { return valeursJson; }
    public void setValeursJson(String valeursJson) { this.valeursJson = valeursJson; }
    public int getRappelFrequenceJours() { return rappelFrequenceJours; }
    public void setRappelFrequenceJours(int v) { this.rappelFrequenceJours = v; }
    public int getRappelsMax() { return rappelsMax; }
    public void setRappelsMax(int v) { this.rappelsMax = v; }
    public String getCopies() { return copies; }
    public void setCopies(String copies) { this.copies = copies; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getMajLe() { return majLe; }
}
