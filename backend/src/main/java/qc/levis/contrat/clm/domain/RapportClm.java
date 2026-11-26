package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rapport enregistré par l'administration d'une organisation (Annexe 10, AF-37 ; ADR-032) : un type, une période glissante,
 * des filtres (unité, type de contrat, statut) et les colonnes retenues, dans l'ordre voulu. Le calcul des délais est fait
 * ici, à partir du journal scellé de la trousse, pour qu'il soit le même à l'écran et dans chaque export.
 */
@Entity
@Table(name = "clm_rapport")
public class RapportClm {

    public static final int NOM_MAX = 120;
    public static final int PERIODE_MAX_JOURS = 366;

    /** Chaque type annonce ses colonnes ; un rapport en retient tout ou partie. */
    public enum Type {
        SIGNATURES_EN_ATTENTE("reference", "titre", "unite", "typeContrat", "etape", "enAttenteDepuis", "echeance", "joursAttente"),
        DELAIS("reference", "titre", "unite", "typeContrat", "lanceeLe", "termineeLe", "delaiHeures"),
        DELAIS_ETAPES("etape", "nombre", "delaiMoyenHeures"),
        COMPLETES("reference", "titre", "unite", "typeContrat", "termineeLe"),
        REJETS("reference", "titre", "unite", "typeContrat", "rejeteeLe", "motif"),
        EXPIRATIONS("reference", "titre", "unite", "etape", "depasseeLe"),
        UTILISATION("indicateur", "valeur"),
        VOLUMETRIE("mois", "creees", "lancees", "terminees", "rejetees"),
        INCIDENTS("date", "categorie", "reference", "detail");

        private final List<String> colonnes;

        Type(String... colonnes) {
            this.colonnes = List.of(colonnes);
        }

        public List<String> colonnes() {
            return colonnes;
        }
    }

    /** Définition saisie ; {@code colonnes} vide = toutes, dans l'ordre du type. */
    public record Definition(String nom, Type type, int periodeJours, String unite, String typeContrat, String statut, List<String> colonnes) {
        public Definition {
            nom = nom == null ? null : nom.strip();
            unite = vide(unite);
            typeContrat = vide(typeContrat);
            statut = vide(statut);
            colonnes = colonnes == null ? List.of() : colonnes.stream().filter(c -> c != null && !c.isBlank()).map(String::strip).toList();
        }

        public List<String> erreurs() {
            List<String> erreurs = new ArrayList<>();
            if (nom == null || nom.isEmpty() || nom.length() > NOM_MAX) {
                erreurs.add("Nom du rapport : 1 à " + NOM_MAX + " caractères.");
            }
            if (type == null) {
                erreurs.add("Type de rapport requis.");
                return erreurs;
            }
            if (periodeJours < 1 || periodeJours > PERIODE_MAX_JOURS) {
                erreurs.add("Période : de 1 à " + PERIODE_MAX_JOURS + " jours.");
            }
            colonnes.stream().filter(c -> !type.colonnes().contains(c))
                    .forEach(c -> erreurs.add("Colonne « " + c + " » inconnue pour ce type de rapport."));
            return erreurs;
        }

        /** Colonnes affichées et exportées : celles demandées, sinon toutes celles du type. */
        public List<String> colonnesRetenues() {
            return colonnes.isEmpty() ? type.colonnes() : colonnes;
        }

        private static String vide(String v) {
            return v == null || v.isBlank() ? null : v.strip();
        }
    }

    /** Événement du journal scellé, réduit à ce que le calcul des délais exige. */
    public record Evenement(String type, String detail, Instant horodatage) {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = NOM_MAX)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type type;

    @Column(name = "periode_jours", nullable = false)
    private int periodeJours;

    @Column(length = 80)
    private String unite;

    @Column(name = "type_contrat", length = 80)
    private String typeContrat;

    @Column(length = 20)
    private String statut;

    @Column(length = 500)
    private String colonnes;

    @Column(nullable = false, length = 150)
    private String proprietaire;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected RapportClm() {
    }

    public RapportClm(Long tenantId, String proprietaire) {
        this.tenantId = tenantId;
        this.proprietaire = proprietaire;
    }

    /** Définition préalablement validée par {@link Definition#erreurs()}. */
    public void definir(Definition d, Instant le) {
        nom = d.nom();
        type = d.type();
        periodeJours = d.periodeJours();
        unite = d.unite();
        typeContrat = d.typeContrat();
        statut = d.statut();
        colonnes = String.join(",", d.colonnes());
        modifieLe = le;
    }

    public Definition definition() {
        return new Definition(nom, type, periodeJours, unite, typeContrat, statut,
                colonnes == null || colonnes.isEmpty() ? List.of() : Arrays.asList(colonnes.split(",")));
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getProprietaire() { return proprietaire; }
    public Instant getModifieLe() { return modifieLe; }

    // --- Délais (AF-37 : délais moyens) --------------------------------------------------------------------------------

    /** Délai de traitement : du premier lancement à l'achèvement ; absent si la trousse n'a pas été lancée puis terminée. */
    public static Optional<Duration> delaiTraitement(List<Evenement> journal) {
        Instant lancee = premier(journal, "TROUSSE_LANCEE");
        Instant terminee = premier(journal, "TROUSSE_TERMINEE");
        return lancee == null || terminee == null ? Optional.empty() : Optional.of(Duration.between(lancee, terminee));
    }

    /** Délai de chaque étape décidée : de son activation à la décision qui la clôt, par libellé d'étape. */
    public static Map<String, List<Duration>> delaisEtapes(List<Evenement> journal) {
        Map<String, Instant> activees = new LinkedHashMap<>();
        Map<String, List<Duration>> delais = new LinkedHashMap<>();
        for (Evenement e : journal) {
            if ("ETAPE_ACTIVEE".equals(e.type())) {
                activees.put(e.detail(), e.horodatage());
            } else if (("ETAPE_APPROUVEE".equals(e.type()) || "ETAPE_REJETEE".equals(e.type())) && activees.containsKey(e.detail())) {
                delais.computeIfAbsent(e.detail(), k -> new ArrayList<>()).add(Duration.between(activees.remove(e.detail()), e.horodatage()));
            }
        }
        return delais;
    }

    /** Moyenne exprimée en heures, à une décimale ; une liste vide n'a pas de moyenne. */
    public static Optional<Double> moyenneHeures(Collection<Duration> delais) {
        if (delais.isEmpty()) {
            return Optional.empty();
        }
        double minutes = delais.stream().mapToLong(Duration::toMinutes).average().orElseThrow();
        return Optional.of(Math.round(minutes / 6.0) / 10.0);
    }

    public static double heures(Duration d) {
        return Math.round(d.toMinutes() / 6.0) / 10.0;
    }

    private static Instant premier(List<Evenement> journal, String type) {
        return journal.stream().filter(e -> type.equals(e.type())).map(Evenement::horodatage).findFirst().orElse(null);
    }
}
